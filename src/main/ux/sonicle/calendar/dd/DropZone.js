/*
 * Internal drop zone implementation for the calendar components. This provides base functionality
 * and is primarily for the month view -- DayViewDD adds day/week view-specific functionality.
 */
Ext.define('Sonicle.calendar.dd.DropZone', {
    extend: 'Ext.dd.DropZone',
    
    requires: [
        'Sonicle.Date',
        'Sonicle.calendar.data.EventMappings'
    ],

    ddGroup: 'CalendarDD',
    eventSelector: '.ext-cal-evt',
	dateRangeFormat: '{0} - {1}',
	dateFormat: 'n/j',

    // private
    shims: [],

    getTargetFromEvent: function(e) {
        var dragOffset = this.dragOffset || 0,
        y = e.getY() - dragOffset,
        d = this.view.getDayAt(e.getX(), y);

        return d.el ? d: null;
    },

    onNodeOver: function(n, dd, e, data) {
        var me = this,
				soDate = Sonicle.Date,
				eventDragText = (e.ctrlKey || e.altKey) ? me.copyText: me.moveText,
				start = (data.type === 'eventdrag') ? n.date: soDate.min(data.start, n.date),
				end = (data.type === 'eventdrag') ? soDate.add(n.date, {days: soDate.diffDays(data.eventStart, data.eventEnd)}) : soDate.max(data.start, n.date);

        if (!me.dragStartDate || !me.dragEndDate || (soDate.diffDays(start, me.dragStartDate) !== 0) || (soDate.diffDays(end, me.dragEndDate) !== 0)) {
            me.dragStartDate = start;
            me.dragEndDate = soDate.add(end, {days: 1, millis: -1, clearTime: true});
            me.shim(start, end);

            var range = Ext.Date.format(start, me.dateFormat);
            if (soDate.diffDays(start, end) > 0) {
				end = Ext.Date.format(end, me.dateFormat);
				range = Ext.String.format(me.dateRangeFormat, range, end);
            }
			me.currentRange = range;
        }
		
		var msg = Ext.String.format((data.type === 'eventdrag') ? eventDragText : me.createText, me.currentRange);
		data.proxy.updateMsg(msg);
        return this.dropAllowed;
    },

    shim: function(start, end) {
        this.currWeek = -1;
        this.DDMInstance.notifyOccluded = true;
        var dt = Ext.Date.clone(start),
            i = 0,
            shim,
            box,
            D = Sonicle.Date,
            cnt = D.diffDays(dt, end) + 1;

        Ext.each(this.shims,
            function(shim) {
                if (shim) {
                    shim.isActive = false;
                }
            }
        );

        while (i++<cnt) {
            var dayEl = this.view.getDayEl(dt);

            // if the date is not in the current view ignore it (this
            // can happen when an event is dragged to the end of the
            // month so that it ends outside the view)
            if (dayEl) {
                var wk = this.view.getWeekIndex(dt);
                shim = this.shims[wk];

                if (!shim) {
                    shim = this.createShim();
                    this.shims[wk] = shim;
                }
                if (wk !== this.currWeek) {
                    shim.boxInfo = dayEl.getBox();
                    this.currWeek = wk;
                }
                else {
                    box = dayEl.getBox();
                    shim.boxInfo.right = box.right;
                    shim.boxInfo.width = box.right - shim.boxInfo.x;
                }
                shim.isActive = true;
            }
            dt = D.add(dt, {days: 1});
        }

        Ext.each(this.shims, function(shim) {
            if (shim) {
                if (shim.isActive) {
                    shim.show();
                    shim.setBox(shim.boxInfo);
                }
                else if (shim.isVisible()) {
                    shim.hide();
                }
            }
        });
    },

    createShim: function() {
        if (!this.shimCt) {
            this.shimCt = Ext.get('ext-dd-shim-ct');
            if (!this.shimCt) {
                this.shimCt = document.createElement('div');
                this.shimCt.id = 'ext-dd-shim-ct';
                Ext.getBody().appendChild(this.shimCt);
            }
        }
        var el = document.createElement('div');
        el.className = 'ext-dd-shim';
        this.shimCt.appendChild(el);

        el = Ext.get(el);

        el.setVisibilityMode(2);

        return el;
    },

    clearShims: function() {
        Ext.each(this.shims,
        function(shim) {
            if (shim) {
                shim.hide();
            }
        });
        this.DDMInstance.notifyOccluded = false;
    },

    onContainerOver: function(dd, e, data) {
        return this.dropAllowed;
    },

    onCalendarDragComplete: function() {
        delete this.dragStartDate;
        delete this.dragEndDate;
        this.clearShims();
    },

    onNodeDrop: function(n, dd, e, data) {
		var me = this,
				soDate = Sonicle.Date;
        if (n && data) {
            if (data.type === 'eventdrag') {
                var rec = me.view.getEventRecordFromEl(data.ddel),
                dt = soDate.copyTime(rec.data[Sonicle.calendar.data.EventMappings.StartDate.name], n.date);
				
				me.view.onEventDrop(rec, dt, (e.ctrlKey || e.altKey) ? 'copy': 'move');
                me.onCalendarDragComplete();
                return true;
            }
            if (data.type === 'caldrag') {
				if(!me.dragEndDate) {
					// this can occur on a long click where drag starts but onNodeOver is never executed
					me.dragStartDate = Ext.Date.clearTime(data.start);
					me.dragEndDate = soDate.add(me.dragStartDate, {days: 1, millis: -1, clearTime: true});
				}
				
                me.view.onCalendarEndDrag(me.dragStartDate, me.dragEndDate,
                Ext.bind(me.onCalendarDragComplete, me));
                //shims are NOT cleared here -- they stay visible until the handling
                //code calls the onCalendarDragComplete callback which hides them.
                return true;
            }
        }
        me.onCalendarDragComplete();
        return false;
    },

    onContainerDrop: function(dd, e, data) {
        this.onCalendarDragComplete();
        return false;
    },
	
	destroy: function() {
		Ext.each(this.shims, function(shim) {
			if(shim) Ext.destroy(shim);
		});
		Ext.removeNode(this.shimCt);
		delete this.shimCt;
		this.shims.length = 0;
	}
});
