/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.sdk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author malbinola
 * @param <K>
 * @param <V>
 */
public abstract class AbstractMapCache<K, V> implements Cache<K, V> {
	protected final Map<K, V> map;
	
	protected abstract void internalInitCache();
	protected abstract void internalMissKey(K key);
	
	public AbstractMapCache() {
		map = createCache();
	}
	
	protected Map<K,V> createCache() {
		return new HashMap<>();
	}
	
	public final synchronized void init() {
		clear();
		internalInitCache();
	}
	
	@Override
	public synchronized void clear() {
		map.clear();
	}

	@Override
	public synchronized V get(K key) {
		if (!map.containsKey(key)) {
			internalMissKey(key);
		}
		return map.get(key);
	}

	@Override
	public synchronized V put(K key, V value) {
		map.put(key, value);
		return value;
	}

	@Override
	public synchronized V remove(K key) {
		return map.remove(key);
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public Set<K> keys() {
		final Set<K> keys = map.keySet();
		if (!keys.isEmpty()) {
			return Collections.unmodifiableSet(keys);
		} else {
			return Collections.emptySet();
		}
	}
	
	@Override
	public Collection<V> values() {
		final Collection<V> values = map.values();
		if (!map.isEmpty()) {
			return Collections.unmodifiableCollection(values);
		} else {
			return Collections.emptySet();
		}
	}
}
