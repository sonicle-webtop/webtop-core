Ext.define('Sonicle.webtop.core.view.Viewport', {
	extend: 'Ext.container.Viewport',
	layout: 'border',
	
	requires: [
		'Sonicle.webtop.core.view.ViewportController',
		'Sonicle.webtop.core.view.Menu'
	],
	controller: Ext.create('Sonicle.webtop.core.view.ViewportController'),
	
	svctbs: null,
	svctools: null,
	svcmains: null,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		var header = Ext.create({
			xtype: 'container',
			region: 'north',
			itemId: 'header',
			layout: 'border',
			height: 40,
			items: [
				{
					xtype: 'toolbar',
					region: 'west',
					itemId: 'newtb',
					style: {
						paddingTop: 0,
						paddingBottom: 0
					},
					items: [{
							xtype: 'button',
							text: 'Nuovo'
						}
                                        ]
				}, {
					xtype: 'container',
					region: 'center',
					itemId: 'svctbs',
					layout: 'card',
					defaults: {
						padding: 0
					}
				}, {
					xtype: 'toolbar',
					region: 'east',
					itemId: 'menutb',
					style: {
						paddingTop: 0,
						paddingBottom: 0
					},
					items: [{
							xtype: 'button',
							glyph: 0xf0c9,
							menu: {
								xtype: 'menu',
								plain: true,
								width: 150,
								items: [{
										xtype: 'buttongroup',
										columns: 2,
										defaults: {
											xtype: 'button',
											scale: 'large',
											iconAlign: 'center',
											width: '100%',
											handler: 'onMenuButtonClick'
										},
										items: [{
												itemId: 'feedback',
												tooltip: 'Feedback',
												glyph: 0xf1d8
											}, {
												itemId: 'whatsnew',
												tooltip: 'novità',
												glyph: 0xf0eb
											}, {
												itemId: 'settings',
												tooltip: 'Impostazioni',
												glyph: 0xf013
											}, {
												xtype: 'container'
											}, {
												itemId: 'logout',
												colspan: 2,
												scale: 'small',
												tooltip: 'Esci',
												glyph: 0xf011
											}
										]
									}
								]
							}
						}
					]
				}
			]
		});
		me.svctbs = header.queryById('svctbs');
		me.add(header);
		
		var navtb = Ext.create({
			xtype: 'toolbar',
			region: 'west',
			itemId: 'navtb',
			border: false,
			vertical: true
		});
		WT.getApp().services.each(function(svc) {
			navtb.add(Ext.create({
				xtype: 'button',
				itemId: svc.getId(),
				iconCls: 'add16',
				tooltip: svc.getName(),
				handler: 'onNavTbButtonClick'
			}));
		}, me);
		me.add(navtb);
		
		var center = Ext.create({
			xtype: 'container',
			region: 'center',
			itemId: 'center',
			layout: 'border',
			defaults: {
				split: true
			},
			items: [{
					xtype: 'container',
					region: 'west',
					itemId: 'svctools',
					layout: 'card',
					collapsible: true
				}, {
					xtype: 'container',
					region: 'center',
					itemId: 'svcmains',
					layout: 'card'
				}
			]
		});
		me.svctools = center.queryById('svctools');
		me.svcmains = center.queryById('svcmains');
		me.add(center);
	}
});
