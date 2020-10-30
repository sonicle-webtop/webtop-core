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
	
	waiting: function() {
		return this._waitCount > 0;
	},
	
	/**
	 * Shows a loading-mask with message.
	 * Depending on the value of update parameter, this will increment an 
	 * internal counter in order to make sure that when calling {@link #unwait}, 
	 * the loading-mask is removed after the same number of calls. 
	 * When update is `true`, the current only loading-mask's message is updated 
	 * and counter is not incremented. When `false` (default behaviour) the 
	 * counter is incremented but message is effectively applied only at the 
	 * first call time. Same situation with update set to `true` and no mask 
	 * displayed yet.
	 * @param {String} [msg] The message to show within the indicator.
	 * @param {Boolean} [update=false] Whether to update current message.
	 */
	wait: function(msg, update) {
		var me = this, cmp = me.ownerCt || me;
		if (me._waitCount === 0) {
			cmp.setLoading(msg || WT.res('waiting'));
			me._waitCount++;
		} else if ((update === true) && me._waitCount > 0) {
			cmp.setLoading(msg || WT.res('waiting'));
		} else {
			me._waitCount++;
		}
	},
	
	/**
	 * Hides the loading-mask, if visible
	 * Every time this method will be called, an internal counter is decremented,
	 * hiding loading-mask effectively only after the same number of calls to 
	 * {@link #wait} with update `false`.
	 */
	unwait: function(force) {
		var me = this, cmp = me.ownerCt || me;
		me._waitCount--;
		if (me._waitCount === 0 || (force === true)) {
			cmp.setLoading(false);
			me._waitCount = 0;
		}
	}
});
