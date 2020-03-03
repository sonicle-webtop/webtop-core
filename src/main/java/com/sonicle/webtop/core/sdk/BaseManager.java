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
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.products.AuditProduct;
import java.sql.SQLException;
import java.util.Collection;
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
	
	private final AuditProduct AUDIT_PRODUCT;
	private boolean auditEnabled = false;
	//private AuditProduct auditProduct;
	
	public BaseManager(boolean fastInit, UserProfileId targetProfileId) {
		SERVICE_ID = WT.findServiceId(this.getClass());
		this.fastInit = fastInit;
		this.targetProfile = targetProfileId;
		this.softwareName = null;
		this.locale = guessLocale();
		
		String internetName = WT.getDomainInternetName(targetProfileId.getDomainId());
		AUDIT_PRODUCT = new AuditProduct(internetName);
		if (WT.isLicensed(AUDIT_PRODUCT)) {
			auditEnabled = new CoreServiceSettings(CoreManifest.ID, targetProfile.getDomainId()).isAuditEnabled(); 
			if (auditEnabled) {
				CoreServiceSettings scss = new CoreServiceSettings(SERVICE_ID, targetProfile.getDomainId());
				if (scss.hasAuditEnabled()) {
					auditEnabled = scss.isAuditEnabled();
				}
			}
		}
		
		/*
		//audit
		String internetDomain=WT.getDomainInternetName(targetProfileId.getDomainId());
		if (internetDomain!=null) {
			this.auditProduct=new AuditProduct(internetDomain);
			if (WT.isLicensed(auditProduct)) {
				String domainId=targetProfile.getDomainId();
				boolean coreAuditEnabled=new CoreServiceSettings(CoreManifest.ID, domainId).isAuditEnabled();
				auditEnabled = coreAuditEnabled;
				if (coreAuditEnabled) {
					CoreServiceSettings scss=new CoreServiceSettings(SERVICE_ID, domainId);
					//if we have an entry for this service, use this
					if (scss.hasAuditEnabled())
						auditEnabled = scss.isAuditEnabled();
					else {
						//user main core setup
					}
				}
			}
		}
		*/
	}
	
	protected final Locale guessLocale() {
		UserProfile.Data ud = null;
		ud = WT.getUserData(getTargetProfileId());
		if (ud != null) return ud.getLocale();
		ud = WT.getUserData(RunContext.getRunProfileId());
		if (ud != null) return ud.getLocale();
		return WT.LOCALE_ENGLISH;
	}
	
	protected WTException wrapException(Exception ex) {
		if (ex instanceof WTException) {
			return (WTException)ex;
		} else if ((ex instanceof SQLException) || (ex instanceof DAOException)) {
			return new WTException(ex, "DB error");
		} else {
			return new WTException(ex);
		}
	}
	
	/**
	 * Returns the current targetProfile; it can be null.
	 * @return 
	 */
	public UserProfileId getTargetProfileId() {
		return targetProfile;
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
	
	/**
	 * Gets current locale.
	 * By default locale is guessed during initialization by taking the first
	 * non-null value from the following: (1) targetProfile locale,
	 * (3) runningProfile locale, (3) english locale (fallback).
	 * @return 
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * Sets the current associated locale.
	 * @param locale 
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * Checks if audit-logs are enabled.
	 * @return 
	 */
	public boolean isAuditEnabled() {
		return auditEnabled;
	}
	
	public Locale getProfileOrTargetLocale(UserProfileId profile) {
		Locale loc = getLocale();
		if (profile != null) {
			UserProfile.Data ud = WT.getUserData(profile);
			if (ud != null) loc = ud.getLocale();
		}
		return loc;
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
	
	/**
	 * Checks if passed profile is equal to the current one,
	 * taking into account possible null values.
	 * @param profileId The userProfile to check
	 * @return 
	 */
	public boolean isTargetProfileEqualTo(UserProfileId profileId) {
		if (profileId == null) return false;
		return profileId.equals(targetProfile);
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
		if (!StringUtils.equals(callerServiceId, callerServiceIdMustBe)) throw new MethodAuthException(methodName, callerServiceId, RunContext.getRunProfileId());
	}
	
	/**
	 * Checks if the running profile (see runContext) and target profile are the same.
	 * This security check is skipped for WebTop admin.
	 * @throws AuthException When profiles not match.
	 */
	public void ensureProfile() throws AuthException {
		ensureProfile(true);
	}
	
	/**
	 * Checks if the running profile (see runContext) and target profile are the same.
	 * @param skipIfAdmin True to skip check if running profile is a WebTop admin.
	 * @throws AuthException When profiles not match.
	 */
	public void ensureProfile(boolean skipIfAdmin) throws AuthException {
		UserProfileId runPid = RunContext.getRunProfileId();
		if (skipIfAdmin && RunContext.isWebTopAdmin(runPid)) return;
		if (!runPid.equals(getTargetProfileId())) throw new AuthException("Running profile [{0}] does not match with target profile [{1}]", runPid, getTargetProfileId());
	}
	
	/**
	 * Checks if the running profile's domain ID (see runContext) and target profile's domain ID are the same.
	 * This security check is skipped for WebTop admin.
	 * @throws AuthException When profile's domain not match.
	 */
	public void ensureProfileDomain() throws AuthException {
		ensureProfileDomain(true);
	}
	
	/**
	 * Checks if the running profile's domain ID (see runContext) and target profile's domain ID are the same.
	 * @param skipIfAdmin True to skip check if running profile is a WebTop admin.
	 * @throws AuthException When profile's domain not match.
	 */
	public void ensureProfileDomain(boolean skipIfAdmin) throws AuthException {
		UserProfileId runPid = RunContext.getRunProfileId();
		if (skipIfAdmin && RunContext.isWebTopAdmin(runPid)) return;
		if (!runPid.hasDomain(getTargetProfileId().getDomainId())) throw new AuthException("Running profile's domain [{0}] does not match with target's domain [{1}]", runPid.getDomainId(), getTargetProfileId().getDomainId());
	}
	
	/**
	 * Checks if the running profile's domain ID (see runContext) and passed one are the same.
	 * This security check is skipped for WebTop admin.
	 * @param domainId Desired domain ID.
	 * @throws AuthException When profile's domain do not match.
	 */
	public void ensureProfileDomain(String domainId) throws AuthException {
		ensureProfileDomain(true, domainId);
	}
	
	/**
	 * Checks if the running profile's domain ID (see runContext) and passed one are the same.
	 * @param skipIfAdmin True to skip check if running profile is a WebTop admin.
	 * @param domainId Desired domain ID.
	 * @throws AuthException When profile's domain not match.
	 */
	public void ensureProfileDomain(boolean skipIfAdmin, String domainId) throws AuthException {
		if (domainId == null) return;
		UserProfileId runPid = RunContext.getRunProfileId();
		if (skipIfAdmin && RunContext.isWebTopAdmin(runPid)) return;
		if (!runPid.hasDomain(domainId)) throw new AuthException("Running profile's domain [{0}] does not match with passed one [{1}]", runPid.getDomainId(), domainId);
	}
	
	/**
	 * Checks if target profile's domain ID and passed one are the same.
	 * @param domainId Desired domain ID.
	 * @throws AuthException When profile's domain do not match.
	 */
	public void ensureTargetProfileDomain(String domainId) throws AuthException {
		if (!getTargetProfileId().hasDomain(domainId)) throw new AuthException("Target profile's domain [{}] should match with [{}]", getTargetProfileId().getDomainId(), domainId);
	}
	
	/**
	 * Checks if the running profile (see runContext) is SysAdmin.
	 * Code must be running as administrator.
	 */
	public void ensureSysAdmin() {
		if (!RunContext.isSysAdmin()) throw new AuthException("Running profile must be SysAdmin [{0}]", RunContext.getRunProfileId());
	}
	
	/**
	 * Writes a new entry into audit-log in order to trace user activity.
	 * @param context A string that identifies the context involved.
	 * @param action A string that identifies the action performed.
	 * @param reference An optional string that uniquely identifies the item (of context above) that suffered the action.
	 * @param data An optional data (eg. JSON payload) to complete info about operation.
	 */
	public void writeAuditLog(final String context, final String action, final String reference, final String data) {
		if (isAuditEnabled()) WT.writeAuditLog(SERVICE_ID, context, action, reference, data);
	}
	
	/**
	 * Writes new multiple entry into audit-log in order to trace user activity.
	 * @param context A string that identifies the context involved.
	 * @param action A string that identifies the action performed.
	 * @param entries A collection of multiple reference/data objects.
	 */
	public void writeAuditLog(final String context, final String action, final Collection<AuditReferenceDataEntry> entries) {
		if (isAuditEnabled()) WT.writeAuditLog(SERVICE_ID, context, action, entries);
	}
	
	public Session getMailSession() {
		WebTopSession wts = SessionContext.getCurrent(false);
		Session s=(wts != null) ? wts.getMailSession() : null;
		if (s==null) s=WT.getGlobalMailSession(getTargetProfileId());
		return s;
	}
	
	/**
	 * @deprecated use ensureProfile instead
	 * Checks if the running profile (see runContext) and target profile are the same.
	 * This security check is skipped for SysAdmin.
	 * @throws AuthException When profiles not match.
	 */
	@Deprecated
	public void ensureUser() throws AuthException {
		ensureProfile();
	}
	
	/**
	 * @deprecated use ensureProfileDomain instead
	 * Checks if the running profile's domain ID (see runContext) and target profile's domain ID are the same.
	 * This security check is skipped for SysAdmin.
	 * @throws AuthException When domain IDs do not match.
	 */
	@Deprecated
	public void ensureUserDomain() throws AuthException {
		ensureProfileDomain();
	}
	
	/**
	 * @deprecated use ensureProfileDomain instead
	 * Checks if the running profile's domain ID (see runContext) and passed domain ID matches.
	 * @param domainId Required domain ID.
	 * @throws AuthException When domain IDs do not match.
	 */
	@Deprecated
	public void ensureUserDomain(String domainId) throws AuthException {
		ensureProfileDomain(domainId);
	}
}
