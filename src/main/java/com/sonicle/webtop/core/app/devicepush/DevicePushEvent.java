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
package com.sonicle.webtop.core.app.devicepush;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A service-originated event to deliver to a user's mobile devices through
 * {@link DevicePushManager}. The producing service owns the content
 * ({@link #field}: copied as-is at the top level of the outbound frame, in
 * insertion order); core owns transport, routing and the delivery guarantees
 * selected by {@link DeliveryClass}.
 */
public final class DevicePushEvent {

	/**
	 * How much the transport may sacrifice under pressure.
	 */
	public static enum DeliveryClass {
		/**
		 * A cumulative value (e.g. an unread count): only the latest per
		 * {@link #getCoalesceKey() key} matters, older pending ones are replaced.
		 */
		STATE,
		/**
		 * An incremental change (e.g. flags changed, messages deleted): when the
		 * transport is saturated it is degraded to a single per-key frame
		 * stripped of its {@link #degradeDrops detail fields}, which clients
		 * read as "refresh this scope".
		 */
		DELTA,
		/**
		 * A user-visible notification: never displaced by the other classes,
		 * retried across reconnects until its {@link #ttl} expires.
		 */
		ALERT
	}

	private final String serviceId;
	private final String type;
	private final DeliveryClass deliveryClass;
	private final JsonObject fields = new JsonObject();
	private final Set<String> degradeDropFields = new LinkedHashSet<>();
	private String coalesceKey = "";
	private long ttlMillis = 0;

	public DevicePushEvent(final String serviceId, final String type, final DeliveryClass deliveryClass) {
		if (serviceId == null || type == null || deliveryClass == null) throw new IllegalArgumentException("serviceId, type and deliveryClass are mandatory");
		this.serviceId = serviceId;
		this.type = type;
		this.deliveryClass = deliveryClass;
	}

	/**
	 * Scope the event refers to inside the service (e.g. account + folder).
	 * Drives STATE replacement and DELTA degradation; ignored for ALERT.
	 */
	public DevicePushEvent coalesceKey(final String coalesceKey) {
		this.coalesceKey = (coalesceKey != null) ? coalesceKey : "";
		return this;
	}

	/**
	 * Maximum time the event is worth delivering, 0 (default) for no limit.
	 */
	public DevicePushEvent ttl(final long ttlMillis) {
		this.ttlMillis = Math.max(0, ttlMillis);
		return this;
	}

	/**
	 * Names the fields removed when a DELTA is degraded.
	 */
	public DevicePushEvent degradeDrops(final String... fieldNames) {
		Collections.addAll(degradeDropFields, fieldNames);
		return this;
	}

	public DevicePushEvent field(final String name, final String value) {
		if (value == null) fields.add(name, JsonNull.INSTANCE);
		else fields.addProperty(name, value);
		return this;
	}

	public DevicePushEvent field(final String name, final Number value) {
		if (value == null) fields.add(name, JsonNull.INSTANCE);
		else fields.addProperty(name, value);
		return this;
	}

	public DevicePushEvent field(final String name, final JsonElement value) {
		fields.add(name, (value != null) ? value : JsonNull.INSTANCE);
		return this;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getType() {
		return type;
	}

	public DeliveryClass getDeliveryClass() {
		return deliveryClass;
	}

	public String getCoalesceKey() {
		return coalesceKey;
	}

	public long getTtlMillis() {
		return ttlMillis;
	}

	JsonObject getFields() {
		return fields;
	}

	Set<String> getDegradeDropFields() {
		return degradeDropFields;
	}
}
