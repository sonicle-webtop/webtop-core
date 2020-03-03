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
package com.sonicle.webtop.core.app.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class ClassHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassHelper.class);
	
	public static Class loadClass(boolean skipIfBlank, String className, Class requiredParentClass, String targetDescription) {
		if (skipIfBlank && StringUtils.isBlank(className)) {
			return null;
		} else {
			return loadClass(className, requiredParentClass, targetDescription);
		}
	}
	
	public static Class loadClass(String className, Class requiredParentClass, String targetDescription) {
		String tdesc = StringUtils.defaultIfBlank(targetDescription, "Target");
		
		try {
			Class clazz = loadClass(className, targetDescription);
			if ((clazz != null) && !isInheritingFromParent(clazz, requiredParentClass)) throw new ClassCastException();
			return clazz;

		} catch(ClassCastException ex) {
			LOGGER.warn("A valid {} class must extends '{}' class", tdesc, requiredParentClass.toString());
		} catch(Throwable t) {
			LOGGER.error("Unable to load class [{}]", className, t);
		}
		return null;
	}
	
	public static Class loadClass(String className, String targetDescription) {
		String tdesc = StringUtils.defaultIfBlank(targetDescription, "Target");
		
		try {
			Class clazz = Class.forName(className);
			return clazz;
			
		} catch(ClassNotFoundException ex) {
			LOGGER.debug("{} class not found [{}]", tdesc, className);
		}
		return null;
	}
	
	public static boolean isInheritingFromParent(Class clazz, Class parentClass) {
		if (clazz == null) return false;
		if (parentClass == null) return false;
		return parentClass.isAssignableFrom(clazz);
	}
	
	public static boolean isImplementingInterface(Class clazz, Class interfaceClass) {
		if (clazz == null) return false;
		if (interfaceClass == null) return false;
		return interfaceClass.isAssignableFrom(clazz);
	}
	
	public static Annotation getClassAnnotation(Class clazz, Class annotationClass) {
		if (clazz == null) return null;
		Set<Annotation> annotations = getClassAnnotations(clazz, annotationClass);
		return annotations.isEmpty() ? null : annotations.iterator().next();
	}
	
	public static Set<Annotation> getClassAnnotations(Class clazz, Class annotationClass) {
		if (clazz == null) return null;
		Annotation[] annotations = clazz.getAnnotationsByType(annotationClass);
		return Arrays.stream(annotations).unordered().collect(Collectors.toSet());
	}
}
