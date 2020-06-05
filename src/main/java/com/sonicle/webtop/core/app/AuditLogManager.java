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
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.bol.OAuditLog;
import com.sonicle.webtop.core.dal.AuditLogDAO;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class AuditLogManager {
	private static final Logger logger = WT.getLogger(AuditLogManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized AuditLogManager initialize(WebTopApp wta) {
		if (initialized) throw new RuntimeException("Initialization already done");
		AuditLogManager logm = new AuditLogManager(wta);
		initialized = true;
		logger.info("Initialized");
		return logm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private AuditLogManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		wta = null;
		logger.info("Cleaned up");
	}
	
	public boolean write(UserProfileId profileId, String sessionId, String serviceId, String context, String action, String referenceId, String data) {
		if (!initialized || RunContext.isImpersonated()) return false;
		
		AuditLogDAO dao = AuditLogDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OAuditLog item = new OAuditLog();
			item.setTimestamp(BaseDAO.createRevisionTimestamp());
			item.setDomainId(profileId.getDomain());
			item.setUserId(profileId.getUserId());
			item.setServiceId(serviceId);
			item.setContext(context);
			item.setAction(action);
			item.setReferenceId(referenceId);
			item.setSessionId(sessionId);
			item.setData(data);
			
			return dao.insert(con, item) == 1;
			
		} catch(SQLException | DAOException ex) {
			logger.error("DB error", ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean write(UserProfileId profileId, String sessionId, String serviceId, String context, String action, Collection<AuditReferenceDataEntry> entries) {
		if (!initialized || RunContext.isImpersonated()) return false;
		
		AuditLogDAO dao = AuditLogDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OAuditLog baseItem = new OAuditLog();
			baseItem.setTimestamp(BaseDAO.createRevisionTimestamp());
			baseItem.setDomainId(profileId.getDomain());
			baseItem.setUserId(profileId.getUserId());
			baseItem.setServiceId(serviceId);
			baseItem.setContext(context);
			baseItem.setAction(action);
			baseItem.setSessionId(sessionId);
			
			return dao.batchInsert(con, baseItem, entries).length == entries.size();
			
		} catch(SQLException | DAOException ex) {
			logger.error("DB error", ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
}
