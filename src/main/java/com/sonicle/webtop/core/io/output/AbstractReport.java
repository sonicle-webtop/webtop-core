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
package com.sonicle.webtop.core.io.output;

import com.sonicle.commons.ClassUtils;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRenderable;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.renderers.BatikRenderer;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author malbinola
 */
public abstract class AbstractReport {
	protected String name;
	protected String resourceBundleName;
	protected String path;
	protected ReportConfig config;
	protected Map<String, Object> params = new HashMap<>();
	protected JRDataSource dataSource;
	
	public AbstractReport(ReportConfig config) {
		this.config = config;
		buildPath();
		fillBuiltInParams();
	}
	
	public String getName() {
		return name;
	}
	
	public String getResourceBundleName() {
		return resourceBundleName;
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
	
	public void setDataSource(Collection<?> beanCollection) {
		setDataSource(new JRBeanCollectionDataSource(beanCollection, false));
	}
	
	protected void buildPath() {
		String pkg = ClassUtils.getClassPackageName(this.getClass());
		this.path = ClassUtils.classPackageAsPath(pkg) + "/";
	}
	
	protected void fillBuiltInParams() {
		params.put(JRParameter.REPORT_LOCALE, config.getLocale());
		params.put(JRParameter.REPORT_TIME_ZONE, config.getTimeZone());
		params.put("REPORT_PATH", path);
		params.put("WT_GENERATED_BY", config.getGeneratedBy());
		params.put("WT_PRINTED_BY", config.getPrintedBy());
		params.put("WT_DATE_FORMAT_SHORT", config.getDateFormatShort());
		params.put("WT_DATE_FORMAT_LONG", config.getDateFormatLong());
		params.put("WT_TIME_FORMAT_SHORT", config.getTimeFormatShort());
		params.put("WT_TIME_FORMAT_LONG", config.getTimeFormatLong());
	}
	
	protected void addTextStreamAsParam(String paramName, ClassLoader classLoader, String textResourceName, Charset charset) {
		try {
			params.put(paramName, readStringResource(classLoader, textResourceName, charset));
		} catch (IOException ex) {
			throw new WTRuntimeException("Unable to read stream [{}]", textResourceName, ex);
		}
	}
	
	protected void addSvgStreamAsParam(String paramName, ClassLoader classLoader, String textResourceName, Charset charset) {
		try {
			params.put(paramName, (JRRenderable)BatikRenderer.getInstanceFromText(readStringResource(classLoader, textResourceName, charset)));
		} catch (IOException | JRException ex) {
			throw new WTRuntimeException("Unable to read stream [{}]", textResourceName, ex);
		}
	}
	
	protected void addImageStreamAsParam(String paramName, ClassLoader classLoader, String imageResourceName) {
		InputStream is = null;
		try {
			is = classLoader.getResourceAsStream(imageResourceName);
			params.put(paramName, ImageIO.read(is));
		} catch (IOException ex) {
			throw new WTRuntimeException("Unable to read stream", ex);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	protected String readStringResource(ClassLoader classLoader, String textResourceName, Charset charset) throws IOException {
		InputStream is = null;
		try {
			is = classLoader.getResourceAsStream(textResourceName);
			if (is == null) throw new IOException("InputStream is null");
			return IOUtils.toString(is, charset);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	public static enum OutputType {
		PDF,
		HTML
	}
}
