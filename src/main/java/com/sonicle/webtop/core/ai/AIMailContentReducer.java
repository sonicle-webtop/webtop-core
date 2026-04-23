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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * Reduces untrusted email HTML to the semantic subset the model needs for
 * analysis (tables, lists, emphasis, links, color/highlight signal) while
 * stripping content that either has no informational value or exists only
 * to hide instructions from a human reader.
 *
 * What is preserved:
 *  - Block/heading/list/table structure
 *  - Inline emphasis (bold/italic/underline/mark)
 *  - Links (href + visible text)
 *  - Foreground/background color as a normalized named bucket on
 *    data-ai-fg / data-ai-bg attributes (e.g. "yellow", "red")
 *
 * What is removed:
 *  - &lt;script&gt;, &lt;style&gt;, &lt;link&gt;, &lt;meta&gt;, &lt;head&gt;, &lt;title&gt;
 *  - HTML comments (common carrier for MSO directives and hidden prompts)
 *  - Microsoft Office XML namespaces (o:*, v:*, w:*, x:*)
 *  - Elements hidden via CSS (display:none, visibility:hidden, opacity:0,
 *    zero width/height, zero font-size) — these have no human-visible
 *    content and are the canonical carrier for invisible prompt injection
 *  - Tracking pixels (&lt;img&gt; with 0 or 1 px dimensions)
 *  - Event handlers and javascript:/data: URLs
 *
 * The output is still HTML, so downstream structural-isolation (the
 * untrusted-block wrapper in {@link AIPromptBuilder}) remains required.
 */
public class AIMailContentReducer {

