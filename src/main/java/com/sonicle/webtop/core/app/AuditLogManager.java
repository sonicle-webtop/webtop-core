/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.net.IPUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.ipstack.IPLookupResponse;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.TplHelper;
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.app.util.EmailNotification;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.bol.OAuditLog;
import com.sonicle.webtop.core.bol.OIPGeoCache;
import com.sonicle.webtop.core.dal.AuditKnownDeviceDAO;
import com.sonicle.webtop.core.dal.AuditLogDAO;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.IPGeoCacheDao;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import freemarker.template.TemplateException;
import inet.ipaddr.IPAddress;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.sf.qualitycheck.Check;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class AuditLogManager extends AbstractAppManager<AuditLogManager> {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(AuditLogManager.class);
	private static final Logger AUTH_LOGGER = (Logger)LoggerFactory.getLogger("com.sonicle.webtop.AuthLog");
	
	private final Cache<String, IPLookupResponse> ipLookupRespCache = Caffeine.newBuilder()
		.expireAfterWrite(10, TimeUnit.MINUTES)
		.maximumSize(100)
		.build();
	private final KeyedReentrantLocks<String> lockKnownDevice = new KeyedReentrantLocks<>();
	
	AuditLogManager(WebTopApp wta) {
		super(wta);
		initialized();
	}
	
	@Override
	protected Logger internalGetLogger() {
		return LOGGER;
	}
	
	@Override
	protected void internalAppManagerCleanup() {
		ipLookupRespCache.cleanUp();
	}
	
	public static void logAuth(LogbackHelper.Level level, String clientIp, String sessionId, UserProfileId profileId, String action) {
		final String pattern = "[{}][webtop:core] {}: client={} profile={} action={}";
		if (LogbackHelper.Level.TRACE.equals(level)) {
			AUTH_LOGGER.trace(pattern, WebTopApp.getWebappName(), sessionId, clientIp, profileId, action);
		} else if (LogbackHelper.Level.DEBUG.equals(level)) {
			AUTH_LOGGER.debug(pattern, WebTopApp.getWebappName(), sessionId, clientIp, profileId, action);
		} else if (LogbackHelper.Level.INFO.equals(level)) {
			AUTH_LOGGER.info(pattern, WebTopApp.getWebappName(), sessionId, clientIp, profileId, action);
		} else if (LogbackHelper.Level.WARN.equals(level)) {
			AUTH_LOGGER.warn(pattern, WebTopApp.getWebappName(), sessionId, clientIp, profileId, action);
		} else if (LogbackHelper.Level.ERROR.equals(level)) {
			AUTH_LOGGER.error(pattern, WebTopApp.getWebappName(), sessionId, clientIp, profileId, action);
		}
	}
	
	public <C extends Enum<C>, A extends Enum<A>> void write(final UserProfileId profileId, final String sessionId, final String serviceId, final C context, final A action, final Collection<AuditReferenceDataEntry> entries) {
		write(profileId, sessionId, serviceId, EnumUtils.getValue(context), EnumUtils.getValue(action), entries);
	}
	
	public void write(final UserProfileId profileId, final String sessionId, final String serviceId, final String context, final String action, final Collection<AuditReferenceDataEntry> entries) {
		Check.notNull(profileId, "profileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notEmpty(action, "action");
		boolean isImpersonated = RunContext.isImpersonated();
		
		try {
			readyLock();
			try {
				if (isImpersonated && !logImpersonatedEnabled(profileId)) return;
				WT.runPrivileged(() -> {
					try {
						internalWriteSync(profileId.getDomain(), buildAuditUserId(profileId, isImpersonated), sessionId, serviceId, context, action, entries);
					} catch (WTException ex) {
						LOGGER.error("Unable to write entries", ex);
					}
				});
			} finally {
				readyUnlock();
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
	}
	
	public <C extends Enum<C>, A extends Enum<A>> void write(final UserProfileId profileId, final String sessionId, final String serviceId, final C context, final A action, final Object reference, final Object data) {
		write(profileId, sessionId, serviceId, EnumUtils.getValue(context), EnumUtils.getValue(action), reference, data);
	}
	
	public void write(final UserProfileId profileId, final String sessionId, final String serviceId, final String context, final String action, final Object reference, final Object data) {
		Check.notNull(profileId, "profileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notEmpty(action, "action");
		boolean isImpersonated = RunContext.isImpersonated();
		
		try {
			readyLock();
			try {
				if (isImpersonated && !logImpersonatedEnabled(profileId)) return;
				WT.runPrivileged(() -> {
					try {
						internalWriteSync(profileId.getDomain(), buildAuditUserId(profileId, isImpersonated), sessionId, serviceId, context, action, reference, data);
					} catch (WTException ex) {
						LOGGER.error("Unable to write entries", ex);
					}
				});
			} finally {
				readyUnlock();
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
	}
	
	public <C extends Enum<C>, A extends Enum<A>> Batch getBatch(final UserProfileId profileId, final String sessionId, final String serviceId, final C context, final A action) {
		return getBatch(profileId, sessionId, serviceId, EnumUtils.getValue(context), EnumUtils.getValue(action));
	}
	
	/**
	 * Returns the Batch interface on which issue log-entry writes in batch-mode.
	 * Entries will be trasparently managed/saved in groups of 100 items maximizing 
	 * performances when you have a huge amount of entries.
	 * @param profileId The UserProfileId.
	 * @param sessionId The session reference. Optional.
	 * @param serviceId The Service ID.
	 * @param context
	 * @param action
	 * @return 
	 */
	public Batch getBatch(final UserProfileId profileId, final String sessionId, final String serviceId, final String context, final String action) {
		Check.notNull(profileId, "profileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notEmpty(action, "action");
		boolean isImpersonated = RunContext.isImpersonated();
		
		try {
			readyLock();
			try {
				if (isImpersonated && !logImpersonatedEnabled(profileId)) {
					return null;
				} else {
					return new Batch(this, profileId.getDomain(), buildAuditUserId(profileId, isImpersonated), sessionId, serviceId, context, action, 50);
				}
			} finally {
				readyUnlock();
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
		return null;
	}
	
	public <C extends Enum<C>> void rebaseReference(final UserProfileId profileId, final String serviceId, final C context, final Object oldReference, final Object newReference) {
		rebaseReference(profileId, serviceId, EnumUtils.getValue(context), oldReference, newReference);
	}
	
	public void rebaseReference(final UserProfileId profileId, final String serviceId, final String context, final Object oldReference, final Object newReference) {
		Check.notNull(profileId, "profileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notNull(oldReference, "oldReference");
		Check.notNull(newReference, "newReference");
		boolean isImpersonated = RunContext.isImpersonated();
		
		try {
			readyLock();
			try {
				if (isImpersonated && !logImpersonatedEnabled(profileId)) return;
				WT.runPrivileged(() -> {
					try {
						internalUpdateReference(profileId, serviceId, context, oldReference, newReference);
					} catch (WTException ex) {
						LOGGER.error("Unable to write entries", ex);
					}
				});
			} finally {
				readyUnlock();
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
	}
	
	public boolean isKnownDeviceVerificationEnabled(final UserProfileId profileId) {
		return isKnownDeviceVerificationEnabled(profileId.getDomainId());
	}
	
	public boolean isKnownDeviceVerificationEnabled(final String domainId) {
		CoreServiceSettings css = new CoreServiceSettings(getWebTopApp().getSettingsManager(), CoreManifest.ID, domainId);
		return css.getSecurityKnownDeviceVerificationEnabled();
	}
	
	public List<String> getKnownDeviceVerificationNetWhiletist(final UserProfileId profileId) {
		return getKnownDeviceVerificationNetWhiletist(profileId.getDomainId());
	}
	
	public List<String> getKnownDeviceVerificationNetWhiletist(final String domainId) {
		CoreServiceSettings css = new CoreServiceSettings(getWebTopApp().getSettingsManager(), CoreManifest.ID, domainId);
		return css.getSecurityKnownDeviceVerificationNetWhiletist();
	}
	
	public void sendDeviceVerificationNotice(final UserProfileId profileId, final boolean profileIsImpersonated, final String deviceId, final String userAgent, final IPAddress remoteIpAddress) throws WTException {
		Check.notNull(profileId, "profileId");
		Check.notNull(deviceId, "deviceId");
		Check.notNull(userAgent, "userAgent");
		Check.notNull(remoteIpAddress, "remoteIpAddress");
		
		readyLock();
		try {
			CoreServiceSettings css = new CoreServiceSettings(getWebTopApp().getSettingsManager(), CoreManifest.ID, profileId.getDomainId());

			try {
				UserProfile.Data ud = WT.getUserData(profileId);
				if (ud == null) throw new WTException("User-data not found [{}]", profileId);

				String remoteIp = remoteIpAddress.toAddressString().toString();
				ReadableUserAgent rua = WebTopApp.getUserAgentInfo(userAgent);
				IPLookupResponse ipData = null;
				if (IPUtils.isPublicAddress(remoteIpAddress)) {
					ipData = getIPGeolocationData(profileId.getDomainId(), remoteIp);
				}

				String subject = EmailNotification.buildSubject(ud.getLocale(), CoreManifest.ID, WT.lookupResource(CoreManifest.ID, ud.getLocale(), "tpl.email.newDevice.subject"));
				String customBodyHtml = TplHelper.buildNewDeviceNoticeBody(ud.getProfileEmailAddress(), DateTimeUtils.now(), rua, remoteIp, ipData, ud.getLocale(), ud.getTimeZone(), ud.getShortDateFormat(), ud.getShortTimeFormat());
				String html = new EmailNotification.NoReplyBuilder()
					.withCustomBody(null, customBodyHtml)
					.build(ud.getLocale(), EmailNotification.buildSource(ud.getLocale(), CoreManifest.ID)).write();

				InternetAddress from = WT.getNoReplyAddress(profileId.getDomainId());
				if (from == null) throw new WTException("Error getting no-reply address for '{}'", profileId.getDomainId());

				ArrayList<InternetAddress> ccns = new ArrayList<>();
				ArrayList<InternetAddress> tos = new ArrayList<>();
				// Do not include additional recipients (defined in settings) when 
				// the target profile is sysAdmin or during impersonation. This helps
				// to keep connections private during some *special* activities.
				if (!WebTopManager.isSysAdmin(profileId) && !profileIsImpersonated) {
					tos.add(ud.getPersonalEmail());
					//TODO: evaluate how to treat domain admin when will be implemented!
					for (String email : css.getSecurityKnownDeviceVerificationRecipients()) {
						InternetAddress ia = InternetAddressUtils.toInternetAddress(email);
						if (ia != null) ccns.add(ia);
					}
				} else {
					if (profileIsImpersonated) {
						UserProfile.Data aud = WT.getUserData(WebTopManager.sysAdminProfileId());
						if (aud == null) throw new WTException("User-data not found [{}]", WebTopManager.sysAdminProfileId());
						tos.add(aud.getPersonalEmail());
					} else {
						tos.add(ud.getPersonalEmail());
					}
				}

				WT.sendEmail(WT.getGlobalMailSession(profileId), true, from, tos, null, ccns, subject, html, null);

			} catch (IOException | TemplateException ex) {
				LOGGER.error("Unable to build email template", ex);
			} catch (MessagingException ex) {
				LOGGER.error("Unable to send email", ex);
			}
		} finally {
			readyUnlock();
		}
	}
	
	public void addKnownDevice(final UserProfileId profileId, final String deviceId) throws WTException {
		Check.notNull(profileId, "profileId");
		Check.notNull(deviceId, "deviceId");
		
		readyLock();
		try {
			AuditKnownDeviceDAO akdDao = AuditKnownDeviceDAO.getInstance();
			Connection con = null;

			try {
				con = WT.getCoreConnection();
				akdDao.insert(con, profileId.getDomainId(), profileId.getUserId(), deviceId, BaseDAO.createRevisionTimestamp());

			} catch (Throwable t) {
				throw ExceptionUtils.wrapThrowable(t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} finally {
			readyUnlock();
		}
	}
	
	public boolean testAndSaveKnownDevice(final UserProfileId profileId, final String deviceId) throws WTException {
		Check.notNull(profileId, "profileId");
		Check.notNull(deviceId, "deviceId");
		
		readyLock();
		try {
			final String key = profileId.toString() + deviceId;
			try {
				lockKnownDevice.tryLock(key, 60, TimeUnit.SECONDS);

				AuditKnownDeviceDAO akdDao = AuditKnownDeviceDAO.getInstance();
				Connection con = null;

				try {
					con = WT.getCoreConnection();
					int ret = akdDao.updateLastSeenByProfileDevice(con, profileId.getDomainId(), profileId.getUserId(), deviceId, BaseDAO.createRevisionTimestamp());
					if (ret == 0) { // Device is not present yet: adds it...
						akdDao.insert(con, profileId.getDomainId(), profileId.getUserId(), deviceId, BaseDAO.createRevisionTimestamp());
					}
					return ret == 1;

				} catch (Throwable t) {
					throw ExceptionUtils.wrapThrowable(t);
				} finally {
					DbUtils.closeQuietly(con);
				}

			} catch (InterruptedException ex) {
				return false;
			} finally {
				lockKnownDevice.unlock(key);
			}
		} finally {
			readyUnlock();
		}
	}
	
	public IPLookupResponse getIPGeolocationData(final String domainId, final String ipAddress) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(ipAddress, "ipAddress");
		
		readyLock();
		try {
			return internalGetIPGeolocationData(domainId, ipAddress);
		} finally {
			readyUnlock();
		}
	}
	
	private void batchWrite(final String domainId, final String userId, final String sessionId, final String serviceId, final String context, final String action, final Collection<AuditReferenceDataEntry> entries) {
		try {
			readyLock();
			try {
				WT.runPrivileged(() -> {
					try {
						internalWriteSync(domainId, userId, sessionId, serviceId, context, action, entries);
					} catch (WTException ex) {
						LOGGER.error("Unable to write entries", ex);
					}
				});
			} finally {
				readyUnlock();
			}
		} catch (WTException ex1) {
			LOGGER.trace("Not ready", ex1);
		}
	}
	
	private int internalWriteSync(final String domainId, final String userId, final String sessionId, final String serviceId, final String context, final String action, final Object reference, final Object data) throws WTException {
		AuditLogDAO audDao = AuditLogDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			OAuditLog oal = new OAuditLog();
			oal.setTimestamp(BaseDAO.createRevisionTimestamp());
			oal.setDomainId(domainId);
			oal.setUserId(userId);
			oal.setSessionId(sessionId);
			oal.setServiceId(serviceId);
			oal.setContext(context);
			oal.setAction(action);
			oal.setReferenceId(reference != null ? String.valueOf(reference) : null);
			oal.setData(data != null ? String.valueOf(data) : null);
			return audDao.insert(con, oal);

		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private int[] internalWriteSync(final String domainId, final String userId, final String sessionId, final String serviceId, final String context, final String action, final Collection<AuditReferenceDataEntry> entries) throws WTException {
		AuditLogDAO audDao = AuditLogDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			OAuditLog oalBase = new OAuditLog();
			oalBase.setTimestamp(BaseDAO.createRevisionTimestamp());
			oalBase.setDomainId(domainId);
			oalBase.setUserId(userId);
			oalBase.setServiceId(serviceId);
			oalBase.setContext(context);
			oalBase.setAction(action);
			oalBase.setSessionId(sessionId);
			return audDao.batchInsert(con, oalBase, entries);

		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void internalUpdateReference(final UserProfileId profileId, final String serviceId, final String context, final Object oldReference, final Object newReference) throws WTException {
		AuditLogDAO audDao = AuditLogDAO.getInstance();
		Connection con = null;

		try {
			con = WT.getCoreConnection();
			audDao.updateReferences(con, serviceId, profileId.getDomain(), context, String.valueOf(oldReference), String.valueOf(newReference));
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String buildAuditUserId(UserProfileId profileId, boolean impersonated) {
		return impersonated ? WebTopManager.SYSADMIN_USERID + "!" + profileId.getUserId() : profileId.getUserId();
	}
	
	private boolean logImpersonatedEnabled(UserProfileId profileId) {
		CoreServiceSettings css = new CoreServiceSettings(getWebTopApp().getSettingsManager(), CoreManifest.ID, profileId.getDomainId());
		return css.isAuditLogImpersonated();
	}
	
	public static class Batch {
		private final AuditLogManager auditLogManager;
		private final String domainId;
		private final String userId;
		private final String sessionId;
		private final String serviceId;
		private final String context;
		private final String action;
		private final int batchSize;
		private LinkedList<AuditReferenceDataEntry> buffer;
		private final Object lock = new Object();
		
		private Batch(AuditLogManager auditLogManager, String domainId, String userId, String sessionId, String serviceId, String context, String action, int batchSize) {
			this.auditLogManager = Check.notNull(auditLogManager, "auditLogManager");
			this.domainId = Check.notEmpty(domainId, "domainId");
			this.userId = Check.notEmpty(userId, "userId");
			this.sessionId = sessionId;
			this.serviceId = Check.notEmpty(serviceId, "serviceId");
			this.context = Check.notEmpty(context, "context");
			this.action = Check.notEmpty(action, "action");
			this.batchSize = batchSize;
			this.buffer = new LinkedList<>();
		}
		
		public void write(final Object reference, final Object data) {
			write(Arrays.asList(new MyARDEntry(reference, data)));
		}
		
		public void write(final String reference, final String data) {
			write(Arrays.asList(new MyARDEntry(reference, data)));
		}
		
		public void write(final AuditReferenceDataEntry entry) {
			write(Arrays.asList(Check.notNull(entry, "entry")));
		}
		
		public void write(final Collection<AuditReferenceDataEntry> entries) {
			Check.notNull(entries, "entries");
			synchronized (lock) {
				// Add entries to the buffer
				buffer.addAll(entries);
				int size = buffer.size();
				if (size >= batchSize) { // If buffer size reached the minimum batch-size, write entries...
					int i;
					// Writes entries divided into groups of batch-size
					for (i = 0; i < (size / batchSize); i++) {
						int start = i * batchSize;		
						auditLogManager.batchWrite(domainId, userId, sessionId, serviceId, context, action, buffer.subList(start, start + batchSize));
					}
					// Initializes new buffer with remaining entries (if any)
					int remaining = size % batchSize;
					if (remaining > 0) {
						this.buffer = new LinkedList<>(buffer.subList(i * batchSize, (i * batchSize) + remaining));
					} else {
						this.buffer = new LinkedList<>();
					}
				}
			}
		}
		
		public void flush() {
			synchronized (lock) {
				// The buffer here can contain only a number of items lower than batch-size
				if (!buffer.isEmpty()) {
					// Writes the whole buffer and re-init it...
					auditLogManager.batchWrite(domainId, userId, sessionId, serviceId, context, action, buffer);
					this.buffer = new LinkedList<>();
				}
			}
		}
	}
	
	private static class MyARDEntry implements AuditReferenceDataEntry {
		private final String reference;
		private final String data;
		
		public MyARDEntry(Object reference, Object data) {
			this.reference = reference != null ? String.valueOf(reference) : null;
			this.data = data != null ? String.valueOf(data) : null;
		}
		
		@Override
		public String getReference() {
			return reference;
		}

		@Override
		public String getData() {
			return data;
		}
	}
	
	private IPLookupResponse internalGetIPGeolocationData(final String domainId, final String ipAddress) {
		return ipLookupRespCache.get(ipAddress, k -> doLoadIPLookupResponse(domainId, k));
	}
	
	private IPLookupResponse doLoadIPLookupResponse(String domainId, String ipAddress) {
		OIPGeoCache oigr = ipGeoCacheGet(ipAddress);
		IPLookupResponse resp = null;
		if (oigr == null) {
			// Lookup info from provider
			resp = lookupSingleIPInfo(domainId, ipAddress);
			if (resp != null) {
				// Persists data for later use
				OIPGeoCache noigr = new OIPGeoCache();
				noigr.setIpAddress(ipAddress);
				if (setIPGeoResultData(noigr, CoreSettings.GeolocationProvider.IPSTACK, resp)) {
					ipGeoCacheInsert(noigr);
				}
			}
		} else {
			// Extract data from saved result
			resp = getIPGeoResultData(oigr);
		}
		return resp;
	}
	
	private IPLookupResponse lookupSingleIPInfo(String domainId, String ipAddress) {
		List<IPLookupResponse> values = null;
		try {
			values = lookupIPInfo(domainId, Arrays.asList(ipAddress));
		} catch(IOException ex) {}
		return (values != null && !values.isEmpty()) ? values.get(0) : null;
	}
	
	private List<IPLookupResponse> lookupIPInfo(final String domainId, final Collection<String> ipAddresses) throws IOException {
		HttpClient httpCli = null;
		try {
			IPLookupProviderConfig config = getIPLookupProviderConfig(domainId);
			if (config.provider == null) return null;
			// Do not check provider here, ipstack it's the only one supported for now!
			
			httpCli = HttpClientBuilder.create().build();
			//https://ipregistry.co/
			//https://rapidapi.com/blog/ip-geolocation-api/
			URI uri = new URIBuilder("http://api.ipstack.com/" + StringUtils.join(ipAddresses, ","))
				.addParameter("access_key", config.apiKey)
				.addParameter("output", "json")
				.addParameter("fields", "main,location.country_flag")
				.build();
			
			String json = HttpClientUtils.getStringContent(httpCli, uri);
			if (ipAddresses.size() > 1) {
				return JsonResult.gson().fromJson(json, IPLookupResponse.List.class);
			} else {
				return Arrays.asList(JsonResult.gson().fromJson(json, IPLookupResponse.class));
			}
			
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException(ex);
		} finally {
			HttpClientUtils.closeQuietly(httpCli);
		}
	}
	
	private OIPGeoCache ipGeoCacheGet(String ipAddress) {
		IPGeoCacheDao igrDao = IPGeoCacheDao.getInstance();
		Connection con = null;
		
		try {
			con = getWebTopApp().getConnectionManager().getConnection();
			return igrDao.selectLastByIP(con, ipAddress);
			
		} catch(Throwable t) {
			LOGGER.error("DB error", t);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean ipGeoCacheInsert(OIPGeoCache oigr) {
		IPGeoCacheDao igrDao = IPGeoCacheDao.getInstance();
		Connection con = null;
		
		try {
			con = getWebTopApp().getConnectionManager().getConnection();
			return igrDao.insert(con, oigr, BaseDAO.createRevisionTimestamp()) == 1;
			
		} catch(Throwable t) {
			LOGGER.error("DB error", t);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean setIPGeoResultData(OIPGeoCache result, CoreSettings.GeolocationProvider provider, IPLookupResponse response) {
		try {
			if (CoreSettings.GeolocationProvider.IPSTACK.equals(provider)) {
				result.setData(JsonResult.gson(false).toJson(response));
				result.setProvider(EnumUtils.toSerializedName(provider));
				return true;
			}
		} catch(Throwable t) {
			LOGGER.error("Unable to encode IP geo provider data [{}]", EnumUtils.toSerializedName(provider), t);
		}
		return false;
	}
	
	private IPLookupResponse getIPGeoResultData(OIPGeoCache result) {
		CoreSettings.GeolocationProvider provider = EnumUtils.forSerializedName(result.getProvider(), CoreSettings.GeolocationProvider.class);
		try {
			if (CoreSettings.GeolocationProvider.IPSTACK.equals(provider)) {
				return JsonResult.gson().fromJson(result.getData(), IPLookupResponse.class);
			}
		} catch(Throwable t) {
			LOGGER.error("Unable to decode IP geo provider data [{}]", EnumUtils.toSerializedName(provider), t);
		}
		return null;
	}
	
	private IPLookupProviderConfig getIPLookupProviderConfig(String domainId) {
		CoreServiceSettings css = new CoreServiceSettings(getWebTopApp().getSettingsManager(), CoreManifest.ID, domainId);
		CoreSettings.GeolocationProvider provider = css.getGeolocationProvider();
		String apiKey = null;
			if (CoreSettings.GeolocationProvider.IPSTACK.equals(provider)) {
				apiKey = css.getIpstackGeolocationProviderApiKey();
			}
			return new IPLookupProviderConfig(EnumUtils.toSerializedName(provider), apiKey);
	}
	
	private class IPLookupProviderConfig {
		public final String provider;
		public final String apiKey;
		
		public IPLookupProviderConfig(String provider, String apiKey) {
			this.provider = provider;
			this.apiKey = apiKey;
		}
	}
}
