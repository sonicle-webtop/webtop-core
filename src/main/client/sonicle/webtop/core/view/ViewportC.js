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
Ext.define('Sonicle.webtop.core.view.ViewportC', {
	extend: 'Ext.app.ViewController',
	
	onNavTbButtonClick: function(s) {
		WT.getApp().activateService(s.getItemId());
	},
	
	onMenuButtonClick: function(s) {
		switch(s.getItemId()) {
			case 'logout':
				document.location = 'logout';
				break;
			default:
				alert('Hai premuto il bottone '+s.getItemId());
		}
	},
	
	setServiceToolbar: function(cmp) {
		var co = this.getView().svctb;
		if(!co.contains(cmp)) co.add(cmp);
		co.getLayout().setActiveItem(cmp);
	},
	
	setServiceToolCmp: function(cmp) {
		var co = this.getView().svctool;
		if(!co.contains(cmp)) co.add(cmp);
		co.getLayout().setActiveItem(cmp);
	},
	
	setServiceMainCmp: function(cmp) {
		var co = this.getView().svcmain;
		if(!co.contains(cmp)) co.add(cmp);
		co.getLayout().setActiveItem(cmp);
	}
});
