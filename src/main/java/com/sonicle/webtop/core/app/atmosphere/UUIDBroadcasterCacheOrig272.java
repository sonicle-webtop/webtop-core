/*
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.atmosphere;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterCache;
import org.atmosphere.cpr.BroadcasterCacheListener;
import org.atmosphere.util.ExecutorsFactory;
import org.atmosphere.util.UUIDProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.atmosphere.cache.BroadcastMessage;
import org.atmosphere.cache.BroadcasterCacheInspector;
import org.atmosphere.cache.CacheMessage;

import static org.atmosphere.cpr.ApplicationConfig.UUIDBROADCASTERCACHE_CLIENT_IDLETIME;
import static org.atmosphere.cpr.ApplicationConfig.UUIDBROADCASTERCACHE_IDLE_CACHE_INTERVAL;

/**
 * An improved {@link BroadcasterCache} implementation that is based on the unique identifier (UUID) that all
 * {@link AtmosphereResource}s have.
 *
 * @author Paul Khodchenkov
 * @author Jeanfrancois Arcand
 * 
 * UUIDBroadcasterCache class backported from Atmosphere Framework 2.7.2
 * See: https://github.com/Atmosphere/atmosphere/blob/atmosphere-project-2.7.2/modules/cpr/src/main/java/org/atmosphere/cache/UUIDBroadcasterCache.java
 */
public class UUIDBroadcasterCacheOrig272 implements BroadcasterCache {

    private final static Logger logger = LoggerFactory.getLogger(UUIDBroadcasterCache.class);

    protected final Map<String, ConcurrentLinkedQueue<CacheMessage>> messages = new ConcurrentHashMap<>(); // Modified to 'protected' in order to allow class extension!
    protected final Map<String, Long> activeClients = new ConcurrentHashMap<>(); // Modified to 'protected' in order to allow class extension!
    protected final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Modified to 'protected' in order to allow class extension!

    protected final List<BroadcasterCacheInspector> inspectors = new LinkedList<>();
    private ScheduledFuture<?> scheduledFuture;
    protected ScheduledExecutorService taskScheduler;
    private long clientIdleTime = TimeUnit.SECONDS.toMillis(60); // 1 minutes
    private long invalidateCacheInterval = TimeUnit.SECONDS.toMillis(30); // 30 seconds
    private boolean shared = true;
    protected final List<Object> emptyList = Collections.emptyList();
    protected final List<BroadcasterCacheListener> listeners = new LinkedList<>();
    private UUIDProvider uuidProvider;

    public UUIDBroadcasterCacheOrig272() {
    }

        @Override
    public void configure(AtmosphereConfig config) {
        Object o = config.properties().get("shared");
        if (o != null) {
            shared = Boolean.parseBoolean(o.toString());
        }

        if (shared) {
            taskScheduler = ExecutorsFactory.getScheduler(config);
        } else {
            taskScheduler = Executors.newSingleThreadScheduledExecutor();
        }

        clientIdleTime = TimeUnit.SECONDS.toMillis(
                Long.parseLong(config.getInitParameter(UUIDBROADCASTERCACHE_CLIENT_IDLETIME, "60")));

        invalidateCacheInterval = TimeUnit.SECONDS.toMillis(
                Long.parseLong(config.getInitParameter(UUIDBROADCASTERCACHE_IDLE_CACHE_INTERVAL, "30")));

        uuidProvider = config.uuidProvider();
    }

