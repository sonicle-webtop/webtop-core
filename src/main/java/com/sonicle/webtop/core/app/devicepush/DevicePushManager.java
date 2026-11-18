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
package com.sonicle.webtop.core.app.devicepush;

import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.products.ConnectProduct;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mobile push seam (application-wide singleton), shared by every service.
 * <ul>
 * <li>Keeps the registry of the devices subscribed for each user. The registry
 * is IN-MEMORY: the external gateway owns the durable device state and replays
 * it after every (re)connection.</li>
 * <li>Keeps one link to the gateway for each domain holding a valid "WebTop
 * Connect" license, reconciled against the license state on a fixed cadence.</li>
 * <li>Delivers {@link DevicePushEvent}s produced by services to the devices of
 * the target user ({@link #push}).</li>
 * <li>Notifies {@link DevicePushListener}s, so services can hold per-user
 * machinery only while a device needs it.</li>
 * </ul>
 * A subscription is accepted only for a user of the link's own domain having
 * at least one active app session (non-revoked, non-expired refresh token):
 * the gateway is not trusted to name arbitrary users, and a device whose
 * session is gone has no title to receive the user's data. Subscribed users
 * are re-checked periodically and whenever their sessions are revoked.
 */
public class DevicePushManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DevicePushManager.class);
	private static final DevicePushManager INSTANCE = new DevicePushManager();

	public static DevicePushManager getInstance() {
		return INSTANCE;
	}

	private static final String PROP_ENABLED = "webtop.push-gateway.enabled";
	private static final String PROP_URL = "webtop.push-gateway.url";
	/** Accepts a non-TLS (ws://) gateway URL: development only. */
	private static final String PROP_ALLOW_INSECURE = "webtop.push-gateway.allow-insecure";
	/** Set to false to accept subscriptions of users with no active app session. */
	private static final String PROP_REQUIRE_AUTH_SESSION = "webtop.push-gateway.require-auth-session";

	// Matches WebTopApp.webappVersionCheck: no reason to react faster than the
	// LicenseManager cache refreshes. Cloud revocations arrive silently via a
	// daily job, so this poll is the only place they are noticed.
	private static final long RECONCILE_INTERVAL_MS = 60_000L;
	private static final int AUTH_SESSIONS_SWEEP_EVERY_TICKS = 5;

	private final ConcurrentHashMap<UserProfileId, Set<String>> subscriptions = new ConcurrentHashMap<>();
	private final CopyOnWriteArrayList<DevicePushListener> listeners = new CopyOnWriteArrayList<>();
	private final Object subscriptionsLock = new Object();
	private final Object linksLock = new Object();
	private final Map<String, NodejsDevicePushLink> links = new HashMap<>();
	private volatile Map<String, NodejsDevicePushLink> linksView = Collections.emptyMap();
	private ScheduledExecutorService poller = null;
	private boolean stopped = true;
	private int tick = 0;
	// Captured where Shiro's holder is populated: gateway read threads and the
	// poller are plain threads with no SecurityManager of their own.
	private volatile SecurityManager securityManager;

	private DevicePushManager() {}

	/**
	 * Starts the license reconcile loop. Must be called with a Subject bound
	 * (core BackgroundService initialization).
	 */
	public void start() {
		synchronized (linksLock) {
			if (poller != null) return;
			try {
				securityManager = SecurityUtils.getSecurityManager();
			} catch (Throwable t) {
				LOGGER.error("No SecurityManager available on calling thread: device subscriptions will fail", t);
			}
			stopped = false;
			poller = Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "devicePush-reconciler");
				t.setDaemon(true);
				return t;
			});
			// immediate first tick: a licensed instance connects without waiting a
			// whole cycle after startup
			poller.scheduleWithFixedDelay(RunContext.getSubject().associateWith(this::reconcile), 0, RECONCILE_INTERVAL_MS, TimeUnit.MILLISECONDS);
		}
	}

	public void stop() {
		final List<NodejsDevicePushLink> toShutdown;
		synchronized (linksLock) {
			stopped = true;
			if (poller != null) {
				poller.shutdownNow();
				poller = null;
			}
			toShutdown = new ArrayList<>(links.values());
			links.clear();
			linksView = Collections.emptyMap();
		}
		for (NodejsDevicePushLink link : toShutdown) {
			try { link.shutdown(); } catch (Throwable t) { LOGGER.warn("[{}] push link shutdown", link.getDomainId(), t); }
		}
	}

	/**
	 * Registers a listener and replays the already-known devices to it, so the
	 * registration order between services and gateway frames does not matter.
	 */
	public void addListener(final DevicePushListener listener) {
		synchronized (subscriptionsLock) {
			if (!listeners.addIfAbsent(listener)) return;
			for (Map.Entry<UserProfileId, Set<String>> entry : subscriptions.entrySet()) {
				final UserProfileId profileId = entry.getKey();
				for (String deviceId : new ArrayList<>(entry.getValue())) {
					runAs(profileId, () -> notifyAdded(listener, profileId, deviceId));
				}
			}
		}
	}

	public void removeListener(final DevicePushListener listener) {
		listeners.remove(listener);
	}

	public boolean hasDevices(final UserProfileId profileId) {
		final Set<String> deviceIds = subscriptions.get(profileId);
		return deviceIds != null && !deviceIds.isEmpty();
	}

	public Set<String> getDeviceIds(final UserProfileId profileId) {
		final Set<String> deviceIds = subscriptions.get(profileId);
		return (deviceIds == null) ? Collections.emptySet() : new LinkedHashSet<>(deviceIds);
	}

	/**
	 * Delivers an event to the devices of a user. A no-op when the user has no
	 * devices or its domain has no gateway link. Never blocks, never throws.
	 */
	public void push(final UserProfileId profileId, final DevicePushEvent event) {
		try {
			final Set<String> deviceIds = getDeviceIds(profileId);
			if (deviceIds.isEmpty()) return;
			final NodejsDevicePushLink link = linksView.get(profileId.getDomainId());
			if (link == null) {
				LOGGER.debug("[{}] no push link for domain: {} event skipped", profileId, event.getType());
				return;
			}
			link.push(profileId, deviceIds, event);
		} catch (Throwable t) {
			LOGGER.error("[{}] push failed", profileId, t);
		}
	}

	/**
	 * Registers a device for a user and notifies the listeners. Idempotent per
	 * (user, deviceId). Must be called with the target user's Subject bound.
	 */
	public void subscribe(final UserProfileId profileId, final String deviceId) {
		synchronized (subscriptionsLock) {
			subscriptions.computeIfAbsent(profileId, k -> ConcurrentHashMap.newKeySet()).add(deviceId);
			// listeners are notified also for a known device: re-subscribes are
			// their chance to heal lost state
			for (DevicePushListener listener : listeners) notifyAdded(listener, profileId, deviceId);
			LOGGER.trace("[{}] push device '{}' subscribed", profileId, deviceId);
		}
	}

	/**
	 * Unregisters a device and notifies the listeners. Safe for a device that
	 * was never (or no longer is) subscribed.
	 */
	public void unsubscribe(final UserProfileId profileId, final String deviceId) {
		synchronized (subscriptionsLock) {
			boolean lastDevice = false;
			final Set<String> deviceIds = subscriptions.get(profileId);
			if (deviceIds != null) {
				deviceIds.remove(deviceId);
				if (deviceIds.isEmpty()) {
					subscriptions.remove(profileId);
					lastDevice = true;
				}
			}
			for (DevicePushListener listener : listeners) {
				try {
					listener.onDeviceRemoved(profileId, deviceId, lastDevice);
				} catch (Throwable t) {
					LOGGER.error("[{}] push listener error on device removal", profileId, t);
				}
			}
			LOGGER.trace("[{}] push device '{}' unsubscribed", profileId, deviceId);
		}
	}

	private void notifyAdded(DevicePushListener listener, UserProfileId profileId, String deviceId) {
		try {
			listener.onDeviceAdded(profileId, deviceId);
		} catch (Throwable t) {
			LOGGER.error("[{}] push listener error on device addition", profileId, t);
		}
	}

	/**
	 * Entry point for the commands issued by the gateway on a link.
	 */
	void onGatewaySubscribe(final NodejsDevicePushLink link, final UserProfileId profileId, final String deviceId, final boolean isSubscribe) {
		if (!link.getDomainId().equals(profileId.getDomainId())) {
			LOGGER.warn("[{}] {} for a user of another domain refused [{}]", link.getDomainId(), isSubscribe ? "subscribe" : "unsubscribe", profileId);
			return;
		}
		if (isSubscribe && isAuthSessionRequired() && !hasActiveAuthSession(profileId)) {
			LOGGER.info("[{}] push subscribe refused: no active app session (device '{}')", profileId, deviceId);
			return;
		}
		runAs(profileId, () -> {
			if (isSubscribe) subscribe(profileId, deviceId);
			else unsubscribe(profileId, deviceId);
		});
	}

	/**
	 * To be called after app sessions of a user have been revoked: when none
	 * is left active, the user's devices stop receiving its data right away.
	 */
	public void onAuthSessionsRevoked(final UserProfileId profileId) {
		try {
			if (!hasDevices(profileId) || !isAuthSessionRequired()) return;
			if (!hasActiveAuthSession(profileId)) dropProfile(profileId, "app sessions revoked");
		} catch (Throwable t) {
			LOGGER.error("[{}] push check after session revocation failed", profileId, t);
		}
	}

	private void dropProfile(final UserProfileId profileId, final String reason) {
		final Set<String> deviceIds = getDeviceIds(profileId);
		if (deviceIds.isEmpty()) return;
		LOGGER.info("[{}] dropping {} push device(s): {}", profileId, deviceIds.size(), reason);
		for (String deviceId : deviceIds) {
			runAs(profileId, () -> unsubscribe(profileId, deviceId));
		}
	}

	private boolean isAuthSessionRequired() {
		return Boolean.parseBoolean(WebTopApp.getInstanceProperties().getProperty(PROP_REQUIRE_AUTH_SESSION, "true"));
	}

	private boolean hasActiveAuthSession(final UserProfileId profileId) {
		try {
			return !WebTopApp.getInstance().getWebTopManager()
				.listActiveAuthSessions(profileId.getDomainId(), profileId.getUserId()).isEmpty();
		} catch (Throwable t) {
			// a failing lookup must not cut push for everybody
			LOGGER.warn("[{}] unable to check app sessions: assuming active", profileId, t);
			return true;
		}
	}

	private void runAs(final UserProfileId profileId, final Runnable work) {
		final SecurityManager sm = securityManager;
		if (sm == null) {
			LOGGER.error("[{}] no SecurityManager captured: push operation skipped", profileId);
			return;
		}
		// the TARGET USER's Subject (not an admin one): listeners resolve the
		// user's own managers and settings through it
		final Subject userSubject = RunContext.buildSubject(sm, profileId);
		userSubject.execute(work);
	}

	/**
	 * Aligns the gateway links with the current license state, one link for
	 * each licensed domain, and periodically re-checks the subscribed users.
	 */
	private void reconcile() {
		try {
			final Map<String, String> licensed = WT.isLatestWebApp() ? collectLicensedDomains() : Collections.emptyMap();
			final List<NodejsDevicePushLink> toUnregister = new ArrayList<>();
			final List<NodejsDevicePushLink> toShutdown = new ArrayList<>();

			synchronized (linksLock) {
				if (stopped) return;
				for (NodejsDevicePushLink link : new ArrayList<>(links.values())) {
					final String token = licensed.get(link.getDomainId());
					if (link.isShutDown()) {
						// self-terminated (no longer the latest webapp instance)
						links.remove(link.getDomainId());
					} else if (token == null) {
						if (WT.isLatestWebApp()) {
							LOGGER.info("[{}] Connect license no longer active: unregistering push link", link.getDomainId());
							toUnregister.add(link);
						} else {
							toShutdown.add(link);
						}
						links.remove(link.getDomainId());
					} else if (!token.equals(link.getLicenseToken())) {
						LOGGER.info("[{}] Connect license changed: restarting push link", link.getDomainId());
						toShutdown.add(link);
						links.remove(link.getDomainId());
					}
				}
				for (Map.Entry<String, String> entry : licensed.entrySet()) {
					if (links.containsKey(entry.getKey())) continue;
					final NodejsDevicePushLink link = bootLink(entry.getKey(), entry.getValue());
					if (link != null) links.put(entry.getKey(), link);
				}
				linksView = new HashMap<>(links);
			}
			for (NodejsDevicePushLink link : toUnregister) {
				try { link.unregister(); } catch (Throwable t) { LOGGER.warn("[{}] push link unregister failed", link.getDomainId(), t); }
			}
			for (NodejsDevicePushLink link : toShutdown) {
				try { link.shutdown(); } catch (Throwable t) { LOGGER.warn("[{}] push link shutdown failed", link.getDomainId(), t); }
			}

			if (++tick % AUTH_SESSIONS_SWEEP_EVERY_TICKS == 0) sweepAuthSessions();

		} catch (Throwable t) {
			LOGGER.warn("push reconcile failed", t);
		}
	}

	/**
	 * A device that vanished never unsubscribes; its app session however
	 * expires, and that is the sign of life that can be checked from here.
	 */
	private void sweepAuthSessions() {
		if (!isAuthSessionRequired()) return;
		for (UserProfileId profileId : new ArrayList<>(subscriptions.keySet())) {
			if (!hasActiveAuthSession(profileId)) dropProfile(profileId, "no active app session");
		}
	}

	/**
	 * @return The license string of each enabled domain holding a valid
	 * Connect license (empty when push is disabled or misconfigured).
	 */
	private Map<String, String> collectLicensedDomains() {
		final Map<String, String> licensed = new LinkedHashMap<>();
		final Properties props = WebTopApp.getInstanceProperties();
		if (!Boolean.parseBoolean(props.getProperty(PROP_ENABLED, "false"))) return licensed;
		try {
			final Set<String> domainIds = WebTopApp.getInstance().getWebTopManager().listDomainIds(EnabledCond.ENABLED_ONLY);
			if (domainIds == null) return licensed;
			for (String domainId : domainIds) {
				final ConnectProduct product = new ConnectProduct(domainId);
				if (!WT.isLicensed(product)) continue;
				final ProductLicense plic = WT.findProductLicense(product);
				if (plic == null) continue;
				String token;
				try {
					final String activated = plic.getActivatedLicenseString();
					token = (activated != null && !activated.isEmpty()) ? activated : plic.getLicenseString();
				} catch (Throwable ignore) {
					token = plic.getLicenseString();
				}
				if (token != null && !token.isEmpty()) licensed.put(domainId, token);
			}
		} catch (Throwable t) {
			LOGGER.warn("Connect license check failed", t);
		}
		return licensed;
	}

	private NodejsDevicePushLink bootLink(final String domainId, final String licenseToken) {
		final Properties props = WebTopApp.getInstanceProperties();
		final String url = trimToNull(props.getProperty(PROP_URL));
		if (url == null) {
			LOGGER.error("push gateway enabled but {} missing", PROP_URL);
			return null;
		}
		try {
			final URI uri = URI.create(url);
			if (!"wss".equalsIgnoreCase(uri.getScheme()) && !Boolean.parseBoolean(props.getProperty(PROP_ALLOW_INSECURE, "false"))) {
				// the hello carries the license and events carry mail data
				LOGGER.error("push gateway URL must use wss:// ({}={}); set {}=true to accept it in development", PROP_URL, url, PROP_ALLOW_INSECURE);
				return null;
			}
			// the public URL the mobile app registers against: no operator-set
			// property needed
			final String baseUrl = trimToNull(WT.getPublicBaseUrl(domainId));
			if (baseUrl == null) {
				LOGGER.error("[{}] push gateway: no public base URL for domain", domainId);
				return null;
			}
			// the PRIMARY (internet) domain-name: the very value the Connect license
			// is bound to (ConnectProduct, HardwareIDSource.DOMAIN_INTERNET_NAME),
			// not the authentication domain-name. The gateway needs it, with the
			// domain ID, to validate the license: never say hello without it.
			final String domainName = trimToNull(WT.getPrimaryDomainName(domainId));
			if (domainName == null) {
				LOGGER.error("[{}] push gateway: no domain-name for domain", domainId);
				return null;
			}
			final NodejsDevicePushLink link = new NodejsDevicePushLink(this::onGatewaySubscribe, WT::isLatestWebApp, domainId, domainName, uri, baseUrl, licenseToken);
			link.start();
			LOGGER.info("[{}] push link started (url={}, baseUrl={}, domainName={})", domainId, url, baseUrl, domainName);
			return link;
		} catch (Throwable t) {
			LOGGER.error("[{}] push link failed to start", domainId, t);
			return null;
		}
	}

	private static String trimToNull(String s) {
		if (s == null) return null;
		final String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
