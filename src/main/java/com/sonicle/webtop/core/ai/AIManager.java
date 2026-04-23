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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

/**
 * Base for backend-specific AI managers. Instances are cached per
 * {@link com.sonicle.webtop.core.app.WebTopSession} and reused across
 * multiple requests, so any state that varies per call must travel in
 * {@link AIRequestConfig} rather than in instance fields.
 *
 * Instance state held here is session-stable: backend URL, API token,
 * model, locale, user id, and per-backend defaults (temperature,
 * useTemperature flag, connection timeouts) that the config can override
 * but doesn't have to.
 *
 * @author gabriele.bulfon
 */
public abstract class AIManager {

	protected String model;
	protected String role = "user";
	protected int defaultMaxTokens;
	protected float defaultTemperature = 0.7f;
	protected boolean stream = false;

	protected String apiUrl;
	protected String apiCompletionPath = "/v1/chat/completions";
	protected String apiRAGAskPath;
	protected String apiToken;
	protected boolean useTemperature = true;
	protected String userId;

	protected int connectionTimeout = 200_000;
	protected int readTimeout = 200_000;

	protected Locale locale;

	public AIManager(String apiUrl, String apiToken, Locale locale) {
		this.locale = locale;
		this.apiUrl = apiUrl;
		this.apiToken = apiToken;
	}

