/**
 * @class Sonicle.calendar.view.Week
 * @extends Sonicle.calendar.DayView
 * <p>Displays a calendar view by week. This class does not usually need ot be used directly as you can
 * use a {@link Sonicle.calendar.CalendarPanel CalendarPanel} to manage multiple calendar views at once including
 * the week view.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.Week5', {
	extend: 'Sonicle.calendar.view.Day',
	alias: 'widget.week5view',
	
	/**
	 * @cfg {Number} dayCount
	 * The number of days to display in the view (defaults to 5)
	 */
	dayCount: 5,
	
	/**
	 * @cfg {Boolean} skipWeekend
	 * True to avoid display of week-end days, false otherwise.
	 */
	skipWeekend: true
});
