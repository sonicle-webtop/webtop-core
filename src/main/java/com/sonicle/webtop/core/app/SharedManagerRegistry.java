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

import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.SharedManager;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.slf4j.Logger;

/**
 * App-level registry that holds ONE shared {@link BaseManager} instance per
 * (serviceId, {@link UserProfileId}) pair, so a single instance serves every web
 * session of that user and every sessionless REST call, instead of the old
 * per-session cache + per-REST-call throwaway model.
 *
 * <p>An instance is created lazily on first {@link #acquire} / {@link #getOrTouch}
 * (invoking {@link SharedManager#onSharedStartup()} once), kept alive while it has
 * at least one reference (web session, or a future mobile-push subscription), and
 * evicted by {@link #sweep(long)} once it has no references and has been idle
 * beyond the grace period (invoking {@link SharedManager#onSharedShutdown()}).</p>
 *
 * <p>Only Managers whose class implements {@link SharedManager} are held here;
 * routing lives in {@link WT#getServiceManager} / {@link WebTopSession}.</p>
 *
 * @author gbulfon
 */
class SharedManagerRegistry {
	private static final Logger LOGGER = WT.getLogger(SharedManagerRegistry.class);

	/** Ref-source prefix for durable web-session references. */
	static final String REF_SESSION = "s:";
	/** Ref-source prefix for durable mobile push-subscription references (future). */
	static final String REF_SUBSCRIPTION = "m:";

