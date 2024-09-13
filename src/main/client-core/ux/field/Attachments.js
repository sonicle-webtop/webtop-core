/* 
 * Copyright (C) 2024 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2024 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.field.Attachments', {
	alternateClassName: 'WTA.ux.field.Attachments',
	extend: 'Sonicle.form.field.TagView',
	alias: 'widget.wtattachmentsfield',
	requires: [
		'Sonicle.Bytes',
		'Sonicle.Number',
		'Sonicle.Utils'
	],
	
	/**
	 * @cfg {String} idField
	 * The underlying {@link Ext.data.Field#name data field name} that targets attachment filename.
	 */
	idField: 'id',
	
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
	 * @cfg {String} limitFilename
	 * The underlying {@link Ext.data.Field#name data field name} that targets attachment size.
	 */
	limitFilename: 50,
	
	itemEditable: false,
	showItemIcon: true,
	growMaxLines: 3,
	
	cls: 'wt-attachmentsfield',
	
	constructor: function(cfg) {
		var me = this,
			SoU = Sonicle.Utils,
			icfg = SoU.getConstructorConfigs(me, cfg, ['idField', 'filenameField', 'sizeField', 'getIcon', 'labelTpl', 'valuesStoreRecordCreator', 'limitFilename']),
			idField = icfg.idField || me.idField,
			filenameField = icfg.filenameField || me.filenameField,
			sizeField = icfg.sizeField || me.sizeField,
			limitFilename = SoU.numberCoalesce(icfg.limitFilename, me.limitFilename, 50);
		
		cfg.itemsValueField = icfg.idField || me.idField;
		cfg.itemsDisplayField = filenameField;
		cfg.iconField = filenameField;
		if (!Ext.isFunction(icfg.getIcon)) {
			cfg.getIcon = function(values, value) {
				return WTF.fileTypeCssIconCls(WTA.Util.getFileExtension(value));
			};
		}
		if (!icfg.labelTpl && !Ext.isEmpty(filenameField) && !Ext.isEmpty(sizeField)) {
			cfg.labelTpl = new Ext.XTemplate(
				'{[this.limitFileName(values.' + filenameField + ', ' + limitFilename + ')]} ({[this.formatBytes(values.' + sizeField + ')]})',
				{
					formatBytes: function(fileSize) {
						return Sonicle.Bytes.format(fileSize);
					},
					limitFileName: function(fileName, maxLength) {
						return Ext.String.ellipsis(fileName, maxLength);
					}
				}
			);
		}
		if (!icfg.tipTpl && !Ext.isEmpty(filenameField)) {
			cfg.tipTpl = '{' + filenameField + '}';
		}
		if (!Ext.isFunction(icfg.valuesStoreRecordCreator)) {
			cfg.valuesStoreRecordCreator = function(data, Model) {
				return new Model({
					id: data.id,
					name: data.name,
					size: data.size
				});
			};
		}
		cfg.disablePicker = true; // This MUST be set here in constructor's config otherwise will NOT work!
		this.callParent([cfg]);
	}
});