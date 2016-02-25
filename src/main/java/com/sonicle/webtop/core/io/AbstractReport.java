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
package com.sonicle.webtop.core.io;

import com.sonicle.commons.LangUtils;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;

/**
 *
 * @author malbinola
 */
public abstract class AbstractReport {
	protected String name;
	protected String path;
	protected ReportConfig config;
	protected Map<String, Object> params = new HashMap<>();
	protected JRDataSource dataSource;
	
	public static enum OutputType {
		PDF,
		HTML
	}
	
	public AbstractReport(ReportConfig config) {
		this.config = config;
		buildPath();
		fillBuiltInParams();
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public ReportConfig getConfig() {
		return config;
	}
	
	public Map<String, Object> getParameters() {
		return params;
	}
	
	public JRDataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(JRDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	protected void buildPath() {
		String pkg = LangUtils.getClassPackageName(this.getClass().getCanonicalName());
		this.path = LangUtils.packageToPath(pkg);
	}
	
	protected void fillBuiltInParams() {
		params.put(JRParameter.REPORT_LOCALE, config.getLocale());
		params.put(JRParameter.REPORT_TIME_ZONE, config.getTimeZone());
		params.put("WT_GENERATED_BY", config.getGeneratedBy());
		params.put("WT_PRINTED_BY", config.getPrintedBy());
		params.put("WT_DATE_FORMAT_SHORT", config.getDateFormatShort());
		params.put("WT_DATE_FORMAT_LONG", config.getDateFormatLong());
		params.put("WT_TIME_FORMAT_SHORT", config.getTimeFormatShort());
		params.put("WT_TIME_FORMAT_LONG", config.getTimeFormatLong());
	}
}
