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
Ext.define('Sonicle.form.field.DisplayImage', {
	extend: 'Ext.form.field.Text',
	alias: ['widget.sodisplayimagefield'],
	
	ariaRole: 'img',
	focusable: false,
	maskOnDisable: false,
	
	/**
	 * @cfg {String} [fieldCls="x-form-image-field"]
	 * The default CSS class for the field.
	 */
	fieldCls: Ext.baseCSSPrefix + 'form-image-field',
	fieldBodyCls: Ext.baseCSSPrefix + 'form-image-field-body',
	
	fieldSubTpl: [
		'<div id="{id}" role="{role}" {inputAttrTpl}',
		'<tpl if="fieldStyle"> style="{fieldStyle}"</tpl>',
		' class="{fieldCls} {fieldCls}-{ui}"></div>',
		{
			compiled: true,
			disableFormats: true
		}
	],
	
	config: {
		imageUrl: '',
		urlParam: 'id',
		blankImageUrl: Ext.BLANK_IMAGE_URL,
		imageWidth: 100,
		imageHeight: 100,
		geometry: 'square'
	},
	
	getFieldStyles: function() {
		var styles = {
			position: 'relative',
			verticalAlign: 'bottom',
			backgroundColor: '#FFFFFF',
			backgroundRepeat: 'no-repeat',
			backgroundPosition: 'center',
			backgroundSize: 'cover',
			backgroundClip: 'padding-box',
			backgroundOrigin: 'padding-box'
		};
		Ext.apply(styles, this.getBorderRadius());
		return Ext.dom.Helper.generateStyles(styles);
	},
	
	getBorderRadius: function() {
		return (this.getGeometry() === 'circle') ? {borderRadius: '50%'} : {};
	},
	
	onRender: function() {
		var me = this;
		me.callParent();
		//if(me.triggerWrap) me.triggerWrap.applyStyles(me.getBorderRadius());
		if(me.inputWrap) me.inputWrap.applyStyles({padding: '5px'});
		if(me.value) me.applyBackground(me.inputEl, me.value);
	},
	
	setValue: function(value) {
		var me = this;
		me.applyBackground(me.inputEl, value);
		me.callParent(arguments);
		return me;
	},
	
	applyBackground: function(el, value) {
		var me = this, url;
		if(el) {
			url = Ext.isEmpty(value) ? me.getBlankImageUrl() : me.buildBackgroundUrl(value);
			el.applyStyles({
				backgroundImage: 'url(' + url + ')',
				width: me.getImageWidth() + 'px',
				height: me.getImageHeight() + 'px'
			});
		}
	},
	
	buildBackgroundUrl: function(value) {
		var params = {};
		params[this.getUrlParam()] = value;
		return Ext.String.urlAppend(this.getImageUrl(), Ext.Object.toQueryString(params));
	}
});	
