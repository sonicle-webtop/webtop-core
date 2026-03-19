/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.google.gson.annotations.SerializedName;
import com.sonicle.webtop.core.app.sdk.BaseDocEditorDocumentHandler;
import com.sonicle.commons.AlgoUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.time.JodaTimeUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
 * Manages active OnlyOffice editing sessions and the related runtime state.
 * 
 * This manager keeps track of:
 * - the mapping between WebTop session IDs and active editing IDs
 * - the runtime data associated with each editing session
 * - delayed cleanup of editing sessions after HTTP session destruction
 * 
 * Concurrency model:
 * - a keyed lock is used to serialize operations on the same editingId
 * - a dedicated index lock protects shared secondary indexes based on sessionId
 * 
 * @author malbinola
 */
public class DocEditorManager extends AbstractAppManager<DocEditorManager> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocEditorManager.class);
	private final long expirationTTL;
	private final MultiValuedMap<String, String> editingIdsBySessionId = new ArrayListValuedHashMap<>();
	private final ConcurrentMap<String, EditingEntry> entriesByEditingId = new ConcurrentHashMap<>();
	private final Map<String, Long> expirationQuarantine = new HashMap<>(); // Session IDs to be expired
	private final Object indexLock = new Object();
	private final KeyedReentrantLocks<String> editingLocks = new KeyedReentrantLocks<>();
	
	DocEditorManager(WebTopApp wta, final long expirationTTL ){
		super(wta);
		this.expirationTTL = expirationTTL;
		LOGGER.debug("expirationTTL: {}", expirationTTL);
	}

	@Override
	protected Logger doGetLogger() {
		return LOGGER;
	}
	
	@Override
	protected void doAppManagerCleanup() {	
		synchronized (indexLock) {
			editingIdsBySessionId.clear();
			expirationQuarantine.clear();
		}
		entriesByEditingId.clear();
	}
	
	public static boolean isEditable(final String fileName) {
		return getDocumentType(fileName) != null;
	}
	
	/**
	 * Builds a stable document key for OnlyOffice based on the document unique ID
	 * and the last modification timestamp.
	 * If the last modification timestamp is unavailable, the current time is used.
	 * @param documentUniqueId the logical unique identifier of the document
	 * @param lastModifiedTime the last modification time, or a negative value if unavailable
	 * @return a shortened MD5-based document key
	 */
	public String buildDocumentKey(final String documentUniqueId, final long lastModifiedTime) {
		String s = documentUniqueId + String.valueOf((lastModifiedTime > -1) ? lastModifiedTime : JodaTimeUtils.now().getMillis());
		return StringUtils.left(AlgoUtils.md5Hex(s), 20);
	}
	
	/**
	 * Marks all editing sessions associated with the specified WebTop session
	 * as expiration candidates.
	 * Cleanup is intentionally delayed to tolerate late callbacks or trailing requests
	 * that may still arrive shortly after HTTP session destruction.
	 * @param sessionId the destroyed WebTop session ID
	 */
	void onHttpSessionDestroyCleanup(final String sessionId) {
		try {
			LOGGER.debug("onHttpSessionDestroyCleanup [{}]", sessionId);
			ensureStateReady();
			synchronized (indexLock) {
				if (editingIdsBySessionId.containsKey(sessionId)) {
					long millis = System.currentTimeMillis();
					LOGGER.debug("Adding HTTP session '{}' into expiration quarantine [{}]", sessionId, millis);
					expirationQuarantine.put(sessionId, millis);
					
				} else {
					LOGGER.debug("No editing-sessions bound to HTTP session '{}'", sessionId);
				}
			}
			
		} catch (WTException ex) {
			// Do NOT rethrown errors...
			LOGGER.warn("Manager NOT ready yet", ex);
		}
	}
	
	/**
	 * Registers a new document editing session and returns the client-side metadata
	 * required to initialize the editor.
	 * 
	 * A fresh editing ID is generated for each registration.
	 * Before creating the new session, expired cleanup candidates are processed.
	 * 
	 * @param sessionId the owning HTTP session ID
	 * @param docHandler the document handler backing the editing session
	 * @param filename the original document filename
	 * @param lastModifiedTime the document last modification time, or a negative value if unavailable
	 * @return the editing session descriptor
	 * @throws WTException if the file type is unsupported, the manager is not ready
	 * @throws FileSystemException if the document handler cannot access the target resource
	 */
	public EditingResult registerEditing(final String sessionId, final BaseDocEditorDocumentHandler docHandler, final String filename, final long lastModifiedTime) throws WTException, FileSystemException {
		Check.notEmpty(sessionId, "sessionId");
		Check.notNull(docHandler, "docHandler");
		Check.notEmpty(filename, "filename");
		
		LOGGER.debug("Registering new editing-session [{}, '{}']", sessionId, filename);
		DocumentType type = getDocumentType(filename);
		if (type == null) throw new WTException("File is not supported by DocumentEditor [{}]", filename);
		
		ensureStateReady();
		internalCleanupExpiredSessions(System.currentTimeMillis());
		
		final String editingId = buildEditingId(RunContext.getRunProfileId());
		LOGGER.debug("{} is the generated editing ID [{}, {}]", editingId, sessionId, filename);
		editingLocks.lock(editingId);
		try {
			String ext = getDocumentExtension(filename);
			String key = buildDocumentKey(docHandler.getDocumentUniqueId(), lastModifiedTime);
			String baseUrl = getWebTopApp().getDocumentServerLoopbackUrl();
			if (StringUtils.isEmpty(baseUrl)) baseUrl = WT.getPublicBaseUrl(docHandler.getTargetProfileId().getDomainId());
			String domainPublicName = WT.getDomainPublicName(docHandler.getTargetProfileId().getDomainId());
			String url = generateUrl(baseUrl, domainPublicName, sessionId, editingId).toString();
			String callbackUrl = buildCallbackUrl(baseUrl, domainPublicName, sessionId, editingId).toString();
			EditingParams params = new EditingParams(type, UriParser.decode(filename), ext, key, url, callbackUrl);
			EditingEntry entry = new EditingEntry(sessionId, docHandler, params);

			LOGGER.debug("DocumentHandler [{}, {}, {}, {} -> '{}']", docHandler.getTargetProfileId(), editingId, sessionId, docHandler.getDocumentUniqueId(), filename);
			LOGGER.debug("Document URL: {}", params.url);
			LOGGER.debug("Document callback URL: {}", params.callbackUrl);
			
			LOGGER.debug("Registering editing-session [{}]", editingId);
			entriesByEditingId.put(editingId, entry);
			synchronized (indexLock) {
				editingIdsBySessionId.put(sessionId, editingId);
				expirationQuarantine.remove(sessionId);
			}

			return new EditingResult(editingId, params.type, params.name, params.extension, docHandler.isWriteSupported());

		} catch (URISyntaxException ex) {
			throw new WTException("Unable to build URL", ex);
			
		} finally {
			editingLocks.unlock(editingId);
		}
	}
	
	/**
	 * Builds the OnlyOffice client configuration for the specified editing session.
	 * The returned object includes document metadata, permissions, callback URL
	 * and, when configured, a signed JWT token.
	 * @param editingId the editing session identifier
	 * @param view `true` to open the editor in view mode, `false` for edit mode
	 * @return the client API configuration
	 * @throws WTException if the manager is not ready or the editing session does not exist
	 */
	public OOClientAPIBaseConfig getClientAPIConfig(final String editingId, final boolean view) throws WTException {
		Check.notEmpty(editingId, "editingId");
		ensureStateReady();
		
		LOGGER.debug("Generating OO client config for editing-session [{}]", editingId);
		editingLocks.lock(editingId);
		try {
			EditingEntry entry = entriesByEditingId.get(editingId);
			if (entry == null) throw new WTNotFoundException("Editing-session not found [{}]", editingId);
			
			OOClientAPIBaseConfig config = new OOClientAPIBaseConfig();
			config.document.fileType = entry.params.extension;
			config.document.key = entry.params.key;
			config.document.title = entry.params.name;
			config.document.url = entry.params.url;
			if (entry.handler.isWriteSupported()) config.document.permissions.withAllowEditing();
			config.editorConfig.callbackUrl = entry.params.callbackUrl;
			config.editorConfig.mode = view ? OOEditorConfig.Mode.VIEW : OOEditorConfig.Mode.EDIT;
			
			String secret = getWebTopApp().getDocumentServerSecretOut(entry.handler.getTargetProfileId().getDomainId());
			if (!StringUtils.isBlank(secret)) {
				config.token = generateToken(config, secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256);
				LOGGER.debug("JWT: {}", config.token);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("JSON config: {}", JsonResult.gson(false).toJson(config, OOClientAPIBaseConfig.class));
			}
			return config;
			
		} finally {
			editingLocks.unlock(editingId);
		}
	}
	
	/**
	 * Returns the document handler associated with the specified editing session.
	 * @param editingId the editing session identifier
	 * @return the associated document handler
	 * @throws WTException if the manager is not ready or the editing session does not exist
	 */
	public BaseDocEditorDocumentHandler getDocumentHandler(final String editingId) throws WTException {
		Check.notEmpty(editingId, "editingId");
		
		ensureStateReady();
		editingLocks.lock(editingId);
		try {
			EditingEntry entry = entriesByEditingId.get(editingId);
			if (entry == null) throw new WTNotFoundException("Editing-session not found [{}]", editingId);
			return entry.handler;

		} finally {
			editingLocks.unlock(editingId);
		}
	}
	
	/**
	 * Removes a single editing session from the manager and updates all related indexes.
	 * This method only removes the specified editing ID and preserves any other
	 * editing sessions still associated with the same WebTop session.
	 * @param editingId the editing session identifier
	 * @throws WTException if the manager is not ready
	 */
	public void clearEditing(final String editingId) throws WTException {
		Check.notEmpty(editingId, "editingId");
		ensureStateReady();
		
		LOGGER.debug("Clear editing-session [{}]", editingId);
		editingLocks.lock(editingId);
		try {
			LOGGER.debug("Unregistering editing-entry... [{}]", editingId);
			EditingEntry entry = entriesByEditingId.remove(editingId);
			if (entry != null) {
				LOGGER.debug("Removing HTTP session mapping [{}, {}]", editingId, entry.sessionId);
				synchronized (indexLock) {
					editingIdsBySessionId.removeMapping(entry.sessionId, editingId);
					// do not remove expirationCandidates here
					/*
					if (!editingIdsBySessionId.containsKey(entry.sessionId)) {
						expirationCandidatesBySessionId.remove(entry.sessionId);
					}
					*/
				}
			}
			
		} finally {
			editingLocks.unlock(editingId);
		}
	}
	
	/**
	 * Removes all sessions whose delayed cleanup TTL has expired.
	 * This method first collects expired session IDs under the index lock and then
	 * performs the actual removal outside that lock.
	 * @param now 
	 */
	private void internalCleanupExpiredSessions(final long now) {
		final Set<String> foundExpiredSessionIds = new LinkedHashSet<>();
		LOGGER.debug("Cleaning expired HTTP sessions...");
		synchronized (indexLock) {
			for (Map.Entry<String, Long> entry : expirationQuarantine.entrySet()) {
				if (now >= (entry.getValue() + expirationTTL)) {
					LOGGER.debug("HTTP session '{}' is expired", entry.getKey());
					foundExpiredSessionIds.add(entry.getKey());
				}
			}
			for (String sessionId : foundExpiredSessionIds) {
				LOGGER.debug("Removing HTTP session '{}' from expiration quarantine", sessionId);
				expirationQuarantine.remove(sessionId);
			}
		}
		for (String sessionId : foundExpiredSessionIds) {
			LOGGER.debug("Expiring entries for HTTP session '{}'...", sessionId);
			internalRemoveBySession(sessionId);
		}
	}
	
	/**
	 * Removes all editing sessions associated with the specified WebTop session.
	 * The related editing IDs are locked in deterministic order to reduce the risk
	 * of lock-ordering issues during bulk cleanup.
	 * @param sessionId 
	 */
	private void internalRemoveBySession(String sessionId) {
		final List<String> editingIds;
		synchronized (indexLock) {
			Collection<String> values = editingIdsBySessionId.get(sessionId);
			if (values == null || values.isEmpty()) return;
			editingIds = new ArrayList<>(values);
			editingIdsBySessionId.remove(sessionId);
			expirationQuarantine.remove(sessionId);
		}
		
		Collections.sort(editingIds);
		for (String editingId : editingIds) {
			editingLocks.lock(editingId);
		}
		try {
			for (String editingId : editingIds) {
				LOGGER.debug("Cleaning editing-entry [{}]", editingId);
				entriesByEditingId.remove(editingId);
			}
		} finally {
			for (int i = editingIds.size() -1; i >= 0; --i) {
				editingLocks.unlock(editingIds.get(i));
			}
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
	
	private static final class EditingEntry {
		private final String sessionId;
		private final BaseDocEditorDocumentHandler handler;
		private final EditingParams params;
		
		private EditingEntry(final String sessionId, final BaseDocEditorDocumentHandler handler, final EditingParams params) {
			this.sessionId = Check.notEmpty(sessionId, "sessionId");
			this.handler = Check.notNull(handler, "handler");
			this.params = Check.notNull(params, "params");
		}
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
		// isForm
		public String key;
		// referenceData
		public String title;
		public String url;
		
		public final Permissions permissions = new Permissions();
		
		// https://api.onlyoffice.com/docs/docs-api/usage-api/config/document/#referencedata
		public static class ReferenceData {
			public String fileKey;
			public String instanceId;
		}
		
		// https://api.onlyoffice.com/docs/docs-api/usage-api/config/document/permissions/
		public static class Permissions {
			public boolean chat = false;
			public boolean comment = false;
			// commentGroups
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
			// reviewGroups
			// userInfoGroups
			
			public Permissions withAllowEditing() {
				this.comment = true;
				this.edit = true;
				this.protect = true;
				this.review = true;
				return this;
			}
		}
	}
	
	/**
	 * https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/
	 */
	public static class OOEditorConfig {
		// actionLink
		public String callbackUrl;
		// coEditing 
		// createUrl
		// lang
		// location
		public Mode mode = Mode.EDIT;
		// recent
		// region
		// templates
		// user
		
		// https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/#coediting
		public static class CoEditing {
			public Mode mode = Mode.FAST;
			public boolean change = true;
			
			public static enum Mode {
				@SerializedName("fast") FAST,
				@SerializedName("strict") STRICT
			}
		}
		
		// https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/#mode
		public static enum Mode {
			@SerializedName("edit") EDIT,
			@SerializedName("view") VIEW
		}
		
		// https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/#recent
		public static class Recent {
			public String folder;
			public String title;
			public String url;
		}
		
		// https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/#templates
		public static class Template {
			public String image;
			public String title;
			public String url;
		}
		
		// https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/#user
		public static class User {
			public String group;
			public String id;
			public String image;
			public String name;
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