	private static final Pattern HIDDEN_STYLE = Pattern.compile(
		"(^|;)\\s*(display\\s*:\\s*none"
		+ "|visibility\\s*:\\s*hidden"
		+ "|opacity\\s*:\\s*0(?:\\.0+)?"
		+ "|font-size\\s*:\\s*0(?:pt|px|em|rem)?"
		+ "|height\\s*:\\s*0(?:pt|px)?"
		+ "|width\\s*:\\s*0(?:pt|px)?"
		+ ")\\s*(;|$)",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern COLOR_DECL = Pattern.compile(
		"(?i)(?:^|;)\\s*(color|background-color|background)\\s*:\\s*([^;]+)");

	private static final Pattern HEX3 = Pattern.compile("^#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])$");
	private static final Pattern HEX6 = Pattern.compile("^#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$");
	private static final Pattern RGB   = Pattern.compile("^rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)(?:\\s*,\\s*[0-9.]+)?\\s*\\)$");

	public static String reduce(String rawHtml) {
		if (StringUtils.isBlank(rawHtml)) return "";

		Document doc = Jsoup.parse(rawHtml);

		// 1. Drop outright-unwanted elements
		doc.select("script, style, link, meta, head, title").remove();

		// 2. Drop MSO/Office namespaced elements (common in Outlook-generated email)
		for (Element el : new ArrayList<>(doc.getAllElements())) {
			String tag = el.tagName();
			if (tag.contains(":")) {
				String ns = tag.substring(0, tag.indexOf(':'));
				if (ns.equals("o") || ns.equals("v") || ns.equals("w") || ns.equals("x") || ns.equals("m")) {
					el.remove();
				}
			}
		}

		// 3. Drop tracking pixels
		for (Element img : new ArrayList<>(doc.select("img"))) {
			if (isTrackingPixel(img)) img.remove();
		}

		// 4. Drop elements hidden via inline CSS
		removeHiddenElements(doc);

		// 5. Drop HTML comments
		removeComments(doc);

		// 6. Normalize color/background into named buckets on data-ai-* attrs
		normalizeColors(doc);

		// 7. Run the reducer whitelist to keep only semantic tags/attrs
		return Jsoup.clean(doc.body() == null ? doc.html() : doc.body().html(), buildReducerWhitelist());
	}

	// Whitelist for the reduced email content (jsoup 1.8.3 naming).
	// Public so callers can inspect/extend if policy needs to change.
	public static Whitelist buildReducerWhitelist() {
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

	private static boolean isTrackingPixel(Element img) {
		String w = img.attr("width");
		String h = img.attr("height");
		if (isZeroOrOne(w) && isZeroOrOne(h)) return true;
		String style = img.attr("style");
		if (!style.isEmpty()) {
			String s = style.toLowerCase().replaceAll("\\s+", "");
			if ((s.contains("width:0") || s.contains("width:1px"))
				&& (s.contains("height:0") || s.contains("height:1px"))) return true;
		}
		return false;
	}

	private static boolean isZeroOrOne(String dim) {
		if (dim == null || dim.isEmpty()) return false;
		String d = dim.trim().toLowerCase();
		if (d.endsWith("px")) d = d.substring(0, d.length() - 2).trim();
		return d.equals("0") || d.equals("1");
	}

	private static void removeHiddenElements(Document doc) {
		for (Element el : new ArrayList<>(doc.getAllElements())) {
			String style = el.attr("style");
			if (style.isEmpty()) continue;
			if (HIDDEN_STYLE.matcher(style).find()) {
				el.remove();
			}
		}
	}

	private static void removeComments(Document doc) {
		final List<Node> toRemove = new ArrayList<>();
		new NodeTraversor(new NodeVisitor() {
			public void head(Node node, int depth) {
				if (node instanceof Comment) toRemove.add(node);
			}
			public void tail(Node node, int depth) {}
		}).traverse(doc);
		for (Node n : toRemove) n.remove();
	}

	private static void normalizeColors(Document doc) {
		for (Element el : doc.getAllElements()) {
			String style = el.attr("style");
			if (style.isEmpty()) continue;

			String fgBucket = null;
			String bgBucket = null;
			Matcher m = COLOR_DECL.matcher(style);
			while (m.find()) {
				String prop = m.group(1).toLowerCase();
				String val = m.group(2).trim();
				String bucket = bucketize(val);
				if (bucket == null) continue;
				if (prop.equals("color")) fgBucket = bucket;
				else bgBucket = bucket;
			}
			if (fgBucket != null) el.attr("data-ai-fg", fgBucket);
			if (bgBucket != null) el.attr("data-ai-bg", bgBucket);
		}
	}

	// Coarse named buckets: the model reads "yellow" more reliably than "#f1c40f".
	// Returns null for values we can't parse; caller leaves the attr unset.
	public static String bucketize(String raw) {
		if (raw == null) return null;
		String v = raw.trim().toLowerCase();
		if (v.isEmpty() || v.equals("transparent") || v.equals("inherit") || v.equals("currentcolor")) return null;

		int r, g, b;
		Matcher hex3 = HEX3.matcher(v);
		Matcher hex6 = HEX6.matcher(v);
		Matcher rgb  = RGB.matcher(v);
		if (hex6.matches()) {
			r = Integer.parseInt(hex6.group(1), 16);
			g = Integer.parseInt(hex6.group(2), 16);
			b = Integer.parseInt(hex6.group(3), 16);
		} else if (hex3.matches()) {
			r = Integer.parseInt(hex3.group(1) + hex3.group(1), 16);
			g = Integer.parseInt(hex3.group(2) + hex3.group(2), 16);
			b = Integer.parseInt(hex3.group(3) + hex3.group(3), 16);
		} else if (rgb.matches()) {
			try {
				r = Integer.parseInt(rgb.group(1));
				g = Integer.parseInt(rgb.group(2));
				b = Integer.parseInt(rgb.group(3));
			} catch (NumberFormatException e) { return null; }
		} else {
			// Named color or something we don't parse — pass through if it looks word-like.
			if (v.matches("[a-z]+")) return v;
			return null;
		}
		return rgbToBucket(r, g, b);
	}

	private static String rgbToBucket(int r, int g, int b) {
		int max = Math.max(r, Math.max(g, b));
		int min = Math.min(r, Math.min(g, b));
		int delta = max - min;
		if (delta < 25) {
			if (max < 50) return "black";
			if (max > 220) return "white";
			return "gray";
		}
		double hue;
		if (max == r)      hue = (60.0 * (g - b) / delta + 360) % 360;
		else if (max == g) hue = 60.0 * (b - r) / delta + 120;
		else               hue = 60.0 * (r - g) / delta + 240;
		if (hue < 15 || hue >= 345) return "red";
		if (hue < 45)  return "orange";
		if (hue < 75)  return "yellow";
		if (hue < 165) return "green";
		if (hue < 195) return "cyan";
		if (hue < 255) return "blue";
		if (hue < 285) return "purple";
		return "pink";
	}
}
