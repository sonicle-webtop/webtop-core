/**
 * @class Sonicle.calendar.view.AbstractCalendar
 * @extends Ext.BoxComponent
 * <p>This is an abstract class that serves as the base for other calendar views. This class is not
 * intended to be directly instantiated.</p>
 * <p>When extending this class to create a custom calendar view, you must provide an implementation
 * for the <code>renderItems</code> method, as there is no default implementation for rendering events
 * The rendering logic is totally dependent on how the UI structures its data, which
 * is determined by the underlying UI template (this base class does not have a template).</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.AbstractCalendar', {
	extend: 'Ext.Component',
	alias: 'widget.calendarview',
	requires: [
		'Sonicle.Date',
		'Sonicle.ColorUtils',
		'Sonicle.calendar.util.EventUtils',
		'Sonicle.calendar.data.EventMappings'
	],
	
	/**
	 * @cfg {String} timezoneIconCls
	 * A css class which sets an image to be used as the icon for timezone events.
	 * There are no default icon classes that come with this component.
	 */
	
	/**
	 * @cfg {String} privateIconCls
	 * A css class which sets an image to be used as the icon for private events.
	 * There are no default icon classes that come with this component.
	 */
	
	/**
	 * @cfg {String} reminderIconCls
	 * A css class which sets an image to be used as the icon for reminder events.
	 * There are no default icon classes that come with this component.
	 */
	
	/**
	 * @cfg {String} recurrenceIconCls
	 * A css class which sets an image to be used as the icon for recurring events.
	 * There are no default icon classes that come with this component.
	 */
	
	/**
	 * @cfg {String} recurrenceBrokenIconCls
	 * A css class which sets an image to be used as the icon for recurring broken events.
	 * There are no default icon classes that come with this component.
	 */
	
	/**
	 * @cfg {Number} dayCount
	 * The number of days to display in the view (defaults to 1)
	 */
	dayCount: 1,
	
	/**
	 * @cfg {Number} startDay
	 * The 0-based index for the day on which the calendar week begins (0=Sunday, which is the default)
	 */
	startDay: 0,
	
	/**
	 * @cfg {Boolean} startDayIsStatic
	 * If you set <tt>startDayIsStatic</tt> to <tt>true</tt>, then the view will *always* begin on
	 * {@link #startDay}. For any {@link #dayCount} less than 7, days outside the startDay + dayCount range
	 * will not be viewable. If a date that is not in the viewable range is set into the view it will
	 * automatically advance to the first viewable date for the current range.  This could be useful for
	 * creating custom views like a weekday-only or weekend-only view.
	 * 
	 * Some example {@link Sonicle.calendar.CalendarPanel CalendarPanel} configs:
	 * 
	 *		// Weekdays only:
	 *		showMultiDayView: true,
	 *		multiDayViewCfg: {
	 *			dayCount: 5,
	 *			startDay: 1,
	 *			startDayIsStatic: true
	 *		}
	 *
	 *		// Weekends only:
	 *		showMultiDayView: true,
	 *		multiDayViewCfg: {
	 *			dayCount: 2,
	 *			startDay: 6,
	 *			startDayIsStatic: true
	 *		}
	 */
	startDayIsStatic: false,
	
	/**
	 * @cfg {Boolean} user24HourTime
	 * Determines whether times should be displayed as 12 hour times with am/pm (default)
	 * or 24 hour / military format.
	 */
	use24HourTime: false,
	
	/**
	 * @cfg {String} timezone
	 * Timezone that should be considered as default view timezone.
	 * A globe icon will be displayed on events that have a timezone different to this.
	 */
	timezone: null,
	
	/**
	 * @cfg {Boolean} spansHavePriority
	 * Allows switching between two different modes of rendering events that span multiple days. When true,
	 * span events are always sorted first, possibly at the expense of start dates being out of order (e.g., 
	 * a span event that starts at 11am one day and spans into the next day would display before a non-spanning 
	 * event that starts at 10am, even though they would not be in date order). This can lead to more compact
	 * layouts when there are many overlapping events. If false (the default), events will always sort by start date
	 * first which can result in a less compact, but chronologically consistent layout.
	 */
	spansHavePriority: false,
	
	/**
	 * @cfg {Boolean} trackMouseOver
	 * Whether or not the view tracks and responds to the browser mouseover event on contained elements (defaults to
	 * true). If you don't need mouseover event highlighting you can disable this.
	 */
	trackMouseOver: true,
	
	/**
	 * @cfg {Boolean} enableFx
	 * Determines whether or not visual effects for CRUD actions are enabled (defaults to true). If this is false
	 * it will override any values for {@link #enableAddFx}, {@link #enableUpdateFx} or {@link enableRemoveFx} and
	 * all animations will be disabled.
	 */
	enableFx: true,
	
	/**
	 * @cfg {Boolean} enableAddFx
	 * True to enable a visual effect on adding a new event (the default), false to disable it. Note that if 
	 * {@link #enableFx} is false it will override this value. The specific effect that runs is defined in the
	 * {@link #doAddFx} method.
	 */
	enableAddFx: true,
	
	/**
	 * @cfg {Boolean} enableUpdateFx
	 * True to enable a visual effect on updating an event, false to disable it (the default). Note that if 
	 * {@link #enableFx} is false it will override this value. The specific effect that runs is defined in the
	 * {@link #doUpdateFx} method.
	 */
	enableUpdateFx: false,
	
	/**
	 * @cfg {Boolean} enableRemoveFx
	 * True to enable a visual effect on removing an event (the default), false to disable it. Note that if 
	 * {@link #enableFx} is false it will override this value. The specific effect that runs is defined in the
	 * {@link #doRemoveFx} method.
	 */
	enableRemoveFx: true,
	
	/**
	 * @cfg {Boolean} enableDD
	 * True to enable drag and drop in the calendar view (the default), false to disable it
	 */
	enableDD: true,
	
	/**
	 * @cfg {Boolean} monitorResize
	 * True to monitor the browser's resize event (the default), false to ignore it. If the calendar view is rendered
	 * into a fixed-size container this can be set to false. However, if the view can change dimensions (e.g., it's in 
	 * fit layout in a viewport or some other resizable container) it is very important that this config is true so that
	 * any resize event propagates properly to all subcomponents and layouts get recalculated properly.
	 */
	monitorResize: true,
	
	/**
	 * @cfg {String} todayText
	 * The text to display in the current day's box in the calendar when {@link #showTodayText} is true (defaults to 'Today')
	 */
	todayText: 'Today',
	
	/**
	 * @cfg {String} ddCreateEventText
	 * The text to display inside the drag proxy while dragging over the calendar to create a new event (defaults to 
	 * 'Create event for {0}' where {0} is a date range supplied by the view)
	 */
	ddCreateEventText: 'Create event for {0}',
	
	/**
	 * @cfg {String} ddCopyEventText
	 * The text to display inside the drag proxy while alt-dragging an event to copy it (defaults to
	 * 'Copy event to {0}' where {0} is the updated event start date/time supplied by the view)
	 */
	ddCopyEventText: 'Copy event to {0}',
	
	/**
	 * @cfg {String} ddMoveEventText
	 * The text to display inside the drag proxy while dragging an event to reposition it (defaults to 
	 * 'Move event to {0}' where {0} is the updated event start date/time supplied by the view)
	 */
	ddMoveEventText: 'Move event to {0}',
	
	/**
	 * @cfg {String} ddResizeEventText
	 * The string displayed to the user in the drag proxy while dragging the resize handle of an event (defaults to 
	 * 'Update event to {0}' where {0} is the updated event start-end range supplied by the view). Note that 
	 * this text is only used in views
	 * that allow resizing of events.
	 */
	ddResizeEventText: 'Update event to {0}',
	
	/**
	 * @cfg {String} ddDateFormat
	 * String used for formatting date in texts ({@link #ddCreateEventText}, {@link #ddCopyEventText} or 
	 * {@link #ddMoveEventText}) displayed in the drag proxy while dragging an event.
	 */
	ddDateFormat: 'n/j',
	
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
	dateParamFormat: 'Y-m-d',
	
	/**
	 * @property ownerCalendarPanel
	 * @readonly
	 * @type Sonicle.calendar.CalendarPanel
	 * If this view is hosted inside a {@link Sonicle.calendar.CalendarPanel} this property will reference it.
	 * If the view was created directly outside of a CalendarPanel this property will be null.
	 */
	ownerCalendarPanel: null,
	
	/**
	 * @property titleEditor
	 * @readonly
	 * @type Ext.Editor
	 */
	titleEditor: null,
	
	//private properties -- do not override:
	weekCount: 1,
	eventTitleSelector: '.ext-evt-bd',
	eventSelector: '.ext-cal-evt',
	eventSelectorDepth: 10,
	eventOverClass: 'ext-evt-over',
	eventElIdDelimiter: '-evt-',
	dayElIdDelimiter: '-day-',
	
	/**
	 * Returns a string of HTML template markup to be used as the body portion of the event template created
	 * by {@link #getEventTemplate}. This provdes the flexibility to customize what's in the body without
	 * having to override the entire XTemplate. This string can include any valid {@link Ext.Template} code, and
	 * any data tokens accessible to the containing event template can be referenced in this string.
	 * @return {String} The body template string
	 */
	getEventBodyMarkup: Ext.emptyFn,
	// must be implemented by a subclass

	/**
	 * <p>Returns the XTemplate that is bound to the calendar's event store (it expects records of type
	 * {@link Sonicle.calendar.EventRecord}) to populate the calendar views with events. Internally this method
	 * by default generates different markup for browsers that support CSS border radius and those that don't.
	 * This method can be overridden as needed to customize the markup generated.</p>
	 * <p>Note that this method calls {@link #getEventBodyMarkup} to retrieve the body markup for events separately
	 * from the surrounding container markup.  This provdes the flexibility to customize what's in the body without
	 * having to override the entire XTemplate. If you do override this method, you should make sure that your 
	 * overridden version also does the same.</p>
	 * @return {Ext.XTemplate} The event XTemplate
	 */
	getEventTemplate: Ext.emptyFn,
	
	/**
	 * @event eventsrendered
	 * Fires after events are finished rendering in the view
	 * @param {Sonicle.calendar.view.AbstractCalendar} this 
	 */

	/**
	 * @event eventclick
	 * Fires after the user clicks on an event element
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that was clicked on
	 * @param {HTMLNode} el The DOM node that was clicked on
	 * @param {Ext.event.Event} evt The raw event object.
	 */
	
	/**
	 * @event eventdblclick
	 * Fires after the user clicks on an event element
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that was clicked on
	 * @param {HTMLNode} el The DOM node that was clicked on
	 * @param {Ext.event.Event} evt The raw event object.
	 */

	/**
	 * @event eventover
	 * Fires anytime the mouse is over an event element
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that the cursor is over
	 * @param {HTMLNode} el The DOM node that is being moused over
	 */

	/**
	 * @event eventout
	 * Fires anytime the mouse exits an event element
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that the cursor exited
	 * @param {HTMLNode} el The DOM node that was exited
	 */

	/**
	 * @event datechange
	 * Fires after the start date of the view changes
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Date} startDate The start date of the view (as explained in {@link #getStartDate}
	 * @param {Date} viewStart The first displayed date in the view
	 * @param {Date} viewEnd The last displayed date in the view
	 */

	/**
	 * @event rangeselect
	 * Fires after the user drags on the calendar to select a range of dates/times in which to create an event
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Object} dates An object containing the start (StartDate property) and end (EndDate property) dates selected
	 * @param {Function} callback A callback function that MUST be called after the event handling is complete so that
	 * the view is properly cleaned up (shim elements are persisted in the view while the user is prompted to handle the
	 * range selection). The callback is already created in the proper scope, so it simply needs to be executed as a standard
	 * function call (e.g., callback()).
	 */

	/**
	 * @event beforeeventcopy
	 * Fires before an existing event is duplicated by the user via the "copy" command. This is a
	 * cancelable event, so returning false from a handler will cancel the copy operation.
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.data.EventModel} rec The {@link Sonicle.calendar.data.EventModel
	 * record} for the event that will be copied
	 * @param {Date} dt The new start date to be set in the copy (the end date will be automaticaly
	 * adjusted to match the original event duration)
	 */

	/**
	 * @event eventcopy
	 * Fires after an event has been duplicated by the user via the "copy" command. If you need to
	 * cancel the copy operation you should handle the {@link #beforeeventcopy} event and return
	 * false from your handler function.
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.data.EventModel} rec The {@link Sonicle.calendar.data.EventModel
	 * record} for the event that was copied (with updated start and end dates)
	 */

	/**
	 * @event beforeeventmove
	 * Fires after an event element has been dragged by the user and dropped in a new position, but before
	 * the event record is updated with the new dates, providing a hook for canceling the update.
	 * To cancel the move, return false from a handling function. This could be useful for validating
	 * that a user can only move events within a certain date range, for example.
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.data.EventModel} rec The {@link Sonicle.calendar.data.EventModel record}
	 * for the event that will be moved. Start and end dates will be the original values before the move started.
	 * @param {Date} dt The new start date to be set (the end date will be automaticaly calculated to match
	 * based on the event duration)
	 */

	/**
	 * @event eventmove
	 * Fires after an event element has been moved to a new position and its data updated. If you need to
	 * cancel the move operation you should handle the {@link #beforeeventmove} event and return false
	 * from your handler function.
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.data.EventModel} rec The {@link Sonicle.calendar.data.EventModel record}
	 * for the event that was moved with updated start and end dates
	 */

	/**
	 * @event initdrag
	 * Fires when a drag operation is initiated in the view
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 */

	/**
	 * @event dayover
	 * Fires while the mouse is over a day element 
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Date} dt The date that is being moused over
	 * @param {Ext.core.Element} el The day Element that is being moused over
	 */

	/**
	 * @event dayout
	 * Fires when the mouse exits a day element 
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Date} dt The date that is exited
	 * @param {Ext.core.Element} el The day Element that is exited
	 */

	/*
	 * @event eventdelete
	 * Fires after an event element is deleted by the user. Not currently implemented directly at the view level -- currently 
	 * deletes only happen from one of the forms.
	 * @param {Sonicle.calendar.view.AbstractCalendar} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that was deleted
	 */

	// must be implemented by a subclass
	// private
	initComponent: function () {
		var me = this;
		me.setStartDate(me.startDate || new Date());
		
		me.callParent(arguments);
		
		me.titleEditor = Ext.create({
			xtype: 'editor',
			updateEl: false,
			hideEl: false,
			
			alignment: 'c-c',
			minHeight: 24,
			autoSize: {
				width: 'boundEl',
				height: 'boundEl'
			},
			field: {
				xtype: 'textarea',
				enterIsSpecial: true,
				fieldCls: 'ext-cal-editor'
			},
			listeners: {
				startEdit: function(s, el) {
					// Fix: avoids incorrect auto editor alignment 
					// performed using alignment config
					Ext.defer(function() {
						s.alignTo(el, 'c-c');
					}, 10);
				},
				complete: function(s, val, sval) {
					if(val !== sval) {
						var rec = me.getEventRecord(s.getItemId());
						me.doEventTitleUpdate(rec, val);
					}
				}
			}
		});
	},
	
	// private
	afterRender: function () {
		var me = this;
		me.callParent(arguments);

		me.renderTemplate();

		if (me.store) {
			me.setStore(me.store, true);
		}

		me.el.on({
			'mouseover': me.onMouseOver,
			'mouseout': me.onMouseOut,
			'click': me.onClick,
			'dblclick': me.onClick,
			'contextmenu': me.onContextMenu,
			scope: me
		});
		
		me.el.unselectable();

		if (me.enableDD && me.initDD) {
			me.initDD();
		}

		me.on('eventsrendered', me.forceSize);
		Ext.defer(me.forceSize, 100, me);
	},
	
	// private
	forceSize: function () {
		if (this.el && this.el.down) {
			var hd = this.el.down('.ext-cal-hd-ct'),
					bd = this.el.down('.ext-cal-body-ct');
			
			if (!bd || !hd) return;
			var headerHeight = hd.getHeight(),
					sz = this.el.parent().getSize();
			bd.setHeight(sz.height - headerHeight);
		}
	},
	
	getWeekCount: function () {
		var days = Sonicle.Date.diffDays(this.viewStart, this.viewEnd);
		return Math.ceil(days / this.dayCount);
	},
	
	// private
	prepareData: function () {
		var me = this,
				EM = Sonicle.calendar.data.EventMappings,
				lastInMonth = Ext.Date.getLastDateOfMonth(me.startDate),
				w = 0, d,
				dt = Ext.Date.clone(me.viewStart),
				weeks = me.weekCount < 1 ? 6 : me.weekCount;

		me.eventGrid = [[]];
		me.allDayGrid = [[]];
		me.evtMaxCount = [];

		var evtsInView = me.store.queryBy(function (rec) {
			return me.isEventVisible(rec.data);
		}, me);
		
		var filterFn = function(rec) {
			var startDt = Ext.Date.clearTime(rec.data[EM.StartDate.name], true),
					startsOnDate = dt.getTime() === startDt.getTime(),
					spansFromPrevView = ((w === 0) && (d === 0) && (dt > rec.data[EM.StartDate.name]));
			
			return startsOnDate || spansFromPrevView;
		};

		for (; w < weeks; w++) {
			me.evtMaxCount[w] = 0;
			if (me.weekCount === -1 && dt > lastInMonth) {
				//current week is fully in next month so skip
				break;
			}
			me.eventGrid[w] = me.eventGrid[w] || [];
			me.allDayGrid[w] = me.allDayGrid[w] || [];

			for (d = 0; d < me.dayCount; d++) {
				if (evtsInView.getCount() > 0) {
					var evts = evtsInView.filterBy(filterFn, me);

					me.sortEventRecordsForDay(evts);
					me.prepareEventGrid(evts, w, d);
				}
				dt = Sonicle.Date.add(dt, {days: 1});
			}
		}
		this.currentWeekCount = w;
	},
	
	// private
	prepareEventGrid: function (evts, w, d) {
		var me = this,
				soDate = Sonicle.Date,
				EU = Sonicle.calendar.util.EventUtils,
				EM = Sonicle.calendar.data.EventMappings,
				row = 0;

		evts.each(function (evt) {
			if (EU.isSpanning(evt.data[EM.StartDate.name], evt.data[EM.EndDate.name])) {
				// Event spans on multiple days...
				var daysInView = soDate.diffDays(
							soDate.max(me.viewStart, evt.data[EM.StartDate.name]),
							soDate.min(me.viewEnd, evt.data[EM.EndDate.name])
					) +1;
				
				//TODO: 24h threshold as config
				if(EU.durationInHours(evt.data[EM.StartDate.name], evt.data[EM.EndDate.name]) >= 24) {
					me.prepareEventGridSpans(evt, me.eventGrid, w, d, daysInView);
					me.prepareEventGridSpans(evt, me.allDayGrid, w, d, daysInView, true);
				} else {
					// If event length in hours is less than desired threshold,
					// prepare info for drawing it in day view (event spans vertically) 
					// instead of in header view (event spans horizontally)
					me.prepareEventGridSpans(evt, me.eventGrid, w, d, daysInView);
				}
				
			} else {
				// Event take only single day...
				row = me.findEmptyRowIndex(w, d);
				me.eventGrid[w][d] = me.eventGrid[w][d] || [];
				me.eventGrid[w][d][row] = evt;

				if (evt.data[EM.IsAllDay.name]) {
					row = me.findEmptyRowIndex(w, d, true);
					me.allDayGrid[w][d] = me.allDayGrid[w][d] || [];
					me.allDayGrid[w][d][row] = evt;
				}
			}
			
			me.setMaxEventsForDay(w, d);
			return true;
		});
	},
	
	setMaxEventsForDay: function(weekIndex, dayIndex) {
		var me = this,
				max = (me.maxEventsPerDay + 1) || 999;
		
		// If calculating the max event count for the day/week view header, use the allDayGrid
		// so that only all-day events displayed in that area get counted, otherwise count all events.
		var maxEventsForDay = me[me.isHeaderView ? 'allDayGrid' : 'eventGrid'][weekIndex][dayIndex] || [];
		
		me.evtMaxCount[weekIndex] = me.evtMaxCount[weekIndex] || 0;
		
		if(maxEventsForDay.length && me.evtMaxCount[weekIndex] < maxEventsForDay.length) {
			me.evtMaxCount[weekIndex] = Math.min(max, maxEventsForDay.length);
		}
	},
	
	// private
	prepareEventGridSpans: function (evt, grid, w, d, days, allday) {
		// this event spans multiple days/weeks, so we have to preprocess
		// the events and store special span events as placeholders so that
		// the render routine can build the necessary TD spans correctly.
		var me = this,
				w1 = w,
				d1 = d,
				row = me.findEmptyRowIndex(w, d, allday),
				dt = Ext.Date.clone(me.viewStart);

		var start = {
			event: evt,
			isSpan: true,
			isSpanStart: true,
			spanTop: false,
			spanBottom: false,
			spanLeft: false,
			spanRight: (d === 6)
		};
		grid[w][d] = grid[w][d] || [];
		grid[w][d][row] = start;

		me.setMaxEventsForDay(w, d);

		while (--days) {
			dt = Sonicle.Date.add(dt, {days: 1});
			if (dt > me.viewEnd) {
				break;
			}
			if (++d1 > 6) {
				// reset counters to the next week
				d1 = 0;
				w1++;
				row = me.findEmptyRowIndex(w1, 0);
			}
			grid[w1] = grid[w1] || [];
			grid[w1][d1] = grid[w1][d1] || [];

			grid[w1][d1][row] = {
				event: evt,
				isSpan: true,
				isSpanStart: (d1 === 0),
				spanLeft: (w1 > w) && (d1 % 7 === 0),
				spanRight: (d1 === 6) && (days > 1)
			};

			// In this loop we are pre-processing empty span placeholders. In the case
			// where a given week might only contain such spans, we have to make this
			// max event check on each iteration to make sure that our empty placeholder
			// divs get created correctly even without "real" events:
			me.setMaxEventsForDay(w1, d1);
		}
	},
	
	// private
	findEmptyRowIndex: function (w, d, allday) {
		var grid = allday ? this.allDayGrid : this.eventGrid,
				day = grid[w] ? grid[w][d] || [] : [],
				i = 0,
				ln = day.length;

		for (; i < ln; i++) {
			if (!day[i])
				return i;
		}
		return ln;
	},
	
	// private
	renderTemplate: function () {
		if (this.tpl) {
			this.el.select('*').destroy();
			this.tpl.overwrite(this.el, this.getParams());
			this.lastRenderStart = Ext.Date.clone(this.viewStart);
			this.lastRenderEnd = Ext.Date.clone(this.viewEnd);
		}
	},
	
	disableStoreEvents: function () {
		this.monitorStoreEvents = false;
	},
	
	enableStoreEvents: function (refresh) {
		this.monitorStoreEvents = true;
		if (refresh === true) {
			this.refresh();
		}
	},
	
	// private
	onResize: function () {
		this.callParent(arguments);
		this.refresh(false);
	},
	
	// private
	onInitDrag: function () {
		this.fireEvent('initdrag', this);
	},
	
	// private
	onEventDrop: function (rec, dt, mode) {
		this[(mode || 'move') + 'Event'](rec, dt);
	},
	
	// private
	onCalendarEndDrag: function (start, end, onComplete) {
		var me = this,
				EM = Sonicle.calendar.data.EventMappings,
				dates = {};
		
		if (start && end) {
			// set this flag for other event handlers that might conflict while we're waiting
			me.dragPending = true;
			
			dates[EM.StartDate.name] = start;
			dates[EM.EndDate.name] = end;
			
			// have to wait for the user to save or cancel before finalizing the dd interation
			var boundOnComplete = Ext.bind(me.onCalendarEndDragComplete, me, [onComplete]);
			if(me.fireEvent('rangeselect', me, dates, boundOnComplete) !== false) {
				// if handled, user must call boundOnComplete method!
			} else {
				// client code canceled the selection so clean up immediately
				me.onCalendarEndDragComplete(boundOnComplete);
			}
		}
	},
	
	// private
	onCalendarEndDragComplete: function (onComplete) {
		// callback for the drop zone to clean up
		onComplete();
		// clear flag for other events to resume normally
		this.dragPending = false;
	},
	
	/**
	 * Refresh the view. Determine if a store reload is required after a given CRUD operation.
	 * @param {String} action One of 'create', 'update' or 'delete'
	 * @param {Ext.data.Operation} operation he affected operation
	 */
	refreshAfterEventChange: function(action, operation) {
		// Determine if a store reload is needed. A store reload is needed if the event is recurring after being
		// edited or was recurring before being edited AND an event store reload has not been triggered already for
		// this operation. If an event is not currently recurring (isRecurring = false) but still has an instance
		
		//TODO: completare...
		this.refresh(true);
	},
	
	onStoreBeforeLoad: function(store, op, o) {
		op.setParams(Ext.apply(op.getParams() || {}, this.getStoreParams()));
	},
	
	onStoreLoad: function(sto, recs, succ) {
		this.refresh(false);
	},
	
	onStoreWrite: function(store, op) {
		if(op.wasSuccessful()) {
			switch(op.action) {
				case 'create':
					this.onAdd(store, op);
					break;
				case 'update':
					this.onUpdate(store, op, Ext.data.Record.COMMIT);
					break;
				case 'destroy':
					this.onRemove(store, op);
					break;
			}
		}
	},
	
	// private
	onAdd: function (store, op) {
		var me = this,
				rec = op.getRecords()[0];
		
		if(me.hidden === true || me.ownerCt.hidden === true || me.monitorStoreEvents === false) {
			// Hidden calendar view don't need to be refreshed. For views composed of header and body (for example
			// Sonicle.calendar.view.Day or Sonicle.calendar.view.Week) we need to check the ownerCt to find out
			// if a view is hidden.
			return;
		}
		
		me.refresh(true);
		if (me.enableFx && me.enableUpdateFx) {
			me.doAddFx(me.getEventEls(rec.data[Sonicle.calendar.data.EventMappings.Id.name]), {
				scope: me
			});
		}
	},
	
	// private
	onUpdate: function (store, op, updateType) {
		var me = this,
				rec = op.getRecords()[0];
		
		if (me.hidden === true || me.ownerCt.hidden === true || me.monitorStoreEvents === false) {
			// Hidden calendar view don't need to be refreshed. For views composed of header and body (for example
			// Sonicle.calendar.view.Day or Sonicle.calendar.view.Week) we need to check the ownerCt to find out
			// if a view is hidden.
			return;
		}
		if (updateType === Ext.data.Record.COMMIT) {
			me.refresh(true);
			if (me.enableFx && me.enableUpdateFx) {
				me.doUpdateFx(me.getEventEls(rec.data[Sonicle.calendar.data.EventMappings.Id.name]), {
					scope: me
				});
			}
		}
	},
	
	// private
	onRemove: function (store, op) {
		var me = this,
				EM = Sonicle.calendar.data.EventMappings,
				rec = op.getRecords()[0];
		
		if (me.hidden === true || me.ownerCt.hidden === true || me.monitorStoreEvents === false) {
			// Hidden calendar view don't need to be refreshed. For views composed of header and body (for example
			// Sonicle.calendar.view.Day or Sonicle.calendar.view.Week) we need to check the ownerCt to find out
			// if a view is hidden.
			return;
		}
		
		if(me.enableFx && me.enableRemoveFx) {
			me.doRemoveFx(me.getEventEls(rec.data[EM.EventId.name]), {
				remove: true,
				scope: me,
				callback: Ext.bind(me.refresh, me, [])
			});
		} else {
			me.getEventEls(rec.data[EM.EventId.name]).remove();
			me.refresh();
		}
	},

    /**
	 * Provides the element effect(s) to run after an event is added. The method is passed a {@link Ext.CompositeElement}
	 * that contains one or more elements in the DOM representing the event that was added. The default
	 * effect is {@link Ext.Element#fadeIn fadeIn}. Note that this method will only be called when
	 * {@link #enableAddFx} is true (it is true by default).
	 * @param {Ext.CompositeElement} el The {@link Ext.CompositeElement} representing the added event
	 * @param {Object} options An options object to be passed through to any Element.Fx methods. By default this
	 * object only contains the current scope (<tt>{scope:this}</tt>) but you can also add any additional fx-specific
	 * options that might be needed for a particular effect to this object.
	 */
	doAddFx: function (els, o) {
		els.fadeIn(Ext.apply(o, {
			duration: 2000
		}));
	},
	
    /**
	 * Provides the element effect(s) to run after an event is updated. The method is passed a {@link Ext.CompositeElement}
	 * that contains one or more elements in the DOM representing the event that was updated. The default
	 * effect is {@link Ext.Element#highlight highlight}. Note that this method will only be called when
	 * {@link #enableUpdateFx} is true (it is false by default).
	 * @param {Ext.CompositeElement} el The {@link Ext.CompositeElement} representing the updated event
	 * @param {Object} options An options object to be passed through to any Element.Fx methods. By default this
	 * object only contains the current scope (<tt>{scope:this}</tt>) but you can also add any additional fx-specific
	 * options that might be needed for a particular effect to this object.
	 */
	doUpdateFx: function (els, o) {
		this.highlightEvent(els, null, o);
	},
	
    /**
	 * Provides the element effect(s) to run after an event is removed. The method is passed a {@link Ext.CompositeElement}
	 * that contains one or more elements in the DOM representing the event that was removed. The default
	 * effect is {@link Ext.Element#fadeOut fadeOut}. Note that this method will only be called when
	 * {@link #enableRemoveFx} is true (it is true by default).
	 * @param {Ext.CompositeElement} el The {@link Ext.CompositeElement} representing the removed event
	 * @param {Object} options An options object to be passed through to any Element.Fx methods. By default this
	 * object contains the following properties:
	 *		{
	 *			remove: true, // required by fadeOut to actually remove the element(s)
	 *			scope: this,  // required for the callback
	 *			callback: fn  // required to refresh the view after the fx finish
	 *		}
	 * While you can modify this options object as needed if you change the effect used, please note that the
	 * callback method (and scope) MUST still be passed in order for the view to refresh correctly after the removal.
	 * Please see the inline code comments before overriding this method.
	 */
	doRemoveFx: function (els, o) {
		// Please make sure you keep this entire code block or removing events might not work correctly!
		// Removing is a little different because we have to wait for the fx to finish, then we have to actually
		// refresh the view AFTER the fx are run (this is different than add and update).
		if(els.getCount() === 0 && Ext.isFunction(o.callback)) {
			// if there are no matching elements in the view make sure the callback still runs.
			// this can happen when an event accessed from the "more" popup is deleted.
			o.callback.call(o.scope || this);
		} else {
			// If you'd like to customize the remove fx do so here. Just make sure you
			// DO NOT override the default callback property on the options object, and that
			// you still pass that object in whatever fx method you choose.
			els.fadeOut(o);
		}
	},
	
	/**
	 * Builds strings useful for displaying an event.
	 * @param {Object} edata Event data.
	 * @param {String} timeFmt Desired time format string.
	 * @return {Object} An object containing title and tooltip properties.
	 */
	buildEventDisplayInfo: function(edata, timeFmt) {
		var EM = Sonicle.calendar.data.EventMappings,
				title = edata[EM.Title.name],
				location = edata[EM.Location.name],
				start = Ext.Date.format(edata[EM.StartDate.name], timeFmt),
				end = Ext.Date.format(edata[EM.EndDate.name], timeFmt),
				titloc = Ext.isEmpty(location) ? title : Ext.String.format('{0} @{1}', title, location);
		
		return {
			title: titloc,
			tooltip: (edata[EM.IsAllDay.name] === true) ? titloc : Ext.String.format('{0} {1} {2}', start, end, titloc)
		};
	},
	
	/**
	 * Visually highlights an event using {@link Ext.Fx#highlight} config options.
	 * If {@link #highlightEventActions} is false this method will have no effect.
	 * @param {Ext.CompositeElement} els The element(s) to highlight
	 * @param {Object} color (optional) The highlight color. Should be a 6 char hex 
	 * color without the leading # (defaults to yellow: 'ffff9c')
	 * @param {Object} o (optional) Object literal with any of the {@link Ext.Fx} config 
	 * options. See {@link Ext.Fx#highlight} for usage examples.
	 */
	highlightEvent: function (els, color, o) {
		if (this.enableFx) {
			var c;
			!(Ext.isIE || Ext.isOpera) ?
					els.highlight(color, o) :
					// Fun IE/Opera handling:
					els.each(function (el) {
						el.highlight(color, Ext.applyIf({
							attr: 'color'
						},
						o));
						c = el.down('.ext-cal-evm');
						if (c) {
							c.highlight(color, o);
						}
					},
							this);
		}
	},
	
	/**
	 * Retrieve an Event object's id from its corresponding node in the DOM.
	 * @param {String/Element/HTMLElement} el An {@link Ext.core.Element}, DOM node or id
	 */
	getEventIdFromEl: function (el) {
		el = Ext.get(el);
		var id = el.id.split(this.eventElIdDelimiter)[1],
				lastHypen = id.lastIndexOf('-');

		// MUST look for last hyphen because autogenned record IDs can contain hyphens
		if (lastHypen > -1) {
			//This id has the index of the week it is rendered in as the suffix.
			//This allows events that span across weeks to still have reproducibly-unique DOM ids.
			id = id.substr(0, lastHypen);
		}
		return id;
	},
	
	// private
	getEventId: function (eventId) {
		if (eventId === undefined && this.tempEventId) {
			eventId = this.tempEventId;
		}
		return eventId;
	},
	
	getEventForeColor: function(bgColor) {
		var me = this,
				etc = me.eventTextColor;
		if(etc === 'auto') {
			return Sonicle.ColorUtils.getBestContrast(bgColor, me.colorLuminance);
		} else if(etc === 'white') {
			return '#FFFFFF';
		} else {
			return '#000000';
		}
	},
	
	/**
	 * 
	 * @param {String} eventId
	 * @param {Boolean} forSelect
	 * @return {String} The selector class
	 */
	getEventSelectorCls: function (eventId, forSelect) {
		var prefix = forSelect ? '.' : '';
		return prefix + this.id + this.eventElIdDelimiter + this.getEventId(eventId);
	},
	
	/**
	 * 
	 * @param {String} eventId
	 * @return {Ext.CompositeElement} The matching CompositeElement of nodes
	 * that comprise the rendered event. Any event that spans across a view 
	 * boundary will contain more than one internal Element.
	 */
	getEventEls: function (eventId) {
		var els = Ext.select(this.getEventSelectorCls(this.getEventId(eventId), true), false, this.el.dom);
		return new Ext.CompositeElement(els);
	},
	
	/**
	 * Returns true if the view is currently displaying today's date, else false.
	 * @return {Boolean} True or false
	 */
	isToday: function () {
		var today = Ext.Date.clearTime(new Date()).getTime();
		return this.viewStart.getTime() <= today && this.viewEnd.getTime() >= today;
	},
	
	// private
	isEventVisible: function (evt) {
		var M = Sonicle.calendar.data.EventMappings,
				data = evt.data || evt,
				start = this.viewStart.getTime(),
				end = this.viewEnd.getTime(),
				evStart = data[M.StartDate.name].getTime(),
				evEnd = data[M.EndDate.name].getTime();
		evEnd = Sonicle.Date.add(data[M.EndDate.name], {seconds: -1}).getTime();

		return this.rangesOverlap(start, end, evStart, evEnd);
	},
	
	rangesOverlap: function (start1, end1, start2, end2) {
		var startsInRange = (start1 >= start2 && start1 <= end2),
				endsInRange = (end1 >= start2 && end1 <= end2),
				spansRange = (start1 <= start2 && end1 >= end2);

		return (startsInRange || endsInRange || spansRange);
	},
	
	// private
	isOverlapping: function (evt1, evt2) {
		var soDate = Sonicle.Date,
				EM = Sonicle.calendar.data.EventMappings,
				ev1 = evt1.data ? evt1.data : evt1,
				ev2 = evt2.data ? evt2.data : evt2,
				start1 = ev1[EM.StartDate.name].getTime(),
				end1 = soDate.add(ev1[EM.EndDate.name], {seconds: -1}).getTime(),
				start2 = ev2[EM.StartDate.name].getTime(),
				end2 = soDate.add(ev2[EM.EndDate.name], {seconds: -1}).getTime();

		if (end1 < start1) end1 = start1;
		if (end2 < start2) end2 = start2;
		return (start1 <= end2 && end1 >= start2);
	},
	
	isEventSpanning: function(evt) {
		var EU = Sonicle.calendar.util.EventUtils,
				EM = Sonicle.calendar.data.EventMappings,
				data = evt.data || evt;
		return EU.isSpanning(data[EM.StartDate.name], data[EM.EndDate.name]);
	},
	
	/*
	isEventSpanning: function(evt, hoursThreshold) {
		var EM = Sonicle.calendar.data.EventMappings,
				soDate = Sonicle.Date,
				data = evt.data || evt,
				diff;
		
		diff = soDate.diffDays(data[EM.StartDate.name], data[EM.EndDate.name]);
		if(hoursThreshold) {
			return (diff > 0) && (soDate.diff(data[EM.StartDate.name], data[EM.EndDate.name], 'hours') >= hoursThreshold);
		} else {
			return (diff > 0);
		}
		
		//TODO: Prevent 00:00 end time from causing a span. This logic is OK, but
        //      other changes are still needed for it to work fully. Deferring for now.
		//        if (diff <= 1 && Extensible.Date.isMidnight(data[M.EndDate.name])) {
		//            return false;
		//        }
		
	},
	*/
	
	eventDurationInHours: function(evt) {
		var EU = Sonicle.calendar.util.EventUtils,
				EM = Sonicle.calendar.data.EventMappings,
				data = evt.data || evt;
		return EU.durationInHours(data[EM.StartDate.name], data[EM.EndDate.name]);
	},
	
	getDayEl: function (dt) {
		return Ext.get(this.getDayId(dt));
	},
	
	getDayId: function (dt) {
		if (Ext.isDate(dt)) {
			dt = Ext.Date.format(dt, 'Ymd');
		}
		return this.id + this.dayElIdDelimiter + dt;
	},
	
	/**
	 * Returns the start date of the view, as set by {@link #setStartDate}. Note that this may not 
	 * be the first date displayed in the rendered calendar -- to get the start and end dates displayed
	 * to the user use {@link #getViewBounds}.
	 * @return {Date} The start date
	 */
	getStartDate: function () {
		return this.startDate;
	},
	
	/**
	 * Sets the start date used to calculate the view boundaries to display. The displayed view will be the 
	 * earliest and latest dates that match the view requirements and contain the date passed to this function.
	 * @param {Date} dt The date used to calculate the new view boundaries
	 */
	setStartDate: function (start, reload) {
		var me = this,
				eDate = Ext.Date,
				cloneDt = eDate.clone,
				cloStart = eDate.clone(start),
				cloStartDate = (me.startDate) ? cloneDt(me.startDate) : null,
				cloViewStart = (me.viewStart) ? cloneDt(me.viewStart) : null,
				cloViewEnd = (me.viewEnd) ? cloneDt(me.viewEnd) : null;
		
		if(me.fireEvent('beforedatechange', me, cloStartDate, cloStart, cloViewStart, cloViewEnd) !== false) {
			me.startDate = eDate.clearTime(start);
			me.setViewBounds(start);
			
			if(me.ownerCalendarPanel && me.ownerCalendarPanel.startDate !== me.startDate) {
				// Sync the owning CalendarPanel's start date directly, not via CalendarPanel.setStartDate(),
				// since that would in turn call this method again.
				me.ownerCalendarPanel.startDate = me.startDate;
			}
			if(me.rendered) me.refresh(reload);
			
			me.fireEvent('datechange', me, cloneDt(me.startDate), cloneDt(me.viewStart), cloneDt(me.viewEnd));
		}
	},
	
	// private
	setViewBounds: function (startDate) {
		var me = this,
				start = startDate || me.startDate,
				offset = start.getDay() - me.startDay,
				soDate = Sonicle.Date;

		if (offset < 0) {
			// if the offset is negative then some days will be in the previous week so add a week to the offset
			offset += 7;
		}

		switch (this.weekCount) {
			case 0:
			case 1:
				me.viewStart = ((me.dayCount < 7) && !me.startDayIsStatic) ? start
						: soDate.add(start, {days: -offset, clearTime: true});
				me.viewEnd = soDate.add(me.viewStart, {days: me.dayCount || 7, seconds: -1});
				return;

			case -1:
				// auto by month
				start = Ext.Date.getFirstDateOfMonth(start);
				offset = start.getDay() - me.startDay;
				if (offset < 0) {
					// if the offset is negative then some days will be in the previous week so add a week to the offset
					offset += 7;
				}
				me.viewStart = soDate.add(start, {days: -offset, clearTime: true});

				// start from current month start, not view start:
				var end = soDate.add(start, {months: 1, seconds: -1});

				// fill out to the end of the week:
				offset = me.startDay;

				if (offset > end.getDay()) {
					// if the offset is larger than the end day index then the last row will be empty so skip it
					offset -= 7;
				}
				;

				me.viewEnd = soDate.add(end, {days: 6 - end.getDay() + offset});
				return;

			default:
				me.viewStart = soDate.add(start, {days: -offset, clearTime: true});
				me.viewEnd = soDate.add(me.viewStart, {days: me.weekCount * 7, seconds: -1});
		}
	},
	
	// private
	getViewBounds: function () {
		return {
			start: this.viewStart,
			end: this.viewEnd
		};
	},
	
	/* private
	 * Sort events for a single day for display in the calendar.  This sorts allday
	 * events first, then non-allday events are sorted either based on event start
	 * priority or span priority based on the value of {@link #spansHavePriority} 
	 * (defaults to event start priority).
	 * @param {MixedCollection} evts A {@link Ext.util.MixedCollection MixedCollection}  
	 * of {@link #Sonicle.calendar.EventRecord EventRecord} objects
	 */
	sortEventRecordsForDay: function (evts) {
		if (evts.length < 2) {
			return;
		}
		evts.sortBy(Ext.bind(function (evtA, evtB) {
			var a = evtA.data,
					b = evtB.data,
					M = Sonicle.calendar.data.EventMappings;

			// Always sort all day events before anything else
			if (a[M.IsAllDay.name]) {
				return -1;
			}
			else if (b[M.IsAllDay.name]) {
				return 1;
			}
			if (this.spansHavePriority) {
				// This logic always weights span events higher than non-span events
				// (at the possible expense of start time order). This seems to
				// be the approach used by Google calendar and can lead to a more
				// visually appealing layout in complex cases, but event order is
				// not guaranteed to be consistent.
				var diff = Sonicle.Date.diffDays;
				if (diff(a[M.StartDate.name], a[M.EndDate.name]) > 0) {
					if (diff(b[M.StartDate.name], b[M.EndDate.name]) > 0) {
						// Both events are multi-day
						if (a[M.StartDate.name].getTime() === b[M.StartDate.name].getTime()) {
							// If both events start at the same time, sort the one
							// that ends later (potentially longer span bar) first
							return b[M.EndDate.name].getTime() - a[M.EndDate.name].getTime();
						}
						return a[M.StartDate.name].getTime() - b[M.StartDate.name].getTime();
					}
					return -1;
				}
				else if (diff(b[M.StartDate.name], b[M.EndDate.name]) > 0) {
					return 1;
				}
				return a[M.StartDate.name].getTime() - b[M.StartDate.name].getTime();
			}
			else {
				// Doing this allows span and non-span events to intermingle but
				// remain sorted sequentially by start time. This seems more proper
				// but can make for a less visually-compact layout when there are
				// many such events mixed together closely on the calendar.
				return a[M.StartDate.name].getTime() - b[M.StartDate.name].getTime();
			}
		}, this));
	},
	
	/**
	 * Updates the view to contain the passed date
	 * @param {Date} dt The date to display
	 * @return {Date} The new view start date
	 */
	moveTo: function (dt, noRefresh) {
		if (Ext.isDate(dt)) {
			this.setStartDate(dt);
			if (noRefresh !== false)
				this.refresh();
			return this.startDate;
		}
		return dt;
	},
	
	/**
	 * Updates the view to the next consecutive date(s)
	 * @return {Date} The new view start date
	 */
	moveNext: function (noRefresh) {
		return this.moveTo(Sonicle.Date.add(this.viewEnd, {days: 1}));
	},
	
	/**
	 * Updates the view to the previous consecutive date(s)
	 * @return {Date} The new view start date
	 */
	movePrev: function (noRefresh) {
		var days = Sonicle.Date.diffDays(this.viewStart, this.viewEnd) + 1;
		return this.moveDays(-days, noRefresh);
	},
	
	/**
	 * Shifts the view by the passed number of months relative to the currently set date
	 * @param {Number} value The number of months (positive or negative) by which to shift the view
	 * @return {Date} The new view start date
	 */
	moveMonths: function (value, noRefresh) {
		return this.moveTo(Sonicle.Date.add(this.startDate, {months: value}), noRefresh);
	},
	
	/**
	 * Shifts the view by the passed number of weeks relative to the currently set date
	 * @param {Number} value The number of weeks (positive or negative) by which to shift the view
	 * @return {Date} The new view start date
	 */
	moveWeeks: function (value, noRefresh) {
		return this.moveTo(Sonicle.Date.add(this.startDate, {days: value * 7}), noRefresh);
	},
	
	/**
	 * Shifts the view by the passed number of days relative to the currently set date
	 * @param {Number} value The number of days (positive or negative) by which to shift the view
	 * @return {Date} The new view start date
	 */
	moveDays: function (value, noRefresh) {
		return this.moveTo(Sonicle.Date.add(this.startDate, {days: value}), noRefresh);
	},
	
	/**
	 * Updates the view to show today
	 * @return {Date} Today's date
	 */
	moveToday: function (noRefresh) {
		return this.moveTo(new Date(), noRefresh);
	},
	
	/**
	 * Sets the event store used by the calendar to display {@link Sonicle.calendar.EventRecord events}.
	 * @param {Ext.data.Store} store
	 */
	setStore: function (store, initial) {
		var me = this;
		if (!initial && me.store) {
			me.store.un('load', me.onStoreLoad, me);
			me.store.un("write", me.onStoreWrite, me);
			me.store.un("clear", me.refresh, me);
		}
		if (store) {
			store.on('load', me.onStoreLoad, me);
			store.on("write", me.onStoreWrite, me);
			store.on("clear", me.refresh, me);
		}
		me.store = store;
		if (store && store.getCount() > 0) {
			me.refresh();
		}
	},
	
	/**
	 * Returns an object containing the start and end dates to be passed as params in all calls
	 * to load the event store. The param names are customizable using {@link #dateParamStart}
	 * and {@link #dateParamEnd} and the date format used in requests is defined by {@link #dateParamFormat}.
	 * If you need to add additional parameters to be sent when loading the store see {@link #getStoreParams}.
	 * @return {Object} An object containing the start and end dates.
	 */
	getStoreParams: function() {
		var me = this, o = {};
		o[me.dateParamStart] = Ext.Date.format(this.viewStart, this.dateParamFormat);
		o[me.dateParamEnd] = Ext.Date.format(this.viewEnd, this.dateParamFormat);
		return o;
	},
	
	/**
	 * Reloads the view's underlying event store using the params returned from {@link #getStoreParams}.
	 * Reloading the store is typically managed automatically by the view itself, but the method is
	 * available in case a manual reload is ever needed.
	 * @param {Object} [opts] An object matching the format used by Store's {@link Ext.data.Store#load load} method
	 */
	reloadStore: function(opts) {
		opts = opts || {};
		var me = this,
				proxy = me.store.getProxy();
		proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), me.getStoreParams()));
		me.store.load(opts);
	},
	
	/**
	 * Refresh the current view, optionally reloading the event store also. While this is normally
	 * managed internally on any navigation and/or CRUD action, there are times when you might want
	 * to refresh the view manually (e.g., if you'd like to reload using different {@link #getStoreParams params}).
	 * @param {Boolean} reloadData True to reload the store data first, false to simply redraw the view using current data (defaults to false)
	 * @return {undefined}
	 */
	refresh: function(reloadData) {
		var me = this;
		if(!me.isActiveView()) return;
		
		if(reloadData === true) {
			me.reloadStore();
		} else {
			me.prepareData();
			me.renderTemplate();
			me.renderItems();
		}
	},
	
	getEventRecord: function (id) {
		var idx = this.store.find(Sonicle.calendar.data.EventMappings.Id.name, id);
		return this.store.getAt(idx);
	},
	
	getEventRecordFromEl: function (el) {
		return this.getEventRecord(this.getEventIdFromEl(el));
	},
	
	// private
	getParams: function () {
		return {
			viewStart: this.viewStart,
			viewEnd: this.viewEnd,
			startDate: this.startDate,
			dayCount: this.dayCount,
			weekCount: this.weekCount,
			title: this.getTitle()
		};
	},
	
	getTitle: function () {
		return Ext.Date.format(this.startDate, 'F Y');
	},
	
	startTitleEditing: function(id, title, el) {
		var me = this;
		
		if(me.titleEditor.editing) me.titleEditor.cancelEdit();
		me.titleEditor.itemId = id;
		me.titleEditor.startEdit(el, title);
		me.titleEditor.field.focus();
	},
	
	
	/*
	 * Shared click handling.  Each specific view also provides view-specific
	 * click handling that calls this first.  This method returns true if it
	 * can handle the click (and so the subclass should ignore it) else false.
	 */
	onClick: function (e, t) {
		/*
		if(e.ctrlKey && e.getTarget('.ext-evt-bd', 1) !== null) {
			// ignore clicks on the evt-bd
			return;
		}
		*/
		var me = this, 
				EM = Sonicle.calendar.data.EventMappings,
				el, tel, id, rec;
		/*
		el = e.getTarget(me.eventTitleSelector, 1);
		if(e.ctrlKey && el) {
			var evtEl = e.getTarget(me.eventSelector, 5);
			if(evtEl) {
				id = me.getEventIdFromEl(evtEl);
				rec = me.getEventRecord(id);
				me.startTitleEditing(id, rec.data[Sonicle.calendar.data.EventMappings.Title.name], el);
				return true;
			}
		}
		*/
		
		el = e.getTarget(me.eventSelector, 5);
		if(el) {
			id = me.getEventIdFromEl(el);
			rec = me.getEventRecord(id);
			
			// Intercepts CTRL+click on title for displaying a speedy editor
			if(e.ctrlKey && !rec.data[EM.IsReadOnly.name] && !rec.data[EM.IsRecurring.name]) {
				tel = e.getTarget(me.eventTitleSelector, 1);
				if(tel) {
					me.startTitleEditing(id, rec.data[EM.Title.name], tel);
					return true;
				}
			}
			
			// We handle eventclick/eventdblclick in same way...
			me.fireEvent('event'+e.type, me, me.getEventRecord(id), el, e);
			return true;
		}
	},
	
	onContextMenu: function(e, t) {
		var el = e.getTarget(this.eventSelector, 5, true);
		if(el) {
			var id = this.getEventIdFromEl(el);
			this.fireEvent('eventcontextmenu', this, this.getEventRecord(id), el, e);
		}
	},
	
	// private
	onMouseOver: function (e, t) {
		if (this.trackMouseOver !== false && (this.dragZone === undefined || !this.dragZone.dragging)) {
			if (!this.handleEventMouseEvent(e, t, 'over')) {
				this.handleDayMouseEvent(e, t, 'over');
			}
		}
	},
	// private
	onMouseOut: function (e, t) {
		if (this.trackMouseOver !== false && (this.dragZone === undefined || !this.dragZone.dragging)) {
			if (!this.handleEventMouseEvent(e, t, 'out')) {
				this.handleDayMouseEvent(e, t, 'out');
			}
		}
	},
	
	// private
	handleEventMouseEvent: function (e, t, type) {
		var me = this,
				el = e.getTarget(me.eventSelector, me.eventSelectorDepth, true),
				rel,
				els,
				evtId;
		
		if (el) {
			rel = Ext.get(e.getRelatedTarget());
			if (el === rel || el.contains(rel)) {
				return true;
			}

			evtId = me.getEventIdFromEl(el);

			if (me.eventOverClass) {
				els = me.getEventEls(evtId);
				els[type === 'over' ? 'addCls' : 'removeCls'](me.eventOverClass);
			}
			me.fireEvent('event' + type, me, me.getEventRecord(evtId), el);
			return true;
		}
		return false;
	},
	
	// private
	getDateFromId: function (id, delim) {
		var parts = id.split(delim);
		return parts[parts.length - 1];
	},
	
	// private
	handleDayMouseEvent: function (e, t, type) {
		t = e.getTarget('td', 3);
		if (t) {
			if (t.id && t.id.indexOf(this.dayElIdDelimiter) > -1) {
				var dt = this.getDateFromId(t.id, this.dayElIdDelimiter),
						rel = Ext.get(e.getRelatedTarget()),
						relTD,
						relDate;

				if (rel) {
					relTD = rel.is('td') ? rel : rel.up('td', 3);
					relDate = relTD && relTD.id ? this.getDateFromId(relTD.id, this.dayElIdDelimiter) : '';
				}
				if (!rel || (dt !== relDate)) {
					var el = this.getDayEl(dt);
					if (el && this.dayOverClass !== '') {
						el[(type === 'over') ? 'addCls' : 'removeCls'](this.dayOverClass);
					}
					this.fireEvent('day' + type, this, Ext.Date.parseDate(dt, "Ymd"), el);
				}
			}
		}
	},
	
	// private
	renderItems: function () {
		throw 'This method must be implemented by a subclass';
	},
	
	/**
	 * Returns true only if this is the active view inside of an owning
	 * {@link Sonicle.calendar.CalendarPanel CalendarPanel}. If it is not active, or
	 * ot hosted inside a CalendarPanel, returns false.
	 * @return {Boolean} True if this is the active CalendarPanel view, else false
	 */
	isActiveView: function() {
		var calendarPanel = this.ownerCalendarPanel;
		return (calendarPanel && calendarPanel.getActiveView().id === this.id);
	},
	
	// private
	destroy: function () {
		this.callParent(arguments);

		if (this.el) {
			this.el.un('contextmenu', this.onContextMenu, this);
		}
		Ext.destroy(
			this.editWin,
			this.eventMenu,
			this.dragZone,
			this.dropZone
		);
	},
	
	/**
	 * Create a copy of the event with a new start date, preserving the original event duration.
	 * @param {Object} rec The original event {@link Sonicle.calendar.data.EventModel record}
	 * @param {Object} newStartDate The new start date. The end date of the created event copy will be adjusted
	 * automatically to preserve the original duration.
	 */
	copyEvent: function (rec, newStartDate) {
		this.shiftEvent(rec, newStartDate, 'copy');
	},
	
	/**
	 * Move the event to a new start date, preserving the original event duration.
	 * @param {Object} rec The event {@link Sonicle.calendar.data.EventModel record}
	 * @param {Object} newStartDate The new start date
	 */
	moveEvent: function (rec, newStartDate) {
		this.shiftEvent(rec, newStartDate, 'move');
	},
	
	// private
	shiftEvent: function (rec, newStartDate, moveOrCopy) {
		var me = this,
				newRec;
		
		if (moveOrCopy === 'move') {
			if (Sonicle.Date.compare(rec.data[Sonicle.calendar.data.EventMappings.StartDate.name], newStartDate) === 0) {
				// No changes, so we aren't actually moving. Copying to the same date is OK.
				return;
			}
			newRec = rec;
		} else {
			newRec = rec.copy(null);
		}

		if (me.fireEvent('beforeevent' + moveOrCopy, me, newRec, Ext.Date.clone(newStartDate)) !== false) {
			me.doShiftEvent(newRec, newStartDate, moveOrCopy);
		}
	},
	
	// private
	doShiftEvent: function (rec, newStartDate, moveOrCopy) {
		var me = this,
				EM = Sonicle.calendar.data.EventMappings,
				start = rec.data[EM.StartDate.name],
				end = rec.data[EM.EndDate.name],
				diff = newStartDate.getTime() - start.getTime();
		
		rec.beginEdit();
		rec.set(EM.StartDate.name, newStartDate);
		rec.set(EM.EndDate.name, Sonicle.Date.add(end, {millis: diff}));
		rec.endEdit();
		if(rec.phantom) me.store.add(rec);

		me.fireEvent('event' + moveOrCopy, me, rec);
	},
	
	doEventTitleUpdate: function(rec, newTitle) {
		rec.beginEdit();
		rec.set(Sonicle.calendar.data.EventMappings.Title.name, newTitle);
		rec.endEdit();
		this.fireEvent('eventTitleUpdate', this, rec);
	}
});
