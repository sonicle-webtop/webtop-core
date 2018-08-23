/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.URIUtils;
import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.webtop.core.app.sdk.BaseDocEditorDocumentHandler;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.DocEditorManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.sdk.WTServletException;
import com.sonicle.webtop.core.app.servlet.js.DocEditorCallbackPayload;
import com.sonicle.webtop.core.app.servlet.js.DocEditorCallbackResponse;
import com.sonicle.webtop.core.sdk.DomainURIPath;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class DocEditor extends AbstractServlet {
	private static final Logger logger = LoggerFactory.getLogger(DocEditor.class);
	public static final String URL = "/doc-editor"; // Shiro.ini must reflect this URI!
	public static final String DOWNLOAD_PATH = "/oo/download";
	public static final String TRACK_PATH = "/oo/track";
	public static final String SESSION_ID_PARAM = "sid";
	public static final String EDITING_ID_PARAM = "eid";
	
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		
		ThreadState threadState = new SubjectThreadState(wta.getAdminSubject());
		threadState.bind();
		try {
			processRequestAsAdmin(request, response);
		} catch(Throwable t) {
			logger.error("Error fulfilling request", t);
			throw t;
		} finally {
			threadState.clear();
		}
	}
	
	protected void processRequestAsAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DomainURIPath path = new DomainURIPath(URIUtils.removeTrailingSeparator(request.getPathInfo()));
		WebTopApp wta = WebTopApp.get(request);
		DocEditorManager docEdMgr = wta.getDocEditorManager();
		
		String domainId = WT.findDomainIdByPublicName(path.getDomainPublicName());
		if (domainId == null) throw new WTServletException("Invalid domain public name [{0}]", path.getDomainPublicName());
		if (!wta.getDocumentServerEnabled(domainId)) throw new WTServletException("DocumentServer not enabled for domain [{}]", domainId);
		
		String remainingPath = path.getRemainingPath();
		if (StringUtils.equalsIgnoreCase(remainingPath, DOWNLOAD_PATH)) {
			String editingId = ServletUtils.getStringParameter(request, EDITING_ID_PARAM, true);
			
			BaseDocEditorDocumentHandler docHandler = docEdMgr.getDocumentHandler(editingId);
			if (docHandler == null) throw new WTServletException("Missing DocumentHandler [{}]", editingId);
			
			ServletUtils.setContentTypeHeader(response, "application/octet-stream");
			IOUtils.copy(docHandler.readDocument(), response.getOutputStream());
			
		} else if (StringUtils.equalsIgnoreCase(remainingPath, TRACK_PATH)) {
			String editingId = ServletUtils.getStringParameter(request, EDITING_ID_PARAM, true);
			Payload<MapItem, DocEditorCallbackPayload> payload = ServletUtils.getPayload(request, DocEditorCallbackPayload.class);
			
			BaseDocEditorDocumentHandler docHandler = docEdMgr.getDocumentHandler(editingId);
			if (docHandler == null) throw new WTServletException("Missing DocumentHandler [{}]", editingId);
			
			if (payload.data.status == 1) {
				logger.debug("Document is being edited [{}, {}]", editingId, payload.data.key);
				
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
				
			} else if ((payload.data.status == 2) || (payload.data.status == 6)) {
				if (payload.data.status == 2) {
					logger.debug("Document is ready for saving [{}, {}]", editingId, payload.data.key);
				} else if (payload.data.status == 6) {
					logger.debug("Document is being edited, but the current document state is saved [{}, {}]", editingId, payload.data.key);
				}
				if (!docHandler.isWriteSupported()) throw new WTServletException("Write is not supported here [{}]", editingId);
				
				URI url = URIUtils.createURIQuietly(payload.data.url);
				if (url == null) throw new WTServletException("Invalid URL [{}]", payload.data.url);
				
				/*
				if (true) {
					long lastModified = docHandler.getLastModifiedTime();
					if (lastModified != -1) {
						String key = docEdMgr.buildDocumentKey(docHandler.getDocumentUniqueId(), lastModified);
						if (!StringUtils.equals(payload.data.key, key)) {
							throw new WTServletException("Original file was modified outside this session [{}]", editingId);
						}
					}
				}
				*/
				
				InputStream is = null;
				try {
					HttpClient httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), url);
					is = HttpClientUtils.getContent(httpCli, url);
					docHandler.writeDocument(is);
				} catch(IOException ex) {
					throw new WTServletException("Unable to save edited content [{}]", editingId, ex);
				} finally {
					IOUtils.closeQuietly(is);
				}
				
				//UserProfileId profileId = new UserProfileId(payload.data.users.get(0));
				if (payload.data.status == 2) {
					docEdMgr.unregisterDocumentHandler(editingId);
				}
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
				
			} else if ((payload.data.status == 3) || (payload.data.status == 7)) {
				if (payload.data.status == 3) {
					logger.debug("Document saving error has occurred [{}, {}]", payload.data.key);
				} else if (payload.data.status == 7) {
					logger.debug("Error has occurred while force saving the document [{}, {}]", payload.data.key);
				}
				docEdMgr.unregisterDocumentHandler(editingId);
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
				
			} else if (payload.data.status == 4) {
				logger.debug("Document is closed with no changes [{}, {}]", editingId, payload.data.key);
				docEdMgr.unregisterDocumentHandler(editingId);
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
			}
		}
	}
}
