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
Ext.define('Sonicle.webtop.core.ux.field.htmleditor.TemplateTool', {
	extend: 'Sonicle.form.field.tinymce.tool.base.SplitButton',
	alias: ['widget.wt-htmleditortooltemplate'],
	mixins: {
		tmcetool: 'Sonicle.form.field.tinymce.tool.Mixin'
	},
	requires: [
		'Sonicle.menu.StoreMenu'
	],
	uses: [
		'Sonicle.String',
		'Sonicle.webtop.core.ux.field.htmleditor.TemplateToolBodyBox'
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
	 * @cfg {String} [bodyField=html]
	 */
	bodyField: 'html',
	
	/**
	 * @cfg {String/Object} [tooltip]
	 */
	
	/**
	 * @cfg {String} [emptyText]
	 */
	
	/**
	 * @cfg {String} [emptySelErrorText]
	 */
	
	/**
	 * @cfg {String} [confirmDeleteText]
	 */
	
	/**
	 * @cfg {String} [promptNameTitleText]
	 */
	
	/**
	 * @cfg {String} [promptNameMsgText]
	 */
	
	/**
	 * @cfg {String} [confirmBodyTitleText]
	 */
	
	/**
	 * @cfg {String} [confirmBodyMsgText]
	 */
	
	constructor: function(cfg) {
		var me = this,
				icfg = Sonicle.Utils.getConstructorConfigs(me, cfg, [
					{tooltip: true}, {saveAsTemplateText: true}, {emptyText: true}, {emptySelErrorText: true}, {confirmDeleteText: true}, {promptNameTitleText: true}, {promptNameMsgText: true}, {confirmBodyTitleText: true}, {confirmBodyMsgText: true}
				]),
				applyResIfEmpty = function(icfg, cfg, name, suffkey) {
					if (Ext.isEmpty(icfg[name])) cfg[name] = WT.res('htmleditor.tool.template.'+suffkey);
				};
		
		if (Ext.isEmpty(icfg.tooltip)) {
			cfg.tooltip = {title: WT.res('htmleditor.tool.template.tip.tit'), text: WT.res('htmleditor.tool.template.tip.txt')};
			cfg.overflowText = cfg.tooltip.title;
		} else {
			cfg.overflowText = Ext.isObject(icfg.tooltip) ? Sonicle.Object.coalesce(icfg.tooltip.title, icfg.tooltip.text) : icfg.tooltip;
		}
		applyResIfEmpty(icfg, cfg, 'emptyText', 'emp');
		applyResIfEmpty(icfg, cfg, 'saveAsTemplateText', 'saveAsTemplateText');
		applyResIfEmpty(icfg, cfg, 'emptySelErrorText', 'emptySelErrorText');
		applyResIfEmpty(icfg, cfg, 'confirmDeleteText', 'confirmDeleteText');
		applyResIfEmpty(icfg, cfg, 'promptNameTitleText', 'promptNameTitleText');
		applyResIfEmpty(icfg, cfg, 'promptNameMsgText', 'promptNameMsgText');
		applyResIfEmpty(icfg, cfg, 'confirmBodyTitleText', 'confirmBodyTitleText');
		applyResIfEmpty(icfg, cfg, 'confirmBodyMsgText', 'confirmBodyMsgText');
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			iconCls: 'wt-icon-htmled-template',
			menu: {
				plain: true,
				items: [
					{
						text: me.saveAsTemplateText,
						iconCls: 'wt-icon-htmled-addTemplate',
						handler: function() {
							me.addAsTemplate();
						}
					},
					'-',
					{
						xtype: 'grid',
						itemId: 'grid',
						store: me.store,
						viewConfig: {
							deferEmptyText: false,
							emptyText: me.emptyText
						},
						disableSelection: true,
						hideHeaders: true,
						columns: [
							{
								dataIndex: me.nameField,
								flex: 1
							}, {
								xtype: 'soactioncolumn',
								items: [
									{
										glyph: 'xf044@FontAwesome',
										tooltip: WT.res('act-edit.lbl'),
										handler: function(g, ridx) {
											var sto = g.getStore(),
													rec = sto.getAt(ridx);
											me.promptTemplateBody(rec.get(me.bodyField), function(bid, body) {
												if (bid === 'ok') {
													rec.set(me.bodyField, body);
													me.syncChanges(sto);
												}
											}, me);
										}
									}, {
										glyph: 'xf014@FontAwesome',
										tooltip: WT.res('act-remove.lbl'),
										handler: function(g, ridx) {
											var sto = g.getStore(),
													rec = sto.getAt(ridx);
											WT.confirm(Ext.String.format(me.confirmDeleteText, rec.get(me.nameField)), function(bid) {
												if (bid === 'yes') {
													sto.remove(rec);
													me.syncChanges(sto);
												}
											}, me);
										}
									}
								]
							}
						],
						listeners: {
							rowclick: function(s, rec) {
								var hed = me.getHtmlEditor();
								hed.editorInsertContent(rec.get(me.bodyField));
							}
						},
						height: 150,
						width: 200
					}
				]
			}
		});
		me.callParent(arguments);
		me.on('click', function() {
			me.addAsTemplate();
		}, me);
	},
	
	privates: {
		addAsTemplate: function() {
			var me = this,
					sto = me.getMenu().getComponent('grid').getStore(),
					hed = me.getHtmlEditor(),
					html = hed.editorGetSelectionContent();

			if (Ext.isEmpty(html)) {
				WT.warn(me.emptySelErrorText);

			} else {
				WT.prompt(me.promptNameMsgText, {
					title: me.promptNameTitleText,
					fn: function(bid, name, cfg) {
						if (bid === 'ok') {
							if (Ext.isEmpty(name)) {
								Ext.MessageBox.show(Ext.apply({}, {msg: cfg.msg}, cfg));
							} else {
								me.promptTemplateBody(html, function(bid, body) {
									if (bid === 'ok') {
										var data = {};
										data[me.nameField] = name;
										data[me.bodyField] = body;
										sto.add(sto.createModel(data));
										me.syncChanges(sto);
									}
								}, me);
							}
						}
					}
				});
			}
		},
		
		promptTemplateBody: function(value, cb, scope) {
			var me = this;
			WT.prompt(me.confirmBodyMsgText, {
				instClass: 'Sonicle.webtop.core.ux.field.htmleditor.TemplateToolBodyBox',
				config: {
					buttonText: {
						ok: WT.res('act-save.lbl')
					}
				},
				//instConfig: {},
				title: me.confirmBodyTitleText,
				fn: cb,
				scope: scope,
				value: value
			});
		},
		
		syncChanges: function(store) {
			var me = this,
					sto = store || me.getMenu().getStore();
			sto.sync({
				success: function() {
					me.syncCount++;
				},
				failure: function() {
					sto.reload();
				}
			});
		}
	}
});
