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
Ext.define('Sonicle.webtop.core.sdk.DockableView', {
	alternateClassName: 'WT.sdk.DockableView',
	extend: 'WT.sdk.BaseView',
	
	config: {
		/**
		 * @cfg {String} iconCls
		 * The icon class to be used to apply as container's iconCls.
		 */
		iconCls: null,
		
		/**
		 * @cfg {String} title
		 * The title text to be used to apply as container's title.
		 * If value begins with @ is treated as frameword resource string.
		 */
		title: null,
		
		/**
		 * @cfg {Boolean} promptConfirm
		 * If true, a confirm message will be shown in case of false return at canCloseView method.
		 */
		promptConfirm: true,
		
		/**
		 * @cfg {String} confirmMsg
		 * Custom confirm message to show.
		 */
		confirmMsg: null
	},
	
	/**
	 * @property {Boolean} ctInited
	 * @private
	 */
	ctInited: false,
	
	constructor: function(cfg) {
		var me = this;
		me.initConfig(cfg);
		me.callParent(arguments);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.on('added', function(s,ct) {
			me.initCt(ct);
		}, me, {single: true});
		me.on('removed', function(s,ct) {
			me.cleanupCt(ct);
		}, me, {single: true});
	},
	
	initCt: function(ct) {
		var me = this;
		if(me.ctInited) return;
		if(ct.isXType('window')) {
			// Apply as config (window is not yet rendered)
			ct.title = WT.resStr(me.title);
			ct.iconCls = me.iconCls;
			
			ct.on('show', me.onCtWndShow, me);
			ct.on('beforeclose', me.onCtBeforeClose, me);
			ct.on('close', me.onCtWndClose, me);
			/* TODO: handle window groups
			if(me.useWG) ct.on('hide', me.onCtWndHide, me);
			*/
		}
		me.ctInited = true;
	},
	
	cleanupCt: function(ct) {
		var me = this;
		if(!me.ctInited) return;
		
		if(ct.isXType('window')) {
			ct.un('show', me.onCtWndShow, me);
			ct.un('beforeclose', me.onCtBeforeClose, me);
			ct.un('close', me.onCtWndClose, me);
			ct.un('hide', me.onCtWndHide, me);
		}
		me.ctInited = false;
	},
	
	onCtWndShow: function() {
		var me = this;
		/* TODO: handle window groups
		if(me.useWG) {
			me.wg.each(function(wnd) {
				wnd.show();
			}, me);
		}
		*/
		me.fireEvent('showview', me);
	},
	
	onCtBeforeClose: function() {
		var me = this;
		/* TODO: handle window groups
		if(me.useWG && me.hasWindows()) return false;
		*/
		var cc = Ext.callback(me.canCloseView, me);
		//var cc = (Ext.isFunction(this.canClose)) ? this.canClose() : true;
		if(me.promptConfirm && !cc) {
			WT.confirmYNC(me.confirmMsg, function(bid) {
				if(bid === 'yes') {
					me.onConfirmView();
				} else if(bid === 'no') {
					me.onDiscardView();
				}
			});
			return false;
		}
		return true;
	},
	
	onCtWndClose: function() {
		var me = this;
		me.fireEvent('closeview', me);
	},
	
	onCtWndHide: function() {
		/* TODO: handle window groups
		var me = this;
		if(me.useWG) {
			me.wg.hideAll();
		}
		*/
	},
	
	/**
	 * Handler method exexuted on confirm continue (answer: yes).
	 * Child classes can override this method to implement their own custom logic.
	 */
	onConfirmView: function() {
		//Ext.callback(me.save, me, [true]);
		var me = this;
		me.fireEvent('confirmview', me);
		me.closeView(false);
	},
	
	/**
	 * Handler method exexuted on confirm discard (answer: no).
	 * Child classes can override this method to implement their own custom logic.
	 */
	onDiscardView: function() {
		var me = this;
		me.fireEvent('discardview', me);
		me.closeView(false);
	},
	
	/**
	 * Closes this view.
	 * @param {Boolean} [prompt] Allow to override prompt close behaviour.
	 */
	closeView: function(prompt) {
		var me = this;
		if(prompt !== undefined) me.promptConfirm = prompt;
		if(me.ctInited) me.ownerCt.close();
	},
	
	/**
	 * Test method in order to defermine if view can be closed without
	 * prompting any confirm message.
	 * Child classes can override this method to implement their own custom logic.
	 * @return {Boolean}
	 */
	canCloseView: function() {
		return true;
	}
});
