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
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.JobService;
import com.sonicle.webtop.core.TplHelper;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobServiceTask;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.NotificationHelper;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class DevicesSyncCheckJob extends BaseJobServiceTask {
	private static final Logger logger = WT.getLogger(DevicesSyncCheckJob.class);
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
				logger.debug("DevicesSyncCheckJob START [{}]", now);
				internalExecuteWork(now);
			} finally {
				logger.debug("DevicesSyncCheckJob END", now);
				running.set(false);
			}
		}
	}
	
	private void internalExecuteWork(DateTime now) {
		try {
			logger.debug("Collecting profiles to check...");
			List<UserProfileId> pids = getProfilesToCheck();
			if (pids == null) return;
			
			pids = pids.stream()
					.filter(profileId -> RunContext.isPermitted(true, profileId, jobService.SERVICE_ID, "DEVICES_SYNC", "ACCESS"))
					.collect(Collectors.toList());
			
			logger.debug("Found {} profiles", pids.size());
			if (pids.isEmpty()) return;
			
			List<SyncDevice> devices = jobService.getCoreManager().listZPushDevices();
			logger.debug("Working on profiles...");
			for (UserProfileId pid : pids) {
				UserProfile.Data ud = WT.getUserData(pid);
				if (ud.getPersonalEmail() == null) continue; // Skip profiles that cannot receive email alerts
				logger.debug("Working on profile [{}, {}]", pid.toString(), ud.getPersonalEmailAddress());
				
				int daysTolerance = new CoreUserSettings(pid).getDevicesSyncAlertTolerance();
				if (!checkSyncStatusForUser(devices, ud.getProfileEmailAddress(), now, daysTolerance * 24)) {
					sendEmail(pid, ud);
				}
			}
		} catch(WTException ex) {
			logger.error("Unable to check device sync status", ex);
		}
	}
	
	private List<UserProfileId> getProfilesToCheck() {
		SettingsManager setMgr = getSettingsManager();
		return (setMgr == null) ? null : setMgr.listProfilesWith(jobService.SERVICE_ID, CoreSettings.DEVICES_SYNC_ALERT_ENABLED, true);
	}
	
	private SettingsManager getSettingsManager() {
		WebTopApp wta = WebTopApp.getInstance();
		return (wta != null) ? wta.getSettingsManager() : null;
	}
	
	private boolean checkSyncStatusForUser(List<SyncDevice> devices, String currentUserEmail, DateTime now, int hours) {
		for (SyncDevice device : devices) {
			if (device.lastSync != null) {
				int hoursDiff = Hours.hoursBetween(device.lastSync, now).getHours();
				if (StringUtils.equals(device.user, currentUserEmail) && (hoursDiff > hours)) return false;
			}	
		}
		return true;
	}
	
	private void sendEmail(UserProfileId pid, UserProfile.Data userData) {
		try {
			String bodyHeader = jobService.lookupResource(userData.getLocale(), CoreLocaleKey.TPL_EMAIL_DEVICESYNCCHECK_BODY_HEADER);
			String subject = NotificationHelper.buildSubject(userData.getLocale(), jobService.SERVICE_ID, bodyHeader);
			String html = TplHelper.buildDeviceSyncCheckEmail(userData.getLocale());

			InternetAddress from = WT.getNotificationAddress(pid.getDomainId());
			if(from == null) throw new WTException("Error building sender address");
			InternetAddress to = userData.getPersonalEmail();
			if(to == null) throw new WTException("Error building destination address");
			WT.sendEmail(WT.getGlobalMailSession(pid), true, from, to, subject, html);

		} catch(IOException | TemplateException ex) {
			logger.error("Unable to build email template", ex);
		} catch(Exception ex) {
			logger.error("Unable to send email", ex);
		}
	}
}
