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
package com.sonicle.webtop.core.xmpp.packet;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 *
 * @author malbinola
 */
public class OutOfBandData implements ExtensionElement {
	public static final String NAMESPACE = "jabber:x:oob";
	public static final String ELEMENT_NAME = "x";
	
	private final String url;
	private final String mime;
	private final long length;
	private final boolean encrypted;
	
	public OutOfBandData(String url) {
		this(url, null, -1, false);
	}
	
	public OutOfBandData(String url, String mime, long length) {
		this(url, mime, length, false);
	}
	
	public OutOfBandData(String url, String mime, long length, boolean encrypted) {
		this.url = url;
		this.mime = mime;
		this.length = length;
		this.encrypted = encrypted;
	}

	public String getUrl() {
		return url;
	}

	public String getMime() {
		return mime;
	}

	public long getLength() {
		return length;
	}

	public boolean isEncrypted() {
		return encrypted;
	}
	
	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String getElementName() {
		return ELEMENT_NAME;
	}

	@Override
	public CharSequence toXML() {
		/**
		 * <x xmlns='jabber:x:oob'>
		 *	<url type='image/png' length='2034782'>http://www.sonicle.com/media/filename_or_hash</url>
		 * </x>
		 */
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<%s xmlns=\"%s\"><url", ELEMENT_NAME, NAMESPACE));
		if (mime != null) sb.append(String.format(" type=\"%s\"", mime));
		if (length >= 0) sb.append(String.format(" length=\"%d\"", length));
		if (encrypted) sb.append(" encrypted=\"true\"");
		sb.append(">");
		sb.append(url);
		sb.append(String.format("</url></%s>", ELEMENT_NAME));
		return sb.toString();
	}
	
	public static final class Provider extends ExtensionElementProvider<OutOfBandData> {

		@Override
		public OutOfBandData parse(XmlPullParser parser, int initialDepth) throws Exception {
			String url = null, mime = null;
			long length = -1;
			boolean encrypted = false;
			boolean in_url = false, done = false;
			
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					if ("url".equals(parser.getName())) {
						in_url = true;
						mime = parser.getAttributeValue(null, "type");
						String _length = parser.getAttributeValue(null, "length");
						try {
							length = Long.parseLong(_length);
						} catch (Exception ex) { /* Ignore this... */ }
						String _encrypted = parser.getAttributeValue(null, "encrypted");
						encrypted = Boolean.parseBoolean(_encrypted);
					}
					
				} else if (eventType == XmlPullParser.END_TAG) {
					if ("url".equals(parser.getName())) {
						done = true;
					}
					
				} else if (eventType == XmlPullParser.TEXT && in_url) {
					url = parser.getText();
				}
			}
			
			return (url != null) ? new OutOfBandData(url, mime, length, encrypted) : null;
		}
	}
}
