/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
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
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
Ext.define('Sonicle.webtop.core.view.SMS', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'WTA.ux.panel.Form'
	],
	
	dockableConfig: {
		title: '{SMS.tit}',
		iconCls: 'wt-icon-sms',
		width: 450,
		height: 300
	},
	
	layout: 'border',
	bodyPadding: 0,
	
	number: null,
	name: null,
	
	viewModel: {
		data: {
			text: ''
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		
		Ext.merge(cfg, {
			dockableConfig: {
				title: WT.res('SMS.tit')+(cfg.name?" : "+cfg.name+" ["+cfg.number+"]":"["+cfg.number+"]")
			}
		});
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foGetRemainingCharsHTML: WTF.foGetFn(null, 'text', function(v) {
				if (v) {
					var n=v.length,
					    s=n>0?(n+" (1 SMS)"):"";
					if (n>160) s='<span style="color:red">'+n+"&nbsp;("+(Math.trunc(n/160)+1)+" SMS)"+'</span>';
					return s;
				}
				return '';
			}),
			foIsTextEmpty: WTF.foGetFn(null, 'text', function(v) {
				if (v) {
					return v.length===0;
				}
				return true;
			})
			
		});
	},
	
	initComponent: function() {
		var me = this,
			ic = me.getInitialConfig();
		
		if (ic.text && !Ext.isEmpty(ic.text)) me.getVM().set('text', ic.text);
		
		
		Ext.apply(me, {
			bbar: [
				{
					xtype: 'tbtext',
					bind: {
						html: '{foGetRemainingCharsHTML}'
					}
				},
				'->',
				{
					reference: 'btnsend',
					xtype: 'button',
					text: WT.res('act-send.lbl'),
					handler: function(s) {
						s.setDisabled(true); // Avoids multi-runs!
						me.send();
					},
					bind: {
						disabled: '{foIsTextEmpty}'
					}
				}, {
					reference: 'btncancel',
					xtype: 'button',
					text: WT.res('act-cancel.lbl'),
					handler: function() {
						me.closeView();
					}
				}
			]
		});
		me.callParent(arguments);
		
		me.add({
			xtype: 'textarea',
			reference: 'txtText',
			bind: '{text}'
		});
	},
	
	send: function() {
		var me=this;
		WT.ajaxReq(WT.ID, 'SendSMS', {
			params: {
				number: me.number,
				text: me.getVM().get("text")
			},
			callback: function(success, json) {
				if (success) {
					me.closeView();
				} else {
					WT.error(json.message);
				}
			}
		});
	}
	
});

