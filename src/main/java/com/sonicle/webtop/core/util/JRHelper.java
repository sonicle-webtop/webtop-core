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

import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRenderable;
import net.sf.jasperreports.renderers.BatikRenderer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class JRHelper {
	
	public static final String SWATCH_SVG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
		"<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"16px\" height=\"16px\" viewBox=\"0 0 16 16\" enable-background=\"new 0 0 16 16\" xml:space=\"preserve\">\n" +
		"  <path d=\"M2 0H14Q16 0 16 2V14Q16 16 14 16H2Q0 16 0 14V2Q0 0 2 0\" fill=\"{FILL_COLOR}\" stroke=\"{STROKE_COLOR}\" stroke-width=\"1\" stroke-linecap=\"round\" stroke-miterlimit=\"1\" stroke-linejoin=\"round\"/>\n" +
		"</svg>";
	
	public static String saniString(String s) {
		return StringUtils.defaultIfEmpty(s, null);
	}
	
	public static java.util.Date dateTimeAsDate(DateTime dateTime, DateTimeZone timezone) {
		if (dateTime == null) {
			return null;
		} else {
			return timezone != null ? dateTime.withZone(timezone).toDate() : dateTime.toDate();
		}
	}
	
	public static JRRenderable colorAsSvg(final String hexColor) {
		String fillColor = StringUtils.defaultIfBlank(toColorString(hexColor), "none");
		String strokeColor = "#FFFFFF".equalsIgnoreCase(fillColor) ? "#A8A8A8" : "none";
		String svg = SWATCH_SVG.replace("{FILL_COLOR}", fillColor).replace("{STROKE_COLOR}", strokeColor);
		try {
			return (JRRenderable)BatikRenderer.getInstanceFromText(svg);
		} catch (JRException ex) {
			throw new WTRuntimeException("Unable to prepare svg", ex);
		}
	}
	
	public static Image colorAsImage(String hexColor) {
		try {
			BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			bi.setRGB(0, 0, Color.decode("#" + hexColor).getRGB());
			return bi;
		} catch(Throwable t) {
			return null;
		}
	}
	
	private static String toColorString(String hexColor) {
		String color = StringUtils.removeStart(hexColor, "#");
		return StringUtils.isBlank(color) ? color : "#"+color;
	}
}
