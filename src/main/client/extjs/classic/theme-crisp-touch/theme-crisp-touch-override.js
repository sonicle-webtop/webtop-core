Ext.define('Ext.theme.crisptouch.Component', {
    override: 'Ext.Component'
}, function() {
	//Ext.namespace('Ext.theme.ui.button').primary = 'default';
	Ext.namespace('Ext.theme.ui.button').toolbar = 'default-toolbar';
});
Ext.define('Sonicle.overrides.panel.KanbanColumn', {
	override: 'Sonicle.panel.KanbanColumn',
	
	border: true
});