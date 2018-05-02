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
package com.sonicle.webtop.core.app.atmosphere;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.atmosphere.cache.BroadcastMessage;
import org.atmosphere.cache.BroadcasterCacheInspector;
import org.atmosphere.cache.CacheMessage;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.BroadcasterCache;
import org.atmosphere.cpr.BroadcasterCacheListener;
import org.atmosphere.util.ExecutorsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.atmosphere.cpr.ApplicationConfig.UUIDBROADCASTERCACHE_CLIENT_IDLETIME;
import static org.atmosphere.cpr.ApplicationConfig.UUIDBROADCASTERCACHE_IDLE_CACHE_INTERVAL;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.util.UUIDProvider;

/**
 *
 * @author malbinola
 */
public class UUIDBroadcasterCache implements BroadcasterCache {

    private final static Logger logger = LoggerFactory.getLogger(UUIDBroadcasterCache.class);

    private final Map<String, ClientQueue> messages = new ConcurrentHashMap<String, ClientQueue>();
    private final Map<String, Long> activeClients = new ConcurrentHashMap<String, Long>();
    protected final List<BroadcasterCacheInspector> inspectors = new LinkedList<BroadcasterCacheInspector>();
    private ScheduledFuture scheduledFuture;
    protected ScheduledExecutorService taskScheduler;
    private long clientIdleTime = TimeUnit.SECONDS.toMillis(60); // 1 minutes
    private long invalidateCacheInterval = TimeUnit.SECONDS.toMillis(30); // 30 seconds
    private boolean shared = true;
    protected final List<Object> emptyList = Collections.<Object>emptyList();
    protected final List<BroadcasterCacheListener> listeners = new LinkedList<BroadcasterCacheListener>();
    private UUIDProvider uuidProvider;
	private static final String NULL_ATR_ID = "0000000-0000-0000-0000-000000000000";
	private boolean cacheForAllOnNull = false;

    /**
     * This class wraps all messages to be delivered to a client. The class is thread safe to be accessed in a
     * concurrent context.
     */
    public final static class ClientQueue implements Serializable {
        private static final long serialVersionUID = -126253550299206646L;

        private final ConcurrentLinkedQueue<CacheMessage> queue = new ConcurrentLinkedQueue<CacheMessage>();
        private final Set<String> ids = Collections.synchronizedSet(new HashSet<String>());

        public ConcurrentLinkedQueue<CacheMessage> getQueue() {
            return queue;
        }

        public Set<String> getIds() {
            return ids;
        }

        @Override
        public String toString() {
            return queue.toString();
        }
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
                Long.valueOf(config.getInitParameter(UUIDBROADCASTERCACHE_CLIENT_IDLETIME, "60")));

        invalidateCacheInterval = TimeUnit.SECONDS.toMillis(
                Long.valueOf(config.getInitParameter(UUIDBROADCASTERCACHE_IDLE_CACHE_INTERVAL, "30")));

