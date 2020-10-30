/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.field.htmleditor.PublicImageTool', {
	extend: 'Sonicle.form.field.tinymce.tool.base.Button',
	alias: ['widget.wt-htmleditortoolpublicimage'],
	mixins: {
		tmcetool: 'Sonicle.form.field.tinymce.tool.Mixin'
	},
	requires: [
		'Sonicle.menu.StoreMenu'
	],
	uses: [
		'Sonicle.String'
	],
	
	/**
	 * @cfg {Ext.data.Store} store
	 */
	store: null,
	
	/**
	 * @cfg {String} idField
	 */
	
	/**
	 * @cfg {String} [nameField=name]
	 */
	nameField: 'name',
	
	/**
	 * @cfg {String} [urlField=url]
	 */
	urlField: 'url',
	
	/**
	 * @cfg {String/Object} [tooltip]
	 */
	
	/**
	 * @cfg {String} [emptyText]
	 */
	
	constructor: function(cfg) {
		var me = this,
				icfg = Sonicle.Utils.getConstructorConfigs(me, cfg, [
					{tooltip: true}, {emptyText: true}
				]),
				applyResIfEmpty = function(icfg, cfg, name, suffkey) {
					if (Ext.isEmpty(icfg[name])) cfg[name] = WT.res('htmleditor.tool.publicimage.'+suffkey);
				};
		
		if (Ext.isEmpty(icfg.tooltip)) {
			cfg.tooltip = {title: WT.res('htmleditor.tool.publicimage.tip.tit'), text: WT.res('htmleditor.tool.publicimage.tip.txt')};
			cfg.overflowText = cfg.tooltip.title;
		} else {
			cfg.overflowText = Ext.isObject(icfg.tooltip) ? Sonicle.Object.coalesce(icfg.tooltip.title, icfg.tooltip.text) : icfg.tooltip;
		}
		applyResIfEmpty(icfg, cfg, 'emptyText', 'emp');
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
				SoS = Sonicle.String;
		Ext.apply(me, {
			iconCls: 'wt-icon-htmled-publicImage',
			menu: {
				xtype: 'sostoremenu',
				store: me.store,
				idField: me.idField,
				textField: me.nameField,
				emptyText: me.emptyText,
				listeners: {
					click: function(s, item) {
						if (item) {
							var hed = me.getHtmlEditor(),
									rec = s.findStoreRecordByItemId(item.getItemId()),
									url;
							if (rec) {
								url = rec.get(me.urlField);
								hed.editorInsertContent(hed.editorCreateHTML('img', {src: url, alt: SoS.htmlEncode(rec.get(me.nameField) || SoS.substrAfterLast(url, '/', false) || '')}));
							}
						}
					}
				}
			}
		});
		me.callParent(arguments);
	}
});