	private final ServiceManager serviceManager;
	private final ConcurrentHashMap<String, Holder> holders = new ConcurrentHashMap<>();
	//Sweep evictions run here, NOT on the sweeper timer thread: one user's slow
	//teardown (IMAP disconnects can take seconds) must not delay other evictions
	//or the next sweep tick. Daemon threads so a hung teardown can't block JVM exit.
	private final ExecutorService shutdownExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
		private final AtomicInteger seq = new AtomicInteger(1);
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "sharedManagerShutdown-" + seq.getAndIncrement());
			t.setDaemon(true);
			return t;
		}
	});

	SharedManagerRegistry(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	private static String key(String serviceId, UserProfileId pid) {
		return serviceId + "|" + ((pid != null) ? pid.toString() : "-");
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	/**
	 * Returns the shared instance for (serviceId, pid), creating it if absent, and
	 * registers a durable reference {@code refId} that keeps it from being evicted
	 * until {@link #release} is called with the same id.
	 */
	BaseManager acquire(String serviceId, UserProfileId pid, String refId) {
		return getOrCreate(serviceId, pid, refId);
	}

	/**
	 * Returns the shared instance for (serviceId, pid), creating it if absent, and
	 * only bumps its last-access time (no durable reference). Suitable for
	 * transient/background access and short REST calls that rely on the idle grace
	 * to keep the instance warm.
	 */
	BaseManager getOrTouch(String serviceId, UserProfileId pid) {
		return getOrCreate(serviceId, pid, null);
	}

	private BaseManager getOrCreate(String serviceId, UserProfileId pid, String refId) {
		final boolean[] iAmCreator = { false };
		Holder h = holders.compute(key(serviceId, pid), (k, existing) -> {
			if (existing == null) {
				// Constructor only (cheap) under the bin lock; the heavy
				// onSharedStartup() runs AFTER compute returns, guarded by the latch.
				BaseManager inst = serviceManager.instantiateServiceManager(serviceId, false, pid);
				if (inst == null) throw new WTRuntimeException("Cannot instantiate shared manager [{0}] for [{1}]", serviceId, pid);
				Holder nh = new Holder(inst);
				nh.lastAccessTs = now();
				if (refId != null) nh.refs.add(refId);
				iAmCreator[0] = true;
				return nh;
			} else {
				existing.lastAccessTs = now();
				if (refId != null) existing.refs.add(refId);
				return existing;
			}
		});

		if (iAmCreator[0]) {
			try {
				if (h.instance instanceof SharedManager) {
					LOGGER.debug("Shared manager [{}] starting up for [{}]", serviceId, pid);
					((SharedManager)h.instance).onSharedStartup();
				}
			} catch (Throwable t) {
				h.startupError = t;
				holders.remove(key(serviceId, pid), h);
				h.startupLatch.countDown();
				throw new WTRuntimeException(t, "Shared manager [{0}] startup failed for [{1}]", serviceId, pid);
			}
			h.startupLatch.countDown();
		} else {
			awaitUninterruptibly(h.startupLatch);
			if (h.startupError != null) {
				throw new WTRuntimeException(h.startupError, "Shared manager [{0}] startup failed for [{1}]", serviceId, pid);
			}
		}
		return h.instance;
	}

	/**
	 * Removes a durable reference previously added via {@link #acquire}. Does not
	 * evict immediately; {@link #sweep(long)} reclaims once idle beyond the grace.
	 */
	void release(String serviceId, UserProfileId pid, String refId) {
		Holder h = holders.get(key(serviceId, pid));
		if (h == null) return;
		h.refs.remove(refId);
		h.lastAccessTs = now();
	}

	/**
	 * Bumps the last-access time for the instance, if present (warm-keep).
	 */
	void touch(String serviceId, UserProfileId pid) {
		Holder h = holders.get(key(serviceId, pid));
		if (h != null) h.lastAccessTs = now();
	}

	/**
	 * Evicts every instance that has no references and has been idle longer than
	 * {@code graceMs}, invoking {@link SharedManager#onSharedShutdown()} on each
	 * (outside the map lock). Invoked periodically by the app sweeper.
	 */
	void sweep(long graceMs) {
		final long deadline = now() - graceMs;
		final List<Holder> evicted = new ArrayList<>();
		for (String k : new ArrayList<>(holders.keySet())) {
			holders.compute(k, (kk, h) -> {
				if (h == null) return null;
				if (h.startupLatch.getCount() != 0) return h; // still starting up
				if (h.refs.isEmpty() && h.lastAccessTs <= deadline) {
					evicted.add(h);
					return null; // remove atomically wrt acquire/getOrCreate
				}
				return h;
			});
		}
		for (Holder h : evicted) shutdownAsync(h);
		if (!evicted.isEmpty()) LOGGER.debug("Shared managers swept: {} evictions dispatched", evicted.size());
	}

	/**
	 * Evicts everything (application shutdown), invoking onSharedShutdown() on each.
	 * Runs synchronously — the container is unloading us, teardown must complete —
	 * then drains any sweep evictions still in flight on the executor.
	 */
	void shutdownAll() {
		final List<Holder> all = new ArrayList<>();
		for (String k : new ArrayList<>(holders.keySet())) {
			Holder h = holders.remove(k);
			if (h != null) all.add(h);
		}
		for (Holder h : all) shutdownQuietly(h);
		shutdownExecutor.shutdown();
		try {
			if (!shutdownExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
				LOGGER.warn("Async shared-manager evictions still running after 30s, proceeding with shutdown");
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		if (!all.isEmpty()) LOGGER.info("Shared managers: {} instances shut down", all.size());
	}

	//Runs one eviction's onSharedShutdown on the executor, re-binding the subject
	//bound on the calling (sweeper) thread — teardown code may hit RunContext.
	//Falls back inline if the executor is already stopped (app shutdown race).
	private void shutdownAsync(final Holder h) {
		Subject subject = null;
		try { subject = SecurityUtils.getSubject(); } catch (Throwable t) {}
		final Subject boundSubject = subject;
		try {
			shutdownExecutor.execute(new Runnable() {
				@Override
				public void run() {
					ThreadState threadState = (boundSubject != null) ? new SubjectThreadState(boundSubject) : null;
					try {
						if (threadState != null) threadState.bind();
						long t0 = System.currentTimeMillis();
						shutdownQuietly(h);
						LOGGER.debug("Shared manager evicted in {}ms", System.currentTimeMillis() - t0);
					} finally {
						if (threadState != null) threadState.clear();
					}
				}
			});
		} catch (RejectedExecutionException ex) {
			shutdownQuietly(h);
		}
	}

	int size() {
		return holders.size();
	}

	private void shutdownQuietly(Holder h) {
		try {
			if (h.instance instanceof SharedManager) ((SharedManager)h.instance).onSharedShutdown();
		} catch (Throwable t) {
			LOGGER.error("Shared manager shutdown threw", t);
		}
	}

	private static void awaitUninterruptibly(CountDownLatch latch) {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					latch.await();
					return;
				} catch (InterruptedException ex) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted) Thread.currentThread().interrupt();
		}
	}

	private static final class Holder {
		final BaseManager instance;
		final Set<String> refs = ConcurrentHashMap.newKeySet();
		volatile long lastAccessTs;
		final CountDownLatch startupLatch = new CountDownLatch(1);
		volatile Throwable startupError;

		Holder(BaseManager instance) {
			this.instance = instance;
		}
	}
}
