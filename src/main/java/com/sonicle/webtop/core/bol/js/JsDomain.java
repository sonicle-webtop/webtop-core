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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.security.ConnectionSecurity;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.webtop.core.bol.model.DomainEntity;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malbinola
 */
public class JsDomain {
	public String domainId;
	public String internetName;
	public Boolean enabled;
	public String displayName;
	public String dirScheme;
	public String dirHost;
	public Integer dirPort;
	public String dirPath;
	public String dirUsername;
	public String dirPassword;
	public String dirConSecurity;
	public Boolean dirCaseSensitive;
	public Boolean dirPasswordPolicy;
	public Boolean userAutoCreation;
	
	public JsDomain() {}
	
	public JsDomain(DomainEntity o) throws URISyntaxException {
		domainId = o.getDomainId();
		internetName = o.getInternetName();
		enabled = o.getEnabled();
		displayName = o.getDisplayName();
		URI uri = new URI(o.getDirUri());
		dirScheme = uri.getScheme();
		dirHost = uri.getHost();
		dirPort = URIUtils.getPort(uri);
		dirPath = uri.getPath();
		dirUsername = o.getDirUsername();
		dirPassword = o.getDirPassword();
		dirConSecurity = StringUtils.defaultIfBlank(EnumUtils.getName(o.getDirConnectionSecurity()), "null");
		dirCaseSensitive = o.getDirCaseSensitive();
		dirPasswordPolicy = o.getDirPasswordPolicy();
		userAutoCreation = o.getUserAutoCreation();
	}
	
	public static DomainEntity buildDomainEntity(JsDomain js, AbstractDirectory dir) throws URISyntaxException {
		DomainEntity de = new DomainEntity();
		de.setDomainId(js.domainId);
		de.setInternetName(js.internetName);
		de.setEnabled(js.enabled);
		de.setDisplayName(js.displayName);
		de.setDirUri(dir.buildUri(js.dirHost, js.dirPort, js.dirPath).toString());
		de.setDirUsername(js.dirUsername);
		de.setDirPassword(js.dirPassword);
		de.setDirConnectionSecurity(EnumUtils.getEnum(ConnectionSecurity.class, js.dirConSecurity));
		de.setDirCaseSensitive(js.dirCaseSensitive);
		de.setDirPasswordPolicy(js.dirPasswordPolicy);
		de.setUserAutoCreation(js.userAutoCreation);
		return de;
	}
}
