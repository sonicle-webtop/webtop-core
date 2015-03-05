/**S
 * @class Sonicle.calendar.view.DayBody
 * @extends Sonicle.calendar.view.AbstractCalendar
 * <p>This is the scrolling container within the day and week views where non-all-day events are displayed.
 * Normally you should not need to use this class directly -- instead you should use {@link Sonicle.calendar.DayView DayView}
 * which aggregates this class and the {@link Sonicle.calendar.DayHeaderView DayHeaderView} into the single unified view
 * presented by {@link Sonicle.calendar.CalendarPanel CalendarPanel}.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.DayBody', {
	extend: 'Sonicle.calendar.view.AbstractCalendar',
	alias: 'widget.daybodyview',
	requires: [
		'Ext.XTemplate',
		'Sonicle.calendar.template.DayBody',
		'Sonicle.calendar.data.EventMappings',
		'Sonicle.calendar.dd.DayDragZone',
		'Sonicle.calendar.dd.DayDropZone'
	],
	
	//private
	dayColumnElIdDelimiter: '-day-col-',
	hourIncrement: 60,
	
	/**
	 * @event beforeeventresize
	 * Fires after the user drags the resize handle of an event to resize it, but before the resize
	 * operation is carried out. This is a cancelable event, so returning false from a handler will
	 * cancel the resize operation.
	 * @param {Sonicle.calendar.view.DayBody} this
	 * @param {Sonicle.calendar.data.EventModel} rec The original {@link
	 * Extensible.calendar.data.EventModel record} for the event that was resized
	 * @param {Object} data An object containing the new start and end dates that will be set into the
	 * event record if the event is not canceled. Format of the object is: {StartDate: [date], EndDate: [date]}
	 */
	
	/**
	 * @event eventresize
	 * Fires after the user has drag-dropped the resize handle of an event and the resize operation is
	 * complete. If you need to cancel the resize operation you should handle the {@link #beforeeventresize}
	 * event and return false from your handler function.
	 * @param {Sonicle.calendar.view.DayBody} this
	 * @param {Sonicle.calendar.EventModel} rec The {@link Sonicle.calendar.EventModel record} for the event that was resized
	 * containing the updated start and end dates
	 */

	/**
	 * @event dayclick
	 * Fires after the user clicks within the day view container and not on an event element
	 * @param {Sonicle.calendar.view.DayBody} this
	 * @param {Date} dt The date/time that was clicked on
	 * @param {Boolean} allday True if the day clicked on represents an all-day box, else false. Clicks within the 
	 * DayBodyView always return false for this param.
	 * @param {Ext.core.Element} el The Element that was clicked on
	 */

	initComponent: function() {
		var me = this;
		me.callParent(arguments);

		if (me.readOnly === true)
			me.enableEventResize = false;
		me.incrementsPerHour = me.hourIncrement / me.ddIncrement;
		me.minEventHeight = me.minEventDisplayMinutes / (me.hourIncrement / me.hourHeight);
	},
	
	//private
	initDD: function() {
		var me = this, cfg = {
			use24HourTime: me.use24HourTime,
			createText: me.ddCreateEventText,
			copyText: me.ddCopyEventText,
			moveText: me.ddMoveEventText,
			resizeText: me.ddResizeEventText,
			dateFormat: me.ddDateFormat,
			ddIncrement: me.ddIncrement
		};

		this.el.ddScrollConfig = {
			// scrolling is buggy in IE/Opera for some reason. A larger vthresh
			// makes it at least functional if not perfect
			vthresh: Ext.isIE || Ext.isOpera ? 100 : 40,
			hthresh: -1,
			frequency: 50,
			increment: 100,
			ddGroup: 'DayViewDD'
		};
		me.dragZone = Ext.create('Sonicle.calendar.dd.DayDragZone', me.el, Ext.apply({
			view: me,
			containerScroll: true
		},
		cfg));

		me.dropZone = Ext.create('Sonicle.calendar.dd.DayDropZone', me.el, Ext.apply({
			view: me
		},
		cfg));
	},
	
	//private
	refresh: function() {
		var top = this.el.getScroll().top;
		this.prepareData();
		this.renderTemplate();
		this.renderItems();

		// skip this if the initial render scroll position has not yet been set.
		// necessary since IE/Opera must be deferred, so the first refresh will
		// override the initial position by default and always set it to 0.
		if (this.scrollReady) {
			this.scrollTo(top);
		}
	},
	
	/**
	 * Scrolls the container to the specified vertical position. If the view is large enough that
	 * there is no scroll overflow then this method will have no effect.
	 * @param {Number} y The new vertical scroll position in pixels 
	 * @param {Boolean} defer (optional) <p>True to slightly defer the call, false to execute immediately.</p> 
	 * <p>This method will automatically defer itself for IE and Opera (even if you pass false) otherwise
	 * the scroll position will not update in those browsers. You can optionally pass true, however, to
	 * force the defer in all browsers, or use your own custom conditions to determine whether this is needed.</p>
	 * <p>Note that this method should not generally need to be called directly as scroll position is managed internally.</p>
	 */
	scrollTo: function(y, defer) {
		defer = defer || (Ext.isIE || Ext.isOpera);
		if (defer) {
			Ext.defer(function() {
				this.el.scrollTo('top', y, true);
				this.scrollReady = true;
			}, 10, this);
		} else {
			this.el.scrollTo('top', y, true);
			this.scrollReady = true;
		}
	},
	
	// private
	afterRender: function() {
		var me = this;
		if (!me.tpl) {
			me.tpl = Ext.create('Sonicle.calendar.template.DayBody', {
				id: me.id,
				use24HourTime: me.use24HourTime,
				dayCount: me.dayCount,
				showTodayText: me.showTodayText,
				todayText: me.todayText,
				showTime: me.showTime,
				showHourSeparator: me.showHourSeparator,
				viewStartHour: me.viewStartHour,
				viewEndHour: me.viewEndHour,
				hourIncrement: me.hourIncrement,
				hourHeight: me.hourHeight,
				highlightBusinessHours: me.highlightBusinessHours,
				businessHoursStart: me.businessHoursStart,
				businessHoursEnd: me.businessHoursEnd
			});
		}
		me.tpl.compile();
		me.addCls('ext-cal-body-ct');

		me.callParent(arguments);
		
		// default scroll position to scrollStartHour (7am by default) or min view hour if later
		var startHour = Math.max(me.scrollStartHour, me.viewStartHour),
				scrollStart = Math.max(0, startHour - me.viewStartHour);
		if (scrollStart > 0) me.scrollTo(scrollStart * me.hourHeight, true);
	},
	
	// private
	forceSize: Ext.emptyFn,
	
	// private (called from DayViewDropZone)
	onEventResize: function(rec, data) {
		var me = this,
				soDate = Sonicle.Date,
				start = Sonicle.calendar.data.EventMappings.StartDate.name,
				end = Sonicle.calendar.data.EventMappings.EndDate.name;

		if (soDate.compare(rec.data[start], data.StartDate) === 0 &&
				soDate.compare(rec.data[end], data.EndDate) === 0) {
			// no changes
			return;
		}
		
		if (me.fireEvent('beforeeventresize', me, rec, data) !== false) {
			me.doEventResize(rec, data);
		}
	},
	
	doEventResize: function(rec, data) {
		var me = this,
				start = Sonicle.calendar.data.EventMappings.StartDate.name,
				end = Sonicle.calendar.data.EventMappings.EndDate.name;
		
		rec.set(start, data.StartDate);
		rec.set(end, data.EndDate);
		rec.commit();
		
		me.fireEvent('eventupdate', this, rec);
		me.fireEvent('eventresize', this, rec);
	},
	
	// inherited docs
	getEventBodyMarkup: function() {
		if (!this.eventBodyMarkup) {
			this.eventBodyMarkup = [
				'<tpl if="_isTimezone">',
				'<i class="ext-cal-ic {_tzIconCls}">&#160;</i>',
				'</tpl>',
				'<tpl if="_isPrivate">',
				'<i class="ext-cal-ic {_pvtIconCls}">&#160;</i>',
				'</tpl>',
				'{Title}',
				'<tpl if="_isRecurring">',
				'<i class="ext-cal-ic {_recIconCls}">&#160;</i>',
				'</tpl>',
				'<tpl if="_isReminder">',
				'<i class="ext-cal-ic {_remIconCls}">&#160;</i>',
				'</tpl>'
			].join('');
		}
		return this.eventBodyMarkup;
	},
	
	// inherited docs
	getEventTemplate: function() {
		var me = this;
		if (!me.eventTpl) {
			me.eventTpl = !(Ext.isIE || Ext.isOpera) ?
				Ext.create('Ext.XTemplate',
					'<div id="{_elId}" class="{_selectorCls} {_colorCls} ext-cal-evt ext-cal-evr" style="left: {_left}%; width: {_width}%; top: {_top}px; height: {_height}px; background:{_bgColor};">',
						me.enableEventResize ?
						'<div class="ext-evt-rsz ext-evt-rsz-top"><div class="ext-evt-rsz-h">&#160;</div></div>' : '',
						'<div class="ext-evt-bd" style="color:{_foreColor};">', me.getEventBodyMarkup(), '</div>',
						me.enableEventResize ?
						'<div class="ext-evt-rsz ext-evt-rsz-bottom"><div class="ext-evt-rsz-h">&#160;</div></div>' : '',
					'</div>'
				)
				: Ext.create('Ext.XTemplate',
					'<div id="{_elId}" class="ext-cal-evt {_selectorCls} {_colorCls}-x" style="left: {_left}%; width: {_width}%; top: {_top}px; background:{_bgColor};">',
						'<div class="ext-cal-evb">&#160;</div>',
						'<dl style="height: {_height}px;" class="ext-cal-evdm">',
							me.enableEventResize ?
							'<div class="ext-evt-rsz ext-evt-rsz-top"><div class="ext-evt-rsz-h">&#160;</div></div>' : '',
							'<dd class="ext-evt-bd" style="color:{_foreColor};">',
							me.getEventBodyMarkup(),
							'</dd>',
							me.enableEventResize ?
							'<div class="ext-evt-rsz ext-evt-rsz-bottom"><div class="ext-evt-rsz-h">&#160;</div></div>' : '',
						'</dl>',
						'<div class="ext-cal-evb">&#160;</div>',
					'</div>'
				);
			me.eventTpl.compile();
		}
		return me.eventTpl;
	},
	
	/**
	 * <p>Returns the XTemplate that is bound to the calendar's event store (it expects records of type
	 * {@link Sonicle.calendar.EventRecord}) to populate the calendar views with <strong>all-day</strong> events. 
	 * Internally this method by default generates different markup for browsers that support CSS border radius 
	 * and those that don't. This method can be overridden as needed to customize the markup generated.</p>
	 * <p>Note that this method calls {@link #getEventBodyMarkup} to retrieve the body markup for events separately
	 * from the surrounding container markup.  This provdes the flexibility to customize what's in the body without
	 * having to override the entire XTemplate. If you do override this method, you should make sure that your 
	 * overridden version also does the same.</p>
	 * @return {Ext.XTemplate} The event XTemplate
	 */
	getEventAllDayTemplate: function() {
		if (!this.eventAllDayTpl) {
			var tpl,
					body = this.getEventBodyMarkup();

			tpl = !(Ext.isIE || Ext.isOpera) ?
				Ext.create('Ext.XTemplate',
					'<div id="{_elId}" class="{_selectorCls} {_colorCls} {spanCls} ext-cal-evt ext-cal-evr" style="left: {_left}%; width: {_width}%; top: {_top}px; height: {_height}px; background:{_bgColor};">',
					body,
					'</div>'
				)
				: Ext.create('Ext.XTemplate',
					'<div id="{_elId}" class="ext-cal-evt" style="left: {_left}%; width: {_width}%; top: {_top}px; height: {_height}px; background:{_bgColor};">',
						'<div class="{_selectorCls} {spanCls} {_colorCls} ext-cal-evo">',
							'<div class="ext-cal-evm">',
								'<div class="ext-cal-evi">',
								body,
							'</div>',
						'</div>',
					'</div></div>'
				);
			tpl.compile();
			this.eventAllDayTpl = tpl;
		}
		return this.eventAllDayTpl;
	},
	
	// private
	getTemplateEventData: function(evt) {
		var me = this,
				M = Sonicle.calendar.data.EventMappings,
				selector = me.getEventSelectorCls(evt[M.EventId.name]),
				data = {},
				timeFmt = (me.use24HourTime) ? 'G:i ' : 'g:ia ';

		me.getTemplateEventBox(evt);
		data._selectorCls = selector;
		data._bgColor = (evt[M.Color.name] || '');
		data._foreColor = me.getEventForeColor(data._bgColor),
		data._colorCls = 'ext-color-' + (evt[M.Color.name] || 'nocolor') + (evt._renderAsAllDay ? '-ad' : '');
		data._elId = selector + (evt._weekIndex ? '-' + evt._weekIndex : '');
		data._isTimezone = !Ext.isEmpty(evt[M.Timezone.name]);
		data._isPrivate = evt[M.IsPrivate.name];
		data._isRecurring = evt.Recurrence && evt.Recurrence !== '';
		data._isReminder = !Ext.isEmpty(evt[M.Reminder.name]);
		data._tzIconCls = me.timezoneIconCls;
		data._pvtIconCls = me.privateIconCls;
		data._recIconCls = me.recurringIconCls;
		data._remIconCls = me.reminderIconCls;
		var title = evt[M.Title.name];
		data.Title = (evt[M.IsAllDay.name] ? '' : Ext.Date.format(evt[M.StartDate.name], timeFmt)) + (!title || title.length === 0 ? '(No title)' : title);

		return Ext.applyIf(data, evt);
	},
	
	// private
	getEventPositionOffsets: function() {
		return {
			top: 0,
			height: -1
		};
	},
	
	// private
	getTemplateEventBox: function(evt) {
		var me = this,
				heightFactor = me.hourHeight / me.hourIncrement,
				start = evt[Sonicle.calendar.data.EventMappings.StartDate.name],
				end = evt[Sonicle.calendar.data.EventMappings.EndDate.name],
				startOffset = Math.max(start.getHours() - me.viewStartHour, 0),
				endOffset = Math.min(end.getHours() - me.viewStartHour, me.viewEndHour - me.viewStartHour),
				startMins = startOffset * me.hourIncrement,
				endMins = endOffset * me.hourIncrement,
				viewEndDt = Sonicle.Date.add(Ext.Date.clone(end), {hours: me.viewEndHour, clearTime: true}),
				evtOffsets = this.getEventPositionOffsets();
		
		if(start.getHours() >= me.viewStartHour) {
			// only add the minutes if the start is visible, otherwise it offsets the event incorrectly
			startMins += start.getMinutes();
		}
		if(end <= viewEndDt) {
			// only add the minutes if the end is visible, otherwise it offsets the event incorrectly
			endMins += end.getMinutes();
		}

		evt._left = 0;
		evt._width = 100;
		evt._top = startMins * heightFactor + evtOffsets.top;
		evt._height = Math.max(((endMins - startMins) * heightFactor), me.minEventHeight) + evtOffsets.height;
	},
	
	// private
	renderItems: function() {
		var day = 0,
				evts = [],
				ev,
				d,
				ct,
				item,
				i,
				j,
				l,
				emptyCells, skipped,
				evt,
				evt2,
				overlapCols,
				prevCol,
				colWidth,
				evtWidth,
				markup,
				target;
		for (; day < this.dayCount; day++) {
			ev = emptyCells = skipped = 0;
			d = this.eventGrid[0][day];
			ct = d ? d.length : 0;

			for (; ev < ct; ev++) {
				evt = d[ev];
				if (!evt) {
					continue;
				}
				item = evt.data || evt.event.data;
				if (item._renderAsAllDay) {
					continue;
				}
				Ext.apply(item, {
					cls: 'ext-cal-ev',
					_positioned: true
				});
				evts.push({
					data: this.getTemplateEventData(item),
					date: Sonicle.Date.add(this.viewStart, {days: day})
				});
			}
		}

		// overlapping event pre-processing loop
		i = j = overlapCols = prevCol = 0;
		l = evts.length;
		for (; i < l; i++) {
			evt = evts[i].data;
			evt2 = null;
			prevCol = overlapCols;
			for (j = 0; j < l; j++) {
				if (i === j) {
					continue;
				}
				evt2 = evts[j].data;
				if (this.isOverlapping(evt, evt2)) {
					evt._overlap = evt._overlap === undefined ? 1 : evt._overlap + 1;
					if (i < j) {
						if (evt._overcol === undefined) {
							evt._overcol = 0;
						}
						evt2._overcol = evt._overcol + 1;
						overlapCols = Math.max(overlapCols, evt2._overcol);
					}
				}
			}
		}

		// rendering loop
		for (i = 0; i < l; i++) {
			evt = evts[i].data;
			if (evt._overlap !== undefined) {
				colWidth = 100 / (overlapCols + 1);
				evtWidth = 100 - (colWidth * evt._overlap);

				evt._width = colWidth;
				evt._left = colWidth * evt._overcol;
			}
			markup = this.getEventTemplate().apply(evt);
			target = this.id + '-day-col-' + Ext.Date.format(evts[i].date, 'Ymd');

			Ext.core.DomHelper.append(target, markup);
		}

		this.fireEvent('eventsrendered', this);
	},
	
	// private
	getDayEl: function(dt) {
		return Ext.get(this.getDayId(dt));
	},
	
	// private
	getDayId: function(dt) {
		if (Ext.isDate(dt)) {
			dt = Ext.Date.format(dt, 'Ymd');
		}
		return this.id + this.dayColumnElIdDelimiter + dt;
	},
	
	// private
	getDaySize: function() {
		var box = this.el.down('.ext-cal-day-col-inner').getBox();
		return {
			height: box.height,
			width: box.width
		};
	},
	
	// private
	getDayAt: function(x, y) {
		var xoffset = this.el.down('.ext-cal-day-times').getWidth(),
				viewBox = this.el.getBox(),
				daySize = this.getDaySize(false),
				relX = x - viewBox.x - xoffset,
				dayIndex = Math.floor(relX / daySize.width), // clicked col index
				scroll = this.el.getScroll(),
				row = this.el.down('.ext-cal-bg-row'), // first avail row, just to calc size
				rowH = row.getHeight() / this.incrementsPerHour,
				relY = y - viewBox.y - rowH + scroll.top,
				rowIndex = Math.max(0, Math.ceil(relY / rowH)),
				mins = rowIndex * (this.hourIncrement / this.incrementsPerHour),
				dt = Sonicle.Date.add(this.viewStart, {days: dayIndex, minutes: mins, hours: this.viewStartHour}),
				el = this.getDayEl(dt),
				timeX = x;

		if (el) {
			timeX = el.getX();
		}

		return {
			date: dt,
			el: el,
			// this is the box for the specific time block in the day that was clicked on:
			timeBox: {
				x: timeX,
				y: (rowIndex * this.hourHeight / this.incrementsPerHour) + viewBox.y - scroll.top,
				width: daySize.width,
				height: rowH
			}
		};
	},
	
	// private
	onClick: function(e, t) {
		if (this.dragPending || Sonicle.calendar.view.DayBody.superclass.onClick.apply(this, arguments)) {
			// The superclass handled the click already so exit
			return;
		}
		if (e.getTarget('.ext-cal-day-times', 3) !== null) {
			// ignore clicks on the times-of-day gutter
			return;
		}
		var el = e.getTarget('td', 3);
		if (el) {
			if (el.id && el.id.indexOf(this.dayElIdDelimiter) > -1) {
				var dt = this.getDateFromId(el.id, this.dayElIdDelimiter);
				this.fireEvent('dayclick', this, Ext.Date.parseDate(dt, 'Ymd'), true, Ext.get(this.getDayId(dt, true)));
				return;
			}
		}
		var day = this.getDayAt(e.getX(), e.getY());
		if (day && day.date) {
			this.fireEvent('dayclick', this, day.date, false, null);
		}
	}
});
