/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.pbx;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * NethVoice chat (chat-island): logs the WebTop user in on the NethVoice CTI
 * middleware and returns the JWT the chat uses as its XMPP password.
 * WebTop and NethVoice share the same user base, so the WebTop credentials are
 * the CTI ones.
 */
public class NethVoiceChat {
	private static final String API_LOGIN = "/api/login";
	private static final String API_OPERATORS = "/api/user/endpoints/all";
	
	private final String ctiHost;
	
	public NethVoiceChat(String ctiHost) {
		this.ctiHost = ctiHost;
	}
	
	public String getCtiHost() {
		return ctiHost;
	}
	
	/**
	 * The CTI operators (the chat's people): username to display name.
	 * @param token A CTI token.
	 * @return The operators, by username.
	 * @throws WTException When the token is refused (an {@link Unauthorized}) or the CTI cannot be reached.
	 */
	public Map<String, String> listOperators(String token) throws WTException {
		try {
			HttpResponse<String> resp = Unirest.get("https://" + ctiHost + API_OPERATORS)
				.header("Authorization", "Bearer " + token)
				.asString();
			if (resp.getStatus() == 401 || resp.getStatus() == 403) throw new Unauthorized();
			if (resp.getStatus() != 200) throw new WTException("NethVoice CTI operators failed [{}]", resp.getStatus());
			JsonObject json = JsonResult.gson().fromJson(resp.getBody(), JsonObject.class);
			LinkedHashMap<String, String> users = new LinkedHashMap<>();
			if (json != null) for (Map.Entry<String, com.google.gson.JsonElement> e : json.entrySet()) {
				JsonObject op = e.getValue().isJsonObject() ? e.getValue().getAsJsonObject() : null;
				String name = (op != null && op.has("name") && !op.get("name").isJsonNull()) ? op.get("name").getAsString() : null;
				users.put(e.getKey(), StringUtils.defaultIfBlank(name, e.getKey()));
			}
			return users;
			
		} catch (WTException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new WTException(ex, "NethVoice CTI operators failed");
		}
	}
	
	/** The CTI refused the token: log in again. */
	public static class Unauthorized extends WTException {
		public Unauthorized() {
			super("NethVoice CTI token refused");
		}
	}
	
	/**
	 * Logs in on the CTI and returns its JWT.
	 * @param username The CTI username (the WebTop user ID).
	 * @param password The user password.
	 * @return The CTI token.
	 * @throws WTException When the CTI refuses the login or cannot be reached.
	 */
	public String login(String username, char[] password) throws WTException {
		if (StringUtils.isBlank(ctiHost)) throw new WTException("NethVoice CTI host not configured");
		if (password == null) throw new WTException("User password not available");
		try {
			JsonObject body = new JsonObject();
			body.addProperty("username", username);
			body.addProperty("password", new String(password));
			HttpResponse<String> resp = Unirest.post("https://" + ctiHost + API_LOGIN)
				.header("Content-Type", "application/json")
				.body(body.toString())
				.asString();
			if (resp.getStatus() != 200) throw new WTException("NethVoice CTI login refused [{}]", resp.getStatus());
			JsonObject json = JsonResult.gson().fromJson(resp.getBody(), JsonObject.class);
			String token = (json != null && json.has("token")) ? json.get("token").getAsString() : null;
			if (StringUtils.isBlank(token)) throw new WTException("NethVoice CTI login returned no token");
			return token;
			
		} catch (WTException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new WTException(ex, "NethVoice CTI login failed");
		}
	}
}
