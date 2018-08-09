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
package com.sonicle.webtop.core.app;

import com.sonicle.webtop.core.app.sdk.BaseDocEditorDocumentHandler;
import com.sonicle.commons.AlgoUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.servlet.DocEditor;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import groovy.json.internal.Charsets;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class DocEditorManager extends AbstractAppManager {
	private static final Logger logger = LoggerFactory.getLogger(DocEditorManager.class);
	private static final String DOCUMENTTYPE_TEXT = "text";
	private static final Set<String> EXTENSIONS_TEXT = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"doc", "docm", "docx", "dot", "dotm", "dotx", "epub", "fodt", "htm", "html", "mht", "odt", "ott", "pdf", "rtf", "txt", "djvu", "xps"
	)));
	private static final String DOCUMENTTYPE_SPREADSHEET = "spreadsheet";
	private static final Set<String> EXTENSIONS_SPREADSHEET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"csv", "fods", "ods", "ots", "xls", "xlsm", "xlsx", "xlt", "xltm", "xltx"
	)));
	private static final String DOCUMENTTYPE_PRESENTATION = "presentation";
	private static final Set<String> EXTENSIONS_PRESENTATION = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"fodp", "odp", "otp", "pot", "potm", "potx", "pps", "ppsm", "ppsx", "ppt", "pptm", "pptx"
	)));
	
	private final Map<String, BaseDocEditorDocumentHandler> handlers = new HashMap<>();
	
	DocEditorManager(WebTopApp wta) {
		super(wta);
	}
	
	@Override
	protected void internalCleanup() {
		handlers.clear();
	}
	
	public DocumentConfig addDocumentHandler(String filename, String uniqueId, long lastModifiedTime, BaseDocEditorDocumentHandler docHandler) throws WTException {
		lock.lock();
		try {
			String documentType = getDocumentType(filename);
			if (documentType == null) throw new WTException("File is not editable [{}]", filename);
			String ext = FilenameUtils.getExtension(filename);
			
			String editingId = buildEditingId(RunContext.getRunProfileId());
			handlers.put(editingId, docHandler);
			
			String secret = wta.getDocumentServerSecret(docHandler.getTargetProfileId().getDomainId());
			String token = StringUtils.isBlank(secret) ? null : generateToken(secret.getBytes(Charsets.UTF_8), SignatureAlgorithm.HS256);
			String domainPublicName = WT.getDomainPublicName(docHandler.getTargetProfileId().getDomainId());
			String key = buildKey(filename, uniqueId, lastModifiedTime);
			String baseUrl = wta.getDocumentServerLoopbackUrl();
			String url = generateUrl(baseUrl, domainPublicName, editingId).toString();
			String callbackUrl = buildCallbackUrl(baseUrl, domainPublicName, editingId).toString();
			return new DocumentConfig(editingId, token, documentType, ext, key, url, callbackUrl, docHandler.isWriteSupported());
		
		} catch(URISyntaxException ex) {
			logger.error("Unable to build URL", ex);
			return null;
		} finally {
			lock.unlock();
		}
	}
	
	public BaseDocEditorDocumentHandler getDocumentHandler(String docId) {
		lock.lock();
		try {
			return handlers.get(docId);
			
		} finally {
			lock.unlock();
		}
	}
	
	public void removeDocumentHandler(String docId) {
		lock.lock();
		try {
			handlers.remove(docId);
			
		} finally {
			lock.unlock();
		}
	}
	
	private String buildKey(String fileName, String uniqueId, long lastModifiedTime) {
		String s = StringUtils.defaultIfBlank(uniqueId, fileName);
		s += String.valueOf((lastModifiedTime > -1) ? lastModifiedTime : DateTimeUtils.now().getMillis());
		return StringUtils.left(AlgoUtils.md5Hex(s), 20);
	}
	
	private String buildEditingId(UserProfileId profileId) {
		StringBuilder sb = new StringBuilder();
		sb.append(IdentifierUtils.getUUIDTimeBased(true));
		if (profileId != null) {
			sb.append("-");
			sb.append(profileId.toString());
		}
		return DigestUtils.md5Hex(sb.toString());
	}
	
	private String generateToken(byte[] key, SignatureAlgorithm keyAlgorithm) {
		SecretKey signingKey = new SecretKeySpec(key, keyAlgorithm.getJcaName());
		return Jwts.builder()
				.setPayload("{}")
				.signWith(signingKey, keyAlgorithm)
				.compact();
	}
	
	private URI generateUrl(String loopbackUrl, String domainPubName, String editingId) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(loopbackUrl);
		URIUtils.appendPath(builder, URIUtils.concatPaths(DocEditor.URL, DocEditor.DOWNLOAD_PATH));
		builder.addParameter(DocEditor.DOMAIN_PARAM, domainPubName);
		builder.addParameter(DocEditor.EDITING_ID_PARAM, editingId);
		return builder.build();
	}
	
	private URI buildCallbackUrl(String loopbackUrl, String domainPubName, String editingId) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(loopbackUrl);
		URIUtils.appendPath(builder, URIUtils.concatPaths(DocEditor.URL, DocEditor.TRACK_PATH));
		builder.addParameter(DocEditor.DOMAIN_PARAM, domainPubName);
		builder.addParameter(DocEditor.EDITING_ID_PARAM, editingId);
		return builder.build();
	}
	
	public static boolean isEditable(String fileName) {
		return getDocumentType(fileName) != null;
	}
	
	public static String getDocumentType(String fileName) {
		String ext = FilenameUtils.getExtension(fileName);
		if (EXTENSIONS_TEXT.contains(ext)) return DOCUMENTTYPE_TEXT;
		if (EXTENSIONS_SPREADSHEET.contains(ext)) return DOCUMENTTYPE_SPREADSHEET;
		if (EXTENSIONS_PRESENTATION.contains(ext)) return DOCUMENTTYPE_PRESENTATION;
		return null;
	}
	
	public static class DocumentConfig {
		public final String editingId;
		public final String token;
		public final String docType;
		public final String docExtension;
		public final String docKey;
		public final String docUrl;
		public final String callbackUrl;
		public final boolean writeSupported;
		
		public DocumentConfig(String editingId, String token, String docType, String docExtension, String docKey, String docUrl, String callbackUrl, boolean writeSupported) {
			this.editingId = editingId;
			this.token = token;
			this.docType = docType;
			this.docExtension = docExtension;
			this.docKey = docKey;
			this.docUrl = docUrl;
			this.callbackUrl = callbackUrl;
			this.writeSupported = writeSupported;
		}
	}
}
