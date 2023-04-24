/*
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.bg;

import com.sonicle.webtop.core.BackgroundService;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerRemindersHooks;
import com.sonicle.webtop.core.bol.OSnoozedReminder;
import com.sonicle.webtop.core.bol.js.JsReminderInApp;
import com.sonicle.webtop.core.bol.model.ReminderMessage;
import com.sonicle.webtop.core.sdk.BaseBackgroundServiceTask;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ReminderEmail;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class ReminderTask extends BaseBackgroundServiceTask {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(ReminderTask.class);
	
	@Override
	public Logger getLogger() {
		return LOGGER;
	}
	
	@Override
	public void executeWork(JobExecutionContext jec, BaseBackgroundServiceTask.TaskContext context) throws Exception {
		BackgroundService bs = ((BackgroundService)getBackgroundService(jec));
		CoreManager coreMgr = WT.getCoreManager();
		HashMap<UserProfileId, ArrayList<ServiceMessage>> byProfile = new HashMap<>();
		
		try {
			ArrayList<BaseReminder> alerts = new ArrayList<>();
			
			LOGGER.debug("Collecting reminders...");
			ServiceManager svcMgr = coreMgr.getServiceManager();
			for (String sid : bs.getServiceIdsHandlingReminders()) {
				if (shouldStop()) break; // Speed-up shutdown process!
				final BaseController instance = svcMgr.getController(sid);
				final IControllerRemindersHooks controller = (IControllerRemindersHooks)instance;
				final List<BaseReminder> svcAlerts = controller.returnReminders(context.getExecuteInstant());
				LOGGER.debug("{} -> {}", sid, svcAlerts.size());
				alerts.addAll(svcAlerts);
			}
			LOGGER.debug("Collected {} reminders", alerts.size());

			if (!alerts.isEmpty()) {
				LOGGER.debug("Preparing reminders...");
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

		} catch (Exception ex) {
			LOGGER.error("Error processing reminders", ex);
		}
		
		if (shouldStop()) return; // Speed-up shutdown process!
		
		try {
			LOGGER.debug("Collecting snoozed reminders...");
			List<OSnoozedReminder> prems = coreMgr.listExpiredSnoozedReminders(context.getExecuteInstant());
			for (OSnoozedReminder prem : prems) {
				UserProfileId pid = new UserProfileId(prem.getDomainId(), prem.getUserId());
				ReminderMessage msg = new ReminderMessage(new JsReminderInApp(prem));
				if (!byProfile.containsKey(pid)) {
					byProfile.put(pid, new ArrayList<>());
				}
				byProfile.get(pid).add(msg);
			}

		} catch (WTException ex) {
			LOGGER.error("Error processing snoozed reminders", ex);
		}

		// Process messages...
		for (UserProfileId pid : byProfile.keySet()) {
			if (shouldStop()) break; // Speed-up shutdown process!
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

		} catch (Exception ex) {
			LOGGER.error("Unable to send email", ex);
		}
	}
}
