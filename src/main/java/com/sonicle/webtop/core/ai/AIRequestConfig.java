/*
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable per-request configuration for an AI call.
 *
 * Why this exists: {@link AIManager} instances are cached per session in
 * {@link com.sonicle.webtop.core.app.WebTopSession#getAIManager()}. Having
 * a single long-lived manager means any per-call mutable state (temperature,
 * output format, RAG filters, ...) would race between concurrent calls from
 * the same session (two browser tabs, parallel AJAX). This class holds all
 * that state as an immutable value so each call is self-contained.
 *
 * Null-valued fields mean "use the manager's constructor default". Any
 * non-null value overrides. This lets subclasses ({@link OpenAIManager},
 * {@link ClaudeAIManager}, ...) set sensible per-backend defaults while
 * callers only specify the dimensions they care about.
 */
public final class AIRequestConfig {

	public static final AIRequestConfig DEFAULT = builder().build();

	private final String outputFormat;
	private final Float temperature;
	private final Integer maxTokens;
	private final String serviceId;
	private final String operation;
	private final Map<String, String> ragProperties;

	private AIRequestConfig(Builder b) {
		this.outputFormat = b.outputFormat;
		this.temperature = b.temperature;
		this.maxTokens = b.maxTokens;
		this.serviceId = b.serviceId;
		this.operation = b.operation;
		this.ragProperties = b.ragProperties == null || b.ragProperties.isEmpty()
			? Collections.<String, String>emptyMap()
			: Collections.unmodifiableMap(new HashMap<>(b.ragProperties));
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public Float getTemperature() {
		return temperature;
	}

	public Integer getMaxTokens() {
		return maxTokens;
	}

	/**
	 * WebTop service id of the calling service (e.g. {@code com.sonicle.webtop.mail}).
	 * Recorded on the usage row so reports can slice spend by service. Null
	 * means the call wasn't tagged.
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * Free-form tag identifying what the call is for (AI menu item id,
	 * "rag", "embedding", ...). Recorded on the usage row so reports can
	 * slice spend by feature. Null means "unknown".
	 */
	public String getOperation() {
		return operation;
	}

	public Map<String, String> getRagProperties() {
		return ragProperties;
	}

	public static Builder builder() {
		return new Builder();
	}

	public Builder toBuilder() {
		Builder b = new Builder();
		b.outputFormat = this.outputFormat;
		b.temperature = this.temperature;
		b.maxTokens = this.maxTokens;
		b.serviceId = this.serviceId;
		b.operation = this.operation;
		b.ragProperties = this.ragProperties.isEmpty() ? null : new HashMap<>(this.ragProperties);
		return b;
	}

	public static final class Builder {
		private String outputFormat;
		private Float temperature;
		private Integer maxTokens;
		private String serviceId;
		private String operation;
		private Map<String, String> ragProperties;

		public Builder outputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
			return this;
		}

		public Builder temperature(Float temperature) {
			this.temperature = temperature;
			return this;
		}

		public Builder temperature(float temperature) {
			this.temperature = Float.valueOf(temperature);
			return this;
		}

		public Builder maxTokens(Integer maxTokens) {
			this.maxTokens = maxTokens;
			return this;
		}

		public Builder maxTokens(int maxTokens) {
			this.maxTokens = Integer.valueOf(maxTokens);
			return this;
		}

		public Builder serviceId(String serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		public Builder operation(String operation) {
			this.operation = operation;
			return this;
		}

		public Builder ragProperty(String key, String value) {
			if (ragProperties == null) ragProperties = new HashMap<>();
			ragProperties.put(key, value);
			return this;
		}

		public Builder ragProperties(Map<String, String> props) {
			if (props == null) {
				this.ragProperties = null;
			} else {
				this.ragProperties = new HashMap<>(props);
			}
			return this;
		}

		public AIRequestConfig build() {
			return new AIRequestConfig(this);
		}
	}
}
