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
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.RegexUtils;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomField;
import com.sonicle.webtop.core.bol.OCustomPanel;
import com.sonicle.webtop.core.bol.OMasterData;
import com.sonicle.webtop.core.bol.OTag;
import com.sonicle.webtop.core.model.Activity;
import com.sonicle.webtop.core.model.BaseMasterData;
import com.sonicle.webtop.core.model.Causal;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomPanel;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.model.MasterDataLookup;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ManagerUtils {
	private static Pattern PATT_DBFIELD_NAME = RegexUtils.matchStartEnd(RegexUtils.MATCH_DBFIELD_NAME);
	
	static Activity createActivity(OActivity src) {
		if (src == null) return null;
		return fillActivity(new Activity(), src);
	}
	
	static <T extends Activity> T fillActivity(T tgt, OActivity src) {
		if ((tgt != null) && (src != null)) {
			tgt.setActivityId(src.getActivityId());
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), Activity.RevisionStatus.class));
			tgt.setDescription(src.getDescription());
			tgt.setReadOnly(src.getReadOnly());
			tgt.setExternalId(src.getExternalId());
		}
		return tgt;
	}
	
	static Causal createCausal(OCausal src) {
		if (src == null) return null;
		return fillCausal(new Causal(), src);
	}
	
	static <T extends Causal> T fillCausal(T tgt, OCausal src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCausalId(src.getCausalId());
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setMasterDataId(src.getMasterDataId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), Causal.RevisionStatus.class));
			tgt.setDescription(src.getDescription());
			tgt.setReadOnly(src.getReadOnly());
			tgt.setExternalId(src.getExternalId());
		}
		return tgt;
	}
	
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
	
	static Tag createTag(OTag src) {
		if (src == null) return null;
		return fillTag(new Tag(), src);
	}
	
	static <T extends Tag> T fillTag(T tgt, OTag src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTagId(src.getTagId());
			tgt.setDomainId(src.getDomainId());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setName(src.getName());
			tgt.setColor(src.getColor());
		}
		return tgt;
	}
	
	static OTag createOTag(Tag src) {
		if (src == null) return null;
		return fillOTag(new OTag(), src);
	}
	
	static <T extends OTag> T fillOTag(T tgt, Tag src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTagId(src.getTagId());
			tgt.setDomainId(src.getDomainId());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setName(src.getName());
			tgt.setColor(src.getColor());
		}
		return tgt;
	}
	
	static CustomPanel createCustomPanel(OCustomPanel src, Set<String> fields, Set<String> tags) {
		if (src == null) return null;
		CustomPanel tgt = fillCustomPanel(new CustomPanel(), src);
		tgt.setFields(fields);
		tgt.setTags(tags);
		return tgt;
	}
	
	static <T extends CustomPanel> T fillCustomPanel(T tgt, OCustomPanel src) {
		if ((tgt != null) && (src != null)) {
			tgt.setPanelId(src.getCustomPanelId());
			tgt.setDomainId(src.getDomainId());
			tgt.setServiceId(src.getServiceId());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setTitleI18n(LangUtils.deserialize(src.getTitleI18n(), new CustomPanel.TitleI18n(), CustomPanel.TitleI18n.class));
		}
		return tgt;
	}
	
	static OCustomPanel createOCustomPanel(CustomPanel src) {
		if (src == null) return null;
		return fillOCustomPanel(new OCustomPanel(), src);
	}
	
	static <T extends OCustomPanel> T fillOCustomPanel(T tgt, CustomPanel src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCustomPanelId(src.getPanelId());
			tgt.setDomainId(src.getDomainId());
			tgt.setServiceId(src.getServiceId());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setTitleI18n(LangUtils.serialize(src.getTitleI18n(), CustomPanel.TitleI18n.class));
		}
		return tgt;
	}
	
	static CustomField createCustomField(OCustomField src) {
		if (src == null) return null;
		return fillCustomField(new CustomField(), src);
	}
	
	static <T extends CustomField> T fillCustomField(T tgt, OCustomField src) {
		if ((tgt != null) && (src != null)) {
			tgt.setFieldId(src.getCustomFieldId());
			tgt.setDomainId(src.getDomainId());
			tgt.setServiceId(src.getServiceId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), CustomField.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setType(EnumUtils.forSerializedName(src.getType(), CustomField.Type.class));
			tgt.setProps(LangUtils.deserialize(src.getProperties(), new CustomField.Props(), CustomField.Props.class));
			tgt.setValues(LangUtils.deserialize(src.getValues(), new CustomField.Values(), CustomField.Values.class));
			tgt.setLabelI18n(LangUtils.deserialize(src.getLabelI18n(), new CustomField.LabelI18n(), CustomField.LabelI18n.class));
		}
		return tgt;
	}
	
	static OCustomField createOCustomField(CustomField src) {
		if (src == null) return null;
		return fillOCustomField(new OCustomField(), src);
	}
	
	static <T extends OCustomField> T fillOCustomField(T tgt, CustomField src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCustomFieldId(src.getFieldId());
			tgt.setDomainId(src.getDomainId());
			tgt.setServiceId(src.getServiceId());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setType(EnumUtils.toSerializedName(src.getType()));
			tgt.setProperties(LangUtils.serialize(src.getProps(), CustomField.Props.class));
			tgt.setValues(LangUtils.serialize(src.getValues(), CustomField.Values.class));
			tgt.setLabelI18n(LangUtils.serialize(src.getLabelI18n(), CustomField.LabelI18n.class));
		}
		return tgt;
	}
	
	static <T extends OCustomField> boolean validate(T src) throws WTException {
		if (StringUtils.isBlank(src.getName())) throw new WTException("'Name' is blank");
		if (!PATT_DBFIELD_NAME.matcher(src.getName()).matches()) throw new WTException("Patter for 'Name' is not satisfied [{}]", RegexUtils.MATCH_DBFIELD_NAME);
		return true;
	}
}
