/**
 * @class Sonicle.calendar.view.DayHeader
 * @extends Sonicle.calendar.MonthView
 * <p>This is the header area container within the day and week views where all-day events are displayed.
 * Normally you should not need to use this class directly -- instead you should use {@link Sonicle.calendar.DayView DayView}
 * which aggregates this class and the {@link Sonicle.calendar.DayBodyView DayBodyView} into the single unified view
 * presented by {@link Sonicle.calendar.CalendarPanel CalendarPanel}.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.DayHeader', {
	extend: 'Sonicle.calendar.view.Month',
	alias: 'widget.dayheaderview',
	requires: [
		'Sonicle.calendar.template.DayHeader'
	],
	
	// private configs
	weekCount: 1,
	dayCount: 1,
	allDayOnly: true,
	monitorResize: false,
	isHeaderView: true,
	
	/**
	 * @event dayclick
	 * Fires after the user clicks within the day view container and not on an event element
	 * @param {Sonicle.calendar.DayBodyView} this
	 * @param {Date} dt The date/time that was clicked on
	 * @param {Boolean} allday True if the day clicked on represents an all-day box, else false. Clicks within the 
	 * DayHeaderView always return true for this param.
	 * @param {Ext.core.Element} el The Element that was clicked on
	 * @param {Ext.event.Event} evt The raw event object.
	 */
	
	/**
	 * @event daydblclick
	 * Fires after the user clicks within the day view container and not on an event element
	 * @param {Sonicle.calendar.DayBodyView} this
	 * @param {Date} dt The date/time that was clicked on
	 * @param {Boolean} allday True if the day clicked on represents an all-day box, else false. Clicks within the 
	 * DayHeaderView always return true for this param.
	 * @param {Ext.core.Element} el The Element that was clicked on
	 * @param {Ext.event.Event} evt The raw event object.
	 */

	// private
	afterRender: function() {
		var me = this;
		if(!me.tpl) {
			me.tpl = Ext.create('Sonicle.calendar.template.DayHeader', {
				id: me.id,
				showTodayText: me.showTodayText,
				todayText: me.todayText,
				showTime: me.showTime
			});
		}
		me.tpl.compile();
		me.addCls('ext-cal-day-header');

		me.callParent(arguments);
	},
	
	// private
	forceSize: Ext.emptyFn,
	
	// private
	refresh: function(reloadData) {
		this.callParent(false);
		this.recalcHeaderBox();
	},
	
	// private
	recalcHeaderBox: function() {
		var me = this, tbl = me.el.down('.ext-cal-evt-tbl'),
				h = tbl.getHeight();
				//h = Math.max(tbl.getHeight(), 80);
		tbl.setHeight(h);
		me.el.setHeight(h + 7);
		
		// These should be auto-height, but since that does not work reliably
		// across browser / doc type, we have to size them manually
		me.el.down('.ext-cal-hd-ad-inner').setHeight(h + 5);
		me.el.down('.ext-cal-bg-tbl').setHeight(h + 5);
	},
	
	// private
	moveNext: function(noRefresh) {
		return this.moveDays(this.dayCount, noRefresh);
	},
	
	// private
	movePrev: function(noRefresh) {
		return this.moveDays(-this.dayCount, noRefresh);
	},
	
	// private
	onClick: function(e, t) {
		var el = e.getTarget('td', 3),
				parts,
				dt;
		if(el) {
			if(el.id && el.id.indexOf(this.dayElIdDelimiter) > -1) {
				parts = el.id.split(this.dayElIdDelimiter);
				dt = parts[parts.length - 1];
				// We handle dayclick/daydblclick in same way...
				this.fireEvent('day'+e.type, this, Ext.Date.parseDate(dt, 'Ymd'), true, Ext.get(this.getDayId(dt)), e);
				return;
			}
		}
		this.callParent(arguments);
	},
	
	// inherited docs
	isActiveView: function() {
		var calendarPanel = this.ownerCalendarPanel;
		return (calendarPanel && calendarPanel.getActiveView().isDayView);
	}
});
