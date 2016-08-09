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



/**
 * This file includes the required ext-all js and css files based upon "theme" and "rtl"
 * url parameters.  It first searches for these parameters on the page url, and if they
 * are not found there, it looks for them on the script tag src query string.
 * For example, to include the neptune flavor of ext from an index page in a subdirectory
 * of extjs/examples/:
 * <script type="text/javascript" src="../../examples/shared/include-ext.js?theme=neptune"></script>
 */
(function () {
	function getQueryParam(name) {
		var regex = RegExp('[?&]' + name + '=([^&]*)');

		var match = regex.exec(location.search) || regex.exec(path);
		return match && decodeURIComponent(match[1]);
	}

	function hasOption(opt, queryString) {
		var s = queryString || location.search;
		var re = new RegExp('(?:^|[&?])' + opt + '(?:[=]([^&]*))?(?:$|[&])', 'i');
		var m = re.exec(s);

		return m ? (m[1] === undefined || m[1] === '' ? true : m[1]) : false;
	}

	function getCookieValue(name) {
		var cookies = document.cookie.split('; '),
				i = cookies.length,
				cookie, value;

		while (i--) {
			cookie = cookies[i].split('=');
			if (cookie[0] === name) {
				value = cookie[1];
			}
		}

		return value;
	}
	
	Ext = window.Ext || {};
	Ext.setRepoDevModeCookie = function () {
		document.cookie = 'ExtRepoDevMode=true; expires=Wed, 01 Jan 3000 07:00:00 GMT;';
	};
	Ext.clearRepoDevModeCookie = function () {
		document.cookie = 'ExtRepoDevMode=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
	};
	
	var scriptEls = document.getElementsByTagName('script'),
		path = scriptEls[scriptEls.length - 1].src,
		extPath = 'resources/extjs',
		soUxPath = 'resources/sonicle',
		corePath = 'resources/com.sonicle.webtop.core',
		language = getQueryParam('language') || 'it',
		theme = getQueryParam('theme') || 'crisp',
		laf = getQueryParam('laf') || 'default',
		rtl = getQueryParam('rtl'),
		includeCSS = !hasOption('nocss', extPath),
		useDebug = getQueryParam('debug'),
		hasOverrides = !hasOption('nooverrides', extPath) && !!{
			// TODO: remove neptune
			aria: 1,
			neptune: 1,
			classic: 1,
			gray: 1,
			'neptune-touch': 1,
			crisp: 1,
			'crisp-touch': 1
			}[theme],
		repoDevMode = Ext.repoDevMode = getCookieValue('ExtRepoDevMode'),
		packagePath,
		localePath,
		extLocale,
		extTheme,
		themePath,
		chartsCSSPath,
		overridePath,
		extPrefix,
		lafPath,
		soUxCSSPath;
	
	rtl = rtl && rtl.toString() === 'true';
	useDebug = useDebug && useDebug.toString() === 'true';
	packagePath = extPath + '/packages/';
	extLocale = 'ext-locale-' + language;
	localePath = packagePath + 'ext-locale/build/' + extLocale;
	extTheme = 'ext-theme-' + theme;
	themePath = packagePath + extTheme + '/build/resources/' + extTheme + (rtl ? '-all-rtl' : '-all');
	chartsCSSPath = packagePath + 'sencha-charts/build/' + theme +
			'/resources/sencha-charts' + (rtl ? '-all-rtl' : '-all');
	lafPath = corePath + '/laf/' + laf + '/';
	soUxCSSPath = soUxPath + '/resources/';

	if (includeCSS) {
		document.write('<link rel="stylesheet" type="text/css" href="' +
				themePath + (useDebug ? '-debug.css' : '.css') + '"/>');
		document.write('<link rel="stylesheet" type="text/css" href="' + 
				chartsCSSPath + (useDebug ? '-debug.css' : '.css') + '"/>');
		document.write('<link rel="stylesheet" type="text/css" href="' +
				soUxCSSPath + 'index.css' + '"/>');
	}
	
	extPrefix = useDebug ? '/ext-all-debug' : '/ext-all';

	document.write('<script type="text/javascript" src="' + extPath + extPrefix +
			(rtl ? '-rtl' : '') + '.js"></script>');
	document.write('<script type="text/javascript" src="' + localePath +
			(useDebug ? '-debug' : '') + '.js"></script>');

	if (hasOverrides) {
		// since document.write('<script>') does not block execution in IE, we need to 
		// makes sure we prevent ext-theme-neptune.js from executing before ext-all.js
		// normally this can be done using the defer attribute on the script tag, however
		// this method does not work in IE when in repoDevMode.  It seems the reason for
		// this is because in repoDevMode ext-all.js is simply a script that loads other
		// scripts and so Ext is still undefined when the neptune overrides are executed.
		// To work around this we use the _beforereadyhandler hook to load the neptune
		// overrides dynamically after Ext has been defined.
		overridePath = packagePath + extTheme + '/build/' + extTheme + (repoDevMode ? '-debug' : '') + '.js';

		if (repoDevMode && window.ActiveXObject) {
			Ext = {
				_beforereadyhandler: function () {
					Ext.Loader.loadScript({url: overridePath});
				}
			};
		} else {
			document.write('<script type="text/javascript" src="' +
					overridePath + '" defer></script>');
		}
	}

})();
