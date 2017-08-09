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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.model.IMMessage;
import com.sonicle.webtop.core.xmpp.ChatMessage;
import com.sonicle.webtop.core.xmpp.packet.OutOfBandData;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsGridIMMessage {
	public String id;
	public String fromId;
	public String fromNick;
	public Boolean isSent;
	public String timestamp;
	public String action;
	public String text;
	public String data;
	public Boolean fromArchive;
	
	public JsGridIMMessage() {}
	
	public JsGridIMMessage(boolean isSent, IMMessage message, String senderNick, DateTimeZone utz) {
		DateTimeFormatter ymdhms = DateTimeUtils.createYmdHmsFormatter(utz);
		
		this.id = message.getMessageUid();
		this.fromId = message.getSenderJid();
		this.fromNick = senderNick;
		this.isSent = isSent;
		this.timestamp = ymdhms.print(message.getTimestamp());
		this.action = EnumUtils.toSerializedName(message.getAction());
		this.text = message.getText();
		this.data = message.getData();
		this.fromArchive = true;
	}
	
	public JsGridIMMessage(boolean isSent, ChatMessage message, DateTimeZone utz) {
		DateTimeFormatter fmt = DateTimeUtils.createYmdHmsFormatter(utz);
		
		this.id = message.getMessageUid();
		this.fromId = message.getFromUser().toString();
		this.fromNick = message.getFromUserNickname();
		this.isSent = isSent;
		this.timestamp = fmt.print(message.getTimestamp());
		this.action = EnumUtils.toSerializedName(IMMessage.Action.NONE);
		this.text = message.getText();
		this.data = null;
		OutOfBandData oob = message.getOutOfBandExtension();
		if (oob != null) {
			this.action = EnumUtils.toSerializedName(IMMessage.Action.FILE);
			this.data = JsGridIMMessage.toData(message.getText(), oob);
		}
		this.fromArchive = false;
	}

	public JsGridIMMessage(String id, String fromId, String fromNick, Boolean isSent, String timestamp, String action, String text, String data, boolean fromArchive) {
		this.id = id;
		this.fromId = fromId;
		this.fromNick = fromNick;
		this.isSent = isSent;
		this.timestamp = timestamp;
		this.action = action;
		this.text = text;
		this.data = data;
		this.fromArchive = fromArchive;
	}
	
	public static JsGridIMMessage asDateAction(String id, LocalDate date) {
		DateTimeFormatter ymd = DateTimeUtils.createYmdFormatter();
		return new JsGridIMMessage("!"+id, null, null, false, ymd.print(date) + " 00:00:00", "date", null, null, false);
	}
	
	public static JsGridIMMessage asWarnAction(String id, DateTime timestamp, String key) {
		DateTimeFormatter ymdmhs = DateTimeUtils.createYmdHmsFormatter();
		return new JsGridIMMessage("!"+id, null, null, false, ymdmhs.print(timestamp), "warn", key, null, false);
	}
	
	public static String toData(String filename, OutOfBandData oob) {
		if (oob == null) return "";
		MapItem mi = new MapItem();
		mi.add("url", oob.getUrl());
		mi.add("mime", oob.getMime());
		mi.add("ext", FilenameUtils.getExtension(filename));
		mi.add("size", oob.getLength());
		return JsonResult.GSON.toJson(mi);
	}
}
