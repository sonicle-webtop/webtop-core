/* 
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.app.ServicesTools', {
	alternateClassName: 'WTA.ux.app.ServicesTools',
	extend: 'Ext.Panel',
	
	activeSvcXid: null,
	activeCmpId: null,
	
	constructor: function(cfg) {
		var me = this;
		cfg = Ext.merge(cfg || {}, {
			listeners: {
				beforestaterestore: me.onBeforeStateRestore,
				beforestatesave: me.onBeforeStateSave
			}
		});
		me.callParent([cfg]);
	},
	
	setActiveTool: function(svc, cmp) {
		var me = this;
		me.activeSvcXid = svc.XID;
		if (cmp) {
			me.activeCmpId = cmp.getId();
			me.getLayout().setActiveItem(cmp);
			me.setTitle(cmp.getTitle());
			me.initState();
			me.setVisible(true);
		} else {
			me.activeCmpId = null;
			me.setVisible(false);
		}
	},
	
	getStateId: function() {
		var me = this,
				id = me.callParent();
		if (me.activeCmpId) {
			return id + '-' + me.activeSvcXid;
		} else {
			return id;
		}
	},
	
	onBeforeStateRestore: function(s, state) {
		if (s.getStateId() !== s.stateId) {
			if (state.width) {
				s.setWidth(state.width);
			} else {
				s.setWidth(s.self.calcToolSize().width);
				s.saveState();
			}
		}
		return false; // Stop self restore, we restore width manually above!
	},
	
	onBeforeStateSave: function(s, state) {
		if (s.getStateId() === s.stateId) return false;
	},
	
	statics: {
		calcToolSize: function() {
			var bw = Ext.getBody().getWidth(),
					w, mw;
			if (WT.plTags.desktop) {
				w = (bw < 250) ? bw * 0.7 : 250;
				mw = 100;
			} else if (WT.plTags.tablet) {
				w = (bw < 300) ? bw * 0.8 : 300;
				mw = 100;
			} else {
				w = bw * 0.6;
				mw = 50;
			}
			return {
				width: w,
				minWidth: mw
			};
		}
	}
});
