/* 
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
/**
 * avaiable CSS classes:
 * 
 * wt-viewport-content
 * wt-theme-text-header1 (to be reviewed)
 * -
 * wt-viewport-topbar
 * wt-viewport-topbar-watermark-bottom
 * wt-viewport-taskbar
 * wt-tool
 * wt-tool-bg
 * wt-tool-hd-wrap
 * wt-tool-hd
 * wt-tool-hd-icon
 * wt-tool-hd-title
 * -
 * wt-viewport-navbar
 * wt-viewport-navbar-watermark-left
 * wt-viewport-navbar-watermark-right
 * wt-viewport-bottombar
 */
Ext.define('Sonicle.webtop.core.viewport.private.Default', {
	alternateClassName: 'WTA.viewport.private.Default',
	extend: 'WTA.viewport.private.BaseAbstract',
	
	getLauncher: function() {
		var drawer = this.drawerCmp();
		return drawer ? drawer.getComponent(1) : undefined;
	},
	
	getTaskbar: function() {
		var bdock = this.bottomDockCmp();
		return bdock ? bdock.getComponent(0) : undefined;
	},
	
	addServiceButton: function(desc) {
		var me = this,
			launcher = me.getLauncher();
		me.doAddServiceButton(launcher, desc);
		if (launcher.items.getCount()>4) {
			me.drawerCmp().setHidden(false);
			me.getToolRegion().setHidden(false);
		}
	},
	
	addLinkButton: function(link) {
		var me = this;
		me.drawerCmp().setHidden(false);
		me.getToolRegion().setHidden(false);
		me.doAddLinkButton(me.getLauncher(), link);
	},
	
	returnDrawerCfg: function() {
		return {
			xtype: 'container',
			hidden: true,
			layout: {
				type: 'hbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'component',
					autoEl: 'div',
					cls: 'wt-viewport-navbar-watermark-left'
				}, {
					xtype: 'toolbar',
					vertical: true,
					border: false,
					cls: 'wt-viewport-navbar',
					enableOverflow: true,
					overflowHandler: 'scroller',
					items: this.createNavbarItemsCfg(),
					height: '100%',
					flex: 1
				}, {
					xtype: 'component',
					autoEl: 'div',
					cls: 'wt-viewport-navbar-watermark-right'
				}
			]
		};
	},

	returnBottomDockCfg: function() {
		var me = this;
		return {
			xtype: 'container',
			layout: 'hbox',
			cls: 'wt-viewport-bottombar',
			items: [
				me.applyMoreCfg(me.createTaskBarCfg(), {
					height: '100%',
					flex: 1
				})
			],
			height: me.bottombarHeight()
		};
	},
	
	privates: {

		createNavbarItemsCfg: function() {
			var me = this;
			return [
				'->', // This controls alignment: remove this element to disable vertical centering!
				//me.applyMoreCfg(me.createPortalButtonCfg(), {scale: me.navbarItemsScale()}),
				me.applyMoreCfg(me.createMoreLinksButtonCfg(), {scale: me.navbarItemsScale()}),
				'->'
			];
		}
	}
});
