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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Sanitizes model-generated content before it reaches the UI.
 *
 * The model output is treated as untrusted: even when the prompt asked for
 * HTML, the returned HTML must not be allowed to carry &lt;script&gt;,
 * javascript: URLs, event handlers, or other active content into the
 * rendered page. If the model is jailbroken or echoes an injected payload
 * from a mail body, this is the last line of defense.
 *
 * The default HTML whitelist covers the semantic subset we want to render
 * in chat-like UI (paragraphs, lists, tables, emphasis, links, basic
 * inline styling via data-ai-fg / data-ai-bg, code blocks).
 *
 * Customization points:
 *  - {@link #buildDefaultHtmlWhitelist()} — build a fresh default policy
 *    that callers can mutate (addTags/addAttributes/removeTags/...) before
 *    passing it to {@link #sanitizeHtml(String, Whitelist)}.
 *  - {@link #setDefaultHtmlWhitelist(Whitelist)} — replace the process-wide
 *    default used by the no-arg overloads.
 *
 * Note: jsoup 1.8.3 calls this class "Whitelist"; newer jsoup calls it
 * "Safelist". We stay on 1.8.3 to match the transitive version already
 * pulled in by other WebTop modules.
 */
public class AIOutputSanitizer {

	private static volatile Whitelist defaultHtmlWhitelist = buildDefaultHtmlWhitelist();

	/**
	 * Returns a fresh whitelist with the default policy. Callers can mutate
	 * the returned instance (addTags/addAttributes/removeTags/etc.) without
	 * affecting the process-wide default.
	 */
	public static Whitelist buildDefaultHtmlWhitelist() {
		return new Whitelist()
			.addTags(
				"p", "br", "hr",
				"h1", "h2", "h3", "h4", "h5", "h6",
				"ul", "ol", "li",
				"blockquote", "pre", "code",
				"b", "strong", "i", "em", "u", "s", "mark",
				"table", "thead", "tbody", "tfoot", "tr", "th", "td", "caption",
				"a", "span", "div"
			)
			.addAttributes(":all", "data-ai-fg", "data-ai-bg")
			.addAttributes("a", "href", "title")
			.addAttributes("td", "colspan", "rowspan")
			.addAttributes("th", "colspan", "rowspan", "scope")
			.addProtocols("a", "href", "http", "https", "mailto", "tel");
	}

	public static Whitelist getDefaultHtmlWhitelist() {
		return defaultHtmlWhitelist;
	}

	/**
	 * Replaces the process-wide default whitelist. Use this to permanently
	 * widen or tighten policy (e.g. to allow &lt;img&gt; in chat output).
	 * Passing null resets to the built-in default.
	 */
	public static void setDefaultHtmlWhitelist(Whitelist wl) {
		defaultHtmlWhitelist = (wl == null) ? buildDefaultHtmlWhitelist() : wl;
	}

	public static String sanitizeHtml(String html) {
		return sanitizeHtml(html, defaultHtmlWhitelist);
	}

	public static String sanitizeHtml(String html, Whitelist wl) {
		if (StringUtils.isBlank(html)) return html == null ? "" : html;
		if (wl == null) wl = defaultHtmlWhitelist;
		return Jsoup.clean(html, wl);
	}

	/**
	 * Routes sanitization based on the declared output format. "html" and
	 * "minimal html" go through {@link #sanitizeHtml(String)}; anything
	 * else (plain text, JSON, markdown, ...) is passed through unchanged —
	 * those formats carry no active-content risk at render time on their own,
	 * and the caller is responsible for rendering them as text.
	 */
	public static String sanitizeByFormat(String content, String format) {
		if (content == null) return "";
		if (format == null) return content;
		String f = format.trim().toLowerCase();
		if (f.equals("html") || f.equals("minimal html") || f.equals("minimal-html")) {
			return sanitizeHtml(content);
		}
		return content;
	}
}
