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
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.IPGeoCacheDao;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import freemarker.template.TemplateException;
import inet.ipaddr.IPAddress;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
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
public class AuditLogManager {
	private static final Logger logger = WT.getLogger(AuditLogManager.class);
	private static final Logger AUTH_LOGGER = (Logger)LoggerFactory.getLogger("com.sonicle.webtop.AuthLog");
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized AuditLogManager initialize(WebTopApp wta) {
		if (initialized) throw new RuntimeException("Initialization already done");
		AuditLogManager logm = new AuditLogManager(wta);
		initialized = true;
		logger.info("Initialized");
		return logm;
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
	
	private WebTopApp wta = null;
	private final Cache<String, IPLookupResponse> ipLookupRespCache = Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();
	private final KeyedReentrantLocks lockKnownDevice = new KeyedReentrantLocks<String>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private AuditLogManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		wta = null;
		logger.info("Cleaned up");
	}
	
	public void write(UserProfileId profileId, String sessionId, String serviceId, String context, String action, String referenceId, String data) {
		if (!initialized) return;
		CoreServiceSettings css = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, profileId.getDomainId());
		boolean impersonated = RunContext.isImpersonated();
		
		if (impersonated && !css.isAuditLogImpersonated()) return;
		WT.runPrivileged(() -> {
			AuditLogDAO dao = AuditLogDAO.getInstance();
			Connection con = null;

			try {
				con = WT.getCoreConnection();

				OAuditLog item = new OAuditLog();
				item.setTimestamp(BaseDAO.createRevisionTimestamp());
				item.setDomainId(profileId.getDomain());
				item.setUserId((impersonated?WebTopManager.SYSADMIN_USERID + "!":"") + profileId.getUserId());
				item.setServiceId(serviceId);
				item.setContext(context);
				item.setAction(action);
				item.setReferenceId(referenceId);
				item.setSessionId(sessionId);
				item.setData(data);

				dao.insert(con, item);

			} catch(SQLException | DAOException ex) {
				logger.error("DB error", ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		});
	}
	
	public void write(UserProfileId profileId, String sessionId, String serviceId, String context, String action, Collection<AuditReferenceDataEntry> entries) {
		if (!initialized) return;
		CoreServiceSettings css = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, profileId.getDomainId());
		boolean impersonated = RunContext.isImpersonated();
		
		if (impersonated && !css.isAuditLogImpersonated()) return;
		WT.runPrivileged(() -> {
			AuditLogDAO dao = AuditLogDAO.getInstance();
			Connection con = null;

			try {
				con = WT.getCoreConnection();

				OAuditLog baseItem = new OAuditLog();
				baseItem.setTimestamp(BaseDAO.createRevisionTimestamp());
				baseItem.setDomainId(profileId.getDomain());
				baseItem.setUserId((impersonated?WebTopManager.SYSADMIN_USERID + "!":"") + profileId.getUserId());
				baseItem.setServiceId(serviceId);
				baseItem.setContext(context);
				baseItem.setAction(action);
				baseItem.setSessionId(sessionId);

				dao.batchInsert(con, baseItem, entries);

			} catch(SQLException | DAOException ex) {
				logger.error("DB error", ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		});
	}
	
	public boolean isKnownDeviceVerificationEnabled(final UserProfileId profileId) {
		return isKnownDeviceVerificationEnabled(profileId.getDomainId());
	}
	
	public boolean isKnownDeviceVerificationEnabled(final String domainId) {
		CoreServiceSettings css = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, domainId);
		return css.getSecurityKnownDeviceVerificationEnabled();
	}
	
	public List<String> getKnownDeviceVerificationNetWhiletist(final UserProfileId profileId) {
		return getKnownDeviceVerificationNetWhiletist(profileId.getDomainId());
	}
	
	public List<String> getKnownDeviceVerificationNetWhiletist(final String domainId) {
		CoreServiceSettings css = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, domainId);
		return css.getSecurityKnownDeviceVerificationNetWhiletist();
	}
	
	public void sendDeviceVerificationNotice(final UserProfileId profileId, final boolean profileIsImpersonated, final String deviceId, final String userAgent, final IPAddress remoteIpAddress) throws WTException {
		if (!initialized) return;
		CoreServiceSettings css = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, profileId.getDomainId());
		
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
			
		} catch(IOException | TemplateException ex) {
			logger.error("Unable to build email template", ex);
		} catch(MessagingException ex) {
			logger.error("Unable to send email", ex);
		}
	}
	
	public void addKnownDevice(final UserProfileId profileId, final String deviceId) throws WTException {
		if (!initialized) return;
		
		AuditKnownDeviceDAO akdDao = AuditKnownDeviceDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			akdDao.insert(con, profileId.getDomainId(), profileId.getUserId(), deviceId, BaseDAO.createRevisionTimestamp());
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean testAndSaveKnownDevice(final UserProfileId profileId, final String deviceId) throws WTException {
		if (!initialized) return false;
		
		final String key = profileId.toString() + deviceId;
		try (KeyedReentrantLocks.KeyedLock lock = lockKnownDevice.tryAcquire(key, 60 * 1000)) {
			if (lock == null) return false;
			
			AuditKnownDeviceDAO akdDao = AuditKnownDeviceDAO.getInstance();
			Connection con = null;
			
			try {
				con = WT.getCoreConnection();
				int ret = akdDao.updateLastSeenByProfileDevice(con, profileId.getDomainId(), profileId.getUserId(), deviceId, BaseDAO.createRevisionTimestamp());
				if (ret == 0) { // Device is not present yet: adds it...
					akdDao.insert(con, profileId.getDomainId(), profileId.getUserId(), deviceId, BaseDAO.createRevisionTimestamp());
				}
				return ret == 1;
				
			} catch(Throwable t) {
				throw ExceptionUtils.wrapThrowable(t);
			} finally {
				DbUtils.closeQuietly(con);
			}	
		}	
	}
	
	public IPLookupResponse getIPGeolocationData(final String domainId, final String ipAddress) {
		return ipLookupRespCache.get(ipAddress, k -> doLoadIPLookupResponse(domainId, k));
	}
	
	public List<IPLookupResponse> lookupIPInfo(final String domainId, final Collection<String> ipAddresses) throws IOException {
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
	
	private OIPGeoCache ipGeoCacheGet(String ipAddress) {
		IPGeoCacheDao igrDao = IPGeoCacheDao.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return igrDao.selectLastByIP(con, ipAddress);
			
		} catch(Throwable t) {
			logger.error("DB error", t);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean ipGeoCacheInsert(OIPGeoCache oigr) {
		IPGeoCacheDao igrDao = IPGeoCacheDao.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return igrDao.insert(con, oigr, BaseDAO.createRevisionTimestamp()) == 1;
			
		} catch(Throwable t) {
			logger.error("DB error", t);
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
			logger.error("Unable to encode IP geo provider data [{}]", EnumUtils.toSerializedName(provider), t);
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
			logger.error("Unable to decode IP geo provider data [{}]", EnumUtils.toSerializedName(provider), t);
		}
		return null;
	}
	
	private IPLookupProviderConfig getIPLookupProviderConfig(String domainId) {
		CoreServiceSettings css = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, domainId);
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
