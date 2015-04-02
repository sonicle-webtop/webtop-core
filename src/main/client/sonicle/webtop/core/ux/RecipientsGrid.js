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
	alternateClassName: 'WT.ux.RegipientsGrid',
	extend: 'Ext.grid.Panel',
	alias: ['widget.wtrecipientsgrid'],
	requires: [
		'Sonicle.webtop.core.model.Recipient',
		'Sonicle.webtop.core.model.RecipientType'
	],

	/**
	 * @cfg {String} sid
	 * Webtop service ID, if different from Contacts sid.
	 */
	sid: 'com.sonicle.webtop.contacts',
	
	/**
	 * @cfg {String} action
	 * Webtop action, if different from LookupContacts.
	 */
	action: 'LookupContacts',
	
	/**
	 * @cfg {String} suggestionContext
	 * Suggestion context.
	 */
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			selModel: 'cellmodel',
			hideHeaders: true,
			plugins: {
				ptype: 'cellediting',
				clicksToEdit: 1
			},
			store: {
				model: 'Sonicle.webtop.core.model.Recipient',
				data: [ ['to',''] ]
			},
			
			columns: [
				{
					width: 50, 
					dataIndex: 'recipientType', 
					editor: Ext.create('Ext.form.ComboBox',{
					  forceSelection: true,
					  queryMode: 'local',
					  displayField: 'description',
					  triggerAction: 'all',
					  //selectOnFocus: true,
					  width: 30,
					  //editable: false,
					  store: {
						  model: "Sonicle.webtop.core.model.RecipientType",
						  data: [
							  { recipientType: 'to', description: WT.res('recipienttype.to') },
							  { recipientType: 'cc', description: WT.res('recipienttype.cc') },
							  { recipientType: 'bcc', description: WT.res('recipienttype.bcc') }
						  ]
					  },
					  value: 'to',
					  valueField: 'recipientType'
					}),
					renderer: function(value, md, record, ri, ci, s, view) {
						return '<font color="black">'+WT.res('recipient.'+value)+'</font>';
					}
				},
				{
					width: 400,
					dataIndex: 'email', 
					editor: 'textfield'
				}
			]
		});
		
		me.callParent(arguments);
	}	
});