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
package com.sonicle.webtop.core.xmpp;

import com.sonicle.commons.web.json.CompositeId;
import org.apache.commons.codec.digest.DigestUtils;
import org.jivesoftware.smack.packet.Message;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;

/**
 *
 * @author malbinola
 */
public class ChatMessage {
	private final EntityBareJid chatJid;
	private final EntityBareJid fromUser;
	private final String fromUserResource;
	private final String fromUserNickame;
	private final DateTime timestamp;
	private final String messageUid;
	private final Message message;
	
	public ChatMessage(EntityBareJid chatJid, Jid fromUser, String fromUserNickame, DateTime timestamp, Message message) {
		this(chatJid, fromUser.asEntityBareJidIfPossible(), XMPPHelper.asResourcepartString(fromUser), fromUserNickame, timestamp, message);
	}
	
	public ChatMessage(EntityBareJid chatJid, EntityBareJid fromUser, String fromUserResource, String fromUserNickame, DateTime timestamp, Message message) {
		this.chatJid = chatJid;
		this.fromUser = fromUser;
		this.fromUserResource = fromUserResource;
		this.fromUserNickame = fromUserNickame;
		this.timestamp = timestamp;
		this.messageUid = buildUniqueId(fromUser, fromUserResource, timestamp);
		this.message = message;
	}
	
	public EntityBareJid getChatJid() {
		return chatJid;
	}
	
	public EntityBareJid getFromUser() {
		return fromUser;
	}
	
	public String getFromUserResource() {
		return fromUserResource;
	}
	
	public String getFromUserNickname() {
		return fromUserNickame;
	}
	
	public DateTime getTimestamp() {
		return timestamp;
	}
	
	public String getMessageUid() {
		return messageUid;
	}
	
	public Message getRawMessage() {
		return message;
	}
	
	public String getStanzaId() {
		return message.getStanzaId();
	}
	
	public String getText() {
		return message.getBody();
	}
	
	public static String buildUniqueId(Jid fromUser, DateTime timestamp) {
		return buildUniqueId(fromUser.asEntityBareJidIfPossible(), XMPPHelper.asResourcepartString(fromUser), timestamp);
	}
	
	public static String buildUniqueId(EntityBareJid fromUser, String fromUserResource, DateTime timestamp) {
		CompositeId cid = new CompositeId(fromUser, fromUserResource, new Long(timestamp.withZone(DateTimeZone.UTC).getMillis()));
		return DigestUtils.md5Hex(cid.toString());
	}
}
