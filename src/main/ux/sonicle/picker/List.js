/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.picker.List', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.solistpicker',
	requires: [
		'Sonicle.form.trigger.Clear'
	],
	
	referenceHolder: true,
	
	emptyText: 'No items to display',
	searchText: 'Search...',
	okText: 'Ok',
	cancelText: 'Cancel',
	
	valueField: null,
	displayField: null,
	searchField: null,
	
	/**
	 * @cfg {Boolean} [anyMatch=true]
	 * Configure as `false` to disallow matching of the typed characters at any position in the {@link #searchField}'s value.
	 */
	anyMatch: true,
	
	/**
	 * @cfg {Boolean} [caseSensitive=false]
	 * Configure as `true` to make the filtering match with exact case matching.
	 */
	caseSensitive: false,
	
	/**
	 * @cfg {Function} handler
	 * Optional. A function that will handle the pick event of this picker.
	 * The handler is passed the following parameters:
	 *   - `picker` : Sonicle.picker.List
	 * This component.
	 *   - `value` : Mixed
	 * The selected value, according to {@link #valueField}.
	 *   - `record` : Ext.data.Model
	 * The whole record associated to the value.
	 */
	
	/**
	 * @cfg {Object} scope 
	 * The scope (`this` reference) in which the `{@link #handler}` function will be called.
	 * Defaults to this ListPicker instance.
	 */
	
	/**
	 * @event cancelclick
	 * Fires when the cancel button is pressed.
	 * @param {Sonicle.picker.List} this
	 */
	
	/**
	 * @event okclick
	 * Fires when the ok button is pressed.
	 * @param {Sonicle.picker.List} this
	 */
	
	/**
     * @event pick
     * Fires when a value is selected (corresponding row has been dblclicked).
	 * @param {Sonicle.picker.List} this
	 * @param {Mixed} value The selected value, according to {@link #valueField}.
	 * @param {Ext.data.Model} record The whole record associated to the value.
     */
	
	initComponent: function() {
		var me = this;
		
		me.selModel = {
			type: 'rowmodel'
		};
		me.viewConfig = {
			deferEmptyText: false,
			emptyText: me.emptyText
		};
		
		if(!me.columns) {
			me.hideHeaders = true;
			me.columns = [{
				dataIndex: me.displayField,
				flex: 1
			}];
		}
		/*
		me.columns = me.columns || [];
		me.columns = Ext.Array.merge([{
			dataIndex: 
			flex: 1
		}], me.columns);
		*/
		
		if(me.searchField) {
			me.dockedItems = me.makeDockedItems();
		}
		
		me.buttons = [/*{
			text: me.okText,
			handler: function() {
				me.fireEvent('okclick', me);
				me.firePick();
			}
		}, */{
			text: me.cancelText,
			handler: function() {
				me.fireEvent('cancelclick', me);
			}
		}];
		
		me.callParent(arguments);
		me.on('rowdblclick', me.onRowDblClick, me);
		me.on('afterrender', function() {
			me.lookupReference('searchField').focus();
		}, me, {single: true});
	},
	
	makeDockedItems: function() {
		var me = this;
		return [{
			xtype: 'textfield',
			reference: 'searchField',
			dock: 'top',
			hideFieldLabel: true,
			emptyText: me.searchText,
			triggers: {
				clear: {
					type: 'soclear'
				}
			},
			listeners: {
				change: {
					fn: me.onSearchChange,
					scope: me,
					options: {buffer: 300}
				},
				specialkey: {
					fn: me.onSearchSpecialkey,
					scope: me
				}
			}
		}];
	},
	
	search: function(text) {
		var me = this,
				filters = me.getStore().getFilters(),
				filter = me.searchFilter;

		if(text) {
			filters.beginUpdate();
			if (filter) {
				filter.setValue(text);
			} else {
				me.searchFilter = filter = new Ext.util.Filter({
					id: 'search',
					anyMatch: me.anyMatch,
					caseSensitive: me.caseSensitive,
					property: me.searchField,
					value: text
				});
			}
			filters.add(filter);
			filters.endUpdate();
			
		} else if (filter) {
			filters.remove(filter);
		}
	},
	
	firePick: function(rec) {
		var me = this,
				value = me.valueField ? rec.get(me.valueField) : rec.getId(),
				handler = me.handler;
		me.fireEvent('pick', me, value, rec);
		if(handler) handler.call(me.scope || me, me, value, rec);
	},
	
	privates: {
		onSearchChange: function(s) {
			this.search(s.getValue());
		},
		
		onSearchSpecialkey: function(s, e) {
			if(e.getKey() === e.DOWN) this.getSelectionModel().select(0);
		},
		
		onRowDblClick: function(s, rec) {
			this.firePick(rec);
		}
	}
});
