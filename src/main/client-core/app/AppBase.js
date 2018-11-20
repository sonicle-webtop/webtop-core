/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.app.AppBase', {
	extend: 'Ext.app.Application',
	requires: [
		'Sonicle.String',
		'Sonicle.Date',
		'Sonicle.PageMgr',
		'Sonicle.URLMgr',
		'Sonicle.PrintMgr',
		'Sonicle.upload.Uploader',
		'Sonicle.data.proxy.Ajax',
		'Sonicle.data.identifier.NegativeString',
		'Sonicle.form.field.VTypes',
		
		'Sonicle.plugin.EnterKeyPlugin',
		'Sonicle.plugin.FieldTooltip',
		
		'Sonicle.webtop.core.ux.data.BaseModel',
		'Sonicle.webtop.core.ux.data.EmptyModel',
		'Sonicle.webtop.core.ux.data.SimpleModel',
		'Sonicle.webtop.core.ux.data.ArrayStore',
		'Sonicle.webtop.core.ux.panel.Panel',
		'Sonicle.webtop.core.ux.panel.Fields',
		'Sonicle.webtop.core.ux.panel.Form',
		'Sonicle.webtop.core.ux.panel.Tab',
		
		'Sonicle.webtop.core.app.WT',
		'Sonicle.webtop.core.app.FileTypes',
		'Sonicle.webtop.core.app.Factory',
		'Sonicle.webtop.core.app.Util',
		'Sonicle.webtop.core.app.Log',
		'Sonicle.webtop.core.app.ThemeMgr'
	],
	
	uiid: null,
	platformName: null,
	contextPath: null,
	baseUrl: null,
	pushUrl: null,
	
	/**
	 * @property {Ext.util.HashMap} locales
	 * A collection of locale classes.
	 */
	locales: null,
	
	/**
	 * @property {Ext.util.HashMap} locales
	 * A collection of locale classes.
	 */
	descriptors: null,
	
	/**
	 * @property {Array} services
	 * Array of active service IDs. The order in the collection infers
	 * the appearance order of services in the UI. 
	 */
	services: null,
	
	/**
	 * @property {Objects} roles
	 * A map of assigned roles.
	 */
	roles: null,
	
	/**
	 * @private
	 * @property {Number} maskCount
	 */
	maskCount: 0,
	
	constructor: function() {
		var me = this;
		Ext.themeName = WTS.themeName;
		WT.app = me;
		WT.plTags = Ext.platformTags;
		me.initPlatformTags();
		me.uiid = Sonicle.Crypto.randomString(10);
		me.platformName = WTS.platformName;
		me.contextPath = WTS.contextPath;
		me.baseUrl = window.location.origin + me.contextPath;
		me.pushUrl = WTS.pushUrl;
		me.locales = Ext.create('Ext.util.HashMap');
		me.descriptors = Ext.create('Ext.util.HashMap');
		me.services = [];
		me.roles = {};
		me.callParent(arguments);
	},
	
	init: function() {
		WTA.Log.debug('application:init');
		
		Ext.tip.QuickTipManager.init();
		Ext.setGlyphFontFamily('FontAwesome');
		Ext.getDoc().on('contextmenu', function(e) {
			console.log(e.getTarget().tagName);
		});
		if (!WT.plTags.desktop) {
			Ext.dd.DragDropManager.lock();
		}
		
		// Inits state provider
		if (Ext.util.LocalStorage.supported) {
			Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());
		} else {
			Ext.state.Manager.setProvider(new Ext.state.CookieProvider({
				expires: new Date(Ext.Date.now() + (1000*60*60*24*90)) // 90 days
			}));
		}
		
		WTA.FileTypes.init(WTS.fileTypes);
	},
	
	initPlatformTags: function() {
		var XpT = Ext.platformTags;
		XpT.mobile = XpT.tablet || XpT.phone;
		XpT.touchtheme = Ext.themeName.indexOf('touch') !== -1;
	},
	
	initDescriptors: function() {
		var me = this;
		
		Ext.iterate(WTS.manifests, function(sid, obj) {
			me.locales.add(sid, Ext.create(obj.localeCN));
			var desc = me.createServiceDescriptor({
				id: sid,
				xid: obj.xid,
				ns: obj.ns,
				path: obj.path,
				name: obj.name,
				description: obj.description,
				version: obj.version,
				maintenance: obj.maintenance,
				company: obj.company,
				build: obj.build
			});
			me.descriptors.add(sid, desc);
		});
	},
	
	createServiceDescriptor: function(cfg) {
		Ext.raise('If you see this there is something wrong. This method must be overridden!');
	},
	
	mask: function(msg, update) {
		var me = this;
		me.maskCount++;
		if ((me.maskCount === 1) || ((me.maskCount > 1) && (update === true))) {
			me.viewport.mask(msg);
		}
	},
	
	unmask: function(force) {
		var me = this;
		me.maskCount--;
		if ((me.maskCount === 0) || (force === true)) {
			me.maskCount = 0;
			me.viewport.unmask();
		}
	},
	
	/*
	alert: function(text) {
		var me = this;
		me.maskCount++;
		if (me.maskCount === 1) me.viewport.mask();
		alert(text);
		if (me.maskCount === 0) me.viewport.mask();
	},
	*/
	
	/**
	 * Returns desired locale instance.
	 * @param {String} sid The service ID.
	 * @returns {WT.Locale}
	 */
	getLocale: function(sid) {
		return this.locales.get(sid);
	},
	
	/**
	 * Checks passed role set against current user profile.
	 * @param {Array} roles The role set
	 * @returns {Array} Positional array of results.
	 */
	hasRoles: function(roles) {
		var me = this, arr = [], i;
		for (i=0; i<roles.length; i++) {
			if (me.roles[roles[i]]) {
				arr.push(true);
			} else {
				arr.push(false);
			}
		}
		return arr;
	},
	
	/**
	 * Checks if whole role set is satisfied.
	 * @param {Array} roles The role set
	 * @returns {Boolean} True if current profile has all roles, false otherwise.
	 */
	hasAllRoles: function(roles) {
		var me = this, i;
		for (i=0; i<roles.length; i++) {
			if (!me.roles[roles[i]]) return false;
		}
		return true;
	},
	
	/**
	 * Returns loaded service descriptors.
	 * @param {Boolean} [skip] False to include core descriptor. Default to true.
	 * @returns {WTA.DescriptorBase[]}
	 */
	getDescriptors: function(skip) {
		if (!Ext.isDefined(skip)) skip = true;
		var me = this, ret = [];
		Ext.iterate(me.services, function(sid, ix) {
			if (!skip || (ix !== 0)) { // Skip core descriptor at index 0
				var desc = me.getDescriptor(sid);
				if (!desc) Ext.raise('');
				ret.push(desc);
			}
		});
		return ret;
	},
	
	/**
	 * Checks if specified service descriptor is present.
	 * @param {String} sid The service ID.
	 */
	hasDescriptor: function(sid) {
		return this.descriptors.containsKey(sid);
	},
	
	/**
	 * Returns a service descriptor.
	 * @param {String} sid The service ID.
	 * @returns {WTA.ServiceBase} The instance or undefined if not found. 
	 */
	getDescriptor: function(sid) {
		return this.descriptors.get(sid);
	},
	
	getServices: function() {
		return this.services;
	},
	
	/**
	 * Returns a service instance.
	 * @param {String} sid The service ID.
	 * @returns {WTA.sdk.Service} The instance or null if not found. 
	 */
	getService: function(sid) {
		var desc = this.getDescriptor(sid);
		return (desc) ? desc.getInstance() : null;
	},
	
	/**
	 * Returns a service version.
	 * @param {String} sid The service ID.
	 * @returns {String} The version string. 
	 */
	getServiceVersion: function(sid) {
		var desc = this.getDescriptor(sid);
		return (desc) ? desc.getVersion() : "0.0.0";
	}
});

