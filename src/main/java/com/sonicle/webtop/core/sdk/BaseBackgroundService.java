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

import com.sonicle.webtop.core.app.AbstractService;
import com.sonicle.webtop.core.util.LoggerUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public abstract class BaseBackgroundService extends AbstractService {
	private final static Logger LOGGER = (Logger)LoggerFactory.getLogger(BaseBackgroundService.class);
	private final AtomicBoolean configured = new AtomicBoolean(false);
	private Scheduler scheduler;
	private Subject subject;
	
	protected abstract Collection<TaskDefinition> createTasks();
	
	Subject getSubject() {
		return subject;
	}
	
	/*
	@Override
	public void initialize() throws Exception {
		
	}
	
	@Override
	public void cleanup() throws Exception {
		//scheduler.getListenerManager().addTriggerListener(this, GroupMatcher.jobGroupEquals(this.SERVICE_ID));
	}
	*/
	
	public final void configure(Scheduler scheduler, Subject subject) {
		if (configured.compareAndSet(false, true)) {
			this.scheduler = Check.notNull(scheduler, "scheduler");
			this.subject = Check.notNull(subject, "subject");
		}
	}
	
	public final boolean scheduleTasks() {
		Collection<TaskDefinition> defs = null;
		try {
			LoggerUtils.setContextDC(this.SERVICE_ID);
			defs = createTasks();
			
		} catch (Exception ex) {
			LOGGER.error("Fail to get task definitions", ex);
			return false;
		} finally {
			LoggerUtils.clearContextServiceDC();
		}
		
		boolean ret = unscheduleTasks();
		if (!ret) return false;
		
		if (defs != null) {
			for (TaskDefinition def : defs) {
				final JobDetail jobDetail = createJobDetail(def);
				final Trigger trigger = createTrigger(def);
				
				try {
					LOGGER.debug("Scheduling task '{}' into group '{}'", jobDetail.getKey().getName(), jobDetail.getKey().getGroup());
					scheduler.scheduleJob(jobDetail, trigger);
					LOGGER.debug("Task '{}' successfully scheduled", jobDetail.getKey().getName(), jobDetail.getKey().getGroup());
				} catch (SchedulerException ex) {
					LOGGER.error("Unable to schedule task '{}'", jobDetail.getKey().toString(), ex);
					return false;
				}
			}
		}
		return true;
	}
	
	public final boolean unscheduleTasks() {
		String group = this.SERVICE_ID;
		try {
			LOGGER.debug("Clearing tasks of group '{}'", group);
			Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
			scheduler.deleteJobs(new ArrayList<>(keys));
			LOGGER.debug("Tasks for group '{}' successfully cleared", group);
			return true;
			
		} catch (SchedulerException ex) {
			LOGGER.error("Unable to delete tasks of group '{}'", group, ex);
			return false;
		}
	}
	
	private JobDetail createJobDetail(final TaskDefinition taskDef) {
		String classBaseName = taskDef.getTaskClass().getSimpleName();
		JobDataMap data = (taskDef.getData() != null) ? taskDef.getData() : new JobDataMap();
		data.put("backgroundService", this);
		
		JobBuilder jb = JobBuilder.newJob(taskDef.getTaskClass())
			.usingJobData(data)
			.withIdentity(classBaseName, this.SERVICE_ID);
		
		if (!StringUtils.isEmpty(taskDef.getDescription())) {
			jb.withDescription(taskDef.getDescription());
		}
		return jb.build();
	}
	
	private Trigger createTrigger(final TaskDefinition taskDef) {
		return taskDef.getTrigger().getTriggerBuilder()
			.withIdentity(taskDef.getTaskClass().getSimpleName(), this.SERVICE_ID)
			.startNow()
			.build();
	}
	
	protected static class TaskDefinition {
		private final Class<? extends BaseBackgroundServiceTask> clazz;
		private final JobDataMap data;
		private final Trigger trigger;
		private final String description;
		
		public TaskDefinition(final Class<? extends BaseBackgroundServiceTask> clazz, final Trigger trigger) {
			this(clazz, null, trigger, null);
		}
		
		public TaskDefinition(final Class clazz, final JobDataMap data, final Trigger trigger) {
			this(clazz, data, trigger, null);
		}
		
		public TaskDefinition(final Class clazz, final JobDataMap data, final Trigger trigger, final String description) {
			this.clazz = Check.notNull(clazz, "clazz");
			this.data = data;
			this.trigger = Check.notNull(trigger , "trigger");
			this.description = description;
		}

		public Class<? extends BaseBackgroundServiceTask> getTaskClass() {
			return clazz;
		}

		public JobDataMap getData() {
			return data;
		}

		public Trigger getTrigger() {
			return trigger;
		}

		public String getDescription() {
			return description;
		}
	}
}
