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
	services2: null,
	
	/**
	 * @property {Objects} roles
	 * A map of assigned roles.
	 */
	roles: null,
	
	constructor: function() {
		var me = this;
		WT.app = me;
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
				company: obj.company,
				version: obj.version,
				build: obj.build
			});
			me.descriptors.add(sid, desc);
		});
	},
	
	createServiceDescriptor: function(cfg) {
		Ext.raise('If you see this there is something wrong. This method must be overridden!');
	},
	
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
