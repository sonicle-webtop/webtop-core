Ext.define('Sonicle.overrides.webtop.core.app.util.FoldersTree2', {
	override: 'Sonicle.webtop.core.app.util.FoldersTree2',
	
	coloredBoxTreeRendererCfg: function(opts) {
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
Ext.define('Sonicle.overrides.mixin.Avatar', {
	override: 'Sonicle.mixin.Avatar'
	
}, function() {
	//Zinc/300 Red/300, Orange/300, Amber/300, Lime/300, Emerald/300, Sky/300, Indigo/300, Purple/300, Pink/300, Rose/300
	Sonicle.mixin.Avatar.palette = ['#d4d4d8', '#fca5a5', '#fdba74', '#fcd34d', '#bef264', '#6ee7b7', '#7dd3fc', '#a5b4fc', '#d8b4fe', '#f9a8d4', '#fda4af'];
	//Sonicle.mixin.Avatar.palette = ['#F44336', '#64748b', '#78716c', '#ef4444', '#f97316', '#f59e0b', '#eab308', '#84cc16', '#22c55e', '#10b981', '#14b8a6', '#06b6d4', '#0ea5e9', '#3b82f6', '#6366f1', '#8b5cf6', '#a855f7', '#d946ef', '#ec4899', '#f43f5e'];
});
Ext.define('Sonicle.overrides.button.Avatar', {
	override: 'Sonicle.button.Avatar',
	
	colors: ['#000000']
	//colors: ['#525252']
});

/*
Ext.define('Sonicle.overrides.webtop.core.viewport.private.Default', {
	override: 'Sonicle.webtop.core.viewport.private.Default',
	
	privates: {
		navbarItemsScale: function() {
			return 'large';
		}
	}
});
*/
Ext.define('Sonicle.overrides.grid.RowEditorButtons', {
	override: 'Ext.grid.RowEditorButtons',
	
	reverseButtons: true,
	updateButtonUI: '{primary}',
	cancelButtonUI: '{secondary}'
});
Ext.define('Sonicle.overrides.window.MessageBox', {
	override: 'Ext.window.MessageBox',
	
	/* Swap buttons, align them and customize pseudo-UI */
	reverseButtons: true,
	buttonsAlign: 'right',
	buttonPseudoUi: {
		ok: '{primary}',
		yes: '{primary}',
		no: '{tertiary}',
		cancel: '{tertiary}'
	}
});