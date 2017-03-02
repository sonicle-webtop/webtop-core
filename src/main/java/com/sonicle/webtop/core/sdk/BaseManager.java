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

import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import java.util.Locale;
import javax.mail.Session;
import org.apache.commons.lang3.StringUtils;
import sun.reflect.Reflection;

/**
 *
 * @author malbinola
 */
public abstract class BaseManager {
	public final String SERVICE_ID;
	private final UserProfileId targetProfile;
	protected final boolean fastInit;
	private String softwareName;
	private Locale locale;
	
	public BaseManager(boolean fastInit, UserProfileId targetProfileId) {
		SERVICE_ID = WT.findServiceId(this.getClass());
		this.fastInit = fastInit;
		this.targetProfile = targetProfileId;
		this.softwareName = null;
		this.locale = null;
		locale = findLocale();
	}
	
	protected Locale findLocale() {
		WebTopSession wts = RunContext.getWebTopSession();
		if (wts != null) return wts.getLocale();
		UserProfile.Data ud = WT.getUserData(getTargetProfileId());
		if (ud != null) return ud.getLocale();
		return WT.LOCALE_ENGLISH;
	}
	
	public UserProfileId getTargetProfileId() {
		return targetProfile;
	}
	
	/**
	 * Checks if service of this runContext matches the passed one.
	 * For example, in order to ensure that call is coming from a specific service.
	 * @param callerServiceIdMustBe The service ID allowed
	 * @param methodName The method name for debugging purposes
	 * @throws AuthException When the running service does not match the passed one
	 */
	public void ensureCallerService(String callerServiceIdMustBe, String methodName) throws MethodAuthException {
		String callerServiceId = WT.findServiceId(Reflection.getCallerClass(3));
		if(!StringUtils.equals(callerServiceId, callerServiceIdMustBe)) throw new MethodAuthException(methodName, callerServiceId, RunContext.getRunProfileId());
	}
	
	/**
	 * Checks if the running profile (see runContext) and target profile are the same.
	 * This security check is skipped for SysAdmin.
	 * @throws AuthException When profiles not match.
	 */
	public void ensureUser() throws AuthException {
		UserProfileId runPid = RunContext.getRunProfileId();
		if(RunContext.isWebTopAdmin(runPid)) return;
		if(!runPid.equals(getTargetProfileId())) throw new AuthException("");
	}
	
	/**
	 * Checks if the running profile's domain ID (see runContext) and target profile's domain ID are the same.
	 * This security check is skipped for SysAdmin.
	 * @throws AuthException When domain IDs do not match.
	 */
	public void ensureUserDomain() throws AuthException {
		UserProfileId runPid = RunContext.getRunProfileId();
		if(RunContext.isWebTopAdmin(runPid)) return;
		if(!runPid.hasDomain(getTargetProfileId().getDomainId())) throw new AuthException("Domain ID for the running profile [{0}] does not match with the target [{1}]", runPid.getDomainId(), getTargetProfileId().getDomainId());
	}
	
	/**
	 * Checks if the running profile's domain ID (see runContext) and passed domain ID matches.
	 * @param domainId Required domain ID.
	 * @throws AuthException When domain IDs do not match.
	 */
	public void ensureUserDomain(String domainId) throws AuthException {
		UserProfileId runPid = RunContext.getRunProfileId();
		if(RunContext.isWebTopAdmin(runPid)) return;
		if(!runPid.hasDomain(domainId)) throw new AuthException("Domain ID for the running profile [{0}] does not match with passed one [{1}]", runPid.getDomainId(), domainId);
	}
	
	/**
	 * Returns specified software name that is using this manager. Defaults to null.
	 * @return provided software name, or null if no value is provided
	 */
	public String getSoftwareName() {
		return softwareName;
	}
	
	/**
	 * Sets the current software name value.
	 * @param softwareName 
	 */
	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public Session getMailSession() {
		WebTopSession wts = RunContext.getWebTopSession();
		return (wts != null) ? wts.getMailSession() : WT.getGlobalMailSession(getTargetProfileId());
	}
	
	/**
	 * Returns the manifest associated to the service owning this manager.
	 * @return The service's manifest
	 */
	public ServiceManifest getManifest() {
		return WT.getManifest(SERVICE_ID);
	}
	
	/**
	 * Returns the localized string associated to the key.
	 * @param locale The requested locale.
	 * @param key The resource key.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(Locale locale, String key) {
		return WT.lookupResource(SERVICE_ID, locale, key);
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param locale The requested locale.
	 * @param key The resource key.
	 * @param escapeHtml True to apply HTML escaping.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(Locale locale, String key, boolean escapeHtml) {
		return WT.lookupResource(SERVICE_ID, locale, key, escapeHtml);
	}
	
	public final void writeLog(String action, String data) {
		WT.writeLog(action, softwareName, data);
	}
}
