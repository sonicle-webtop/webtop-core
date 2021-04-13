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
import com.sonicle.webtop.core.xmpp.packet.OutOfBandData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jivesoftware.smackx.xdata.form.Form;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.jid.util.JidUtil;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * https://github.com/igniterealtime/jxmpp/tree/master/jxmpp-jid/src/main/java/org/jxmpp/jid
 * 
 * EntityBareJid -> user@xmpp.org
 * EntityFullJid -> user@xmpp.org/resource
 * DomainBareJid -> xmpp.org
 * DomainFullJid -> xmpp.org/resource
 * FullJid -> localpart@domain.part/resourcepart
 * 
 * @author malbinola
 */
public class XMPPClient {
	final static Logger logger = (Logger) LoggerFactory.getLogger(XMPPClient.class);
	public static final String CHAT_DOMAIN_PREFIX = "instant";
	
	public static final String MUC_ROOMCONFIG_ROOMNAME = "muc#roomconfig_roomname";
	public static final String MUC_ROOMCONFIG_ROOMDESC = "muc#roomconfig_roomdesc";
	public static final String MUC_ROOMCONFIG_ENABLELOGGING = "muc#roomconfig_enablelogging";
	public static final String MUC_ROOMCONFIG_CHANGESUBJECT = "muc#roomconfig_changesubject";
	public static final String MUC_ROOMCONFIG_ALLOWINVITES = "muc#roomconfig_allowinvites";
	public static final String MUC_ROOMCONFIG_ALLOWPM = "muc#roomconfig_allowpm";
	public static final String MUC_ROOMCONFIG_MAXUSERS = "muc#roomconfig_maxusers";
	public static final String MUC_ROOMCONFIG_PRESENCEBROADCAST = "muc#roomconfig_presencebroadcast";
	public static final String MUC_ROOMCONFIG_GETMEMBERLIST = "muc#roomconfig_getmemberlist";
	public static final String MUC_ROOMCONFIG_PUBLICROOM = "muc#roomconfig_publicroom";
	public static final String MUC_ROOMCONFIG_PERSISTENTROOM = "muc#roomconfig_persistentroom";
	public static final String MUC_ROOMCONFIG_MODERATEDROOM = "muc#roomconfig_moderatedroom";
	public static final String MUC_ROOMCONFIG_MEMBERSONLY = "muc#roomconfig_membersonly";
	public static final String MUC_ROOMCONFIG_PASSWORDPROTECTEDROOM = "muc#roomconfig_passwordprotectedroom";
	public static final String MUC_ROOMCONFIG_ROOMSECRET = "muc#roomconfig_roomsecret";
	public static final String MUC_ROOMCONFIG_WHOIS = "muc#roomconfig_whois";
	public static final String MUC_ROOMCONFIG_MAXHISTORYFETCH = "muc#maxhistoryfetch";
	public static final String MUC_ROOMCONFIG_ROOMADMINS = "muc#roomconfig_roomadmins";
	public static final String MUC_ROOMCONFIG_ROOMOWNERS = "muc#roomconfig_roomowners";
	
	private XMPPTCPConnectionConfiguration config;
	private final String mucSubDomain;
	private final EntityFullJid userJid;
	private Resourcepart userNickname = null;
	private final AbstractXMPPConnection con;
	private final XMPPClientListener listener;
	private final ConversationHistory history;
	private final XmppsRosterListener rosterListener;
	private final XmppsICIncomingMessageListener icIncomingMessageListener;
	private final XmppsMUCInvitationListener mucInvitationListener;
	private final Map<EntityBareJid, IChat> instantChats = new HashMap<>();
	private final Map<EntityBareJid, GChat> groupChats = new HashMap<>();
	private int loginCount = 0;
	private final AtomicBoolean isDisconnecting = new AtomicBoolean(false);
	private Presence lastPresence = null;
	private ConcurrentHashMap<String, EntityBareJid> cacheInstantChatToFriend = new ConcurrentHashMap<>();
	
	static {
		ProviderManager.addExtensionProvider(OutOfBandData.ELEMENT_NAME, OutOfBandData.NAMESPACE, new OutOfBandData.Provider());
	}
	
	public XMPPClient(XMPPTCPConnectionConfiguration.Builder builder, String mucSubdomain, String nickname, XMPPClientListener listener) {
		this(builder, mucSubdomain, nickname, listener, null);
	}
	
