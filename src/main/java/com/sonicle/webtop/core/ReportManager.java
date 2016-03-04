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

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.io.AbstractReport;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ReportManager {
	private static final Logger logger = WT.getLogger(ReportManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized ReportManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		ReportManager rptm = new ReportManager(wta);
		initialized = true;
		logger.info("ReportManager initialized.");
		return rptm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private ReportManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		
	}
	
	private void exportReportToPdfStream(JasperPrint jasperPrint, OutputStream outputStream) throws JRException {
		JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
	}
	
	private void exportReportToHtmlStream(JasperPrint jasperPrint, OutputStream outputStream) throws JRException, WTException {
		File temp = wta.createTempFile();
		FileInputStream fis = null;
		try {
			JasperExportManager.exportReportToHtmlFile(jasperPrint, temp.getAbsolutePath());
			fis = new FileInputStream(temp);
			IOUtils.copy(fis, outputStream);
		} catch (IOException ex) {
			throw new WTException(ex);
		} finally {
			IOUtils.closeQuietly(fis);
			wta.deleteTempFile(temp.getName());
		}
	}
	
	public void generateToStream(AbstractReport report, AbstractReport.OutputType outputType, OutputStream outputStream) throws JRException, WTException {
		InputStream rptIs = null;
		
		try {
			if(report.getConfig().getHasResourceBundle()) {
				ResourceBundle bundle = loadResourceBundle(report);
				report.getParameters().put(JRParameter.REPORT_RESOURCE_BUNDLE, bundle);
			}
			
			rptIs = loadReport(report);
			JasperPrint jp = JasperFillManager.fillReport(rptIs, report.getParameters(), report.getDataSource());
			switch(outputType) {
				case HTML:
					exportReportToHtmlStream(jp, outputStream);
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
		//String pkgName = LangUtils.getClassPackageName(report.getClass());
		//String path = LangUtils.packageToPath(pkgName) + "/" + report.getName();
		String path = report.getPath() + report.getName();
		return ResourceBundle.getBundle(path, report.getConfig().getLocale());
	}
	
	private InputStream loadReport(AbstractReport report) throws WTException {
		String rptName = report.getName() + ".jasper";
		//String pkgName = LangUtils.getClassPackageName(report.getClass());
		//String path = LangUtils.packageToPath(pkgName) + "/" + rptName;
		String path = report.getPath() + rptName;
		ClassLoader cl = LangUtils.findClassLoader(report.getClass());
		InputStream is = cl.getResourceAsStream(path);
		if(is == null) throw new WTException("Unable to load resource [{0}]", path);
		return is;
	}
}
