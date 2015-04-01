/**
 * @class Sonicle.calendar.view.Day
 * @extends Ext.container.Container
 * <p>Unlike other calendar views, is not actually a subclass of {@link Sonicle.calendar.view.AbstractCalendar AbstractCalendar}.
 * Instead it is a {@link Ext.container.Container Container} subclass that internally creates and manages the layouts of
 * a {@link Sonicle.calendar.DayHeaderView DayHeaderView} and a {@link Sonicle.calendar.DayBodyView DayBodyView}. As such
 * DayView accepts any config values that are valid for DayHeaderView and DayBodyView and passes those through
 * to the contained views. It also supports the interface required of any calendar view and in turn calls methods
 * on the contained views as necessary.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.Day', {
    extend: 'Ext.container.Container',
    alias: 'widget.dayview',
    
    requires: [
        'Sonicle.calendar.view.AbstractCalendar',
        'Sonicle.calendar.view.DayHeader',
        'Sonicle.calendar.view.DayBody'
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
	 * @cfg {Integer} viewStartHour
	 * The hour of the day at which to begin the scrolling body area's times (defaults to 0, which equals early 12am / 00:00).
	 * Valid values are integers from 0 to 24, but should be less than the value of {@link viewEndHour}.
	 */
	viewStartHour: 0,
	
	/**
	 * @cfg {Integer} viewEndHour
	 * The hour of the day at which to end the scrolling body area's times (defaults to 24, which equals late 12am / 00:00).
	 * Valid values are integers from 0 to 24, but should be greater than the value of {@link viewStartHour}.
	 */
	viewEndHour: 24,
	
	/**
	 * @cfg {Integer} scrollStartHour
	 * The default hour of the day at which to set the body scroll position on view load (defaults to 7, which equals 7am / 07:00).
	 * Note that if the body is not sufficiently overflowed to allow this positioning this setting will have no effect.
	 * This setting should be equal to or greater than {@link viewStartHour}.
	 */
	scrollStartHour: 7,
	
	/**
	 * @cfg {Boolean} highlightBusinessHours
	 * True to highlight business hours changing their background (the default), false otherwise.
	 */
	highlightBusinessHours: true,
	
	/**
	 * @cfg {Integer} businessHoursStart
	 * The hour of the day at which to begin business hours (defaults to 9, which equals early 9am / 09:00).
	 * Valid values are integers from 0 to 24, but should be less than the value of {@link businessHoursEnd}.
	 */
	businessHoursStart: 9,
	
	/**
	 * @cfg {Integer} businessHoursEnd
	 * The hour of the day at which to end business hours (defaults to 17, which equals late 5pm / 17:00).
	 * Valid values are integers from 0 to 24, but should be greater than the value of {@link businessHoursStart}.
	 */
	businessHoursEnd: 17,
	
	/**
	 * @cfg {Integer} minEventDisplayMinutes
	 * This is the minimum **display** height, in minutes, for events shown in the view (defaults to 30). This setting
	 * ensures that events with short duration are still readable (e.g., by default any event where the start and end
	 * times were the same would have 0 height). It also applies when calculating whether multiple events should be
	 * displayed as overlapping. In datetime terms, an event that starts and ends at 9:00 and another event that starts
	 * and ends at 9:05 do not overlap, but visually the second event would obscure the first in the view. This setting
	 * provides a way to ensure that such events will still be calculated as overlapping and displayed correctly.
	 */
	minEventDisplayMinutes: 30,
	
	/**
	 * @cfg {Integer} hourHeight
	 * The height, in pixels, of each hour block displayed in the scrolling body area of the view (defaults to 42).
	 * 
	 * **Important note:** 
	 * While this config can be set to any reasonable integer value, note that it is also used to calculate the ratio used 
	 * when assigning event heights. By default, an hour is 60 minutes and 42 pixels high, so the pixel-to-minute ratio is 
	 * 42 / 60, or 0.7. This same ratio is then used when rendering events. When rendering a 30 minute event, the rendered 
	 * height would be 30 minutes * 0.7 = 21 pixels (as expected).
	 * 
	 * This is important to understand when changing this value because some browsers may handle pixel rounding in different 
	 * ways which could lead to inconsistent visual results in some cases. If you have any problems with pixel precision in 
	 * how events are laid out, you might try to stick with hourHeight values that will generate discreet ratios. This is 
	 * easily done by simply multiplying 60 minutes by different discreet ratios (.6, .8, 1.1, etc.) to get the corresponding 
	 * hourHeight pixel values (36, 48, 66, etc.) that will map back to those ratios. By contrast, if you chose an hourHeight 
	 * of 50 for example, the resulting height ratio would be 50 / 60 = .833333... This will work just fine, just be aware 
	 * that browsers may sometimes round the resulting height values inconsistently.
	 */
	hourHeight: 42,
	
	/**
	 * @cfg {Number} minBodyHeight
	 * The minimum height for the scrollable body view (defaults to 150 pixels). By default the body is auto
	 * height and simply fills the available area left by the overall layout. However, if the browser window
	 * is too short and/or the header area contains a lot of events on a given day, the body area could
	 * become too small to be usable. Because of that, if the body falls below this minimum height, the
	 * layout will automatically adjust itself by fixing the body height to this minimum height and making the
	 * overall Day view container vertically scrollable.
	 */
	minBodyHeight: 150,
	
	/**
	 * @cfg {Boolean} showHourSeparator
	 * True to display a dotted line that separates each hour block in the scrolling body area at the half-hour mark
	 * (the default), false to hide it.
	 */
	showHourSeparator: true,
    
    /**
     * @cfg {Boolean} showTime
     * True to display the current time in today's box in the calendar, false to not display it (defautls to true)
     */
    showTime: true,
	
    /**
     * @cfg {Boolean} showTodayText
     * True to display the {@link #todayText} string in today's box in the calendar, false to not display it (defautls to true)
     */
    showTodayText: true,
	
    /**
     * @cfg {String} todayText
     * The text to display in the current day's box in the calendar when {@link #showTodayText} is true (defaults to 'Today')
     */
    todayText: 'Today',
	
	/**
	 * @cfg {Boolean} readOnly
	 * True to prevent clicks on events or the view from providing CRUD capabilities, false to enable CRUD (the default).
	 */
	readOnly: false,
	
	/**
	 * @cfg {Boolean} enableEventResize
	 * True to allow events in the view's scrolling body area to be updated by a resize handle at the
     * bottom of the event, false to disallow it (defaults to true). If {@link #readOnly} is true event
     * resizing will be disabled automatically.
	 */
	enableEventResize: true,
	
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
	 * @cfg {Integer} ddIncrement
	 * The number of minutes between each step during various drag/drop operations in the view (defaults to 30).
	 * This controls the number of times the dragged object will "snap" to the view during a drag operation, and does
	 * not have to match with the time boundaries displayed in the view. E.g., the view could be displayed in 30 minute
	 * increments (the default) but you could configure ddIncrement to 10, which would snap a dragged object to the
	 * view at 10 minute increments.
	 * 
	 * This config currently applies while dragging to move an event, resizing an event by its handle or dragging
	 * on the view to create a new event.
	 */
	ddIncrement: 30,
	
	/**
	 * @private
	 */
	isDayView: true,
	
	constructor: function(cfg) {
		if(cfg.dayCount) cfg.dayCount = (cfg.dayCount > 7) ? 7 : cfg.dayCount;
		this.callParent([cfg]);
	},
    
    // private
    initComponent : function(){
		var me = this;
        // rendering more than 7 days per view is not supported
        me.dayCount = me.dayCount > 7 ? 7 : me.dayCount;
        
        var cfg = Ext.apply({}, me.initialConfig);
		cfg.timezoneIconCls = me.timezoneIconCls;
		cfg.privateIconCls = me.privateIconCls;
		cfg.reminderIconCls = me.reminderIconCls;
		cfg.recurrenceIconCls = me.recurrenceIconCls;
		cfg.recurrenceBrokenIconCls = me.recurrenceBrokenIconCls;
		cfg.use24HourTime = me.use24HourTime;
        cfg.showTime = me.showTime;
        cfg.showTodatText = me.showTodayText;
        cfg.todayText = me.todayText;
        cfg.dayCount = me.dayCount;
        cfg.weekCount = 1;
		cfg.readOnly = me.readOnly;
		cfg.ddIncrement = me.ddIncrement;
		cfg.minEventDisplayMinutes = me.minEventDisplayMinutes;
		
        var header = Ext.applyIf({
            xtype: 'dayheaderview',
            id: me.id+'-hd',
			ownerCalendarPanel: me.ownerCalendarPanel
        }, cfg);
        
        var body = Ext.applyIf({
            xtype: 'daybodyview',
            id: me.id+'-bd',
			ownerCalendarPanel: me.ownerCalendarPanel,
			enableEventResize: me.enableEventResize,
			showHourSeparator: me.showHourSeparator,
			viewStartHour: me.viewStartHour,
			viewEndHour: me.viewEndHour,
			scrollStartHour: me.scrollStartHour,
			hourHeight: me.hourHeight,
			highlightBusinessHours: me.highlightBusinessHours,
			businessHoursStart: me.businessHoursStart,
			businessHoursEnd: me.businessHoursEnd
        }, cfg);
        
        me.items = [header, body];
        me.addCls('ext-cal-dayview ext-cal-ct');
        
        me.callParent(arguments);
    },
    
    // private
    afterRender : function(){
        this.callParent(arguments);
        
        this.header = Ext.getCmp(this.id+'-hd');
        this.body = Ext.getCmp(this.id+'-bd');
        this.body.on('eventsrendered', this.forceSize, this);
    },
    
    // private
    refresh : function(){
        this.header.refresh();
        this.body.refresh();
    },
    
    // private
    forceSize: function() {
		var me = this;
		
        // The defer call is mainly for good ol' IE, but it doesn't hurt in
        // general to make sure that the window resize is good and done first
        // so that we can properly calculate sizes.
        Ext.defer(function() {
            var ct = me.el.up('.x-panel-body'),
					hd = me.el.down('.ext-cal-day-header'),
					bH = ct ? ct.getHeight() - hd.getHeight() : false;
            
			if(bH) {
				if(bH < me.minBodyHeight) {
					bH = me.minBodyHeight;
					me.addCls('ext-cal-overflow-y');
				} else {
					me.removeCls('ext-cal-overflow-y');
				}
				//this.el.down('.ext-cal-body-ct').setHeight(h);
				me.el.down('.ext-cal-body-ct').setHeight(bH - 1);
			}
        }, Ext.isIE ? 1 : 0, this);
    },
    
    // private
    onResize : function() {
        this.callParent(arguments);
        this.forceSize();
    },
    
    // private
    getViewBounds : function(){
        return this.header.getViewBounds();
    },
    
    /**
     * Returns the start date of the view, as set by {@link #setStartDate}. Note that this may not 
     * be the first date displayed in the rendered calendar -- to get the start and end dates displayed
     * to the user use {@link #getViewBounds}.
     * @return {Date} The start date
     */
    getStartDate : function(){
        return this.header.getStartDate();
    },

    /**
     * Sets the start date used to calculate the view boundaries to display. The displayed view will be the 
     * earliest and latest dates that match the view requirements and contain the date passed to this function.
     * @param {Date} dt The date used to calculate the new view boundaries
     */
    setStartDate: function(dt){
        this.header.setStartDate(dt, true);
        this.body.setStartDate(dt, true);
    },

    // private
    renderItems: function(){
        this.header.renderItems();
        this.body.renderItems();
    },
    
    /**
     * Returns true if the view is currently displaying today's date, else false.
     * @return {Boolean} True or false
     */
    isToday : function(){
        return this.header.isToday();
    },
    
    /**
     * Updates the view to contain the passed date
     * @param {Date} dt The date to display
     * @return {Date} The new view start date
     */
    moveTo : function(dt, noRefresh){
        this.header.moveTo(dt, noRefresh);
        return this.body.moveTo(dt, noRefresh);
    },
    
    /**
     * Updates the view to the next consecutive date(s)
     * @return {Date} The new view start date
     */
    moveNext : function(noRefresh){
        this.header.moveNext(noRefresh);
        return this.body.moveNext(noRefresh);
    },
    
    /**
     * Updates the view to the previous consecutive date(s)
     * @return {Date} The new view start date
     */
    movePrev : function(noRefresh){
        this.header.movePrev(noRefresh);
        return this.body.movePrev(noRefresh);
    },

    /**
     * Shifts the view by the passed number of days relative to the currently set date
     * @param {Number} value The number of days (positive or negative) by which to shift the view
     * @return {Date} The new view start date
     */
    moveDays : function(value, noRefresh){
        this.header.moveDays(value, noRefresh);
        return this.body.moveDays(value, noRefresh);
    },
    
    /**
     * Updates the view to show today
     * @return {Date} Today's date
     */
    moveToday : function(noRefresh){
        this.header.moveToday(noRefresh);
        return this.body.moveToday(noRefresh);
    }
});
