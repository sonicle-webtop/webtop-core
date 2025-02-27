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

import com.google.gson.annotations.SerializedName;
import com.sonicle.webtop.core.app.sdk.BaseDocEditorDocumentHandler;
import com.sonicle.commons.AlgoUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.servlet.DocEditor;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
import net.sf.qualitycheck.Check;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class DocEditorManager extends AbstractAppManager<DocEditorManager> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocEditorManager.class);
	private final MultiValuedMap<String, String> editingIdsBySessionId = new ArrayListValuedHashMap<>();
	private final Map<String, String> sessionIdByEditingId = new HashMap<>();
	private final Map<String, BaseDocEditorDocumentHandler> handlers = new HashMap<>();
	private final Map<String, EditingParams> handlersParams = new HashMap<>();
	private final long timeToLiveMillis;
	private final Map<String, Long> expirationCandidates = new HashMap();
	
	DocEditorManager(WebTopApp wta, final long timeToLiveMillis) {
		super(wta, true);
		this.timeToLiveMillis = timeToLiveMillis;
		LOGGER.debug("timeToLive: {}", timeToLiveMillis);
		initialize();
	}

	@Override
	protected Logger doGetLogger() {
		return LOGGER;
	}
	
	@Override
	protected void doAppManagerCleanup() {
		handlers.clear();
		handlersParams.clear();
		sessionIdByEditingId.clear();
		editingIdsBySessionId.clear();
		expirationCandidates.clear();
	}
	
	public static boolean isEditable(final String fileName) {
		return getDocumentType(fileName) != null;
	}
	
	public EditingResult registerEditing(final String wtSessionId, final BaseDocEditorDocumentHandler docHandler, final String filename, final long lastModifiedTime) throws WTException, FileSystemException {
		Check.notEmpty(wtSessionId, "wtSessionId");
		Check.notNull(docHandler, "docHandler");
		Check.notEmpty(filename, "filename");
		
		long stamp = readyLock();
		try {
			try {
				internalCleanupExpired(System.currentTimeMillis());
				String editingId = buildEditingId(RunContext.getRunProfileId());
				DocumentType type = getDocumentType(filename);
				if (type == null) throw new WTException("File is not supported by DocumentEditor [{}]", filename);
				String ext = getDocumentExtension(filename);
				String key = buildDocumentKey(docHandler.getDocumentUniqueId(), lastModifiedTime);
				String baseUrl = getWebTopApp().getDocumentServerLoopbackUrl();
				String domainPublicName = WT.getDomainPublicName(docHandler.getTargetProfileId().getDomainId());
				String url = generateUrl(baseUrl, domainPublicName, wtSessionId, editingId).toString();
				String callbackUrl = buildCallbackUrl(baseUrl, domainPublicName, wtSessionId, editingId).toString();
				
				EditingParams params = new EditingParams(type, UriParser.decode(filename), ext, key, url, callbackUrl);
				
				LOGGER.debug("Registering DocumentHandler [{}, {}, {}, {} -> {}]", editingId, docHandler.getTargetProfileId().getDomainId(), wtSessionId, docHandler.getDocumentUniqueId(), filename);
				LOGGER.debug("Document URL: {}", params.url);
				LOGGER.debug("Document callback URL: {}", params.callbackUrl);
				editingIdsBySessionId.put(wtSessionId, editingId);
				sessionIdByEditingId.put(editingId, wtSessionId);
				handlers.put(editingId, docHandler);
				handlersParams.put(editingId, params);
				
				return new EditingResult(editingId, params.type, params.name, params.extension, docHandler.isWriteSupported());

			} catch(URISyntaxException ex) {
				LOGGER.error("Unable to build URL", ex);
				return null;
			}
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public OOClientAPIBaseConfig getClientAPIConfig(final String editingId, final boolean view) {
		Check.notEmpty(editingId, "editingId");
		
		try {
			long stamp = readyLock();
			try {
				LOGGER.debug("Generating ClientAPI config [{}]", editingId);
				BaseDocEditorDocumentHandler docHandler = handlers.get(editingId);
				EditingParams editingParams = handlersParams.get(editingId);
				if (docHandler == null || editingParams == null) throw new WTNotFoundException("Editing session not found [{}]", editingId);
				
				OOClientAPIBaseConfig config = new OOClientAPIBaseConfig();
				config.document.fileType = editingParams.extension;
				config.document.key = editingParams.key;
				config.document.title = editingParams.name;
				config.document.url = editingParams.url;
				if (docHandler.isWriteSupported()) config.document.permissions.allowEditing();
				config.editorConfig.callbackUrl = editingParams.callbackUrl;
				config.editorConfig.mode = view ? OOEditorConfig.Mode.VIEW : OOEditorConfig.Mode.EDIT;
				
				String secret = getWebTopApp().getDocumentServerSecretOut(docHandler.getTargetProfileId().getDomainId());
				if (!StringUtils.isBlank(secret)) {
					config.token = generateToken(config, secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256);
					LOGGER.debug("JWT: {}", config.token);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("JSON config: {}", JsonResult.gson(false).toJson(config, OOClientAPIBaseConfig.class));
				}
				
				return config;
				
			} finally {
				readyUnlock(stamp);
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
		return null;
	}
	
	public void clearEditing(final String editingId) {
		Check.notEmpty(editingId, "editingId");
		
		try {
			long stamp = readyLock();
			try {
				LOGGER.debug("Unregistering DocumentHandler [{}]", editingId);
				String sessionId = sessionIdByEditingId.remove(editingId);
				editingIdsBySessionId.remove(sessionId);
				handlers.remove(editingId);
				handlersParams.remove(editingId);
			} finally {
				readyUnlock(stamp);
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
	}
	
	public BaseDocEditorDocumentHandler getDocumentHandler(final String editingId) {
		Check.notEmpty(editingId, "editingId");
		
		try {
			long stamp = readyLock();
			try {
				return handlers.get(editingId);
			} finally {
				readyUnlock(stamp);
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
		return null;
	}
	
	public String buildDocumentKey(String documentUniqueId, long lastModifiedTime) {
		String s = documentUniqueId + String.valueOf((lastModifiedTime > -1) ? lastModifiedTime : DateTimeUtils.now().getMillis());
		return StringUtils.left(AlgoUtils.md5Hex(s), 20);
	}
	
	void cleanupOnSessionDestroy(String sessionId) {
		try {
			long stamp = readyLock();
			try {
				if (editingIdsBySessionId.containsKey(sessionId)) {
					expirationCandidates.put(sessionId, System.currentTimeMillis());
				}
			} finally {
				readyUnlock(stamp);
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
	}
	
	private void internalCleanupExpired(final long now) {
		final Iterator<Map.Entry<String, Long>> it = expirationCandidates.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<String, Long> entry = it.next();
			if (now >= (entry.getValue() + timeToLiveMillis)) {
				final String sessionId = entry.getKey();
				if (editingIdsBySessionId.containsKey(sessionId)) {
					LOGGER.debug("Expiring entries for session {}", sessionId);
					internalRemoveBySession(sessionId);
				}
			}
			it.remove();
		}
	}
	
	private void internalRemoveBySession(String sessionId) {
		Collection<String> editingIds = editingIdsBySessionId.remove(sessionId);
		for (String editingId : editingIds) {
			LOGGER.debug("Cleaning DocumentHandler [{}]", editingId);
			sessionIdByEditingId.remove(editingId);
			handlers.remove(editingId);
			handlersParams.remove(editingId);
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
	
	private String generateToken(final OOClientAPIBaseConfig payload, final byte[] key, final SignatureAlgorithm keyAlgorithm) {
		return generateToken(JsonResult.gsonPlain(false).toJson(payload, OOClientAPIBaseConfig.class), key, keyAlgorithm);
	}
	
	private String generateToken(final String payload, final byte[] key, final SignatureAlgorithm keyAlgorithm) {
		SecretKey signingKey = new SecretKeySpec(key, keyAlgorithm.getJcaName());
		return Jwts.builder()
			.setPayload(payload)
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
	
	/**
	 * https://api.onlyoffice.com/docs/docs-api/additional-api/signature/browser/
	 * https://api.onlyoffice.com/docs/docs-api/usage-api/advanced-parameters/
	 * //https://github.com/ONLYOFFICE/server/blob/8f4fd3383656602115abfd00a67c77aa6e5d46f9/DocService/sources/DocsCoServer.js#L2330
	 */
	public static class OOClientAPIBaseConfig {
		public final OODocument document = new OODocument();
		public final OOEditorConfig editorConfig = new OOEditorConfig();
		public String token;
	}
	
	/**
	 * https://api.onlyoffice.com/docs/docs-api/usage-api/config/document/
	 */
	public static class OODocument {
		public String fileType;
		public String key;
		public String title;
		public String url;
		public final Permissions permissions = new Permissions();

		public static class Permissions {
			public boolean chat = false;
			public boolean comment = false;
			//commentGroups
			public boolean copy = true;
			public boolean deleteCommentAuthorOnly = false;
			public boolean download = true;
			public boolean edit = false;
			public boolean editCommentAuthorOnly = false;
			public boolean fillForms = true;
			public boolean modifyContentControl = true;
			public boolean modifyFilter = true;
			public boolean print = true;
			public boolean protect = false;
			public boolean review = false;
			//reviewGroups
			//userInfoGroups
			
			public void allowEditing() {
				this.comment = true;
				this.edit = true;
				this.protect = true;
				this.review = true;
			}
		}

		public static class ReferenceData {
			public String fileKey;
			public String instanceId;

		}
	}
	
	/**
	 * https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/
	 */
	public static class OOEditorConfig {
		public String callbackUrl;
		public Mode mode = Mode.EDIT;
		//...user
		
		public static enum Mode {
			@SerializedName("edit") EDIT,
			@SerializedName("view") VIEW
		}
	}
	
	public static class EditingResult {
		public final String editingId;
		public final DocumentType docType;
		public final String docName;
		public final String docExtension;
		public final boolean editable;
		public final boolean downloadable;
		public final boolean printable;
		public final boolean commentable;
		public final boolean reviewable;
		
		public EditingResult(String editingId, DocumentType type, String name, String extension, boolean editable) {
			this.editingId = Check.notNull(editingId, "editingId");
			this.docType = Check.notNull(type, "docType");
			this.docName = Check.notEmpty(name, "docName");
			this.docExtension = Check.notNull(extension, "docExtension");
			this.editable = editable;
			this.downloadable = false;
			this.printable = false;
			this.commentable = false;
			this.reviewable = false;
		}
	}
	
	private static class EditingParams {
		public final DocumentType type;
		public final String name;
		public final String extension;
		public final String key;
		public final String url;
		public final String callbackUrl;
		
		public EditingParams(DocumentType type, String name, String extension, String key, String url, String callbackUrl) {
			this.type = Check.notNull(type, "type");
			this.name = Check.notEmpty(name, "name");
			this.extension = Check.notNull(extension, "extension");
			this.key = Check.notEmpty(key, "key");
			this.url = Check.notEmpty(url, "url");
			this.callbackUrl = Check.notEmpty(callbackUrl, "callbackUrl");
		}
	}
	
	private static String getDocumentExtension(String filename) {
		return StringUtils.lowerCase(FilenameUtils.getExtension(filename));
	}
	
	public static DocumentType getDocumentType(String filename) {
		String ext = getDocumentExtension(filename);
		if (TEXT_DOCUMENT_EXTENSIONS.contains(ext)) return DocumentType.TEXT_DOCUMENT;
		if (SPREADSHEET_EXTENSIONS.contains(ext)) return DocumentType.SPREADSHEET;
		if (PRESENTATION_EXTENSIONS.contains(ext)) return DocumentType.PRESENTATION;
		if (PDF_EXTENSIONS.contains(ext)) return DocumentType.PORTABLE_DOCUMENT;
		return null;
	}
	
	public static enum DocumentType {
		@SerializedName("word") TEXT_DOCUMENT,
		@SerializedName("cell") SPREADSHEET,
		@SerializedName("slide") PRESENTATION,
		@SerializedName("pdf") PORTABLE_DOCUMENT
	}
	
	// Enumerates avail document types: https://api.onlyoffice.com/docs/docs-api/usage-api/config/
	private static final Set<String> TEXT_DOCUMENT_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"doc", "docm", "docx", "dot", "dotm", "dotx", "epub", "fb2", "fodt", "htm", "html", "mht", "mhtml", "odt", "ott", "pages", "rtf", "stw", "sxw", "txt", "wps", "wpt", "xml"
	)));
	private static final Set<String> SPREADSHEET_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"csv", "et", "ett", "fods", "numbers", "ods", "ots", "sxc", "xls", "xlsb", "xlsm", "xlsx", "xlt", "xltm", "xltx", "xml"
	)));
	private static final Set<String> PRESENTATION_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"dps", "dpt", "fodp", "key", "odp", "otp", "pot", "potm", "potx", "pps", "ppsm", "ppsx", "ppt", "pptm", "pptx", "sxi"
	)));
	private static final Set<String> PDF_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"djvu", "docxf", "oform", "oxps", "pdf", "xps"
	)));
}