	/**
	 * Builds the system-prompt preamble on every call so the "Today is ..."
	 * date stays current across long-lived cached managers (a session can
	 * span midnight).
	 */
	protected String buildGlobalSystemPrompt() {
		return "Today is "+DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH).format(new java.util.Date())+".\n"+
			"Always answer in "+locale.getDisplayLanguage(Locale.ENGLISH)+", unless the user explicitly requests a specific translation or another language.\n"+
			"Return only one answer and only in the requested format, or in plain text if not specified.\n\n";
	}

	public boolean hasRag() {
		return false;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getModel() {
		return model;
	}

	public void setUseTemperature(boolean useTemperature) {
		this.useTemperature = useTemperature;
	}

	public boolean getUseTemperature() {
		return useTemperature;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public static String hashUserId(String stringId) {
		if (StringUtils.isBlank(stringId)) return null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(("wt:" + stringId).getBytes(StandardCharsets.UTF_8));
			return Hex.encodeHexString(digest);
		} catch (Exception e) {
			return null;
		}
	}

	public String prompt(String prompt, AIRequestConfig cfg) throws WTException {
		if (cfg == null) cfg = AIRequestConfig.DEFAULT;
		return callCompletionsAPI(buildGlobalSystemPrompt() + prompt, cfg);
	}

	public String askRAG(String question, String instructions, String iso_datestart, String iso_dateend, List<String> relevantFolders, AIRequestConfig cfg) throws WTException {
		if (cfg == null) cfg = AIRequestConfig.DEFAULT;
		instructions = (instructions == null ? "" : instructions);
		return callRAGAPI(question, instructions, iso_datestart, iso_dateend, relevantFolders, cfg);
	}

	protected String callCompletionsAPI(String prompt, AIRequestConfig cfg) throws WTException {
		String outputFormat = cfg.getOutputFormat();
		if (!StringUtils.isBlank(outputFormat) && !StringUtils.equalsIgnoreCase("text", outputFormat)) {
			String formatPrompt =
				"Format the output only in " + outputFormat + " format.\n"+
				"Please return your answer with only your raw "+ outputFormat + " content, without wrapping it in Markdown code blocks (no ```" + outputFormat+ " or other delimiters).\n";
			prompt = formatPrompt + prompt;
		}
		System.out.println("PROMPT: "+prompt);

		com.google.gson.Gson gson = new com.google.gson.Gson();

		// Create the 'messages' array
		com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
		com.google.gson.JsonObject message = new com.google.gson.JsonObject();
		message.addProperty("role", role);
		message.addProperty("content", prompt);
		messages.add(message);

		// Create the full payload
		com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
		payload.addProperty("model", model);
		payload.add("messages", messages);
		if (useTemperature) {
			float t = cfg.getTemperature() != null ? cfg.getTemperature() : defaultTemperature;
			payload.addProperty("temperature", t);
		}
		int mt = cfg.getMaxTokens() != null ? cfg.getMaxTokens() : defaultMaxTokens;
		if (mt > 0) payload.addProperty("max_tokens", mt);
		payload.addProperty("stream", stream);
		if (!StringUtils.isBlank(userId)) payload.addProperty("user", userId);

		// Convert to JSON string
		String jsonPayload = gson.toJson(payload);

		try {
			String jsonResponse = getAnswer(new URL(apiUrl+apiCompletionPath), jsonPayload, apiToken);
			return AIOutputSanitizer.sanitizeByFormat(extractAssistantReply(jsonResponse), outputFormat);
		} catch(Exception exc) {
			exc.printStackTrace();
			throw new WTException("AI backend request failed");
		}
	}

	protected String getAnswer(URL url, String jsonPayload) throws Exception {
		return getAnswer(url, jsonPayload, null);
	}

	protected String getAnswer(URL url, String jsonPayload, String token) throws Exception {
		String answer;
		// Create connection
		java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

		// Configure request
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(connectionTimeout);
		conn.setReadTimeout(readTimeout);
		conn.setDoOutput(true); // We will send data
		conn.setRequestProperty("Content-Type", "application/json");
		if (token!=null) conn.setRequestProperty("Authorization", "Bearer "+token);

		// Write payload
		OutputStream os = conn.getOutputStream();
		os.write(jsonPayload.getBytes("UTF-8"));
		os.flush();
		os.close();

		// Read response
		int responseCode = conn.getResponseCode();

		answer = conn.getResponseMessage();
		if (responseCode == 200) {
			Scanner scanner = new Scanner(
				conn.getInputStream(), "UTF-8"
			).useDelimiter("\\A");

			answer = scanner.hasNext() ? scanner.next() : "";

			scanner.close();
		}
		conn.disconnect();
		if (responseCode != 200) throw new Exception("Error "+responseCode+" : "+answer);
		return answer;
	}

	abstract String callRAGAPI(String question, String instructions, String iso_datestart, String iso_dateend, List<String> relevantFolders, AIRequestConfig cfg) throws WTException;

	private static String extractAssistantReply(String jsonResponse) {
		try {
			JsonObject root = new JsonParser().parse(jsonResponse).getAsJsonObject();
			JsonArray choices = root.getAsJsonArray("choices");

			if (choices != null && choices.size() > 0) {
				JsonObject firstChoice = choices.get(0).getAsJsonObject();
				JsonObject message = firstChoice.getAsJsonObject("message");
				return message.get("content").getAsString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	protected static List<String> extractBodiesFromQdrantResponse(String jsonResponse) {
		List<String> bodies = new ArrayList<>();

		try {
			JsonObject root = new JsonParser().parse(jsonResponse).getAsJsonObject();
			JsonArray results = root.getAsJsonArray("result");

			if (results != null) {
				for (JsonElement resultElem : results) {
					JsonObject resultObj = resultElem.getAsJsonObject();
					JsonObject payload = resultObj.getAsJsonObject("payload");
					if (payload != null && payload.has("body")) {
						bodies.add(payload.get("body").getAsString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bodies;
	}

	protected static JsonArray extractEmbeddingAsJsonArray(String jsonResponse) {
		JsonArray array = new JsonArray();

		try {
			JsonObject root = new JsonParser().parse(jsonResponse).getAsJsonObject();
			JsonArray data = root.getAsJsonArray("data");
			if (data != null && data.size() > 0) {
				JsonObject firstItem = data.get(0).getAsJsonObject();
				JsonArray embedding = firstItem.getAsJsonArray("embedding");
				for (JsonElement el : embedding) {
					array.add(el.getAsFloat());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return array;
	}


}
