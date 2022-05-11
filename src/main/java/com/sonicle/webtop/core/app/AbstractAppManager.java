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
package com.sonicle.webtop.core.app;

import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 * @param <T>
 */
public abstract class AbstractAppManager<T> {
	private final ReentrantReadWriteLock readyLock = new ReentrantReadWriteLock();
	private boolean ready = false;
	private WebTopApp wta;
	
	AbstractAppManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	protected abstract Logger internalGetLogger();
	protected abstract void internalAppManagerCleanup();
	
	protected void initialized() {
		this.ready = true;
		internalGetLogger().info("Initialized");
	}
	
	protected WebTopApp getWebTopApp() {
		return wta;
	}
	
	protected Connection getConnection(final String namespace) throws SQLException {
		return getWebTopApp().getConnectionManager().getConnection(namespace);
	}
	
	protected Connection getConnection(final String namespace, final boolean autoCommit) throws SQLException {
		return getWebTopApp().getConnectionManager().getConnection(namespace, autoCommit);
	}
	
	protected void readyLock() throws WTException {
		try {
			readyLock.readLock().lockInterruptibly();
			if (!ready) throw new WTException("Manager cannot handle your request");
			
		} catch (InterruptedException ex) {
			throw new WTException(ex);
		}
	}
	
	protected void readyUnlock() {
		readyLock.readLock().unlock();
	}
	
	/**
	 * Clears internal structures marking this Manager as NOT ready.
	 * @return Always return `null` reference, for clearing reference easily.
	 */
	T cleanup() {
		readyLock.writeLock().lock();
		try {
			this.ready = false;
			internalAppManagerCleanup();
		} finally {
			this.wta = null;
			readyLock.writeLock().unlock();
			internalGetLogger().info("Cleaned up");
		}
		return (T)null;
	}
}
