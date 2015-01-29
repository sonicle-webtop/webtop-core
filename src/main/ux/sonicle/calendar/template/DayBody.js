/**
 * @class Sonicle.calendar.template.DayBody
 * @extends Ext.XTemplate
 * <p>This is the template used to render the scrolling body container used in {@link Sonicle.calendar.DayView DayView} and 
 * {@link Sonicle.calendar.WeekView WeekView}. This template is automatically bound to the underlying event store by the 
 * calendar components and expects records of type {@link Sonicle.calendar.EventRecord}.</p>
 * <p>Note that this template would not normally be used directly. Instead you would use the {@link Sonicle.calendar.DayViewTemplate}
 * that internally creates an instance of this template along with a {@link Sonicle.calendar.DayHeaderTemplate}.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.template.DayBody', {
	extend: 'Ext.XTemplate',
	requires: [
		'Sonicle.Date'
	],
	
	constructor: function (cfg) {
		Ext.apply(this, cfg);

		this.callParent([
			'<table class="ext-cal-bg-tbl" cellspacing="0" cellpadding="0">',
				'<tbody>',
					'<tr height="1">',
						'<td class="ext-cal-gutter"></td>',
						'<td colspan="{dayCount}">',
							'<div class="ext-cal-bg-rows">',
								'<div class="ext-cal-bg-rows-inner">',
								'<tpl for="times">',
									'<div class="ext-cal-bg-row" style="height:{parent.hourHeight}px;">',
										'<div class="ext-cal-bg-row-div ext-row-{[xindex]}" style="height:{parent.hourSeparatorHeight}px;"></div>',
									'</div>',
								'</tpl>',
								'</div>',
							'</div>',
						'</td>',
					'</tr>',
					'<tr>',
						'<td class="ext-cal-day-times">',
						'<tpl for="times">',
							'<div class="ext-cal-bg-row" style="height:{parent.hourHeight}px;">',
								'<div class="ext-cal-day-time-inner" style="height:{parent.hourHeight}px;">{.}</div>',
							'</div>',
						'</tpl>',
						'</td>',
						'<tpl for="days">',
						'<td class="ext-cal-day-col">',
							'<div class="ext-cal-day-col-inner">',
								'<div id="{[this.id]}-day-col-{.:date("Ymd")}" class="ext-cal-day-col-gutter" style="height:{parent.dayHeight}px;"></div>',
							'</div>',
						'</td>',
						'</tpl>',
					'</tr>',
				'</tbody>',
			'</table>'
		]);
	},
	
	// private
	applyTemplate: function (o) {
		var me = this,
				eDate = Ext.Date,
				soDate = Sonicle.Date,
				timeFmt = (me.use24HourTime) ? 'G:i' : 'ga',
				start = me.viewStartHour,
				end = me.viewEndHour,
				mins = me.hourIncrement,
				dayHeight = me.hourHeight * (end - start),
				i = 0,
				days = [],
				dt = Ext.Date.clone(o.viewStart),
				times = [];
		
		me.today = soDate.today();
		me.dayCount = me.dayCount || 1;
		
		for (; i < me.dayCount; i++) {
			days[i] = soDate.add(dt, {days: i});
		}

		// use a fixed DST-safe date so times don't get skipped on DST boundaries
		dt = eDate.clearTime(new Date('5/26/1972'));

		for (i=start; i<end; i++) {
			times.push(eDate.format(dt, timeFmt));
			dt = soDate.add(dt, {minutes: mins});
		}

		return me.applyOut({
			days: days,
			dayCount: days.length,
			times: times,
			hourHeight: me.hourHeight,
			dayHeight: dayHeight,
			hourSeparatorHeight: (me.hourHeight / 2)
		}, []).join('');
	},
	
	apply: function (values) {
		return this.applyTemplate.apply(this, arguments);
	}
});