// This is the way to override ExtJs default timeouts.
// We can control timeout specifically in the places we need it. So keep commented for now!
//Ext.Ajax.timeout = 60*1000;
//Ext.override(Ext.data.proxy.Server, {timeout: 60*1000});
//Ext.override(Ext.data.Connection, {timeout: 60*1000});

Ext.override(Ext.window.Window, {

	//fix bug in windows with unselectable content
	//[solved in 6.2.1]
	onShow: function() {
		this.callParent(arguments);
		this.removeCls("x-unselectable");
	}
	
});

Ext.override(Ext.data.PageMap, {

	//fix bug when mistakenly called with start=0 and end=-1
    hasRange: function(start, end) {
        var me = this,
            pageNumber = me.getPageFromRecordIndex(start),
            endPageNumber = me.getPageFromRecordIndex(end);
        for (; pageNumber <= endPageNumber; pageNumber++) {
            if (!me.hasPage(pageNumber)) {
                return false;
            }
        }
		//here fix bug: if getPage returns null, just return true to go on
		var xp=me.getPage(endPageNumber);
        // Check that the last page is filled enough to encapsulate the range.
        if (xp) return (endPageNumber - 1) * me._pageSize + xp.length > end;
		return true;
    }
});

Ext.override(Ext.util.LruCache, {
    // private. Only used by internal methods.
    unlinkEntry: function (entry) {
        // Stitch the list back up.
        if (entry) {
            if (this.last && this.last.key === entry.key)
                this.last = entry.prev;
            if (this.first && this.first.key === entry.key)
                this.first = entry.next;


            if (entry.next) {
                entry.next.prev = entry.prev;
            } else {
                this.last = entry.prev;
            }
            if (entry.prev) {
                entry.prev.next = entry.next;
            } else {
                this.first = entry.next;
            }
            entry.prev = entry.next = null;
        }
    }
});
Ext.override(Ext.dd.DragDropManager, {
    stopEvent: function(e) {
        if (this.stopPropagation) {
            e.stopPropagation();
        }
 
		//avoid a bug while dragging elements
        if (this.preventDefault /* && e.pointerType === 'touch' */) {
            e.preventDefault();
        }
    }	
});
Ext.override(Ext.data.Validation, {
	
	getErrors: function() {
		var errs = [];
		Ext.iterate(this.getData(), function(field, value) {
			if (value !== true) errs.push({id: field, msg: value});
		});
		return errs;
	}
});

