/**
 * @class Sonicle.calendar.view.DblWeek
 * <p>Displays a calendar view by 2-week. This class does not usually need to be used directly as you can
 * use a {@link Sonicle.calendar.CalendarPanel CalendarPanel} to manage multiple calendar views at once including
 * the week (agenda) view.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.DblWeek', {
    extend: 'Sonicle.calendar.view.Month',
	alias: 'widget.dblweekview',
	
	constructor: function(cfg) {
		cfg.weekCount = 2;
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
