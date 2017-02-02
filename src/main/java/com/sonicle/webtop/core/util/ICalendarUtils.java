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
package com.sonicle.webtop.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ICalendarUtils {
	
	public static void setUnfoldingRelaxed(boolean value) {
		System.setProperty("ical4j.unfolding.relaxed", String.valueOf(value));
	}
	
	public static void setParsingRelaxed(boolean value) {
		System.setProperty("ical4j.parsing.relaxed", String.valueOf(value));
	}
	
	public static void setValidationRelaxed(boolean value) {
		System.setProperty("ical4j.validation.relaxed", String.valueOf(value));
	}
	
	public static void setCompatibilityOutlook(boolean value) {
		System.setProperty("ical4j.compatibility.outlook", String.valueOf(value));
	}
	
	public static void setCompatibilityNotes(boolean value) {
		System.setProperty("ical4j.compatibility.notes", String.valueOf(value));
	}
	
	public static Calendar parseRelaxed(InputStream is) throws ParserException, IOException {
		setUnfoldingRelaxed(true);
		setParsingRelaxed(true);
		setValidationRelaxed(true);
		return parse(is);
	}
	
	public static Calendar parse(InputStream is) throws ParserException, IOException {
		CalendarBuilder builder = new CalendarBuilder();
		return builder.build(is);
	}
	
	/**
	 * Extract the first VEvent's from a calendar object.
	 * @param ical The Calendar object
	 * @return The first VEvent object or null if not found
	 */
	public static VEvent getVEvent(Calendar ical) {
		ComponentList cl = ical.getComponents();
		for (Iterator cIt = cl.iterator(); cIt.hasNext();) {
			Component component = (Component) cIt.next();
			if (component instanceof VEvent) return (VEvent)component;
		}
		return null;
	}
	
	/**
	 * Extract VEvent's uid from a calendar object.
	 * Only the first VEvent element will be taken into account.
	 * This method should be used only on an invitation object (that 
	 * brings with it only a single VEvent).
	 * @param ical The Calendar object
	 * @return The VEvent's Uid or null if not found
	 */
	public static String getInvitationUid(Calendar ical) {
		VEvent ve = getVEvent(ical);
		return (ve == null) ? null : ve.getUid().getValue();
	}
	
	public static String buildProdId(String company, String product) {
		return "-//" + company + "//" + product + "//EN";
	}
	
	public static String buildUid(String left, String host) {
		return left + "@" + host;
	}
	
	public static Calendar buildInvitationReply(Calendar ical, String forAddress, PartStat response) throws URISyntaxException, ParseException, IOException {
		Calendar nical = new Calendar(ical);
		PropertyList plist = nical.getProperties();
		plist.remove(plist.getProperty(Property.METHOD));
		nical.getProperties().add(Method.REPLY);
		
		VEvent ve = getVEvent(nical);
		
		// Iterates over attendees...
		PropertyList atts = ve.getProperties(Property.ATTENDEE);
		ArrayList<Attendee> toDelete = new ArrayList<>();
		for (Iterator attIt = atts.iterator(); attIt.hasNext();) {
			Attendee att = (Attendee) attIt.next();

			// Keep only right attendee element, we are looking for a specific attendee
			URI uri = att.getCalAddress();
			if(StringUtils.equals(uri.getSchemeSpecificPart(), forAddress)) {
				att.getParameters().replace(response);
			} else {
				// Mark useless attendee
				toDelete.add(att);
			}
		}
		if(atts.size() == toDelete.size()) return null; // Not found!
		
		// Removes unwanted attendees
		for(Attendee att : toDelete) atts.remove(att);
		
		return nical;
	}
}