    @Override
    public void start() {
        scheduledFuture = taskScheduler.scheduleWithFixedDelay(this::invalidateExpiredEntries, 0, invalidateCacheInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        cleanup();

        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }

    @Override
    public void cleanup() {
        messages.clear();
        activeClients.clear();
        emptyList.clear();
        inspectors.clear();

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

    @Override
    public CacheMessage addToCache(String broadcasterId, String uuid, BroadcastMessage message) {
		if (logger.isTraceEnabled()) logger.trace("addToCache({}, {})", broadcasterId, uuid);
        if (logger.isTraceEnabled()) {
            logger.trace("Adding for AtmosphereResource {} cached messages {}", uuid, message.message());
            logger.trace("Active clients {}", activeClients());
        }

        String messageId = uuidProvider.generateUuid();
        boolean cache = true;
        if (!inspect(message)) {
            cache = false;
        }

        CacheMessage cacheMessage = new CacheMessage(messageId, message.message(), uuid);
        if (cache) {
            if (uuid.equals(NULL)) {
                //no clients are connected right now, caching message for all active clients
                for (Map.Entry<String, Long> entry : activeClients.entrySet()) {
                    addMessageIfNotExists(broadcasterId, entry.getKey(), cacheMessage);
                }
				if (activeClients.isEmpty()) logger.trace("Message NOT cached");
            } else {
                cacheCandidate(broadcasterId, uuid);
                addMessageIfNotExists(broadcasterId, uuid, cacheMessage);
            }
        }
        return cacheMessage;
    }

    @Override
    public List<Object> retrieveFromCache(String broadcasterId, String uuid) {
		if (logger.isTraceEnabled()) logger.trace("retrieveFromCache({}, {})", broadcasterId, uuid);
        try {
            readWriteLock.writeLock().lock();
            cacheCandidate(broadcasterId, uuid);

            ConcurrentLinkedQueue<CacheMessage> clientQueue = messages.remove(uuid);
            if (clientQueue != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Retrieved for AtmosphereResource {} cached messages {}", uuid, (long) clientQueue.size());
                    logger.trace("Available cached message {}", messages);
                }
                return clientQueue.parallelStream().map(CacheMessage::getMessage).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public BroadcasterCache clearCache(String broadcasterId, String uuid, CacheMessage message) {
		if (logger.isTraceEnabled()) logger.trace("clearCache({}, {})", broadcasterId, uuid);
        ConcurrentLinkedQueue<CacheMessage> clientQueue = messages.get(uuid);
        if (clientQueue != null && !clientQueue.isEmpty()) {
            if (logger.isTraceEnabled()) logger.trace("Removing for AtmosphereResource {} cached message {}", uuid, message.getMessage());
            notifyRemoveCache(broadcasterId, new CacheMessage(message.getId(), message.getCreateTime(), message.getMessage(), uuid));
            clientQueue.remove(message);
        }
        return this;
    }

    @Override
    public BroadcasterCache inspector(BroadcasterCacheInspector b) {
        inspectors.add(b);
        return this;
    }

    @Override
    public BroadcasterCache addBroadcasterCacheListener(BroadcasterCacheListener l) {
        listeners.add(l);
        return this;
    }

    @Override
    public BroadcasterCache removeBroadcasterCacheListener(BroadcasterCacheListener l) {
        listeners.remove(l);
        return this;
    }

    protected String uuid(AtmosphereResource r) {
        return r.uuid();
    }

    private void addMessageIfNotExists(String broadcasterId, String clientId, CacheMessage message) {
        if (!hasMessage(clientId, message.getId())) {
            addMessage(broadcasterId, clientId, message);
        } else {
            if (logger.isDebugEnabled()) logger.debug("Duplicate message {} for client {}", message, clientId);
        }
    }

    private void addMessage(String broadcasterId, String clientId, CacheMessage message) {
        try {
            readWriteLock.readLock().lock();
            ConcurrentLinkedQueue<CacheMessage> clientQueue = messages.get(clientId);

            if (clientQueue == null) {
                clientQueue = new ConcurrentLinkedQueue<>();
                // Make sure the client is not in the process of being invalidated
                if (activeClients.get(clientId) != null) {
                    messages.put(clientId, clientQueue);
                } else {
                    // The entry has been invalidated
                    if (logger.isDebugEnabled()) logger.debug("Client {} is no longer active. Not caching message {}}", clientId, message);
                    return;
                }
            }
            notifyAddCache(broadcasterId, message);
            clientQueue.offer(message);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private void notifyAddCache(String broadcasterId, CacheMessage message) {
        for (BroadcasterCacheListener l : listeners) {
            try {
                l.onAddCache(broadcasterId, message);
            } catch (Exception ex) {
                logger.warn("Listener exception", ex);
            }
        }
    }

    private void notifyRemoveCache(String broadcasterId, CacheMessage message) {
        for (BroadcasterCacheListener l : listeners) {
            try {
                l.onRemoveCache(broadcasterId, message);
            } catch (Exception ex) {
                logger.warn("Listener exception", ex);
            }
        }
    }

    private boolean hasMessage(String clientId, String messageId) {
        ConcurrentLinkedQueue<CacheMessage> clientQueue = messages.get(clientId);
        return clientQueue != null && clientQueue.parallelStream().anyMatch(m -> Objects.equals(m.getId(), messageId));
    }

    public Map<String, ConcurrentLinkedQueue<CacheMessage>> messages() {
        return messages;
    }

    public Map<String, Long> activeClients() {
        return activeClients;
    }

    protected boolean inspect(BroadcastMessage m) {
        for (BroadcasterCacheInspector b : inspectors) {
            if (!b.inspect(m)) return false;
        }
        return true;
    }

    public void setInvalidateCacheInterval(long invalidateCacheInterval) {
        this.invalidateCacheInterval = invalidateCacheInterval;
        scheduledFuture.cancel(true);
        start();
    }

    public void setClientIdleTime(long clientIdleTime) {
        this.clientIdleTime = clientIdleTime;
    }

    protected void invalidateExpiredEntries() {
        long now = System.currentTimeMillis();

        Set<String> inactiveClients = new HashSet<>();
        for (Map.Entry<String, Long> entry : activeClients.entrySet()) {
            if (now - entry.getValue() > clientIdleTime) {
                if (logger.isTraceEnabled()) logger.trace("Invalidate client {}", entry.getKey());
                inactiveClients.add(entry.getKey());
            }
        }

        for (String clientId : inactiveClients) {
            activeClients.remove(clientId);
            messages.remove(clientId);
        }

        for (String msg : messages().keySet()) {
            if (!activeClients().containsKey(msg)) {
                messages().remove(msg);
            }
        }
		
		dump();
    }

    @Override
    public BroadcasterCache excludeFromCache(String broadcasterId, AtmosphereResource r) {
        activeClients.remove(r.uuid());
        return this;
    }

    @Override
    public BroadcasterCache cacheCandidate(String broadcasterId, String uuid) {
        activeClients.put(uuid, System.currentTimeMillis());
        return this;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public List<BroadcasterCacheListener> listeners() {
        return listeners;
    }

    public List<BroadcasterCacheInspector> inspectors() {
        return inspectors;
    }
	
	protected void dump() {}
}
