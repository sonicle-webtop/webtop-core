/*
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.job;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.JobService;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OSnoozedReminder;
import com.sonicle.webtop.core.bol.js.JsReminderInApp;
import com.sonicle.webtop.core.bol.model.ReminderMessage;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobServiceTask;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ReminderEmail;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.internet.InternetAddress;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerRemindersHooks;

/**
 *
 * @author malbinola
 */
public class ReminderJob extends BaseJobServiceTask {
	private static final Logger logger = WT.getLogger(ReminderJob.class);
	private static final AtomicBoolean running = new AtomicBoolean(false);
	private JobService jobService = null;
	
	@Override
	public void setJobService(BaseJobService jobService) {
		this.jobService = (JobService)jobService;
	}

	@Override
	public void executeWork() {
		if (running.compareAndSet(false, true)) {
			DateTime now = DateTimeUtils.now();
			try {
				logger.debug("ReminderJob START [{}]", now);
				internalExecuteWork(now);
			} finally {
				logger.debug("ReminderJob END", now);
				running.set(false);
			}
		}
	}
	
	private void internalExecuteWork(DateTime now) {
		HashMap<UserProfileId, ArrayList<ServiceMessage>> byProfile = new HashMap<>();
		
		try {
			ArrayList<BaseReminder> alerts = new ArrayList<>();
			
			logger.debug("Collecting reminders...");
			ServiceManager svcMgr = jobService.getCoreManager().getServiceManager();
			for (String sid : jobService.getServiceIdsHandlingReminders()) {
				final BaseController instance = svcMgr.getController(sid);
				final IControllerRemindersHooks controller = (IControllerRemindersHooks)instance;
				final List<BaseReminder> svcAlerts = controller.returnReminders(now);
				logger.debug("{} -> {}", sid, svcAlerts.size());
				alerts.addAll(svcAlerts);
			}
			logger.debug("Collected {} reminders", alerts.size());

			if (!alerts.isEmpty()) {
				logger.debug("Preparing reminders...");
				for (BaseReminder alert : alerts) {
					if (alert instanceof ReminderEmail) {
						sendEmail((ReminderEmail)alert);

					} else if (alert instanceof ReminderInApp) {
						ReminderMessage msg = new ReminderMessage(new JsReminderInApp((ReminderInApp)alert));
						if (!byProfile.containsKey(alert.getProfileId())) {
							byProfile.put(alert.getProfileId(), new ArrayList<>());
						}
						byProfile.get(alert.getProfileId()).add(msg);
					}
				}
			}

		} catch(RuntimeException ex) {
			logger.error("Error processing reminders", ex);
		}
		
		try {
			logger.debug("Collecting snoozed reminders...");
			List<OSnoozedReminder> prems = jobService.getCoreManager().listExpiredSnoozedReminders(now);
			for (OSnoozedReminder prem : prems) {
				UserProfileId pid = new UserProfileId(prem.getDomainId(), prem.getUserId());
				ReminderMessage msg = new ReminderMessage(new JsReminderInApp(prem));
				if (!byProfile.containsKey(pid)) {
					byProfile.put(pid, new ArrayList<>());
				}
				byProfile.get(pid).add(msg);
			}

		} catch(WTException ex) {
			logger.error("Error processing snoozed reminders", ex);
		}

		// Process messages...
		for (UserProfileId pid : byProfile.keySet()) {
			WT.notify(pid, byProfile.get(pid), true);
		}
	}
	
	private void sendEmail(ReminderEmail reminder) {
		try {
			UserProfile.Data ud = WT.getUserData(reminder.getProfileId());
			InternetAddress from = WT.getNotificationAddress(reminder.getProfileId().getDomainId());
			if (from == null) throw new WTException("Error building sender address");
			InternetAddress to = ud.getPersonalEmail();
			if (to == null) throw new WTException("Error building destination address");
			WT.sendEmail(WT.getGlobalMailSession(reminder.getProfileId()), reminder.getRich(), from, to, reminder.getSubject(), reminder.getBody());

		} catch(Exception ex) {
			logger.error("Unable to send email", ex);
		}
	}
}
