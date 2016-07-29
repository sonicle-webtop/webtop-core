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
	
	config: {
		/**
		 * @cfg {Number} [minDepth=0]
		 * Minimum depth at which begin to display nodes.
		 * Nodes with depth below this value will be hidden.
		 */
		minDepth: 0,
		
		/**
		 * @cfg {Boolean|Number} [shorten=30]
		 */
		shorten: 30
	},
	
	updateStore: function(store, oldStore) {
		var me = this;
		me._needsSync = true;
		if(oldStore) oldStore.un('load', me._onStoreLoad, me);
		if(store) store.on('load', me._onStoreLoad, me);
		if(store && !me.isConfiguring) {
			me.setSelection(store.getRoot());
		}
	},
	
	getSelectionParent: function() {
		var node = this.getSelection(),
				pNode = node ? node.parentNode : null;
		if(pNode && pNode.get('depth') >= this.getMinDepth()) {
			return pNode;
		}
		return null;
	},
	
	/**
	 * Ported ExtJs 6.0.2 implementation plus minDepth utilization.
	 */
	updateSelection: function(node, prevNode) {
		var me = this,
				buttons = me._buttons,
				items = [],
				itemCount = me.items.getCount(),
				needsSync = me._needsSync,
				displayField = me.getDisplayField(),
				md = me.getMinDepth(),
				sho = me.getShorten(),
				needSho = (sho === false) ? false : true,
				shoLen = (needSho && Ext.isNumber(sho)) ? sho : 30,
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
					button.setText(needSho ? Ext.String.ellipsis(text, shoLen) : text);
				} else {
					// no button in the cache - make one and add it to the cache 
					button = buttons[i] = Ext.create({
						xtype: me.getUseSplitButtons() ? 'splitbutton' : 'button',
						ui: me.getButtonUI(),
						hidden: i < md,
						cls: me._btnCls + ' ' + me._btnCls + '-' + me.ui,
						text: needSho ? Ext.String.ellipsis(text, shoLen) : text,
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
		 * @param {Ext.data.TreeModel} prevNode The previously selected node.
		 */
		me.fireEvent('selectionchange', me, node, prevNode);
		
		if (me._shouldFireChangeEvent) {
			/**
			 * @event change
			 * Fires when the user changes the selected record. In contrast to the {@link #selectionchange} event, this does
			 * *not* fire at render time, only in response to user activity.
			 * @param {Ext.toolbar.Breadcrumb} this
			 * @param {Ext.data.TreeModel} node The selected node.
			 * @param {Ext.data.TreeModel} prevNode The previously selected node.
			 */
			me.fireEvent('change', me, node, prevNode);
		}
		me._shouldFireChangeEvent = true;

		me._needsSync = false;
	},
	
	privates: {
		_onStoreLoad: function(s,recs,succ,op,node) {
			var me = this,
					buttons = me._buttons,
					depth = node.get('depth'),
					button = buttons[depth];
			
			if(button && button._breadcrumbNodeId === node.getId()) {
				button.setArrowVisible(node.hasChildNodes());
			}
		}
	}
});
