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

Ext.define('Sonicle.webtop.core.ux.AddressBook', {
	alternateClassName: 'WTA.ux.AddressBook',
	extend: 'Ext.form.Panel',
	alias: ['widget.wtaddressbook'],
	
	requires: [
		'WTA.model.InternetRecipient'
	],
	
	/**
	 * @cfg {Object[]} recipients
	 * The initial recipients to load
	 * as an array of objects like : { type: 'to/cc/bcc', email: 'email' }
	 */
	recipients: null,
	
	/**
	 * @cfg {String} sourceId
	 * The sourceId to lookup, or '*' for any
	 */
	sourceId: '*',

	mys: null,
	
	dirty: false,
	layout: 'border',
	referenceHolder: true,
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			tbar: [
				{
					xtype: 'textfield',
					reference: 'query',
					plugins: [
						'soenterkeyplugin'
					],
					width: 150,
					listeners: {
						enterkey: {
							fn: function() {
								me.actionSearch();
							}
						}
					}
				},
				{
					xtype: 'button',
					text: WT.res('word.search'),
					handler: me.actionSearch,
					scope: me
				}
			]
		});
		me.callParent(arguments);
		
		me.add({
			xtype: 'panel',
			region: 'west',
			layout: 'fit',
			split: true,
			width: 250,
			items: [
				{
					xtype: 'grid',
					reference: 'grdContacts',
					margin: '5 5 5 5',
					border: true,
					hideHeaders: true,
					selModel: { 
						type: 'sorowmodel',
						mode: 'MULTI'
					},
					store: {
						model: 'WTA.model.InternetRecipient',
						proxy: WTF.apiProxy(WT.ID, 'ManageInternetRecipients', 'recipients')
					},
					columns: [
						{
							dataIndex: 'address',
							width: '100%',
							renderer: function(v,md,r,ri,ci,s) {
								return Ext.util.Format.htmlEncode(me._buildEmail(r.get("address"),r.get("personal")));
							}
						}
					],
					listeners: {
						rowdblclick: {
							fn: function(g,r,tr,ri,e,eopts) {
								me.addRecipient('to',me._buildEmail(r.get("address"),r.get("personal")));
							}
						}
					}
				}
			]
		});
		me.add({
			xtype: 'form',
			region: 'center',
            layout: {
				type: 'table',
                tableAttrs: {
                    style: {
                        width: '100%'
                    }
                },
                columns: 1
            },
			items: [
				me._createRecipientsPanel('grdto',WT.res("store.rcptType.to"),function() { me.addSelection('to')}, function() { me.delSelection('to') }),
				me._createRecipientsPanel('grdcc',WT.res("store.rcptType.cc"),function() { me.addSelection('cc')}, function() { me.delSelection('cc') }),
				me._createRecipientsPanel('grdbcc',WT.res("store.rcptType.bcc"),function() { me.addSelection('bcc')}, function() { me.delSelection('bcc') })
			]
		});
		if (me.recipients)
			me.on('render',function() {
				Ext.each(me.recipients,function(r) {
					if (r && !Ext.isEmpty(r.email.trim())) me.addRecipient(r.type,r.email);
				});
			});
	},
	
	setSource: function(src) {
		this.sourceId=src;
	},
	
	actionSearch: function() {
		var me=this;
		me.lookupReference('grdContacts').getStore().reload({
			params: {
				sources: me.sourceId==="*"?[]:[ me.sourceId ],
				query: me.lookupReference("query").getValue(),
				limit: 0
			}
		});
	},
	
    addSelection: function(type) {
		var me=this,
            grdto=me.lookupReference('grdto'),
            grdcc=me.lookupReference('grdcc'),
            grdbcc=me.lookupReference('grdbcc'),
			srcgrd=me.lookupReference('grdContacts'),
			srcst=srcgrd.getStore(),
			srcsel=srcgrd.getSelection();
	
		Ext.each(srcsel,function(r) {
			var addr=r.get("address"),
				pers=r.get("personal");
			if (!grdto.store.findRecord('address',addr) && !grdcc.store.findRecord('address',addr) && !grdbcc.store.findRecord('address',addr)) {
				me.addRecipient(type,me._buildEmail(addr,pers));
				srcst.remove(r);
			}
		});
    },
	
	delSelection: function(type) {
        var me=this,
			srcgrd=me.lookupReference('grd'+type),
			srcst=srcgrd.getStore(),
			srcsel=srcgrd.getSelection();
	
		srcst.remove(srcsel);
	},
	
	addRecipient: function(type,email) {
        var me=this,
			dstgrd=me.lookupReference('grd'+type);
		dstgrd.store.add({ email: email });
	},
	
	getRecipients: function() {
		var me=this,
			rcpts=[];
		me._addRecipients(rcpts,me.lookupReference('grdto').store,'to');
		me._addRecipients(rcpts,me.lookupReference('grdcc').store,'cc');
		me._addRecipients(rcpts,me.lookupReference('grdbcc').store,'bcc');
		return rcpts;
	},
	

	_addRecipients: function(rcpts,store,type) {
		Ext.each(store.getRange(),function(r) {
			rcpts[rcpts.length]={ type: type, email: r.get("email") };
		});
	},
	
	_buildEmail: function(addr,pers) {
		return pers?pers+" <"+addr+">":addr;
	},
	
    _createRecipientsPanel: function btn(ref,text,addHandler,delHandler) {
		return {
			xtype: 'panel',
			layout: 'border',
			margin: '5 5 5 5',
            height: 120,
			items: [
				{
					region: 'west',
					layout: {
						type:'vbox',
						align: 'center'
					},
					width: 70,
					items: [
						{xtype: 'label', text: text},
						{xtype: 'button', text:'>>', width: 40, handler: addHandler},
						{xtype: 'button', text:'<<', width: 40, handler: delHandler}
					]
				},{
					region: 'center',
					layout: 'fit',
					border: true,
					items: [
						{
							xtype: 'grid',
							reference: ref,
							region: 'center',
							selModel: { 
								type: 'sorowmodel',
								mode: 'MULTI'
							},
							hideHeaders: true,
							columns: [{
									width: 245,
									dataIndex: 'email'
							}],
							store: new Ext.data.ArrayStore({
								data: [],
								fields: ['email']
							}),
							listeners: {
								rowdblclick: {
									fn: function(g,r,tr,ri,e,eopts) {
										g.store.remove(r);
									}
								}
							}
						}
					]
				}
			]
		};
	}
	
	
});
