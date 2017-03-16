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
	
	baseUrl: null,
	
	wsPushUrl: null,
	
	/**
	 * @property {Ext.util.HashMap} locales
	 * A collection of locale classes.
	 */
	locales: null,
	
	/**
	 * @property {Ext.util.Collection} services
	 * A collection of service descriptors.
	 */
	services: null,
	
	constructor: function() {
		var me = this;
		WT.app = me;
		me.platformName = WTS.platformName;
		me.baseUrl = WTS.baseUrl;
		me.wsPushUrl = WTS.wsPushUrl;
		me.locales = Ext.create('Ext.util.HashMap');
		me.services = Ext.create('Ext.util.Collection');
		me.callParent(arguments);
	},
	
	/**
	 * Returns desired locale instance.
	 * @param {String} id The service ID.
	 * @returns {WT.Locale}
	 */
	getLocale: function(id) {
		return this.locales.get(id);
	},
	
	/**
	 * Returns loaded service descriptors.
	 * @param {Boolean} [skip] False to include core descriptor. Default to true.
	 * @returns {WTA.ServiceDescriptor[]}
	 */
	getDescriptors: function(skip) {
		if(!Ext.isDefined(skip)) skip = true;
		var ret = [];
		this.services.each(function(desc) {
			if(!skip || (desc.getIndex() !== 0)) { // Skip core descriptor at index 0
				Ext.Array.push(ret, desc);
			}
		});
		return ret;
	},
	
	/**
	 * Returns a service descriptor.
	 * @param {String} id The service ID.
	 * @returns {WTA.ServiceDescriptor} The instance or undefined if not found. 
	 */
	getDescriptor: function(id) {
		return this.services.get(id);
	},
	
	/**
	 * Returns a service instance.
	 * @param {String} id The service ID.
	 * @returns {WTA.sdk.Service} The instance or null if not found. 
	 */
	getService: function(id) {
		var desc = this.getDescriptor(id);
		return (desc) ? desc.getInstance() : null;
	},
	
	/**
	 * Returns a service version.
	 * @param {String} id The service ID.
	 * @returns {String} The version string. 
	 */
	getServiceVersion: function(id) {
		var desc = this.getDescriptor(id);
		return (desc) ? desc.getVersion() : "0.0.0";
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