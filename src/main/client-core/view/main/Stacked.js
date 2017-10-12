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
Ext.define('Sonicle.webtop.core.view.main.Stacked', {
	alternateClassName: 'WTA.view.main.Stacked',
	extend: 'WTA.view.main.Abstract',
	requires: [
		'WTA.ux.ServiceButtonStacked'
	],
	
	l1ButtonsLimit: 3,
	addedCount: 0,
	
	getCollapsible: function() {
		return this.getWest();
	},
	
	getToolsCard: function() {
		return this.getWest().lookupReference('tool');
	},
	
	getMainCard: function() {
		return this.getCenter();
	},
	
	addServiceButton: function(desc) {
		var me = this,
				west = me.lookupReference('west'),
				l = west.lookupReference('launchers'),
				l1 = west.lookupReference('launcher1'),
				l2 = west.lookupReference('launcher2'),
				cmp;
		
		me.addedCount++;
		if(me.addedCount <= me.l1ButtonsLimit) {
			l1.add({
				xclass: 'WTA.ux.ServiceButtonStacked',
				sid: desc.getId(),
				handler: 'onLauncherButtonClick'
			});
			//cmp.setBadgeText(Ext.Number.randomInt(0,99)+'');
		} else {
			l2.add({
				xclass: 'WTA.ux.ServiceButton',
				sid: desc.getId(),
				scale: 'small',
				handler: 'onLauncherButtonClick'
			});
		}
		
		// When last service is added...
		if(me.addedCount === me.getServicesCount()) {
			// Toolbar item real height depends on theme (touch or not) and on
			// choosen scale. We need to measure it getting current height 
			// during first item insertion.
			l.setHeight(me.calculateHeight(l1, l2));
			l.updateLayout();
		}
	},
	
	createWestCmp: function() {
		var me = this, items;
		items = [{
			region: 'center',
			xtype: 'toolbar',
			reference: 'launcher1',
			vertical: true,
			border: false,
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: []
		}];
		if(me.getServicesCount() > me.l1ButtonsLimit) {
			items.push({
				region: 'south',
				xtype: 'toolbar',
				reference: 'launcher2',
				enableOverflow: true,
				border: false,
				items: [
					this.createPortalButton({scale: 'small'})
				]
			});
		}
		
		return {
			xtype: 'panel',
			referenceHolder: true,
			split: true,
			collapsible: true,
			border: false,
			width: 200,
			minWidth: 100,
			layout: 'border',
			items: [{
				xtype: 'container',
				region: 'center',
				reference: 'tool',
				layout: 'card',
				items: []
			}, {
				region: 'south',
				xtype: 'container',
				reference: 'launchers',
				height: 100, // Real height will be calculated later...
				layout: 'border',
				items: items
			}],
			listeners: {
				resize: 'onToolResize'
			}
		};
	},
	
	createCenterCmp: function() {
		return {
			xtype: 'container',
			//cls: 'wt-center-stacked',
			layout: 'card',
			items: []
		};
	},
	
	calculateHeight: function(l1, l2) {
		var theme = WT.getTheme(),
				tbMarginTop = WTA.ThemeMgr.getMetric(theme, 'toolbar', 'marginTop') || 6,
				tbMarginBottom = WTA.ThemeMgr.getMetric(theme, 'toolbar', 'marginBottom') || 6,
				tbItemsSpacing = WTA.ThemeMgr.getMetric(theme, 'toolbar', 'itemsSpacing') || 6,
				l1CmpH = l1.getComponent(0).getHeight() || 0,
				l1Items = l1.items.getCount(),
				l2CmpH = l2 ? l2.getComponent(0).getHeight() : 0;
		
		return (tbMarginTop + tbMarginBottom) // l1 toolbar top&bottom margins
			+ (l1CmpH * l1Items) // l1 toolbar height
			+ ((l1Items-1) * tbItemsSpacing) // l1 toolbar items spacing
			+ (l2 ? (tbMarginTop + tbMarginBottom) : 0) // l2 toolbar top&bottom margins
			+ (l2 ? l2CmpH : 0); // l2 toolbar height
		// 24 -> 32 -> 38
	}
});
