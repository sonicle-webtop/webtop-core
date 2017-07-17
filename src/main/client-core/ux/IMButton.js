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
Ext.define('Sonicle.webtop.core.ux.IMButton', {
	alternateClassName: 'WTA.ux.IMButton',
	extend: 'Ext.button.Split',
	alias: ['widget.wtimbutton'],
	requires: [
		'WTA.ux.IMStatusMenu'
	],
	
	config: {
		/**
		 * @cfg {online|away|dnd|offline} presenceStatus
		 * The selected IM status.
		 */
		presenceStatus: null
	},
	
	statusmenu: null,
	
	referenceHolder: true,
	
	constructor: function(cfg) {
		var me = this,
				icfg = me.getInitialConfig(),
				ps = cfg.presenceStatus || icfg.presenceStatus;
		
		Ext.apply(me, {
			tooltip: WT.res(WT.ID, 'wtimbutton.tip', WT.res('im.pstatus.'+ps)),
			iconCls: WTF.cssIconCls(WT.XID, 'im-status-'+ps, 'xs'),
			menu: {
				items: [{
					itemId: 'status',
					text: WT.res('wtimbutton.mni-status.txt'),
					menu: {
						items: me.buildStatusItems(['online', 'away', 'dnd', 'offline'], ps)
					}
				}]
			}
		});
		me.callParent([cfg]);
	},
	
	destroy: function() {
		this.statusmenu = null;
		this.callParent();
	},
	
	updatePresenceStatus: function(nv, ov) {
		var me = this, mnu, itm;
		if (!me.isConfiguring) {
			me.setTooltip(WT.res(WT.ID, 'imbutton.tip', WT.res('im.pstatus.'+nv)));
			me.setIconCls(WTF.cssIconCls(WT.XID, 'im-status-'+nv, 'xs'));
			me.statusmenu.getComponent(nv).setChecked(true);
		}
	},
	
	onRender: function() {
		var me = this;
		me.callParent(arguments);
		me.statusmenu = me.down('menuitem#status').getMenu();
	},
	
	privates: {
		buildStatusItems: function(statuses, active) {
			var me = this, items = [];
			Ext.each(statuses, function(status) {
				items.push({
					xtype: 'menucheckitem',
					itemId: status,
					text: WT.res('im.pstatus.'+status),
					iconCls: WTF.cssIconCls(WT.XID, 'im-pstatus-'+status, 'xs'),
					group: 'pstatus',
					checked: (status === active),
					checkHandler: me.statusCheckHandler,
					scope: me
				});
			});
			return items;
		},
		
		statusCheckHandler: function(itm, checked) {
			if (checked) this.fireEvent('presencestatusselect', this, itm.getItemId(), itm);
		}
	}
});
