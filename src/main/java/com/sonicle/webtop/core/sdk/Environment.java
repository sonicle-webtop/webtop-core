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
package com.sonicle.webtop.core.sdk;

import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.Manifest;
import com.sonicle.webtop.core.UserProfile;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.WebTopSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import net.sf.uadetector.ReadableUserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Environment implements BasicEnvironment {

	public final static Logger logger = (Logger) LoggerFactory.getLogger(Environment.class);
	
	protected final WebTopApp wta;
	protected final WebTopSession wts;
	private final UserProfile profile;
	private final ReadableUserAgent userAgent;
	
	public Environment(WebTopApp wta, WebTopSession wts, UserProfile profile, ReadableUserAgent userAgent) {
		this.wta = wta;
		this.wts = wts;
		this.profile = profile;
		this.userAgent = userAgent;
	}

	@Override
	public UserProfile getProfile() {
		return profile;
	}
	
	@Override
	public ReadableUserAgent getUserAgent() {
		return userAgent;
	}
	
	@Override
	public String lookupResource(String serviceId, Locale locale, String key) {
		return wta.lookupResource(serviceId, locale, key);
	}
	
	@Override
	public String lookupResource(String serviceId, Locale locale, String key, boolean escapeHtml) {
		return wta.lookupResource(serviceId, locale, key, escapeHtml);
	}

	@Override
	public Principal getPrincipal(String domainId, String mailUserId) {
/*        Connection con=null;
        String iddomain=wtd.getLocalIDDomain();
        String dlogin=mailusername;
        String dname=mailusername;
        AuthenticationDomain ad=getAuthenticationDomain(iddomain);
        try {
            con=getMainConnection();
            stmt=con.createStatement();
            rs=stmt.executeQuery("select login,username from users where iddomain='"+iddomain+"' and mailusername='"+mailusername+"'");
            if (rs.next()) {
                dlogin=rs.getString("login");
                dname=rs.getString("username");
            }
            else if (wtd.isLdap()) {
                rs.close();
                rs=stmt.executeQuery("select login,username from users where iddomain='"+iddomain+"' and login='"+mailusername+"'");
                if (rs.next()) {
                    dlogin=rs.getString("login");
                    dname=rs.getString("username");
                }
            }
        } catch(SQLException exc) {
            exc.printStackTrace();
        } finally {
            if (rs!=null) try { rs.close(); } catch(Exception exc) {}
            if (stmt!=null) try { stmt.close(); } catch(Exception exc) {}
            if (con!=null) try { con.close(); } catch(Exception exc) {}
        }
        com.sonicle.security.acl.Principal p=new com.sonicle.security.acl.Principal(dlogin,ad,dname);*/
		Principal p=null;
		try {
			Connection con=wta.getConnectionManager().getConnection(Manifest.ID);
			p=new Principal(mailUserId,AuthenticationDomain.getInstance(con, domainId),mailUserId);
		} catch(SQLException exc) {
			logger.error("Error instantiating AuthenticationDomain for {}",domainId,exc);
		}		
        return p;
	}

}
