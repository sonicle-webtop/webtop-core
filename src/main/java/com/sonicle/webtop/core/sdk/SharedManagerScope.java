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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares WHICH consumers of a {@link SharedManager} service are routed to the
 * app-level per-user shared instance. Absent annotation means {@link Scope#ALL}
 * (the original shared-manager behavior).
 *
 * <p>{@link Scope#SESSIONLESS_ONLY} is the "hybrid" model: web sessions build
 * and own a private per-session Manager (historical lifecycle: re-login yields
 * a fresh instance, the session tears it down at logout), while sessionless
 * consumers — REST calls and mobile-push subscriptions — share the registry
 * instance. Designed for machinery-heavy services (mail) where session and app
 * worlds want independent state lifecycles.</p>
 *
 * @author gbulfon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SharedManagerScope {

	Scope value();

	public enum Scope {
		/** Every consumer (web sessions, REST, subscriptions) shares the registry instance. */
		ALL,
		/** Only sessionless consumers (REST, subscriptions) share it; web sessions keep private per-session instances. */
		SESSIONLESS_ONLY
	}
}
