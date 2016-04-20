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
	alternateClassName: 'WT.view.main.Stacked',
	extend: 'WT.view.main.Abstract',
	requires: [
		'WT.ux.StackServiceButton'
	],
	
	measuredL1Height: null,
	measuredL2Height: null,
	
	createWestCmp: function() {
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
				height: 155,
				layout: 'border',
				items: [{
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
				}, {
					region: 'south',
					xtype: 'toolbar',
					reference: 'launcher2',
					enableOverflow: true,
					border: false,
					items: []
				}]
			}],
			listeners: {
				resize: 'onToolResize'
			}
		};
	},
	
	createCenterCmp: function() {
		return {
			region: 'center',
			xtype: 'container',
			layout: 'border',
			cls: 'wt-center-stacked',
			items: [{
				region: 'center',
				xtype: 'container',
				reference: 'main',
				layout: 'card',
				items: []
			},
				this.createTaskBar({region: 'south'})
			]
		};
	},
	
	getSide: function() {
		return this.lookupReference('west');
	},
	
	getToolStack: function() {
		return this.lookupReference('west').lookupReference('tool');
	},
	
	getMainStack: function() {
		return this.lookupReference('center').lookupReference('main');
	},
	
	addServiceButton: function(desc) {
		var me = this,
				west = me.lookupReference('west'),
				l = west.lookupReference('launchers'),
				l1 = west.lookupReference('launcher1'),
				l2 = west.lookupReference('launcher2'),
				cmp;
		
		if(l1.items.getCount() < 3) {
			cmp = l1.add(Ext.create('WT.ux.StackServiceButton', desc, {
				handler: 'onLauncherButtonClick'
			}));
			//cmp.setBadgeText(Ext.Number.randomInt(0,99)+'');
			// Toolbar item real height depends on theme (touch or not) and on
			// choosen scale. We need to measure it getting current height 
			// during first item insertion.
			if(!me.measuredL1Height) me.measuredL1Height = cmp.getHeight();
			
		} else {
			cmp = l2.add(Ext.create('WT.ux.ServiceButton', desc, {
				scale: 'small',
				handler: 'onLauncherButtonClick'
			}));
			//cmp.setBadgeText(Ext.Number.randomInt(0,99)+'');
			// Toolbar item real height depends on theme (touch or not) and on
			// choosen scale. We need to measure it getting current height 
			// during first item insertion.
			if(!me.measuredL2Height) me.measuredL2Height = cmp.getHeight();
		}
		l.setHeight(me.calculateHeight(l1, l2));
		l.updateLayout();
	},
	
	calculateHeight: function(l1, l2) {
		var me = this,
				height1 = me.measuredL1Height || 0,
				height2 = me.measuredL2Height || 0,
				l1Rows = l1.items.getCount();
		// 24 -> 32 -> 38
		return (6 + 6) // l1 toolbar top&bottom margins
				+ (height1 * l1Rows) // l1 toolbar height
				+ (6 * (l1Rows -1)) // l1 toolbar items spacing
				+ (6 + 6) // l2 toolbar top&bottom margins
				+ ((l2.items.getCount() > 0) ? height2 : 0); // l2 toolbar height
	}
});
