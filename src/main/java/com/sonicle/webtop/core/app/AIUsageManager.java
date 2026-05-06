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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.ai.AIQuotaExceededException;
import com.sonicle.webtop.core.ai.AIUsageRecorder;
import com.sonicle.webtop.core.bol.OAIUsage;
import com.sonicle.webtop.core.dal.AIUsageDAO;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists AI usage rows and keeps an in-memory per-user counter of tokens
 * used "today" (where "today" is interpreted in the user's domain timezone).
 *
 * The counter is the input the future per-user/global daily-token-cap will
 * read — keeping it in memory avoids a sum query on every AI call. Entries
 * roll over implicitly: each entry stamps the LocalDate it was seeded for,
 * and on every read/write we compare that date to today (in the domain tz).
 * If they differ, the entry is re-seeded from the database, which also
 * covers "first call after JVM restart" via the same code path.
 *
 * Seed source: {@link AIUsageDAO#sumTokensSince}, which sums total_tokens
 * for one user since today's local-midnight. Failed calls whose backend
 * still reported tokens count toward the total — the cap reflects what
 * was actually billed, not what succeeded end-to-end.
 *
 * Inactive users' entries linger across days (negligible memory footprint
 * per entry). Active sweeping can be added later if needed.
 */
public class AIUsageManager extends AbstractAppManager<AIUsageManager> implements AIUsageRecorder {
	private static final Logger LOGGER = LoggerFactory.getLogger(AIUsageManager.class);

	private final ConcurrentHashMap<UserProfileId, DailyCounter> counters = new ConcurrentHashMap<>();

	AIUsageManager(WebTopApp wta) {
		super(wta);
	}

	@Override
	protected Logger doGetLogger() {
		return LOGGER;
	}

	@Override
	protected void doAppManagerCleanup() {
		counters.clear();
	}

	/**
	 * Persist a usage row and, if it carries a non-null total_tokens count,
	 * add it to the per-user daily counter. DB failures are logged and
	 * swallowed so the AI request that triggered the recording is never
	 * affected; the in-memory counter is still incremented so today's
	 * remaining quota stays accurate against what the backend just charged.
	 */
	@Override
	public void record(OAIUsage usage) {
		if (usage == null) return;
		UserProfileId pid = usage.getProfileId();
		Integer total = usage.getTotalTokens();

		try {
			persist(usage);
		} catch (Throwable t) {
			LOGGER.error("Unable to persist AI usage row for {}", pid, t);
		}

		if (total != null && total > 0) {
			incrementToday(pid, total.longValue());
		}
	}

	/**
	 * Resolves the effective daily token cap for the user (per-user override
	 * or domain default) and throws {@link AIQuotaExceededException} when
	 * today's usage already meets or exceeds it. A cap of 0 (or negative)
	 * disables the gate. Call this immediately before invoking the AI
	 * backend so the typed exception can flow back to the caller.
	 */
	public void enforceQuota(UserProfileId profileId) throws AIQuotaExceededException {
		if (profileId == null) return;
		int max;
		try {
			CoreUserSettings cus = new CoreUserSettings(profileId);
			if (cus.getAiApiBackendUserOverride()!=null && cus.getAiApiTokenUserOverride()!=null)
				max = cus.getAiUserMaxTokens();
			else
				max = cus.getAiDomainMaxTokens();
		} catch (Throwable t) {
			LOGGER.warn("Unable to read AI quota for {} (assuming uncapped)", profileId, t);
			return;
		}
		if (max <= 0) return;
		long used = getTodayTokens(profileId);
		if (used >= max) throw new AIQuotaExceededException(used, max);
	}

	/**
	 * Returns the number of tokens this user has consumed since local
	 * midnight in their domain's timezone. Seeds from the database on
	 * first read after startup or after the day rolled over. Returns 0
	 * if the seed query fails (best-effort — the cap will simply allow
	 * one more call than strictly intended in that rare case).
	 */
	public long getTodayTokens(UserProfileId profileId) {
		if (profileId == null) return 0L;
		DateTimeZone tz = resolveDomainTimeZone(profileId.getDomainId());
		LocalDate today = LocalDate.now(tz);
		DailyCounter c = counters.compute(profileId, (k, existing) -> {
			if (existing != null && today.equals(existing.date)) return existing;
			long seed = seedFromDb(k, today, tz);
			return new DailyCounter(today, new AtomicLong(seed));
		});
		return c.tokens.get();
	}

	private void incrementToday(UserProfileId profileId, long delta) {
		DateTimeZone tz = resolveDomainTimeZone(profileId.getDomainId());
		LocalDate today = LocalDate.now(tz);
		DailyCounter c = counters.compute(profileId, (k, existing) -> {
			if (existing != null && today.equals(existing.date)) return existing;
			long seed = seedFromDb(k, today, tz);
			return new DailyCounter(today, new AtomicLong(seed));
		});
		c.tokens.addAndGet(delta);
	}

	private long seedFromDb(UserProfileId profileId, LocalDate today, DateTimeZone tz) {
		DateTime startOfDay = today.toDateTimeAtStartOfDay(tz);
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			return AIUsageDAO.getInstance().sumTokensSince(con, profileId.getDomainId(), profileId.getUserId(), startOfDay);
		} catch (Throwable t) {
			LOGGER.warn("Unable to seed AI daily counter for {} (assuming 0)", profileId, t);
			return 0L;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	private void persist(OAIUsage usage) throws Exception {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			AIUsageDAO.getInstance().insert(con, usage);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	private DateTimeZone resolveDomainTimeZone(String domainId) {
		try {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, domainId);
			return DateTimeZone.forID(css.getDefaultTimezone());
		} catch (Throwable t) {
			LOGGER.warn("Unable to resolve domain timezone for {}, falling back to UTC", domainId, t);
			return DateTimeZone.UTC;
		}
	}

	private static final class DailyCounter {
		final LocalDate date;
		final AtomicLong tokens;
		DailyCounter(LocalDate date, AtomicLong tokens) {
			this.date = date;
			this.tokens = tokens;
		}
	}
}
