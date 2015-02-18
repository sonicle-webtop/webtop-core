/**
 * @class Sonicle.calendar.view.Week
 * @extends Sonicle.calendar.DayView
 * <p>Displays a calendar view by week. This class does not usually need to be used directly as you can
 * use a {@link Sonicle.calendar.CalendarPanel CalendarPanel} to manage multiple calendar views at once including
 * the week view.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.Week5', {
	extend: 'Sonicle.calendar.view.Day',
	alias: 'widget.week5view',
	
	constructor: function(cfg) {
		cfg.dayCount = 5;
		cfg.startDay = 1;
		cfg.startDayIsStatic = true;
		this.callParent([cfg]);
	}
});
