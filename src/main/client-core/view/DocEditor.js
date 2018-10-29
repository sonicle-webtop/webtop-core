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
Ext.define('Sonicle.webtop.core.view.DocEditor', {
	alternateClassName: 'WTA.view.DocEditor',
	extend: 'WTA.sdk.UIView',
	uses: [
		'Sonicle.Crypto',
		'Sonicle.String'
	],
	
	dockableConfig: {
		width: 800,
		height: 600,
		minimizable: true,
		maximizable: true,
		maximized: true
	},
	promptConfirm: true,
	confirm: 'yn',
	
	config: {
		/**
		 * @cfg {Boolean} enableSwitchBanner
		 * If document is opened in view mode and document is editable, 
		 * a switch banner will be displayed. Set to `false` to disable display.
		 */
		enableSwitchBanner: true
	},
	
	/**
	 * @cfg {String} editingId
	 * The editing session identifier.
	 */
	editingId: null,
	
	/**
	 * @cfg {Object} editorConfig
	 * Set of configuration properties that will be used to set-up DocEditor
	 * component. This object may contain any of the following properties:
	 * 
	 * @param {text|spreadsheet|presentation} opts.docType The document type.
	 * @param {String} opts.docExtension The document file extension.
	 * @param {String} opts.docTitle The document tile.
	 * @param {String} opts.docUrl The absolute URL where the source viewed or edited document is stored.
	 * @param {String} [opts.docKey] The unique document identifier used for document recognition; if known the document will be taken from the cache.
	 * @param {Boolean} [opts.editable] If `true` the Edit button will be enabled, defaults to `false`.
	 * @param {Boolean} [opts.downloadable] If `true` the Download button will be enabled, defaults to `true`.
	 * @param {Boolean} [opts.printable] If `true` the Print button will be enabled, defaults to `true`.
	 * @param {Boolean} [opts.commentable] If `true` the Comments menu will be enabled, defaults to `false`.
	 * @param {Boolean} [opts.reviewable] If `true` the Review menu will be enabled, defaults to `false`.
	 * @param {Boolean} [opts.autosave] Defines if the Autosave feature is enabled or disabled. Defaults to `true`.
	 * @param {String} opts.callbackUrl The absolute callback URL to track editor actions.
	 */
	editorConfig: null,
	
	/**
	 * @private
	 * @property {DocsAPI.DocEditor} docEditor
	 * The DocEditor object instance when initialized.
	 */
	
	/**
	 * @private
	 * @property {Boolean} isEdit
	 */
	
	/**
	 * @private
	 * @property {Boolean} isDirty
	 */
	
	constructor: function(cfg) {
		var me = this;
		if (Ext.isEmpty(cfg.editingId)) Ext.raise('`editingId` is mandatory');
		Ext.apply(cfg, {
			confirmMsg: WT.res('docEditor.confirm.close')
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
				divId = me.buildDocEditorPlaceholderId();
		
		Ext.apply(me, {
			layout: 'auto',
			/*
			style: {
				marginTop: '-16px'
			},
			*/
			items: [{
				xtype: 'box',
				style: {
					width: '100%',
					height: '100%',
					overflow: 'hidden'
				},
				html: '<div id="' + divId + '"></div>'
			}]
		});
		me.callParent(arguments);
	},
	
	destroy: function() {
		var me = this;
		if (me.docEditor) me.docEditor.destroyEditor();
		me.callParent();
	},
	
	canCloseView: function() {
		return !(this.isDirty === true);
	},
	
	onCtClose: function() {
		var me = this;
		if (me.isEdit === false) {
			WT.ajaxReq(me.mys.ID, 'CleanupDocManagerEditing', {
				params: {
					editingId: me.editingId
				}
			});
		}
		me.callParent(arguments);
	},
	
	/**
	 * Opens the configured document in specified mode.
	 * @param {view|edit} mode The opening mode.
	 */
	begin: function(mode) {
		if (mode === 'edit') {
			this.beginEdit();
		} else {
			this.beginView();
		}
	},
	
	/**
	 * Opens the configured document in view mode.
	 * If not disabled, a 'Switch mode' banner will be dispayed above the editor.
	 */
	beginView: function() {
		var me = this,
				cfg = me.editorConfig;
		
		me.isEdit = false;
		me.setViewTitle(cfg.docTitle);
		me.setViewIconCls(WTF.fileTypeCssIconCls(cfg.docExtension));
		if ((cfg.editable === true) && me.getEnableSwitchBanner()) {
			me.addDocked(Ext.apply(me.createSwitchTb(), {
				dock: 'top'
			}));
		}
		me.initDocEditor(me.createDocEditorCfg(Ext.apply(cfg, {
			editorMode: 'view'
		})));
	},
	
	/**
	 * Opens the configured document in edit mode.
	 */
	beginEdit: function() {
		var me = this,
				cfg = me.editorConfig;
		
		me.isEdit = true;
		me.setViewTitle(cfg.docTitle);
		me.setViewIconCls(WTF.fileTypeCssIconCls(cfg.docExtension));
		me.initDocEditor(me.createDocEditorCfg(Ext.apply(cfg, {
			editorMode: 'edit'
		})));
	},
	
	initDocEditor: function(edCfg) {
		var me = this;
		if (me.docEditor) {
			me.docEditor.destroyEditor();
			me.docEditor = new DocsAPI.DocEditor(me.buildDocEditorPlaceholderId(), edCfg);
		} else {
			me.loadApi(function(success) {
				if (success) {
					me.docEditor = new DocsAPI.DocEditor(me.buildDocEditorPlaceholderId(), edCfg);
				} else {
					WT.error(WT.res('docEditor.error.loading'));
				}
			}, me);
		}
	},
	
	loadApi: function(callback, scope) {
		var baseUrl = WT.getVar('docServerPublicUrl');
		Ext.Loader.loadScript({
			url: Sonicle.String.urlAppendPath(baseUrl, '/web-apps/apps/api/documents/api.js'),
			onLoad: function() {
				Ext.callback(callback, scope || this, [true]);
			},
			onError: function() {
				Ext.callback(callback, scope || this, [false]);
			},
			scope: this
		});
	},
	
	privates: {
		buildDocEditorPlaceholderId: function() {
			return this.getId() + '-deplaceholder';
		},
		
		buildFakeDocKey: function(docTitle) {
			return Sonicle.String.left(Sonicle.Crypto.md5Hex(docTitle + new Date().getTime().toString()), 20);
		},
		
		createDocEditorCfg: function(cfg) {
			var me = this,
					autosave = !(cfg.autosave === false),
					edCfg = {
						width: '100%',
						height: '100%',
						type: WT.plTags.desktop ? 'desktop' : 'mobile',
						documentType: cfg.docType
					};
			
			if (!Ext.isEmpty(cfg.token)) edCfg = Ext.apply(edCfg, {token: cfg.token});
			edCfg = Ext.apply(edCfg, {
				document: {
					key: Ext.isString(cfg.docKey) ? cfg.docKey : me.buildFakeDocKey(cfg.docTitle),
					fileType: cfg.docExtension,
					title: cfg.docTitle,
					url: cfg.docUrl,
					permissions: {
						edit: (cfg.editable === true) ? true : false,
						download: (cfg.downloadable === false) ? false : true,
						print: (cfg.printable === false) ? false : true,
						comment: (cfg.commentable === true) ? true : false,
						review: (cfg.reviewable === true) ? true : false
					}
				}
			});
			edCfg = Ext.apply(edCfg, {
				editorConfig: {
					mode: (cfg.editorMode === 'edit') ? 'edit' : 'view',
					lang: WT.getVar('language'),
					user: {
						id: WT.getVar('profileId'),
						name: WT.getVar('userDisplayName')
					},
					callbackUrl: Ext.isString(cfg.callbackUrl) ? cfg.callbackUrl : null,
					customization: {
						autosave: autosave,
						chat: false,
						compactToolbar: false,
						feedback: false,
						forcesave: !autosave,
						goback: false
					}
				},
				events: {
					onDocumentReady: Ext.bind(me.onEdDocumentReady, me),
					onDocumentStateChange: Ext.bind(me.onEdDocumentStateChange, me)
				}
			});
			return edCfg;
		},
		
		onEdDocumentStateChange: function(e) {
			var me = this,
					nv = (e.data === true);
			if (me.isDirty !== nv) {
				me.setViewTitle(me.editorConfig.docTitle + (nv ? '*' : ''));
			}
			me.isDirty = nv;
		},
		
		createSwitchTb: function() {
			var me = this;
			return {
				xtype: 'toolbar',
				items: [{
					xtype: 'tbtext',
					html: WTF.headerWithGlyphIcon('fa fa-eye') + '&nbsp;' + WT.res('docEditor.tbi-viewMode.lbl')
				}, '->', {
					xtype: 'button',
					text: 'Switch to edit mode',
					cls: 'wt-doced-switch-btn',
					handler: function(s) {
						me.removeDocked(s.up('toolbar', 1));
						me.beginEdit();
					}
				}, '->', {
					xtype: 'button',
					tooltip: WT.res('act-close.lbl'),
					glyph: 'xf00d@FontAwesome',
					handler: function(s) {
						me.removeDocked(s.up('toolbar', 1));
					}
				}, ' ']
			};
		}
	}
});
