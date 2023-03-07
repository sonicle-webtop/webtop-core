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
package com.sonicle.webtop.core.app;

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.app.model.HomedThrowable;
import com.sonicle.webtop.core.app.sdk.EventBase;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 *
 * @author malbinola
 */
public class EventBus {
	//private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(EventBus.class);
	private final CachedErrorHandler errorCollector;
	private final MBassador<EventBase> bus;
	
	EventBus() {
		Properties props = WT.getProperties();
		this.errorCollector = new CachedErrorHandler();
		this.bus = new MBassador<>(new BusConfiguration()
			.addFeature(Feature.SyncPubSub.Default())
			.addFeature(Feature.AsynchronousHandlerInvocation.Default(WebTopProps.getEventBusMinThreads(props), WebTopProps.getEventBusMaxThreads(props)))
			.addFeature(Feature.AsynchronousMessageDispatch.Default())
			.addPublicationErrorHandler(errorCollector)
		);
	}
	
	void shutdown() {
		bus.shutdown();
	}
	
	public void subscribe(final Object listener) {
		bus.subscribe(listener);
	}
	
	public void unsubscribe(final Object listener) {
		bus.unsubscribe(listener);
	}
	
	public PostResult postNow(final EventBase event) {
		return postNow(event, false);
	}
	
	public PostResult postNow(final EventBase event, final boolean trackHandlerErrors) {
		if (trackHandlerErrors) errorCollector.startTrackingEvent(event);
		IMessagePublication publication = bus.post(event).now();
		return new PostResult(publication, trackHandlerErrors ? errorCollector.stopTrackingEvent(event) : null);
	}
	
	private class CachedErrorHandler implements IPublicationErrorHandler {
		private final HashSet<EventBase> trackedEvents = new HashSet<>();
		private final MultiValuedMap<EventBase, HandlerError> publicationErrors = new ArrayListValuedHashMap<>();
		
		public void startTrackingEvent(final EventBase event) {
			trackedEvents.add(event);
		}
		
		public Collection<HandlerError> stopTrackingEvent(final EventBase event) {
			final boolean removed = trackedEvents.remove(event);
			return removed ? publicationErrors.remove(event) : null;
		}

		@Override
		public void handleError(PublicationError pe) {
			final EventBase event = (EventBase)pe.getPublishedMessage();
			if (trackedEvents.contains(event)) {
				publicationErrors.put(event, new HandlerError(pe));
			}
		}
	}
	
	public static class PostResult {
		private final IMessagePublication publication;
		private final Collection<HandlerError> handlerErrors;
		
		PostResult(IMessagePublication publication, Collection<HandlerError> handlerErrors) {
			this.publication = publication;
			this.handlerErrors = handlerErrors;
		}
		
		public boolean isRunning() {
			return publication.isRunning();
		}
		
		public boolean isFinished() {
			return publication.isFinished();
		}
		
		public boolean hasPublicationError() {
			return publication.hasError();
		}
		
		public boolean hasHandlerErrors() {
			return handlerErrors != null && !handlerErrors.isEmpty();
		}
		
		public Collection<HandlerError> getHandlerErrors() {
			return (handlerErrors != null) ? Collections.unmodifiableCollection(handlerErrors) : null;
		}
		
		public Collection<HomedThrowable> getHandlerErrorsCauses() {
			return getHandlerErrorsCauses(false);
		}
		
		public Collection<HomedThrowable> getHandlerErrorsCauses(boolean deepest) {
			if (handlerErrors == null) return null;
			ArrayList<HomedThrowable> causes = new ArrayList<>();
			for (HandlerError he : handlerErrors) {
				final Throwable cause = deepest ? he.getDeepestCause() : he.getHighestCause();
				if (cause != null) {
					final Class clazz = he.getHandlerMethodDeclaringClass();
					final String serviceId = WT.findServiceId(clazz);
					causes.add(new HomedThrowable(serviceId, cause));
				}
			}
			return Collections.unmodifiableCollection(causes);
		}
	}
	
	public static class HandlerError {
		private final PublicationError publicationError;
		
		HandlerError(PublicationError publicationError) {
			this.publicationError = publicationError;
		}
		
		public Method getHandlerMethod() {
			return publicationError.getHandler();
		}
		
		public String getHandlerMethodName() {
			return getHandlerMethod().getName();
		}
		
		public Class<?> getHandlerMethodDeclaringClass() {
			return getHandlerMethod().getDeclaringClass();
		}
		
		public Throwable getHighestCause() {
			final Throwable cause = publicationError.getCause();
			if (cause != null && cause instanceof java.lang.reflect.InvocationTargetException) {
				// If cause is an InvocationTargetException (created by mbassador and i don't know why), work on the target exception instead!
				return ((java.lang.reflect.InvocationTargetException)cause).getTargetException();
			} else {
				return cause;
			}
		}
		
		public Throwable getDeepestCause() {
			final Throwable cause = publicationError.getCause();
			if (cause != null && cause instanceof java.lang.reflect.InvocationTargetException) {
				// If cause is an InvocationTargetException (created by mbassador and i don't know why), work on the target exception instead!
				return LangUtils.getDeepestCause(((java.lang.reflect.InvocationTargetException)cause).getTargetException());
			} else {
				return LangUtils.getDeepestCause(cause);
			}
			//return LangUtils.getDeepestCause(publicationError.getCause());
		}
	}
}
