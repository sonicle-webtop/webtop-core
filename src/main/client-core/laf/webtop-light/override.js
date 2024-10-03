Ext.define('Sonicle.overrides.webtop.core.app.Factory', {
	override: 'Sonicle.webtop.core.app.Factory',
	
	coloredCheckboxTreeRenderer: function(opts) {
		return Ext.apply(this.callParent(arguments), {
			geometry: 'circle'
		});
	}
});