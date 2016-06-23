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
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.bol.OFileType;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.FileTypeDAO;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.sdk.interfaces.IConnectionProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author malbinola
 */
public class FileTypes {
	private final HashMap<String, String> extToFileType = new HashMap<>();
	
	private FileTypes() {}
	
	public boolean containsExtension(String extension) {
		synchronized(extToFileType) {
			return extToFileType.containsKey(extension);
		}
	}
	
	public String getFileType(String extension) {
		synchronized(extToFileType) {
			return extToFileType.get(extension);
		}
	}
	
	public void update(IConnectionProvider conp) {
		synchronized(extToFileType) {
			load(conp);
		}
	}
	
	private void load(IConnectionProvider conp) {
		FileTypeDAO dao = FileTypeDAO.getInstance();
		Connection con = null;
		
		try {
			con = conp.getConnection();
			List<OFileType> ftypes = dao.selectAll(con);
			extToFileType.clear();
			for(OFileType ftype : ftypes) {
				if(!ftype.getExtension().isEmpty() && !ftype.getType().isEmpty()) {
					if(!ftype.getSubtype().isEmpty()) {
						extToFileType.put(ftype.getExtension(), ftype.getType()+"-"+ftype.getSubtype());
					} else {
						extToFileType.put(ftype.getExtension(), ftype.getType());
					}
				}
			}
		} catch(SQLException | DAOException ex) {
			throw new WTRuntimeException(ex, "Unable to load fileTypes");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public static FileTypes init(IConnectionProvider conp) {
		FileTypes o = new FileTypes();
		o.update(conp);
		return o;
	}
	
	@Override
	public String toString() {
		synchronized(extToFileType) {
			return JsonResult.GSON_WONULLS.toJson(extToFileType);
		}
	}
}
