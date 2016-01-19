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
Ext.define('Sonicle.form.field.Image', {
	extend: 'Ext.form.field.Text',
	alias: ['widget.soimagefield'],
	requires: [
		'Ext.ux.form.trigger.Clear',
		'Sonicle.upload.Uploader'
	],
	
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
	postSubTpl: [
			'</div>', // end inputWrap
			'<tpl for="triggers">{[values.renderTrigger(parent)]}</tpl>',
			'<div id={cmpId}-pluWrap data-ref="pluWrap" style="display:none;"></div>',
		'</div>' // end triggerWrap
	],
	childEls: ['triggerWrap','inputWrap','pluWrap'],
	
	config: {
		imageUrl: '',
		urlParam: 'id',
		blankImageUrl: Ext.BLANK_IMAGE_URL,

		imageWidth: 100,
		imageHeight: 100,
		geometry: 'square',
		uploadDisabled: false,
		clearTriggerCls: '',
		uploadTriggerCls: ''
	},
	
	uploader: null,
	
	constructor: function(cfg) {
		var me = this, 
				iniCfg = Ext.apply({}, cfg, me.getInitialConfig()), 
				triggers = {};
		
		triggers = Ext.apply(triggers, {
			clear: {
				type: 'clear',
				weight: -1,
				cls: iniCfg.clearTriggerCls,
				handler: me.onClearClick
			}
		});
		if(!iniCfg.uploadDisabled) {
			triggers = Ext.apply(triggers, {
				upload: {
					type: 'hideable',
					weight: -1,
					hideOn: 'value',
					cls: iniCfg.uploadTriggerCls,
					handler: me.onUploadClick
				}
			});
		}
		Ext.apply(cfg, {triggers: triggers});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		if(!me.getUploadDisabled()) {
			me.uploader = Ext.create('Sonicle.upload.Uploader', me, me.initialConfig.uploaderConfig);
			me.on('afterrender', function() {
				//me.uploader.setBrowseButton(me.triggers['upload'].domId);
				////me.uploader.setBrowseButton(me.inputWrap.getId());
				//me.uploader.setContainer(me.bodyEl.getId());
				//me.uploader.setDropElement(me.getId());
				me.uploader.setBrowseButton(me.triggers['upload'].domId);
				me.uploader.setContainer(me.pluWrap.getId());
				me.uploader.setDropElement(me.inputWrap.getId());
				me.uploader.init();
			}, {single: true});

			me.relayEvents(me.uploader, [
				'beforestart',
				'uploadready',
				'uploadstarted',
				'uploadcomplete',
				'uploaderror',
				'filesadded',
				'beforeupload',
				'fileuploaded',
				'updateprogress',
				'uploadprogress',
				'storeempty'
			]);
			
		}
	},
	
	initEvents: function(){
        var me = this,
				el = me.inputEl;
		me.callParent();
	},
	
	getSubTplData: function(fieldData) {
		var ret = this.callParent(arguments);
		ret.fieldStyle = this.getFieldStyles() + ret.fieldStyle;
		return ret;
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
	
	/**
	 * Sets the read-only state of this field.
	 * @param {Boolean} readOnly True to prevent the user changing the field, explicitly
	 * hide the trigger(s) and disable upload. See {@link Ext.form.field.Text#readOnly readOnly} for more info.
	 */
	setReadOnly: function(readOnly) {
		var me = this;
		me.callParent([readOnly]);
		
		if(me.rendered && me.uploader) {
			if(readOnly) me.uploader.disable();
			else me.uploader.enable();
		}
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
	},
	
	onClearClick: function(me) {
		me.setValue(null);
	},
	
	onUploadClick: function(me) {
		
	}
});
