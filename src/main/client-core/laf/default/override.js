Ext.define('Sonicle.overrides.panel.KanbanColumn', {
	override: 'Sonicle.panel.KanbanColumn',
	
	border: true
});
Ext.define('Sonicle.overrides.grid.RowEditorButtons', {
	override: 'Ext.grid.RowEditorButtons',
	
	reverseButtons: true
});
Ext.define('Sonicle.overrides.window.MessageBox', {
	override: 'Ext.window.MessageBox',
	
	/* Swap buttons and align them */
	reverseButtons: true
});
