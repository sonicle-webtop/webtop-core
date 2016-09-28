/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.toolbar.PathBreadcrumb', {
	extend: 'Ext.Container',
	alias: 'widget.sopathbreadcrumb',
	requires: [
		'Ext.button.Split'
	],
	mixins: [
		'Ext.util.FocusableContainer'
	],
	
	isBreadcrumb: true,
	baseCls: Ext.baseCSSPrefix + 'breadcrumb',
	
	layout: 'hbox',
	
	config: {
		/**
		 * @cfg {String} [buttonUI='plain-toolbar']
		 * Button UI to use for breadcrumb items.  Use {@link #extjs-breadcrumb-ui} to
		 * add special styling to the breadcrumb arrows
		 */
		buttonUI: 'plain-toolbar',
		
		/**
		 * @cfg {String} [overflowHandler=null]
		 * The overflowHandler for this Breadcrumb:
		 *
		 * - `null` - hidden overflow
		 * - `'scroller'` to render left/right scroller buttons on either side of the breadcrumb
		 */
		overflowHandler: null,
		
		/**
		 * @cfg {Boolean} [showIcons=true]
		 *
		 * Controls whether or not icons of tree nodes are displayed in the breadcrumb
		 * buttons.  There are 2 possible values for this config:
		 *
		 * 1. `true` - Icons specified in the tree data will be displayed, and the default
		 * "root" and "folder" icons will be displayed for path parts.
		 *
		 * 2. `false` - No icons will be displayed in the breadcrumb buttons, only text.
		 */
		showIcons: true,
		
		/**
		 * @cfg {Boolean} [useSplitButtons=true]
		 * `false` to use regular {@link Ext.button.Button Button}s instead of {@link
		 * Ext.button.Split Split Buttons}.  When `true`, a click on the body of a button
		 * will navigate to the specified node, and a click on the arrow will show a menu
		 * containing the the child nodes.  When `false`, the only mode of navigation is
		 * the menu, since a click anywhere on the button will show the menu.
		 */
		useSplitButtons: true,
		
		/**
		 * @cfg {Boolean|Number} [shorten=30]
		 */
		shorten: 30
	},
	
	/**
	 * @cfg {String} rootFolderIconCls
	 * IconCls to use for the root folder instead of the default one.
	 */
	
	/**
	 * @cfg {String} folderIconCls
	 * IconCls to use for the root folder instead of the default one.
	 */
	
	renderConfig: {
		path: '/'
	},
	
	publishes: ['path'],
	twoWayBindable: ['path'],
	
	_breadcrumbCls: Ext.baseCSSPrefix + 'breadcrumb',
	_btnCls: Ext.baseCSSPrefix + 'breadcrumb-btn',
	_rootFolderIconCls: Ext.baseCSSPrefix + 'breadcrumb-icon-folder',
	_folderIconCls: Ext.baseCSSPrefix + 'breadcrumb-icon-folder',
	
	initComponent: function() {
		var me = this,
				layout = me.layout,
				overflowHandler = me.getOverflowHandler();
		
		if (typeof layout === 'string') {
			layout = {type: layout};
		}
		if (overflowHandler) {
			layout.overflowHandler = overflowHandler;
		}
		me.layout = layout;
		
		// set defaultButtonUI for possible menu overflow handler.
		me.defaultButtonUI = me.getButtonUI();
		
		me.addCls([me._breadcrumbCls, me._breadcrumbCls + '-' + me.ui]);
		me.callParent();
	},
	
	afterComponentLayout: function() {
		var me = this,
				overflowHandler = me.layout.overflowHandler;

		me.callParent(arguments);

		if (overflowHandler && me.tooNarrow && overflowHandler.scrollToItem) {
			overflowHandler.scrollToItem(me.items.getCount()-1);
		}
	},
	
	/**
	 * @method getPath
	 * Returns the current path.
	 * @return {String}
	 */

	/**
	 * @method setPath
	 * Sets the passed path in the breadcrumb component.
	 * @param {String} path The path to display.
	 * @return {Ext.toolbar.Breadcrumb} this The breadcrumb component
	 */
	
	updatePath: function(path) {
		var me = this,
				sho = me.getShorten(),
				needSho = (sho === false) ? false : true,
				shoLen = (needSho && Ext.isNumber(sho)) ? sho : 30,
				showIcons = me.getShowIcons(),
				parts, text, iconCls;
		
		path = path || '';
		if(!Ext.isEmpty(path) && path.substring(path.length-1) === '/') {
			path = path.substring(0, path.length-1);
		}
		parts = path.split('/');
		
		Ext.suspendLayouts();
		me.removeAll();
		for(var i=0; i<parts.length; i++) {
			text = parts[i];
			if(i === 0) {
				iconCls = !Ext.isEmpty(me.rootFolderIconCls) ? me.rootFolderIconCls : me._rootFolderIconCls + '-' + me.ui;
			} else {
				iconCls = !Ext.isEmpty(me.folderIconCls) ? me.folderIconCls : me._folderIconCls + '-' + me.ui;
			}
			
			me.add(Ext.create({
				xtype: me.getUseSplitButtons() ? 'splitbutton' : 'button',
				ui: me.getButtonUI(),
				cls: me._btnCls + ' ' + me._btnCls + '-' + me.ui,
				text: needSho ? Ext.String.ellipsis(text, shoLen) : text,
				iconCls: showIcons ? iconCls : null,
				arrowVisible: (i !== parts.length-1),
				listeners: {
					click: '_onButtonClick',
					arrowclick: '_onButtonArrowClick',
					scope: me
				}
			}));
		}
		Ext.resumeLayouts(true);
		
		me.fireEvent('pathchange', me, path);
	},
	
	getParentPath: function() {
		var path = this.getPath();
		if(Ext.isEmpty(path) || path === '/') return null;
		if(path.substring(path.length-1) === '/') path = path.substring(0, path.length-1);
		return path.substring(0, path.lastIndexOf('/')) + '/';
	},
	
	privates: {
		/**
		 * Handles a click on a breadcrumb button
		 * @private
		 * @param {Ext.button.Split} s
		 * @param {Ext.event.Event} e
		 */
		_onButtonClick: function(s, e) {
			var me = this;
			me.fireEvent('pathclick', me, me._pathUpToButton(s), s);
		},
		
		/**
		 * Handles a click on a breadcrumb button arrow
		 * @private
		 * @param {Ext.button.Split} s
		 * @param {Ext.event.Event} e
		 */
		_onButtonArrowClick: function(s, e) {
			var me = this;
			me.fireEvent('pathclick', me, me._pathUpToButton(s), s);
		},
		
		_pathUpToButton: function(btn) {
			var me = this, path = '/';
			me.items.each(function(itm, i) {
				if(i !== 0) {
					path += itm.getText();
					path += '/';
				}
				if(itm.getId() === btn.getId()) return false;
			});
			return path;
		}
	}
});
