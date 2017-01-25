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
package com.sonicle.webtop.core.app;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.bol.OMessageQueue;
import com.sonicle.webtop.core.dal.MessageQueueDAO;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class SessionComManager {
	private final static Logger logger = WT.getLogger(SessionComManager.class);
	private SessionManager sesm = null;
	private final String sessionId;
	private final UserProfile.Id profileId;
	private final ArrayDeque<ServiceMessage> messageQueue = new ArrayDeque<>();
	
	public SessionComManager(SessionManager sesm, String sessionId, UserProfile.Id profileId) {
		this.sesm = sesm;
		this.sessionId = sessionId;
		this.profileId = profileId;
		
		Connection con = null;
		
		try {
			String pid = UUID.randomUUID().toString();
			MessageQueueDAO mqdao = MessageQueueDAO.getInstance();
			
			con = WT.getCoreConnection();
			// Mark user's messages as "under process" and gets them
			mqdao.updatePidIfNullByDomainUser(con, profileId.getDomainId(), profileId.getUserId(), pid);
			List<OMessageQueue> queued = mqdao.selectByPid(con, pid);
			
			if(!queued.isEmpty()) {
				// Add offline messages into current session queue
				Class clazz = null;
				ArrayList<ServiceMessage> offlines = new ArrayList<>();
				for(OMessageQueue message : queued) {
					try {
						clazz = Class.forName(message.getMessageType());
						offlines.add((ServiceMessage)JsonResult.gson.fromJson(message.getMessageRaw(), clazz));
					} catch(Exception ex1) {
						logger.warn("Unable to unserialize message [{}] for [{}]", message.getMessageType(), profileId.toString(), ex1);
					}
				}
				//TODO: valutare l'ipotesi di imporre un limite ai messaggi accodati
				enqueueMessages(offlines);
				
				// Cleanup processed messages
				mqdao.deleteByPid(con, pid);
			}
			
		} catch(Exception ex) {
			logger.error("Error adding offline messages", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void notify(ServiceMessage message) {
		notify(Arrays.asList(new ServiceMessage[]{message}));
	}
	
	public void notify(List<ServiceMessage> messages) {
		if(!sesm.pushData(sessionId, messages)) {
			enqueueMessages(messages);
		}
	}
	
	/*
	public List<String> popEnqueuedMessages() {
		ArrayList<String> messages = new ArrayList();
		synchronized(messageQueue) {
			while(!messageQueue.isEmpty()) {
				messages.add(messageQueue.pollFirst());
			}
			logger.trace("Dequeued {} messages", messages.size());
			logger.trace("Queue now contains {} messages", messageQueue.size());
		}
		return messages;
	}
	
	private void enqueueMessages(String[] messages) {
		synchronized(messageQueue) {
			for(String message : messages) {
				logger.trace("Queuing message");
				messageQueue.addLast(message);
			}
			logger.trace("Enqueued {} messages", messages.length);
			logger.trace("Queue now contains {} messages", messageQueue.size());
		}
	}
	
	
	private void enqueueMessages(List<ServiceMessage> messages) {
		synchronized(messageQueue) {
			for(ServiceMessage message : messages) {
				logger.trace("Queuing message");
				messageQueue.addLast(JsonResult.gson.toJson(message));
			}
			logger.trace("Enqueued {} messages", messages.size());
			logger.trace("Queue now contains {} messages", messageQueue.size());
		}
	}
	*/
	
	public List<ServiceMessage> popEnqueuedMessages() {
		ArrayList<ServiceMessage> messages = new ArrayList();
		synchronized(messageQueue) {
			while(!messageQueue.isEmpty()) {
				messages.add(messageQueue.pollFirst());
			}
			logger.trace("Dequeued {} messages", messages.size());
			logger.trace("Queue now contains {} messages", messageQueue.size());
		}
		return messages;
	}
	
	private void enqueueMessages(List<ServiceMessage> messages) {
		synchronized(messageQueue) {
			for(ServiceMessage message : messages) {
				messageQueue.addLast(message);
			}
			logger.trace("Enqueued {} messages", messages.size());
			logger.trace("Queue now contains {} messages", messageQueue.size());
		}
	}
	
}
