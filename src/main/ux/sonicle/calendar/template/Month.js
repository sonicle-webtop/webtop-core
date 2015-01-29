/**
 * @class Sonicle.calendar.template.Month
 * @extends Ext.XTemplate
 * <p>This is the template used to render the {@link Sonicle.calendar.template.Month MonthView}. Internally this class defers to an
 * instance of {@link Ext.calerndar.BoxLayoutTemplate} to handle the inner layout rendering and adds containing elements around
 * that to form the month view.</p> 
 * <p>This template is automatically bound to the underlying event store by the 
 * calendar components and expects records of type {@link Sonicle.calendar.EventRecord}.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.template.Month', {
	extend: 'Ext.XTemplate',
	requires: ['Sonicle.calendar.template.BoxLayout'],
	
	constructor: function(config) {
		var me = this;
		Ext.apply(me, config);

		me.weekTpl = Ext.create('Sonicle.calendar.template.BoxLayout', config);
		me.weekTpl.compile();

		var weekLinkTpl = me.showWeekLinks ? '<div class="ext-cal-week-link-hd">&#160;</div>' : '';

		me.callParent([
			'<div class="ext-cal-inner-ct {extraClasses}">',
				'<div class="ext-cal-hd-ct ext-cal-month-hd">',
					weekLinkTpl,
					'<table class="ext-cal-hd-days-tbl" cellpadding="0" cellspacing="0">',
					'<tbody>',
						'<tr>',
							'<tpl for="days">',
								'<th class="ext-cal-hd-day{[xindex==1 ? " ext-cal-day-first" : ""]}" title="{.:date("l, F j, Y")}">{.:date("D")}</th>',
							'</tpl>',
						'</tr>',
					'</tbody>',
					'</table>',
				'</div>',
				'<div class="ext-cal-body-ct">{weeks}</div>',
			'</div>'
		]);
	},
	
	// private
	applyTemplate: function(o) {
		var me = this;
		var days = [],
				weeks = me.weekTpl.apply(o),
				dt = o.viewStart,
				soDate = Sonicle.Date;

		for (var i = 0; i < 7; i++) {
			days.push(soDate.add(dt, {days: i}));
		}

		var extraClasses = me.showHeader === true ? '' : 'ext-cal-noheader';
		if (me.showWeekLinks) {
			extraClasses += ' ext-cal-week-links';
		}

		return me.applyOut({
			days: days,
			weeks: weeks,
			extraClasses: extraClasses
		}, []).join('');
	},
	
	apply: function(values) {
		return this.applyTemplate.apply(this, arguments);
	}
});
