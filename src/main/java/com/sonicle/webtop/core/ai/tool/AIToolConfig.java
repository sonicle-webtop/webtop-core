/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.ai.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable AI tool configuration parsed from the bundled ai-tool.json.
 *
 * All user-visible strings (labels, prompts, input questions, dialog title,
 * format hints, no-selection error messages) are carried inline as ISO-639
 * language maps; resolution against the user's locale happens via
 * {@link #resolve(Map, String)}.
 *
 * Responsibilities:
 *  - Parse the JSON tree into AIToolItem/AIToolInputSpec instances.
 *  - Build a flat id→item index for O(1) lookup during AIPrompt dispatch.
 *  - Refuse duplicate ids; refuse leaves without a prompt map.
 */
public final class AIToolConfig {

	public static final String DEFAULT_LANGUAGE_FALLBACK = "en";

	private final String defaultLanguage;
	private final Map<String, String> dialogTitle;
	private final Map<String, String> minimalHtmlFormatHint;
	private final List<AIToolItem> items;
	private final Map<String, AIToolItem> index;

	private AIToolConfig(
			String defaultLanguage,
			Map<String, String> dialogTitle,
			Map<String, String> minimalHtmlFormatHint,
			List<AIToolItem> items,
			Map<String, AIToolItem> index) {
		this.defaultLanguage = defaultLanguage;
		this.dialogTitle = Collections.unmodifiableMap(dialogTitle);
		this.minimalHtmlFormatHint = Collections.unmodifiableMap(minimalHtmlFormatHint);
		this.items = Collections.unmodifiableList(items);
		this.index = Collections.unmodifiableMap(index);
	}

	public String getDefaultLanguage() { return defaultLanguage; }
	public Map<String, String> getDialogTitle() { return dialogTitle; }
	public Map<String, String> getMinimalHtmlFormatHint() { return minimalHtmlFormatHint; }
	public List<AIToolItem> getItems() { return items; }

	public AIToolItem findById(String id) {
		return id == null ? null : index.get(id);
	}

	/**
	 * Pick the entry for {@code lang}; fall back to the configured default
	 * language; fall back to any available entry. Returns null only if the
	 * map is null or empty.
	 */
	public String resolve(Map<String, String> map, String lang) {
		if (map == null || map.isEmpty()) return null;
		if (lang != null) {
			String v = map.get(lang);
			if (v != null) return v;
		}
		String v = map.get(defaultLanguage);
		if (v != null) return v;
		return map.values().iterator().next();
	}

	public static AIToolConfig load(InputStream in) throws IOException {
		if (in == null) throw new IOException("ai-tool.json resource not found");
		JsonElement root;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			root = new JsonParser().parse(r);
		}
		if (root == null || !root.isJsonObject()) {
			throw new IOException("ai-tool.json: expected an object at root");
		}
		JsonObject rootObj = root.getAsJsonObject();

		String defaultLanguage = getString(rootObj, "defaultLanguage", DEFAULT_LANGUAGE_FALLBACK);
		Map<String, String> dialogTitle = parseLangMap(rootObj, "dialogTitle", false, null);
		Map<String, String> minimalHtmlHint = new LinkedHashMap<>();
		if (rootObj.has("formatHints") && rootObj.get("formatHints").isJsonObject()) {
			JsonObject fh = rootObj.getAsJsonObject("formatHints");
			minimalHtmlHint = parseLangMap(fh, "minimalHtml", false, null);
		}

		JsonElement itemsEl = rootObj.get("items");
		if (itemsEl == null || !itemsEl.isJsonArray()) {
			throw new IOException("ai-tool.json: missing 'items' array");
		}
		List<AIToolItem> parsed = new ArrayList<>();
		Map<String, AIToolItem> idx = new HashMap<>();
		for (JsonElement el : itemsEl.getAsJsonArray()) {
			parsed.add(parseItem(el, idx));
		}
		return new AIToolConfig(defaultLanguage, dialogTitle, minimalHtmlHint, parsed, idx);
	}

	private static AIToolItem parseItem(JsonElement el, Map<String, AIToolItem> idx) throws IOException {
		if (el == null || !el.isJsonObject()) {
			throw new IOException("ai-tool.json: item must be an object");
		}
		JsonObject o = el.getAsJsonObject();
		String id = getString(o, "id", null);
		if (id == null || id.isEmpty()) throw new IOException("ai-tool.json: item missing 'id'");
		Map<String, String> label = parseLangMap(o, "label", true, "item '" + id + "'");

		List<AIToolItem> children = new ArrayList<>();
		if (o.has("children") && o.get("children").isJsonArray()) {
			JsonArray arr = o.getAsJsonArray("children");
			for (JsonElement cel : arr) children.add(parseItem(cel, idx));
		}

		AIToolMode mode = null;
		Map<String, String> prompt = null;
		AIToolInputSpec input = null;
		boolean requiresSelection = false;
		Map<String, String> noSelectionError = null;

		if (children.isEmpty()) {
			mode = AIToolMode.parse(getString(o, "mode", null), AIToolMode.INSERT);
			prompt = parseLangMap(o, "prompt", true, "leaf '" + id + "'");
			requiresSelection = getBoolean(o, "requiresSelection", false);
			noSelectionError = parseLangMap(o, "noSelectionError", false, "leaf '" + id + "'");
			if (o.has("input") && o.get("input").isJsonObject()) {
				JsonObject ino = o.getAsJsonObject("input");
				Map<String, String> question = parseLangMap(ino, "question", true, "input of '" + id + "'");
				boolean multiline = getBoolean(ino, "multiline", false);
				boolean required = getBoolean(ino, "required", true);
				input = new AIToolInputSpec(question, multiline, required);
			}
		}

		AIToolItem item = new AIToolItem(id, label, mode, prompt, input, requiresSelection, noSelectionError, children);
		if (idx.put(id, item) != null) {
			throw new IOException("ai-tool.json: duplicate id '" + id + "'");
		}
		return item;
	}

	private static Map<String, String> parseLangMap(JsonObject parent, String field, boolean required, String ownerDesc) throws IOException {
		JsonElement el = parent.get(field);
		if (el == null || el.isJsonNull()) {
			if (required) throw new IOException("ai-tool.json: " + ownerDesc + " missing '" + field + "' map");
			return new LinkedHashMap<>();
		}
		if (!el.isJsonObject()) {
			throw new IOException("ai-tool.json: '" + field + "' must be a {lang: string} object"
					+ (ownerDesc == null ? "" : " (" + ownerDesc + ")"));
		}
		Map<String, String> out = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
			JsonElement v = e.getValue();
			if (v == null || v.isJsonNull()) continue;
			out.put(e.getKey(), v.getAsString());
		}
		if (required && out.isEmpty()) {
			throw new IOException("ai-tool.json: " + ownerDesc + " has empty '" + field + "' map");
		}
		return out;
	}

	private static String getString(JsonObject o, String key, String def) {
		JsonElement e = o.get(key);
		if (e == null || e.isJsonNull()) return def;
		return e.getAsString();
	}

	private static boolean getBoolean(JsonObject o, String key, boolean def) {
		JsonElement e = o.get(key);
		if (e == null || e.isJsonNull()) return def;
		return e.getAsBoolean();
	}
}
