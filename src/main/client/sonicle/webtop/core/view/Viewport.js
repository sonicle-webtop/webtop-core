Ext.define('Sonicle.webtop.core.view.Viewport', {
	extend: 'Ext.container.Viewport',
	layout: 'border',
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		var header = Ext.create({
			xtype: 'container',
			region: 'north',
			itemId: 'header',
			layout: 'border',
			height: 50,
			items: [
				{
					xtype: 'toolbar',
					region: 'west',
					itemId: 'newtb',
					items: [{
							xtype: 'button',
							text: 'Nuovo'
						}
					]
				}, {
					xtype: 'toolbar',
					region: 'center',
					itemId: 'svctb',
					items: [{
							xtype: 'button',
							text: 'Bottone 1'
						}, {
							xtype: 'button',
							text: 'Bottone 2'
						}, {
							xtype: 'button',
							text: 'Bottone 3'
						}
					]
				}, {
					xtype: 'toolbar',
					region: 'east',
					itemId: 'menutb',
					items: [{
							xtype: 'button',
							text: 'Logout'
						}
					]
				}
			]
		});
		me.add(header);
		
		var svcstb = Ext.create({
			xtype: 'toolbar',
			region: 'west',
			itemId: 'svcstb',
			border: false,
			vertical: true
		});
		WT.getApp().services.each(function(svc) {
			svcstb.add(Ext.create({
				xtype: 'button',
				itemId: svc.getId(),
				iconCls: 'add16',
				tooltip: svc.getName()
			}));
		}, me);
		me.add(svcstb);
		
		var center = Ext.create({
			xtype: 'container',
			region: 'center',
			itemId: 'center',
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
});