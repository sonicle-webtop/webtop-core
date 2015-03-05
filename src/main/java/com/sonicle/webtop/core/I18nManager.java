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

import com.sonicle.webtop.core.sdk.AppLocale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class I18nManager {
	
	private static final Logger logger = WebTopApp.getLogger(I18nManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @param languageTags
	 * @return The instance.
	 */
	public static synchronized I18nManager initialize(WebTopApp wta, String[] languageTags) {
		if(initialized) throw new RuntimeException("Initialization already done");
		I18nManager locm = new I18nManager(wta, languageTags);
		initialized = true;
		logger.info("I18nManager initialized.");
		return locm;
	}
	
	private WebTopApp wta = null;
	private static final String VALID_TIMEZONES_RE = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";
	private final List<TimeZone> timezones;
	private final HashMap<String, AppLocale> locales = new HashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private I18nManager(WebTopApp wta, String[] languageTags) {
		this.wta = wta;
		
		timezones = getAvailableTimezones();
		for(String tag : languageTags) {
			locales.put(tag, new AppLocale(tag));
		}
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		timezones.clear();
		locales.clear();
	}
	
	public List<TimeZone> getTimezones() {
		return timezones;
	}
	
	public List<AppLocale> getLocales() {
		return new ArrayList<>(locales.values());
	}
	
	public AppLocale getLocale(String languageTag) {
		return locales.get(languageTag);
	}
	
	public static List<TimeZone> getAvailableTimezones() {
		ArrayList<TimeZone> tzs = new ArrayList<>();
		final String[] ids = TimeZone.getAvailableIDs();
		for(final String id : ids) {
			if(id.matches(VALID_TIMEZONES_RE)) tzs.add(TimeZone.getTimeZone(id));
		}
		Collections.sort(tzs, new Comparator<TimeZone>() {
			@Override
			public int compare(final TimeZone t1, final TimeZone t2) {
				return t1.getID().compareTo(t2.getID());
			}
		});
		return tzs;
	}
}
