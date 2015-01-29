/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.MultiCalendar', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.somulticalendar',
	
	requires: [
		'Sonicle.picker.Date'
	],
	mixins: [
		'Ext.form.field.Field'
	],
	
	cls: 'so-multicalendar',
	//enableBubble: ['change'],
	config: {
		/**
		 * @cfg {String} noOfMonth
		 * Total number of month pickers to display.
		*/
		noOfMonth : 2,
		
		/**
		 * @cfg {String} noOfMonthPerRow
		 * Number of month pickers per row.
		 */
		noOfMonthPerRow : 1,
		
		/**
		 * @cfg {String} startDay
		 * One of: 0 sunday, 1 monday.
		*/
		startDay: 1
	},
	
	/**
	 * @cfg {String} highlightMode
	 * One of: "d" day, "w5" work week, "w" week, "wa" week agenda, "m" month.
	 */
	highlightMode : 'w5',
	
	/**
	 * @cfg {Array} dates
	 */
	dates: null,
	
	constructor: function(cfg) {
		var me = this, tcfg = Ext.apply(me.defaultConfig, cfg), 
				columns = (tcfg.noOfMonthPerRow > tcfg.noOfMonth) ? tcfg.noOfMonth : tcfg.noOfMonthPerRow, 
				rows = (tcfg.noOfMonth / tcfg.noOfMonthPerRow);
		
		// Defines component layout
		if(columns === 1) {
			cfg.layout = 'vbox';
		} else if(rows === 1) {
			cfg.layout = 'hbox';
		} else {
			cfg.layout = 'table';
			cfg.layoutConfig = {
				columns: columns
			};
		}
		
		if(!Ext.isDate(cfg.value)) cfg.value = new Date();
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		// Init mixins
		me.initField(arguments);
		
		me.createPickers();
		me.setValue(me.getValue()); // Force pickers update
	},
	
	createPickers: function() {
		var me = this, months = me.getNoOfMonth(), first, last;
		
		for(var i=0; i<months; i++) {
			first = (i === 0);
			last = (i === months-1);
			me.add(Ext.create({
				xtype: 'sodatepicker',
				border: false,
				showToday: false,
				showMonthpicker: first,
				startDay: me.startDay,
				highlightMode: me.highlightMode,
				boldDates: me.dates,
				highlightBefore: first,
				highlightAfter: last,
				listeners: {
					select: function(s,date) {
						me.activePicker = s;
						me.setValue(date);
					}
				}
			}));
		}
		me.activePicker = me.getComponent(0);
	},
	
	/*
	getState: function() {
		this.addPropertyToState(this.callParent(), 'highlightMode');
		return this.addPropertyToState(this.callParent(), 'value');
	},
	
	applyState: function(state) {
		this.callParent(arguments);
		if(state.hasOwnProperty('highlightMode')) {
			this.setHighlightMode(state.highlightMode);
		}
		if(state.hasOwnProperty('value')) {
			this.setValue(state.value);
		}
	},
	*/
	
	/*
	onChange: function (newVal, oldVal) {
		var me = this;
		me.fireEvent('changemerda', me, newVal, oldVal); // Why?
		me.mixins.field.onChange.call(me, newVal);
	},
	*/
	
	
	
	setValue: function(value) {
		if(!Ext.isDate(value)) return;
		var me = this;
		me.mixins.field.setValue.call(me, value);
		me.updatePickersValue();
		return me;
	},
	
	getHighlightMode: function() {
		return this.highlightMode;
	},
	
	setHighlightMode: function(value) {
		var me = this;
		me.highlightMode = value;
		Ext.suspendLayouts();
		me.items.each(function(cmp) {
			cmp.setHighlightMode(value);
		});
		Ext.resumeLayouts(true);
	},
	
	getDates: function() {
		return this.dates;
	},
	
	setDates: function(value) {
		var me = this;
		me.dates = value;
		Ext.suspendLayouts();
		me.items.each(function(cmp) {
			cmp.setBoldDates(value, true);
		});
		Ext.resumeLayouts(true);
	},
	
	setToday: function() {
		this.setValue(this.moveDate(0));
	},
	
	setPreviousDay: function() {
		this.setValue(this.moveDate(-1));
	},
	
	setNextDay: function() {
		this.setValue(this.moveDate(1));
	},
	
	moveDate: function(direction) {
		if(direction === 0) {
			return new Date();
		} else {
			var dt = this.value, hm = this.highlightMode, eDate = Ext.Date, int, val;
			if(hm === 'd') {
				int = eDate.DAY;
				val = 1;
			} else if((hm === 'w5') || (hm === 'w') || (hm === 'aw')) {
				int = eDate.DAY;
				val = 7;
			} else {
				int = eDate.MONTH;
				val = 1;
			}
			return eDate.add(dt, int, val*direction);
		}
	},
	
	updatePickersValue: function() {
		var me = this, eDate = Ext.Date, len = me.items.getCount();
		if((len === 0) || !me.activePicker) return;
		
		var	off = me.items.indexOf(me.activePicker), date = me.getValue(), 
				start = eDate.add(new Date(date.getFullYear(), date.getMonth(), 1), eDate.MONTH, -off);	
		
		Ext.suspendLayouts();
		me.items.each(function(cmp, i) {
			cmp.setBoldDates(null);
			cmp.setHighlightDate(date);
			if(cmp === me.activePicker) {
				cmp.setValue(date);
			} else {
				cmp.setValue(eDate.add(start, eDate.MONTH, i));
			}
		});
		Ext.resumeLayouts(true);
	}
});
