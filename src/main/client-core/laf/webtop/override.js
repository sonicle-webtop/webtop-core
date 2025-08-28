Ext.define('Sonicle.overrides.webtop.core.app.util.FoldersTree2', {
	override: 'Sonicle.webtop.core.app.util.FoldersTree2',
	
	coloredBotTreeRendererCfg: function(opts) {
		return Ext.apply(this.callParent(arguments), {
			geometry: 'circle'
		});
	},
	
	coloredCheckboxTreeRendererCfg: function(opts) {
		return Ext.apply(this.callParent(arguments), {
			geometry: 'circle'
		});
	}
});
