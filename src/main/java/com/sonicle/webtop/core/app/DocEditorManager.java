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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
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
	
	private final MultiValuedMap<String, String> editingIdsBySessionId = new ArrayListValuedHashMap<>();
	private final Map<String, String> sessionIdByEditingId = new HashMap<>();
	private final Map<String, BaseDocEditorDocumentHandler> handlers = new HashMap<>();
	private final long timeToLiveMillis;
	private final Map<String, Long> expirationCandidates = new HashMap();
	
	DocEditorManager(WebTopApp wta, final long timeToLiveMillis) {
		super(wta);
		this.timeToLiveMillis = timeToLiveMillis;
		logger.info("Initialized");
		logger.debug("timeToLive: {}", timeToLiveMillis);
	}
	
	@Override
	protected void internalAppManagerCleanup() {
		handlers.clear();
		sessionIdByEditingId.clear();
		editingIdsBySessionId.clear();
		expirationCandidates.clear();
		logger.info("Cleaned up");
	}
	
	public DocumentConfig registerDocumentHandler(String sessionId, BaseDocEditorDocumentHandler docHandler, String filename, long lastModifiedTime) throws WTException {
		lock.lock();
		try {
			internalCleanupExpired(System.currentTimeMillis());
			String documentType = getDocumentType(filename);
			if (documentType == null) throw new WTException("File is not supported by DocumentEditor [{}]", filename);
			String ext = FilenameUtils.getExtension(filename);
			
			String editingId = buildEditingId(RunContext.getRunProfileId());
			String secret = wta.getDocumentServerSecretOut(docHandler.getTargetProfileId().getDomainId());
			//TODO: read the algo from a dedicated setting
			String token = StringUtils.isBlank(secret) ? null : generateToken(secret.getBytes(Charsets.UTF_8), SignatureAlgorithm.HS256);
			String domainPublicName = WT.getDomainPublicName(docHandler.getTargetProfileId().getDomainId());
			String key = buildDocumentKey(docHandler.getDocumentUniqueId(), lastModifiedTime);
			String baseUrl = wta.getDocumentServerLoopbackUrl();
			String url = generateUrl(baseUrl, domainPublicName, sessionId, editingId).toString();
			String callbackUrl = buildCallbackUrl(baseUrl, domainPublicName, sessionId, editingId).toString();
			
			logger.debug("Registering DocumentHandler [{}, {}, {}, {} -> {}]", editingId, docHandler.getTargetProfileId().getDomainId(), sessionId, docHandler.getDocumentUniqueId(), filename);
			editingIdsBySessionId.put(sessionId, editingId);
			sessionIdByEditingId.put(editingId, sessionId);
			handlers.put(editingId, docHandler);
			
			logger.debug("Document URL: {}", url);
			logger.debug("Document callback URL: {}", callbackUrl);
			logger.debug("JWT: {}", token);
			
			return new DocumentConfig(editingId, token, documentType, filename, ext, key, url, callbackUrl, docHandler.isWriteSupported());
		
		} catch(URISyntaxException ex) {
			logger.error("Unable to build URL", ex);
			return null;
		} finally {
			lock.unlock();
		}
	}
	
	public void unregisterDocumentHandler(String editingId) {
		lock.lock();
		try {
			logger.debug("Unregistering DocumentHandler [{}]", editingId);
			String sessionId = sessionIdByEditingId.remove(editingId);
			editingIdsBySessionId.remove(sessionId);
			handlers.remove(editingId);
		} finally {
			lock.unlock();
		}
	}
	
	public BaseDocEditorDocumentHandler getDocumentHandler(String editingId) {
		lock.lock();
		try {
			return handlers.get(editingId);
		} finally {
			lock.unlock();
		}
	}
	
	public String buildDocumentKey(String documentUniqueId, long lastModifiedTime) {
		String s = documentUniqueId + String.valueOf((lastModifiedTime > -1) ? lastModifiedTime : DateTimeUtils.now().getMillis());
		return StringUtils.left(AlgoUtils.md5Hex(s), 20);
	}
	
	void cleanupOnSessionDestroy(String sessionId) {
		lock.lock();
		try {
			if (editingIdsBySessionId.containsKey(sessionId)) {
				expirationCandidates.put(sessionId, System.currentTimeMillis());
			}
		} finally {
			lock.unlock();
		}
	}
	
	private void internalCleanupExpired(final long now) {
		final Iterator<Map.Entry<String, Long>> it = expirationCandidates.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<String, Long> entry = it.next();
			if (now >= (entry.getValue() + timeToLiveMillis)) {
				final String sessionId = entry.getKey();
				if (editingIdsBySessionId.containsKey(sessionId)) {
					logger.debug("Expiring entries for session {}", sessionId);
					internalRemoveBySession(sessionId);
				}
			}
			it.remove();
		}
	}
	
	private void internalRemoveBySession(String sessionId) {
		Collection<String> editingIds = editingIdsBySessionId.remove(sessionId);
		for (String editingId : editingIds) {
			logger.debug("Cleaning DocumentHandler [{}]", editingId);
			sessionIdByEditingId.remove(editingId);
			handlers.remove(editingId);
		}
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
	
	private URI generateUrl(String loopbackUrl, String domainPubName, String sessionId, String editingId) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(loopbackUrl);
		URIUtils.appendPath(builder, URIUtils.concatPaths(DocEditor.URL, domainPubName, DocEditor.DOWNLOAD_PATH));
		if (!StringUtils.isBlank(sessionId)) builder.addParameter(DocEditor.SESSION_ID_PARAM, sessionId);
		builder.addParameter(DocEditor.EDITING_ID_PARAM, editingId);
		return builder.build();
	}
	
	private URI buildCallbackUrl(String loopbackUrl, String domainPubName, String sessionId, String editingId) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(loopbackUrl);
		URIUtils.appendPath(builder, URIUtils.concatPaths(DocEditor.URL, domainPubName, DocEditor.TRACK_PATH));
		if (!StringUtils.isBlank(sessionId)) builder.addParameter(DocEditor.SESSION_ID_PARAM, sessionId);
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
		public final String docName;
		public final String docExtension;
		public final String docKey;
		public final String docUrl;
		public final String callbackUrl;
		public final boolean writeSupported;
		
		public DocumentConfig(String editingId, String token, String docType, String docName, String docExtension, String docKey, String docUrl, String callbackUrl, boolean writeSupported) {
			this.editingId = editingId;
			this.token = token;
			this.docType = docType;
			this.docName = docName;
			this.docExtension = docExtension;
			this.docKey = docKey;
			this.docUrl = docUrl;
			this.callbackUrl = callbackUrl;
			this.writeSupported = writeSupported;
		}
	}
}
