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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author gabriele.bulfon
 */
public class OllamaAIManager extends AIManager {
	
	String embeddingModel = "bge-m3";
	String apiEmbeddingPath = "/api/embeddings";
	
	String qdrantHost = "192.168.222.221";
	String qdrantURL = "http://"+qdrantHost+":6333/collections/emails/points/search";
	int qdrantLimit = 10;
	
	public OllamaAIManager(String apiToken, Locale locale) {
		super("http://localhost:11434", null, locale);
		model = "gpt-oss:20b";
	} 
	
	public boolean hasRag() {
		return true;
	}
	
	public String callRAGAPI(String question, String instructions, String iso_datestart, String iso_dateend, List<String> relevantFolders, AIRequestConfig cfg) throws WTException {
        com.google.gson.Gson gson = new com.google.gson.Gson();

        // Create the full payload for embedding
        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("prompt", question);
        payload.addProperty("model", embeddingModel);
        // Convert to JSON string
        String jsonPayload = gson.toJson(payload);

		String answer;
		try {
			String response = getAnswer(new URL(apiUrl+apiEmbeddingPath), jsonPayload, apiToken);
            // Parse JSON
            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
            JsonArray embeddingVector = root.getAsJsonArray("embedding");

			//Qdrant api call
			gson = new com.google.gson.Gson();
			payload = new com.google.gson.JsonObject();
			payload.add("vector", embeddingVector);
			payload.addProperty("limit", qdrantLimit);
			payload.addProperty("with_payload", true);
			// Convert to JSON string
			jsonPayload = gson.toJson(payload);

			String qdrantResponse = getAnswer(new URL(qdrantURL), jsonPayload);

			//build prompt from qdrant bodies
			List<String> bodyChunks = extractBodiesFromQdrantResponse(qdrantResponse);
			StringBuilder bodies = new StringBuilder();
			for (String body : bodyChunks) {
				bodies.append(body).append("\n\n=========================================\n\n");
			}
			String prompt = instructions + "\n\n" + question + "\n\nUse this list of emails:\n\n"+bodies;
			answer = prompt(prompt, cfg);
		} catch(WTException wtexc) {
			throw wtexc;
		} catch(Exception exc) {
			exc.printStackTrace();
			throw new WTException("AI RAG request failed");
		}

		return answer;
	};
	
}