/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.bol.OMediaType;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.MediaTypeDAO;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.sdk.interfaces.IConnectionProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.locks.StampedLock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class MediaTypes {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(LicenseManager.class);
	private final StampedLock lock = new StampedLock();
	private final HashMap<String, String> extToMediaType = new LinkedHashMap<>();
	
	public static MediaTypes init(IConnectionProvider conp) {
		MediaTypes o = new MediaTypes();
		o.update(conp);
		return o;
	}
	
	private MediaTypes() {}
	
	public boolean containsExtension(String extension) {
		String ext = StringUtils.lowerCase(extension);
		long stamp = lock.readLock();
		try {
			return extToMediaType.containsKey(ext);
		} finally {
			lock.unlock(stamp);
		}
	}
	
	public String getMediaType(String extension) {
		String ext = StringUtils.lowerCase(extension);
		long stamp = lock.readLock();
		try {
			return extToMediaType.get(ext);
		} finally {
			lock.unlock(stamp);
		}
	}
	
	public void update(IConnectionProvider conp) {
		long stamp = lock.writeLock();
		try {
			internalLoad(conp);
		} finally {
			lock.unlockWrite(stamp);
		}
	}
	
	private void internalLoad(IConnectionProvider connectionProvider) {
		MediaTypeDAO mtDao = MediaTypeDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("[MediaTypes] Loading fileExtension -> mediaType mappings...");
			con = connectionProvider.getConnection();
			List<OMediaType> mtypes = mtDao.selectAll(con);
			extToMediaType.clear();
			for (OMediaType mtype : mtypes) {
				if (!StringUtils.isBlank(mtype.getExtension()) && !StringUtils.isBlank(mtype.getMediaType())) {
					String old = extToMediaType.put(mtype.getExtension().toLowerCase(), mtype.getMediaType());
					if (old != null) LOGGER.warn("[MediaTypes] Overridden extension, last wins ('{}' -> '{}')", mtype.getExtension().toLowerCase(), mtype.getMediaType());
				}
			}
			LOGGER.debug("[MediaTypes] Cached {} mappings", extToMediaType.size());
			
		} catch(SQLException | DAOException ex) {
			throw new WTRuntimeException(ex, "Unable to load fileTypes");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public String toString() {
		long stamp = lock.readLock();
		try {
			return JsonResult.GSON_WONULLS.toJson(extToMediaType);
		} finally {
			lock.unlock(stamp);
		}
	}
}
