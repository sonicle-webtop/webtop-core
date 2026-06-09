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
Ext.define('Sonicle.webtop.core.ux.field.htmleditor.AITool', {
	extend: 'Sonicle.form.field.tinymce.tool.base.Button',
	alias: ['widget.wt-htmleditortoolai'],

	buttonOk: 'Ok',

	initComponent: function() {
		var me = this,
			cfg = WT.getVar('aiTool'),
			menuItems = [];
		if (!cfg || !cfg.items || !cfg.items.length) {
			var msg = WT.res('ai.notConfigured.message');
			menuItems.push({
				text: msg,
				handler: function() { WT.info(msg); }
			});
		} else {
			me.buttonOk = cfg.buttonOk || 'Ok'; 
			for (var i = 0; i < cfg.items.length; i++) {
				menuItems.push(me.buildMenuItem(cfg.items[i]));
			}
		}
		Ext.apply(me, {
			iconCls: 'wt-icon-ai',
			arrowVisible: false,
			hidden: !WT.getVar('hasAI'),
			menu: {
				plain: true,
				items: menuItems
			}
		});
		me.callParent(arguments);
	},

	buildMenuItem: function(def) {
		var me = this;
		if (def.children && def.children.length) {
			var kids = [];
			for (var i = 0; i < def.children.length; i++) {
				kids.push(me.buildMenuItem(def.children[i]));
			}
			return {
				text: def.label,
				menu: { items: kids }
			};
		}
		return {
			text: def.label,
			handler: function() {
				me.runAction(def);
			}
		};
	},

	runAction: function(def) {
		var me = this, selection = '';
		if (def.requiresSelection) {
			selection = me.getEditorSelection();
			if (!selection || selection === '') {
				WT.error(WT.res('ai.noSelectionError.message'), {
					title: WT.res('ai.noSelectionError.tit'),
					okText: WT.res('act-close.lbl')
				});
				return;
			}
		}
		if (def.input) {
			WT.prompt(def.input.question+'<br><br>', {
				title: def.label,
				multiline: !!def.input.multiline,
				okText: me.buttonOk,
				fn: function(btn, value) {
					if (btn !== 'ok') return;
					if (def.input.required && (!value || value === '')) return;
					me.dispatch(def, value || '', selection);
				}
			});
		} else {
			me.dispatch(def, '', selection);
		}
	},

	dispatch: function(def, userInput, selection) {
		var me = this;
		me.setLoading(true);
		me.callServer(def, userInput, selection, function(success, answer, format) {
			me.setLoading(false);
			if (success) {
				var hed = me.getHtmlEditor();
				if (def.mode === 'show' || !hed) {
					me.showAIView(answer, false, format);
				} else {
					hed.editorInsertContent(answer);
				}
			} else {
				me.showAIView(answer, true);
			}
		});
	},

	callServer: function(def, userInput, selection, cb) {
		var format = 'minimal html',
			params = {
				menuaction: def.id,
				userInput: userInput || '',
				selection: selection || '',
				format: format
			};
		WT.ajaxReq(WT.ID, 'AIPrompt', {
			timeout: WT.getVar('ajaxLongTimeout'),
			params: params,
			callback: function(success, json) {
				if (!success) {
					// WT.ajaxReq passes json.success as the first arg, so this
					// branch covers server-side {success:false, message:...}
					// (e.g. AIQuotaExceededException). Surface the actual
					// message; fall back only if it's truly missing.
					var msg = (json && json.message) ? json.message : 'Request error';
					cb(false, msg, format);
					return;
				}
				cb(true, json.data, format);
			},
			failure: function(response) {
				if (response.status === 0) {
					cb(false, 'Request failed: possible timeout or network issue', format);
				} else {
					cb(false, 'Failure with status: ' + response.statusText, format);
				}
			}
		});
	},

	getEditorSelection: function() {
		var me = this, hed = me.getHtmlEditor(),
			content = hed ? hed.editorGetSelectionContent() : "";
		return content;
	},

	showAIView: function(answer, error, format) {
		var v = WT.createView(WT.ID, 'view.AIView', {
				viewCfg: {}
			}),
			view = v.getView();
		v.show();
		if (error) view.setError(answer);
		else view.setAnswer(answer, format || 'text');
	},

	setLoadMask: function(loadMask) {
		this.loadMask = loadMask;
	}

});
