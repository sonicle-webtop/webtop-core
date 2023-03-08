/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.util.LoggerUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public abstract class BaseBackgroundServiceTask implements Job {
	private static final AtomicBoolean running = new AtomicBoolean(false);
	
	public abstract Logger getLogger();
	//public abstract String getName();
	public abstract void executeWork(JobExecutionContext jec, TaskContext context) throws Exception;

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		if (!WebTopApp.isShuttingDown()) {
			if (WebTopApp.isWebappTheLatest()) {
				if (running.compareAndSet(false, true)) {
					try {
						Subject subject = getBackgroundService(jec).getSubject();
						ThreadState threadState = new SubjectThreadState(subject);
						LoggerUtils.initDC();

						try {
							threadState.bind();
							doExecuteWork(jec);
						} finally {
							threadState.clear();
						}
					} finally {
						LoggerUtils.clearDC();
						running.set(false);
					}
				}
			} else {
				WebTopApp.getInstance().getServiceManager().unscheduleAllBackgroundServicesTasks();
			}	
		}
	}
	
	protected void doExecuteWork(JobExecutionContext jec) {
		DateTime now = DateTimeUtils.now();

		try {
			getLogger().debug("Started [{}]", now);
			executeWork(jec, new TaskContext(getBackgroundService(jec), now));
			
		} catch (Exception ex) {
			getLogger().error("Error", ex);
		} finally {
			getLogger().debug("Ended [{}]", now);
		}
	}
	
	protected final BaseBackgroundService getBackgroundService(JobExecutionContext jec) {
		return (BaseBackgroundService)jec.getMergedJobDataMap().get("backgroundService");
	}
	
	protected final boolean shouldStop() {
		return WebTopApp.isShuttingDown();
	}
	
	public static class TaskContext {
		private final BaseBackgroundService backgroundService;
		private final DateTime executeInstant;
		
		public TaskContext(BaseBackgroundService backgroundService, DateTime executeInstant) {
			this.backgroundService = backgroundService;
			this.executeInstant = executeInstant;
		}
		
		public BaseBackgroundService getBackgroundService() {
			return backgroundService;
		}

		public DateTime getExecuteInstant() {
			return executeInstant;
		}
	}
}
