/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.upload.Field', {
	extend: 'Ext.form.field.Text',
	alias: 'widget.souploadfield',
	requires: [
		'Sonicle.upload.Button',
		'Ext.form.trigger.Component'
	],
	
	needArrowKeys: false,
	
	triggers: {
		uploadbutton: {
			type: 'component',
			hideOnReadOnly: false
		}
	},
	
	serverResponsePropertyAsValue: '',
	
	/**
	 * @cfg {String} buttonText
	 * The button text to display on the upload button. Note that if you supply a value for
	 * {@link #buttonConfig}, the buttonConfig.text value will be used instead if available.
	 */
	buttonText: 'Browse...',
	
	/**
	 * @cfg {Boolean} buttonOnly
	 * True to display the file upload field as a button with no visible text field. If true, all
	 * inherited Text members will still be available.
	 */
	buttonOnly: false,
	
	/**
	 * @cfg {Number} buttonMargin
	 * The number of pixels of space reserved between the button and the text field. Note that this only
	 * applies if {@link #buttonOnly} = false.
	 */
	buttonMargin: 3,
	
	/**
	 * @cfg {Object} buttonConfig
	 * Specify optional custom button {@link Ext.button.Button} config (eg. iconCls, text) for the upload button
	 */
	
	extraFieldBodyCls: Ext.baseCSSPrefix + 'form-file-wrap',
	
	inputCls: Ext.baseCSSPrefix + 'form-text-file',
	
	/**
	 * @cfg {Boolean} [readOnly=true]
	 * Unlike with other form fields, the readOnly config defaults to true in File field.
	 */
	readOnly: true,
	
	/**
	 * @cfg {Boolean} editable
	 * @inheritdoc
	 */
	editable: false,
	
	submitValue: false,
	
	/**
	 * @private
	 * Do not show hand pointer over text field since file choose dialog is only shown when clicking in the button
	 */
	triggerNoEditCls: '',
	
	/**
	 * @private
	 * Extract the file element, button outer element, and button active element.
	 */
	childEls: ['browseButtonWrap'],
	
	/**
	 * @private
	 */
	applyTriggers: function(triggers) {
		var me = this,
				triggerCfg = (triggers || {}).uploadbutton,
				btnCfg = me.buttonConfig || {};

		if(triggerCfg) {
			btnCfg.uploaderConfig = Ext.apply(btnCfg.uploaderConfig || {}, {
				multiSelection: false
			});
			triggerCfg.component = Ext.apply({
				xtype: 'souploadbutton',
				ownerCt: me,
				id: me.id + '-button',
				ui: me.ui,
				disabled: me.disabled,
				text: me.buttonText,
				style: me.buttonOnly ? '' : me.getButtonMarginProp() + me.buttonMargin + 'px',
				inputName: me.getName()
			}, btnCfg);

			return me.callParent([triggers]);
		}
	},
	
	/**
	 * @private
	 */
	onRender: function() {
		var me = this,
				inputEl, button, buttonEl, trigger;

		me.callParent(arguments);
		
		inputEl = me.inputEl;
		//name goes on the fileInput, not the text input
		inputEl.dom.name = '';

		// Some browsers will show a blinking cursor in the field, even if it's readonly. If we do happen
		// to receive focus, forward it on to our focusEl. Also note that in IE, the file input is treated as
		// 2 elements for tabbing purposes (the text, then the button). So as you tab through, it will take 2
		// tabs to get to the next field. As far as I know there's no way around this in any kind of reasonable way.
		inputEl.on('focus', me.onInputFocus, me);
		inputEl.on('mousedown', me.onInputMouseDown, me);

		trigger = me.getTrigger('uploadbutton');
		button = me.button = trigger.component;
		button.on('fileuploaded', me.onFileUploaded, me);
		buttonEl = button.el;

		if (me.buttonOnly) {
			me.inputWrap.setDisplayed(false);
			me.shrinkWrap = 3;
		}

		// Ensure the trigger element is sized correctly upon render
		trigger.el.setWidth(buttonEl.getWidth() + buttonEl.getMargin('lr'));
		if (Ext.isIE) {
			me.button.getEl().repaint();
		}
	},
	
	/**
	 * Gets the markup to be inserted into the subTplMarkup.
	 */
	getTriggerMarkup: function() {
		return '<td id="' + this.id + '-browseButtonWrap" data-ref="browseButtonWrap" role="presentation"></td>';
	},
	
	onFileUploaded: function(s, file) {
		this.doSetValue(file);
	},
	
	/**
	 * Overridden to skip rawValue processing in {@link Ext.form.field.Base#getValue}
	 */
	getValue: function() {
		return this.value;
	},
	
	/**
	 * Overridden to do nothing
	 */
	setValue: Ext.emptyFn,
	
	/**
	 * @private
	 */
	doSetValue: function(file) {
		var me = this,
				srpas = me.serverResponsePropertyAsValue,
				val;
		me.setRawValue(file.name);
		if(file.server_response && !Ext.isEmpty(srpas)) {
			val = file.server_response[srpas];
		} else {
			val = file.name;
		}
		me.mixins.field.setValue.call(me, val);
		me.applyEmptyText();
		return me;
	},
	
	reset: function() {
		var me = this,
				clear = me.clearOnSubmit;
		if (me.rendered) {
			me.button.reset(clear);
			if (clear) {
				me.inputEl.dom.value = '';
				// Reset the underlying value if we're clearing it
				Ext.form.field.File.superclass.setValue.call(this, null);
			}
		}
		me.callParent();
	},
	
	onShow: function() {
		this.callParent();
		// If we started out hidden, the button may have a messed up layout
		// since we don't act like a container
		this.button.updateLayout();
	},
	
	onDisable: function () {
		this.callParent();
		this.button.disable();
	},
	
	onEnable: function () {
		this.callParent();
		this.button.enable();
	},
	
	onDestroy: function() {
		this.button = null;
		this.callParent();
	},
	
	getButtonMarginProp: function() {
		return 'margin-left:';
	},
	
	onInputFocus: function(e) {
		this.focus();
	},
	
	onInputMouseDown: function(e) {
		// Some browsers will show the cursor even if the input is read only,
		// which will be visible in the short instant between inputEl focusing
		// and subsequent focus jump to the FileButton. Preventing inputEl from
		// focusing eliminates that flicker.
		e.preventDefault();
		this.focus();
	},
	
	privates: {
		getFocusEl: function() {
			return this.button;
		},
		
		getFocusClsEl: Ext.privateFn
	}
});
