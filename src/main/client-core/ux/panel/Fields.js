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
Ext.define('Sonicle.webtop.core.ux.panel.Fields', {
	alternateClassName: 'WTA.ux.panel.Fields',
	extend: 'WTA.ux.panel.Panel',
	alias: ['widget.wtfieldspanel'],
	
	componentCls: 'wt-'+'fieldspanel',
	layout: 'anchor',
	
	/**
	 * @cfg {Boolean} paddingTop
	 * Set to `true` to add top-padding CSS class to panel's body
	 */
	paddingTop: true, //FIXME: set to false when base themes consolidation is done (see override in neth-laf project)!
	
	/**
	 * @cfg {Boolean} paddingBottom
	 * Set to `true` to add bottom-padding CSS class to panel's body
	 */
	paddingBottom: true, //FIXME: set to false when base themes consolidation is done (see override in neth-laf project)!
	
	/**
	 * @cfg {Boolean} paddingLR
	 * Set to `true` to add left/right-padding CSS class to panel's body
	 */
	paddingSides: true, //FIXME: set to false when base themes consolidation is done (see override in neth-laf project)!
	
	autoPadding: 'trbl', //FIXME: set to false when base themes consolidation is done (see override in neth-laf project)!
	
	paddingTopCls: 'wt-'+'fieldspanel-body-padding-t',
	paddingBottomCls: 'wt-'+'fieldspanel-body-padding-b',
	paddingSidesCls: 'wt-'+'fieldspanel-body-padding-lr',
	
	initComponent: function() {
		var me = this,
			autoPadding = me.autoPadding,
			ap;
		
		if (me.autoPadding === true) {
			ap = 'trbl';
		} else if (me.autoPadding === false) {
			ap = '';
		} else {
			ap = Sonicle.String.deflt(autoPadding, '');
		}
		if (me.paddingTop === true) me.addBodyCls(me.paddingTopCls);
		if (me.paddingBottom === true) me.addBodyCls(me.paddingBottomCls);
		if (me.paddingSides === true) me.addBodyCls(me.paddingSidesCls);
		me.callParent(arguments);
	}
});
