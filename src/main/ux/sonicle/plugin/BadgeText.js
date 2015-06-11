/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.plugin.BadgeText', {
	extend: 'Ext.plugin.Abstract',
	alias: 'plugin.sobadgetext',
	textColor: 'white',
	disableOpacity: 0,
	align: 'cr',
	text: '',
	disable: false,
	
	/**
	 * @readonly
	 * @property {Ext.button.Button} button
	 */
	button: null,
	
	init: function(button) {
		var me = this;
		me.button = button;
		Ext.apply(button, {
			setBadgeText: function(text) {
				if (typeof text === "string") me.text = text;
				if (Ext.isEmpty(me.text)) {
					if (Ext.isDefined(me.button.badgeEl)) {
						// Hide badge
						me.button.badgeEl.update(me.text);
						me.button.badgeEl.setOpacity(0);
					}
				} else {
					if (!Ext.isDefined(me.button.badgeEl)) {
						// Add badge
						me.addBadgeEl(me.button);
					}
					me.button.badgeEl.update(me.text);
					me.button.badgeEl.setOpacity(1);
				}
			},
			
			getBadgeText: function() {
				return me.text;
			}
		});
		button.on('render', me.button.setBadgeText, me);
	},
	
	addBadgeEl: function (button) {
		var me = this,
				styles = {
					'position': 'absolute',
					'background-color': "red",
					'opacity': (Ext.isEmpty(me.text) ? 0 : 1),
					'color': me.textColor,
					'index': 50,
					'border-radius': '3px',
					'font-weight': 'bold',
					'text-shadow': 'rgba(0, 0, 0, 0.5) 0 -0.08em 0',
					'box-shadow': 'rgba(0, 0, 0, 0.3) 0 0.1em 0.1em',
					'cursor': 'pointer'
				};
						
		/*		
				styles = {
					'position': 'absolute',
					'background-color': "red",
					'opacity': (Ext.isEmpty(me.text) ? 0 : 1),
					'font-size': me.textSize + 'px',
					'color': me.textColor,
					'padding': '1px 2px',
					'index': 50,
					'top': '-8px',
					'border-radius': '3px',
					'font-weight': 'bold',
					'text-shadow': 'rgba(0, 0, 0, 0.5) 0 -0.08em 0',
					'box-shadow': 'rgba(0, 0, 0, 0.3) 0 0.1em 0.1em',
					'cursor': 'pointer'
				};
				
		if (me.align === 'left') {
			styles.left = '2px';
		} else {
			styles.right = '2px';
		}
		*/
		
		if(button.scale === 'small') {
			Ext.apply(styles, {
				'height': '12px',
				'line-height': '14px',
				'font-size': '8px',
				'padding': '0px 2px'
			});
			Ext.apply(styles, me.calculateAnchors(me.align, button.getHeight(), 12));
		} else if(button.scale === 'medium') {
			Ext.apply(styles, {
				'height': '14px',
				'line-height': '16px',
				'font-size': '10px',
				'padding': '0px 2px'
			});
			Ext.apply(styles, me.calculateAnchors(me.align, button.getHeight(), 14));
		} else {
			Ext.apply(styles, {
				'height': '18px',
				'font-size': '10px',
				'padding': '1px 2px'
			});
			Ext.apply(styles, me.calculateAnchors(me.align, button.getHeight(), 18));
		}
		
		button.setStyle({
			"overflow": "visible"
		});
		
		button.badgeEl = Ext.DomHelper.append(button.el, {
			tag: 'div',
			cls: 'badgeText x-unselectable'
		}, true);
		
		button.badgeEl.setStyle(styles);
	},
	
	calculateAnchors: function(align, buttonHeight, badgeHeight) {
		switch (align) {
			case 'tl':
				return {'top': '0px', 'left': '1px'};
			case 'tr':
				return {'top': '0px', 'right': '1px'};
			case 'cl':
				return {'top': (Math.round((buttonHeight-badgeHeight)/2)-1)+'px', 'left': '1px'};
			case 'cr':
				return {'top': (Math.round((buttonHeight-badgeHeight)/2)-1)+'px', 'right': '1px'};
			case 'bl':
				return {'bottom': '0px', 'left': '1px'};
			case 'br':
				return {'bottom': '0px', 'right': '1px'};
		}
	},
	
	onBadgeClick: function () {
		var me = this;
		me.button.fireEvent('badgeclick', me.button, me.text);
	},
	
	setDisabled: function (disable) {
		var me = this;

		me.button.badgeEl.setStyle({
			'opacity': (disable ? 0 : 1)
		});

		me.button.badgeEl.clearListeners();
		if(!disable) {
			me.button.badgeEl.on('click', me.onBadgeClick, me, {
				preventDefault: true,
				stopEvent: true
			});
		}
	}
});