	public XMPPClient(XMPPTCPConnectionConfiguration.Builder builder, String mucSubdomain, String nickname, XMPPClientListener listener, ConversationHistory history) {
		builder.setSendPresence(false);
		builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		this.config = builder.build();
		this.mucSubDomain = mucSubdomain;
		this.userJid = JidCreate.entityFullFrom(XMPPHelper.asLocalpart(config.getUsername()), config.getXMPPServiceDomain(), config.getResource());
		this.userNickname = XMPPHelper.asResourcepart(nickname);
		this.con = new XMPPTCPConnection(config);
		this.listener = listener;
		this.history = history;
		this.rosterListener = new XmppsRosterListener();
		this.icIncomingMessageListener = new XmppsICIncomingMessageListener();
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
	
	public void disconnect() throws XMPPClientException {
		synchronized(con) {
			if (!isConnected() && (loginCount == 0)) return;
			checkConnection();
			try {
				isDisconnecting.set(true);
				con.sendStanza(createOfflinePresence());
				internalLogout();
				con.disconnect();
				isDisconnecting.set(false);
			} catch(SmackException | InterruptedException ex) {
				throw new XMPPClientException(ex);
			}
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
		
		Presence presence = new Presence(PresenceStatus.presenceType(presenceStatus));
		//presence.setPriority(24);
		Presence.Mode mode = PresenceStatus.presenceMode(presenceStatus);
		if (mode != null) presence.setMode(mode);
		if (statusText != null) presence.setStatus(statusText);
		
		try {
			internalSendPresence(presence);
			
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
				final EntityBareJid friendJid = entry.getJid().asEntityBareJidOrThrow();
				final String instantChat = generateChatBareJid(friendJid).toString();
				cacheInstantChatToFriend.putIfAbsent(instantChat, friendJid);
				FriendPresence presence = getFriendPresence(friendJid);
				friends.add(new Friend(entry, presence, instantChat));
			}
			return friends;
			
		} catch(SmackException | InterruptedException ex) {
			throw new XMPPClientException(ex);
		}
	}
	
	public FriendPresence getFriendPresence(String friendBareJid) throws XMPPClientException {
		return getFriendPresence(XMPPHelper.asEntityBareJid(friendBareJid));
	}
	
	public FriendPresence getFriendPresence(EntityBareJid friend) throws XMPPClientException {
		checkAuthentication();
		
		Roster roster = Roster.getInstanceFor(con);
		Presence presence = roster.getPresence(friend.asBareJid());
		return (presence != null) ? new FriendPresence(presence, generateChatBareJid(friend).toString()) : null;
	}
	
	public String getFriendNickname(EntityBareJid friend) throws XMPPClientException {
		return getFriendNickname(friend, true);
	}
	
	public String getFriendNickname(EntityBareJid friend, boolean guessIfNull) throws XMPPClientException {
		checkAuthentication();
		
		Roster roster = Roster.getInstanceFor(con);
		return getRosterEntryNickname(roster, friend, guessIfNull);
	}
	
	public LastActivity getLastActivity(Jid friend) throws XMPPClientException {
		checkAuthentication();
		
		try {
			final LastActivityManager lastActivityManager = LastActivityManager.getInstanceFor(con);
			return lastActivityManager.getLastActivity(friend);
			
		} catch(SmackException | XMPPException | InterruptedException ex) {
			throw new XMPPClientException(ex);
		}
	}
	
	public EntityBareJid generateChatBareJid(String withUserBareJid) {
		return createInstantChatJid(withUserBareJid);
	}
	
	public EntityBareJid generateChatBareJid(EntityBareJid withUser) {
		return createInstantChatJid(withUser);
	}
	
	public List<ChatRoom> listChats() throws XMPPClientException {
		checkAuthentication();
		
		ArrayList<ChatRoom> chats = new ArrayList<>();
		synchronized(instantChats) {
			for(IChat chatObj : instantChats.values()) {
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
	
	public EntityBareJid newInstantChat(EntityBareJid withFriend) throws XMPPClientException {
		checkAuthentication();
		
		try {
			final Roster roster = getRoster();
			checkRosterLoaded(roster);
			
			final EntityBareJid chatJid = createInstantChatJid(withFriend);
			final String withUserNick = getRosterEntryNickname(roster, withFriend, true);
			
			synchronized(instantChats) {
				IChat chatObj = instantChats.get(chatJid);
				if (chatObj == null) {
					final ChatManager chatMgr = getChatManager();
					final Chat chat = chatMgr.chatWith(withFriend);
					chatObj = doAddChat(false, true, chatJid, userJid.asEntityBareJid(), withUserNick, null, withFriend, chat);
				}
			}
			return chatJid;
			
		} catch(SmackException | InterruptedException ex) {
			throw new XMPPClientException(ex);
		}
	}
	
	public EntityBareJid newGroupChat(String name, List<EntityBareJid> withFriends) throws XMPPClientException {
		checkAuthentication();
		
		final EntityBareJid myJid = userJid.asEntityBareJid();
		final EntityBareJid chatJid = createGroupChatJid();
		
		synchronized(groupChats) {
			GChat chatObj = groupChats.get(chatJid);
			if (chatObj == null) {
				try {
					final MultiUserChatManager muChatMgr = getMUChatManager();
					final MultiUserChat muc = muChatMgr.getMultiUserChat(chatJid);
					
					muc.create(XMPPHelper.asResourcepart(name));
					Form form = muc.getConfigurationForm();
					FillableForm fillableForm = form.getFillableForm();
					configureMucForm(fillableForm, chatJid, name, myJid);
					muc.sendConfigurationForm(fillableForm);
					muc.changeSubject(name);
					muc.join(userNickname);
					chatObj = doAddGroupChat(false, true, chatJid, myJid, name, null, muc);
					
					for(EntityBareJid withFriend : withFriends) {
						muc.invite(withFriend, name);
					}
					
				} catch(SmackException | XMPPException | InterruptedException ex) {
					throw new XMPPClientException(ex);
				}
			}
		}
		
		return chatJid;
	}
	
	public ChatRoom getChat(String chatJid) throws XMPPClientException {
		return getChat(XMPPHelper.asEntityBareJid(chatJid));
	}
	
	public ChatRoom getChat(EntityBareJid chatJid) throws XMPPClientException {
		checkAuthentication();
		
		if (isInstantChat(chatJid)) {
			IChat chatObj = instantChats.get(chatJid);
			return (chatObj != null) ? chatObj.getChatRoom() : null;
		} else {
			GChat chatObj = groupChats.get(chatJid);
			return (chatObj != null) ? chatObj.getChatRoom() : null;
		}
	}
	
	public void existChat(String chatJid) throws XMPPClientException {
		existChat(XMPPHelper.asEntityBareJid(chatJid));
	}
	
	public boolean existChat(EntityBareJid chatJid) throws XMPPClientException {
		checkAuthentication();
		
		if (isInstantChat(chatJid)) {
			return instantChats.containsKey(chatJid);
		} else {
			return groupChats.containsKey(chatJid);
		}
	}
	
	public void forgetChat(String chatJid) throws XMPPClientException {
		forgetChat(XMPPHelper.asEntityBareJid(chatJid));
	}
	
	public void forgetChat(EntityBareJid chatJid) throws XMPPClientException {
		checkAuthentication();
		
		if (isInstantChat(chatJid)) {
			synchronized(instantChats) {
				if (instantChats.containsKey(chatJid)) doRemoveChat(chatJid);
			}
			
		} else {
			try {
				synchronized(groupChats) {
					if (groupChats.containsKey(chatJid)) doRemoveGroupChat(chatJid);
				}
			} catch(XMPPException | SmackException | InterruptedException ex) {
				throw new XMPPClientException(ex);
			}
		}
	}
	
	public List<ChatMember> getChatMembers(String chatJid) throws XMPPClientException {
		return getChatMembers(XMPPHelper.asEntityBareJid(chatJid));
	}
	
	public List<ChatMember> getChatMembers(EntityBareJid chatJid) throws XMPPClientException {
		ArrayList<ChatMember> members = new ArrayList<>();
		
		checkAuthentication();
		
		final EntityBareJid myJid = userJid.asEntityBareJid();
		
		if (isInstantChat(chatJid)) {
			synchronized(instantChats) {
				IChat chatObj = instantChats.get(chatJid);
				if (chatObj == null) return null;
				
				try {
					final Roster roster = getRoster();
					checkRosterLoaded(roster);
					
					final String withNickname = getRosterEntryNickname(roster, chatObj.getChatRoom().getWithJid(), true);
					if (chatObj.getChatRoom().isOwner(myJid)) {
						members.add(new ChatMember(myJid, MemberRole.OWNER, userNickname.toString()));
						members.add(new ChatMember(chatObj.getChatRoom().getWithJid(), MemberRole.PARTECIPANT, withNickname));
					} else {
						members.add(new ChatMember(chatObj.getChatRoom().getWithJid(), MemberRole.OWNER, withNickname));
						members.add(new ChatMember(myJid, MemberRole.PARTECIPANT, userNickname.toString()));
					}
					
				} catch(SmackException | InterruptedException ex) {
					throw new XMPPClientException(ex);
				}
				return members;
			}
		} else {
			synchronized(groupChats) {
				GChat chatObj = groupChats.get(chatJid);
				if (chatObj == null) return null;
				
				try {
					final MultiUserChat muc = chatObj.getRawChat();
					final Roster roster = getRoster();
					checkRosterLoaded(roster);
					
					for(Affiliate aff : muc.getOwners()) {
						final EntityBareJid jid = aff.getJid().asEntityBareJidOrThrow();
						final String nick = getEntryNickname(roster, jid, true);
						members.add(new ChatMember(jid, MemberRole.OWNER, nick));
					}
					for(Affiliate aff : muc.getMembers()) {
						final EntityBareJid jid = aff.getJid().asEntityBareJidOrThrow();
						final String nick = getEntryNickname(roster, jid, true);
						members.add(new ChatMember(jid, MemberRole.PARTECIPANT, nick));
					}
					return members;
					
				} catch(XMPPException | SmackException | InterruptedException ex) {
					throw new XMPPClientException(ex);
				}
			}
		}
	}
	
	public FriendPresence getChatPresence(String chatJid) throws XMPPClientException {
		return getChatPresence(XMPPHelper.asEntityBareJid(chatJid));
	}
	
	public FriendPresence getChatPresence(EntityBareJid chatJid) throws XMPPClientException {
		checkAuthentication();
		
		if (isInstantChat(chatJid)) {
			synchronized(instantChats) {
				EntityBareJid presenceUser = null;
				IChat chatObj = instantChats.get(chatJid);
				if (chatObj == null) {
					presenceUser = cacheInstantChatToFriend.get(chatJid.toString());
					if (presenceUser == null) return null;
				} else {
					presenceUser = chatObj.getChatRoom().getWithJid();
				}
				return getFriendPresence(presenceUser);
			}
		} else {
			throw new UnsupportedOperationException("Feature not available for a groupChat");
		}
	}
	
	public ChatMessage sendTextMessage(String chatBareJid, String text) throws XMPPClientException {
		return sendTextMessage(XMPPHelper.asEntityBareJid(chatBareJid), text);
	}
	
	public ChatMessage sendTextMessage(EntityBareJid chatJid, String text) throws XMPPClientException {
		Message message = new Message();
		message.setBody(text);
		return internalSendMessage(chatJid, message);
	}
	
	public ChatMessage sendFileMessage(String chatBareJid, String name, String url, String mediaType, long size) throws XMPPClientException {
		return sendFileMessage(XMPPHelper.asEntityBareJid(chatBareJid), name, url, mediaType, size);
	}
	
	public ChatMessage sendFileMessage(EntityBareJid chatJid, String name, String url, String mediaType, long size) throws XMPPClientException {
		Message message = new Message();
		message.setBody(name);
		OutOfBandData oobData = new OutOfBandData(url, mediaType, size);
		message.addExtension(oobData);
		return internalSendMessage(chatJid, message);
	}
	
	private ChatMessage internalSendMessage(EntityBareJid chatJid, Message message) throws XMPPClientException {
		checkAuthentication();
		
		final EntityBareJid myJid = userJid.asEntityBareJid();
		final String myResource = XMPPHelper.asResourcepartString(userJid.getResourceOrNull());
		
		if (isInstantChat(chatJid)) {
			synchronized(instantChats) {
				IChat chatObj = instantChats.get(chatJid);
				if (chatObj == null) {
					EntityBareJid withUser = cacheInstantChatToFriend.get(chatJid.toString());
					if (withUser != null) {
						newInstantChat(withUser);
						chatObj = instantChats.get(chatJid);
					}
				}
				if (chatObj != null) {
					try {
						final DateTime now = ChatMessage.nowTimestamp();
						chatObj.getRawChat().send(message);
						if (logger.isTraceEnabled()) {
							logger.trace("Message sent [{}, {}]", now, StringUtils.abbreviate(message.getBody(), 20));
						}
						ChatMessage chatMessage = new ChatMessage(chatJid, myJid, myResource, userNickname.toString(), now, now, message);

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
			
		} else {
			synchronized(groupChats) {
				GChat chatObj = groupChats.get(chatJid);
				if (chatObj != null) {
					try {
						final DateTime now = ChatMessage.nowTimestamp();
						chatObj.getRawChat().sendMessage(message);
						if (logger.isTraceEnabled()) {
							logger.trace("Message sent [{}, {}]", now, StringUtils.abbreviate(message.getBody(), 20));
						}
						ChatMessage chatMessage = new ChatMessage(chatJid, myJid, myResource, userNickname.toString(), now, now, message);

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
		}
		
		throw new XMPPClientException("Chat not found. Please create a chat before try to send messages in it!");
	}
	
	private IChat doAddChat(boolean skipListeners, boolean self, EntityBareJid chatJid, EntityBareJid ownerJid, String name, DateTime lastSeenActivity, EntityBareJid withJid, Chat chat) {
		return doAddChat(skipListeners, self, new InstantChatRoom(chatJid, ownerJid, name, lastSeenActivity, withJid), chat);
	}
	
	private IChat doAddChat(boolean skipListeners, boolean self, InstantChatRoom chatRoom, Chat chat) {
		logger.debug("Adding instant chat [{}]", chatRoom.getChatJid().toString());
		IChat chatObj = new IChat(chatRoom, chat);
		instantChats.put(chatRoom.getChatJid(), chatObj);
		
		if (!skipListeners) {
			try {
				listener.onChatRoomAdded(chatObj.getChatRoom(), getChatOwnerNick(chatRoom), self);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}
		
		return chatObj;
	}
	
	private String getChatOwnerNick(ChatRoom chatRoom) throws XMPPClientException {
		if (XMPPHelper.jidEquals(chatRoom.getOwnerJid(), getUserJid())) {
			return getUserNickame();
		} else {
			return getFriendNickname(chatRoom.getOwnerJid(), true);
		}
	}
	
	private void doRemoveChat(EntityBareJid chatJid) {
		logger.debug("Removing instant chat [{}]", chatJid.toString());
		IChat chatObj = instantChats.remove(chatJid);
		
		try {
			final ChatRoom chatRoom = chatObj.getChatRoom();
			listener.onChatRoomRemoved(chatObj.getChatRoom().getChatJid(), chatRoom.getName(), chatRoom.getOwnerJid(), getChatOwnerNick(chatRoom));
		} catch(Throwable t) {
			logger.error("Listener error", t);
		}
	}
	
	private GChat doAddGroupChat(boolean skipListeners, boolean self, EntityBareJid chatJid, EntityBareJid ownerJid, String name, DateTime lastSeenActivity, MultiUserChat chat) {
		return doAddGroupChat(skipListeners, self, new GroupChatRoom(chatJid, ownerJid, name, lastSeenActivity), chat);
	}
	
	private GChat doAddGroupChat(boolean skipListeners, boolean self, GroupChatRoom chatRoom, MultiUserChat chat) {
		logger.debug("Creating group chat [{}]", chatRoom.getChatJid().toString());
		GChat chatObj = new GChat(chatRoom);
		groupChats.put(chatRoom.getChatJid(), chatObj);
		
		if (!skipListeners) {
			try {
				listener.onChatRoomAdded(chatObj.getChatRoom(), getChatOwnerNick(chatRoom), self);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}
		
		chat.addSubjectUpdatedListener(chatObj);
		chat.addMessageListener(chatObj);
		chat.addUserStatusListener(chatObj);
		chat.addParticipantStatusListener(chatObj);
		
		return chatObj;
	}
	
	private void doRemoveGroupChat(EntityBareJid chatJid) throws XMPPException, SmackException, InterruptedException {
		logger.debug("Removing group chat [{}]", chatJid.toString());
		GChat chatObj = groupChats.remove(chatJid);
		
		MultiUserChat chat = chatObj.getRawChat();
		chat.removeSubjectUpdatedListener(chatObj);
		chat.removeMessageListener(chatObj);
		chat.removeUserStatusListener(chatObj);
		chat.removeParticipantStatusListener(chatObj);
		if (XMPPHelper.jidEquals(chatObj.getChatRoom().getOwnerJid(), getUserJid().asEntityBareJid())) {
			chat.destroy("Cleanup", null);
		}
		
		try {
			final ChatRoom chatRoom = chatObj.getChatRoom();
			listener.onChatRoomRemoved(chatObj.getChatRoom().getChatJid(), chatRoom.getName(), chatRoom.getOwnerJid(), getChatOwnerNick(chatRoom));
		} catch(Throwable t) {
			logger.error("Listener error", t);
		}
	}
	
	private Presence createOfflinePresence() {
		Presence presence = null;
		if (this.lastPresence != null) {
			presence = this.lastPresence.clone();
			presence.setType(Presence.Type.unavailable);
		} else {
			presence = new Presence(Presence.Type.unavailable);
		}
		presence.setMode(Presence.Mode.away);
		//presence.setPriority(24);
		return presence;
	}
	
	private void checkRosterLoaded(final Roster roster) throws SmackException, InterruptedException {
		if (!roster.isLoaded()) roster.reloadAndWait();
	}
	
	private String chatDomain(String subname) {
		return subname + "." + userJid.getDomain().toString();
	}
	
	private EntityBareJid createInstantChatJid(EntityBareJid friend) {
		return (friend == null) ? null : createInstantChatJid(friend.toString());
	}
	
	private EntityBareJid createInstantChatJid(String friendBareJid) {
		return buildChatJid(DigestUtils.md5Hex(friendBareJid), chatDomain(CHAT_DOMAIN_PREFIX));
	}
	
	private EntityBareJid createGroupChatJid() {
		return buildChatJid(IdentifierUtils.getUUIDTimeBased(true), chatDomain(mucSubDomain));
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
	
	private EntityBareJid createOccupantJid(String nick) {
		return JidCreate.entityBareFrom(XMPPHelper.asLocalpart(nick), userJid.getDomain());
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
	
	private String getEntryNickname(Roster roster, BareJid jid, boolean guessIfNull) {
		if (XMPPHelper.jidBareEquals(jid, userJid)) {
			return userNickname.toString();
		} else {
			return getRosterEntryNickname(roster, jid, guessIfNull);
		}
	}
	
	private String getRosterEntryNickname(Roster roster, BareJid jid, boolean guessIfNull) {
		RosterEntry entry = roster.getEntry(jid);
		String nick = (entry != null) ? entry.getName() : null;
		if (StringUtils.isBlank(nick)) {
			return guessIfNull ? XMPPHelper.buildGuessedString(jid.toString()) : null;
		} else {
			return nick;
		}
	}
	
	private void configureMucForm(FillableForm fillableForm, EntityBareJid chatJid, String chatName, EntityBareJid ownerJid) {
		// Is still useful is this new version?
		// Apply defaults for all form fields
		/*
		for (FormField field : fillableForm.getDataForm().getFields()) {
			if (field.getType() == FormField.Type.hidden || StringUtils.isBlank(field.getFieldName())) {
				continue;
			}
			fillableForm.setDefaultAnswer(field.getFieldName());
		}
		*/
		
		/*
		public void setDefaultAnswer(String variable) {
			if (!isSubmitType()) {
				throw new IllegalStateException("Cannot set an answer if the form is not of type " +
				"\"submit\"");
			}
			FormField field = getField(variable);
			if (field != null) {
				// Clear the old values
				field.resetValues();
				// Set the default value
				for (String value : field.getValues()) {
					field.addValue(value);
				}
			}
			else {
				throw new IllegalArgumentException("Couldn't find a field for the specified variable.");
			}
		}
		*/
		
		// Natural-Language Room Name
		fillableForm.setAnswer(MUC_ROOMCONFIG_ROOMNAME, chatName);
		// Short Description of Room
		fillableForm.setAnswer(MUC_ROOMCONFIG_ROOMDESC, "");
		// Enable Public Logging?
		if (fillableForm.hasField(MUC_ROOMCONFIG_ENABLELOGGING)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_ENABLELOGGING, false);
		}
		// Allow Occupants to Change Subject?
		if (fillableForm.hasField(MUC_ROOMCONFIG_CHANGESUBJECT)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_CHANGESUBJECT, false);
		}
		// Allow Occupants to Invite Others?
		if (fillableForm.hasField(MUC_ROOMCONFIG_ALLOWINVITES)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_ALLOWINVITES, false);
		}
		// Who Can Send Private Messages? (anyone, participants, moderators, none)
		if (fillableForm.hasField(MUC_ROOMCONFIG_ALLOWPM)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_ALLOWPM, XMPPHelper.asFormListSingleType("none"));
		}
		// Maximum Number of Occupants (10, 20, 30, 50, 100, none)
		if (fillableForm.hasField(MUC_ROOMCONFIG_MAXUSERS)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_MAXUSERS, XMPPHelper.asFormListSingleType("50"));
		}
		// Roles for which Presence is Broadcasted (moderator, participant, visitor)
		/*
		if (answerForm.hasField(MUC_ROOMCONFIG_PRESENCEBROADCAST)) {
			answerForm.setAnswer(MUC_ROOMCONFIG_PRESENCEBROADCAST, XMPPHelper.asFormListMultiType("participant"));
		}
		*/
		// Roles and Affiliations that May Retrieve Member List (moderator, participant, visitor)
		if (fillableForm.hasField(MUC_ROOMCONFIG_GETMEMBERLIST)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_GETMEMBERLIST, XMPPHelper.asFormListMultiType("participant"));
		}
		// Make Room Publicly Searchable?
		if (fillableForm.hasField(MUC_ROOMCONFIG_PUBLICROOM)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_PUBLICROOM, false);
		}
		// Make Room Persistent?
		if (fillableForm.hasField(MUC_ROOMCONFIG_PERSISTENTROOM)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_PERSISTENTROOM, true);
		}
		// Make Room Moderated?
		if (fillableForm.hasField(MUC_ROOMCONFIG_MODERATEDROOM)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_MODERATEDROOM, false);
		}
		// Make Room Members Only?
		if (fillableForm.hasField(MUC_ROOMCONFIG_MEMBERSONLY)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_MEMBERSONLY, true);
		}
		// Password Required for Entry?
		if (fillableForm.hasField(MUC_ROOMCONFIG_PASSWORDPROTECTEDROOM)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_PASSWORDPROTECTEDROOM, false);
		}
		// Who May Discover Real JIDs? (moderators, anyone)
		if (fillableForm.hasField(MUC_ROOMCONFIG_WHOIS)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_WHOIS, XMPPHelper.asFormListSingleType("anyone"));
		}
		// Maximum Number of History Messages Returned by Room
		if (fillableForm.hasField(MUC_ROOMCONFIG_MAXHISTORYFETCH)) {
			fillableForm.setAnswer(MUC_ROOMCONFIG_MAXHISTORYFETCH, "50");
		}
		// Additional Room Admins
		// Room Owners
		if (fillableForm.hasField(MUC_ROOMCONFIG_ROOMOWNERS)) {
			Set<EntityBareJid> owners = new HashSet<>();
			owners.add(ownerJid);
			fillableForm.setAnswer(MUC_ROOMCONFIG_ROOMOWNERS, JidUtil.toStringList(owners));
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("Dumping MUC configuration...");
			for(FormField field : fillableForm.getDataForm().getFields()) {
				logger.trace("{}: {}", field.getFieldName(), field.getValues());
			}
		}
	}
	
	private boolean joinMuc(final MultiUserChat muc, final DateTime lastSeenActivity) {
		try {
			logger.debug("Joining group chat [{}, {}]", muc.getRoom().toString(), lastSeenActivity);
			MucEnterConfiguration.Builder builder = muc.getEnterConfigurationBuilder(userNickname)
					.withPassword(null);
			if (lastSeenActivity != null) {
				DateTime now = new DateTime(DateTimeZone.UTC);
				Seconds seconds = Seconds.secondsBetween(lastSeenActivity.withZone(DateTimeZone.UTC), now);
				builder = builder.requestHistorySince(Math.abs(seconds.getSeconds()));
			}
			muc.join(builder.build());
			return true;

		} catch(SmackException | XMPPException | InterruptedException ex) {
			logger.error("Error joining group chat", ex);
			return false;
		}
	}
	
	private GuessMucFromResult guessMucFrom(Roster roster, MultiUserChat muc, EntityFullJid rawFrom) {
		EntityFullJid fromJid = null;
		String fromNick = null;
		
		Occupant occupant = muc.getOccupant(rawFrom);
		if (occupant != null) {
			fromJid = occupant.getJid().asEntityFullJidIfPossible();
			fromNick = getRosterEntryNickname(roster, occupant.getJid().asBareJid(), false);
			if (StringUtils.isBlank(fromNick)) {
				fromNick = XMPPHelper.buildGuessedString(occupant.getJid().asBareJid());
			}
			
		} else {
			// Due to occupant is not available, we assume that nick in message
			// sender is reconducible to a domain user...
			final EntityBareJid occupantBareJid = createOccupantJid(XMPPHelper.asResourcepartString(rawFrom));
			fromJid = JidCreate.entityFullFrom(occupantBareJid, XMPPHelper.asResourcepart(rawFrom.asEntityBareJidString()));
			fromNick = getRosterEntryNickname(roster, occupantBareJid, false);
			if (StringUtils.isBlank(fromNick)) {
				fromNick = XMPPHelper.buildGuessedString(XMPPHelper.asResourcepartString(rawFrom.getResourceOrNull()));
			}
		}
		return new GuessMucFromResult(fromJid, fromNick);
	}
	
	private DateTime retrieveDelayInformation(Message message) {
		DelayInformation dinfo = DelayInformation.from(message);
		if (dinfo != null) {
			return new DateTime(dinfo.getStamp(), DateTimeZone.UTC);
		} else {
			return null;
		}
	}
	
	private void internalLogin() throws SmackException, XMPPException, InterruptedException, IOException {
		final boolean restoreHistory = (loginCount == 0);
		loginCount++;
		
		final Roster roster = getRoster();
		roster.addRosterListener(rosterListener);
		final ChatManager chatMgr = getChatManager();
		chatMgr.addIncomingListener(icIncomingMessageListener);
		final MultiUserChatManager muChatMgr = getMUChatManager();
		muChatMgr.addInvitationListener(mucInvitationListener);
		
		// Restore instant (one-to-one) chat rooms from history
		if (restoreHistory && (history != null)) {
			for(ChatRoom chat : history.getChats()) {
				if (chat instanceof InstantChatRoom) {
					final InstantChatRoom chatObj = (InstantChatRoom)chat;
					doAddChat(true, true, chatObj, null);
				}
			}
		}
		
		con.login();
		checkRosterLoaded(roster);
		
		// Restore previous multi chat rooms from history
		if (restoreHistory && (history != null)) {
			for(ChatRoom chat : history.getChats()) {
				if (chat instanceof GroupChatRoom) {
					final GroupChatRoom chatRoom = (GroupChatRoom)chat;
					
					RoomInfo info = getRoomInfo(muChatMgr, chat.getChatJid());
					if (info != null) {
						final MultiUserChat muc = muChatMgr.getMultiUserChat(chat.getChatJid());
						doAddGroupChat(true, true, chatRoom, muc);
					} else {
						logger.debug("Group chat unavailable [{}, {}]", chatRoom.getChatJid(), chatRoom.getName());
						try {
							listener.onChatRoomUnavailable(chatRoom, getChatOwnerNick(chatRoom));
						} catch(Throwable t) {
							logger.error("Listener error", t);
						}
					}
				}
			}
		}
		
		for(IChat chatObj : instantChats.values()) {
			final EntityBareJid withJid = chatObj.getChatRoom().getWithJid().asEntityBareJid();
			String nick = chatObj.getChatRoom().getName();
			String friendNick = getRosterEntryNickname(roster, withJid, false);
			if (friendNick != null) nick = friendNick;
			if (StringUtils.isBlank(nick)) nick = XMPPHelper.buildGuessedString(withJid.toString());
			if (!StringUtils.equals(chatObj.getChatRoom().getName(), nick)) {
				// Nick updated
			}
		}
		
		for(GChat chatObj : groupChats.values()) {
			final MultiUserChat muc = chatObj.getRawChat();
			if (!muc.isJoined()) {
				boolean joined = joinMuc(muc, chatObj.getChatRoom().getLastSeenActivity());
				if (joined) logger.debug("Group chat re-joined [{}]", muc.getNickname());
			}
		}
		
		if (this.lastPresence != null) {
			internalSendPresence(this.lastPresence);
		}
	}
	
	private void internalSendPresence(Presence presence) throws SmackException, InterruptedException {
		this.lastPresence = presence;
		con.sendStanza(presence);
	}
	
	private RoomInfo getRoomInfo(MultiUserChatManager mucManager, EntityBareJid chatJid) throws SmackException, XMPPException, InterruptedException, IOException {
		try {
			return mucManager.getRoomInfo(chatJid);
		} catch(XMPPException.XMPPErrorException ex) {
			if (ex.getStanzaError().getCondition() == StanzaError.Condition.item_not_found) {
				return null;
			} else {
				throw ex;
			}
		} catch(SmackException | XMPPException | InterruptedException ex) {
			throw ex;
		}
	}
	
	private void internalLogout() {
		final MultiUserChatManager muChatMgr = getMUChatManager();
		muChatMgr.removeInvitationListener(mucInvitationListener);
		final ChatManager chatMgr = getChatManager();
		chatMgr.removeIncomingListener(icIncomingMessageListener);
		final Roster roster = getRoster();
		roster.removeRosterListener(rosterListener);
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
			try {
				logger.debug("Entry added [{}]", clctn);
				listener.onFriendsAdded(clctn);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void entriesUpdated(Collection<Jid> clctn) {
			if (isDisconnecting.get()) return;
			try {
				logger.debug("Entry updated [{}]", clctn);
				listener.onFriendsUpdated(clctn);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void entriesDeleted(Collection<Jid> clctn) {
			if (isDisconnecting.get()) return;
			try {
				logger.debug("Entry deleted [{}]", clctn);
				listener.onFriendsDeleted(clctn);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void presenceChanged(Presence prsnc) {
			if (isDisconnecting.get()) return;
			final EntityBareJid friendJid = prsnc.getFrom().asEntityBareJidIfPossible();
			final FriendPresence presence = new FriendPresence(prsnc, generateChatBareJid(friendJid).toString());
			logger.debug("Presence changed [{}, {}]", EnumUtils.toSerializedName(presence.getPresenceStatus()), presence.getFriendFullJid());
			try {
				listener.onFriendPresenceChanged(prsnc.getFrom(), presence, getFriendPresence(friendJid));
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}
	}
	
	private class XmppsICIncomingMessageListener implements IncomingChatMessageListener {

		@Override
		public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
			if (isDisconnecting.get()) return;
			
			try {
				final Roster roster = getRoster();
				checkRosterLoaded(roster);

				final EntityBareJid chatJid = createInstantChatJid(from);
				final String fromNick = getRosterEntryNickname(roster, from, true);

				IChat chatObj = null;
				synchronized(instantChats) {
					chatObj = instantChats.get(chatJid);
					if (chatObj == null) {
						chatObj = doAddChat(false, false, chatJid, from, fromNick, ChatMessage.nowTimestamp(), from, chat);
					}
				}
				
				final DateTime delay = retrieveDelayInformation(message);
				final DateTime now = ChatMessage.nowTimestamp();
				chatObj.getChatRoom().setLastSeenActivity(now);
				
				DateTime msgTs, delivTs;
				if (delay != null) {
					msgTs = delay;
					delivTs = now;
				} else {
					msgTs = now;
					delivTs = now;
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace("Message received [{}, {}, {}]", msgTs, delivTs, StringUtils.abbreviate(message.getBody(), 20));
				}

				try {
					ChatMessage chatMessage = new ChatMessage(chatJid, message.getFrom(), fromNick, msgTs, delivTs, message);
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
			final String name = reason;
			
			logger.debug("Group chat invitation received [{}, {}]", chatJid.toString(), inviterJid.toString());
			MultiUserChat muc = muChatMgr.getMultiUserChat(chatJid);
			
			GChat chatObj = null;
			synchronized(groupChats) {
				chatObj = groupChats.get(chatJid);
				if (chatObj == null) {
					chatObj = doAddGroupChat(false, false, chatJid, inviterJid.asEntityBareJid(), name, null, muc);
				}
				
				if (!muc.isJoined()) {
					boolean joined = joinMuc(muc, null);
					if (joined) logger.debug("Group chat re-joined [{}]", muc.getNickname());
				} else {
					logger.warn("Unexpected! Group chat already joined [{}]", multiUserChat.getNickname());
				}
			}
		}
	}
	
	private class IChat {
		private final InstantChatRoom chatRoom;
		private Chat rawChat;
		
		public IChat(InstantChatRoom chatRoom) {
			this(chatRoom, null);
		}
		
		public IChat(InstantChatRoom chatRoom, Chat rawChat) {
			this.chatRoom = chatRoom;
			this.rawChat = rawChat;
		}
		
		public InstantChatRoom getChatRoom() {
			return chatRoom;
		}
		
		public synchronized Chat getRawChat() {
			if (rawChat == null) {
				rawChat = getChatManager().chatWith(chatRoom.getWithJid());
			}
			return rawChat;
		}
	}
	
	private class GChat implements SubjectUpdatedListener, MessageListener, UserStatusListener, ParticipantStatusListener {
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
			chatRoom.setName(subject);
			try {
				listener.onChatRoomUpdated(chatRoom, false);
			} catch(Throwable t) {
				logger.error("Listener error", t);
			}
		}

		@Override
		public void processMessage(Message message) {
			final Roster roster = getRoster();
			
			if (message.getSubjects().size() > 0) {
				logger.warn("Subject update {}", message);
				
			} else if (!StringUtils.isBlank(message.getBody())) {
				boolean skipListener = false;
				
				// In groupchat the sender jid is not directly reconducible to
				// an internal roster jid: it is like to "chat@conference.domain.tld/nick"
				// Instead of above, we want fromJid like this: "user@domain.tld/resource"
				// NB: Now we assume that the nick provided is reconducible to a real user!
				final Jid rawFromJid = message.getFrom();
				final GuessMucFromResult guessedFrom = guessMucFrom(roster, getRawChat(), rawFromJid.asEntityFullJidOrThrow());
				
				// Ignore messages coming from myself
				if (XMPPHelper.jidEquals(guessedFrom.jid, getUserJid())) skipListener = true;
				
				final DateTime delay = retrieveDelayInformation(message);
				if (!skipListener) {
					if (delay != null && history != null) {
						final HashSet<String> idsInHistory = history.getStanzaIds(getChatRoom().getChatJid());
						if (idsInHistory != null) {
							skipListener = idsInHistory.contains(message.getStanzaId());
						}
					}
				}
				
				final DateTime now = ChatMessage.nowTimestamp();
				chatRoom.setLastSeenActivity(now);
				
				DateTime msgTs, delivTs;
				if (delay != null) {
					msgTs = delay;
					delivTs = now;
				} else {
					msgTs = now;
					delivTs = now;
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace("Message received [{}, {}, {}]", msgTs, delivTs, StringUtils.abbreviate(message.getBody(), 20));
				}
				
				if (!skipListener) {
					try {
						ChatMessage chatMessage = new ChatMessage(chatRoom.getChatJid(), guessedFrom.jid.asEntityBareJidIfPossible(), XMPPHelper.asResourcepartString(guessedFrom.jid.getResourceOrNull()), guessedFrom.nick, msgTs, delivTs, message);
						listener.onChatRoomMessageReceived(chatRoom, chatMessage);
					} catch(Throwable t) {
						logger.error("Listener error", t);
					}
				}
				
				// In groupchat the sender jid is not directly reconducible to
				// an internal roster jid: it is like to "chat@conference.domain.top/nick"
				// Instead of above, we want fromJid like this: "user@domain.top/resource"
				//final Jid rawFromJid = message.getFrom();
				// We need to get firstly the occupant and then guess the real jid
				// and its nickname from internal roster.
				//final Occupant occupant = getRawChat().getOccupant(rawFromJid.asEntityFullJidOrThrow());
				//final EntityFullJid fromJid = occupant.getJid().asEntityFullJidIfPossible();
				//final String fromNick = guessOccupantNick(roster, occupant);
				//final EntityFullJid fromJid = guessOccupantJid(getRawChat(), rawFromJid.asEntityFullJidOrThrow());
				//final String fromNick = guessOccupantNick(roster, getRawChat(), rawFromJid.asEntityFullJidOrThrow());
				//final Jid fromJid = message.getFrom();
				//final String fromNick = getRosterEntryNickname(roster, fromJid.asBareJid(), true);
				/*
				if (!XMPPHelper.jidEquals(fromJid, getUserJid())) {
					try {
						ChatMessage chatMessage = new ChatMessage(chatRoom.getChatJid(), fromJid.asEntityBareJidIfPossible(), XMPPHelper.asResourcepartString(fromJid.getResourceOrNull()), fromNick, ts, message);
						listener.onChatRoomMessageReceived(chatRoom, chatMessage);
					} catch(Throwable t) {
						logger.error("Listener error", t);
					}
				}
				*/
				
			} else {
				logger.warn("Empty message {}", message);
			}
		}
		
		@Override
		public void kicked(Jid actor, String reason) {}

		@Override
		public void voiceGranted() {}

		@Override
		public void voiceRevoked() {}

		@Override
		public void banned(Jid actor, String reason) {}

		@Override
		public void membershipGranted() {}

		@Override
		public void membershipRevoked() {}

		@Override
		public void moderatorGranted() {}

		@Override
		public void moderatorRevoked() {}

		@Override
		public void ownershipGranted() {}

		@Override
		public void ownershipRevoked() {}

		@Override
		public void adminGranted() {}

		@Override
		public void adminRevoked() {}

		@Override
		public void roomDestroyed(MultiUserChat alternateMuc, String reason) {
			/*
			if (isDisconnecting.get()) return;
			final EntityBareJid chatJid = getChatRoom().getChatJid();
			
			logger.debug("Group chat destroyed [{}, {}]", chatJid.toString(), reason);
			try {
				synchronized(groupChats) {
					if (groupChats.containsKey(chatJid)) doRemoveGroupChat(false, chatJid);
				}
			} catch(XMPPException | SmackException | InterruptedException ex) {
				logger.error("UserStatusListener error", ex);
			}
			*/
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
		public void membershipGranted(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void membershipRevoked(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void moderatorGranted(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void moderatorRevoked(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void ownershipGranted(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void ownershipRevoked(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void adminGranted(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void adminRevoked(EntityFullJid efj) {logger.debug("{}",efj.toString());}

		@Override
		public void nicknameChanged(EntityFullJid efj, Resourcepart r) {logger.debug("{}",efj.toString());}
	}
	
	public static boolean isGroupChat(EntityBareJid chatJid) {
		return isGroupChat(chatJid.toString());
	}
	
	public static boolean isGroupChat(String chatEntityBareJid) {
		return !isInstantChat(chatEntityBareJid);
	}
	
	public static boolean isInstantChat(EntityBareJid chatJid) {
		return isInstantChat(chatJid.toString());
	}
	
	public static boolean isInstantChat(String chatEntityBareJid) {
		return StringUtils.contains(chatEntityBareJid, "@"+CHAT_DOMAIN_PREFIX+".");
	}
	
	private final class GuessMucAffiliateResult {
		public EntityBareJid jid;
		public String nick;
		
		public GuessMucAffiliateResult(EntityBareJid jid, String nick) {
			this.jid = jid;
			this.nick = nick;
		}
	}
	
	private final class GuessMucFromResult {
		public EntityFullJid jid;
		public String nick;
		
		public GuessMucFromResult(EntityFullJid jid, String nick) {
			this.jid = jid;
			this.nick = nick;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	
	private EntityFullJid guessOccupantJid(MultiUserChat muc, EntityFullJid messageFrom) {
		Occupant occupant = muc.getOccupant(messageFrom);
		return (occupant != null) ? occupant.getJid().asEntityFullJidIfPossible() : messageFrom;
	}
	
	private String guessOccupantNick(Roster roster, MultiUserChat muc, EntityFullJid messageFrom) {
		Occupant occupant = muc.getOccupant(messageFrom);
		if (occupant != null) {
			String nick = getRosterEntryNickname(roster, occupant.getJid().asBareJid(), false);
			if (!StringUtils.isBlank(nick)) return nick;

			nick = XMPPHelper.asResourcepartString(occupant.getNick());
			if (!StringUtils.isBlank(nick)) return nick;

			return XMPPHelper.buildGuessedString(occupant.getJid().asBareJid());
		} else {
			return XMPPHelper.asResourcepartString(messageFrom.getResourceOrNull());
		}
	}
	
	private String guessOccupantNick(Roster roster, Occupant occupant) {
		String nick = getRosterEntryNickname(roster, occupant.getJid().asBareJid(), false);
		if (!StringUtils.isBlank(nick)) return nick;

		nick = XMPPHelper.asResourcepartString(occupant.getNick());
		if (!StringUtils.isBlank(nick)) return nick;

		return XMPPHelper.buildGuessedString(occupant.getJid().asBareJid());
	}
	
	
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
