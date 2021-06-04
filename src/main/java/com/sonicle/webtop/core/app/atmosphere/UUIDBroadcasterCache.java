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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.atmosphere.cache.BroadcastMessage;
import org.atmosphere.cache.CacheMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class UUIDBroadcasterCache extends UUIDBroadcasterCacheOrig272 {
	private final static Logger LOGGER = LoggerFactory.getLogger(UUIDBroadcasterCache.class);
	private static final String NOT_READY_UUID = "0000000-0000-0000-0000-000000000000";
	
	private final AtomicInteger readyCounter = new AtomicInteger(0);
	
	@Override
	public void cleanup() {
		readyCounter.set(-1);
		super.cleanup();
	}

	@Override
	public CacheMessage addToCache(String broadcasterId, String uuid, BroadcastMessage message) {
		if (readyCounter.compareAndSet(0, 0)) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("---------- addToCache readyCounter is 0");
			return super.addToCache(broadcasterId, NOT_READY_UUID, message);
		} else {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("---------- addToCache_orig");
			return super.addToCache(broadcasterId, uuid, message);
		}
	}

	@Override
	public List<Object> retrieveFromCache(String broadcasterId, String uuid) {
		List<Object> first = null;
		if (readyCounter.compareAndSet(0, 0)) {
			try {
				readWriteLock.writeLock().lock();
				first = super.retrieveFromCache(broadcasterId, NOT_READY_UUID);
				activeClients.remove(NOT_READY_UUID);
				if (LOGGER.isTraceEnabled()) LOGGER.trace("---------- retrieveFromCache before ready: {} resumed", first.size());
			} finally {
				readWriteLock.writeLock().unlock();
			}
		}
		
		List<Object> remaining = super.retrieveFromCache(broadcasterId, uuid);
		if (first != null) {
			first.addAll(remaining);
			return first;
		} else {
			return remaining;
		}
	}
	
	public void updateResourceReadyState(String broadcasterId, String uuid, boolean ready) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}, {}, {}] updateResourceReadyState", broadcasterId, uuid, ready);
		if (ready) {
			readyCounter.incrementAndGet();
		} else {
			readyCounter.decrementAndGet();
		}
		//readyCounter.updateAndGet((count) -> ready ? count+1 : count-1);
	}
	
	@Override
	protected void dump() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("---------- DUMP ----------");
			LOGGER.trace("readyCounter: {}", readyCounter.get());
			LOGGER.trace("Messages keys: {}", messages.keySet().size());
			for (Map.Entry<String, ConcurrentLinkedQueue<CacheMessage>> entry : messages.entrySet()) {
				LOGGER.trace("Messages key [{}] queue count: {}", entry.getKey(), entry.getValue().size());
			}
			LOGGER.trace("--------------------------");
		}
	}
}
