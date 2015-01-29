/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.picker.Date', {
	alternateClassName: 'Sonicle.DatePicker',
	extend: 'Ext.picker.Date',
	alias: 'widget.sodatepicker',
	
	config: {
		/**
		 * @cfg {Boolean} showMonthpicker
		 * False to prevent the month picker being displayed.
		 */
		
		showMonthpicker: true,
		/**
		 * @cfg {Boolean} highlightBefore
		 * True to highlight displayed dates belonging to the previous month.
		 */
		
		highlightBefore: false,
		/**
		 * @cfg {Boolean} highlightAfter
		 * True to highlight displayed dates belonging to the next month.
		 */
		highlightAfter: false
	},
	
	/**
	 * @cfg {String} highlightMode
	 * One of: "d" day, "w5" work week, "w" week, "wa" week agenda, "m" month.
	 */
	highlightMode : 'd',
	
	/**
	 * @cfg {RegExp} [boldDatesRE=null]
	 * JavaScript regular expression used to bold a pattern of dates. The {@link #boldDates}
	 * config will generate this regex internally, but if you specify boldDatesRE it will take precedence over the
	 * boldDates value.
	 */
	boldDatesRE: null,
	
	/**
	 * @cfg {String[]} boldDates
	 * An array of 'dates' to bold, as strings. These strings will be used to build a dynamic regular expression so
	 * they are very powerful. Some examples:
	 *
	 *   - ['03/08/2003', '09/16/2003'] would bold those exact dates
	 *   - ['03/08', '09/16'] would bold those days for every year
	 *   - ['^03/08'] would only match the beginning (useful if you are using short years)
	 *   - ['03/../2006'] would bold every day in March 2006
	 *   - ['^03'] would bold every day in every March
	 *
	 * Note that the format of the dates included in the array should exactly match the {@link #format} config. In order
	 * to support regular expressions, if you are using a date format that has '.' in it, you will have to escape the
	 * dot when restricting dates. For example: ['03\\.08\\.03'].
	 */
	boldDates: null,
	
	/**
	 * @property {Date} highlightDate
	 */
	highlightDate: null,
	
	constructor: function(cfg) {
		var me = this;
		if(Ext.isDate(cfg.highlightDate)) me.setHighlightDate(cfg.highlightDate);
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.initBoldDays();
	},
	
	onRender: function(container, position) {
		var me = this;
		me.callParent(arguments);
		// Overrides default behaviour in order to apply monthpicker configuration
		/*
		if(!me.showMonthpicker) {
			me.monthBtn.setArrowVisible(false);
			me.monthBtn.updateArrowVisible();
		}
		*/
	},
	
	doShowMonthPicker: function() {
		// Overrides default behaviour in order to prevent monthpicker display
		if(this.showMonthpicker) this.callParent(arguments);
	},
	
	onOkClick: function(picker, value) {
		var me = this;
		me.callParent(arguments);
		// Overrides default behaviour in order to fire select event
		// after choosing new month 
		me.fireEvent('select', me, me.value);
	},
	
	/**
	 * Setup the bold dates regex based on config options
	 * @private
	 */
	initBoldDays: function() {
		var me = this,
				bd = me.boldDates,
				re = '(?:',
				len,
				b, bLen, bI;

		if (!me.boldDatesRE && bd) {
			len = bd.length - 1;
			bLen = bd.length;

			for (b = 0; b < bLen; b++) {
				bI = bd[b];
				re += Ext.isDate(bI) ? '^' + Ext.String.escapeRegex(Ext.Date.dateFormat(bI, me.format)) + '$' : bI;
				if (b !== len) re += '|';
			}
			me.boldDatesRE = new RegExp(re + ')');
		}
	},
	
	getHighlightMode: function() {
		return this.highlightMode;
	},
	
	setHighlightMode: function(value) {
		var me = this;
		me.highlightMode = value;
		me.update(me.activeDate);
	},
	
	getHighlightDate: function() {
		return this.highlightDate;
	},
	
	setHighlightDate: function(date, update) {
		var me = this;
		if(Ext.isDate(date)) me.highlightDate = Ext.Date.clearTime(date, true);
		if(update) me.update(me.value);
	},
	
	setBoldDates: function(bd, update) {
		var me = this;
		if (Ext.isArray(bd)) {
			me.boldDates = bd;
			me.boldDatesRE = null;
		} else {
			me.boldDatesRE = bd;
		}
		me.initBoldDays();
		if(update) me.update(me.value);
		return me;
	},
	
	selectedUpdate: function(date) {
		var me = this;
		me.callParent(arguments);
		// Overrides default behaviour in order update picker styles
		me.updateStyles();
	},
	
	fullUpdate: function(date) {
		var me = this;
		me.callParent(arguments);
		// Overrides default behaviour in order update picker styles
		me.updateStyles();
	},
	
	updateStyles: function() {
		var me = this, eDate = Ext.Date, hdate = me.highlightDate, selCls = me.selectedCls, 
				cells = me.cells, len = cells.getCount(), cell, bdMatch = me.boldDatesRE, 
				hmode = me.highlightMode, sday = me.startDay, 
				t1 = eDate.getFirstDateOfMonth(me.getValue()).getTime(), 
				t31 = eDate.getLastDateOfMonth(me.getValue()).getTime(), 
				dv, d, tfrom, tto, formatValue;
		
		// Defines highlighting bounds
		if(hmode === 'd') {
			tfrom = hdate.getTime();
			tto = hdate.getTime();
		} else if ((hmode === 'w') || (hmode === 'aw')) {
			tfrom = me.getFirstDateOfWeek(hdate, sday).getTime();
			tto = me.getLastDateOfWeek(hdate, sday).getTime();
		} else if (hmode === 'w5')  {
			var foffs = [1,0], loffs = [-1,-2];
			tfrom = eDate.add(me.getFirstDateOfWeek(hdate, sday), eDate.DAY, foffs[sday]).getTime();
			tto = eDate.add(me.getLastDateOfWeek(hdate, sday), eDate.DAY, loffs[sday]).getTime();
		} else if (hmode === 'm') {
			tfrom = eDate.getFirstDateOfMonth(hdate).getTime();
			tto = eDate.getLastDateOfMonth(hdate).getTime();
		}
		if((tfrom < t1) && !me.highlightBefore) tfrom = t1;
		if((tto > t31) && !me.highlightAfter) tto = t31;
		
		// Loop through cells
		for(var c = 0; c < len; c++) {
			cell = cells.item(c);
			dv = me.textNodes[c].dateValue;
			d = new Date(dv);
			
			// Highlight it...
			if((dv >= tfrom) && (dv <= tto)) {
				cell.first().setStyle('background-color', '#eaf3fa');
			} else {
				cell.first().setStyle('background-color', '');
			}
			
			// Removes selection on the first day if it differs from highlight date
			if((d.getDate() === 1) && (!eDate.isEqual(d, hdate))) {
				if(cell.hasCls(selCls)) cell.removeCls(selCls);
			}
			
			// Mark bold dates
			if(bdMatch && me.format) {
				formatValue = eDate.dateFormat(d, me.format);
				if(bdMatch.test(formatValue)) {
					cell.first().setStyle('font-weight', '800');
				} else {
					cell.first().setStyle('font-weight', '');
				}
			}
		}
	},
	
	showPrevMonth: function(e) {
		var me = this;
		me.callParent(arguments);
		me.fireEvent('select', me, me.value);
	},
	
	showNextMonth: function(e) {
		var me = this;
		me.callParent(arguments);
		me.fireEvent('select', me, me.value);
	},
	
	showPrevYear: function() {
		var me = this;
		me.callParent(arguments);
		me.fireEvent('select', me, me.value);
	},
	
	showNextYear: function() {
		var me = this;
		me.callParent(arguments);
		me.fireEvent('select', me, me.value);
	},
	
	getFirstDateOfWeek: function(date, startDay) {
		var eDate = Ext.Date, newDate = eDate.clearTime(date, true), day = newDate.getDay(), sub;
		if (day !== startDay) {
			if (day === 0) {
				sub = 6;
			} else {
				sub = day - startDay;
			}
			return eDate.add(newDate, eDate.DAY, -sub);
		} else {
			return newDate;
		}
	},
	
	getLastDateOfWeek: function(date, startDay) {
		var eDate = Ext.Date, start = this.getFirstDateOfWeek(date, startDay);
		return eDate.add(start, eDate.DAY, 6);
	}
});
