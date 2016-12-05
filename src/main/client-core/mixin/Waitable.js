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
Ext.define('Sonicle.webtop.core.mixin.Waitable', {
	alternateClassName: 'WTA.mixin.Waitable',
	extend: 'Ext.Mixin',
	mixinConfig: {
		id: 'waitable'
	},
	
	_waitCount: 0,
	
	/**
	 * Signals to apply the loading mask.
	 * Every time this method will be called, a counter will be incremented.
	 * Mask will be effectively added only on the first call (when counter=1).
	 * @param {String} [msg] The message to show within the indicator.
	 */
	wait: function(msg) {
		var me = this;
		me._waitCount++;		
		if(me._waitCount === 1) {
			var cmp = me.ownerCt || me;
			cmp.setLoading(msg || WT.res('waiting'));
			//cmp.mask(msg || WT.res('waiting'));
		}
	},
	
	/**
	 * Updates (if displayed) the message of the loading indicator.
	 * @param {String} [msg] The message to show within the indicator.
	 */
	waitUpdate: function(msg) {
		var me = this;
		if(me._waitCount >= 1) {
			var cmp = me.ownerCt || me;
			cmp.setLoading(msg || WT.res('waiting'));s
		}
	},
	
	/**
	 * Signals to remove the loading mask.
	 * Every time this method will be called, a counter will be decremented.
	 * Mask will be effectively removed only when the counter is equal to 0.
	 */
	unwait: function() {
		var me = this;
		me._waitCount--;
		if(me._waitCount === 0) {
			var cmp = me.ownerCt || me;
			cmp.setLoading(false);
			//cmp.unmask();
		}
	}
});
