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
Ext.define('Sonicle.webtop.core.ux.field.htmleditor.Field', {
	extend: 'Sonicle.form.field.tinymce.HTMLEditor',
	alias: ['widget.wthtmleditor'],
	
	showElementPath: false,
	showWordCount: false,
	
	constructor: function(cfg) {
		var me = this,
				icfg = Sonicle.Utils.getConstructorConfigs(me, cfg, [
					{fonts: true}, {fontSizes: true}, {fontColors: true}, {fontColorsTilesPerRow: true}, {toolIcons: true}
				]),
				toolTexts = function(toolName) {
					var prefix = 'htmleditor.tool.',
							obj = {
								tooltipTitle: WT.res(prefix + toolName + '.tip.tit'),
								tooltipText: WT.res(prefix + toolName + '.tip.txt')
							};
					Ext.iterate(Ext.Array.slice(arguments, 1), function(textName) {
						obj[textName+'Text'] = WT.res(prefix + toolName + '.' + textName + 'Text');
					});
					return obj;
				},
				toolIcons = function(toolName) {
					var prefix = 'wt-icon-htmled-',
							obj = {
								toolIconCls: prefix + toolName
							};
					Ext.iterate(Ext.Array.slice(arguments, 1), function(icon) {
						obj[icon+'IconCls'] = prefix + icon;
					});
					return obj;
				};
		
		cfg.language = Sonicle.Object.getValue(me.self.LANG_MAP, WT.getLanguageCode(), 'en');
		cfg.pluginPowerPaste = WT.getVar('wtEditorPP');
		cfg.pasteWordMode = cfg.pasteHtmlMode = WT.getVar('wtEditorPasteMode');
		
		cfg.toolTexts = Ext.merge({
			fontselect: toolTexts('fontselect', 'default'),
			fontsizeselect: toolTexts('fontsizeselect', 'default'),
			forecolor: toolTexts('forecolor', 'removeColor'),
			backcolor: toolTexts('backcolor', 'removeColor'),
			bold: toolTexts('bold'),
			italic: toolTexts('italic'),
			underline: toolTexts('underline'),
			formattools: toolTexts('formattools', 'strikethrough', 'subscript', 'superscript', 'blockquote', 'code', 'pre', 'outdent', 'indent', 'clearformat'),
			alignselect: toolTexts('alignselect', 'alignLeft', 'alignCenter', 'alignRight', 'alignJustify'),
			bulllistselect: toolTexts('bulllistselect', 'default', 'circle', 'square'),
			numlistselect: toolTexts('numlistselect', 'default', 'lowerAlpha', 'lowerGreek', 'lowerRoman', 'upperAlpha', 'upperRoman'),
			emoticons: toolTexts('emoticons'),
			symbols: toolTexts('symbols'),
			link: toolTexts('link', 'promptTitle', 'promptMsg', 'suggestMailTo', 'suggestTel', 'suggestProto'),
			image: toolTexts('image', 'insertImageUrl', 'insertImageUrlPromptTitle', 'insertImageUrlPromptMsg', 'insertImageFile'),
			table: toolTexts('table'),
			devtools: toolTexts('devtools', 'codeSample', 'sourceCode')
		}, icfg.toolTexts || {});
		
		cfg.toolIcons = Ext.merge({
			forecolor: toolIcons('forecolor', 'removeColor'),
			backcolor: toolIcons('backcolor', 'removeColor'),
			bold: toolIcons('bold'),
			italic: toolIcons('italic'),
			underline: toolIcons('underline'),
			formattools: toolIcons('formattools', 'strikethrough', 'subscript', 'superscript', 'blockquote', 'code', 'pre', 'outdent', 'indent', 'clearformat'),
			alignselect: toolIcons('', 'alignLeft', 'alignCenter', 'alignRight', 'alignJustify'),
			bulllistselect: toolIcons('bulletList'),
			numlistselect: toolIcons('numberList'),
			emoticons: toolIcons('emoticons'),
			symbols: toolIcons('symbols'),
			link: toolIcons('link'),
			image: toolIcons('image', 'insertImageUrl', 'insertImageFile'),
			table: toolIcons('table'),
			devtools: toolIcons('devtools', 'codeSample', 'sourceCode')
		}, icfg.toolIcons || {});
		
		if (!Ext.isArray(icfg.fonts)) {
			var fonts = WT.getEditorFonts();
			if (fonts) cfg.fonts = fonts;
		}
		if (!Ext.isArray(icfg.fontSizes)) {
			var sizes = WT.getEditorFontSizes();
			if (sizes) cfg.fontSizes = sizes;
		}
		if (!icfg.fontColors) {
			cfg.fontColors = WT.getColorPalette('html');
			cfg.fontColorsTilesPerRow = 8;
		}
		me.callParent([cfg]);
	},
	
	statics: {
		LANG_MAP: {
			'de': 'de',
			'en': 'en',
			'es': 'es',
			'fr': 'fr_FR',
			'hr': 'hr',
			'hu': 'hu_HU',
			'it': 'it',
			'nl': 'nl',
			'pl': 'pl',
			'ru': 'ru'
		}
	}
});
