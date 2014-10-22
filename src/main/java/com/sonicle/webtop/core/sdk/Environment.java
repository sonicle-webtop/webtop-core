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

import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.Manifest;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.WebTopSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import javax.sql.DataSource;
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
    private final CoreServiceSettings css;
    private final CoreUserSettings cus;
	
	public Environment(WebTopApp wta, WebTopSession wts, UserProfile profile) {
		this.wta = wta;
		this.wts = wts;
		this.profile = profile;
        this.css=new CoreServiceSettings(profile.getDomainId(),Manifest.ID);
        this.cus=new CoreUserSettings(profile.getDomainId(),profile.getUserId(),Manifest.ID);
	}

	@Override
	public UserProfile getProfile() {
		return profile;
	}
	
	@Override
	public ReadableUserAgent getUserAgent() {
		return wts.getUserAgent();
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
    public String getSessionRefererUri() {
        return wts.getRefererURI();
    }

    @Override
    public Connection getCoreConnection() throws SQLException {
        return wta.getConnectionManager().getConnection();
    }

    @Override
    public CoreServiceSettings getCoreServiceSettings() {
        return css;
    }

    @Override
    public CoreUserSettings getCoreUserSettings() {
        return cus;
    }

}