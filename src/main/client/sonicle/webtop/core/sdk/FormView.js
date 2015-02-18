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
Ext.define('Sonicle.webtop.core.sdk.FormView', {
	alternateClassName: 'WT.sdk.FormView',
	extend: 'WT.sdk.DockableView',
	requires: [
		'Sonicle.form.Panel'
	],
	
	config: {
		/**
		 * @cgf {String} formRefKey
		 * Key to use within {@link WT.mixin.RefStorer#getRef} method in order
		 * to retrieve the form component of this view.
		 */
		formRefKey: 'form',
		
		/**
		 * @cfg {Boolean} autoToolbar
		 * True to automatically define a top toolbar with actions for saving
		 * and closing the view (Save/Save&Close buttons).
		 */
		autoToolbar: true,
		
		/**
		 * @cfg {Boolean} showSave
		 * True to define also a Save button in addition to the Save&Close one.
		 */
		showSave: false,
		
		/**
		 * @cgf {Boolean} applyModeTitle
		 * True to update view title according to the operative {@link #mode}.
		 */
		applyModeTitle: true,
		
		/**
		 * @cfg {String} modeTitleFormat
		 * Formatting string to use during title update.
		 */
		modeTitleFormat: '{0}: {1}'
	},
	
	/**
	 * @private
	 * @property {String} mode
	 * The form operative mode.
	 */
	mode: null,
	MODE_NEW: 'new',
	MODE_EDIT: 'edit',
	MODE_VIEW: 'view',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		if(me.autoToolbar) me.initTBar();
		me.callParent(arguments);
		me.on('beforeviewclose', me.onBeforeViewClose);
	},
	
	initTBar: function() {
		var me = this, items = [];
		
		me.addAction('saveClose', {
			text: WT.res('act-saveClose.lbl'),
			iconCls: 'wt-icon-saveClose-xs',
			handler: function() {
				me.doSave(true);
			}
		});
		
		if(!me.showSave) {
			items.push(me.getAction('saveClose'));
			/*
			Ext.apply(me, {
				tbar: [
					me.getAction('saveClose')
				]
			});
			*/
		} else {
			items.push({
				xtype: 'splitbutton',
				text: WT.res('act-saveClose.lbl'),
				iconCls: 'wt-icon-saveClose-xs',
				menu: [
					me.addAction('save', {
						text: WT.res('act-save.lbl'),
						iconCls: 'wt-icon-save-xs',
						handler: function() {
							me.doSave(false);
						}
					}),
					me.getAction('saveClose')
				],
				handler: function() {
					me.getAction('saveClose').execute();
				}
			});
			/*
			Ext.apply(me, {
				tbar: [{
					xtype: 'splitbutton',
					text: WT.res('act-saveClose.lbl'),
					iconCls: 'wt-icon-saveClose-xs',
					menu: [
						me.addAction('save', {
							text: WT.res('act-save.lbl'),
							iconCls: 'wt-icon-save-xs',
							handler: function() {
								me.doSave(false);
							}
						}),
						me.getAction('saveClose')
					],
					handler: function() {
						me.getAction('saveClose').execute();
					}
				}]
			});
			*/
		}
		WT.Util.applyTbItems(me, 'top', items, false);
	},
	
	/**
	 * Loads defined form and sets NEW mode.
	 * @param {Object} data
	 */
	beginNew: function(data) {
		var me = this;
		me.setMode(me.MODE_NEW);
		me.doLoad(data);
	},
	
	/**
	 * Loads defined form and sets VIEW mode.
	 * @param {Object} data
	 */
	beginView: function(data) {
		var me = this;
		me.setMode(me.MODE_VIEW);
		me.doLoad(data);
	},
	
	/**
	 * Loads defined form and sets EDIT mode.
	 * @param {Object} data
	 */
	beginEdit: function(data) {
		var me = this;
		me.setMode(me.MODE_EDIT);
		me.doLoad(data);
	},
	
	getFormCmp: function() {
		return this.getRef(this.formRefKey);
	},
	
	/**
	 * Sets a operative mode.
	 * @param {String} value The mode to set.
	 */
	setMode: function(value) {
		var me = this,
				om = me.mode;
		switch(value) {
			case me.MODE_NEW:
			case me.MODE_VIEW:
			case me.MODE_EDIT:
				me.mode = value;
				break;
			default:
				return;
		}
		if(me.mode !== om) me.onModeChange(value, om);
	},
	
	/**
	 * Checks if current mode match within the passed one.
	 * @param {type} mode Mode value to check.
	 * @returns {Boolean} True if specified mode is currently active, False otherwise.
	 */
	isMode: function(mode) {
		return (this.mode === mode);
	},
	
	doLoad: function(data) {
		var me = this;
		me.getFormCmp().loadForm(data, {
			callback: function(s, success, model) {
				me.fireEvent('viewload', me, success, model);
			}
		});
	},
	
	doSave: function(closeAfter) {
		var me = this,
				form = this.getFormCmp();
		
		if(!form.isValid()) return;
		form.saveForm({
			callback: function(s, success, model) {
				me.fireEvent('viewsave', me, success, model);
				if(success) {
					if(closeAfter) me.closeView(false);
				}
			}
		});
	},
	
	onModeChange: function(nm, om) {
		var me = this;
		if(me.applyModeTitle) me.updateModeTitle(nm);
		me.fireEvent('modechange', me, nm, om);
	},
	
	onConfirmView: function() {
		// User chose to save dirty values before close.
		// Do save, signalling to close the view after a succesful operation.
		this.doSave(true);
	},
	
	onBeforeViewClose: function() {
		// Fields are dirty!
		// Returns false to stop view closing and to display a confirm message.
		if(this.getFormCmp().isDirty()) return false;
	},
	
	updateModeTitle: function(mode) {
		var me = this, mtit;
		if(this.ctInited) {
			switch(mode) {
				case me.MODE_VIEW:
					mtit = WT.res('act-view.lbl');
					break;
				case me.MODE_NEW:
					mtit = WT.res('act-new.lbl');
					break;
				case me.MODE_EDIT:
					mtit = WT.res('act-edit.lbl');
					break;
			}
			me.ownerCt.setTitle(Ext.String.format(me.modeTitleFormat, me.title, mtit));
		}
	}
});
