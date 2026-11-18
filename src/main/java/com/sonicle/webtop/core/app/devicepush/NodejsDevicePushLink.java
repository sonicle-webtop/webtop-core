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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import javax.net.ssl.SSLParameters;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket client link towards the external Node.js push gateway, serving
 * ONE licensed domain. No APNs / FCM / device state lives in Java: the link
 * forwards {@link DevicePushEvent}s and hands the subscribe / unsubscribe
 * commands issued by the gateway to {@link DevicePushManager}.
 *
 * <p>Threading: {@link #push} is called from arbitrary service threads (IMAP
 * idle, background tasks) and never blocks; a single sender thread drains the
 * pending frames. Inbound frames are dispatched on the WebSocket library's
 * read thread.</p>
 *
 * <p>Delivery: pending frames are kept per {@link DevicePushEvent.DeliveryClass}
 * so that a burst of one class cannot push out another. The frame being sent
 * is held until the send succeeds, across reconnects. Nothing is acknowledged
 * by the gateway: a frame handed to an open socket is considered delivered.</p>
 */
class NodejsDevicePushLink {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodejsDevicePushLink.class);
	private static final Gson GSON = new Gson();
	private static final int ALERT_QUEUE_CAP = 2048;
	private static final int STATE_SLOTS_CAP = 8192;
	private static final int DELTA_QUEUE_CAP = 4096;
	private static final int DEGRADED_SLOTS_CAP = 8192;
	private static final long RECONNECT_MIN_MS = 5_000L;
	private static final long RECONNECT_MAX_MS = 60_000L;
	// Matches WebTopApp.webappVersionCheck cadence: no point polling faster
	// than the value we're reading actually refreshes.
	private static final long LATEST_CHECK_INTERVAL_MS = 60_000L;
	private static final long STATS_INTERVAL_MS = 300_000L;

	/** Receiver of the subscribe / unsubscribe commands issued by the gateway. */
	interface CommandHandler {
		void onGatewaySubscribe(NodejsDevicePushLink link, UserProfileId profileId, String deviceId, boolean isSubscribe);
	}

	private final CommandHandler commandHandler;
	// Tomcat parallel deployment: only the latest webapp instance may talk to
	// the gateway
	private final BooleanSupplier latestWebApp;
	private final String domainId;
	private final String domainName;
	private final URI gatewayUri;
	// baseUrl identifies this instance to the gateway (must match the URL the
	// mobile app registers against); licenseToken is the raw WebTop Connect
	// license the gateway validates against the license server on every hello.
	// The license is bound to domainName, which is unrelated to baseUrl.
	private final String baseUrl;
	private final String licenseToken;
	private final ScheduledExecutorService scheduler;
	private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
	private final AtomicBoolean connected = new AtomicBoolean(false);
	private final Random reconnectJitter = new Random();

	private final Object pendingLock = new Object();
	private final ArrayDeque<Pending> alerts = new ArrayDeque<>();
	private final LinkedHashMap<String, Pending> states = new LinkedHashMap<>();
	private final LinkedHashMap<String, Pending> degraded = new LinkedHashMap<>();
	private final ArrayDeque<Pending> deltas = new ArrayDeque<>();

	private final AtomicLong statSent = new AtomicLong();
	private final AtomicLong statCoalesced = new AtomicLong();
	private final AtomicLong statDegraded = new AtomicLong();
	private final AtomicLong statDropped = new AtomicLong();
	private final AtomicLong statExpired = new AtomicLong();
	private final AtomicLong statRetried = new AtomicLong();
	private long statLastTotal = 0;

	private volatile WebSocketClient client;
	private volatile Thread sender;
	private volatile ScheduledFuture<?> reconnectFuture;
	private long backoffMs = RECONNECT_MIN_MS;

	NodejsDevicePushLink(CommandHandler commandHandler, BooleanSupplier latestWebApp, String domainId, String domainName, URI gatewayUri, String baseUrl, String licenseToken) {
		// both travel in every hello: the gateway keys the server and validates
		// the license on them
		if (domainId == null || domainId.isEmpty() || domainName == null || domainName.isEmpty()) throw new IllegalArgumentException("domainId and domainName are mandatory");
		this.commandHandler = commandHandler;
		this.latestWebApp = latestWebApp;
		this.domainId = domainId;
		this.domainName = domainName;
		this.gatewayUri = gatewayUri;
		this.baseUrl = baseUrl;
		this.licenseToken = licenseToken;
		this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
			Thread t = new Thread(r, "devicePush-scheduler-" + domainId);
			t.setDaemon(true);
			return t;
		});
	}

	String getDomainId() {
		return domainId;
	}

	String getLicenseToken() {
		return licenseToken;
	}

	boolean isShutDown() {
		return shuttingDown.get();
	}

	synchronized void start() {
		if (shuttingDown.get()) throw new IllegalStateException("already shut down");
		if (sender == null) {
			sender = new Thread(this::senderLoop, "devicePush-sender-" + domainId);
			sender.setDaemon(true);
			sender.start();
		}
		// Under Tomcat parallel deployment the old webapp instance keeps running
		// (draining browser sessions) but must stop touching the gateway.
		scheduler.scheduleAtFixedRate(this::checkIsLatestAndMaybeShutdown, LATEST_CHECK_INTERVAL_MS, LATEST_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(this::logStats, STATS_INTERVAL_MS, STATS_INTERVAL_MS, TimeUnit.MILLISECONDS);
		connect();
	}

	private void checkIsLatestAndMaybeShutdown() {
		if (shuttingDown.get()) return;
		try {
			if (!latestWebApp.getAsBoolean()) {
				LOGGER.info("[{}] no longer the latest webapp instance: shutting down push link", domainId);
				shutdown();
			}
		} catch (Throwable t) {
			LOGGER.warn("[{}] isLatest poll failed", domainId, t);
		}
	}

	synchronized void shutdown() {
		shuttingDown.set(true);
		if (reconnectFuture != null) reconnectFuture.cancel(false);
		WebSocketClient c = client;
		if (c != null) {
			try { c.close(); } catch (Throwable ignored) {}
		}
		scheduler.shutdownNow();
		if (sender != null) sender.interrupt();
		synchronized (pendingLock) {
			pendingLock.notifyAll();
		}
	}

	/**
	 * Best-effort "unregister" frame followed by a full shutdown: tells the
	 * gateway to drop this server's row (cascade removes devices) instead of
	 * just recording a passive close. Used when the license goes away.
	 */
	synchronized void unregister() {
		if (shuttingDown.get()) return;
		try {
			WebSocketClient c = client;
			if (c != null && connected.get()) {
				JsonObject frame = new JsonObject();
				frame.addProperty("op", "unregister");
				c.send(GSON.toJson(frame));
			}
		} catch (Throwable t) {
			LOGGER.warn("[{}] unregister frame send failed: proceeding with shutdown anyway", domainId, t);
		}
		shutdown();
	}

	void push(UserProfileId profileId, Set<String> deviceIds, DevicePushEvent event) {
		// Tomcat parallel deployment: only the latest instance owns push
		// forwarding, the draining one would duplicate every event.
		if (shuttingDown.get() || !latestWebApp.getAsBoolean()) return;
		try {
			final Pending pending = new Pending(buildFrame(profileId, deviceIds, event, false), event.getTtlMillis());
			final String key = profileId.toString() + "|" + event.getServiceId() + "|" + event.getType() + "|" + event.getCoalesceKey();

			synchronized (pendingLock) {
				switch (event.getDeliveryClass()) {
					case ALERT:
						if (alerts.size() >= ALERT_QUEUE_CAP) {
							alerts.pollFirst();
							statDropped.incrementAndGet();
							LOGGER.warn("[{}] alert queue full ({}): oldest alert dropped", domainId, ALERT_QUEUE_CAP);
						}
						alerts.addLast(pending);
						break;

					case STATE:
						// put() on an existing key keeps its position: a busy scope
						// cannot starve the others
						if (states.put(key, pending) != null) {
							statCoalesced.incrementAndGet();
						} else if (states.size() > STATE_SLOTS_CAP) {
							dropEldest(states);
						}
						break;

					case DELTA:
						if (degraded.containsKey(key)) {
							// a pending "refresh this scope" already covers it
							statDegraded.incrementAndGet();
						} else if (deltas.size() >= DELTA_QUEUE_CAP) {
							degraded.put(key, new Pending(buildFrame(profileId, deviceIds, event, true), 0));
							statDegraded.incrementAndGet();
							if (degraded.size() > DEGRADED_SLOTS_CAP) dropEldest(degraded);
						} else {
							deltas.addLast(pending);
						}
						break;
				}
				pendingLock.notifyAll();
			}
		} catch (Throwable t) {
			LOGGER.error("[{}] failed to enqueue {} event for {}", domainId, event.getType(), profileId, t);
		}
	}

	private void dropEldest(LinkedHashMap<String, Pending> map) {
		Iterator<Map.Entry<String, Pending>> it = map.entrySet().iterator();
		if (it.hasNext()) {
			it.next();
			it.remove();
			statDropped.incrementAndGet();
		}
	}

	private JsonObject buildFrame(UserProfileId profileId, Set<String> deviceIds, DevicePushEvent event, boolean degrade) {
		JsonObject frame = new JsonObject();
		frame.addProperty("op", "event");
		frame.addProperty("profileId", profileId.toString());
		frame.add("deviceIds", GSON.toJsonTree(deviceIds));
		for (Map.Entry<String, JsonElement> field : event.getFields().entrySet()) {
			if (degrade && event.getDegradeDropFields().contains(field.getKey())) continue;
			frame.add(field.getKey(), field.getValue());
		}
		if (!frame.has("type")) frame.addProperty("type", event.getType());
		frame.addProperty("service", event.getServiceId());
		return frame;
	}

	/**
	 * Next frame to send, by priority: alerts, then states, then degraded
	 * deltas, then deltas. Blocks while nothing is pending.
	 */
	private Pending take() throws InterruptedException {
		synchronized (pendingLock) {
			while (true) {
				if (shuttingDown.get()) throw new InterruptedException("shutting down");
				Pending p = alerts.pollFirst();
				if (p == null) p = pollEldest(states);
				if (p == null) p = pollEldest(degraded);
				if (p == null) p = deltas.pollFirst();
				if (p != null) return p;
				pendingLock.wait();
			}
		}
	}

	private Pending pollEldest(LinkedHashMap<String, Pending> map) {
		Iterator<Map.Entry<String, Pending>> it = map.entrySet().iterator();
		if (!it.hasNext()) return null;
		Pending p = it.next().getValue();
		it.remove();
		return p;
	}

	private void senderLoop() {
		Pending current = null;
		while (!shuttingDown.get()) {
			try {
				if (current == null) current = take();
				// pushed just before isLatest flipped: never forward from a
				// draining instance
				if (!latestWebApp.getAsBoolean()) {
					current = null;
					continue;
				}
				if (current.isExpired()) {
					statExpired.incrementAndGet();
					current = null;
					continue;
				}
				final WebSocketClient c = client;
				if (c == null || !connected.get()) {
					Thread.sleep(100);
					continue;
				}
				try {
					c.send(GSON.toJson(current.frame));
					statSent.incrementAndGet();
					current = null;
				} catch (Throwable t) {
					// keep the frame in hand: it goes out after the reconnect
					statRetried.incrementAndGet();
					LOGGER.debug("[{}] send failed, will retry", domainId, t);
					Thread.sleep(500);
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private void logStats() {
		try {
			final long sent = statSent.get(), coalesced = statCoalesced.get(), degr = statDegraded.get(),
				dropped = statDropped.get(), expired = statExpired.get(), retried = statRetried.get();
			final long total = sent + coalesced + degr + dropped + expired + retried;
			if (total == statLastTotal) return;
			statLastTotal = total;
			int pending;
			synchronized (pendingLock) {
				pending = alerts.size() + states.size() + degraded.size() + deltas.size();
			}
			LOGGER.info("[{}] push link stats: sent={}, coalesced={}, degraded={}, dropped={}, expired={}, retried={}, pending={}, connected={}",
				domainId, sent, coalesced, degr, dropped, expired, retried, pending, connected.get());
		} catch (Throwable t) {
			LOGGER.debug("[{}] stats logging failed", domainId, t);
		}
	}

	private synchronized void connect() {
		if (shuttingDown.get()) return;
		if (client != null) return;
		final long ts = System.currentTimeMillis();
		final String nonce = randomNonce();

		try {
			client = new WebSocketClient(gatewayUri) {
				@Override
				protected void onSetSSLParameters(SSLParameters sslParameters) {
					// the gateway's certificate must match the configured host
					sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
				}

				@Override
				public void onOpen(ServerHandshake handshake) {
					JsonObject hello = new JsonObject();
					hello.addProperty("op", "hello");
					hello.addProperty("timestamp", ts);
					hello.addProperty("nonce", nonce);
					hello.addProperty("baseUrl", baseUrl);
					hello.addProperty("licenseToken", licenseToken);
					hello.addProperty("domainId", domainId);
					hello.addProperty("domainName", domainName);
					send(GSON.toJson(hello));
				}

				@Override
				public void onMessage(String message) {
					handleInbound(this, message);
				}

				@Override
				public void onMessage(ByteBuffer bytes) {
					// binary frames not expected on this link
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					LOGGER.info("[{}] gateway connection closed code={} reason={} remote={}", domainId, code, reason, remote);
					connected.set(false);
					synchronized (NodejsDevicePushLink.this) {
						client = null;
					}
					scheduleReconnect();
				}

				@Override
				public void onError(Exception ex) {
					LOGGER.warn("[{}] gateway ws error", domainId, ex);
				}
			};
			client.setConnectionLostTimeout(0); // handled by the gateway's ping
			client.connect();
		} catch (Throwable t) {
			LOGGER.error("[{}] gateway connect failed", domainId, t);
			client = null;
			scheduleReconnect();
		}
	}

	private synchronized void scheduleReconnect() {
		if (shuttingDown.get()) return;
		long jitter = (long)(backoffMs * (0.9 + reconnectJitter.nextDouble() * 0.2));
		long delay = Math.min(RECONNECT_MAX_MS, jitter);
		backoffMs = Math.min(RECONNECT_MAX_MS, backoffMs * 2);
		try {
			reconnectFuture = scheduler.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
		} catch (Throwable t) {
			// scheduler stopped by a concurrent shutdown
		}
	}

	private void handleInbound(WebSocketClient source, String raw) {
		JsonObject frame;
		try {
			JsonElement el = GSON.fromJson(raw, JsonElement.class);
			if (el == null || !el.isJsonObject()) {
				LOGGER.warn("[{}] non-object frame", domainId);
				return;
			}
			frame = el.getAsJsonObject();
		} catch (Throwable t) {
			LOGGER.warn("[{}] unparseable frame", domainId, t);
			return;
		}
		final String op = string(frame, "op", "");
		switch (op) {
			case "hello-ack":
				connected.set(true);
				backoffMs = RECONNECT_MIN_MS;
				LOGGER.info("[{}] gateway handshake complete", domainId);
				return;
			case "hello-reject":
				// close: the reconnect backoff retries the hello (the license may
				// have been renewed meanwhile)
				LOGGER.error("[{}] gateway rejected hello: {}", domainId, string(frame, "reason", "unknown"));
				connected.set(false);
				try { source.close(); } catch (Throwable ignored) {}
				return;
			case "ping":
				JsonObject pong = new JsonObject();
				pong.addProperty("op", "pong");
				try { source.send(GSON.toJson(pong)); } catch (Throwable ignored) {}
				return;
			case "pong":
				return;
		}
		// commands are honoured only on a link the gateway has acknowledged
		if (!connected.get()) {
			LOGGER.warn("[{}] '{}' frame received before hello-ack: ignored", domainId, op);
			return;
		}
		switch (op) {
			case "subscribe":
				dispatchSubscribe(frame, true);
				break;
			case "unsubscribe":
				dispatchSubscribe(frame, false);
				break;
			case "resubscribe-all": {
				JsonElement entries = frame.get("entries");
				if (entries != null && entries.isJsonArray()) {
					for (JsonElement e : entries.getAsJsonArray()) {
						if (!e.isJsonObject()) continue;
						dispatchSubscribe(e.getAsJsonObject(), true);
					}
				}
				break;
			}
			default:
				LOGGER.warn("[{}] unexpected op: {}", domainId, op);
		}
	}

	private void dispatchSubscribe(JsonObject frame, boolean isSubscribe) {
		// a draining old instance must not acquire subscriptions in response to
		// a frame the gateway sent before it noticed the new instance
		if (!latestWebApp.getAsBoolean()) return;
		final String profileIdStr = string(frame, "profileId", null);
		final String deviceId = string(frame, "deviceId", null);
		if (profileIdStr == null || deviceId == null) {
			LOGGER.warn("[{}] subscribe frame missing profileId/deviceId", domainId);
			return;
		}
		final UserProfileId profileId = parseProfileId(profileIdStr);
		if (profileId == null) {
			LOGGER.warn("[{}] unparseable profileId '{}'", domainId, profileIdStr);
			return;
		}
		try {
			commandHandler.onGatewaySubscribe(this, profileId, deviceId, isSubscribe);
		} catch (Throwable t) {
			LOGGER.error("[{}] subscribe dispatch failed for {} / {}", domainId, profileId, deviceId, t);
		}
	}

	private static String string(JsonObject frame, String name, String defaultValue) {
		JsonElement el = frame.get(name);
		return (el != null && el.isJsonPrimitive()) ? el.getAsString() : defaultValue;
	}

	private static UserProfileId parseProfileId(String s) {
		int at = s.indexOf('@');
		if (at <= 0 || at == s.length() - 1) return null;
		return new UserProfileId(s.substring(at + 1), s.substring(0, at));
	}

	private static String randomNonce() {
		byte[] buf = new byte[16];
		new SecureRandom().nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}

	private static final class Pending {
		final JsonObject frame;
		final long expiresAt;

		Pending(JsonObject frame, long ttlMillis) {
			this.frame = frame;
			this.expiresAt = (ttlMillis > 0) ? System.currentTimeMillis() + ttlMillis : 0;
		}

		boolean isExpired() {
			return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
		}
	}
}
