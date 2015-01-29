/**
 * @class Sonicle.calendar.template.DayHeader
 * @extends Ext.XTemplate
 * <p>This is the template used to render the all-day event container used in {@link Sonicle.calendar.DayView DayView} and 
 * {@link Sonicle.calendar.WeekView WeekView}. Internally the majority of the layout logic is deferred to an instance of
 * {@link Sonicle.calendar.BoxLayoutTemplate}.</p> 
 * <p>This template is automatically bound to the underlying event store by the 
 * calendar components and expects records of type {@link Sonicle.calendar.EventRecord}.</p>
 * <p>Note that this template would not normally be used directly. Instead you would use the {@link Sonicle.calendar.DayViewTemplate}
 * that internally creates an instance of this template along with a {@link Sonicle.calendar.DayBodyTemplate}.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.template.DayHeader', {
	extend: 'Ext.XTemplate',
	requires: ['Sonicle.calendar.template.BoxLayout'],
	
	constructor: function(config) {
		var me = this;
		Ext.apply(me, config);

		me.allDayTpl = Ext.create('Sonicle.calendar.template.BoxLayout', config);
		me.allDayTpl.compile();
		
		me.callParent([
			'<div class="ext-cal-hd-ct">',
				'<table class="ext-cal-hd-days-tbl" cellspacing="0" cellpadding="0">',
					'<tbody>',
						'<tr>',
							'<td class="ext-cal-gutter"></td>',
							'<td class="ext-cal-hd-days-td"><div class="ext-cal-hd-ad-inner">{allDayTpl}</div></td>',
							'<td class="ext-cal-gutter-rt"></td>',
						'</tr>',
					'</tbody>',
				'</table>',
			'</div>'
		]);
	},
	
	applyTemplate: function(o) {
		return this.applyOut({
			allDayTpl: this.allDayTpl.apply(o)
		}, []).join('');
	},
	
	apply: function(values) {
		return this.applyTemplate.apply(this, arguments);
	}
});
