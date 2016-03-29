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
package com.sonicle.webtop.core;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.WebTopSession.RequestDump;
import com.sonicle.webtop.core.bol.OSysLog;
import com.sonicle.webtop.core.dal.SysLogDAO;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class LogManager {
	private static final Logger logger = WT.getLogger(LogManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized LogManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		LogManager logm = new LogManager(wta);
		initialized = true;
		logger.info("LogManager initialized.");
		return logm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private LogManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		
	}
	
	public boolean isEnabled(String domainId, String serviceId) {
		if(!initialized) return false;
		CoreServiceSettings css = new CoreServiceSettings(serviceId, domainId);
		//TODO: valutare se introdurre il caching
		return css.getSysLogEnabled();
	}
	
	public boolean write(UserProfile.Id profileId, String serviceId, String action, String softwareName, RequestDump requestDump, String sessionId, String data) {
		String remoteIp = (requestDump != null) ? requestDump.remoteIP : null;
		String userAgent = (requestDump != null) ? requestDump.userAgent : null;
		return write(profileId, serviceId, action, softwareName, remoteIp, userAgent, sessionId, data);
	}
	
	public boolean write(UserProfile.Id profileId, String serviceId, String action, String softwareName, HttpServletRequest request, String sessionId, String data) {
		String remoteIp = ServletUtils.getClientIP(request);
		String userAgent = ServletUtils.getUserAgent(request);
		return write(profileId, serviceId, action, softwareName, remoteIp, userAgent, sessionId, data);
	}
	
	public boolean write(UserProfile.Id profileId, String serviceId, String action, String softwareName, String remoteIp, String userAgent, String sessionId, String data) {
		Connection con = null;
		if(!initialized) return false;
		if(!isEnabled(profileId.getDomain(), serviceId)) return false;
		
		try {
			con = WT.getCoreConnection();
			SysLogDAO dao = SysLogDAO.getInstance();
			OSysLog item = new OSysLog();
			item.setSyslogId(dao.getSequence(con));
			item.setTimestamp(DateTime.now(DateTimeZone.UTC));
			item.setDomainId(profileId.getDomain());
			item.setUserId(profileId.getUserId());
			item.setServiceId(serviceId);
			item.setAction(action);
			item.setSwName(StringUtils.defaultIfBlank(softwareName, wta.getPlatformName()));
			item.setIpAddress(remoteIp);
			item.setUserAgent(userAgent);
			item.setSessionId(sessionId);
			item.setData(data);
			dao.insert(con, item);
			return true;
			
		} catch(SQLException ex) {
			logger.error("DB error", ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
}
