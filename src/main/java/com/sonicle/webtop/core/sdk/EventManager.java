/*
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.sdk;

import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.sdk.BaseEvent;
import com.sonicle.webtop.core.app.sdk.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author malbinola
 */
public class EventManager {
	private final List<EventListener> listeners = new CopyOnWriteArrayList<>();
	private final Map<EventListener, Subject> subjects = new HashMap<>();
	
	public void addListener(final EventListener listener) {
		Subject subject = RunContext.getSubject();
		listeners.add(listener);
		if (subject != null) {
			if (subjects.put(listener, subject) != null) {
				throw new WTRuntimeException("Unexpected duplicated subject map [{}, {}]", listener, subject);
			}
		}
	}
	
	public void removeListener(final EventListener listener) {
		listeners.remove(listener);
		subjects.remove(listener);
	}
	
	public void fireEvent(final BaseEvent event) {
		for (Iterator<EventListener> i = listeners.iterator(); i.hasNext();) {
			EventListener listener = i.next();
			doFire(subjects.get(listener), listener, event);
		}
	}
	
	private void doFire(Subject subject, EventListener listener, BaseEvent event) {
		Runnable runnable = () -> {
			try {
				listener.onEvent(event);
			} catch (Throwable t) { /* Do nothing... */ }
		};
		
		if (subject != null) {
			subject.execute(runnable);
		} else {
			new Thread(runnable).start();
		}
	}
}
