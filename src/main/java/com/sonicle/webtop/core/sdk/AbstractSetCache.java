/*
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.sdk;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

/**
 *
 * @author malbinola
 * @param <K>
 */
public abstract class AbstractSetCache<K> implements SetCache<K> {
	private final StampedLock lock = new StampedLock();
	protected final Set<K> set;
	
	protected abstract void internalInitCache();
	
	public AbstractSetCache() {
		set = createCache();
	}
	
	protected Set<K> createCache() {
		return new HashSet<>();
	}
	
	public final synchronized void init() {
		long stamp = lock.writeLock();
		try {
			set.clear();
			internalInitCache();
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	@Override
	public synchronized boolean contains(K key) {
		long stamp = lock.readLock();
		try {
			return set.contains(key);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public synchronized boolean add(K key) {
		long stamp = lock.writeLock();
		try {
			return set.add(key);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	@Override
	public synchronized boolean remove(K key) {
		long stamp = lock.writeLock();
		try {
			return set.remove(key);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	@Override
	public synchronized void clear() {
		long stamp = lock.writeLock();
		try {
			set.clear();
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	@Override
	public int size() {
		long stamp = lock.readLock();
		try {
			return set.size();
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public Set<K> keys() {
		long stamp = lock.readLock();
		try {
			return set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set);
		} finally {
			lock.unlockRead(stamp);
		}
	}
}