        uuidProvider = config.uuidProvider();
    }

    @Override
    public void start() {
        scheduledFuture = taskScheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                invalidateExpiredEntries();
            }
        }, 0, invalidateCacheInterval, TimeUnit.MILLISECONDS);
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
				if (cacheForAllOnNull) {
					//no clients are connected right now, caching message for all active clients
					for (Map.Entry<String, Long> entry : activeClients.entrySet()) {
						addMessageIfNotExists(broadcasterId, entry.getKey(), cacheMessage);
					}
				} else {
					addMessageIfNotExists(broadcasterId, NULL_ATR_ID, cacheMessage);
				}
            } else {
                cacheCandidate(broadcasterId, uuid);
                addMessageIfNotExists(broadcasterId, uuid, cacheMessage);
            }
        }
        return cacheMessage;
    }
	
    @Override
    public List<Object> retrieveFromCache(String broadcasterId, String uuid) {
        List<Object> result = new ArrayList<>();
		
		if (!cacheForAllOnNull) {
			ClientQueue nullClientQueue = messages.remove(NULL_ATR_ID);
			if (nullClientQueue != null) {
				ConcurrentLinkedQueue<CacheMessage> clientMessages = nullClientQueue.getQueue();
				for (CacheMessage cacheMessage : clientMessages) {
					result.add(cacheMessage.getMessage());
					logger.trace("Removing for NullAtmosphereResource {} cached message {}", uuid, cacheMessage.getMessage());
					nullClientQueue.getQueue().remove(cacheMessage);
					nullClientQueue.getIds().remove(cacheMessage.getId());
				}
			}
		} 

        ClientQueue clientQueue;
        cacheCandidate(broadcasterId, uuid);
        clientQueue = messages.remove(uuid);
        ConcurrentLinkedQueue<CacheMessage> clientMessages;
        if (clientQueue != null) {
            clientMessages = clientQueue.getQueue();

            for (CacheMessage cacheMessage : clientMessages) {
                result.add(cacheMessage.getMessage());
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Retrieved for AtmosphereResource {} cached messages {}", uuid, result);
            logger.trace("Available cached message {}", messages);
        }

        return result;
    }

    @Override
    public BroadcasterCache clearCache(String broadcasterId, String uuid, CacheMessage message) {
        ClientQueue clientQueue;
        clientQueue = messages.get(uuid);
        if (clientQueue != null && !clientQueue.getQueue().isEmpty()) {
            logger.trace("Removing for AtmosphereResource {} cached message {}", uuid, message.getMessage());
            notifyRemoveCache(broadcasterId, new CacheMessage(message.getId(), message.getCreateTime(), message.getMessage(), uuid));
            clientQueue.getQueue().remove(message);
            clientQueue.getIds().remove(message.getId());
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
            logger.debug("Duplicate message {} for client {}", message, clientId);
        }
    }

    private void addMessage(String broadcasterId, String clientId, CacheMessage message) {
        ClientQueue clientQueue = messages.get(clientId);
        if (clientQueue == null) {
            clientQueue = new ClientQueue();
            // Make sure the client is not in the process of being invalidated
            if (activeClients.get(clientId) != null) {
                messages.put(clientId, clientQueue);
			} else if (!cacheForAllOnNull && NULL_ATR_ID.equals(clientId)) {
				messages.put(clientId, clientQueue);
            } else {
                // The entry has been invalidated
                logger.debug("Client {} is no longer active. Not caching message {}}", clientId, message);
                return;
            }
        }
        notifyAddCache(broadcasterId, message);
        clientQueue.getQueue().offer(message);
        clientQueue.getIds().add(message.getId());
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
        ClientQueue clientQueue = messages.get(clientId);
        return clientQueue != null && clientQueue.getIds().contains(messageId);
    }

    public Map<String, ClientQueue> messages() {
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

        Set<String> inactiveClients = new HashSet<String>();
        for (Map.Entry<String, Long> entry : activeClients.entrySet()) {
            if (now - entry.getValue() > clientIdleTime) {
                logger.trace("Invalidate client {}", entry.getKey());
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
    }

    @Override
    public BroadcasterCache excludeFromCache(String broadcasterId, AtmosphereResource r) {
        activeClients.remove(r.uuid());
        return this;
    }

    @Override
    public BroadcasterCache cacheCandidate(String broadcasterId, String uuid) {
        long now = System.currentTimeMillis();
        activeClients.put(uuid, now);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	private final static Logger logger = LoggerFactory.getLogger(UUIDBroadcasterCache.class);
	
	@Override
	public BroadcasterCache clearCache(String broadcasterId, String uuid, CacheMessage message) {
		ClientQueue clientQueue;
		clientQueue = messages.get(uuid);
		if (clientQueue != null && !clientQueue.getQueue().isEmpty()) {
			logger.trace("Removing for AtmosphereResource {} cached message {}", uuid, message.getMessage());
			notifyRemoveCache(broadcasterId, new CacheMessage(message.getId(), message.getCreateTime(), message.getMessage(), uuid));
			clientQueue.getQueue().remove(message);
			clientQueue.getIds().remove(message.getId());
		}
		return this;
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
	*/
}
