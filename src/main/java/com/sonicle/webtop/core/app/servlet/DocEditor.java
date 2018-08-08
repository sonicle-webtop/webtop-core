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
import com.sonicle.commons.web.ParameterException;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.DocEditorManager;
import com.sonicle.webtop.core.app.IDocEditorDocumentHandler;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.servlet.js.DocEditorCallbackPayload;
import com.sonicle.webtop.core.app.servlet.js.DocEditorCallbackResponse;
import com.sonicle.webtop.core.sdk.UserProfileId;
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
	public static final String EDITING_ID_PARAM = "eid";
	
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		
		ThreadState threadState = new SubjectThreadState(wta.getAdminSubject());
		threadState.bind();
		try {
			processRequestAsAdmin(request, response);
		} finally {
			threadState.clear();
		}
	}
	
	protected void processRequestAsAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		DocEditorManager docEdMgr = wta.getDocEditorManager();
		
		String path = URIUtils.removeTrailingSeparator(request.getPathInfo());
		if (StringUtils.equalsIgnoreCase(path, DOWNLOAD_PATH)) {
			String editingId = ServletUtils.getStringParameter(request, EDITING_ID_PARAM, true);
			
			IDocEditorDocumentHandler docHandler = docEdMgr.getDocumentHandler(editingId);
			if (docHandler == null) throw new RuntimeException();
			
			ServletUtils.setContentTypeHeader(response, "application/octet-stream");
			IOUtils.copy(docHandler.readDocument(), response.getOutputStream());
			
		} else if (StringUtils.equalsIgnoreCase(path, TRACK_PATH)) {
			logger.debug("TRACK_PATH");
			String editingId = ServletUtils.getStringParameter(request, EDITING_ID_PARAM, true);
			Payload<MapItem, DocEditorCallbackPayload> payload = ServletUtils.getPayload(request, DocEditorCallbackPayload.class);
			
			IDocEditorDocumentHandler docHandler = docEdMgr.getDocumentHandler(editingId);
			if (docHandler == null) throw new RuntimeException();
			
			if (payload.data.status == 1) { // document is being edited
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
				
			} else if (payload.data.status == 2) { // document is ready for saving
				if (!docHandler.isWriteSupported()) throw new RuntimeException();
				
				URI url = URIUtils.createURIQuietly(payload.data.url);
				if (url == null) throw new RuntimeException();
				
				InputStream is = null;
				try {
					HttpClient httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), url);
					is = HttpClientUtils.getContent(httpCli, url);
					docHandler.writeDocument(is);
				} catch(IOException ex) {
					logger.error("Unable to save edited content", ex);
					throw new RuntimeException(ex);
				} finally {
					IOUtils.closeQuietly(is);
				}
				
				//UserProfileId profileId = new UserProfileId(payload.data.users.get(0));
				docEdMgr.removeDocumentHandler(editingId);
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
				
			} else if (payload.data.status == 3) { // document saving error has occurred
				docEdMgr.removeDocumentHandler(editingId);
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
			} else if (payload.data.status == 4) { // document is closed with no changes
				docEdMgr.removeDocumentHandler(editingId);
				ServletUtils.writeJsonResponse(response, new DocEditorCallbackResponse(0));
			}
		}
	}
	
	
}
