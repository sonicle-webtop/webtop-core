/**
 * @class Sonicle.calendar.view.Month
 * @extends Sonicle.calendar.CalendarView
 * <p>Displays a calendar view by month. This class does not usually need to be used directly as you can
 * use a {@link Sonicle.calendar.CalendarPanel CalendarPanel} to manage multiple calendar views at once including
 * the month view.</p>
 * @constructor
 * @param {Object} config The config object
 */
Ext.define('Sonicle.calendar.view.Month', {
	extend: 'Sonicle.calendar.view.AbstractCalendar',
	alias: 'widget.monthview',
	requires: [
		'Ext.XTemplate',
		'Sonicle.calendar.util.EventUtils',
		'Sonicle.calendar.template.Month',
		'Sonicle.calendar.util.WeekEventRenderer',
		'Sonicle.calendar.view.MonthDayDetail'
	],
	
	/**
	 * @cfg {Boolean} showTime
	 * True to display the current time in today's box in the calendar, false to not display it (defautls to true)
	 */
	showTime: true,
	
	/**
	 * @cfg {Boolean} showTodayText
	 * True to display the {@link #todayText} string in today's box in the calendar, false to not display it (defautls to true)
	 */
	showTodayText: true,
	
	/**
	 * @cfg {String} todayText
	 * The text to display in the current day's box in the calendar when {@link #showTodayText} is true (defaults to 'Today')
	 */
	todayText: 'Today',
	
	/**
	 * @cfg {Boolean} showHeader
	 * True to display a header beneath the navigation bar containing the week names above each week's column, false not to 
	 * show it and instead display the week names in the first row of days in the calendar (defaults to false).
	 */
	showHeader: false,
	
	/**
	 * @cfg {Boolean} showWeekLinks
	 * True to display an extra column before the first day in the calendar that links to the {@link Sonicle.calendar.WeekView view}
	 * for each individual week, false to not show it (defaults to false). If true, the week links can also contain the week 
	 * number depending on the value of {@link #showWeekNumbers}.
	 */
	showWeekLinks: false,
	
	/**
	 * @cfg {Boolean} showWeekNumbers
	 * True to show the week number for each week in the calendar in the week link column, false to show nothing (defaults to false).
	 * Note that if {@link #showWeekLinks} is false this config will have no affect even if true.
	 */
	showWeekNumbers: false,
	
	/**
	 * @cfg {String} weekLinkOverClass
	 * The CSS class name applied when the mouse moves over a week link element (only applies when {@link #showWeekLinks} is true,
	 * defaults to 'ext-week-link-over').
	 */
	weekLinkOverClass: 'ext-week-link-over',
	
	/**
	 * @cfg {String} moreText
	 * The text to display in a day box when there are more events than can be displayed and a link is provided to
	 * show a popup window with all events for that day (defaults to '+{0} more...', where {0} will be
	 * replaced by the number of additional events that are not currently displayed for the day).
	 */
	moreText: '+{0} more...',
	
	/**
	 * @cfg {Number} morePanelMinWidth
	 * When there are more events in a given day than can be displayed in the calendar view, the extra events
	 * are hidden and a "{@link #moreText more events}" link is displayed. When clicked, the link pops up a
	 * detail panel that displays all events for that day. By default the panel will be the same width as the day
	 * box, but this config allows you to set the minimum width of the panel in the case where the width
	 * of the day box is too narrow for the events to be easily readable (defaults to 220 pixels).
	 */
	morePanelMinWidth: 220,
	
	//private properties -- do not override:
	daySelector: '.ext-cal-day',
	moreSelector: '.ext-cal-ev-more',
	weekLinkSelector: '.ext-cal-week-link',
	weekCount: -1,
	// defaults to auto by month
	dayCount: 7,
	moreElIdDelimiter: '-more-',
	weekLinkIdDelimiter: 'ext-cal-week-',
	// See EXTJSIV-11407.
	operaLT11: Ext.isOpera && (parseInt(Ext.operaVersion) < 11),
	
	/**
	 * @event dayclick
	 * Fires after the user clicks within the view container and not on an event element
	 * @param {Sonicle.calendar.view.Month} this
	 * @param {Date} dt The date/time that was clicked on
	 * @param {Boolean} allday True if the day clicked on represents an all-day box, else false. Clicks within the 
	 * MonthView always return true for this param.
	 * @param {Ext.core.Element} el The Element that was clicked on
	 */

	/**
	 * @event weekclick
	 * Fires after the user clicks within a week link (when {@link #showWeekLinks is true)
	 * @param {Sonicle.calendar.view.Month} this
	 * @param {Date} dt The start date of the week that was clicked on
	 */

	// inherited docs
	//dayover: true,
	// inherited docs
	//dayout: true

	// private
	initDD: function() {
		var me = this;
		var cfg = {
			view: me,
			createText: me.ddCreateEventText,
			copyText: me.ddCopyEventText,
			moveText: me.ddMoveEventText,
			dateFormat: me.ddDateFormat,
			ddGroup: 'MonthViewDD'
		};
		
		me.dragZone = Ext.create('Sonicle.calendar.dd.DragZone', me.el, cfg);
		me.dropZone = Ext.create('Sonicle.calendar.dd.DropZone', me.el, cfg);
	},
	
	// private
	onDestroy: function() {
		Ext.destroy(this.ddSelector);
		Ext.destroy(this.dragZone);
		Ext.destroy(this.dropZone);

		this.callParent(arguments);
	},
	
	// private
	afterRender: function() {
		if (!this.tpl) {
			this.tpl = Ext.create('Sonicle.calendar.template.Month', {
				id: this.id,
				showTodayText: this.showTodayText,
				todayText: this.todayText,
				showTime: this.showTime,
				showHeader: this.showHeader,
				showWeekLinks: this.showWeekLinks,
				showWeekNumbers: this.showWeekNumbers
			});
		}
		this.tpl.compile();
		this.addCls('ext-cal-monthview ext-cal-ct');

		this.callParent(arguments);
	},
	
	// private
	onResize: function() {
		var me = this;
		//me.callParent(arguments); ?????????????????????
		if (me.monitorResize) {
			me.maxEventsPerDay = me.getMaxEventsPerDay();
			me.refresh(false);
		}
	},
	
	// private
	forceSize: function() {
		// Compensate for the week link gutter width if visible
		if (this.showWeekLinks && this.el) {
			var hd = this.el.down('.ext-cal-hd-days-tbl'),
					bgTbl = this.el.select('.ext-cal-bg-tbl'),
					evTbl = this.el.select('.ext-cal-evt-tbl'),
					wkLinkW = this.el.down('.ext-cal-week-link').getWidth(),
					w = this.el.getWidth() - wkLinkW;

			hd.setWidth(w);
			bgTbl.setWidth(w);
			evTbl.setWidth(w);
		}
		this.callParent(arguments);
	},
	
	//private
	initClock: function() {
		if (Ext.fly(this.id + '-clock') !== null) {
			this.prevClockDay = new Date().getDay();
			if (this.clockTask) {
				Ext.TaskManager.stop(this.clockTask);
			}
			
			this.clockTask = Ext.TaskManager.start({
				run: function() {
					var el = Ext.fly(this.id + '-clock'),
							t = new Date(),
							timeFmt = Sonicle.calendar.util.EventUtils.timeFmt(this.use24HourTime);

					if (t.getDay() === this.prevClockDay) {
						if (el) {
							el.update(Ext.Date.format(t, timeFmt));
						}
					}
					else {
						this.prevClockDay = t.getDay();
						this.moveTo(t);
					}
				},
				scope: this,
				interval: 1000
			});
		}
	},
	
	// inherited docs
	getEventBodyMarkup: function() {
		if (!this.eventBodyMarkup) {
			this.eventBodyMarkup = [
				'<tpl if="_isRecurring || _isBroken">',
				'<i class="ext-cal-ic {_recIconCls}">&#160;</i>',
				'</tpl>',
				'<tpl if="_isTimezone">',
				'<i class="ext-cal-ic {_tzIconCls}">&#160;</i>',
				'</tpl>',
				'<tpl if="_isPrivate">',
				'<i class="ext-cal-ic {_pvtIconCls}">&#160;</i>',
				'</tpl>',
				'<tpl if="_isReminder">',
				'<i class="ext-cal-ic {_remIconCls}">&#160;</i>',
				'</tpl>',
				'{Title}'
			].join('');
		}
		return this.eventBodyMarkup;
	},
	
	// inherited docs
	getEventTemplate: function() {
		if (!this.eventTpl) {
			var tpl,
					body = this.getEventBodyMarkup();

			tpl = !(Ext.isIE7m || this.operaLT11) ?
					new Ext.XTemplate(
							'<div id="{_elId}" data-qtitle="{Title}" data-qtip="{Tooltip}" data-draggable="{_isDraggable}" class="{_selectorCls} {_colorCls} {_spanCls} ext-cal-evt ext-cal-evr" style="background:{_bgColor};">',
							'<div class="ext-evt-bd" style="color:{_foreColor};">', body, '</div>',
							'</div>'
							)
					: new Ext.XTemplate(
							'<tpl if="_renderAsAllDay">',
							'<div id="{_elId}" data-qtitle="{Title}" data-qtip="{Tooltip}" data-draggable="{_isDraggable}" class="{_selectorCls} {_spanCls} {_colorCls} {_operaLT11} ext-cal-evo" style="background:{_bgColor};">',
								'<div class="ext-cal-evm">',
									'<div class="ext-cal-evi">',
							'</tpl>',
							'<tpl if="!_renderAsAllDay">',
							'<div id="{_elId}" data-qtitle="{Title}" data-qtip="{Tooltip}" class="{_selectorCls} {_colorCls} {_operaLT11} ext-cal-evt ext-cal-evr" style="background:{_bgColor};">',
							'</tpl>',
							'<div class="ext-evt-bd" style="color:{_foreColor};">', body, '</div>',
							'<tpl if="_renderAsAllDay">',
									'</div>',
								'</div>',
							'</tpl>',
							'</div>'
							);
			tpl.compile();
			this.eventTpl = tpl;
		}
		return this.eventTpl;
	},
	
	// private
	getTemplateEventData: function(evt) {
		var me = this,
				EM = Sonicle.calendar.data.EventMappings,
				EU = Sonicle.calendar.util.EventUtils,
				selector = me.getEventSelectorCls(evt[EM.Id.name]),
				bgColor = (evt[EM.Color.name] || ''),
				dinfo = EU.buildDisplayInfo(evt, EU.dateFmt(), EU.timeFmt(me.use24HourTime));
		
		return Ext.applyIf({
			_elId: selector + '-' + evt._weekIndex,
			_selectorCls: selector,
			_bgColor: bgColor,
			_foreColor: me.getEventForeColor(bgColor),
			_colorCls: 'ext-color-' + (evt[EM.Color.name] || 'nocolor') + (evt._renderAsAllDay ? '-ad' : ''),
			_isDraggable: EU.isMovable(evt),
			_isTimezone: (evt[EM.Timezone.name] !== me.timezone),
			_isPrivate: (evt[EM.IsPrivate.name] === true),
			_isRecurring: (evt[EM.IsRecurring.name] === true),
			_isBroken: (evt[EM.IsBroken.name] === true),
			_isReminder: !Ext.isEmpty(evt[EM.Reminder.name]),
			_tzIconCls: me.timezoneIconCls,
			_pvtIconCls: me.privateIconCls,
			_recIconCls: (evt[EM.IsBroken.name] === true) ? me.recurrenceBrokenIconCls : me.recurrenceIconCls,
			_remIconCls: me.reminderIconCls,
			Title: dinfo.title,
			Tooltip: dinfo.tooltip,
			_operaLT11: me.operaLT11 ? 'ext-operaLT11' : ''
		},
		evt);
	},
	
	// private
	refresh: function(reloadData) {
		if (this.detailPanel) {
			this.detailPanel.hide();
		}
		if(!this.isHeaderView) {
			this.maxEventsPerDay = this.getMaxEventsPerDay();
		} else {
			this.maxEventsPerDay = 3;
		}
		this.callParent(arguments);

		if (this.showTime !== false) {
			this.initClock();
		}
	},
	
	// private
	renderItems: function() {
		Sonicle.calendar.util.WeekEventRenderer.render({
			eventGrid: this.allDayOnly ? this.allDayGrid : this.eventGrid,
			viewStart: this.viewStart,
			tpl: this.getEventTemplate(),
			maxEventsPerDay: this.maxEventsPerDay,
			//maxEventsPerDay: this.getMaxEventsPerDay(),
			id: this.id,
			templateDataFn: Ext.bind(this.getTemplateEventData, this),
			evtMaxCount: this.evtMaxCount,
			weekCount: this.weekCount,
			dayCount: this.dayCount,
			moreText: this.moreText
		});
		this.fireEvent('eventsrendered', this);
	},
	
	// private
	getDayEl: function(dt) {
		return Ext.get(this.getDayId(dt));
	},
	
	// private
	getDayId: function(dt) {
		if (Ext.isDate(dt)) {
			dt = Ext.Date.format(dt, 'Ymd');
		}
		return this.id + this.dayElIdDelimiter + dt;
	},
	
	// private
	getWeekIndex: function(dt) {
		var el = this.getDayEl(dt).up('.ext-cal-wk-ct');
		return parseInt(el.id.split('-wk-')[1], 10);
	},
	
	// private
	getDaySize: function(contentOnly) {
		var me = this,
				box = me.el.getBox(),
				padding = me.getViewPadding(),
				w = (box.width - padding.width) / me.dayCount,
				h = (box.height - padding.height) / me.getWeekCount();
		
		if (contentOnly) {
			// measure last row instead of first in case text wraps in first row
			var hd = me.el.select('.ext-cal-dtitle').last().parent('tr');
			h = hd ? h - hd.getHeight(true) : h;
		}
		return {height: h, width: w};
	},
	
	// private
	getEventHeight: function() {
		if (!this.eventHeight) {
			var evt = this.el.select('.ext-cal-evt').first();
			if (evt) {
				this.eventHeight = evt.parent('td').getHeight();
			}
			else {
				return 18; // no events rendered, so try setting this.eventHeight again later
			}
		}
		return this.eventHeight;
	},
	
	// private
	getMaxEventsPerDay: function() {
		var dayHeight = this.getDaySize(true).height,
				eventHeight = this.getEventHeight();
		return Math.max(Math.floor((dayHeight - eventHeight) / eventHeight), 0);
	},
	
	// private
	getViewPadding: function(sides) {
		sides = sides || 'tlbr';
		
		var top = sides.indexOf('t') > -1,
				left = sides.indexOf('l') > -1,
				right = sides.indexOf('r') > -1,
				height = this.showHeader && top ? this.el.select('.ext-cal-hd-days-tbl').first().getHeight() : 0,
				width = 0;

		if (this.isHeaderView) {
			if (left) {
				width = this.el.select('.ext-cal-gutter').first().getWidth();
			}
			if (right) {
				width += this.el.select('.ext-cal-gutter-rt').first().getWidth();
			}
		}
		else if (this.showWeekLinks && left) {
			width = this.el.select('.ext-cal-week-link').first().getWidth();
		}

		return {
			height: height,
			width: width
		};
	},
	
	// private
	getDayAt: function(x, y) {
		var box = this.el.getBox(),
				daySize = this.getDaySize(),
				dayL = Math.floor(((x - box.x) / daySize.width)),
				dayT = Math.floor(((y - box.y) / daySize.height)),
				days = (dayT * 7) + dayL,
				dt = Sonicle.Date.add(this.viewStart, {days: days});
		return {
			date: dt,
			el: this.getDayEl(dt)
		};
	},
	
	// inherited docs
	moveNext: function() {
		return this.moveMonths(1);
	},
	
	// inherited docs
	movePrev: function() {
		return this.moveMonths(-1);
	},
	
	// private
	onInitDrag: function() {
		this.callParent(arguments);

		if (this.dayOverClass) {
			Ext.select(this.daySelector).removeCls(this.dayOverClass);
		}
		if (this.detailPanel) {
			this.detailPanel.hide();
		}
	},
	
	// private
	onMoreClick: function(dt) {
		if (!this.detailPanel) {
			this.detailPanel = Ext.create('Ext.Panel', {
				id: this.id + '-details-panel',
				title: Ext.Date.format(dt, 'F j'),
				layout: 'fit',
				floating: true,
				renderTo: Ext.getBody(),
				tools: [{
						type: 'close',
						handler: function(e, t, p) {
							p.ownerCt.hide();
						}
					}],
				items: {
					xtype: 'monthdaydetailview',
					id: this.id + '-details-view',
					date: dt,
					view: this,
					store: this.store,
					listeners: {
						'eventsrendered': Ext.bind(this.onDetailViewUpdated, this)
					}
				}
			});
		}
		else {
			this.detailPanel.setTitle(Ext.Date.format(dt, 'F j'));
		}
		this.detailPanel.getComponent(this.id + '-details-view').update(dt);
	},
	
	// private
	onDetailViewUpdated: function(view, dt, numEvents) {
		var p = this.detailPanel,
				dayEl = this.getDayEl(dt),
				box = dayEl.getBox();
		
		p.setWidth(Math.max(box.width, this.morePanelMinWidth));
		p.show();
		p.getEl().alignTo(dayEl, 't-t?');
	},
	
	// private
	onHide: function() {
		this.callParent(arguments);

		if (this.detailPanel) {
			this.detailPanel.hide();
		}
	},
	
	// private
	onClick: function(e, t) {
		if (this.detailPanel) {
			this.detailPanel.hide();
		}
		if (Sonicle.calendar.view.Month.superclass.onClick.apply(this, arguments)) {
			// The superclass handled the click already so exit
			return;
		}
		if (this.dropZone) {
			this.dropZone.clearShims();
		}
		var el = e.getTarget(this.weekLinkSelector, 3),
				dt,
				parts;
		if (el) {
			dt = el.id.split(this.weekLinkIdDelimiter)[1];
			this.fireEvent('weekclick', this, Ext.Date.parseDate(dt, 'Ymd'));
			return;
		}
		el = e.getTarget(this.moreSelector, 3);
		if (el) {
			dt = el.id.split(this.moreElIdDelimiter)[1];
			this.onMoreClick(Ext.Date.parseDate(dt, 'Ymd'));
			return;
		}
		el = e.getTarget('td', 3);
		if (el) {
			if (el.id && el.id.indexOf(this.dayElIdDelimiter) > -1) {
				parts = el.id.split(this.dayElIdDelimiter);
				dt = parts[parts.length - 1];

				this.fireEvent('dayclick', this, Ext.Date.parseDate(dt, 'Ymd'), false, Ext.get(this.getDayId(dt)));
				return;
			}
		}
	},
	
	// private
	handleDayMouseEvent: function(e, t, type) {
		var el = e.getTarget(this.weekLinkSelector, 3, true);
		if (el && this.weekLinkOverClass) {
			el[type === 'over' ? 'addCls' : 'removeCls'](this.weekLinkOverClass);
			return;
		}
		this.callParent(arguments);
	}
});
