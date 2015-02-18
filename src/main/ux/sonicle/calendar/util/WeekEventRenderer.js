/* @private
 * This is an internal helper class for the calendar views and should not be overridden.
 * It is responsible for the base event rendering logic underlying all views based on a 
 * box-oriented layout that supports day spanning (MonthView, MultiWeekView, DayHeaderView).
 */
Ext.define('Sonicle.calendar.util.WeekEventRenderer', {
	requires: ['Sonicle.Date'],
	
	statics: {
		// private
		getEventRow: function(id, week, index) {
			var indexOffset = 1,
					//skip row with date #'s
					evtRow,
					wkRow = Ext.get(id + '-wk-' + week);
			if (wkRow) {
				var table = wkRow.child('.ext-cal-evt-tbl', true);
				evtRow = table.tBodies[0].childNodes[index + indexOffset];
				if (!evtRow) {
					evtRow = Ext.core.DomHelper.append(table.tBodies[0], '<tr></tr>');
				}
			}
			return Ext.get(evtRow);
		},
		
		render: function(o) {
			var w = 0,
					eDate = Ext.Date,
					soDate = Sonicle.Date,
					grid = o.eventGrid,
					dt = eDate.clone(o.viewStart),
					eventTpl = o.tpl,
					max = (o.maxEventsPerDay !== undefined) ? o.maxEventsPerDay : 999,
					weekCount = (o.weekCount < 1) ? 6 : o.weekCount,
					dayCount = (o.weekCount === 1) ? o.dayCount : 7,
					cellCfg;
			
			console.log('maxEventsPerDay: '+o.maxEventsPerDay);
			
			for (; w < weekCount; w++) {
				if (!grid[w] || (grid[w].length === 0)) {
					// no events or span cells for the entire week
					if (weekCount === 1) {
						row = this.getEventRow(o.id, w, 0);
						cellCfg = {
							tag: 'td',
							cls: 'ext-cal-ev',
							id: o.id + '-empty-0-day-' + eDate.format(dt, 'Ymd'),
							html: '&#160;'
						};
						if (dayCount > 1) {
							cellCfg.colspan = dayCount;
						}
						Ext.core.DomHelper.append(row, cellCfg);
					}
					dt = soDate.add(dt, {days: 7});
				} else {
					var row,
							d = 0,
							wk = grid[w],
							startOfWeek = eDate.clone(dt),
							endOfWeek = soDate.add(startOfWeek, {days: dayCount, millis: -1});

					for (; d < dayCount; d++) {
						if (wk[d]) {
							var ev = 0,
									emptyCells = 0,
									skipped = 0,
									day = wk[d],
									ct = day.length,
									evt;

							for (; ev < ct; ev++) {
								evt = day[ev];

								// Add an empty cell for days that have sparse arrays.
								// See EXTJSIV-7832.
								if (!evt && (ev < max)) {
									row = this.getEventRow(o.id, w, ev);
									cellCfg = {
										tag: 'td',
										cls: 'ext-cal-ev',
										id: o.id + '-empty-' + ct + '-day-' + eDate.format(dt, 'Ymd')
									};

									Ext.core.DomHelper.append(row, cellCfg);
								}

								if (!evt) {
									continue;
								}

								if (ev >= max) {
									skipped++;
									continue;
								}

								if (!evt.isSpan || evt.isSpanStart) {
									//skip non-starting span cells
									var item = evt.data || evt.event.data;
									item._weekIndex = w;
									item._renderAsAllDay = item[Sonicle.calendar.data.EventMappings.IsAllDay.name] || evt.isSpanStart;
									item.spanLeft = item[Sonicle.calendar.data.EventMappings.StartDate.name].getTime() < startOfWeek.getTime();
									item.spanRight = item[Sonicle.calendar.data.EventMappings.EndDate.name].getTime() > endOfWeek.getTime();
                                    item.spanCls = (item.spanLeft ? (item.spanRight ? 'ext-cal-ev-spanboth':
											'ext-cal-ev-spanleft') : (item.spanRight ? 'ext-cal-ev-spanright': ''));

									row = this.getEventRow(o.id, w, ev);
									cellCfg = {
										tag: 'td',
										cls: 'ext-cal-ev',
										cn: eventTpl.apply(o.templateDataFn(item))
									};
									var diff = soDate.diffDays(dt, item[Sonicle.calendar.data.EventMappings.EndDate.name]) + 1,
											cspan = Math.min(diff, dayCount - d);

									if (cspan > 1) {
										cellCfg.colspan = cspan;
									}
									Ext.core.DomHelper.append(row, cellCfg);
								}
							}
							
							// We're done processing all of the events for the current day. Time to insert the
							// "more events" link or the last empty TD for the day, if needed.
							if (ev > max) {
								// We hit one or more events in the grid that could not be displayed since the max
								// events per day count was exceeded, so add the "more events" link.
								row = this.getEventRow(o.id, w, max);
								Ext.core.DomHelper.append(row, {
									tag: 'td',
									cls: 'ext-cal-ev-more',
									//style: 'outline: 1px solid blue;', // helpful for debugging
									id: 'ext-cal-ev-more-' + eDate.format(dt, 'Ymd'),
									cn: {
										tag: 'a',
										html: Ext.String.format(o.moreText, skipped)
									}
								});
							}
							if (ct < o.evtMaxCount[w]) {
								// We did NOT hit the max event count, meaning that we are now left with a gap in
								// the layout table which we need to fill with one last empty TD.
								row = this.getEventRow(o.id, w, ct);
								if (row) {
									cellCfg = {
										tag: 'td',
										cls: 'ext-cal-ev',
										id: o.id + '-empty-' + (ct + 1) + '-day-' + eDate.format(dt, 'Ymd')
									};
									var rowspan = o.evtMaxCount[w] - ct;
									if (rowspan > 1) {
										cellCfg.rowspan = rowspan;
									}
									Ext.core.DomHelper.append(row, cellCfg);
								}
							}
						} else {
							row = this.getEventRow(o.id, w, 0);
							if (row) {
								cellCfg = {
									tag: 'td',
									cls: 'ext-cal-ev',
									id: o.id + '-empty-day-' + eDate.format(dt, 'Ymd')
								};
								if (o.evtMaxCount[w] > 1) {
									cellCfg.rowSpan = o.evtMaxCount[w];
								}
								Ext.core.DomHelper.append(row, cellCfg);
							}
						}
						dt = soDate.add(dt, {days: 1});
					}
				}
			}
		}
	}
});
