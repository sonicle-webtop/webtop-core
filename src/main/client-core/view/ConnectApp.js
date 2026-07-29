/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.view.ConnectApp', {
	alternateClassName: 'WTA.view.ConnectApp',
	extend: 'WTA.sdk.UIView',

	dockableConfig: {
		title: '{connectapp.tit}',
		iconCls: 'wt-glyph-qrcode',
		width: 380,
		height: 520
	},
	promptConfirm: false,

	initComponent: function() {
		var me = this;
		me.callParent(arguments);

		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'center'
			},
			scrollable: true,
			padding: 20,
			items: [
				{
					xtype: 'component',
					width: '100%',
					html: Ext.String.htmlEncode(WT.res('connectapp.info.txt'))
				}, {
					xtype: 'image',
					margin: '20 0 0 0',
					width: 240,
					height: 240,
					src: WTF.processBinUrl(WT.ID, 'GetConnectQRCode', {size: 240})
				}, {
					xtype: 'component',
					margin: '20 0 0 0',
					style: 'font-weight:bold;text-align:center',
					html: Ext.String.htmlEncode(WT.getVar('wtConnectQrUser'))
				}
			]
		});
	}
});
