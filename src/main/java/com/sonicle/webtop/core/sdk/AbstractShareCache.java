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

import com.sonicle.webtop.core.model.ShareRoot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 *
 * @author malbinola
 * @param <T>
 * @param <R>
 */
public abstract class AbstractShareCache <T, R extends ShareRoot> {
	protected boolean ready = false;
	protected final ArrayList<R> shareRoots = new ArrayList<>();
	protected final HashMap<UserProfileId, R> ownerToShareRoot = new HashMap<>(); // <ownerPid, shareRoot>
	protected final HashMap<UserProfileId, String> ownerToWildcardShareFolder = new HashMap<>();  // <ownerPid, wildcardShareFolderId>
	protected final MultiValuedMap<String, T> rootShareToFolderShare = new ArrayListValuedHashMap<>(); // <shareRootId, folderId>
	protected final ArrayList<T> folderTo = new ArrayList<>();
	protected final HashMap<T, String> folderToShareFolder = new HashMap<>(); // <folderId, shareFolderId>
	protected final HashMap<T, String> folderToWildcardShareFolder = new HashMap<>(); // <folderId, wildcardShareFolderId>
	
	protected abstract void internalInitCache();
	
	public final synchronized void init() {
		clear();
		internalInitCache();
	}
	
	public synchronized void clear() {
		shareRoots.clear();
		ownerToShareRoot.clear();
		ownerToWildcardShareFolder.clear();
		rootShareToFolderShare.clear();
		folderTo.clear();
		folderToShareFolder.clear();
		folderToWildcardShareFolder.clear();
	}
	
	public final synchronized List<R> getShareRoots() {
		if (!ready) internalInitCache();
		return Collections.unmodifiableList(shareRoots);
	}

	public final synchronized R getShareRootByOwner(UserProfileId owner) {
		if (!ready) {
			internalInitCache();
		} else {
			if (!ownerToShareRoot.containsKey(owner)) {
				clear();
				internalInitCache();
			}
		}
		return ownerToShareRoot.get(owner);
	}
	
	public final String getShareRootIdByOwner(UserProfileId owner) {
		final R root = getShareRootByOwner(owner);
		return (root != null) ? root.getShareId() : null;
	}

	public final synchronized String getWildcardShareFolderIdByOwner(UserProfileId owner) {
		if (!ready) {
			internalInitCache();
		} else {
			if (!ownerToWildcardShareFolder.containsKey(owner) && ownerToShareRoot.isEmpty()) {
				clear();
				internalInitCache();
			}
		}
		return ownerToWildcardShareFolder.get(owner);
	}

	public final synchronized String getShareFolderIdByFolderId(T folderId) {
		if (!ready) {
			internalInitCache();
		} else {
			if (!folderToShareFolder.containsKey(folderId) && !folderToWildcardShareFolder.containsKey(folderId)){
				clear();
				internalInitCache();
			}
		}
		if (folderToShareFolder.containsKey(folderId)) return folderToShareFolder.get(folderId);
		if (folderToWildcardShareFolder.containsKey(folderId)) return folderToWildcardShareFolder.get(folderId);
		return null;
	}
	
	public final synchronized List<T> getFolderIds() {
		if (!ready) internalInitCache();
		return Collections.unmodifiableList(folderTo);
	}
	
	public final synchronized List<T> getFolderIdsByShareRoot(String shareRootId) {
		if (!ready) internalInitCache();
		final List<T> ids = new ArrayList<>();
		if (rootShareToFolderShare.containsKey(shareRootId)) ids.addAll(rootShareToFolderShare.get(shareRootId));
		return Collections.unmodifiableList(ids);
	}
	
	public final synchronized String getShareRootIdByFolderId(T folderId) {
		if (!ready) internalInitCache();
		for (R root : shareRoots) {
			Collection<T> folderIds = rootShareToFolderShare.get(root.getShareId());
			if (folderIds.contains(folderId)) return root.getShareId();
		}
		return null;
	}
}
