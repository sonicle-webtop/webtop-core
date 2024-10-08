/* 
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.grid.Recipients', {
	alternateClassName: 'WTA.ux.grid.Recipients',
	alias: ['widget.wtrecipientsgridnew'],
	extend: 'Ext.grid.Panel',
	requires: [
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Action',
		'WTA.ux.grid.RecipientCellEditor',
		'WTA.ux.grid.plugin.RecipientCellEditing',
		'WTA.ux.field.RecipientSuggestCombo',
		'Sonicle.webtop.core.model.Simple',
		'Sonicle.webtop.core.store.RcptType'
	],
	uses: [
		'Sonicle.Object'
	],
	
	hideHeaders: true,
	
	config: {
		/**
		 * @cfg {Boolean} readOnly
		 * `true` to mark this as readOnly, disabling rows editing.
		 */
		readOnly: false
	},
	
	/**
	 * @cfg {String} sid
	 * Webtop service ID.
	 */
	sid: undefined,
	
	/**
	 * @cfg {String} action
	 * Webtop action, if different from LookupRecipients.
	 */
	action: 'LookupRecipients',
	
	/**
	 * @cfg {telephone|telephone2|fax|mobile|pager|email|list|im} [targetRecipientFieldType=email]
	 * The desired recipient's field to return during lookups.
	 */
	targetRecipientFieldType: 'email',
	
	/**
	 * @cfg {Boolean} [automaticRecipientAtEnd=false]
	 * Set to `true` to put automatic suggested contacts last in list.
	 */
	automaticRecipientAtEnd: false,
	
	/**
	 * @cfg {Boolean} [showRecipientLink=false]
	 * Set to `true` to put to enable recipient link.
	 */
	showRecipientLink: false,
	
	/**
	 * @cfg {Object/Object[]} actionItems
	 * A single item, or an array of action {@link Ext.grid.column.Action#items Components} to be added to last column.
	 */
	actionItems: undefined,
	
	/**
	 * @cfg {String} recipientTypeField
	 */
	recipientTypeField: 'recipientType',
	
	/**
	 * @cfg {String} recipientValueField
	 */
	recipientValueField: 'email',
	
	/**
	 * @cfg {String} [contactLinkField]
	 * Specifies the field where to read/write the ID of the linked recipient.
	 * Only used if {@link #showRecipientLink} is set to `true`.
	 */
	recipientLinkField: undefined,
	
	recipientTypeHdText: '',
	recipientValueHdText: '',
	
	/**
	 * @cfg {String} recipientValueEmptyText
	 * The default text to place into an empty value field.
	 */
	recipientValueEmptyText: '',
	
	/**
	 * @cfg {String} recipientLinkIconTooltip
	 * The default tooltip to set to link icon.
	 */
	recipientLinkIconTooltip: '',
	
	/**
	 * @event rcpteditblur
	 * Fired by the editing plugin when the editing session of a recipients blurs out.
	 * Typically in these case we can manually move focus away.
	 * @param {Sonicle.webtop.core.ux.grid.Recipients} this 
	 */
	
	constructor: function(cfg) {
		var me = this;
		if (Ext.isEmpty(cfg.sid)) Ext.raise('`sid` is mandatory');
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		
		if (me.rftype !== undefined) me.targetRecipientFieldType = me.rftype;
		if (me.autoLast !== undefined) me.automaticRecipientAtEnd = me.autoLast;
		if (me.showContactLink !== undefined) me.showRecipientLink = me.showContactLink;
		if (me.contactLinkField !== undefined) me.recipientLinkField = me.contactLinkField;
		if (me.fields) {
			if (me.fields.recipientType) me.recipientTypeField = me.fields.recipientType;
			if (me.fields.email) me.recipientValueField = me.fields.email;
		}
		
		me.selModel = 'cellmodel';
		me.viewConfig = Ext.merge(me.viewConfig || {}, {
			scrollable: true,
			markDirty: false
		});
		me.plugins = Sonicle.Utils.mergePlugins(me.plugins, [
			{
				ptype: 'wtrcptcellediting',
				pluginId: 'wtrcptcellediting',
				clicksToEdit: 1,
				autoEncode: true,
				listeners: {
					beforeedit: function() {
						if (me.getReadOnly() === true) return false;
					},
					rcpteditstart: function(s, rec, ed) {
						if (!Ext.isEmpty(me.recipientLinkField) && rec && ed && ed.field.isXType('wtrcptsuggestcombo')) {
							ed.field.setIdValue(rec.get(me.recipientLinkField));
						}
					},
					rcpteditcomplete: function(s, rec, ed) {
						if (!Ext.isEmpty(me.recipientLinkField) && rec && ed && ed.field.isXType('wtrcptsuggestcombo')) {
							rec.set(me.recipientLinkField, ed.field.getIdValue());
						}
					}
				}
			}
		]);
		me.columns = [
			{
				dataIndex: me.recipientTypeField,
				header: me.recipientTypeHdText,
				draggable: false,
				hideable: false,
				editor: {
					xtype: 'combo',
					editable: false,
					typeAhead: false,
					forceSelection: true,
					triggerAction: 'all',	
					store: {
						xclass: 'WTA.store.RcptType',
						autoLoad: true
					},
					valueField: 'id',
					displayField: 'desc',
					value: 'to'
				},
				renderer: WTF.resColRenderer({
					id: WT.ID,
					key: 'store.rcptType',
					keepcase: true
				}),
				width: 80
			}, {
				dataIndex: me.recipientValueField,
				header: me.recipientValueHdText,
				draggable: false,
				hideable: false,
				editor: {
					xtype: 'wtrcptsuggestcombo',
					rftype: me.targetRecipientFieldType,
					autoLast: me.automaticRecipientAtEnd,
					emptyText: me.recipientValueEmptyText
				},
				emptyCellText: me.recipientValueEmptyText,
				flex: 1
			}
		];
		
		if (me.showRecipientLink) {
			me.columns.push({
				xtype: 'soiconcolumn',
				dataIndex: me.recipientLinkField,
				header: WTF.headerWithGlyphIcon('fa fa-link'),
				draggable: false,
				hideable: false,
				menuDisabled: true,
				stopSelection: true,
				getIconCls: function(value) {
					return Ext.isEmpty(value) ? '' : 'fa fa-link';
				},
				getTooltip: function(value) {
					return Ext.isEmpty(value) ? '' : me.recipientLinkIconTooltip;
				},
				width: 40
			});
		}
		if (me.actionItems) {
			me.columns.push({
				xtype: 'soactioncolumn',
				draggable: false,
				hideable: false,
				items: Ext.Array.from(me.actionItems)
			});
		}
		
		me.callParent(arguments);
	},
	
	/**
	 * Try to focus this component.
	 * @param {boolean} selectText Set to `true` to also select the last item in this list and start editing it.
	 * @param {Boolean/Number} [delay] Delay the focus this number of milliseconds (true for 10 milliseconds).
	 * @param {Function} [callback] Only needed if the `delay` parameter is used. A function to call upon focus.
	 * @param {Function} [scope] Only needed if the `delay` parameter is used. The scope (`this` reference) in which to execute the callback.
	 * @returns {Ext.Component} The focused Component, `this` Component.
	 */
	focus: function(selectText, delay, callback, scope) {
		var me = this,
				ret = me.callParent(arguments),
				sto, rec;
		if (selectText === true) {
			sto = me.getStore();
			if (sto && (rec = sto.last())) {
				if (delay) {
					Ext.defer(function() {
						me.startEdit(rec);
					}, Ext.isNumber(delay) ? delay+5 : 10+5);
				} else {
					me.startEdit(rec);
				}
			}
		}
		return ret;
	},
	
	/**
	 * Adds a new recipient.
	 * @param {to|bc|bcc} [type] Recipient type to use as {@link #recipientTypeField}.
	 * @param {Mixed} value Value to use as {@link #recipientValueField}, typically conform to choosen {@link #targetRecipientFieldType}.
	 * @returns {Record} The added record.
	 */
	addRecipient: function(type, value) {
		if (arguments.length === 1) {
			value = type;
			type = 'to';
		}
		var me = this,
				sto = me.getStore(),
				ret;
		
		if (sto) {
			ret = sto.add(sto.createModel(Sonicle.Object.applyPairs({}, [me.recipientTypeField, me.recipientValueField], [type, value])))[0];
		}
		return ret;
	},
	
	/**
	 * Returns the total count of defined recipients.
	 */
	getRecipientsCount: function() {
		var sto = this.getStore(), ret;
		if (sto) ret = sto.getCount();
		return ret;
	},
	
	/**
	 * Removes any recipients.
	 */
	clearRecipients: function() {
		var sto = this.getStore();
		if (sto) sto.removeAll();
	},
	
	/**
	 * Checks if targeted record has empty recipient data or not.
	 * @param {Ext.data.Model|Number} record The record to check or its index.
	 * @returns {Boolean} Returns `true` if found record is empty, `false` otherwise.
	 */
	isRecipientEmpty: function(record) {
		var me = this,
				sto = me.getStore();
		if (Ext.isNumber(record) && sto) record = sto.getAt(record);
		return (record && record.isModel) ? Ext.isEmpty(record.get(me.recipientValueField)) : false;
	},
	
	/**
	 * Paste recipients adding them to the collection.
	 * @param {String} s The pasted text.
	 * @param {Ext.data.Model|Number} from The record or the index of the record to use as start point.
	 * If record does not have any value it will be overwritten, otherwise new 
	 * recipients will be added just after that record.
	 */
	pasteRecipients: function(s, from) {
		var me = this,
			rtypeField = me.recipientTypeField,
			valueField = me.recipientValueField,
			sto = me.getStore(),
			lines = (s || '').split(/\r\n|\r|\n/g),
			rtype = 'to',
			startAtIdx = false,
			idx = -1,
			data = [],
			line, i;
		
		me.endEdit();
		if (sto && from && from.isModel) {
			idx = sto.indexOf(from);
			if (idx !== -1) {
				rtype = from.get(rtypeField);
				startAtIdx = me.isRecipientEmpty(from);
			}
		} else if (sto && Ext.isNumber(from) && from > -1 && from < sto.getCount()) {
			idx = from;
			from = sto.getAt(idx);
			if (from) {
				rtype = from.get(rtypeField);
				startAtIdx = me.isRecipientEmpty(from);
			}
		}
		
		if (sto) {
			for (i = 0; i<lines.length; ++i) {
				line = lines[i].trim();
				if (!Ext.isEmpty(line)) {
					data.push(Sonicle.Object.applyPairs({}, [rtypeField, valueField], [rtype, line]));
				}
			}
			
			// Suspends commented out due the following internal error:
			//  Uncaught TypeError: Cannot read properties of null (reading 'view')
			//  at constructor.onSelectChange (ext-all-debug.js:270588)
			//sto.suspendEvents();
			sto.insert(startAtIdx ? idx : idx+1, data);
			if (startAtIdx) sto.removeAt(idx + data.length);
			//sto.resumeEvents();
			//me.getView().refresh();
		}	
	},
	
	/**
	 * Starts editing the specified record.
	 * @param {Ext.data.Model} record The record to edit.
	 */
	startEdit: function(record) {
		var sto = this.getStore(),
				plu = this.getPlugin('wtrcptcellediting');
		if (sto && record.isModel && plu) {
			plu.startEditByPosition({row: sto.indexOf(record), column: 1});
		}
	},
	
	/**
	 * Ends current editing session, if any.
	 */
	endEdit: function() {
		var plu = this.getPlugin('wtrcptcellediting');
		if (plu) plu.completeEdit();
	},
	
	/**
	 * For backward compatibility!
	 * @deprecated Use clearRecipients instead
	 */
	clear: function() {
		Ext.log.warn('Method "clear" is deprecated, please use "clearRecipients" instead.');
		this.clearRecipients();
	},
	
	/**
	 * For backward compatibility!
	 * @deprecated Use startEdit instead
	 */
	startEditAt: function(row) {
		Ext.log.warn('Method "startEditAt" is deprecated, please use "startEdit" instead.');
		this.startEdit(row);
	},
	
	/**
	 * For backward compatibility!
	 * @deprecated Use endEdit instead
	 */
	completeEdit: function() {
		Ext.log.warn('Method "completeEdit" is deprecated, please use "endEdit" instead.');
		this.endEdit();
	},
	
	/**
	 * For backward compatibility!
	 * @deprecated Use endEdit pasteRecipients
	 */
	loadValues: function(v) {
		Ext.log.warn('Method "loadValues" is deprecated, please use "pasteRecipients" instead.\nLike: this.pasteRecipients(v, this.getSelectionModel().getSelectionStart());');
		this.pasteRecipients(v, this.getSelectionModel().getSelectionStart());
	}
});
