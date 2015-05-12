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
Ext.define('Sonicle.webtop.core.view.Feedback', {
	alternateClassName: 'WT.view.Feedback',
	extend: 'WT.sdk.FormView',
	requires: [
		'WT.model.Simple',
		'WT.model.Feedback',
		'Sonicle.form.Spacer'
	],
	
	title: '@feedback.tit',
	iconCls: 'wt-icon-feedback-xs',
	autoToolbar: false,
	autoTitle: false,
	confirm: 'yn',
	
	h2cCanvas: null,
	jpegQuality: 0.7, // 0.1 to 1 (1 = 100%)
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			buttons: [{
				text: WT.res('act-send.lbl'),
				handler: me.onSendClick,
				scope: me
			}, {
				text: WT.res('act-cancel.lbl'),
				handler: me.onCancelClick,
				scope: me
			}]
		});
		me.callParent(arguments);
		
		me.add(me.addRef('form', Ext.create({
			region: 'center',
			xtype: 'soform',
			model: 'WT.model.Feedback',
			bodyPadding: 10,
			items: [{
					xtype: 'component',
					html: WT.res('feedback.text')
				}, {
					xtype: 'sospacer',
					mult: 2
				}, {
					xtype: 'combo',
					name: 'serviceId',
					allowBlank: false,
					editable: false,
					store: {
						model: 'WT.model.Simple',
						proxy: WTF.proxy('com.sonicle.webtop.core', 'GetUserServices', 'services')
					},
					valueField: 'id',
					displayField: 'desc',
					width: 400,
					fieldLabel: WT.res('feedback.fld-service.lbl')
				}, {
					xtype: 'textareafield',
					name: 'message',
					allowBlank: false,
					anchor: '100%',
					fieldLabel: WT.res('feedback.fld-message.lbl')
				}, {
					xtype: 'checkbox',
					name: 'anonymous',
					hideLabel: true,
					boxLabel: WT.res('feedback.fld-anonymous.lbl')
				}, {
					xtype: 'checkbox',
					name: 'screenshot',
					submitValue: false,
					hideLabel: true,
					boxLabel: WT.res('feedback.fld-screenshot.lbl'),
					handler: me.onScreenshotChange,
					scope: me
				}, {
					xtype: 'hiddenfield',
					name: 'timestamp'
				}, {
					xtype: 'hiddenfield',
					name: 'image'
				}]
		})));
	},
	listeners: {
		afterrender: function() {
			var ct = this.ownerCt;
			if(ct.isXType('window')) {
				ct.getEl().set({'data-html2canvas-ignore': 'true'});
			}
		},
		viewsave: function(s, success) {
			if(success) {
				WT.info(WT.res('feedback.sent'));
				this.closeView(false);
			}
		},
		viewclose: function() {
			this.clearScreenshot();
		}
	},
	
	onSendClick: function() {
		var me = this,
				form = me.getFormCmp(),
				h2c = (me.h2cCanvas) ? me.h2cCanvas.toDataURL('image/jpeg', me.jpegQuality) : null;
		
		form.setFieldValue('timestamp', new Date().toString());
		form.setFieldValue('image', h2c);
		me.doSave(false);
	},
	
	onCancelClick: function() {
		this.closeView(false);
	},
	
	onScreenshotChange: function(s, chk) {
		if(chk) {
			this.takeScreenshot();
		} else {
			this.clearScreenshot();
		}
	},
	
	takeScreenshot: function() {
		var me = this;
		
		me.wait(WT.res('feedback.capturing'));
		me.clearScreenshot();
		WT.loadScriptAsync('js/html2canvas.js', function(success) {
			if(success) {
				html2canvas([document.body], {
					onrendered: function(canvas) {
						var cel = Ext.get(canvas);
						cel.setStyle('position', 'absolute');
						cel.setStyle('left', 0);
						cel.setStyle('top', 0);
						cel.setStyle('z-index', 8900);
						Ext.get(document.body).insertSibling(cel);
						me.h2cCanvas = canvas;
						me.unwait();
					}
				});
			} else {
				me.unwait();
			}
		}, me);
	},
	
	clearScreenshot: function() {
		var me = this,
				form = me.getFormCmp();
		if(me.h2cCanvas) {
			Ext.removeNode(Ext.get(me.h2cCanvas).dom);
			me.h2cCanvas = null;
			form.setFieldValue('screenshot', false);
		}
	}
});
