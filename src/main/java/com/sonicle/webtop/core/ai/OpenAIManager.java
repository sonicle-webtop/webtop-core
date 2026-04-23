/*
 * Copyright (C) 2025 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sonicle.webtop.core.sdk.WTException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author gabriele.bulfon
 */
public class OpenAIManager extends AIManager {

	String embeddingModel;
	String apiEmebeddingUrl;
	String apiEmbeddingPath;
	String apiEmbeddingToken;

	boolean useOpenAIEmbedding=true;

	public OpenAIManager(String apiToken, Locale locale) {
		super("https://api.openai.com", apiToken, locale);
		model = "gpt-4.1-mini";
		//model = "gpt-4o";
		//model = "gpt-5-mini";
		//model = "gpt-5-nano";
		// gpt-4.1-mini / gpt-4o honor temperature; gpt-5-* reject it.
		// Flip to false (or call setUseTemperature(false)) when switching to a gpt-5-* model.
		useTemperature = true;

		//OpenAI embedding
		if (useOpenAIEmbedding) {
			embeddingModel = "text-embedding-3-large";
			apiEmebeddingUrl = apiUrl;
			apiEmbeddingPath = "/v1/embeddings";
			apiEmbeddingToken = this.apiToken;
		} else {
			//local bge-m3 embedding
			embeddingModel = "bge-m3";
			apiEmebeddingUrl = "http://localhost:11434";
			apiEmbeddingPath = "/api/embeddings";
			apiEmbeddingToken = null;
		}

	}

	public boolean hasRag() {
		return true;
	}

	private String buildQdrantURL(Map<String, String> ragProperties) {
		String host = ragProperties.get("host");
		String port = ragProperties.get("port");
		String user_id = ragProperties.get("user_id");
		String document = ragProperties.get("document");
		if (StringUtils.isBlank(host)) host = "localhost";
		if (StringUtils.isBlank(port)) port = "6333";
		if (StringUtils.isBlank(user_id)) user_id = "";
		if (StringUtils.isBlank(document)) document = "emails";
		String context = document + "_" + user_id;
		return "http://"+host+":"+port+"/collections/"+context+"/points/search";
	}

	public String callRAGAPI(String question, String instructions, String iso_datestart, String iso_dateend, List<String> relevantFolders, AIRequestConfig cfg) throws WTException {

		String answer;
		try {
			Map<String, String> ragProperties = cfg.getRagProperties();
			JsonArray embeddingVector = getEmbeddingVector(question);

			//Qdrant api call
			String qdrantURL = buildQdrantURL(ragProperties);
			String sQdrantLimit = ragProperties.get("limit");
			int qdrantLimit = 20;
			if (!StringUtils.isAllBlank(sQdrantLimit)) qdrantLimit = Integer.parseInt(sQdrantLimit);
			com.google.gson.Gson gson = new com.google.gson.Gson();
			com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
			payload.add("vector", embeddingVector);
			payload.addProperty("limit", qdrantLimit);
			payload.addProperty("with_payload", true);

			JsonArray mustArray = null;
			JsonArray shouldArray = null;
			// Filtro range su campo date_iso
			if (iso_datestart!=null && iso_dateend!=null) {
				JsonObject rangeObj = new JsonObject();
				rangeObj.addProperty("gte", iso_datestart.substring(0, 10));
				rangeObj.addProperty("lte", iso_dateend.substring(0, 10));

				// Nuovo oggetto field per date_iso
				JsonObject dateField = new JsonObject();
				dateField.addProperty("key", "date_iso");
				dateField.add("range", rangeObj);

				mustArray = new JsonArray();
				mustArray.add(dateField);
			}

			if (relevantFolders!=null && relevantFolders.size()>0) {
				shouldArray = new JsonArray();

				for (String folder : relevantFolders) {
					JsonObject matchValue = new JsonObject();
					matchValue.addProperty("value", folder);

					JsonObject match = new JsonObject();
					match.addProperty("key", "folder");
					match.add("match", matchValue);

					shouldArray.add(match);
				}
			}

			JsonObject queryFilter = null;
			if (mustArray != null) {
				if (queryFilter == null) queryFilter = new JsonObject();
				queryFilter.add("must", mustArray);
			}

			if (shouldArray != null) {
				if (queryFilter == null) queryFilter = new JsonObject();
				queryFilter.add("should", shouldArray);
			}

			if (queryFilter != null)
				payload.add("filter", queryFilter);

			// Convert to JSON string
			String jsonPayload = gson.toJson(payload);
			System.out.println("====QDRANT PAYLOAD===");
			System.out.println(jsonPayload);
			System.out.println("=======END===========");

			String qdrantResponse = getAnswer(new URL(qdrantURL), jsonPayload);

			//build prompt from qdrant bodies
			List<String> bodyChunks = extractBodiesFromQdrantResponse(qdrantResponse);
			StringBuilder bodies = new StringBuilder();
			for (String body : bodyChunks) {
				bodies.append("\n\n=== START DOCUMENT===\n\n").append(body).append("\n\n=== END DOCUMENT===\n\n");
			}
			String prompt = instructions + "\n\n" + question +
					"\n\nUse this list of documents, where each document starts with === START DOCUMENT=== and ends with === END DOCUMENT===:\n\n"
					+bodies;
			answer = prompt(prompt, cfg);
		} catch(WTException wtexc) {
			throw wtexc;
		} catch(Exception exc) {
			exc.printStackTrace();
			throw new WTException("AI RAG request failed");
		}

		return answer;
	}

	public String getOpenAIEmebeddingPayload(String question) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        // Create the full payload for embedding
        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("input", question);
        payload.addProperty("model", embeddingModel);
        if (!StringUtils.isBlank(userId)) payload.addProperty("user", userId);

        // Convert to JSON string
        return gson.toJson(payload);
	}


	public String getBGEEmebeddingPayload(String question) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        // Create the full payload for embedding
        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("prompt", question);
        payload.addProperty("model", embeddingModel);

        // Convert to JSON string
        return gson.toJson(payload);
	}

	public String getEmebeddingPayload(String question) {
		if (useOpenAIEmbedding) return getOpenAIEmebeddingPayload(question);
		else return getBGEEmebeddingPayload(question);
	}

	public String getEmbeddingAnswer(String question) throws Exception {
        String jsonPayload = getEmebeddingPayload(question);
		return getAnswer(new URL(apiEmebeddingUrl+apiEmbeddingPath), jsonPayload, apiEmbeddingToken);
	}

	public JsonArray getEmbeddingVector(String question) throws Exception {
		if (useOpenAIEmbedding) {
			return getOpenAIEmbeddingVector(question);
		} else {
			return getBGEEmbeddingVector(question);
		}
	}

	public JsonArray getOpenAIEmbeddingVector(String question) throws Exception {
			String response = getEmbeddingAnswer(question);
			return extractEmbeddingAsJsonArray(response);
	}

	public JsonArray getBGEEmbeddingVector(String question) throws Exception {
		String response = getEmbeddingAnswer(question);
		// Parse JSON
		JsonObject root = new JsonParser().parse(response).getAsJsonObject();
		return root.getAsJsonArray("embedding");
	}


}
