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

import static com.sonicle.webtop.core.xmpp.XMPPClient.logger;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;
import org.jooq.tools.StringUtils;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 *
 * @author malbinola
 */
public class XMPPHelper {
	
	public static XMPPTCPConnectionConfiguration.Builder setupConfigBuilder(String host, int port, String xmppDomain, String username, String password, String resource) {
		try {
			XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder()
				.setHost(host)
				.setPort(port)
				.setXmppDomain(xmppDomain)
				.setUsernameAndPassword(username, password)
				.setResource(resource);
			TLSUtils.acceptAllCertificates(builder);
			TLSUtils.disableHostnameVerificationForTlsCertificates(builder);
			return builder;
		} catch(Exception ex) {
			return null;
		}
	}
	
	/*
	public static AbstractXMPPConnection setupConnection(String host, int port, String xmppDomain, String username, String password, String resource) throws Exception {
		return setupConfigBuilder(setupConfiguration(host, port, xmppDomain, username, password, resource)).build();
	}
	
	public static AbstractXMPPConnection setupConnection(XMPPTCPConnectionConfiguration config) throws Exception {
		AbstractXMPPConnection xmpp = new XMPPTCPConnection(config);
		xmpp.connect();
		return xmpp;
	}
	*/
	
	public static String buildGuessedNickname(String entityBareJid) {
		return "~" + entityBareJid;
	}
	
	public static EntityBareJid asEntityBareJid(String jid) {
		try {
			return JidCreate.entityBareFrom(jid);
		} catch(XmppStringprepException ex) {
			return null;
		}
	}
	
	public static String asResourcepartString(Jid jid) {
		return XMPPHelper.asResourcepartString(jid.getResourceOrNull());
	}
	
	public static String asResourcepartString(Resourcepart resourcepart) {
		return (resourcepart != null) ? resourcepart.toString() : null;
	}
	
	public static Localpart asLocalpart(CharSequence localpart) {
		return asLocalpart(localpart.toString());
	}
	
	public static Localpart asLocalpart(String localpart) {
		try {
			return Localpart.from(localpart);
		} catch(XmppStringprepException ex) {
			logger.error("Error creating Localpart from string [{}]", ex, localpart);
			return null;
		}
	}
	
	public static Resourcepart asResourcepart(String resource) {
		try {
			return Resourcepart.from(resource);
		} catch(XmppStringprepException ex) {
			logger.error("Error creating Resourcepart from string [{}]", ex, resource);
			return null;
		}
	}
}
