/* 
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.grid.Attachments', {
	alternateClassName: 'WTA.ux.grid.Attachments',
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtattachmentsgrid',
	requires: [
		'Sonicle.String',
		'Sonicle.Utils',
		'Sonicle.plugin.DropMask',
		'Sonicle.grid.column.Bytes',
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Link',
		'Sonicle.grid.column.Action'
	],
	
	cls: 'wt-attachmentsgrid',
	
	/**
	 * @cfg {String} sid
	 * WebTop service ID.
	 */
	sid: null,
	
	/**
	 * @cfg {String} uploadContext
	 * The upload context name.
	 */
	uploadContext: null,
	
	/**
	 * @cfg {String} uploadTag
	 * The upload tag to group files.
	 */
	uploadTag: null,
	
	/**
	 * Specify the element ID on which allow D&D. Defaults to this element.
	 */
	dropElementId: null,
	
	/**
	 * @cfg {Boolean} highlightDrop
	 * Set to `true` to show the 'drop here' overlay. Defaults to `false`.
	 */
	highlightDrop: false,
	
	/**
	 * @cfg {String} typeField
	 * The underlying {@link Ext.data.Field#name data field name} that targets attachment type.
	 */
	typeField: 'name',
	
	/**
	 * @cfg {String} filenameField
	 * The underlying {@link Ext.data.Field#name data field name} that targets attachment filename.
	 */
	filenameField: 'name',
	
	/**
	 * @cfg {String} sizeField
	 * The underlying {@link Ext.data.Field#name data field name} that targets attachment size.
	 */
	sizeField: 'size',
	
	/**
	 * @cfg {String} filenameText
	 */
	
	/**
	 * @cfg {String} sizeText
	 */
	
	/**
	 * @cfg {String} fileDropText
	 */
	
	/**
	 * @event attachmentlinkclick
	 * Fired when link is clicked.
	 * @param {Ext.grid.Panel} this
	 * @param {Ext.data.Model} record
	 * @param {Number} rowIndex 
	 */
	
	/**
	 * @event attachmentdownloadclick
	 * Fired when delete button is clicked.
	 * @param {Ext.grid.Panel} this
	 * @param {Ext.data.Model} record
	 * @param {Number} rowIndex 
	 */
	
	/**
	 * @event attachmentdeleteclick
	 * Fired when delete button is clicked.
	 * @param {Ext.grid.Panel} this
	 * @param {Ext.data.Model} record
	 * @param {Number} rowIndex 
	 */
	
	/**
	 * @event attachmentuploaded
	 * Fired when new attachment has been uploaded.
	 * @param {Ext.grid.Panel} this
	 * @param {String} uploadId
	 * @param {Object} file 
	 */
	
	constructor: function(cfg) {
		if (Ext.isEmpty(cfg.sid)) Ext.raise('Config `sid` is mandatory.');
		if (Ext.isEmpty(cfg.uploadContext)) Ext.raise('Config `uploadContext` is mandatory.');
		this.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		
		me.filenameText = me.filenameText || WT.res('wtattachmentsgrid.filename.lbl');
		me.sizeText = me.sizeText || WT.res('wtattachmentsgrid.size.lbl');
		me.fileDropText = me.fileDropText || WT.res('sofiledrop.text');
		
		if (me.highlightDrop === true) {
			me.plugins = Sonicle.Utils.mergePlugins(me.plugins, [
				{
					ptype: 'sodropmask',
					text: me.fileDropText,
					monitorExtDrag: false,
					shouldSkipMasking: function(dragOp) {
						return !Sonicle.plugin.DropMask.isBrowserFileDrag(dragOp);
					}
				}
			]);
		}
		me.columns = me.createColumns();
		me.bbar = me.createBBar();
		
		me.callParent();
	},
	
	createColumns: function() {
		var me = this;
		return [{
			xtype: 'soiconcolumn',
			dataIndex: me.typeField,
			hideable: false,
			header: WTF.headerWithGlyphIcon('far fa-file'),
			getIconCls: function(v, rec) {
				var ext = Sonicle.String.substrAfterLast(rec.get(me.filenameField), '.');
				return WTF.fileTypeCssIconCls(ext);
			},
			iconSize: 16,
			width: 40
		}, {
			xtype: 'solinkcolumn',
			dataIndex: me.filenameField,
			hideable: false,
			header: me.filenameText,
			tdCls: 'wt-theme-text-hyperlink',
			flex: 3,
			listeners: {
				linkclick: function(s, ridx, rec) {
					me.fireEvent('attachmentlinkclick', me, rec, ridx);
				}
			}
		}, {
			xtype: 'sobytescolumn',
			dataIndex: me.sizeField,
			header: me.sizeText,
			flex: 1
		}, {
			xtype: 'soactioncolumn',
			items: [{
				iconCls: 'fas fa-cloud-download-alt',
				tooltip: WT.res('act-download.lbl'),
				handler: function(g, ridx) {
					var rec = g.getStore().getAt(ridx);
					me.fireEvent('attachmentdownloadclick', me, rec, ridx);
				},
				hidden: WT.plTags.mobile
			}, {
				iconCls: 'far fa-trash-alt',
				tooltip: WT.res('act-remove.lbl'),
				handler: function(g, ridx) {
					var rec = g.getStore().getAt(ridx);
					me.fireEvent('attachmentdeleteclick', me, rec, ridx);
				}
			}],
			width: 80
		}];
	},
	
	createBBar: function() {
		var me = this;
		return {
			xtype: 'wtuploadbar',
			sid: me.sid,
			uploadContext: me.uploadContext,
			uploadTag: me.uploadTag,
			dropElement: me.dropElementId ? me.dropElementId : me.getId(),
			listeners: {
				fileuploaded: function(s, file, json) {
					me.fireEvent('attachmentuploaded', me, json.data.uploadId, file);
				}
			}
		};
	}
});
