/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.commons.ClassUtils;
import com.sonicle.webtop.core.app.exc.ManagerLifecycleException;
import com.sonicle.webtop.core.app.sdk.EventBase;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import net.sf.qualitycheck.Check;
import org.slf4j.Logger;

/**
 * Base class for application managers with explicit lifecycle state.
 * 
 * Suggested usage:
 * 
 *	 svcMgr = new ServiceManager(this, scheduler).initialize();
 * 
 * and on shutdown:
 * 
 *   if (svcMgr != null) {
 *     svcMgr = svcMgr.cleanup();
 *   }
 * 
 * @author malbinola
 * @param <T> Concrete manager type
 */
public abstract class AbstractAppManager<T extends AbstractAppManager<T>> {
	private final ReentrantLock lifecycleLock = new ReentrantLock();
	private volatile State state = State.NEW;
	private volatile Throwable failureCause;
	private WebTopApp wta;
	
	AbstractAppManager(WebTopApp wta) {
		this.wta = Check.notNull(wta, "wta");
	}
	
	protected abstract Logger doGetLogger();
	protected void doAppManagerInitialize() {};
	protected abstract void doAppManagerCleanup();
	
	protected final WebTopApp getWebTopApp() {
		return wta;
	}
	
	public final State getState() {
		return state;
	}
	
	public final boolean isStateReady() {
		return state == State.READY;
	}
	
	public final boolean isStateFailed() {
		return state == State.FAILED;
	}
	
	public final Throwable getFailureCause() {
		return failureCause;
	}
	
	protected EventBus.PostResult fireEvent(EventBase event) {
		return fireEvent(event, false, false);
	}
	
	protected EventBus.PostResult fireEvent(EventBase event, boolean trackHandlerErrors, boolean logTrackedErrors) {
		EventBus.PostResult result = wta.getEventBus().postNow(event, trackHandlerErrors);
		if (logTrackedErrors) logEventBusHandlerErrors(result);
		return result;
	}
	
	protected void logEventBusHandlerErrors(EventBus.PostResult result) {
		if (result.hasHandlerErrors()) {
			for (EventBus.HandlerError error : result.getHandlerErrors()) {
				Class clazz = error.getHandlerMethodDeclaringClass();
				String serviceId = WT.findServiceId(clazz);
				doGetLogger().error("[{}] {} -> {}", serviceId, ClassUtils.getSimpleClassName(clazz), error.getHandlerMethodName(), error.getDeepestCause());
			}
		}	
	}
	
	protected Connection getConnection(final boolean autoCommit) throws SQLException {
		return getWebTopApp().getConnectionManager().getConnection(autoCommit);
	}
	
	protected Connection getConnection(final String namespace) throws SQLException {
		return getWebTopApp().getConnectionManager().getConnection(namespace);
	}
	
	protected Connection getConnection(final String namespace, final boolean autoCommit) throws SQLException {
		return getWebTopApp().getConnectionManager().getConnection(namespace, autoCommit);
	}
	
	protected final void ensureStateReady() throws WTException {
		State s = this.state;
		if (s == State.READY) return;
		
		switch (s) {
			case NEW:
				throw new WTException("Manager is not initialized yet");
			case INITIALIZING:
				throw new WTException("Manager is initializing");
			case FAILED:
				throw new WTException(
                    "Manager initialization failed"
                    + ((failureCause != null) ? ": " + failureCause.getMessage() : "")
                );
			case CLEANING_UP:
				throw new WTException("Manager is shutting down");
			case CLEANED:
				throw new WTException("Manager has been cleaned up");
			default:
				throw new WTException("Manager cannot handle your request");
		}
	}
	
	/**
	 * Compatibility helper if you still have old code that does:
	 * Here there is no read lock anymore, so we only validate readiness.
	 * @deprecated
	 */
	@Deprecated protected final long readyLock() throws WTException {
		ensureStateReady();
		return 0L;
	}
	
	/**
	 * Compatibility no-op.
	 * @deprecated
	 */
	@Deprecated protected final void readyUnlock(long stamp) {
		// no-op
	}
	
	/**
	 * Initializes internal structures and marks this manager as READY.
	 * @return this manager instance for fluent assignment.
	 */
	final T initialize() throws ManagerLifecycleException {
		Check.notNull(doGetLogger(), "internalGetLogger()");
		try {
			lifecycleLock.lockInterruptibly();
			try {
				switch (state) {
					case READY:
						return (T) this;
					case INITIALIZING:
						throw new IllegalStateException("Manager already initializing");
					case CLEANING_UP:
					case CLEANED:
						throw new IllegalStateException("Manager has already been cleaned up");
					case FAILED:
					case NEW:
						break;
				}
				
				state = State.INITIALIZING;
				failureCause = null;
				
				long start = System.currentTimeMillis();
				try {
					doGetLogger().info("Initialization started...");
					doAppManagerInitialize();
					doGetLogger().info("Initialization completed in {} ms", System.currentTimeMillis() - start);
					state = State.READY;
					return (T) this;
					
				} catch (Exception ex1) {
					failureCause = ex1;
					state = State.FAILED;
					throw new ManagerLifecycleException("Unable to initialize '{}'", ClassUtils.getSimpleClassName(getClass()));
				}
				
			} finally {
				lifecycleLock.unlock();
			}
			
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new ManagerLifecycleException(ex, "Interrupted while acquiring lifecycle lock for '{}'", ClassUtils.getSimpleClassName(getClass()));
		}
	}
	
	/**
	 * Cleans up internal structures and marks this manager as CLEANED.
	 * Safe to call multiple times.
	 * This may be safely invoked even if initialization never completed.
	 * @return <code>null</code> reference for fluent assignment.
	 */
	final T cleanup() {
		Check.notNull(doGetLogger(), "internalGetLogger()");
		try {
			lifecycleLock.lockInterruptibly();
			try {
				if (state == State.CLEANED || state == State.CLEANING_UP) return (T) null;
				
				state = State.CLEANING_UP;
				long start = System.currentTimeMillis();
				try {
					doGetLogger().info("Clean-up started...");
					doAppManagerCleanup();
					doGetLogger().info("Clean-up completed in {} ms", System.currentTimeMillis() - start);
					
				} catch (Exception ex1) {
					doGetLogger().error("Error during clean-up", ex1);
					
				} finally {
					failureCause = null;
					wta = null;
					state = State.CLEANED;
				}
			} finally {
				lifecycleLock.unlock();
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new WTRuntimeException(ex, "Interrupted while acquiring lifecycle lock for '{0}'", getClass().getSimpleName());
		}
		return (T)null;
	}
	
	public static enum State {
		NEW, // Instance created but not initialized
		INITIALIZING, // Boostrap is in progress
		READY, // Instance ready to serve requests
		FAILED, // Initialization failed
		CLEANING_UP, // Cleanup process is in progress
		CLEANED; // Instance cleared/abandoned
	}
}
