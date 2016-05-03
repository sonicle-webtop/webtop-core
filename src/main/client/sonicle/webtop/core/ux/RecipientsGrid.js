/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */

Ext.define('Sonicle.webtop.core.ux.RecipientsGridNavigationModel',{
	extend: 'Ext.grid.NavigationModel',
	
	//remove cell focus style
	focusCls: '',
	
	startEdit: function(view,record,c) {
		view.grid.plugins[0].startEdit(record, view.ownerCt.getColumnManager().getHeaderAtIndex(c));
	},
	
	onKeyUp: function(ke) {
		var me=this,
			c=ke.position.colIdx;
	
		me.callParent(arguments);
		if (c===1) me.startEdit(ke.view, me.record, 1);
	},
	
	onKeyDown: function(ke) {
		var me=this,
			c=ke.position.colIdx;

		this.callParent(arguments);
		if (c===1) me.startEdit(ke.view, me.record, 1);
    },	
	
	moveUp: function() {
		var me=this,
			ppos=me.previousPosition;
	
		if (ppos.rowIdx>0) {
			me.setPosition(ppos);
			var newpos=me.move("up");
			if (newpos) {
			  me.setPosition(newpos);	
			  me.startEdit(newpos.view, newpos.record, 1);
			  return true;
			}
		}
		return false;
	},
	
	moveDown: function() {
		var me=this,
			ppos=me.previousPosition,
			lastr=ppos.view.store.getCount()-1;
	
		if (ppos.rowIdx<lastr) {
			me.setPosition(me.previousPosition);
			var newpos=me.move("down");
			if (newpos) {
			  me.setPosition(newpos);	
			  me.startEdit(newpos.view, newpos.record, 1);
			  return true;
			}
		}
		return false;
	}
/*    initKeyNav: function(view) {
        var me = this;
		//me.callParent(arguments);
		me.position = new Ext.grid.CellContext(view);

        // Change keynav because default stops any cursor key
		// even when inside the editor
        me.keyNav = new Ext.util.KeyNav({
            target: view,
            ignoreInputFields: true,
            eventName: 'itemkeydown',
            //defaultEventAction: 'stopEvent',
			defaultEventAction: false,

            // Every key event is tagged with the source view, so the NavigationModel is independent.
            processEvent: function(view, record, row, recordIndex, event) {
                return event;
            },
            up: me.onKeyUp,
            down: me.onKeyDown,
            right: me.onKeyRight,
            left: me.onKeyLeft,
            pageDown: me.onKeyPageDown,
            pageUp: me.onKeyPageUp,
            home: me.onKeyHome,
            end: me.onKeyEnd,
            tab: me.onKeyTab,
            space: me.onKeySpace,
            enter: me.onKeyEnter,
            A: {
                ctrl: true,
                // Need a separate function because we don't want the key
                // events passed on to selectAll (causes event suppression).
                handler: me.onSelectAllKeyPress
            },
            scope: me
        });
    }*/
});


Ext.define('Sonicle.webtop.core.ux.CellEditingPlugin', {
	extend: 'Ext.grid.plugin.CellEditing',
	alias: 'plugin.socellediting',
	
    onSpecialKey: function(ed, field, e) {
        var me=this,sm,k=e.getKey();
 
        if (k === e.TAB || k === e.ENTER) {
            e.stopEvent();

			var moved;
			if (e.shiftKey && k === e.TAB) {
				moved=me.navigationModel.moveUp();
			} else {
				moved=me.navigationModel.moveDown();
			}
			
			if (!moved) {
				/*if (ed) {
					// Allow the field to act on tabs before onEditorTab, which ends
					// up calling completeEdit. This is useful for picker type fields.
					ed.onEditorTab(e);
				}

				sm = ed.getRefOwner().getSelectionModel();
				return sm.onEditorTab(ed.editingPlugin, e);*/
			}
			return true;
        }
		else if (k === e.UP) {
			return me.navigationModel.moveUp();
		} else if (k === e.DOWN) {
			return me.navigationModel.moveDown();
		}
    }
});


Ext.define('Sonicle.webtop.core.ux.RecipientsGrid', {
	alternateClassName: 'WT.ux.RegipientsGrid',
	extend: 'Ext.grid.Panel',
	alias: ['widget.wtrecipientsgrid'],
	requires: [
		'Sonicle.webtop.core.ux.field.SuggestCombo',
		'Sonicle.webtop.core.model.Simple'
	],

	/**
	 * @cfg {String} sid
	 * Webtop service ID, if different from Contacts sid.
	 */
	sid: 'com.sonicle.webtop.contacts',
	
	/**
	 * @cfg {String} action
	 * Webtop action, if different from LookupRecipients.
	 */
	action: 'LookupRecipients',
	
	/*
	 * @cfg {String} store
	 * Provides the store to the recipients
	 */
	
	/*
	 * @cfg {Object} fields
	 * Provides { recipientType: 'name', email: 'name' }
	 * field names to be found in the store
	 */
	fields: { recipientType: 'recipientType', email: 'email' },
	
	/**
	 * @cfg {String} suggestionContext
	 * Suggestion context.
	 */
	
	//messageId: null,
	
	initComponent: function() {
		var me = this,
			navModel = Ext.create('Sonicle.webtop.core.ux.RecipientsGridNavigationModel',{ grid: me });
		
		Ext.apply(me, {
			selModel: 'cellmodel',
			hideHeaders: true,
			viewConfig: {
				navigationModel: navModel,
				scrollable: true
			},
			plugins: {
				ptype: 'socellediting',
				clicksToEdit: 1,
				navigationModel: navModel
			},
			columns: [
				{
					width: 50, 
					dataIndex: me.fields.recipientType, 
					editor: Ext.create('Ext.form.ComboBox',{
					  forceSelection: true,
					  queryMode: 'local',
					  displayField: 'desc',
					  triggerAction: 'all',
					  //selectOnFocus: true,
					  width: 30,
					  //editable: false,
					  store: {
						  model: "Sonicle.webtop.core.model.Simple",
						  data: [
							  { id: 'to', desc: WT.res('store.rcptType.to') },
							  { id: 'cc', desc: WT.res('store.rcptType.cc') },
							  { id: 'bcc', desc: WT.res('store.rcptType.bcc') }
						  ]
					  },
					  value: 'to',
					  valueField: 'id'
					}),
					renderer: function(value, md, record, ri, ci, s, view) {
						return '<font color="black">'+WT.res('store.rcptType.'+value)+'</font>';
					}
				},
				{
					flex: 1,
					dataIndex: me.fields.email, 
					//editor: 'textfield'
					editor: Ext.create({
						xtype: 'wtsuggestcombo',
						sid: 'com.sonicle.webtop.mail',
						suggestionContext: 'recipient'
						//width: 400,
					})
				}
			]
		});
		
		me.callParent(arguments);
	},
	
	addRecipient: function(rtype,email) {
		this.getStore().add({ rtype: rtype, email: email});
	}
	
/*	setValue: function(v) {
		console.log("RecipientsGrid: setValue v="+v);
		this.messageId=v;
	}*/
});
