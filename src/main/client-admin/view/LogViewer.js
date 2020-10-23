/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.LogViewer', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.form.field.TextArea'
	],
	
	dockableConfig: {
		title: '{logViewer.tit}',
		iconCls: 'wtadm-icon-logViewer'
	},
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			tbar: [
				me.addAct('download', {
					text: me.mys.res('logViewer.act-download.lbl'),
					iconCls: 'wt-icon-file-download',
					handler: function() {
						me.getLogContentUI(true);
					}
				}),
				'->',
				WTF.lookupCombo('id', 'desc', {
					store: {
						autoLoad: true,
						fields: ['id', 'desc'],
						data: [
							[-1, me.mys.res('logViewer.fld-refresh.manual')],
							[5, me.mys.res('logViewer.fld-refresh.5')],
							[20, me.mys.res('logViewer.fld-refresh.20')],
							[60, me.mys.res('logViewer.fld-refresh.60')],
							[120, me.mys.res('logViewer.fld-refresh.120')],
							[300, me.mys.res('logViewer.fld-refresh.300')]
						]
					},
					value: -1,
					fieldLabel: me.mys.res('logViewer.fld-refresh.lbl'),
					labelAlign: 'right',
					width: 100+140,
					listeners: {
						select: function(s, rec) {
							me.autoRefreshUI(rec.getId());
						}
					}
				}),
				WTF.lookupCombo('id', 'desc', {
					reference: 'fldkbytes',
					store: {
						autoLoad: true,
						fields: ['id', 'desc'],
						data: [
							[25, me.mys.res('logViewer.fld-kbytes.25')],
							[50, me.mys.res('logViewer.fld-kbytes.50')],
							[100, me.mys.res('logViewer.fld-kbytes.100')]
						]
					},
					value: 25,
					width: 120
				}),
				'-',
				me.addAct('refresh', {
					text: null,
					tooltip: WT.res('act-refresh.lbl'),
					iconCls: 'wt-icon-refresh',
					handler: function() {
						me.getLogContentUI(false);
					}
				})
			]
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			layout: 'fit',
			items: [
				{
					xtype: 'sotextarea',
					reference: 'fldcontent',
					editable: false,
					fieldStyle: {
						whiteSpace: 'pre',
						overflowWrap: 'normal',
						fontFamily: '"Courier New", Courier, monospace'
					}
				}
			]
		});
		me.on('afterrender', function() {
			Ext.defer(function() {
				me.getLogContentUI(false);
			}, 200);
		}, me, {single: true});
	},
	
	destroy: function() {
		this.callParent();
		this.autoRefreshUI(-1);
	},
	
	autoRefreshUI: function(interval) {
		var me = this;
		if (me.refIvl) {
			clearInterval(me.refIvl);
			delete me.refIvl;
		}
		if (interval > 0) {
			me.refIvl = setInterval(function() {
				if (!me.waiting()) me.getLogContentUI(false);
				
			}, interval * 1000);
			if (!me.waiting()) me.getLogContentUI(false);
		}
	},
	
	getLogContentUI: function(download) {
		var me = this,
				bytesCount = me.lref('fldkbytes').getValue() * -1024;
		if (!download) {
			me.wait();
			WT.ajaxReq(me.mys.ID, 'GetLogContent', {
				method: 'GET',
				params: {
					nowriter: true,
					bytesCount: bytesCount,
					rawErrorResp: true
				},
				rawCallback: function(success, resp, opts) {
					me.unwait();
					if (success) {
						var fld = me.lref('fldcontent');
						fld.setValue(resp.responseText);
						fld.scrollToBottom();
					} else {
						WT.error(me.mys.res('lowViewer.error.missing'));
					}
				}
			});
		} else {
			Sonicle.URLMgr.downloadFile(WTF.processBinUrl(me.mys.ID, 'GetLogContent', {
				bytesCount: 50 * 1024 * 1024
			}));
		}
	}
});
