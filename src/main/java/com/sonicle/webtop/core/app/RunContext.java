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

import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.MethodAuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author malbinola
 */
public class RunContext {
	private final ServiceContext serviceContext;
	private final Subject subject;
	private final UserProfile.Id subjectProfile;
	
	RunContext(ServiceContext serviceContext, Subject subject) {
		this.serviceContext = Check.notNull(serviceContext);
		this.subject = Check.notNull(subject);
		this.subjectProfile = Check.notNull(ContextUtils.getProfileId(subject));
	}
	
	public RunContext(ServiceContext serviceContext) {
		this.serviceContext = Check.notNull(serviceContext);
		this.subject = Check.notNull(ContextUtils.getSubject());
		this.subjectProfile = Check.notNull(ContextUtils.getProfileId(subject));
	}
	
	public Subject getSubject() {
		return subject;
	}
	
	public String getServiceId() {
		return serviceContext.getServiceId();
	}
	
	public UserProfile.Id getProfileId() {
		return subjectProfile;
	}
	
	public boolean isPermitted(String serviceId, String resource) {
		ensureSameSubject();
		return ContextUtils.isPermitted(subjectProfile, serviceId, resource);
	}
	
	public boolean isPermitted(String serviceId, String resource, String action) {
		ensureSameSubject();
		return ContextUtils.isPermitted(subjectProfile, serviceId, resource, action);
	}
	
	public boolean isPermitted(String serviceId, String resource, String action, String instance) {
		ensureSameSubject();
		return ContextUtils.isPermitted(subjectProfile, serviceId, resource, action, instance);
	}
	
	public boolean isSysAdmin() {
		ensureSameSubject();
		return ContextUtils.isSysAdmin(subjectProfile);
	}
	
	public boolean isWebTopAdmin() {
		ensureSameSubject();
		return ContextUtils.isWebTopAdmin(subjectProfile);
	}
	
	public void ensureIsPermitted(String serviceId, String resource) throws AuthException {
		ensureSameSubject();
		ContextUtils.ensureIsPermitted(subjectProfile, serviceId, resource);
	}
	
	public void ensureIsPermitted(String serviceId, String resource, String action) throws AuthException {
		ensureSameSubject();
		ContextUtils.ensureIsPermitted(subjectProfile, serviceId, resource, action);
	}
	
	public void ensureIsPermitted(String serviceId, String resource, String action, String instance) throws AuthException {
		ensureSameSubject();
		ContextUtils.ensureIsPermitted(subjectProfile, serviceId, resource, action, instance);
	}
	
	public void ensureIsSysAdmin() throws AuthException {
		ensureSameSubject();
		ContextUtils.ensureIsSysAdmin(subjectProfile);
	}
	
	public void ensureIsWebTopAdmin() throws AuthException {
		ensureSameSubject();
		ContextUtils.ensureIsWebTopAdmin(subjectProfile);
	}
	
	private void ensureSameSubject() {
		//if(!subjectProfile.equals(ContextUtils.getProfileId())) throw new AuthException("Executing Subject does not match with that in this RunContext");
	}
	
	/**
	 * Checks if service of this runContext matches the passed one.
	 * For example, in order to ensure that call is coming from a specific service.
	 * @param callingServiceId The service ID allowed
	 * @param methodName The method name for debugging purposes
	 * @throws AuthException When the running service does not match the passed one
	 */
	public void ensureService(String callingServiceId, String methodName) throws MethodAuthException {
		if(!StringUtils.equals(getServiceId(), callingServiceId)) throw new MethodAuthException(methodName, this);
	}
}
