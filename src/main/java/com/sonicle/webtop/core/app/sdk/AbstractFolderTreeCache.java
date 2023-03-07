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

import com.sonicle.commons.cache.AbstractOptionableBulkCache;
import com.sonicle.webtop.core.app.model.FolderShareFolder;
import com.sonicle.webtop.core.app.model.FolderShareOrigin;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import net.sf.qualitycheck.Check;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 *
 * @author malbinola
 */
public abstract class AbstractFolderTreeCache <T, O extends FolderShareOrigin, F extends FolderShareFolder, P> extends AbstractOptionableBulkCache<AbstractFolderTreeCache.Target> {
	protected final LinkedHashMap<UserProfileId, O> origins = new LinkedHashMap<>();
	protected final LinkedHashMap<T, F> folders = new LinkedHashMap<>();
	protected final MultiValuedMap<UserProfileId, F> foldersByOrigin = new ArrayListValuedHashMap<>();
	protected final HashMap<T, O> originsByFolder = new HashMap<>();

	@Override
	protected void internalCleanupCache(final Target options) {
		if (Target.ALL.equals(options) || Target.ORIGINS.equals(options)) {
			origins.clear();
		}
		if (Target.ALL.equals(options) || Target.FOLDERS.equals(options)) {
			folders.clear();
			foldersByOrigin.clear();
			originsByFolder.clear();
		}
	}
	
	@Override
	public void init() {
		super.init(Target.ALL);
	}
	
	public O getOriginByProfile(final UserProfileId originPid) {
		Check.notNull(originPid, "originPid");
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return origins.get(originPid);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public O getOriginByFolder(final T folderId) {
		Check.notNull(folderId, "folderId");
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return originsByFolder.get(folderId);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public O getOrigin(final UserProfileId originPid) {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return origins.get(originPid);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public boolean existsOrigin(final UserProfileId originPid) {
		if (originPid == null) return false;
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return origins.containsKey(originPid);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public Collection<O> getOrigins() {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return Collections.unmodifiableCollection(origins.values());
		} finally {
			unlockRead(stamp);
		}
	}
	
	public Collection<F> getFoldersByOrigin(final O origin) {
		return getFoldersByOrigin(origin.getProfileId());
	}
	
	public Collection<F> getFoldersByOrigin(final UserProfileId originPid) {
		Check.notNull(originPid, "originPid");
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			Collection<F> items = foldersByOrigin.get(originPid);
			return (items == null) ? Collections.emptyList() :  Collections.unmodifiableCollection(items);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public boolean existsFolder(final T folderId) {
		if (folderId == null) return false;
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return folders.containsKey(folderId);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public F getFolder(final T folderId) {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return folders.get(folderId);
		} finally {
			unlockRead(stamp);
		}
	}
	
	public Collection<F> getFolders() {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return Collections.unmodifiableCollection(folders.values());
		} finally {
			unlockRead(stamp);
		}
	}
	
	public Set<T> getFolderIDs() {
		internalCheckBeforeGetDoNotLockThis();
		long stamp = readLock();
		try {
			return Collections.unmodifiableSet(folders.keySet());
		} finally {
			unlockRead(stamp);
		}
	}
	
	public static enum Target {
		ALL, ORIGINS, FOLDERS;
	}
}
