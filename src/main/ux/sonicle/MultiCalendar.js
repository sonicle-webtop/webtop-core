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
		'Sonicle.calendar.DatePicker'
	],
	mixins: [
		'Ext.form.field.Field',
		'Ext.util.StoreHolder'
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
		startDay: 1,
		
		boldDateField: 'date',
		
		/**
		 * @cfg {String} dateParamStart
		 * The param name representing the start date of the current view range that's passed in requests to retrieve events
		 * when loading the view (defaults to 'startDate').
		 */
		dateParamStart: 'startDate',
		
		/**
		 * @cfg {String} dateParamEnd
		 * The param name representing the end date of the current view range that's passed in requests to retrieve events
		 * when loading the view (defaults to 'endDate').
		 */
		dateParamEnd: 'endDate',
		
		/**
		 * @cfg {String} dateParamFormat
		 * The format to use for date parameters sent with requests to retrieve events for the calendar (defaults to 'Y-m-d', e.g. '2010-10-31')
		 */
		dateParamFormat: 'Y-m-d'
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
		me.bindStore(me.store || 'ext-empty-store', true, true);
		me.callParent(arguments);
		
		// Init mixins
		me.initField(arguments);
		
		me.createPickers();
		me.setValue(me.getValue()); // Force pickers update
	},
	
	/**
	 * Binds a store to this instance.
	 * @param {Ext.data.AbstractStore/String} [store] The store to bind or ID of the store.
	 * When no store given (or when `null` or `undefined` passed), unbinds the existing store.
	 */
	bindStore: function(store, /* private */ initial) {
		var me = this;
		me.mixins.storeholder.bindStore.call(me, store, initial);
		store = me.getStore();
	},
	
	/**
	 * private
	 */
	_loadBoldDates: function() {
		var me = this;
		if(me.store && !me.store.loaded) me.store.load();
	},
	
	/**
	 * See {@link Ext.util.StoreHolder StoreHolder}.
	 */
	onBindStore: function(store, initial) {
		// We're being bound, not unbound...
		if(store) {
			if(store.autoCreated) this.boldDateField = 'field1';
		}
	},
	
	/**
	 * See {@link Ext.util.StoreHolder StoreHolder}.
	 */
	getStoreListeners: function(store, o) {
		var me = this;
		return {
			datachanged: me.onStoreDataChanged,
			beforeload: me.onStoreBeforeLoad,
			load: me.onStoreLoad
		};
	},
	
	onStoreDataChanged: function() {
		this.updateBoldDates();
	},
	
	onStoreBeforeLoad: function(store, op, o) {
		op.setParams(Ext.apply(op.getParams() || {}, this.getStoreParams()));
	},
	
	onStoreLoad: function(store, records, success) {
		if(success) this.updateBoldDates();
	},
	
	getStoreParams: function() {
		var me = this, o = {};
		o[me.dateParamStart] = Ext.Date.format(this.viewStart, this.dateParamFormat);
		o[me.dateParamEnd] = Ext.Date.format(this.viewEnd, this.dateParamFormat);
		return o;
	},
	
	createPickers: function() {
		var me = this, months = me.getNoOfMonth(), first, last;
		
		for(var i=0; i<months; i++) {
			first = (i === 0);
			last = (i === months-1);
			me.add(Ext.create({
				xtype: 'socalendarpicker',
				border: false,
				showToday: false,
				showMonthpicker: first,
				startDay: me.startDay,
				highlightMode: me.highlightMode,
				boldDates: me.dates,
				ddTargetCls: 'so-multical-dd-target',
				highlightCls: 'so-multical-highlight',
				highlightPrevDays: first,
				highlightNextDays: last,
				hidePrevDays: !first,
				hideNextDays: !last,
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
	
	updateBoldDates: function() {
		var me = this;
		if(me.store) {
			var dates = [];
			me.store.each(function(rec) {
				dates.push(rec.get(me.boldDateField));
			});
			Ext.suspendLayouts();
			me.items.each(function(cmp) {
				cmp.setBoldDates(dates, true);
			});
			Ext.resumeLayouts(true);
		}
	},
	
	setValue: function(value) {
		if(!Ext.isDate(value)) return;
		var me = this;
		me.mixins.field.setValue.call(me, value);
		me._updatePickersValue();
		return me;
	},
	
	_updatePickersValue: function() {
		var me = this, eDate = Ext.Date, len = me.items.getCount();
		if((len === 0) || !me.activePicker) return;
		
		var	off = me.items.indexOf(me.activePicker),
				date = me.getValue(),
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
		me._updateViewBounds(me.getComponent(0).getValue(), me.getComponent(len-1).getValue());
	},
	
	_updateViewBounds: function(firstDate, lastDate) {
		var me = this,
				soDate = Sonicle.Date,
				newStart = me._calcStartingDate(firstDate),
				newEnd = soDate.add(me._calcStartingDate(lastDate), {days: +41}),
				diffStart = (Ext.isDate(me.viewStart)) ? (soDate.diffDays(me.viewStart, newStart) !== 0) : true,
				diffEnd = (Ext.isDate(me.viewEnd)) ? (soDate.diffDays(me.viewEnd, newEnd) !== 0) : true;
		
		if(diffStart || diffEnd) {
			me.viewStart = newStart;
			me.viewEnd = newEnd;
			me._loadBoldDates();
		}
	},
	
	_calcStartingDate: function(date) {
		var eDate = Ext.Date,
				soDate = Sonicle.Date,
				firstOfMonth = eDate.getFirstDateOfMonth(date),
				startingPos = firstOfMonth.getDay() - this.startDay;
		if(startingPos < 0) startingPos += 7;
		return soDate.add(firstOfMonth, {days: -startingPos});
	},
	
	getViewStart: function() {
		return this.viewStart;
	},
	
	getViewEnd: function() {
		return this.viewEnd;
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
	
	setToday: function() {
		this.setValue(this._moveDate(0));
	},
	
	setPreviousDay: function() {
		this.setValue(this._moveDate(-1));
	},
	
	setNextDay: function() {
		this.setValue(this._moveDate(1));
	},
	
	/**
	 * private
	 */
	_moveDate: function(direction) {
		if(direction === 0) {
			return new Date();
		} else {
			var dt = this.value, hm = this.highlightMode, eDate = Ext.Date, int, val;
			if(hm === 'd') {
				int = eDate.DAY;
				val = 1;
			} else if((hm === 'w5') || (hm === 'w') || (hm === 'wa')) {
				int = eDate.DAY;
				val = 7;
			} else {
				int = eDate.MONTH;
				val = 1;
			}
			return eDate.add(dt, int, val*direction);
		}
	}
});
