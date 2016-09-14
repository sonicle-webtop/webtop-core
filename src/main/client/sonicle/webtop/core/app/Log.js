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
Ext.define('Sonicle.webtop.core.app.Log', {
	singleton: true,
	alternateClassName: 'WT.Log',
	
	constructor: function() {
		var me = this;
		window.onerror = Ext.Function.bind(me.onWindowError, me);
		me.callParent(arguments);
	},
	
	onWindowError: function(message, file, line, column, errorObj) {
		//Ext.global.console.error(message);
		console.log('WINDOW ERROR: ' + message);
		
		/*
		var win = window,
            d = document;

        if (!message || message.match('chrome://') || message.match('Script error')) {
            return;
        }

        if (this.nbrErrorsLogged < this.maxNbrLogs && message && (line || file)) {
            this.nbrErrorsLogged++;

            var windowWidth = win.innerWidth || d.documentElement.clientWidth || d.body.clientWidth,
                windowHeight = win.innerHeight || d.documentElement.clientHeight || d.body.clientHeight;

            var crashData = {
                msg          : message,
                url          : file,
                line         : line,
                href         : win.location.href,
                windowWidth  : windowWidth,
                windowHeight : windowHeight,
                extVersion   : Ext.versions && Ext.versions.extjs && Ext.versions.extjs.version,
                localDate    : new Date().toString(),

                browser : (Ext.ieVersion && "IE" + Ext.ieVersion) ||
                (Ext.chromeVersion && "Chrome" + Ext.chromeVersion) ||
                (Ext.firefoxVersion && "FF" + Ext.firefoxVersion) ||
                (Ext.safariVersion && "Safari" + Ext.safariVersion) ||
                (Ext.operaVersion && "Opera" + Ext.operaVersion) ||
                navigator.userAgent,

                column : column || '',
                stack  : (errorObj && errorObj.stack) || ''
            };

            var crashString = '';

            Ext.Object.each(crashData, function (key, value) {
                crashString += (key + '=' + encodeURIComponent(value) + '&');
            });

            new Image().src = this.logUrl + '?' + crashString;
        }
		*/
	},
	
	/**
	 * @param {String} msg
	 * @param {Mixed...} values
	 */
	log: function(msg, values) {
		var cs = Ext.global.console,
				s = (arguments.length === 1) ? msg : Ext.String.format(msg, Ext.Array.slice(arguments, 1));
		//cs.log.apply(cs, [s]);
		Ext.global.console.log(s);
	},
	
	/**
	 * @param {String} msg
	 * @param {Mixed...} values
	 */
	debug: function(msg, values) {
		var cs = Ext.global.console,
				s = (arguments.length === 1) ? msg : Ext.String.format(msg, Ext.Array.slice(arguments, 1));
		//Function.prototype.apply.apply(console.debug, [console, s]);
		//Function.prototype.bind.call();
		//console.debug.apply(console, [s]);
		//cs.debug.apply(cs, [s]);
		Ext.global.console.debug(s);
	},
	
	/**
	 * @param {String} msg
	 * @param {Mixed...} values
	 */
	warn: function(msg, values) {
		var cs = Ext.global.console,
				s = (arguments.length === 1) ? msg : Ext.String.format(msg, Ext.Array.slice(arguments, 1));
		//cs.warn.apply(cs, [s]);
		Ext.global.console.warn(s);
	},
	
	/**
	 * @param {String} msg
	 * @param {Mixed...} values
	 */
	error: function(msg, values) {
		var cs = Ext.global.console,
				s = (arguments.length === 1) ? msg : Ext.String.format(msg, Ext.Array.slice(arguments, 1));
		//cs.error.apply(cs, [s]);
		Ext.global.console.error(s);
	},
	
	/**
	 * @param {Exception} e
	 */
	exception: function(e) {
		var cs = Ext.global.console;
		Ext.global.console.error(e.stack);
	}
	
});
