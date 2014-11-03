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
Ext.define('Sonicle.webtop.core.sdk.BaseView', {
	extend: 'Ext.form.Panel',
	alternateClassName: 'WT.sdk.BaseView',
	mixins: [
		'WT.sdk.mixin.Waitable',
		'WT.sdk.mixin.Submissible'
	],
	
	layout: 'border',
	
	close: function() {
		this.ownerCt.close();
	}
	
	/*
	initComponent: function() {
		var me = this;
		
		me.on('added', function(s,ct) {
			me.initCt(ct);
		}, me, {single: true});
		me.on('removed', function(s,ct) {
			me.cleanupCt(ct);
		}, me, {single: true});
	},
	*/
	
	/*
	initCt: function(ct) {
		var me = this;
		
		if(me.ctInited) return;
		if(ct.isXType('window')) {
			// In this case panel's header is not necessary.
			// It hasn't been rendered yet, we can remove it easly...
			//this.elements = this.elements.replace(',header','');
			//this.header = false;
			// Apply as config, the window is not rendered
			//ct.title = this.title;
			//ct.iconCls = this.iconCls;
			
			ct.on('show', this.onWndShow, this);
			ct.on('close', this.onWndClose, this);
			//if(this.useWG) ct.on('hide', this.onWndHide, this);
			ct.on('beforeclose', this.onWndBeforeClose, this);
		}
		me.ctInited = true;
	}
	*/
});
