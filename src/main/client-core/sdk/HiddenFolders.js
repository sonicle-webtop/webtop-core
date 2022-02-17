/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
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

Ext.define('Sonicle.webtop.core.sdk.HiddenFolders', {
	alternateClassName: 'WTA.sdk.HiddenFolders',
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.model.HiddenFolder'
	],
	
	dockableConfig: {
		width: 300,
		height: 300,
		modal: true,
		minimizable: false,
		maximizable: false
	},
	promptConfirm: false,
	
	action: 'ManageHiddenFolders',
	extraParams: null,
	callback: null,
	scope: null,

	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			buttons: [{
				text: WT.res('act-ok.lbl'),
				handler: me.onOkClick,
				scope: me
			}, {
				text: WT.res('act-cancel.lbl'),
				handler: me.onCancelClick,
				scope: me
			}]
		});
		me.callParent(arguments);		
		
		me.add({
			region: 'center',
			xtype: 'gridpanel',
			reference: 'gp',
			border: false,
			loadMask: {msg: WT.res('loading')},
			selType: 'sorowmodel',
			hideHeaders: true,
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.model.HiddenFolder',
				proxy: WTF.apiProxy(me.mys.ID, me.action, 'data', {
					extraParams: me.extraParams || {}
				})
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: WT.res('grid.emp')
			},
			columns: [{
				dataIndex: 'id',
				width: 180,
				hidden: true
			}, {
				dataIndex: 'desc',
				flex: 1
			}, {
				xtype: 'actioncolumn',
				width: 30,
				items: [{
					iconCls: 'fas fa-minus-circle',
					tooltip: WT.res('act-remove.lbl'),
					handler: function(s, ri) {
						s.getStore().removeAt(ri);
					}
				}]
			}]
		});
	},
	
	onOkClick: function() {
		var me = this,
				recs = me.lref('gp').getStore().getRemovedRecords();
		me.fireEvent('viewok', me);
		if (recs.length > 0) {
			var ids = [];
			Ext.iterate(recs, function(rec) {
				ids.push(rec.get('id'));
			});							
			WT.ajaxReq(me.mys.ID, me.action, {
				params: {
					crud: 'delete',
					ids: WTU.arrayAsParam(ids)
				},
				callback: function(success, json) {
					me.fireEvent('viewcallback', me, success, json, ids);
					Ext.callback(me.callback, me.scope || me, [success, json, ids]);
					me.closeView(false);
				}
			});
		} else {
			me.closeView(false);
		}
	},
	
	onCancelClick: function() {
		var me = this;
		me.fireEvent('viewcancel', me);
		me.closeView(false);
	}
});
