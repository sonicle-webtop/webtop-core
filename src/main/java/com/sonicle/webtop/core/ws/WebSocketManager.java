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
package com.sonicle.webtop.core.ws;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.WebTopSession;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.Encryption;
import com.sonicle.webtop.core.sdk.Service;
import com.sonicle.webtop.core.sdk.WebSocketMessage;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gbulfon
 */
		
@ServerEndpoint(value = "/wsmanager", configurator=WebSocketManagerConfigurator.class)
public class WebSocketManager {
	
	public final static Logger logger = (Logger) LoggerFactory.getLogger(WebSocketManager.class);
	
    private Session wsSession;
    private HttpSession httpSession;
	private WebTopSession wts;
	private boolean ticketValidated=false;
	
	@OnOpen
    public void open(Session session, EndpointConfig config) {
        this.wsSession = session;
        this.httpSession = (HttpSession) config.getUserProperties()
                                           .get(HttpSession.class.getName());
		wts=WebTopSession.get(this.httpSession);
		wts.setWebSocketManager(this);
    }
	
	@OnMessage
	public void gotMessage(String json) {
		try {
			logger.debug("gotMessage : {}",json);
			if (wsSession!=null && wsSession.isOpen()) {
				WebSocketMessage wsm=JsonResult.gson.fromJson(json, WebSocketMessage.class);
				//core message
				if (wsm.service.equals(CoreManifest.ID)) {
					switch(wsm.action) {
						case TicketMessage.ACTION_TICKET:
							TicketMessage tm=JsonResult.gson.fromJson(json, TicketMessage.class);
							ticketValidated=isTicketValid(tm);
							if (!ticketValidated) sendError("The authorizazion ticket is not valid!");
							else sendInformation("The authorization ticket has been accepted!");
							break;
					}
				//service message
				} else {
					if (ticketValidated) {
						Service wtservice=wts.getServiceById(wsm.service);
						logger.debug("Found service object {}. Sending json :\n",wtservice.getClass(),json);
					} else {
						sendError("No authorization ticket has been validated yet!");
					}
				}
			}
		} catch (IOException e) {
			logger.error("error on gotMessage!",e);
			try {
				wsSession.close();
			} catch (IOException e1) {
				// Ignore
			}
		}
	}	
	
	private boolean isTicketValid(TicketMessage tm) {
		boolean isValid=false;
		Connection con=null;
		try {
			con=WebTopApp.getInstance().getConnectionManager().getConnection();
			OUser ouser=UserDAO.getInstance().selectByDomainUser(con, tm.domainId, tm.userId);
			if (ouser!=null) {
				String sid=Encryption.decipher(tm.encAuthTicket, ouser.getSecret());
				if (httpSession.getId().equals(sid)) {
					isValid=true;
				}
			}
			
		} catch(Exception exc) {
			logger.error("Error during ticket management for {}@{}",tm.userId,tm.domainId,exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return isValid;
	}
	
	public void sendMessage(WebSocketMessage wsm) throws IOException {
		if (wsSession!=null && wsSession.isOpen()) { 
			logger.debug("sending message through websocket");
			wsSession.getBasicRemote().sendText(wsm.toJson());
		} else {
			throw new IOException("websocket is not open!");
		}
	} 
	
	public void sendError(String msg) throws IOException {
		sendMessage(new ErrorMessage(msg));
	}
	
	public void sendInformation(String msg) throws IOException {
		sendMessage(new InformationMessage(msg));
	}
	
}
