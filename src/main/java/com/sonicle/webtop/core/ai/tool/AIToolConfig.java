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
import java.util.List;
import java.util.Map;

/**
 * Immutable AI tool configuration parsed from the bundled ai-tool.json.
 *
 * Responsibilities:
 *  - Parse the JSON tree into AIToolItem/AIToolInputSpec instances.
 *  - Build a flat id→item index for O(1) lookup during AIPrompt dispatch.
 *  - Refuse duplicate ids; refuse leaves without a prompt key.
 */
public final class AIToolConfig {

	private final List<AIToolItem> items;
	private final Map<String, AIToolItem> index;

	private AIToolConfig(List<AIToolItem> items, Map<String, AIToolItem> index) {
		this.items = Collections.unmodifiableList(items);
		this.index = Collections.unmodifiableMap(index);
	}

	public List<AIToolItem> getItems() {
		return items;
	}

	public AIToolItem findById(String id) {
		return id == null ? null : index.get(id);
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
		JsonElement itemsEl = rootObj.get("items");
		if (itemsEl == null || !itemsEl.isJsonArray()) {
			throw new IOException("ai-tool.json: missing 'items' array");
		}
		List<AIToolItem> parsed = new ArrayList<>();
		Map<String, AIToolItem> idx = new HashMap<>();
		for (JsonElement el : itemsEl.getAsJsonArray()) {
			parsed.add(parseItem(el, idx));
		}
		return new AIToolConfig(parsed, idx);
	}

	private static AIToolItem parseItem(JsonElement el, Map<String, AIToolItem> idx) throws IOException {
		if (el == null || !el.isJsonObject()) {
			throw new IOException("ai-tool.json: item must be an object");
		}
		JsonObject o = el.getAsJsonObject();
		String id = getString(o, "id", null);
		String labelKey = getString(o, "labelKey", null);
		if (id == null || id.isEmpty()) throw new IOException("ai-tool.json: item missing 'id'");
		if (labelKey == null || labelKey.isEmpty()) throw new IOException("ai-tool.json: item '" + id + "' missing 'labelKey'");

		List<AIToolItem> children = new ArrayList<>();
		if (o.has("children") && o.get("children").isJsonArray()) {
			JsonArray arr = o.getAsJsonArray("children");
			for (JsonElement cel : arr) children.add(parseItem(cel, idx));
		}

		String promptKey = null;
		AIToolInputSpec input = null;
		boolean requiresSelection = false;
		String noSelectionErrorKey = null;

		if (children.isEmpty()) {
			promptKey = getString(o, "promptKey", null);
			if (promptKey == null || promptKey.isEmpty()) {
				throw new IOException("ai-tool.json: leaf '" + id + "' missing 'promptKey'");
			}
			requiresSelection = getBoolean(o, "requiresSelection", false);
			noSelectionErrorKey = getString(o, "noSelectionErrorKey", null);
			if (o.has("input") && o.get("input").isJsonObject()) {
				JsonObject ino = o.getAsJsonObject("input");
				String titleKey = getString(ino, "titleKey", null);
				String questionKey = getString(ino, "questionKey", null);
				boolean multiline = getBoolean(ino, "multiline", false);
				boolean required = getBoolean(ino, "required", true);
				input = new AIToolInputSpec(titleKey, questionKey, multiline, required);
			}
		}

		AIToolItem item = new AIToolItem(id, labelKey, promptKey, input, requiresSelection, noSelectionErrorKey, children);
		if (idx.put(id, item) != null) {
			throw new IOException("ai-tool.json: duplicate id '" + id + "'");
		}
		return item;
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
