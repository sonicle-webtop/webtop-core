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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jxmpp.jid.Jid;

/**
 *
 * @author malbinola
 */
public class XMPPService {
	private final AbstractXMPPConnection con;
	private final XMPPServiceListener listener;
	private final RosterListenerImpl rosterListenerImpl;
	
	public XMPPService(AbstractXMPPConnection con, XMPPServiceListener listener) {
		this.con = con;
		this.listener = listener;
		this.rosterListenerImpl = new RosterListenerImpl();
	}
	
	public void login() throws InterruptedException, IOException, SmackException, XMPPException {
		if (!con.isConnected()) return;
		
		con.login();
		final Roster roster = Roster.getInstanceFor(con);
		roster.addRosterListener(rosterListenerImpl);
	}
	
	private void ensureRosterLoaded(final Roster roster) throws InterruptedException, SmackException.NotLoggedInException, SmackException.NotConnectedException {
		if (!roster.isLoaded()) roster.reloadAndWait();
	}
	
	public List<Buddy> listBuddies() throws SmackException, InterruptedException {
		if (!con.isConnected()) return null;
		
		final Roster roster = Roster.getInstanceFor(con);
		ensureRosterLoaded(roster);
		ArrayList<Buddy> buddies = new ArrayList<>();
		for(RosterEntry entry : roster.getEntries()) {
			BuddyPresence presence = getPresence(entry.getJid());
			buddies.add(new Buddy(entry, presence));
		}
		return buddies;
	}
	
	
	/*
	public void sendMessage() {
		if (con.isConnected() && con.isAuthenticated()) {
			ChatManager chatManager = ChatManager.getInstanceFor(con);
			
			
			chatManager.
		}
		MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
		
		
	}
	*/
	
	public void updatePresence(PresenceStatus presenceStatus, String statusText) throws SmackException, InterruptedException {
		if (!con.isConnected()) return;
		
		Presence presence = new Presence(PresenceStatus.presenceType(presenceStatus));
		presence.setPriority(24);
		Presence.Mode mode = PresenceStatus.presenceMode(presenceStatus);
		if (mode != null) presence.setMode(mode);
		if (statusText != null) presence.setStatus(statusText);
		con.sendStanza(presence);
	}
	
	public BuddyPresence getPresence(Jid jid) {
		if (!con.isConnected()) return null;
		
		Roster roster = Roster.getInstanceFor(con);
		Presence presence = roster.getPresence(jid.asBareJid());
		return (presence != null) ? new BuddyPresence(presence) : null;
	}
	
	public LastActivity getLastActivity(Jid jid) throws SmackException, XMPPException, InterruptedException {
		if (!con.isConnected()) return null;
		
		final LastActivityManager lastActivityManager = LastActivityManager.getInstanceFor(con);
		return lastActivityManager.getLastActivity(jid);
	}
	
	private class RosterListenerImpl implements RosterListener {

		@Override
		public void entriesAdded(Collection<Jid> clctn) {
			listener.buddiesAdded(clctn);
		}

		@Override
		public void entriesUpdated(Collection<Jid> clctn) {
			listener.buddiesUpdated(clctn);
		}

		@Override
		public void entriesDeleted(Collection<Jid> clctn) {
			listener.buddiesDeleted(clctn);
		}

		@Override
		public void presenceChanged(Presence prsnc) {
			listener.presenceChanged(prsnc.getFrom(), new BuddyPresence(prsnc), getPresence(prsnc.getFrom()));
		}
	}
}
