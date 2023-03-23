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
package com.sonicle.webtop.core.app.sdk;

import com.sonicle.commons.cache.AbstractBulkCache;
import com.sonicle.webtop.core.app.model.FolderShareOriginFolders;
import com.sonicle.webtop.core.app.model.ShareOrigin;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 *
 * @author malbinola
 * @param <T>
 * @param <O>
 */
public abstract class AbstractFolderShareCache <T, O extends ShareOrigin> extends AbstractBulkCache {
	protected final List<O> origins = new ArrayList<>();
	protected final Map<UserProfileId, O> originByProfile = new HashMap<>();
	protected final Map<T, O> originByFolderId = new HashMap<>();
	protected final Map<UserProfileId, FolderShareOriginFolders> foldersByProfile = new HashMap<>();
	protected final MultiValuedMap<UserProfileId, T> folderIdsByProfile = new ArrayListValuedHashMap<>();
	protected final Set<T> folderIds = new LinkedHashSet<>();
	
	@Override
	protected void internalCleanupCache() {
		origins.clear();
		originByProfile.clear();
		originByFolderId.clear();
		foldersByProfile.clear();
		folderIdsByProfile.clear();
		folderIds.clear();
	}
	
	public List<O> getOrigins() {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return Collections.unmodifiableList(origins);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public Map<UserProfileId, O> getOriginsMap() {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return Collections.unmodifiableMap(originByProfile);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public O getOrigin(final UserProfileId originProfileId) {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return originByProfile.get(originProfileId);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public O getOriginByFolderId(final T folderId) {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return originByFolderId.get(folderId);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public Collection<T> getFolderIdsByOrigin(final UserProfileId originProfileId) {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return Collections.unmodifiableCollection(folderIdsByProfile.get(originProfileId));
		} finally {
			unlockRead(stamp);
		}
	}
	
	public Set<T> getFolderIds() {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return Collections.unmodifiableSet(folderIds);
		} finally {
			unlockRead(stamp);
		}
	}
}
