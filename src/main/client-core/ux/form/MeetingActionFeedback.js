/* 
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.form.MeetingActionFeedback', {
	alternateClassName: 'WTA.ux.form.MeetingActionFeedback',
	extend: 'Sonicle.form.ActionFeedback',
	alias: ['widget.wtformmeetingactionfeedback'],
	requires: [
		'Sonicle.String'
	],
	uses: [
		'Sonicle.URLMgr'
	],
	
	config: {
		meetingUrl: null
	},
	
	copyIconCls: 'far fa-clone',
	joinIconCls: 'fas fa-video',
	
	text: 'This contains a meeting link.',
	
	initComponent: function() {
		var me = this;
		me.layout = {
			type: 'hbox',
			align: 'middle'
		};
		me.type = 'info';
		me.buttons = [
			{
				xtype: 'button',
				itemId: 'btncopy',
				ui: 'default-toolbar',
				iconCls: me.copyIconCls,
				tooltip: WT.res('wtmeetingfield.copy'),
				disabled: Ext.isEmpty(me.getMeetingUrl()),
				handler: function(s, e) {
					var url = me.getMeetingUrl();
					Sonicle.ClipboardMgr.copy(url);
					me.fireEvent('copy', me, url);
				}
			}, {
				xtype: 'button',
				itemId: 'btnjoin',
				ui: 'default-toolbar',
				iconCls: me.joinIconCls,
				tooltip: WT.res('wtmeetingfield.join'),
				disabled: Ext.isEmpty(me.getMeetingUrl()),
				handler: function(s, e) {
					var url = me.getMeetingUrl();
					Sonicle.URLMgr.open(url, true);
					me.fireEvent('join', me, url);
				}
			}
		];
		me.callParent(arguments);
	},
	
	updateMeetingUrl: function(nv, ov) {
		var me = this,
			urlValid = !Ext.isEmpty(nv), btn;
		
		if (!me.isConfiguring) {
			btn = me.getComponent('btncopy');
			if (btn) btn.setDisabled(!urlValid);
			btn = me.getComponent('btnjoin');
			if (btn) btn.setDisabled(!urlValid);
		}
	}
});
