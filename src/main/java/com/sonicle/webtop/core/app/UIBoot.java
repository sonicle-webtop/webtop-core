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

import com.sonicle.commons.web.json.extjs.Ext6Manifest;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class UIBoot {
	public static final String EXTJS_PATH = "resources/client/extjs/";
	// Do not replace 0.0.0 with the real version, it limits server traffic.
	public static final String VENDOR_PATH = "resources/com.sonicle.webtop.core/0.0.0/resources/vendor";
	public static final String LIBS_PATH = "resources/com.sonicle.webtop.core/0.0.0/resources/libs";
	
	public static void includeVendorLibraries(final Ext6Manifest appManifest, final Locale locale) {
		//TODO: rendere dinamico il caricamento delle librerie, permettendo ai servizi di aggiungere le loro
		
		appManifest.addJs(VENDOR_PATH + "/jquery/3.3.1/" + "jquery.min.js");
		appManifest.addJs(VENDOR_PATH + "/spark-md5/3.0.0/" + "spark-md5.min.js");
		appManifest.addJs(VENDOR_PATH + "/js-emoji/3.4.1/" + "emoji.min.js");
		appManifest.addJs(VENDOR_PATH + "/ion.sound/3.0.7/" + "ion.sound.min.js");
		appManifest.addJs(VENDOR_PATH + "/linkify/2.1.6/" + "linkify.min.js");
		appManifest.addJs(VENDOR_PATH + "/linkify/2.1.6/" + "linkify-string.min.js");
		appManifest.addJs(VENDOR_PATH + "/screenfull/3.3.2/" + "screenfull.min.js");
		appManifest.addJs(VENDOR_PATH + "/atmosphere/2.3.9/" + "atmosphere.min.js");
		appManifest.addJs(VENDOR_PATH + "/jsxc/3.4.0/" + "jsxc.dep.js");
		appManifest.addJs(VENDOR_PATH + "/tinymce/7.9.1/" + "tinymce.min.js");
		appManifest.addJs(VENDOR_PATH + "/plupload/2.3.6/" + "plupload.full.min.js"); // Remember to update paths in Factory.js
		//appManifest.addJs(VENDOR_PATH + "/rrule/2.1.0/" + "rrule.min.js");
		appManifest.addJs(VENDOR_PATH + "/rrule/2.7.1/" + "rrule.min.js");
		appManifest.addJs(VENDOR_PATH + "/markjs/8.11.1/" + "mark.min.js");
		appManifest.addJs(VENDOR_PATH + "/search-string/3.1.0/" + "search-string.min.js");
		appManifest.addJs(VENDOR_PATH + "/jsdifflib/1.1.0/" + "jsdifflib.min.js");
		appManifest.addJs(VENDOR_PATH + "/guess-language/" + "_languageData.js");
		appManifest.addJs(VENDOR_PATH + "/guess-language/" + "guessLanguage.js");
		appManifest.addJs(VENDOR_PATH + "/showdown/1.9.1/" + "showdown.min.js");
		appManifest.addCss(VENDOR_PATH + "/github-markdown/4.0.0/" + "github-markdown.min.css");
		appManifest.addJs(VENDOR_PATH + "/codemirror/5.65.2/" + "codemirror.min.js");
		appManifest.addJs(VENDOR_PATH + "/codemirror/5.65.2/mode/sql/" + "sql.min.js");
		appManifest.addCss(VENDOR_PATH + "/codemirror/5.65.2/" + "codemirror.min.css");
		appManifest.addJs(VENDOR_PATH + "/jexl/2.3.0/" + "jexl.min.js");
		appManifest.addJs(VENDOR_PATH + "/fullcalendar/6.1.18-premium/dist/" + "index.global.min.js");
		appManifest.addJs(VENDOR_PATH + "/fullcalendar/6.1.18-premium/packages/core/locales/" + locale.getLanguage() + ".global.min.js");
		
		// Uncomment these lines to load debug versions of the libraries ----->
		//appManifest.addJs(VENDOR_PATH + "/jsxc/3.4.0/" + "jsxc.dep.js");
		//appManifest.addJs(VENDOR_PATH + "/tinymce/6.3.1/" + "tinymce.js");
		//appManifest.addJs(VENDOR_PATH + "/tinymce/7.9.1/" + "tinymce.js");
		//appManifest.addJs(VENDOR_PATH + "/plupload/2.3.6/" + "moxie.js");
		//appManifest.addJs(VENDOR_PATH + "/plupload/2.3.6/" + "plupload.dev.js");
		// <-------------------------------------------------------------------
		//appManifest.addJs(VENDOR_PATH + "/ckeditor/" + "ckeditor.js");
		//appManifest.addJs(VENDOR_PATH + "/cke5/20.0.0/" + "ckeditor.js");
	}
	
	public static String getExtJsThemeBaseUrl(final String extToolkit, final String theme) {
		//appManifest.addJs(EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme + "/" + "theme-" + extTheme + extDebug + ".js"); // Original theme's JS file
		String extTheme = theme;
		return EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme;
	}
	
	public static String getExtJsThemeResourcesBaseUrl(final String extToolkit, final String theme) {
		//appManifest.addCss(EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme + "/resources/" + "theme-" + extTheme + "-all" + extRtl + extDebug + ".css"); // Original theme's CSS file
		String extTheme = theme;
		return EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme + "/resources";
	}
	
	public static String getExtJsPackageBaseUrl(final String extToolkit, final String extPackageName) {
		//appManifest.addJs(EXTJS_PATH + "packages/charts/" + extToolkit + "/" + "charts" + extDebug + ".js");
		return EXTJS_PATH + "packages/" + extPackageName + "/" + extToolkit;
	}
	
	public static String getExtJsPackageResourcesBaseUrl(final String extToolkit, final String extPackageName, final String theme) {
		//appManifest.addCss(EXTJS_PATH + "packages/charts/" + extToolkit + "/" + extBaseTheme + "/resources/" + "charts-all" + extRtl + extDebug + ".css");
		String extThemeName = UIBoot.sanitizeExtJSTheme(theme);
		String extBaseTheme = UIBoot.getBaseExtJSTheme(extThemeName);
		return EXTJS_PATH + "packages/" + extPackageName + "/" + extToolkit + "/" + extBaseTheme + "/resources";
	}
	
	public static String[] getExtJsMinimalStylesheetUrls(final String extToolkit, final String theme, final boolean rtl, final boolean debug) {
		String extRtl = extJsRtlSuffix(rtl);
		String extDebug = extJsDebugSuffix(debug);
		String extTheme = theme;
		return new String[]{
			getExtJsThemeResourcesBaseUrl(extToolkit, theme) + "/" + "theme-" + extTheme + "-all" + extRtl + extDebug + ".css",
			getExtJsThemeBaseUrl(extToolkit, theme) + "/" + "theme-" + extTheme + "-override" + ".css"
		};
	}
	
	private static String extJsRtlSuffix(final boolean rtl) {
		return rtl ? "-rtl" : "";
	}
	
	private static String extJsDebugSuffix(final boolean debug) {
		return debug ? "-debug" : "";
	}
	
	public static void includeExtJs(final Ext6Manifest appManifest, final String extToolkit, final Locale locale, final String theme, final boolean rtl, final boolean debug, final boolean extensionsDevMode) {
		String extRtl = extJsRtlSuffix(rtl);
		String extDebug = extJsDebugSuffix(debug);
		String extTheme = theme;
		String extThemeName = UIBoot.sanitizeExtJSTheme(theme);
		String extBaseTheme = UIBoot.getBaseExtJSTheme(extThemeName);
		String extLang = "-" + locale.getLanguage();
		
		// ExtJS: core library + locales
		appManifest.addJs(EXTJS_PATH + "ext-all" + extRtl + extDebug + ".js");
		appManifest.addJs(EXTJS_PATH + extToolkit + "/locale/" + "locale" + extLang + extDebug + ".js");
		// ExtJS: themes library + styles (see below for overrides)
		appManifest.addJs(EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme + "/" + "theme-" + extTheme + extDebug + ".js"); // Original theme's JS file
		appManifest.addCss(EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme + "/resources/" + "theme-" + extTheme + "-all" + extRtl + extDebug + ".css"); // Original theme's CSS file
		// ExtJS: charts library + styles
		appManifest.addJs(EXTJS_PATH + "packages/charts/" + extToolkit + "/" + "charts" + extDebug + ".js");
		appManifest.addCss(EXTJS_PATH + "packages/charts/" + extToolkit + "/" + extBaseTheme + "/resources/" + "charts-all" + extRtl + extDebug + ".css");
		// ExtJS: UX library + styles
		appManifest.addJs(EXTJS_PATH + "packages/ux/" + extToolkit + "/" + "ux" + extDebug + ".js");
		appManifest.addCss(EXTJS_PATH + "packages/ux/" + extToolkit + "/" + extBaseTheme + "/resources/" + "ux-all" + extRtl + extDebug + ".css");
		// ExtJS: themes overrides
		appManifest.addJs(EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme + "/" + "theme-" + extTheme + "-override" + extDebug + ".js"); // Supports overriding theme's JS file
		//TODO: add debug
		appManifest.addCss(EXTJS_PATH + extToolkit + "/" + "theme-" + extTheme + "/" + "theme-" + extTheme + "-override" + ".css"); // Supports overriding theme's CSS file
		
		// Fonts: styles
		appManifest.addCss(EXTJS_PATH + "packages/font-awesome/resources/" + "font-awesome-all" + extRtl + extDebug + ".css");
		appManifest.addCss(EXTJS_PATH + "packages/font-awesome/resources/vendor/6.4.2/css/fontawesome.min.css");
		appManifest.addCss(EXTJS_PATH + "packages/font-awesome/resources/vendor/6.4.2/css/solid.min.css");
		appManifest.addCss(EXTJS_PATH + "packages/font-awesome/resources/vendor/6.4.2/css/regular.min.css");
		appManifest.addCss(EXTJS_PATH + "packages/font-awesome/resources/vendor/6.4.2/css/brands.min.css");
		appManifest.addCss(EXTJS_PATH + "packages/font-awesome/resources/vendor/6.4.2/css/v5-font-face.min.css");
		appManifest.addCss(EXTJS_PATH + "packages/font-ext/resources/" + "font-ext-all" + extRtl + extDebug + ".css");
		appManifest.addCss(EXTJS_PATH + "packages/font-pictos/resources/" + "font-pictos-all" + extRtl + extDebug + ".css");
		
		// Sonicle ExtJs Extensions
		if (extensionsDevMode) {
			appManifest.addPath("Sonicle", EXTJS_PATH + "packages/sonicle-extensions/src");
		} else {
			appManifest.addJs(EXTJS_PATH + "packages/sonicle-extensions/" + "sonicle-extensions" + extDebug + ".js");
		}
		appManifest.addCss(EXTJS_PATH + "packages/sonicle-extensions/base/resources/" + "sonicle-extensions-all" + extRtl + extDebug + ".css");
		appManifest.addCss(EXTJS_PATH + "packages/sonicle-extensions/" + extThemeName + "/resources/" + "sonicle-extensions-all" + extRtl + extDebug + ".css");
		
		// Override default Ext error handling in order to avoid application hang.
		// NB: This is only necessary when using ExtJs debug file!
		if (debug) appManifest.addJs(LIBS_PATH + "/" + "ext-override-errors.js");
	}
	
	public static String sanitizeExtJSTheme(String theme) {
		return StringUtils.removeEnd(theme, "-touch");
	}
	
	public static String getBaseExtJSTheme(String theme) {
		// Each theme in ExtJS has a reference theme that be considered as "base" 
		// theme from sources organization point of view, that describes an implict
		// sort of inheritance between them. This is clearly adopted in packages 
		// folder organization. Please see "Configuring Theme Inheritance" https://docs.sencha.com/extjs/7.5.0/guides/core_concepts/theming.html)
		// for understanding theme "strict" inheritance and ExtJS archive for this 
		// "implicit" inheritance.
		
		switch (theme) {
			case "classic":
			case "crisp":
			case "graphite":
			case "neptune":
			case "triton":
				return theme;
			case "gray":
				return "classic";
			case "aria":
				return "neptune";
			case "material":
				return "triton";
			default:
				return "neptune";
		}
	}
	
	public static String guessColorScheme(final String lafName) {
		final String overlay = StringUtils.lowerCase(StringUtils.substringAfterLast(lafName, "@"));
		return ("light".equals(overlay) || "dark".equals(overlay)) ? overlay : null;
	}
	
	public static String getColorScheme(final String lafName) {
		return getColorScheme(lafName, "light");
	}
	
	public static String getColorScheme(final String lafName, final String defaultColorScheme) {
		return StringUtils.defaultIfBlank(guessColorScheme(lafName), defaultColorScheme);
	}
}
