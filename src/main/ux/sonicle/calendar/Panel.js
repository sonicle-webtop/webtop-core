/**
 * @class Sonicle.calendar.CalendarPanel
 * @extends Ext.Panel
 * <p>This is the default container for Ext calendar views. It supports day, week and month views as well
 * as a built-in event edit form. The only requirement for displaying a calendar is passing in a valid
 * {@link #calendarStore} config containing records of type {@link Sonicle.calendar.EventRecord EventRecord}. In order
 * to make the calendar interactive (enable editing, drag/drop, etc.) you can handle any of the various
 * events fired by the underlying views and exposed through the CalendarPanel.</p>
 * {@link #layoutConfig} option if needed.</p>
 * @constructor
 * @param {Object} config The config object
 * @xtype calendarpanel
 */
Ext.define('Sonicle.calendar.Panel', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.calendarpanel',
	requires: [
		'Ext.layout.container.Card',
		'Sonicle.calendar.view.Day',
		'Sonicle.calendar.view.Week5',
		'Sonicle.calendar.view.Week',
		'Sonicle.calendar.view.WeekAgenda',
		'Sonicle.calendar.view.Month',
		'Sonicle.calendar.data.EventMappings'
	],
	mixins: [
		'Ext.util.StoreHolder'
	],
	
	//layout: 'card',
	
	layout: {
		type: 'card',
		deferredRender: true
	},
	
	/**
	 * @cfg {Boolean} showDayView
	 * True to include the day view (and toolbar button), false to hide them (defaults to true).
	 */
	showDayView: true,
	
	/**
	 * @cfg {Boolean} showWeek5View
	 * True to include the week-5days view (and toolbar button), false to hide them (defaults to true).
	 */
	showWeek5View: true,
	
	/**
	 * @cfg {Boolean} showWeekView
	 * True to include the week view (and toolbar button), false to hide them (defaults to true).
	 */
	showWeekView: true,
	
	/**
	 * @cfg {Boolean} showWeekAgendaView
	 * True to include the week view (agenda style)(and toolbar button), false to hide them (defaults to true).
	 */
	showWeekAgendaView: true,
	
	/**
	 * @cfg {Boolean} showMonthView
	 * True to include the month view (and toolbar button), false to hide them (defaults to true).
	 * If the day and week views are both hidden, the month view will show by default even if
	 * this config is false.
	 */
	showMonthView: true,
	
	/**
	 * @cfg {Boolean} showNavBar
	 * True to display the calendar navigation toolbar, false to hide it (defaults to false). Note that
	 * if you hide the default navigation toolbar you'll have to provide an alternate means of navigating the calendar.
	 */
	showNavBar: false,
	
	/**
	 * @cfg {String} todayText
	 * Alternate text to use for the 'Today' nav bar button.
	 */
	todayText: 'Today',
	
	/**
	 * @cfg {Boolean} showTodayText
	 * True to show the value of {@link #todayText} instead of today's date in the calendar's current day box,
	 * false to display the day number(defaults to true).
	 */
	showTodayText: true,
	
	/**
	 * @cfg {Boolean} showTime
	 * True to display the current time next to the date in the calendar's current day box, false to not show it 
	 * (defaults to true).
	 */
	showTime: true,
	
	/**
	 * @cfg {String} dayText
	 * Alternate text to use for the 'Day' nav bar button.
	 */
	dayText: 'Day',
	
	/**
	 * @cfg {String} week5Text
	 * Alternate text to use for the 'Week5' nav bar button.
	 */
	week5Text: 'Week (5days)',
	
	/**
	 * @cfg {String} weekText
	 * Alternate text to use for the 'Week' nav bar button.
	 */
	weekText: 'Week',
	
	/**
	 * @cfg {String} weekAgendaText
	 * Alternate text to use for the 'WeekAgenda' nav bar button.
	 */
	weekAgendaText: 'Week (agenda)',
	
	/**
	 * @cfg {String} monthText
	 * Alternate text to use for the 'Month' nav bar button.
	 */
	monthText: 'Month',
	
	/**
	 * @cfg {Number} startDay
	 * The 0-based index for the day on which the calendar week begins (0=Sunday, which is the default)
	 */
	startDay: 0,
	
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
	 * @cfg {String} activeView [activeView=d]
	 */
	activeView: 'd',
	
	/**
	 * @cfg {auto|white|black} eventTextColor [eventTextColor=auto]
	 */
	eventTextColor: 'auto',
	
	/**
	 * @cfg {Number} colorLuminance [colorLuminance=195]
	 * Integer number (from 0 to 255) expressing color lumimance boundary value, 
	 * used to set the appropriate text color (black or white) depending on
	 * event's background color. This config is only useful if 
	 * {@link #eventTextColor} is equal to 'auto'.
	 */
	colorLuminance: 195,
	
	/**
     * @cfg {Object} viewCfg
     * A config object that will be applied to all {@link Sonicle.calendar.view.AbstractCalendar views}
     * managed by this CalendarPanel. Any options on this object that do not apply to any particular view
     * will simply be ignored.
     */
	
    /**
     * @cfg {Object} dayViewCfg
     * A config object that will be applied only to the {@link Sonicle.calendar.view.Day DayView}
     * managed by this CalendarPanel.
     */
	
    /**
     * @cfg {Object} weekViewCfg
     * A config object that will be applied only to {@link Sonicle.calendar.view.Week WeekView} and
     * {@link Sonicle.calendar.view.Week Week5View} managed by this CalendarPanel.
     */
	
    /**
     * @cfg {Object} monthViewCfg
     * A config object that will be applied only to the {@link Sonicle.calendar.view.Month MonthView}
     * managed by this CalendarPanel.
     */
	
	/**
	 * @event eventadd
	 * Fires after a new event is added to the underlying store
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The new {@link Sonicle.calendar.EventRecord record} that was added
	 */

	/**
	 * @event eventupdate
	 * Fires after an existing event is updated
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The new {@link Sonicle.calendar.EventRecord record} that was updated
	 */

	/**
	 * @event eventdelete
	 * Fires after an event is removed from the underlying store
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The new {@link Sonicle.calendar.EventRecord record} that was removed
	 */

	/**
	 * @event eventcancel
	 * Fires after an event add/edit operation is canceled by the user and no store update took place
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The new {@link Sonicle.calendar.EventRecord record} that was canceled
	 */

	/**
	 * @event viewchange
	 * Fires after a different calendar view is activated (but not when the event edit form is activated)
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Ext.Sonicle.calendar.view.AbstractCalendar} view The view being activated (any valid {@link Sonicle.calendar.view.AbstractCalendar AbstractCalendar} subclass)
	 * @param {Object} info Extra information about the newly activated view. This is a plain object 
	 * with following properties:<div class="mdetail-params"><ul>
	 * <li><b><code>activeDate</code></b> : <div class="sub-desc">The currently-selected date</div></li>
	 * <li><b><code>viewStart</code></b> : <div class="sub-desc">The first date in the new view range</div></li>
	 * <li><b><code>viewEnd</code></b> : <div class="sub-desc">The last date in the new view range</div></li>
	 * </ul></div>
	 */


	//
	// NOTE: CalendarPanel also relays the following events from contained views as if they originated from this:
	//
	/**
	 * @event eventsrendered
	 * Fires after events are finished rendering in the view
	 * @param {Sonicle.calendar.CalendarPanel} this 
	 */
	/**
	 * @event eventclick
	 * Fires after the user clicks on an event element
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that was clicked on
	 * @param {HTMLNode} el The DOM node that was clicked on
	 */
	/**
	 * @event eventover
	 * Fires anytime the mouse is over an event element
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that the cursor is over
	 * @param {HTMLNode} el The DOM node that is being moused over
	 */
	/**
	 * @event eventout
	 * Fires anytime the mouse exits an event element
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that the cursor exited
	 * @param {HTMLNode} el The DOM node that was exited
	 */
	/**
	 * @event datechange
	 * Fires after the start date of the view changes
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Date} startDate The start date of the view (as explained in {@link #getStartDate}
	 * @param {Date} viewStart The first displayed date in the view
	 * @param {Date} viewEnd The last displayed date in the view
	 */
	/**
	 * @event rangeselect
	 * Fires after the user drags on the calendar to select a range of dates/times in which to create an event
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Object} dates An object containing the start (StartDate property) and end (EndDate property) dates selected
	 * @param {Function} callback A callback function that MUST be called after the event handling is complete so that
	 * the view is properly cleaned up (shim elements are persisted in the view while the user is prompted to handle the
	 * range selection). The callback is already created in the proper scope, so it simply needs to be executed as a standard
	 * function call (e.g., callback()).
	 */
	/**
	 * @event eventmove
	 * Fires after an event element is dragged by the user and dropped in a new position
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that was moved with
	 * updated start and end dates
	 */
	/**
	 * @event initdrag
	 * Fires when a drag operation is initiated in the view
	 * @param {Sonicle.calendar.CalendarPanel} this
	 */
	/**
	 * @event eventresize
	 * Fires after the user drags the resize handle of an event to resize it
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Sonicle.calendar.EventRecord} rec The {@link Sonicle.calendar.EventRecord record} for the event that was resized
	 * containing the updated start and end dates
	 */
	/**
	 * @event dayclick
	 * Fires after the user clicks within a day/week view container and not on an event element
	 * @param {Sonicle.calendar.CalendarPanel} this
	 * @param {Date} dt The date/time that was clicked on
	 * @param {Boolean} allday True if the day clicked on represents an all-day box, else false.
	 * @param {Ext.core.Element} el The Element that was clicked on
	 */
	
	// private property
	viewMap: null,
	startDate: new Date(),
	
	constructor: function(cfg) {
		this.viewMap = {};
		this.callParent([cfg]);
	},
	
	initComponent: function () {
		var me = this, wiewIdx = -1;
		me.bindStore(me.store || 'ext-empty-store', true, true);
		
		me.tbar = {
			cls: 'ext-cal-toolbar',
			border: true,
			items: ['->', {
					id: me.id + '-tb-prev',
					handler: me.onPrevClick,
					scope: me,
					iconCls: 'x-tbar-page-prev'
				}]
		};
		
		// Keep tb buttons in same order of views
		if (me.showDayView) {
			wiewIdx++;
			me.viewMap['d'] = wiewIdx;
			me.tbar.items.push({
				id: me.id + '-tb-day',
				text: me.dayText,
				handler: me.onDayClick,
				scope: me,
				toggleGroup: 'tb-views'
			});
		}
		if (me.showWeek5View) {
			wiewIdx++;
			me.viewMap['w5'] = wiewIdx;
			me.tbar.items.push({
				id: me.id + '-tb-week5',
				text: me.week5Text,
				handler: me.onWeek5Click,
				scope: me,
				toggleGroup: 'tb-views'
			});
		}
		if (me.showWeekView) {
			wiewIdx++;
			me.viewMap['w'] = wiewIdx;
			me.tbar.items.push({
				id: me.id + '-tb-week',
				text: me.weekText,
				handler: me.onWeekClick,
				scope: me,
				toggleGroup: 'tb-views'
			});
		}
		if (me.showWeekAgendaView) {
			wiewIdx++;
			me.viewMap['wa'] = wiewIdx;
			me.tbar.items.push({
				id: me.id + '-tb-weekag',
				text: me.weekAgendaText,
				handler: me.onWeekAgendaClick,
				scope: me,
				toggleGroup: 'tb-views'
			});
		}
		if (me.showMonthView || (me.wiewIdx === -1)) {
			wiewIdx++;
			me.viewMap['m'] = wiewIdx;
			me.tbar.items.push({
				id: me.id + '-tb-month',
				text: me.monthText,
				handler: me.onMonthClick,
				scope: me,
				toggleGroup: 'tb-views'
			});
			me.showMonthView = true;
		}
		me.tbar.items.push({
			id: me.id + '-tb-next',
			handler: me.onNextClick,
			scope: me,
			iconCls: 'x-tbar-page-next'
		});
		me.tbar.items.push('->');
		
		var idx = (Ext.isString(me.activeView)) ? me.viewMap[me.activeView] : wiewIdx;
		me.activeItem =  (!Ext.isDefined(me.activeItem)) ? idx: (me.activeItem > idx ? idx: me.activeItem);
		delete me.activeView;

		if (me.showNavBar === false) {
			delete me.tbar;
			me.addCls('x-calendar-nonav');
		}
		
		me.callParent();
		
		var sharedCfg = {
			ownerCalendarPanel: me,
			startDay: me.startDay,
			use24HourTime: me.use24HourTime,
			timezone: me.timezone,
			eventTextColor: me.eventTextColor,
			colorLuminance: me.colorLuminance,
			showToday: me.showToday,
			showTodayText: me.showTodayText,
			showTime: me.showTime,
			store: me.store
		};
		
		// do not allow override
		if (me.showDayView) {
			var dv = Ext.apply({
				xtype: 'dayview',
				title: me.dayText
			}, sharedCfg);
			
			dv = Ext.apply(Ext.apply(dv, me.viewCfg), me.dayViewCfg);
			dv.id = me.id + '-day';
			me.initEventRelay(dv);
			me.add(dv);
		}
		if (me.showWeek5View) {
			var wv = Ext.applyIf({
				xtype: 'week5view',
				title: me.week5Text
			}, sharedCfg);
			
			wv = Ext.apply(Ext.apply(wv, me.viewCfg), me.weekViewCfg);
			wv.id = me.id + '-week5';
			me.initEventRelay(wv);
			me.add(wv);
		}
		if (me.showWeekView) {
			var wv = Ext.applyIf({
				xtype: 'weekview',
				title: me.weekText
			}, sharedCfg);
			
			wv = Ext.apply(Ext.apply(wv, me.viewCfg), me.weekViewCfg);
			wv.id = me.id + '-week';
			me.initEventRelay(wv);
			me.add(wv);
		}
		if (me.showWeekAgendaView) {
			var wv = Ext.applyIf({
				xtype: 'weekagendaview',
				title: me.weekAgendaText
			}, sharedCfg);
			
			wv = Ext.apply(Ext.apply(wv, me.viewCfg), me.weekAgendaViewCfg);
			wv.id = me.id + '-weekag';
			me.initEventRelay(wv);
			me.add(wv);
		}
		if (me.showMonthView) {
			var mv = Ext.applyIf({
				xtype: 'monthview',
				title: me.monthText,
				listeners: {
					'weekclick': {
						fn: function (s, dt) {
							me.showWeek(dt);
						},
						scope: me
					}
				}
			}, sharedCfg);
			
			mv = Ext.apply(Ext.apply(mv, me.viewCfg), me.monthViewCfg);
			mv.id = me.id + '-month';
			me.initEventRelay(mv);
			me.add(mv);
		}
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
	 * See {@link Ext.util.StoreHolder StoreHolder}.
	 */
	getStoreListeners: function(store, o) {
		var me = this;
		return {
			//datachanged: me.onStoreDataChanged,
			//beforeload: me.onStoreBeforeLoad,
			//load: me.onStoreLoad
		};
	},
	
	// private
	initEventRelay: function (cfg) {
		cfg.listeners = cfg.listeners || {};
		cfg.listeners.afterrender = {
			fn: function (c) {
				// relay the view events so that app code only has to handle them in one place
				this.relayEvents(c, ['eventsrendered', 'eventclick', 'eventdblclick', 'eventover', 'eventout', 
					'dayclick', 'daydblclick', 'eventcontextmenu', 
					'eventmove', 'datechange', 'rangeselect', 'eventdelete', 'eventresize', 'initdrag']);
			},
			scope: this,
			single: true
		};
	},
	
	// private
	afterRender: function () {
		this.callParent(arguments);

		this.body.addCls('x-cal-body');

		Ext.defer(function () {
			this.updateNavState();
			this.fireViewChange();
		}, 10, this);
	},
	
	// private
	onLayout: function () {
		this.callParent();
		if (!this.navInitComplete) {
			this.updateNavState();
			this.navInitComplete = true;
		}
	},
	
	/**
	 * Set the active view, optionally specifying a new start date.
	 * @param {String/Number} id The id of the view to activate (or the 0-based index of the view within 
	 * the CalendarPanel's internal card layout).
	 * @param {Date} startDate (optional) The new view start date (defaults to the current start date)
	 */
	setActiveView: function(id, startDate) {
		var me = this,
				l = me.layout,
				idx = me.viewMap[id];
		
		if(!Ext.isDefined(idx)) return;
		
		// Make sure we're actually changing views
		if(me.getComponent(idx).id !== l.getActiveItem().id) {
			if(startDate) me.startDate = startDate;
			
			// Activate the new view and refresh the layout
			Ext.suspendLayouts();
			l.setActiveItem(idx);
			me.activeView = l.getActiveItem();
			me.activeView.setStartDate(me.startDate, true);
			Ext.resumeLayouts(true);
			
			me.fireViewChange();
		} else {
			if(startDate) me.setStartDate(startDate);
		}
	},
	
	// private
	fireViewChange: function () {
		if (this.layout && this.layout.getActiveItem) {
			var view = this.layout.getActiveItem();
			if (view && view.getViewBounds) {
				var vb = view.getViewBounds();
				var info = {
					activeDate: view.getStartDate(),
					viewStart: vb.start,
					viewEnd: vb.end
				};
			}
			this.fireEvent('viewchange', this, view, info);
		}
	},
	
	// private
	updateNavState: function () {
		if (this.showNavBar !== false) {
			var item = this.layout.activeItem,
					suffix = item.id.split(this.id + '-')[1],
					btn = Ext.getCmp(this.id + '-tb-' + suffix);

			if (btn) {
				btn.toggle(true);
			}
		}
	},
	
	/**
	 * Sets the start date for the currently-active calendar view.
	 * @param {Date} dt The new start date
	 * @return {Extensible.calendar.CalendarPanel} this
	 */
	setStartDate: function (dt) {
		this.startDate = dt;
		console.log('startDate='+dt);
		Ext.suspendLayouts();
		this.layout.activeItem.setStartDate(dt, true);
		Ext.resumeLayouts(true);
		this.fireViewChange();
	},
	
	// private
	showWeek: function (dt) {
		this.setActiveView('w');
		this.setStartDate(dt);
	},
	
	// private
	onPrevClick: function () {
		this.startDate = this.layout.activeItem.movePrev();
		this.updateNavState();
		this.fireViewChange();
	},
	
	// private
	onNextClick: function () {
		this.startDate = this.layout.activeItem.moveNext();
		this.updateNavState();
		this.fireViewChange();
	},
	
	// private
	onDayClick: function () {
		this.setActiveView('d');
	},
	
	onWeek5Click: function () {
		this.setActiveView('w5');
	},
	
	// private
	onWeekClick: function () {
		this.setActiveView('w');
	},
	
	// private
	onWeekAgendaClick: function () {
		this.setActiveView('wa');
	},
	
	// private
	onMonthClick: function () {
		this.setActiveView('m');
	},
	
	/**
	 * Return the calendar view that is currently active, which will be a subclass of
	 * {@link Sonicle.calendar.view.AbstractCalendar AbstractCalendar}.
	 * @return {Sonicle.calendar.view.AbstractCalendar} The active view
	 */
	getActiveView: function () {
		return this.layout.activeItem;
	},
	
	reload: function() {
		var av = this.getActiveView();
		if(av) av.refresh(true);
	}
});
