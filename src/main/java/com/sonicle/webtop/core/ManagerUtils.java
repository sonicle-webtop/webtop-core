/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core;

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.core.bol.OMasterData;
import com.sonicle.webtop.core.model.BaseMasterData;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.model.MasterDataLookup;

/**
 *
 * @author malbinola
 */
public class ManagerUtils {
	
	static MasterData createMasterData(OMasterData src) {
		if (src == null) return null;
		return fillMasterData(new MasterData(), src);
	}
	
	static <T extends MasterData> T fillMasterData(T tgt, OMasterData src) {
		if ((tgt != null) && (src != null)) {
			fillBaseMasterData(tgt, src);
			tgt.setAddress(src.getAddress());
			tgt.setCity(src.getCity());
			tgt.setPostalCode(src.getPostalCode());
			tgt.setState(src.getState());
			tgt.setCountry(src.getCountry());
			tgt.setTelephone(src.getTelephone());
			tgt.setFax(src.getFax());
			tgt.setMobile(src.getMobile());
			tgt.setEmail(src.getEmail());
			tgt.setNotes(src.getNotes());
		}
		return tgt;
	}
	
	static MasterDataLookup createMasterDataLookup(OMasterData src) {
		if (src == null) return null;
		return fillBaseMasterData(new MasterDataLookup(), src);
	}
	
	static <T extends BaseMasterData> T fillBaseMasterData(T tgt, OMasterData src) {
		if ((tgt != null) && (src != null)) {
			tgt.setMasterDataId(src.getMasterDataId());
			tgt.setParentMasterDataId(src.getParentMasterDataId());
			tgt.setExternalId(src.getExternalId());
			tgt.setType(src.getType());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), BaseMasterData.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setLockStatus(EnumUtils.forSerializedName(src.getLockStatus(), BaseMasterData.LoskStatus.class));
			tgt.setDescription(src.getDescription());
		}
		return tgt;
	}
}
