/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.toolbar.Breadcrumb', {
	extend: 'Ext.toolbar.Breadcrumb',
	alias: 'widget.sobreadcrumb',
	
	//buttonUI: 'extjs-breadcrumb-ui',
	
	minDepth: 0,
	
	updateSelection: function(node) {
		var me = this,
				buttons = me._buttons,
				items = [],
				itemCount = me.items.getCount(),
				needsSync = me._needsSync,
				displayField = me.getDisplayField(),
				showIcons, glyph, iconCls, icon, newItemCount, currentNode, text, button, id, depth, i;

		Ext.suspendLayouts();

		if (node) {
			currentNode = node;
			depth = node.get('depth');
			newItemCount = depth + 1;
			i = depth;

			while (currentNode) {
				id = currentNode.getId();

				button = buttons[i];

				if (!needsSync && button && button._breadcrumbNodeId === id) {
					// reached a level in the hierarchy where we are already in sync. 
					break;
				}

				text = currentNode.get(displayField);

				if (button) {
					// If we already have a button for this depth in the button cache reuse it 
					button.setText(text);
				} else {
					// no button in the cache - make one and add it to the cache 
					button = buttons[i] = Ext.create({
						xtype: me.getUseSplitButtons() ? 'splitbutton' : 'button',
						ui: me.getButtonUI(),
						hidden: i < me.minDepth,
						cls: me._btnCls + ' ' + me._btnCls + '-' + me.ui,
						text: text,
						showEmptyMenu: true,
						// begin with an empty menu - items are populated on beforeshow 
						menu: {
							listeners: {
								click: '_onMenuClick',
								beforeshow: '_onMenuBeforeShow',
								scope: this
							}
						},
						handler: '_onButtonClick',
						scope: me
					});
				}

				showIcons = this.getShowIcons();

				if (showIcons !== false) {
					glyph = currentNode.get('glyph');
					icon = currentNode.get('icon');
					iconCls = currentNode.get('iconCls');

					if (glyph) {
						button.setGlyph(glyph);
						button.setIcon(null);
						button.setIconCls(iconCls); // may need css to get glyph 
					} else if (icon) {
						button.setGlyph(null);
						button.setIconCls(null);
						button.setIcon(icon);
					} else if (iconCls) {
						button.setGlyph(null);
						button.setIcon(null);
						button.setIconCls(iconCls);
					} else if (showIcons) {
						// only show default icons if showIcons === true 
						button.setGlyph(null);
						button.setIcon(null);
						button.setIconCls(
								(currentNode.isLeaf() ? me._leafIconCls : me._folderIconCls) + '-' + me.ui
								);
					} else {
						// if showIcons is null do not show default icons 
						button.setGlyph(null);
						button.setIcon(null);
						button.setIconCls(null);
					}
				}

				button.setArrowVisible(currentNode.hasChildNodes());
				button._breadcrumbNodeId = currentNode.getId();
				
				currentNode = currentNode.parentNode;
				i--;
			}

			if (newItemCount > itemCount) {
				// new selection has more buttons than existing selection, add the new buttons 
				items = buttons.slice(itemCount, depth + 1);
				me.add(items);
			} else {
				// new selection has fewer buttons, remove the extra ones from the items, but 
				// do not destroy them, as they are returned to the cache and recycled. 
				for (i = itemCount - 1; i >= newItemCount; i--) {
					me.remove(me.items.items[i], false);
				}
			}

		} else {
			// null selection 
			me.removeAll(false);
		}

		Ext.resumeLayouts(true);

		/**
		 * @event selectionchange
		 * Fires when the selected node changes
		 * @param {Ext.toolbar.Breadcrumb} this
		 * @param {Ext.data.TreeModel} node The selected node (or null if there is no selection)
		 */
		me.fireEvent('selectionchange', me, node);

		me._needsSync = false;
	},
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	applySelection222: function(node) {
		var me = this,
				store = me.getStore();
		if (store) {
			node = (node === 'root') ? me.getStore().getRoot() : node;
			if(node && node.get('depth') < me.minDepth) node = null;
		} else {
			node = null;
		}
		return node;
	},
	
	updateSelection222: function(node) {
		var me = this,
				buttons = me._buttons,
				items = [],
				itemCount = me.items.getCount(),
				needsSync = me._needsSync,
				displayField = me.getDisplayField(),
				showIcons, glyph, iconCls, icon, newItemCount, currentNode, text, button, id, depth, i;

		Ext.suspendLayouts();

		if (node) {
			currentNode = node;
			depth = node.get('depth');
			newItemCount = depth - me.minDepth + 1;
			i = depth;

			while (currentNode) {
				id = currentNode.getId();

				button = buttons[i];

				if (!needsSync && button && button._breadcrumbNodeId === id) {
					// reached a level in the hierarchy where we are already in sync. 
					break;
				}

				text = currentNode.get(displayField);

				if (button) {
					// If we already have a button for this depth in the button cache reuse it 
					button.setText(text);
				} else {
					// no button in the cache - make one and add it to the cache 
					button = buttons[i] = Ext.create({
						xtype: me.getUseSplitButtons() ? 'splitbutton' : 'button',
						ui: me.getButtonUI(),
						cls: me._btnCls + ' ' + me._btnCls + '-' + me.ui,
						text: text,
						showEmptyMenu: true,
						// begin with an empty menu - items are populated on beforeshow 
						menu: {
							listeners: {
								click: '_onMenuClick',
								beforeshow: '_onMenuBeforeShow',
								scope: this
							}
						},
						handler: '_onButtonClick',
						scope: me
					});
				}

				showIcons = this.getShowIcons();

				if (showIcons !== false) {
					glyph = currentNode.get('glyph');
					icon = currentNode.get('icon');
					iconCls = currentNode.get('iconCls');

					if (glyph) {
						button.setGlyph(glyph);
						button.setIcon(null);
						button.setIconCls(iconCls); // may need css to get glyph 
					} else if (icon) {
						button.setGlyph(null);
						button.setIconCls(null);
						button.setIcon(icon);
					} else if (iconCls) {
						button.setGlyph(null);
						button.setIcon(null);
						button.setIconCls(iconCls);
					} else if (showIcons) {
						// only show default icons if showIcons === true 
						button.setGlyph(null);
						button.setIcon(null);
						button.setIconCls(
								(currentNode.isLeaf() ? me._leafIconCls : me._folderIconCls) + '-' + me.ui
								);
					} else {
						// if showIcons is null do not show default icons 
						button.setGlyph(null);
						button.setIcon(null);
						button.setIconCls(null);
					}
				}

				button.setArrowVisible(currentNode.hasChildNodes());
				button._breadcrumbNodeId = currentNode.getId();
				
				i--;
				currentNode = (i < me.minDepth) ? null : currentNode.parentNode;
			}

			if (newItemCount > itemCount) {
				// new selection has more buttons than existing selection, add the new buttons 
				items = buttons.slice(itemCount, depth + 1);
				me.add(items);
			} else {
				// new selection has fewer buttons, remove the extra ones from the items, but 
				// do not destroy them, as they are returned to the cache and recycled. 
				for (i = itemCount - 1; i >= newItemCount; i--) {
					me.remove(me.items.items[i], false);
				}
			}

		} else {
			// null selection 
			me.removeAll(false);
		}

		Ext.resumeLayouts(true);

		/**
		 * @event selectionchange
		 * Fires when the selected node changes
		 * @param {Ext.toolbar.Breadcrumb} this
		 * @param {Ext.data.TreeModel} node The selected node (or null if there is no selection)
		 */
		me.fireEvent('selectionchange', me, node);

		me._needsSync = false;
	}
});
