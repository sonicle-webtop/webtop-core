/**
 * @class Sonicle.calendar.view.Week
 * @extends Sonicle.calendar.DayView
 * <p>Displays a calendar view by week. This class does not usually need to be used directly as you can
 * use a {@link Sonicle.calendar.CalendarPanel CalendarPanel} to manage multiple calendar views at once including
 * the week view.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.Week', {
    extend: 'Sonicle.calendar.view.Day',
	alias: 'widget.weekview',
	
	constructor: function(cfg) {
		cfg.dayCount = 7;
		this.callParent([cfg]);
	}
});
