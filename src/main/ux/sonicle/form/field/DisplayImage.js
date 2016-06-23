/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
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
		if(me.inputWrap) me.inputWrap.applyStyles({padding: '5px'});
		if(me.inputEl && me.value) me.loadImage(me.inputEl, me.value);
	},
	
	setValue: function(value) {
		var me = this;
		if(me.inputEl) me.loadImage(me.inputEl, value);
		me.callParent(arguments);
		return me;
	},
	
	loadImage: function(el, value) {
		var me = this, url;
		
		el.applyStyles({
			width: me.getImageWidth() + 'px',
			height: me.getImageHeight() + 'px'
		});
		url = Ext.isEmpty(value) ? me.getBlankImageUrl() : me.buildBackgroundUrl(value);
		me.displayLoading(true);
		Ext.Ajax.request({
			method: 'GET',
			url: url,
			success: function() {
				me.displayLoading(false);
				el.setStyle('background-image', 'url(' + url + ')');
			},
			failure: function() {
				me.displayLoading(false);
			}
		});
	},
	
	/*
	 * @private
	 */
	displayLoading: function(visible) {
		var me = this, obj = false;
		if(visible) obj = {msg: '', msgWrapCls: ''};
		me.setLoading(obj);
	},
	
	/*
	 * @private
	 */
	buildBackgroundUrl: function(value) {
		var params = {};
		params[this.getUrlParam()] = value;
		return Ext.String.urlAppend(this.getImageUrl(), Ext.Object.toQueryString(params));
	}
});	
