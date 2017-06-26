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

import com.google.gson.annotations.SerializedName;
import org.jivesoftware.smack.packet.Presence;

/**
 *
 * @author malbinola
 */
public enum PresenceStatus {
	@SerializedName("online") ONLINE,
	@SerializedName("away") AWAY,
	@SerializedName("dnd") DO_NOT_DISTURB,
	@SerializedName("offline") OFFLINE;
	
	public static PresenceStatus presenceStatus(Presence presence) {
		if (presence.getType().equals(Presence.Type.available)) {
			Presence.Mode mode = presence.getMode();
			if (mode.equals(Presence.Mode.chat)) {
				return ONLINE;
			} else if (mode.equals(Presence.Mode.away)) {
				return AWAY;
			} else if (mode.equals(Presence.Mode.xa)) {
				return AWAY;
			} else if (mode.equals(Presence.Mode.dnd)) {
				return DO_NOT_DISTURB;
			} else {
				return ONLINE;
			}
		} else {
			return OFFLINE;
		}
	}
	
	public static Presence.Mode presenceMode(PresenceStatus presenceStatus) {
		switch(presenceStatus) {
			case ONLINE:
				return Presence.Mode.available;
			/*
			case CHAT:
				return Presence.Mode.chat;
			*/
			case AWAY:
				return Presence.Mode.away;
			/*
			case EXTENDED_AWAY:
				return Presence.Mode.xa;
			*/
			case DO_NOT_DISTURB:
				return Presence.Mode.dnd;
			default:
				return null;
		}
	}
	
	public static Presence.Type presenceType(PresenceStatus presenceStatus) {
		switch(presenceStatus) {
			case ONLINE:
			//case CHAT:
			case AWAY:
			//case EXTENDED_AWAY:
			case DO_NOT_DISTURB:
				return Presence.Type.available;
			case OFFLINE:
				return Presence.Type.unavailable;
		}
		return null;
	}
}
