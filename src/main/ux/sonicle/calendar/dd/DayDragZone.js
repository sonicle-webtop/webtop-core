/*
 * Internal drag zone implementation for the calendar day and week views.
 */
Ext.define('Sonicle.calendar.dd.DayDragZone', {
	extend: 'Sonicle.calendar.dd.DragZone',
	requires: [
		'Sonicle.calendar.data.EventMappings'
	],
	
	ddGroup: 'DayViewDD',
	topResizeSelector: '.ext-evt-rsz-top',
	bottomResizeSelector: '.ext-evt-rsz-bottom',
	resizeSelector: '.ext-evt-rsz',
	
	getDragData: function (e) {
		var EM = Sonicle.calendar.data.EventMappings,
				t, p, rec;
		
		t = e.getTarget(this.topResizeSelector, 2, true);
		if (t) {
			p = t.parent(this.eventSelector);
			rec = this.view.getEventRecordFromEl(p);
			
			return {
				type: 'eventresize',
				direction: 'top',
				ddel: p.dom,
				//draggable: Sonicle.calendar.dd.DragZone.isEventDraggable(rec),
				draggable: (t.getAttribute('data-draggable') === 'true'),
				eventStart: rec.get(EM.StartDate.name),
				eventEnd: rec.get(EM.EndDate.name),
				proxy: this.proxy
			};
		}
		
		t = e.getTarget(this.bottomResizeSelector, 2, true);
		if (t) {
			p = t.parent(this.eventSelector);
			rec = this.view.getEventRecordFromEl(p);

			return {
				type: 'eventresize',
				direction: 'bottom',
				ddel: p.dom,
				//draggable: Sonicle.calendar.dd.DragZone.isEventDraggable(rec),
				draggable: (t.getAttribute('data-draggable') === 'true'),
				eventStart: rec.get(EM.StartDate.name),
				eventEnd: rec.get(EM.EndDate.name),
				proxy: this.proxy
			};
		}
		
		/*
		t = e.getTarget(this.resizeSelector, 2, true);
		if (t) {
			p = t.parent(this.eventSelector);
			rec = this.view.getEventRecordFromEl(p);

			return {
				type: 'eventresize',
				ddel: p.dom,
				eventStart: rec.get(startDateName),
				eventEnd: rec.get(endDateName),
				proxy: this.proxy
			};
		}
		*/

		t = e.getTarget(this.eventSelector, 3);
		if (t) {
			rec = this.view.getEventRecordFromEl(t);
			return {
				type: 'eventdrag',
				ddel: t,
				//draggable: Sonicle.calendar.dd.DragZone.isEventDraggable(rec),
				draggable: (t.getAttribute('data-draggable') === 'true'),
				eventStart: rec.get(EM.StartDate.name),
				eventEnd: rec.get(EM.EndDate.name),
				proxy: this.proxy
			};
		}

		// If not dragging/resizing an event then we are dragging on
		// the calendar to add a new event
		t = this.view.getDayAt(e.getX(), e.getY());
		if (t.el) {
			return {
				type: 'caldrag',
				dayInfo: t,
				proxy: this.proxy
			};
		}
		return null;
	}
});
