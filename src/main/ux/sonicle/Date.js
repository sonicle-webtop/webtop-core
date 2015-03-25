Ext.define('Sonicle.Date', {
	singleton: true,
	
	fmtCache: {},
	javaFmtMapping: {
		d: 'j',
		dd: 'd',
		yy: 'y',
		yyyy: 'Y',
		a: 'A',
		//A: 'A',
		M: 'n',
		MM: 'm',
		MMM: 'M',
		MMMM: 'F',
		h: 'g',
		hh: 'h',
		H: 'G',
		HH: 'H',
		m: 'i',
		mm: 'i',
		s: 's',
		ss: 's',
		S: 'u',
		SS: 'u',
		SSS: 'u',
		E: 'D',
		EEE: 'D',
		EEEE: 'D',
		D: 'z',
		w: 'W',
		ww: 'W',
		z: 'T',
		zzzz: 'T',
		Z: 'O',
		X: 'O',
		XX: 'O',
		XXX: 'P',
		u: 'w'
	},
	
	/**
	 * Translates the java date format String to a ExtJs format String.
	 * @param {String} format The format String to be translated.
	 * @returns {String}
	 */
	toExtJsFormat: function(format) {
		var me = this;
		if (!me.fmtCache[format]) {
			me.fmtCache[format] = me.translateFormat(format, me.javaFmtMapping);
		}
		return me.fmtCache[format];
	},
	
	/**
	 * Translates the java date format String to a ExtJs format String.
	 * @param {String} format The unmodified format string.
	 * @param {Object} mapping The date format mapping object.
	 * @returns {String}
	 */
	translateFormat: function(format, mapping) {
		var me = this,
				len = format.length,
				i = 0,
				beginIndex = -1,
				lastCh = null,
				curCh = '',
				result = '';
		
		for(; i < len; i++) {
			curCh = format.charAt(i);
			if(lastCh === null || lastCh !== curCh) { // change detected
				format = me._appendMappedString(format, mapping, beginIndex, i, result);
				beginIndex = i;
			}
			lastCh = curCh;
		}
		return me._appendMappedString(format, mapping, beginIndex, i, result);
	},
	
	/**
	 * @private
	 * Checks if the substring is a mapped date format pattern and adds it to the result format String.
	 * @param {String} format The unmodified format String.
	 * @param {Object} mapping The date format mapping Object.
	 * @param {Number} beginIndex The begin index of the continuous format characters.
	 * @param {Number} currentIndex The last index of the continuous format characters.
	 * @param {String} result The result format String.
	 * @returns {String}
	 */
	_appendMappedString: function(format, mapping, beginIndex, currentIndex, result) {
		var temp;
		if(beginIndex !== -1) {
			temp = format.substring(beginIndex, currentIndex);
			// check if the temporary string has a known mapping
			if (mapping[temp]) {
				temp = mapping[temp];
			}
			result = result.concat(temp);
		}
		return result;
	},
	
	/**
	 * Returns localized day name with minimal string length.
	 * @param {Number} day The day index (0=sunday, 1=monday, etc...)
	 * @returns {String} Localized name.
	 */
	getShortestDayName: function(day) {
		return Ext.Date.getShortDayName(day).substring(0, 1);
	},
	
	/**
	 * Returns the time duration between two dates in the specified units. For finding the number of
	 * calendar days (ignoring time) between two dates use {@link Extensible.Date.diffDays diffDays} instead.
	 * @param {Date} start The start date
	 * @param {Date} end The end date
	 * @param {String} unit (optional) The time unit to return. Valid values are 'millis' (the default),
	 * 'seconds', 'minutes' or 'hours'.
	 * @return {Number} The time difference between the dates in the units specified by the unit param,
	 * rounded to the nearest even unit via Math.round().
	 */
	diff: function (start, end, unit) {
		var denom = 1,
				diff = end.getTime() - start.getTime();

		if (unit === 's' || unit === 'seconds') {
			denom = 1000;
		}
		else if (unit === 'm' || unit === 'minutes') {
			denom = 1000 * 60;
		}
		else if (unit === 'h' || unit === 'hours') {
			denom = 1000 * 60 * 60;
		}
		return Math.round(diff / denom);
	},
	
	/**
	 * Calculates the number of calendar days between two dates, ignoring time values.
	 * A time span that starts at 11pm (23:00) on Monday and ends at 1am (01:00) on Wednesday is
	 * only 26 total hours, but it spans 3 calendar days, so this function would return 2. For the
	 * exact time difference, use {@link Extensible.Date.diff diff} instead.
	 * 
	 * NOTE that the dates passed into this function are expected to be in local time matching the
	 * system timezone. This does not work with timezone-relative or UTC dates as the exact date
	 * boundaries can shift with timezone shifts, affecting the output. If you need precise control
	 * over the difference, use {@link Extensible.Date.diff diff} instead.
	 * 
	 * @param {Date} start The start date
	 * @param {Date} end The end date
	 * @return {Number} The number of calendar days difference between the dates
	 */
	diffDays: function (start, end) {
		// All calculations are in milliseconds
		var day = 1000 * 60 * 60 * 24,
				clear = Ext.Date.clearTime,
				timezoneOffset = (start.getTimezoneOffset() - end.getTimezoneOffset()) * 60 * 1000,
				diff = clear(end, true).getTime() - clear(start, true).getTime() + timezoneOffset,
				days = Math.round(diff / day);

		return days;
	},
	
	/**
	 * Copies the date value from one date object into another without altering the target's
	 * date value. This function returns a new Date instance without modifying either original value.
	 * @param {Date} fromDt The original date from which to copy the date
	 * @param {Date} toDt The target date to copy the date to
	 * @returns {Date} The new date/time value
	 */
	copyDate: function(fromDt, toDt) {
		var dt = Ext.Date.clone(toDt);
		dt.setFullYear(
				fromDt.getFullYear(),
				fromDt.getMonth(),
				fromDt.getDate()
		);
		return dt;
	},
	
	/**
	 * Copies the time value from one date object into another without altering the target's
	 * date value. This function returns a new Date instance without modifying either original value.
	 * @param {Date} fromDt The original date from which to copy the time
	 * @param {Date} toDt The target date to copy the time to
	 * @return {Date} The new date/time value
	 */
	copyTime: function(fromDt, toDt) {
		var dt = Ext.Date.clone(toDt);
		dt.setHours(
				fromDt.getHours(),
				fromDt.getMinutes(),
				fromDt.getSeconds(),
				fromDt.getMilliseconds()
		);
		return dt;
	},
	
	/**
	 * Compares two dates and returns a value indicating how they relate to each other.
	 * @param {Date} dt1 The first date
	 * @param {Date} dt2 The second date
	 * @param {Boolean} precise (optional) If true, the milliseconds component is included in the comparison,
	 * else it is ignored (the default).
	 * @return {Number} The number of milliseconds difference between the two dates. If the dates are equal
	 * this will be 0. If the first date is earlier the return value will be positive, and if the second date
	 * is earlier the value will be negative.
	 */
	compare: function(dt1, dt2, precise) {
		if (precise !== true) {
			dt1 = Ext.Date.clone(dt1);
			dt1.setMilliseconds(0);
			dt2 = Ext.Date.clone(dt2);
			dt2.setMilliseconds(0);
		}
		return dt2.getTime() - dt1.getTime();
	},
	
	/**
	 * Returns the maximum date value passed into the function. Any number of date
	 * objects can be passed as separate params.
	 * @param {Date} dt1 The first date
	 * @param {Date} dt2 The second date
	 * @param {Date} dtN (optional) The Nth date, etc.
	 * @return {Date} A new date instance with the latest date value that was passed to the function
	 */
	max: function() {
		return this.maxOrMin.apply(this, [true, arguments]);
	},
	
	/**
	 * Returns the minimum date value passed into the function. Any number of date
	 * objects can be passed as separate params.
	 * @param {Date} dt1 The first date
	 * @param {Date} dt2 The second date
	 * @param {Date} dtN (optional) The Nth date, etc.
	 * @return {Date} A new date instance with the earliest date value that was passed to the function
	 */
	min: function() {
		return this.maxOrMin.apply(this, [false, arguments]);
	},
	
	// private helper fn
	maxOrMin: function(max) {
		var dt = (max ? 0 : Number.MAX_VALUE),
				i = 0,
				args = arguments[1],
				ln = args.length;
		for (; i < ln; i++) {
			dt = Math[max ? 'max' : 'min'](dt, args[i].getTime());
		}
		return new Date(dt);
	},
	
	isInRange: function(dt, rangeStart, rangeEnd) {
		return  (dt >= rangeStart && dt <= rangeEnd);
	},
	
	/**
	 * Returns true if two date ranges overlap (either one starts or ends within the other, or one completely
	 * overlaps the start and end of the other), else false if they do not.
	 * @param {Date} start1 The start date of range 1
	 * @param {Date} end1   The end date of range 1
	 * @param {Date} start2 The start date of range 2
	 * @param {Date} end2   The end date of range 2
	 * @return {Booelan} True if the ranges overlap, else false
	 */
	rangesOverlap: function(start1, end1, start2, end2) {
		var startsInRange = (start1 >= start2 && start1 <= end2),
				endsInRange = (end1 >= start2 && end1 <= end2),
				spansRange = (start1 <= start2 && end1 >= end2);

		return (startsInRange || endsInRange || spansRange);
	},
	
	/**
	 * Returns true if the specified date is a Saturday or Sunday, else false.
	 * @param {Date} dt The date to test
	 * @return {Boolean} True if the date is a weekend day, else false
	 */
	isWeekend: function(dt) {
		return dt.getDay() % 6 === 0;
	},
	
	/**
	 * Returns true if the specified date falls on a Monday through Friday, else false.
	 * @param {Date} dt The date to test
	 * @return {Boolean} True if the date is a week day, else false
	 */
	isWeekday: function(dt) {
		return dt.getDay() % 6 !== 0;
	},
	
	/**
	 * Returns true if the specified date's time component equals 00:00, ignoring
	 * seconds and milliseconds.
	 * @param {Object} dt The date to test
	 * @return {Boolean} True if the time is midnight, else false
	 */
	isMidnight: function(dt) {
		return dt.getHours() === 0 && dt.getMinutes() === 0;
	},
	
	/**
	 * Returns true if the specified date is the current browser-local date, else false.
	 * @param {Object} dt The date to test
	 * @return {Boolean} True if the date is today, else false
	 */
	isToday: function(dt) {
		return this.diffDays(dt, this.today()) === 0;
	},
	
	/**
	 * Convenience method to get the current browser-local date with no time value.
	 * @return {Date} The current date, with time 00:00
	 */
	today: function() {
		return Ext.Date.clearTime(new Date());
	},
	
	/**
	 * Adds time to the specified date and returns a new Date instance as the result (does not
	 * alter the original date object). Time can be specified in any combination of milliseconds
	 * to years, and the functionautomatically takes leap years and daylight savings into account.
	 * Some syntax examples:<code><pre>
	 var now = new Date();
	 
	 // Add 24 hours to the current date/time:
	 var tomorrow = Extensible.Date.add(now, { days: 1 });
	 
	 // More complex, returning a date only with no time value:
	 var futureDate = Extensible.Date.add(now, {
	 weeks: 1,
	 days: 5,
	 minutes: 30,
	 clearTime: true
	 });
	 </pre></code>
	 * @param {Date} dt The starting date to which to add time
	 * @param {Object} o A config object that can contain one or more of the following
	 * properties, each with an integer value:
	 * 
	 * - millis
	 * - seconds
	 * - minutes
	 * - hours
	 * - days
	 * - weeks
	 * - months
	 * - years
	 * 
	 * You can also optionally include the property "clearTime: true" which will perform all of the
	 * date addition first, then clear the time value of the final date before returning it.
	 * @return {Date} A new date instance containing the resulting date/time value
	 */
	add: function(dt, o) {
		if(!o) return dt;
		var eDate = Ext.Date,
				dateAdd = eDate.add,
				newDt = eDate.clone(dt);

		if (o.years) {
			newDt = dateAdd(newDt, eDate.YEAR, o.years);
		}
		if (o.months) {
			newDt = dateAdd(newDt, eDate.MONTH, o.months);
		}
		if (o.weeks) {
			o.days = (o.days || 0) + (o.weeks * 7);
		}
		if (o.days) {
			newDt = dateAdd(newDt, eDate.DAY, o.days);
		}
		if (o.hours) {
			newDt = dateAdd(newDt, eDate.HOUR, o.hours);
		}
		if (o.minutes) {
			newDt = dateAdd(newDt, eDate.MINUTE, o.minutes);
		}
		if (o.seconds) {
			newDt = dateAdd(newDt, eDate.SECOND, o.seconds);
		}
		if (o.millis) {
			newDt = dateAdd(newDt, eDate.MILLI, o.millis);
		}
		return o.clearTime ? eDate.clearTime(newDt) : newDt;
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