//
// Fix bug on Chrome on scroll pageup
//
Ext.override(Ext.grid.plugin.BufferedRenderer,{

    onRangeFetched: function(range, start, end, options, fromLockingPartner) {
        var me = this,
            view = me.view,
            scroller = me.scroller,
            viewEl = view.el,
            rows = view.all,
            increment = 0,
            calculatedTop,
            lockingPartner = (view.lockingPartner && !fromLockingPartner && !me.doNotMirror) && view.lockingPartner.bufferedRenderer,
            variableRowHeight = me.variableRowHeight,
            oldBodyHeight = me.bodyHeight,
            layoutCount = view.componentLayoutCounter,
            activeEl, containsFocus, i, newRows, newTop, newFocus, noOverlap, oldStart, partnerNewRows, pos, removeCount, topAdditionSize, topBufferZone;
        // View may have been destroyed since the DelayedTask was kicked off.
        if (view.destroyed) {
            return;
        }
        // If called as a callback from the Store, the range will be passed, if called from renderRange, it won't
        if (range) {
            if (!fromLockingPartner) {
                // Re-cache the scrollTop if there has been an asynchronous call to the server.
                me.scrollTop = me.scroller.getPosition().y;
            }
        } else {
            range = me.store.getRange(start, end);
            // Store may have been cleared since the DelayedTask was kicked off.
            if (!range) {
                return;
            }
        }
        // If we contain focus now, but do not when we have rendered the new rows, we must focus the view el.
        activeEl = Ext.Element.getActiveElement(true);
        containsFocus = viewEl.contains(activeEl);
        // In case the browser does fire synchronous focus events when a focused element is derendered...
        if (containsFocus) {
            activeEl.suspendFocusEvents();
        }
        // Best guess rendered block position is start row index * row height.
        // We can use this as bodyTop if the row heights are all standard.
        // We MUST use this as bodyTop if the scroll is a telporting scroll.
        // If we are incrementally scrolling, we add the rows to the bottom, and
        // remove a block of rows from the top.
        // The bodyTop is then incremented by the height of the removed block to keep
        // the visuals the same.
        //
        // We cannot always use the calculated top, and compensate by adjusting the scroll position
        // because that would break momentum scrolling on DOM scrolling platforms, and would be
        // immediately undone in the next frame update of a momentum scroll on touch scroll platforms.
        calculatedTop = start * me.rowHeight;
        // The new range encompasses the current range. Refresh and keep the scroll position stable
        if (start < rows.startIndex && end > rows.endIndex) {
            // How many rows will be added at top. So that we can reposition the table to maintain scroll position
            topAdditionSize = rows.startIndex - start;
            // MUST use View method so that itemremove events are fired so widgets can be recycled.
            view.clearViewEl(true);
            newRows = view.doAdd(range, start);
            view.fireItemMutationEvent('itemadd', range, start, newRows, view);
            for (i = 0; i < topAdditionSize; i++) {
                increment -= newRows[i].offsetHeight;
            }
            // We've just added a bunch of rows to the top of our range, so move upwards to keep the row appearance stable
            newTop = me.bodyTop + increment;
        } else {
            // No overlapping nodes; we'll need to render the whole range.
            // teleported flag is set in getFirstVisibleRowIndex/getLastVisibleRowIndex if
            // the table body has moved outside the viewport bounds
            noOverlap = me.teleported || start > rows.endIndex || end < rows.startIndex;
            if (noOverlap) {
                view.clearViewEl(true);
                me.teleported = false;
            }
            if (!rows.getCount()) {
                newRows = view.doAdd(range, start);
                view.fireItemMutationEvent('itemadd', range, start, newRows, view);
                newTop = calculatedTop;
                // Adjust the bodyTop to place the data correctly around the scroll vieport
                if (noOverlap && variableRowHeight) {
                    topBufferZone = me.scrollTop < me.position ? me.leadingBufferZone : me.trailingBufferZone;
                    newTop = Math.max(me.scrollTop - rows.item(rows.startIndex + topBufferZone - 1, true).offsetTop, 0);
                }
            }
            // Moved down the dataset (content moved up): remove rows from top, add to end
            else if (end > rows.endIndex) {
                removeCount = Math.max(start - rows.startIndex, 0);
                // We only have to bump the table down by the height of removed rows if rows are not a standard size
                if (variableRowHeight) {
                    increment = rows.item(rows.startIndex + removeCount, true).offsetTop;
                }
                newRows = rows.scroll(Ext.Array.slice(range, rows.endIndex + 1 - start), 1, removeCount);
				//start chrome fix
                view.el.dom.scrollTop = me.scrollTop;
				//end chrome fix
				
                // We only have to bump the table down by the height of removed rows if rows are not a standard size
                if (variableRowHeight) {
                    // Bump the table downwards by the height scraped off the top
                    newTop = me.bodyTop + increment;
                } else // If the rows are standard size, then the calculated top will be correct
                {
                    newTop = calculatedTop;
                }
            } else // Moved up the dataset: remove rows from end, add to top
            {
                removeCount = Math.max(rows.endIndex - end, 0);
                oldStart = rows.startIndex;
                newRows = rows.scroll(Ext.Array.slice(range, 0, rows.startIndex - start), -1, removeCount);
				//start chrome fix
                view.el.dom.scrollTop = me.scrollTop;
				//end chrome fix
				
                // We only have to bump the table up by the height of top-added rows if rows are not a standard size
                if (variableRowHeight) {
                    // Bump the table upwards by the height added to the top
                    newTop = me.bodyTop - rows.item(oldStart, true).offsetTop;
                    // We've arrived at row zero...
                    if (!rows.startIndex) {
                        // But the calculated top position is out. It must be zero at this point
                        // We adjust the scroll position to keep visual position of table the same.
                        if (newTop) {
                            scroller.scrollTo(null, me.position = (me.scrollTop -= newTop));
                            newTop = 0;
                        }
                    }
                    // Not at zero yet, but the position has moved into negative range
                    else if (newTop < 0) {
                        increment = rows.startIndex * me.rowHeight;
                        scroller.scrollTo(null, me.position = (me.scrollTop += increment));
                        newTop = me.bodyTop + increment;
                    }
                } else // If the rows are standard size, then the calculated top will be correct
                {
                    newTop = calculatedTop;
                }
            }
            // The position property is the scrollTop value *at which the table was last correct*
            // MUST be set at table render/adjustment time
            me.position = me.scrollTop;
        }
        // We contained focus at the start, check whether activeEl has been derendered.
        // Focus the cell's column header if so.
        if (containsFocus) {
            // Restore active element's focus processing.
            activeEl.resumeFocusEvents();
            if (!viewEl.contains(activeEl)) {
                pos = view.actionableMode ? view.actionPosition : view.lastFocused;
                if (pos && pos.column) {
                    // we set the rendering rows to true here so the actionables know
                    // that view is forcing the onFocusLeave method here
                    view.renderingRows = true;
                    view.onFocusLeave({});
                    view.renderingRows = false;
                    // Try to focus the contextual column header.
                    // Failing that, look inside it for a tabbable element.
                    // Failing that, focus the view.
                    // Focus MUST NOT just silently die due to DOM removal
                    if (pos.column.focusable) {
                        newFocus = pos.column;
                    } else {
                        newFocus = pos.column.el.findTabbableElements()[0];
                    }
                    if (!newFocus) {
                        newFocus = view.el;
                    }
                    newFocus.focus();
                }
            }
        }
        // Position the item container.
        newTop = Math.max(Math.floor(newTop), 0);
        if (view.positionBody) {
            me.setBodyTop(newTop);
        }
        // Sync the other side to exactly the same range from the dataset.
        // Then ensure that we are still at exactly the same scroll position.
        if (newRows && lockingPartner && !lockingPartner.disabled) {
            // Set the pointers of the partner so that its onRangeFetched believes it is at the correct position.
            lockingPartner.scrollTop = lockingPartner.position = me.scrollTop;
            if (lockingPartner.view.ownerCt.isVisible()) {
                partnerNewRows = lockingPartner.onRangeFetched(range, start, end, options, true);
                // Sync the row heights if configured to do so, or if one side has variableRowHeight but the other doesn't.
                // variableRowHeight is just a flag for the buffered rendering to know how to measure row height and
                // calculate firstVisibleRow and lastVisibleRow. It does not *necessarily* mean that row heights are going
                // to be asymmetric between sides. For example grouping causes variableRowHeight. But the row heights
                // each side will be symmetric.
                // But if one side has variableRowHeight (eg, a cellWrap: true column), and the other does not, that
                // means there could be asymmetric row heights.
                if (view.ownerGrid.syncRowHeight || view.ownerGrid.syncRowHeightOnNextLayout || (lockingPartner.variableRowHeight !== variableRowHeight)) {
                    me.syncRowHeights(newRows, partnerNewRows);
                    view.ownerGrid.syncRowHeightOnNextLayout = false;
                }
            }
            if (lockingPartner.bodyTop !== newTop) {
                lockingPartner.setBodyTop(newTop);
            }
            // Set the real scrollY position after the correct data has been rendered there.
            // It will not handle a scroll because the scrollTop and position have been preset.
            lockingPartner.scroller.scrollTo(null, me.scrollTop);
        }
        // If there's variableRowHeight and the scroll operation did affect that, remeasure now.
        // We must do this because the RowExpander and RowWidget plugin might make huge differences
        // in rowHeight, so we might scroll from a zone full of 200 pixel hight rows to a zone of
        // all 21 pixel high rows.
        if (me.variableRowHeight && me.bodyHeight !== oldBodyHeight && view.componentLayoutCounter === layoutCount) {
            delete me.rowHeight;
            me.refreshSize();
        }
        // If there are columns to trigger rendering, and the rendered block os not either the view size
        // or, if store count less than view size, the store count, then there's a bug.
        if (view.getVisibleColumnManager().getColumns().length && rows.getCount() !== Math.min(me.store.getCount(), me.viewSize)) {
			//switch raise to console log to avoid complete interface break
            console.log('rendered block refreshed at ' + rows.getCount() + ' rows while BufferedRenderer view size is ' + me.viewSize);
        }
        return newRows;
    }
});
Ext.override(Ext.Date, {
	
	/**
	 * Backported from ExtJs 6.6.0
	 */
	add: function(date, interval, value, preventDstAdjust) {
		var XDate = Ext.Date,
				d = XDate.clone(date),
				base = 0,
				day, decimalValue;
		
		decimalValue = value - parseInt(value, 10);
		value = parseInt(value, 10);

		if (value) {
			switch (interval.toLowerCase()) {
				// See EXTJSIV-7418. We use setTime() here to deal with issues related to 
				// the switchover that occurs when changing to daylight savings and vice 
				// versa. setTime() handles this correctly where setHour/Minute/Second/Millisecond 
				// do not. Let's assume the DST change occurs at 2am and we're incrementing using add 
				// for 15 minutes at time. When entering DST, we should see: 
				// 01:30am 
				// 01:45am 
				// 03:00am // skip 2am because the hour does not exist 
				// ... 
				// Similarly, leaving DST, we should see: 
				// 01:30am 
				// 01:45am 
				// 01:00am // repeat 1am because that's the change over 
				// 01:30am 
				// 01:45am 
				// 02:00am 
				// .... 
				//  
				case XDate.MILLI:
					if (preventDstAdjust) {
						d.setMilliseconds(d.getMilliseconds() + value);
					} else {
						d.setTime(d.getTime() + value);
					}
					break;
				case XDate.SECOND:
					if (preventDstAdjust) {
						d.setSeconds(d.getSeconds() + value);
					} else {
						d.setTime(d.getTime() + value * 1000);
					}
					break;
				case XDate.MINUTE:
					if (preventDstAdjust) {
						d.setMinutes(d.getMinutes() + value);
					} else {
						d.setTime(d.getTime() + value * 60 * 1000);
					}
					break;
				case XDate.HOUR:
					if (preventDstAdjust) {
						d.setHours(d.getHours() + value);
					} else {
						d.setTime(d.getTime() + value * 60 * 60 * 1000);
					}
					break;
				case XDate.DAY:
					if (preventDstAdjust) {
						d.setDate(d.getDate() + value);
					} else {
						d.setTime(d.getTime() + value * 24 * 60 * 60 * 1000);
					}
					break;
				case XDate.MONTH:
					day = date.getDate();
					if (day > 28) {
						day = Math.min(day, XDate.getLastDateOfMonth(XDate.add(XDate.getFirstDateOfMonth(date), XDate.MONTH, value)).getDate());
					}
					d.setDate(day);
					d.setMonth(date.getMonth() + value);
					break;
				case XDate.YEAR:
					day = date.getDate();
					if (day > 28) {
						day = Math.min(day, XDate.getLastDateOfMonth(XDate.add(XDate.getFirstDateOfMonth(date), XDate.YEAR, value)).getDate());
					}
					d.setDate(day);
					d.setFullYear(date.getFullYear() + value);
					break;
			}
		}

		if (decimalValue) {
			switch (interval.toLowerCase()) {
				case XDate.MILLI:
					base = 1;
					break;
				case XDate.SECOND:
					base = 1000;
					break;
				case XDate.MINUTE:
					base = 1000 * 60;
					break;
				case XDate.HOUR:
					base = 1000 * 60 * 60;
					break;
				case XDate.DAY:
					base = 1000 * 60 * 60 * 24;
					break;

				case XDate.MONTH:
					day = XDate.getDaysInMonth(d);
					base = 1000 * 60 * 60 * 24 * day;
					break;

				case XDate.YEAR:
					day = (XDate.isLeapYear(d) ? 366 : 365);
					base = 1000 * 60 * 60 * 24 * day;
					break;
			}
			if (base) {
				d.setTime(d.getTime() + base * decimalValue);
			}
		}

		return d;	
	},
	
	/**
	 * Backported from ExtJs 6.6.0
	 */
	subtract: function(date, interval, value, preventDstAdjust) {
		return Ext.Date.add(date, interval, -value, preventDstAdjust);
	}
});
Ext.override(Ext.ux.colorpick.Field, {
	/**
	 * Overrides default implementation of {@link Ext.ux.colorpick.Field#afterRender}.
	 */
	afterRender: function() {
		var me = this;
		me.callParent(arguments);
		me._applyInputColor(me.getValue());
	},
	
	/**
	 * Overrides default implementation of {@link Ext.ux.colorpick.Field#onChange}.
	 */
	onChange: function(newVal, oldVal) {
		var me = this;
		me.callParent(arguments);
		me._applyInputColor(newVal);
	},
	
	_applyInputColor: function(color) {
		var me = this,
				co = Ext.isEmpty(color) ? 'ffffff' : color;
		if (me.inputEl) {
			me.inputEl.applyStyles({
				backgroundColor: '#'+co,
				color: '#'+co
			});
		}
	}
});
