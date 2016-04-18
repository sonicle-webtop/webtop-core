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

import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.bol.OSnoozedReminder;
import com.sonicle.webtop.core.bol.js.JsReminderInApp;
import com.sonicle.webtop.core.bol.model.ReminderMessage;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobServiceTask;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.ReminderEmail;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.interfaces.IControllerHandlesReminders;
import com.sonicle.webtop.core.util.NotificationHelper;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.mail.internet.InternetAddress;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.LocalTime;
import org.jooq.tools.StringUtils;
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
	List<String> sidHandlingReminders = null;
	
	@Override
	public void initialize() throws Exception {
		core = WT.getCoreManager(getRunContext());
		sidHandlingReminders = core.getServiceManager().listServicesWhichControllerImplements(IControllerHandlesReminders.class);
	}

	@Override
	public void cleanup() throws Exception {
		sidHandlingReminders = null;
		core = null;
	}
	
	@Override
	public List<TaskDefinition> returnTasks() {
		ArrayList<TaskDefinition> tasks = new ArrayList<>();
		
		// Reminder task
		Trigger remTrigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?")) // every minute of the hour
				.build();
		tasks.add(new TaskDefinition(ReminderJob.class, remTrigger));
		
		// Device syncronization check task
		LocalTime time = new CoreServiceSettings(CoreManifest.ID, "*").getDevicesSyncCheckTime();
		Trigger syncTrigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(time.getHourOfDay(), time.getMinuteOfHour())) // every day at...
				.build();
		tasks.add(new TaskDefinition(DevicesSyncCheckJob.class, syncTrigger));
		
		return tasks;
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
			DateTime now = DateTime.now(DateTimeZone.UTC).withMillisOfSecond(0);
			
			logger.trace("ReminderJob started [{}]", now);
			
			try {
				ArrayList<BaseReminder> alerts = new ArrayList<>();
				
				// Creates a controller instance for each service and calls it for reminders...
				ServiceManager svcm = jobService.core.getServiceManager();
				for(String sid : jobService.sidHandlingReminders) {
					BaseController instance = svcm.instantiateController(sid, jobService.getRunContext());
					IControllerHandlesReminders controller = (IControllerHandlesReminders)instance;
					alerts.addAll(controller.returnReminders(now));
				}

				// Process returned reminders...
				logger.trace("Processing {} returned alerts", alerts.size());
				for(BaseReminder alert : alerts) {
					if(alert instanceof ReminderEmail) {
						sendEmail((ReminderEmail)alert);
						
					} else if(alert instanceof ReminderInApp) {
						ReminderMessage msg = new ReminderMessage(new JsReminderInApp((ReminderInApp)alert));
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
				logger.trace("Processing snoozed reminders");
				List<OSnoozedReminder> prems = jobService.core.listExpiredSnoozedReminders(now);
				for(OSnoozedReminder prem : prems) {
					UserProfile.Id pid = new UserProfile.Id(prem.getDomainId(), prem.getUserId());
					ReminderMessage msg = new ReminderMessage(new JsReminderInApp(prem));
					if(!byProfile.containsKey(pid)) {
						byProfile.put(pid, new ArrayList<ServiceMessage>());
					}
					byProfile.get(pid).add(msg);
				}
				
			} catch(WTException ex) {
				logger.error("Unable to process snoozed reminders", ex);
			}
			
			// Process messages...
			for(UserProfile.Id pid : byProfile.keySet()) {
				WT.nofity(pid, byProfile.get(pid), true);
			}
			
			logger.trace("ReminderJob finished [{}]", now);
		}
		
		private void sendEmail(ReminderEmail reminder) {
			try {
				UserProfile.Data ud = jobService.core.getUserData(reminder.getProfileId());
				InternetAddress from = WT.buildDomainInternetAddress(reminder.getProfileId().getDomainId(), "webtop-notification", null);
				if(from == null) throw new WTException("Error building sender address");
				InternetAddress to = ud.getEmail();
				if(to == null) throw new WTException("Error building destination address");
				WT.sendEmail(reminder.getRich(), from, to, reminder.getSubject(), reminder.getBody());
				
			} catch(Exception ex) {
				logger.error("Unable to send email", ex);
			}
		}
	}
	
	public static class DevicesSyncCheckJob extends BaseJobServiceTask {
		private JobService jobService = null;
		
		@Override
		public void setJobService(BaseJobService jobService) {
			// This method is automatically called by scheduler engine
			// while instantiating this task.
			this.jobService = (JobService)jobService;
		}
		
		@Override
		public void executeWork() {
			DateTime now = DateTime.now(DateTimeZone.UTC).withMillisOfSecond(0);
			List<SyncDevice> devices = null;
			
			logger.trace("DevicesSyncCheckJob started [{}]", now);
			try {
				List<UserProfile.Id> pids = jobService.core.listProfilesWithSetting(jobService.SERVICE_ID, CoreUserSettings.DEVICES_SYNC_ALERT_ENABLED, true);
				if(!pids.isEmpty()) devices = jobService.core.listZPushDevices();
				for(UserProfile.Id pid : pids) {
					// Skip profiles that don't have permission for syncing devices
					if(!WT.isPermitted(pid, jobService.SERVICE_ID, "DEVICES_SYNC", "ACCESS")) continue;
					
					UserProfile.Data ud = jobService.core.getUserData(pid);
					// Skip profiles that cannot receive email alerts
					if(ud.getEmail() == null) continue;
					
					int daysTolerance = new CoreUserSettings(pid).getDevicesSyncAlertTolerance();
					if(!checkSyncStatusForUser(devices, ud.getEmail().getAddress(), now, daysTolerance * 24)) {
						sendEmail(pid.getDomainId(), ud);
					}
				}
			} catch(WTException ex) {
				logger.error("Unable to check device sync status", ex);
			}
			logger.trace("DevicesSyncCheckJob finished [{}]", now);
		}
		
		private void sendEmail(String domainId, UserProfile.Data userData) {
			try {
				String mySubject = jobService.lookupResource(userData.getLocale(), CoreLocaleKey.DEVICESYNCCHECK_EMAIL_SUBJECT);
				String source = NotificationHelper.buildSource(userData.getLocale(), jobService.SERVICE_ID);
				String subject = NotificationHelper.buildSubject(userData.getLocale(), jobService.SERVICE_ID, mySubject);
				String bodyMessage = jobService.lookupResource(userData.getLocale(), CoreLocaleKey.DEVICESYNCCHECK_TPL_EMAIL_BODYMESSAGE);
				String html = NotificationHelper.buildNoReplayTpl(userData.getLocale(), source, null, bodyMessage);
				
				InternetAddress from = WT.buildDomainInternetAddress(domainId, "webtop-notification", null);
				if(from == null) throw new WTException("Error building sender address");
				InternetAddress to = userData.getEmail();
				if(to == null) throw new WTException("Error building destination address");
				WT.sendEmail(true, from, to, subject, html);
				
			} catch(IOException | TemplateException ex) {
				logger.error("Unable to build email template", ex);
			} catch(Exception ex) {
				logger.error("Unable to send email", ex);
			}
		}
		
		private boolean checkSyncStatusForUser(List<SyncDevice> devices, String email, DateTime now, int hours) {
			for(SyncDevice device : devices) {
				if(device.lastSync != null) {
					int hoursDiff = Hours.hoursBetween(device.lastSync, now).getHours();
					if(StringUtils.equals(device.user, email) && (hoursDiff > hours)) {
						return false;
					}
				}	
			}
			return true;
		}
	}
}
