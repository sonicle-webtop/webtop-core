Ext.define('Sonicle.webtop.core.view.Viewport', {
	extend: 'Ext.container.Viewport',
	layout: 'border',
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		var north = Ext.create({
			xtype: 'panel',
			region: 'north',
			html: '<h1 class="x-panel-header">Page Title</h1>',
			border: false,
			margin: '0 0 5 0'
		});
		me.add(north);
		
		var west = Ext.create({
			xtype: 'toolbar',
			region: 'west',
			itemId: 'svcstb',
			border: false,
			width: 48,
			vertical: true
		});
		WT.getApp().services.each(function(svc) {
			WT.Log.debug('Adding {0}', svc.getId());
			west.add(Ext.create({
				xtype: 'button',
				itemId: svc.getId(),
				iconCls: 'add16',
				tooltip: svc.getId()
			}));
		}, me);
		
		me.add(west);
		
		var south = Ext.create({
			xtype: 'panel',
			region: 'south',
			title: 'South Panel',
			collapsible: true,
			html: 'Information goes here',
			split: true,
			height: 100,
			minHeight: 100
		});
		me.add(south);
		
		var center = Ext.create({
			xtype: 'container',
			region: 'center',
			layout: 'border',
			items: [{
					region: 'west',
					collapsible: true,
					title: 'Navigation',
					width: 150
				},{
					region: 'center',
					xtype: 'tabpanel', // TabPanel itself has no title
					activeTab: 0, // First tab active by default
					items: {
						title: 'Default Tab',
						html: 'The first tab\'s content. Others may be added dynamically'
					}
				}
			]
		});
		me.add(center);
	}
	
	
	/*,
	items: [{
			region: 'north',
			html: '<h1 class="x-panel-header">Page Title</h1>',
			border: false,
			margin: '0 0 5 0'
		}, {
			region: 'west',
			collapsible: true,
			title: 'Navigation',
			width: 150
					// could use a TreePanel or AccordionLayout for navigational items
		}, {
			region: 'south',
			title: 'South Panel',
			collapsible: true,
			html: 'Information goes here',
			split: true,
			height: 100,
			minHeight: 100
		}, {
			region: 'east',
			title: 'East Panel',
			collapsible: true,
			split: true,
			width: 150
		}, {
			region: 'center',
			xtype: 'tabpanel', // TabPanel itself has no title
			activeTab: 0, // First tab active by default
			items: {
				title: 'Default Tab',
				html: 'The first tab\'s content. Others may be added dynamically'
			}
	}]
*/
});