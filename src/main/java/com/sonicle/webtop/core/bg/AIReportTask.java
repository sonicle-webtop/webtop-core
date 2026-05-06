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
package com.sonicle.webtop.core.bg;

import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.sdk.BaseBackgroundServiceTask;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIReportTask extends BaseBackgroundServiceTask {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AIReportTask.class);

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public void executeWork(JobExecutionContext jec, TaskContext context) throws Exception {
		final WebTopApp wta = WebTopApp.getInstance();
		final WebTopManager wtMgr = wta.getWebTopManager();
		
		Set<String> domainIds = wtMgr.listDomainIds(EnabledCond.ENABLED_ONLY);
		for (String domainId: domainIds) {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, domainId);
			String cadence = css.getAiReportCadence();
			String emailTo = css.getAiReportEmail();
			if (!StringUtils.isEmpty(cadence) && !StringUtils.isEmpty(emailTo) && !cadence.equals("none")) {
				boolean send = false;
				if (cadence.equals("daily")) send = true;
				else {
					LocalDate now = LocalDate.now();
					if (cadence.equals("weekly") && now.getDayOfWeek() == 1) send = true;
					else if (cadence.equals("monthly") && now.getDayOfMonth() == 1) send = true;
				}
				
				if (send) wtMgr.sendAIReportEmail(domainId);
			}
		}
	}
}
