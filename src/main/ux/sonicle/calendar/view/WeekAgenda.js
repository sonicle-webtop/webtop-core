/**
 * @class Sonicle.calendar.view.WeekAgenda
 * @extends Sonicle.calendar.DayView
 * <p>Displays a calendar view by week (agenda). This class does not usually need to be used directly as you can
 * use a {@link Sonicle.calendar.CalendarPanel CalendarPanel} to manage multiple calendar views at once including
 * the week (agenda) view.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.WeekAgenda', {
    extend: 'Sonicle.calendar.view.Month',
	alias: 'widget.weekagendaview',
	
	constructor: function(cfg) {
		cfg.weekCount = 1;
		this.callParent([cfg]);
	},
	
	// inherited docs
	moveNext: function() {
		return this.moveWeeks(this.weekCount, true);
	},
	
	// inherited docs
	movePrev: function() {
		return this.moveWeeks(-this.weekCount, true);
	}
});
