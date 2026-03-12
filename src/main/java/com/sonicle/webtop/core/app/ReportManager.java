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

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ReportManager extends AbstractAppManager<ReportManager> {
	private static final Logger LOGGER = WT.getLogger(ReportManager.class);
	
	ReportManager(WebTopApp wta) {
		super(wta);
	}
	
	@Override
	protected Logger doGetLogger() {
		return LOGGER;
	}
	
	@Override
	protected void doAppManagerCleanup() {}
	
	private void exportReportToPdfStream(JasperPrint jasperPrint, OutputStream outputStream) throws JRException {
		JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
	}
	
	private void exportReportToHtmlStream(String domainId, JasperPrint jasperPrint, OutputStream outputStream) throws JRException, WTException {
		File temp = null;
		FileInputStream fis = null;
		try {
			temp = getWebTopApp().getFileSystem().createTempFile(domainId);
			JasperExportManager.exportReportToHtmlFile(jasperPrint, temp.getAbsolutePath());
			fis = new FileInputStream(temp);
			IOUtils.copy(fis, outputStream);
		} catch (IOException ex) {
			throw new WTException(ex);
		} finally {
			IOUtils.closeQuietly(fis);
			if (temp != null) {
				try {
					getWebTopApp().getFileSystem().deleteTempFile(domainId, temp.getName());
				} catch (IOException ex1) {
					throw new WTException(ex1);
				}
			}
		}
	}
	
	public void generateToStream(String domainId, AbstractReport report, AbstractReport.OutputType outputType, OutputStream outputStream) throws JRException, WTException {
		InputStream rptIs = null;
		
		try {
			if(!StringUtils.isBlank(report.getResourceBundleName())) {
				ResourceBundle bundle = loadResourceBundle(report);
				report.getParameters().put(JRParameter.REPORT_RESOURCE_BUNDLE, bundle);
			}
			
			rptIs = loadReport(report);
			JasperPrint jp = JasperFillManager.fillReport(rptIs, report.getParameters(), report.getDataSource());
			switch(outputType) {
				case HTML:
					exportReportToHtmlStream(domainId, jp, outputStream);
					break;
				case PDF:
					exportReportToPdfStream(jp, outputStream);
					break;
			}
		} finally {
			IOUtils.closeQuietly(rptIs);
		}
	}
	
	private ResourceBundle loadResourceBundle(AbstractReport report) throws WTException {
		if(report.getConfig().getLocale() == null) throw new WTException("Locale is required if /'HasResourceBundle/' is set to true");
		String path = report.getPath() + report.getResourceBundleName();
		return ResourceBundle.getBundle(path, report.getConfig().getLocale());
	}
	
	private InputStream loadReport(AbstractReport report) throws WTException {
		String rptName = report.getName() + ".jasper";
		String path = report.getPath() + rptName;
		ClassLoader cl = LangUtils.findClassLoader(report.getClass());
		InputStream is = cl.getResourceAsStream(path);
		if(is == null) throw new WTException("Unable to load resource [{0}]", path);
		return is;
	}
}
