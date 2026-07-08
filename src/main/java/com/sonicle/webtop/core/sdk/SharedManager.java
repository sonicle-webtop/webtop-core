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
package com.sonicle.webtop.core.sdk;

/**
 * Opt-in marker + lifecycle interface for service Managers that must live as a
 * single shared instance per domain user (see {@code SharedManagerRegistry}),
 * instead of being cached per web session and re-instantiated by every
 * sessionless REST call.
 *
 * <p>A shared Manager is created lazily on first web/REST touch for a given
 * {@code UserProfileId}, reference-counted while web sessions (and, in future,
 * mobile-push subscriptions) reference it, and evicted after an idle grace
 * period once no references remain.</p>
 *
 * <p>{@link #onSharedStartup()} runs exactly once, in the thread of the caller
 * that first creates the instance (which carries that caller's security
 * context). {@link #onSharedShutdown()} runs exactly once at eviction/app
 * shutdown, typically from the registry sweeper thread bound to the admin
 * subject; implementations must not assume the original caller's context.</p>
 *
 * @author gbulfon
 */
public interface SharedManager {

	/**
	 * Invoked once, right after the shared instance is created and before it is
	 * handed to the first caller. Use it to start the long-lived machinery the
	 * instance owns (connections, caches, background/idle threads).
	 * <p>If this throws, the registry discards the half-created instance and
	 * propagates the failure to the caller; no partially-started instance is
	 * cached.</p>
	 */
	void onSharedStartup();

	/**
	 * Invoked once when the shared instance is evicted (no references left after
	 * the grace period) or on application shutdown. Use it to stop everything
	 * {@link #onSharedStartup()} started and release all resources. Must be
	 * idempotent-safe and must not throw for normal teardown paths.
	 */
	void onSharedShutdown();
}
