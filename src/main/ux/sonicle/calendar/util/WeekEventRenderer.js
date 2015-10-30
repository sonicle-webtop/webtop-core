/* @private
 * This is an internal helper class for the calendar views and should not be overridden.
 * It is responsible for the base event rendering logic underlying all views based on a 
 * box-oriented layout that supports day spanning (MonthView, MultiWeekView, DayHeaderView).
 */
Ext.define('Sonicle.calendar.util.WeekEventRenderer', {
	requires: ['Sonicle.Date'],
	
	statics: {
		/**
		 * Retrieve the event layout table row for the specified week and row index. If
		 * the row does not already exist it will get created and appended to the DOM.
		 * This method does not check against the max allowed events -- it is the responsibility
		 * of calling code to ensure that an event row at the specified index is really needed.
		 */
		getEventRow: function(viewId, weekIndex, rowIndex) {
			var indexOffset = 1, //skip the first row with date #'s
					evtRow,
					wkRow = Ext.get(viewId + '-wk-' + weekIndex);
			if (wkRow) {
				var table = wkRow.child('.ext-cal-evt-tbl', true);
				evtRow = table.tBodies[0].childNodes[rowIndex + indexOffset];
				if (!evtRow) {
					evtRow = Ext.core.DomHelper.append(table.tBodies[0], '<tr></tr>');
				}
			}
			return Ext.get(evtRow);
		},
		
		/**
		 * @private
		 * Events are collected into a big multi-dimensional array in the view, then passed here
		 * for rendering. The event grid consists of an array of weeks (1-n), each of which contains an
		 * array of days (1-7), each of which contains an array of events and span placeholders (0-n).
		 * @param {Object} o An object containing all of the supported config options (see Sonicle.calendar.view.Month.renderItems() to see what gets passed).
		 */
		render: function(o) {
			var wi = 0,
					eDate = Ext.Date,
					soDate = Sonicle.Date,
					EM = Sonicle.calendar.data.EventMappings,
					grid = o.eventGrid,
					dt = eDate.clone(o.viewStart),
					eventTpl = o.tpl,
					max = (o.maxEventsPerDay !== undefined) ? o.maxEventsPerDay : 999,
					weekCount = (o.weekCount < 1) ? 6 : o.weekCount,
					dayCount = (o.weekCount === 1) ? o.dayCount : 7,
					cellCfg,
					wGrid;
			
			//TODO: valutare se modificare la funzione come l'exensible
			
			// Loop through each week in the overall event grid
			for (; wi < weekCount; wi++) {
				wGrid = grid[wi];
				
				if (!wGrid || (wGrid.length === 0)) {
					// no events or span cells for the entire week
					if (weekCount === 1) {
						row = this.getEventRow(o.id, wi, 0);
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
							startOfWeek = eDate.clone(dt),
							endOfWeek = soDate.add(startOfWeek, {days: dayCount, millis: -1});
					
					for (; d < dayCount; d++) {
						if (wGrid[d]) {
							var ev = 0,
									emptyCells = 0,
									skipped = 0,
									day = wGrid[d],
									ct = day.length,
									evt;
							
							for (; ev < ct; ev++) {
								evt = day[ev];
								
								// Add an empty cell for days that have sparse arrays.
								// See EXTJSIV-7832.
								if (!evt && (ev < max)) {
									row = this.getEventRow(o.id, wi, ev);
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
									item._weekIndex = wi;
									item._renderAsAllDay = item[EM.IsAllDay.name] || evt.isSpanStart;
									item.spanLeft = item[EM.StartDate.name].getTime() < startOfWeek.getTime();
									item.spanRight = item[EM.EndDate.name].getTime() > endOfWeek.getTime();
                                    item._spanCls = (item.spanLeft ? (item.spanRight ? 'ext-cal-ev-spanboth':
											'ext-cal-ev-spanleft') : (item.spanRight ? 'ext-cal-ev-spanright': ''));

									row = this.getEventRow(o.id, wi, ev);
									cellCfg = {
										tag: 'td',
										cls: 'ext-cal-ev',
										cn: eventTpl.apply(o.templateDataFn(item))
									};
									var diff = soDate.diffDays(dt, item[EM.EndDate.name]) + 1,
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
								row = this.getEventRow(o.id, wi, max);
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
							if (ct < o.evtMaxCount[wi]) {
								// We did NOT hit the max event count, meaning that we are now left with a gap in
								// the layout table which we need to fill with one last empty TD.
								row = this.getEventRow(o.id, wi, ct);
								if (row) {
									cellCfg = {
										tag: 'td',
										cls: 'ext-cal-ev',
										id: o.id + '-empty-' + (ct + 1) + '-day-' + eDate.format(dt, 'Ymd')
									};
									var rowspan = o.evtMaxCount[wi] - ct;
									if (rowspan > 1) {
										cellCfg.rowspan = rowspan;
									}
									Ext.core.DomHelper.append(row, cellCfg);
								}
							}
						} else {
							row = this.getEventRow(o.id, wi, 0);
							if (row) {
								cellCfg = {
									tag: 'td',
									cls: 'ext-cal-ev',
									id: o.id + '-empty-day-' + eDate.format(dt, 'Ymd')
								};
								if (o.evtMaxCount[wi] > 1) {
									cellCfg.rowSpan = o.evtMaxCount[wi];
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
