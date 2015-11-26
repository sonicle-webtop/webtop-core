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
package com.sonicle.webtop.core;

import com.sonicle.webtop.core.bol.OPostponedReminder;
import com.sonicle.webtop.core.bol.js.JsReminderAlert;
import com.sonicle.webtop.core.bol.model.ReminderMessage;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobServiceTask;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.IManagerHandleReminders;
import com.sonicle.webtop.core.sdk.ReminderAlert;
import com.sonicle.webtop.core.sdk.ReminderAlertWeb;
import com.sonicle.webtop.core.sdk.ReminderAlertEmail;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class JobService extends BaseJobService {
	private static final Logger logger = WT.getLogger(JobService.class);
	CoreManager core = null;
	List<String> sidUsingReminders = null;
	
	@Override
	public void initialize() throws Exception {
		core = WT.getCoreManager(getRunContext());
		sidUsingReminders = core.getServiceManager().listServicesWhichManagerImplements(IManagerHandleReminders.class);
	}

	@Override
	public void cleanup() throws Exception {
		sidUsingReminders = null;
		core = null;
	}
	
	@Override
	public List<TaskDefinition> returnTasks() {
		ArrayList<TaskDefinition> jobs = new ArrayList<>();
		
		// Reminder job
		Trigger rjTrigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?")) // every minute of the hour
				.build();
		jobs.add(new TaskDefinition(ReminderJob.class, rjTrigger));
		
		return jobs;
	}
	
	public static class ReminderJob extends BaseJobServiceTask {
		private JobService jobService = null;
		
		@Override
		public void setJobService(BaseJobService jobService) {
			// This method is automatically called by scheduler engine
			// while instantiating this task.
			this.jobService = (JobService)jobService;
		}
		
		@Override
		public void executeWork() {
			HashMap<UserProfile.Id, ArrayList<ServiceMessage>> byProfile = new HashMap<>();
			DateTime now = DateTime.now(DateTimeZone.UTC);
			
			logger.trace("ReminderJob started [{}]", now);
			
			try {
				ArrayList<ReminderAlert> alerts = new ArrayList<>();

				// Creates a manager instance for each service and calls it for reminders...
				for(String sid : jobService.sidUsingReminders) {
					BaseManager instance = jobService.core.getServiceManager().instantiateManager(sid, jobService.getRunContext());
					IManagerHandleReminders manager = (IManagerHandleReminders)instance;
					alerts.addAll(manager.returnReminderAlerts(now));
				}

				// Process returned reminders...
				logger.trace("Processing {} returned alerts", alerts.size());
				for(ReminderAlert alert : alerts) {
					if(alert instanceof ReminderAlertEmail) {
						//TODO: inviare notifica per email

					} else if(alert instanceof ReminderAlertWeb) {
						ReminderMessage msg = new ReminderMessage(new JsReminderAlert((ReminderAlertWeb)alert));
						if(!byProfile.containsKey(alert.getProfileId())) {
							byProfile.put(alert.getProfileId(), new ArrayList<ServiceMessage>());
						}
						byProfile.get(alert.getProfileId()).add(msg);
					}
				}
				
			} catch(RuntimeException ex) {
				logger.error("Unable to process service reminders", ex);
			}
			
			try {
				logger.trace("Processing postponed reminders");
				List<OPostponedReminder> prems = jobService.core.listExpiredPostponedReminders(now);
				for(OPostponedReminder prem : prems) {
					UserProfile.Id pid = new UserProfile.Id(prem.getDomainId(), prem.getUserId());
					ReminderMessage msg = new ReminderMessage(new JsReminderAlert(prem));
					if(!byProfile.containsKey(pid)) {
						byProfile.put(pid, new ArrayList<ServiceMessage>());
					}
					byProfile.get(pid).add(msg);
				}
				
			} catch(WTException ex) {
				logger.error("Unable to process postponed reminders", ex);
			}
			
			// Process messages...
			for(UserProfile.Id pid : byProfile.keySet()) {
				WT.nofity(pid, byProfile.get(pid));
			}
			
			logger.trace("ReminderJob finished");
		}
	}
}
