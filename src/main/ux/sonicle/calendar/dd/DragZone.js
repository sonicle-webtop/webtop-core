/*
 * Internal drag zone implementation for the calendar components. This provides base functionality
 * and is primarily for the month view -- DayViewDD adds day/week view-specific functionality.
 */
Ext.define('Sonicle.calendar.dd.DragZone', {
	extend: 'Ext.dd.DragZone',
	requires: [
		'Sonicle.calendar.dd.StatusProxy',
		'Sonicle.calendar.data.EventMappings'
	],
	
	ddGroup: 'CalendarDD',
	eventSelector: '.ext-cal-evt',
	
	statics: {
		isEventDraggable: function(rec) {
			var EM = Sonicle.calendar.data.EventMappings,
					data = (rec.data) ? rec.data : rec,
					isRR = (data[EM.IsRecurring.name] === true),
					//isRBro = (data[EM.IsBroken.name] === true),
					isRO = (data[EM.IsReadOnly.name] === true);

			if(isRR || isRO) return false;
			return true;
		}
	},
	
	constructor: function(el, config) {
		if (!Sonicle.calendar._statusProxyInstance) {
			Sonicle.calendar._statusProxyInstance = new Sonicle.calendar.dd.StatusProxy();
		}
		this.proxy = Sonicle.calendar._statusProxyInstance;
		this.callParent(arguments);
	},
	
	getDragData: function(e) {
		// Check whether we are dragging on an event first
		var EM = Sonicle.calendar.data.EventMappings,
				t = e.getTarget(this.eventSelector, 3);
		
		if (t) {
			var rec = this.view.getEventRecordFromEl(t);
			return {
				type: 'eventdrag',
				ddel: t,
				draggable: Sonicle.calendar.dd.DragZone.isEventDraggable(rec),
				eventStart: rec.data[EM.StartDate.name],
				eventEnd: rec.data[EM.EndDate.name],
				proxy: this.proxy
			};
		}

		// If not dragging an event then we are dragging on
		// the calendar to add a new event
		t = this.view.getDayAt(e.getX(), e.getY());
		if (t.el) {
			return {
				type: 'caldrag',
				start: t.date,
				proxy: this.proxy
			};
		}
		return null;
	},
	
	onBeforeDrag: function(data, e) {
		return data.draggable;
	},
	
	onInitDrag: function(x, y) {
		if (this.dragData.ddel) {
			var ghost = this.dragData.ddel.cloneNode(true),
					child = Ext.fly(ghost).down('dl');

			Ext.fly(ghost).setWidth('auto');

			if (child) {
				// for IE/Opera
				child.setHeight('auto');
			}
			this.proxy.update(ghost);
			this.onStartDrag(x, y);
		}
		else if (this.dragData.start) {
			this.onStartDrag(x, y);
		}
		this.view.onInitDrag();
		return true;
	},
	
	afterRepair: function() {
		if (Ext.enableFx && this.dragData.ddel) {
			Ext.fly(this.dragData.ddel).highlight(this.hlColor || 'c3daf9');
		}
		this.dragging = false;
	},
	
	getRepairXY: function(e) {
		if (this.dragData.ddel) {
			return Ext.fly(this.dragData.ddel).getXY();
		}
	},
	
	afterInvalidDrop: function(e, id) {
		Ext.select('.ext-dd-shim').hide();
	},
	
	destroy: function() {
		this.callParent(arguments);
		delete Sonicle.calendar._statusProxyInstance;
	}
});
