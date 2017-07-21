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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.IdentifierUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class XMPPClient {
	final static Logger logger = (Logger) LoggerFactory.getLogger(XMPPClient.class);
	public static final String CHAT_DOMAIN_PREFIX = "chat";
	public static final String GROUP_CHAT_DOMAIN_PREFIX = "gchat";
	
	XMPPTCPConnectionConfiguration config;
	private final EntityFullJid userJid;
	private Resourcepart userNickname = null;
	private final AbstractXMPPConnection con;
	private final XMPPClientListener listener;
	private final ConversationHistory history;
	private final XmppsRosterListener rosterListener;
	private final XmppsDCIncomingMessageListener dcIncomingMessageListener;
	private final XmppsMUCInvitationListener mucInvitationListener;
	private final Map<EntityBareJid, DChat> directChats = new HashMap<>();
	private final Map<EntityBareJid, GChat> groupChats = new HashMap<>();
	private final AtomicBoolean isDisconnecting = new AtomicBoolean(false);
	
	public XMPPClient(XMPPTCPConnectionConfiguration.Builder builder, String nickname, XMPPClientListener listener) {
		this(builder, nickname, listener, null);
	}
	
	public XMPPClient(XMPPTCPConnectionConfiguration.Builder builder, String nickname, XMPPClientListener listener, ConversationHistory history) {
		builder.setSendPresence(false);
		
		this.config = builder.build();
		this.userJid = JidCreate.entityFullFrom(XMPPHelper.asLocalpart(config.getUsername()), config.getXMPPServiceDomain(), config.getResource());
		this.userNickname = XMPPHelper.asResourcepart(nickname);
		this.con = new XMPPTCPConnection(config);
		this.listener = listener;
		this.history = history;
		this.rosterListener = new XmppsRosterListener();
		this.dcIncomingMessageListener = new XmppsDCIncomingMessageListener();
		this.mucInvitationListener = new XmppsMUCInvitationListener();
	}
	
	public boolean isConnected() {
		synchronized(con) {
			return con.isConnected();
		}
	}
	
	public boolean isAuthenticated() {
		synchronized(con) {
			return con.isAuthenticated();
		}
	}
	
	public void disconnect() {
		synchronized(con) {
			isDisconnecting.set(true);
			internalLogout();
			con.disconnect();
			isDisconnecting.set(false);
		}
	}
	
	public EntityFullJid getUserJid() {
		return this.userJid;
	}
	
	public String getUserNickame() {
		return  this.userNickname.toString();
	}
	
	public void updatePresence(PresenceStatus presenceStatus, String statusText) throws XMPPClientException {
		checkAuthentication();
		
		try {
			Presence presence = new Presence(PresenceStatus.presenceType(presenceStatus));
			presence.setPriority(24);
			Presence.Mode mode = PresenceStatus.presenceMode(presenceStatus);
			if (mode != null) presence.setMode(mode);
			if (statusText != null) presence.setStatus(statusText);
			con.sendStanza(presence);
			
		} catch(SmackException | InterruptedException ex) {
			throw new XMPPClientException(ex);
		}
	}
	
	public List<Friend> listFriends() throws XMPPClientException {
		checkAuthentication();
		
		try {
			final Roster roster = getRoster();
			
			checkRosterLoaded(roster);
			ArrayList<Friend> friends = new ArrayList<>();
			for(RosterEntry entry : roster.getEntries()) {
				FriendPresence presence = getFriendPresence(entry.getJid().asEntityBareJidIfPossible());
				friends.add(new Friend(entry, presence));
			}
			return friends;
			
		} catch(SmackException | InterruptedException ex) {
			throw new XMPPClientException(ex);
		}
	}
	
	public FriendPresence getFriendPresence(EntityBareJid jid) throws XMPPClientException {
		checkAuthentication();
		
		Roster roster = Roster.getInstanceFor(con);
		Presence presence = roster.getPresence(jid.asBareJid());
		return (presence != null) ? new FriendPresence(presence) : null;
	}
	
	public String getFriendNickname(EntityBareJid jid) throws XMPPClientException {
		return getFriendNickname(jid, true);
	}
	
	public String getFriendNickname(EntityBareJid jid, boolean guessIfNull) throws XMPPClientException {
		checkAuthentication();
		
		Roster roster = Roster.getInstanceFor(con);
		return getRosterEntryNickname(roster, jid, guessIfNull);
	}
	
	public LastActivity getLastActivity(Jid jid) throws XMPPClientException {
		checkAuthentication();
		
		try {
			final LastActivityManager lastActivityManager = LastActivityManager.getInstanceFor(con);
			return lastActivityManager.getLastActivity(jid);
			
		} catch(SmackException | XMPPException | InterruptedException ex) {
			throw new XMPPClientException(ex);
		}
	}
	
	public EntityBareJid generateChatJid(String withUserJid) {
		return createChatJid(withUserJid);
	}
	
	public EntityBareJid generateChatJid(EntityBareJid withUser) {
		return createChatJid(withUser);
	}
	
	public EntityBareJid newChat(EntityBareJid withUser) throws XMPPClientException {
		checkAuthentication();
		
		try {
			final Roster roster = getRoster();
			checkRosterLoaded(roster);
			
			final EntityBareJid chatJid = createChatJid(withUser);
			final String withUserNick = getRosterEntryNickname(roster, withUser, true);
			
			synchronized(directChats) {
				DChat chatObj = directChats.get(chatJid);
				if (chatObj == null) {
					final ChatManager chatMgr = getChatManager();
					final Chat chat = chatMgr.chatWith(withUser);
					chatObj = doAddChat(chatJid, userJid.asEntityBareJid(), withUserNick, withUser, chat);
				}
			}
			return chatJid;
			
		} catch(SmackException | InterruptedException ex) {
			throw new XMPPClientException(ex);
		}
	}
	
	public EntityBareJid newGroupChat(String name, List<EntityBareJid> withUsers) throws XMPPClientException {
		checkAuthentication();
		
		final EntityBareJid myJid = userJid.asEntityBareJid();
		final EntityBareJid chatJid = createGroupChatJid();
		
		synchronized(groupChats) {
			GChat chatObj = groupChats.get(chatJid);
			if (chatObj == null) {
				try {
					final MultiUserChatManager muChatMgr = getMUChatManager();
					final MultiUserChat muc = muChatMgr.getMultiUserChat(chatJid);
					Set<EntityBareJid> owners = new HashSet<>();
					owners.add(myJid);

					muc.create(userNickname)
						.getConfigFormManager()
						.makeMembersOnly()
						.setRoomOwners(owners)
						.submitConfigurationForm();
					muc.changeSubject(name);
					chatObj = doAddGroupChat(chatJid, myJid, name, true, muc);

					for(EntityBareJid withUser : withUsers) {
						muc.invite(withUser, name);
					}
				} catch(SmackException | XMPPException | InterruptedException ex) {
					throw new XMPPClientException(ex);
				}
			}
		}
		
		return chatJid;
	}
	
	public List<ChatRoom> getChats() throws XMPPClientException {
		checkAuthentication();
		
		ArrayList<ChatRoom> chats = new ArrayList<>();
		synchronized(directChats) {
			for(DChat chatObj : directChats.values()) {
				chats.add(chatObj.getChatRoom());
			}
		}
		synchronized(groupChats) {
			for(GChat chatObj : groupChats.values()) {
				chats.add(chatObj.getChatRoom());
			}
		}
		return chats;
	}
	
	public void forgetChat(EntityBareJid chatJid) throws XMPPClientException {
		checkAuthentication();
		
		synchronized(directChats) {
			if (directChats.containsKey(chatJid)) doRemoveChat(chatJid);
		}
		synchronized(groupChats) {
			if (groupChats.containsKey(chatJid)) doRemoveGroupChat(chatJid);
		}
	}
	
	public ChatMessage sendMessage(EntityBareJid chatJid, String text) throws XMPPClientException {
		checkAuthentication();
		
		final EntityBareJid myJid = userJid.asEntityBareJid();
		final String myResource = XMPPHelper.asResourcepartString(userJid.getResourceOrNull());
		
		Message message = new Message();
		message.setBody(text);
		
		synchronized(directChats) {
			DChat chatObj = directChats.get(chatJid);
			if (chatObj != null) {
				try {
					final DateTime ts = DateTime.now(DateTimeZone.UTC);
					chatObj.getRawChat().send(message);
					ChatMessage chatMessage = new ChatMessage(chatJid, myJid, myResource, userNickname.toString(), ts, message);
					
					// TODO: threadify this?
					try {
						listener.onChatRoomMessageSent(chatObj.getChatRoom(), chatMessage);
					} catch(Throwable t) {
						logger.error("Listener error", t);
					}
					
					return chatMessage;
					
				} catch(SmackException | InterruptedException ex) {
					throw new XMPPClientException(ex);
				}	
			}
		}
		
		synchronized(groupChats) {
			GChat chatObj = groupChats.get(chatJid);
			if (chatObj != null) {
				try {
					final DateTime ts = DateTime.now(DateTimeZone.UTC);
					chatObj.getRawChat().sendMessage(message);
					ChatMessage chatMessage = new ChatMessage(chatJid, myJid, myResource, userNickname.toString(), ts, message);
					
					// TODO: threadify this?
					try {
						listener.onChatRoomMessageSent(chatObj.getChatRoom(), chatMessage);
					} catch(Throwable t) {
						logger.error("Listener error", t);
					}
					
					return chatMessage;
					
				} catch(SmackException | InterruptedException ex) {
					throw new XMPPClientException(ex);
				}
			}
		}
		
		throw new XMPPClientException("Chat not found. Please create a chat before try to send messages in it!");
	}
	
	private DChat doAddChat(EntityBareJid chatJid, EntityBareJid ownerJid, String name, EntityBareJid withJid, Chat chat) {
		logger.debug("Creating direct chat [{}]", chatJid.toString());
		DChat chatObj = new DChat(new DirectChatRoom(chatJid, ownerJid, name, withJid), chat);
		directChats.put(chatJid, chatObj);
		
		try {
			listener.onChatRoomAdded(chatObj.getChatRoom());
		} catch(Throwable t) {
			logger.error("Listener error", t);
		}
		
		return chatObj;
	}
	
	private void doRemoveChat(EntityBareJid chatJid) {
		logger.debug("Removing direct chat [{}]", chatJid.toString());
		DChat chatObj = directChats.remove(chatJid);
		
		try {
			listener.onChatRoomRemoved(chatObj.getChatRoom().getChatJid());
		} catch(Throwable t) {
			logger.error("Listener error", t);
		}
	}
	
	private void doRemoveGroupChat(EntityBareJid chatJid) {
		logger.debug("Removing group chat [{}]", chatJid.toString());
		GChat chatObj = groupChats.remove(chatJid);
		
		MultiUserChat chat = chatObj.getRawChat();
		chat.removeSubjectUpdatedListener(chatObj);
		chat.removeMessageListener(chatObj);
		chat.removeParticipantStatusListener(chatObj);
		
		try {
			listener.onChatRoomRemoved(chatObj.getChatRoom().getChatJid());
		} catch(Throwable t) {
			logger.error("Listener error", t);
		}
	}
	
	private GChat doAddGroupChat(EntityBareJid chatJid, EntityBareJid creatorJid, String name, boolean iAmOwner, MultiUserChat chat) {
		logger.debug("Creating group chat [{}]", chatJid.toString());
		GChat chatObj = new GChat(new GroupChatRoom(chatJid, creatorJid, name, iAmOwner));
		groupChats.put(chatJid, chatObj);
		
		try {
			listener.onChatRoomAdded(chatObj.getChatRoom());
		} catch(Throwable t) {
			logger.error("Listener error", t);
		}
					
		chat.addSubjectUpdatedListener(chatObj);
		chat.addMessageListener(chatObj);
		chat.addParticipantStatusListener(chatObj);
		
		return chatObj;
	}
	
	private void checkRosterLoaded(final Roster roster) throws SmackException, InterruptedException {
		if (!roster.isLoaded()) roster.reloadAndWait();
	}
	
	private EntityBareJid createChatJid(EntityBareJid user) {
		return (user == null) ? null : createChatJid(user.toString());
	}
	
	private String chatDomain(String domainPrefix) {
		return domainPrefix + "." + userJid.getDomain().toString();
	}
	
	private EntityBareJid createChatJid(String userJid) {
		return buildChatJid(DigestUtils.md5Hex(userJid), chatDomain(CHAT_DOMAIN_PREFIX));
	}
	
	private EntityBareJid createGroupChatJid() {
		return buildChatJid(IdentifierUtils.getUUIDTimeBased(true), chatDomain(GROUP_CHAT_DOMAIN_PREFIX));
	}
	
	private EntityBareJid buildChatJid(String local, String domain) {
		final String s = local + "@" + domain;
		try {
			return JidCreate.bareFrom(s).asEntityBareJidIfPossible();
		} catch(XmppStringprepException ex) {
			logger.error("Error building MUC jid [{}]", ex, s);
			return null;
		}
	}
	
	private Roster getRoster() {
		return Roster.getInstanceFor(con);
	}
	
	private ChatManager getChatManager() {
		return ChatManager.getInstanceFor(con);
	}
	
	private MultiUserChatManager getMUChatManager() {
		return MultiUserChatManager.getInstanceFor(con);
	}
	
	private String getRosterEntryNickname(Roster roster, BareJid jid, boolean guessIfNull) {
		RosterEntry entry = roster.getEntry(jid);
		String nick = (entry != null) ? entry.getName() : null;
		if (StringUtils.isBlank(nick)) {
			return guessIfNull ? XMPPHelper.buildGuessedNickname(jid.toString()) : null;
		} else {
			return nick;
		}
	}
	
	private void internalLogin() throws SmackException, XMPPException, InterruptedException, IOException {
		final Roster roster = getRoster();
		roster.addRosterListener(rosterListener);
		final ChatManager chatMgr = getChatManager();
		chatMgr.addIncomingListener(dcIncomingMessageListener);
		final MultiUserChatManager muChatMgr = getMUChatManager();
		muChatMgr.addInvitationListener(mucInvitationListener);
		
		// Restore previous  direct (one-to-one) chat rooms
		if (history != null) {
			for(ChatRoom chat : history.getChats()) {
				if (chat instanceof DirectChatRoom) {
					directChats.put(chat.getChatJid(), new DChat((DirectChatRoom)chat));
				}
			}
		}
		
		con.login();
		checkRosterLoaded(roster);
		
		for(DChat dchat : directChats.values()) {
			final EntityBareJid withJid = dchat.getChatRoom().getWithJid().asEntityBareJid();
			String nick = dchat.getChatRoom().getName();
			String friendNick = getRosterEntryNickname(roster, withJid, false);
			if (friendNick != null) nick = friendNick;
			if (StringUtils.isBlank(nick)) nick = XMPPHelper.buildGuessedNickname(withJid.toString());
			if (!StringUtils.equals(dchat.getChatRoom().getName(), nick)) {
				// Nick updated
			}
		}
	}
	
	private void internalLogout() {
		final MultiUserChatManager muChatMgr = getMUChatManager();
		muChatMgr.removeInvitationListener(mucInvitationListener);
		final ChatManager chatMgr = getChatManager();
		chatMgr.removeListener(dcIncomingMessageListener);
		final Roster roster = getRoster();
		roster.removeRosterListener(rosterListener);
		
		directChats.clear();
	}
	
	private void checkAuthentication() throws XMPPClientException {
		synchronized(con) {
			checkConnection();
			
			try {
				if (!con.isAuthenticated()) internalLogin();
			} catch(SmackException | XMPPException | InterruptedException | IOException ex) {
				logger.error("Unable to login", ex);
				throw new XMPPClientException(ex);
			}
		}
	}
	
	private void checkConnection() throws XMPPClientException {
		synchronized(con) {
			try {
				if (!con.isConnected()) con.connect();
			} catch(SmackException | XMPPException | InterruptedException | IOException ex) {
				logger.error("Unable to establish a connection", ex);
				throw new XMPPClientException(ex);
			}
		}
	}
	
	private class XmppsRosterListener implements RosterListener {

		@Override
		public void entriesAdded(Collection<Jid> clctn) {
			if (isDisconnecting.get()) return;
			listener.friendsAdded(clctn);
		}

		@Override
		public void entriesUpdated(Collection<Jid> clctn) {
			if (isDisconnecting.get()) return;
			listener.friendsUpdated(clctn);
		}

		@Override
		public void entriesDeleted(Collection<Jid> clctn) {
			if (isDisconnecting.get()) return;
			listener.friendsDeleted(clctn);
		}

		@Override
		public void presenceChanged(Presence prsnc) {
			if (isDisconnecting.get()) return;
			
			final FriendPresence presence = new FriendPresence(prsnc);
			logger.debug("Presence changed [{}, {}]", EnumUtils.toSerializedName(presence.getPresenceStatus()), prsnc.getFrom().toString());
			try {
				listener.onFriendPresenceChanged(prsnc.getFrom(), presence, getFriendPresence(prsnc.getFrom().asEntityBareJidIfPossible()));
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}
	}
	
	private class XmppsDCIncomingMessageListener implements IncomingChatMessageListener {

		@Override
		public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
			if (isDisconnecting.get()) return;
			
			try {
				final Roster roster = getRoster();
				checkRosterLoaded(roster);

				final EntityBareJid chatJid = createChatJid(from);
				final String fromNick = getRosterEntryNickname(roster, from, true);

				DChat chatObj = null;
				synchronized(directChats) {
					chatObj = directChats.get(chatJid);
					if (chatObj == null) {
						chatObj = doAddChat(chatJid, from, fromNick, from, chat);
					}
				}

				try {
					final DateTime ts = DateTime.now(DateTimeZone.UTC);
					ChatMessage chatMessage = new ChatMessage(chatJid, message.getFrom(), fromNick, ts, message);
					listener.onChatRoomMessageReceived(chatObj.getChatRoom(), chatMessage);
				} catch(Throwable t) {
					logger.error("Listener error", t);
				}

			} catch(SmackException | InterruptedException ex) {
				logger.error("IncomingChatMessageListener error", ex);
			}
		}
	}
	
	private class XmppsMUCInvitationListener implements InvitationListener {

		@Override
		public void invitationReceived(XMPPConnection xmppc, MultiUserChat multiUserChat, EntityJid inviterJid, String reason, String password, Message message, MUCUser.Invite invitation) {
			if (isDisconnecting.get()) return;
			final MultiUserChatManager muChatMgr = getMUChatManager();
			final EntityBareJid chatJid = multiUserChat.getRoom();
			
			logger.debug("Group chat invitation received [{}, {}]", chatJid.toString(), inviterJid.toString());
			MultiUserChat muc = muChatMgr.getMultiUserChat(chatJid);
			
			GChat chatObj = null;
			synchronized(groupChats) {
				chatObj = groupChats.get(chatJid);
				if (chatObj == null) {
					chatObj = doAddGroupChat(chatJid, inviterJid.asEntityBareJid(), null, false, muc);
					/*
					logger.debug("Group chat not found. Creating it... [{}]", chatJid.toString());
					chatObj = new MUChat(new GroupChatRoom(chatJid, inviterJid.asEntityBareJid(), null, false));
					groupChats.put(chatJid, chatObj);
					
					try {
						listener.onChatRoomAdded(chatObj.getChatRoom());
					} catch(Throwable t) {
						logger.error("Listener error", t);
					}
					
					muc.addSubjectUpdatedListener(chatObj);
					muc.addMessageListener(chatObj);
					muc.addParticipantStatusListener(chatObj);
					*/
				}
				
				if (!muc.isJoined()) {
					try {
						muc.join(userNickname);
						logger.debug("Group chat joined [{}]", chatJid.toString());
					} catch(SmackException | XMPPException | InterruptedException ex) {
						logger.error("Error joining group chat", ex);
					}
				} else {
					logger.debug("Group chat already joined [{}]", multiUserChat.getNickname());
				}
			}
		}
	}
	
	private class DChat {
		private final DirectChatRoom chatRoom;
		private Chat rawChat;
		
		public DChat(DirectChatRoom chatRoom) {
			this(chatRoom, null);
		}
		
		public DChat(DirectChatRoom chatRoom, Chat rawChat) {
			this.chatRoom = chatRoom;
			this.rawChat = rawChat;
		}
		
		public DirectChatRoom getChatRoom() {
			return chatRoom;
		}
		
		public synchronized Chat getRawChat() {
			if (rawChat == null) {
				rawChat = getChatManager().chatWith(chatRoom.getWithJid());
			}
			return rawChat;
		}
	}
	
	private class GChat implements SubjectUpdatedListener, MessageListener, ParticipantStatusListener {
		private final GroupChatRoom chatRoom;
		
		public GChat(GroupChatRoom chatRoom) {
			this.chatRoom = chatRoom;
		}
		
		public GroupChatRoom getChatRoom() {
			return chatRoom;
		}
		
		public MultiUserChat getRawChat() {
			return getMUChatManager().getMultiUserChat(chatRoom.getChatJid());
		}
		
		@Override
		public void subjectUpdated(String subject, EntityFullJid from) {
			try {
				chatRoom.setName(subject);
				listener.onChatRoomUpdated(chatRoom);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void processMessage(Message message) {
			final Roster roster = getRoster();
			
			try {
				final DateTime ts = DateTime.now(DateTimeZone.UTC);
				final Jid fromJid = message.getFrom();
				final String fromNick = getRosterEntryNickname(roster, fromJid.asBareJid(), true);
				ChatMessage chatMessage = new ChatMessage(chatRoom.getChatJid(), fromJid.asEntityBareJidIfPossible(), XMPPHelper.asResourcepartString(fromJid.getResourceOrNull()), fromNick, ts, message);
				listener.onChatRoomMessageReceived(chatRoom, chatMessage);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}
		
		@Override
		public void joined(EntityFullJid participant) {
			try {
				listener.onChatRoomParticipantJoined(chatRoom, participant);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void left(EntityFullJid participant) {
			try {
				listener.onChatRoomParticipantLeft(chatRoom, participant, false);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void kicked(EntityFullJid participant, Jid actor, String string) {
			try {
				listener.onChatRoomParticipantLeft(chatRoom, participant, true);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void voiceGranted(EntityFullJid efj) {}

		@Override
		public void voiceRevoked(EntityFullJid efj) {}

		@Override
		public void banned(EntityFullJid efj, Jid jid, String string) {}

		@Override
		public void membershipGranted(EntityFullJid efj) {}

		@Override
		public void membershipRevoked(EntityFullJid efj) {}

		@Override
		public void moderatorGranted(EntityFullJid efj) {}

		@Override
		public void moderatorRevoked(EntityFullJid efj) {}

		@Override
		public void ownershipGranted(EntityFullJid efj) {}

		@Override
		public void ownershipRevoked(EntityFullJid efj) {}

		@Override
		public void adminGranted(EntityFullJid efj) {}

		@Override
		public void adminRevoked(EntityFullJid efj) {}

		@Override
		public void nicknameChanged(EntityFullJid efj, Resourcepart r) {}
	}
	
	public static boolean isGroupChat(EntityBareJid chatJid) {
		return isGroupChat(chatJid.toString());
	}
	
	public static boolean isGroupChat(String chatEntityBareJid) {
		return StringUtils.contains(chatEntityBareJid, "@"+GROUP_CHAT_DOMAIN_PREFIX+".");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	public void login() throws XMPPClientException {
		checkConnection();
		
		try {
			final Roster roster = getRoster();
			roster.addRosterListener(rosterListener);
			final ChatManager chatMgr = getChatManager();
			chatMgr.addIncomingListener(dcIncomingMessageListener);
			final MultiUserChatManager muChatMgr = getMUChatManager();
			muChatMgr.addInvitationListener(mucInvitationListener);
			this.nickname = XMPPHelper.asResourcepart(nickname);
			
			// Restore previous  direct (one-to-one) chat rooms
			if (history != null) {
				synchronized(directChats) {
					for(ChatRoom chat : history.getChats()) {
						if (chat instanceof DirectChatRoom) {
							directChats.put(chat.getChatJid(), new DChat((DirectChatRoom)chat));
						}
					}
				}
			}

			con.login();
			ensureRosterLoaded(roster);
			
			// Checks for updated nicks...
			synchronized(directChats) {
				for(DChat dchat : directChats.values()) {
					final EntityBareJid withJid = dchat.getChatRoom().getWithJid().asEntityBareJid();
					String nick = dchat.getChatRoom().getName();
					String friendNick = getRosterEntryNickname(roster, withJid, false);
					if (friendNick != null) nick = friendNick;
					if (StringUtils.isBlank(nick)) nick = XMPPHelper.buildGuessedNickname(withJid.toString());
					if (!StringUtils.equals(dchat.getChatRoom().getName(), nick)) {
						// Nick updated
					}
				}
			}
			
		} catch(SmackException | XMPPException | InterruptedException | IOException ex) {
			throw new XMPPClientException(ex);
		}
	}
	*/
	
	/*
	private synchronized void ensureAuthentication() throws XMPPClientException {
		checkConnection();
		if (!con.isAuthenticated()) throw new XMPPClientException("No logged user");
	}
	*/
}
