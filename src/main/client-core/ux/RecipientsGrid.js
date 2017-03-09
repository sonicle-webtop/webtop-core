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
Ext.define('Sonicle.webtop.core.ux.RecipientsGrid', {
	alternateClassName: 'WTA.ux.RegipientsGrid',
	extend: 'Ext.grid.Panel',
	alias: ['widget.wtrecipientsgrid'],
	requires: [
		'WTA.ux.grid.RecipientCellEditor',
		'WTA.ux.grid.plugin.RecipientCellEditing',
		'WTA.ux.field.RecipientSuggestCombo',
		'Sonicle.webtop.core.model.Simple',
		'Sonicle.webtop.core.store.RcptType'
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

	/**
	 * @cfg {Object} fields
	 * The default field names on the underlying store
	 */
	fields: { recipientType: 'recipientType', email: 'email' },
	
	rcb: null,

	
	/**
	 * @cfg {String} suggestionContext
	 * Suggestion context.
	 */
	
	//messageId: null,
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			selModel: 'cellmodel',
			hideHeaders: true,
			viewConfig: {
				scrollable: true,
				markDirty: false
			},
			plugins: {
				ptype: 'wtrcptcellediting',
				pluginId: 'wtrcptcellediting',
				clicksToEdit: 1,
				autoEncode: true
			},
			columns: [{
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
						store: Ext.create('WTA.store.RcptType',{ autoLoad: true }),
						value: 'to',
						valueField: 'id'
					}),	
					renderer: function(value, md, record, ri, ci, s, view) {
						return '<font color="black">'+WT.res('store.rcptType.'+value)+'</font>';
					}
				}, {
					flex: 1,
					dataIndex: me.fields.email, 
					//editor: 'textfield'
					editor: me.rcb=Ext.create({
						xtype: 'wtrcptsuggestcombo'
						//width: 400,
					}),
					renderer: Ext.util.Format.htmlEncode
				}
			]
		});
		
		me.callParent(arguments);
	},
	
	addRecipient: function(rtype,email) {
		var me=this,obj={};
		obj[me.fields.recipientType]=rtype;
		obj[me.fields.email]=email;
		var recs=me.getStore().add(obj);
		return recs[0];
	},
	
	getRecipientsCount: function() {
		return this.getStore().getCount();
	},
	
	getRecipientAt: function(row) {
		return this.getStore().getAt(row).get("email");
	},
	
	startEditAt: function(row) {
		var rowIdx = this.getStore().getAt(row);
		this.getPlugin('wtrcptcellediting').startEditByPosition({row: rowIdx, column: 1});
	},
	
	completeEdit: function() {
		this.getPlugin('wtrcptcellediting').completeEdit();
	},
    
	fireExitFocus: function() {
		this.fireEvent('exitfocus',this);
	},
	
    isRecipientComboAutosaveDirty: function() {
        return this.autosaveRcbValue!=this.rcb.getValue();
    },
    
    clearAutosaveDirty: function() {
        this.autosaveRcbValue=this.rcb.getValue();
    },

	clear: function() {
		this.getStore().removeAll();
	}
	
/*	setValue: function(v) {
		console.log("RecipientsGrid: setValue v="+v);
		this.messageId=v;
	}*/
});
