/*
 * Internal drop zone implementation for the calendar day and week views.
 */
Ext.define('Sonicle.calendar.dd.DayDropZone', {
	extend: 'Sonicle.calendar.dd.DropZone',
	requires: [
		'Sonicle.Date'
	],
	
	ddGroup: 'DayViewDD',
	//dateRangeFormat: '{0} - {1}',
	//dateFormat: 'n/j',
	
	updateProxy: function(e, data, start, end) {
		var me = this,
				timeFmt = (me.use24HourTime) ? 'G:i' : 'g:ia',
				text,
				dt;
		
		if(data.type === 'caldrag') {
			text = me.createText;
			dt = Ext.String.format(me.dateRangeFormat, 
					Ext.Date.format(start, timeFmt), 
					Ext.Date.format(end, timeFmt));
			
		} else if(data.type === 'eventdrag') {
			text = (e.ctrlKey || e.altKey) ? me.copyText : me.moveText;
			dt = Ext.Date.format(start, (me.dateFormat + ' ' + timeFmt));
			
		} else if(data.type === 'eventresize') {
			text = me.resizeText;
			dt = Ext.String.format(me.dateRangeFormat, 
					Ext.Date.format(start, timeFmt), 
					Ext.Date.format(end, timeFmt));
		}
		
		data.proxy.updateMsg(Ext.String.format(text, dt));
	},
	
	onNodeOver: function(n, dd, e, data) {
		var me = this,
				soDate = Sonicle.Date,
				dt,
				timeFmt = (me.use24HourTime) ? 'G:i' : 'g:ia',
				box,
				endDt,
				text = me.createText,
				diff,
				curr,
				start,
				end,
				evtEl,
				dayCol;

		if (data.type === 'caldrag') {
			if (!me.dragStartMarker) {
				// Since the container can scroll, this gets a little tricky.
				// There is no el in the DOM that we can measure by default since
				// the box is simply calculated from the original drag start (as opposed
				// to dragging or resizing the event where the orig event box is present).
				// To work around this we add a placeholder el into the DOM and give it
				// the original starting time's box so that we can grab its updated
				// box measurements as the underlying container scrolls up or down.
				// This placeholder is removed in onNodeDrop.
				me.dragStartMarker = n.el.parent().createChild({
					style: 'position:absolute;'
				});
				me.dragStartMarker.setBox(n.timeBox);
				me.dragCreateDt = n.date;
			}
			box = me.dragStartMarker.getBox();
			box.height = Math.ceil(Math.abs(e.xy[1] - box.y) / n.timeBox.height) * n.timeBox.height;

			if (e.xy[1] < box.y) {
				box.height += n.timeBox.height;
				box.y = box.y - box.height + n.timeBox.height;
				endDt = soDate.add(me.dragCreateDt, {minutes: me.ddIncrement});
			} else {
				n.date = soDate.add(n.date, {minutes: me.ddIncrement});
			}
			me.shim(me.dragCreateDt, box);
			
			diff = soDate.diff(me.dragCreateDt, n.date);
			curr = soDate.add(me.dragCreateDt, {millis: diff});
			me.dragStartDate = soDate.min(me.dragCreateDt, curr);
			me.dragEndDate = endDt || soDate.max(me.dragCreateDt, curr);
			me.updateProxy(e, data, me.dragStartDate, me.dragEndDate);
			
		} else {
			evtEl = Ext.get(data.ddel);
			dayCol = evtEl.parent().parent();
			box = evtEl.getBox();

			box.width = dayCol.getWidth();

			if (data.type === 'eventdrag') {
				if (me.dragOffset === undefined) {
					// on fast drags there is a lag between the original drag start xy position and
					// that first detected within the drop zone's getTargetFromEvent method (which is
					// where n.timeBox comes from). to avoid a bad offset we calculate the
					// timeBox based on the initial drag xy, not the current target xy.
					//var initialTimeBox = me.view.getDayAt(data.xy[0], data.xy[1]).timeBox;
					//me.dragOffset = initialTimeBox.y - box.y;
					me.dragOffset = n.timeBox.y - box.y;
					box.y = n.timeBox.y - me.dragOffset;
				} else {
					box.y = n.timeBox.y;
				}
				
				box.x = n.el.getX();
				me.shim(n.date, box);
				/*
				dt = Ext.Date.format(n.date, (me.dateFormat + ' ' + timeFmt));
				text = (e.ctrlKey || e.altKey) ? me.copyText : me.moveText;
				*/
				me.updateProxy(e, data, n.date, n.date);
			}
			if (data.type === 'eventresize') {
				box.x = dayCol.getX();
				
				var units;
				if(data.direction === 'bottom') {
					if (!me.resizeDt) {
						me.resizeDt = n.date;
						me.resizeBox = {
							yRef: box.y, // Reference y-coord for computing units
							height: box.height
						};
					}
					units = (e.xy[1] <= me.resizeBox.yRef) ? 1 : Math.ceil(Math.abs(e.xy[1] - me.resizeBox.yRef) / n.timeBox.height);
					box.height = units * n.timeBox.height;
					
					if(e.xy[1] >= me.resizeBox.yRef) n.date = Ext.Date.add(n.date, Ext.Date.MINUTE, me.ddIncrement);
					curr = soDate.copyTime(n.date, me.resizeDt);
					start = data.eventStart;
					end = soDate.max(curr, soDate.add(data.eventStart, {minutes: me.ddIncrement}));
					
				} else {
					if (!me.resizeDt) {
						me.resizeDt = n.date;
						me.resizeBox = {
							y: box.y,
							yRef: box.y + box.height, // Reference y-coord for computing units
							height: box.height
						};
					}
					
					units = (e.xy[1] >= me.resizeBox.yRef) ? 1 : Math.ceil(Math.abs(e.xy[1] - me.resizeBox.yRef) / n.timeBox.height);
					box.y = me.resizeBox.yRef - (units * n.timeBox.height);
					box.height = units * n.timeBox.height;
					
					if(e.xy[1] <= me.resizeBox.yRef) n.date = Ext.Date.add(n.date, Ext.Date.MINUTE, -me.ddIncrement);
					curr = soDate.add(soDate.copyTime(n.date, me.resizeDt), {minutes: me.ddIncrement});
					start = soDate.min(curr, soDate.add(data.eventEnd, {minutes: -me.ddIncrement}));
					end = data.eventEnd;
				}
				
				me.shim(me.resizeDt, box);
				
				data.resizeDates = {
					StartDate: start,
					EndDate: end
				};
				/*
				dt = Ext.String.format(me.dateRangeFormat, 
						Ext.Date.format(start, timeFmt), 
						Ext.Date.format(end, timeFmt));
				text = me.resizeText;
				*/
				me.updateProxy(e, data, start, end);
			}
		}
		
		//data.proxy.updateMsg(Ext.String.format(text, dt));
		return me.dropAllowed;
	},
	
	shim: function(dt, box) {
		Ext.each(this.shims,
				function(shim) {
					if (shim) {
						shim.isActive = false;
						shim.hide();
					}
				}
		);

		var shim = this.shims[0];
		if (!shim) {
			shim = this.createShim();
			this.shims[0] = shim;
		}

		shim.isActive = true;
		shim.show();
		shim.setBox(box);
		this.DDMInstance.notifyOccluded = true;
	},
	
	onNodeDrop: function(n, dd, e, data) {
		var me = this,
				rec;
		if (n && data) {
			if (data.type === 'eventdrag') {
				rec = me.view.getEventRecordFromEl(data.ddel);
				me.view.onEventDrop(rec, n.date, (e.ctrlKey || e.altKey) ? 'copy' : 'move');
				me.onCalendarDragComplete();
				delete me.dragOffset;
				return true;
			}
			if (data.type === 'eventresize') {
				rec = me.view.getEventRecordFromEl(data.ddel);
				me.view.onEventResize(rec, data.resizeDates);
				me.onCalendarDragComplete();
				delete me.resizeDt;
				delete me.resizeBox;
				return true;
			}
			if (data.type === 'caldrag') {
				Ext.destroy(me.dragStartMarker);
				delete me.dragStartMarker;
				delete me.dragCreateDt;
				this.view.onCalendarEndDrag(me.dragStartDate, me.dragEndDate,
						Ext.bind(me.onCalendarDragComplete, me));
				//shims are NOT cleared here -- they stay visible until the handling
				//code calls the onCalendarDragComplete callback which hides them.
				return true;
			}
		}
		me.onCalendarDragComplete();
		return false;
	}
});