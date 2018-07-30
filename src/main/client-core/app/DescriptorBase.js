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
Ext.define('Sonicle.webtop.core.app.DescriptorBase', {
	id: null,
	xid: null,
	ns: null,
	path: null,
	name: null,
	description: null,
	version: null,
	maintenance: null,
	order: null,
	company: null,
	
	instance: null,
	initCalled: false,
	
	constructor: function(cfg) {
		var me = this;
		if (!Ext.isDefined(cfg.id)) Ext.raise('Missing config [id]');
		if (!Ext.isDefined(cfg.xid)) Ext.raise('Missing config [xid]');
		if (!Ext.isDefined(cfg.ns)) Ext.raise('Missing config [ns]');
		if (!Ext.isDefined(cfg.path)) Ext.raise('Missing config [path]');
		if (!Ext.isDefined(cfg.path)) Ext.warn('Missing config [name]');
		if (!Ext.isDefined(cfg.version)) Ext.raise('Missing config [version]');
		me.initConfig(cfg);
		me.callParent([cfg]);
	},
	
	getId: function() {
		return this.id;
	},
	
	getXid: function() {
		return this.xid;
	},
	
	getNs: function() {
		return this.ns;
	},
	
	getPath: function() {
		return this.path;
	},
	
	getName: function() {
		return this.name;
	},
	
	getDescription: function() {
		return this.description;
	},
	
	getVersion: function() {
		return this.version;
	},
	
	getMaintenance: function() {
		return this.maintenance;
	},
	
	/*
	setMaintenance: function(value) {
		this.maintenance = value;
	},
	*/
	
	getOrder: function() {
		return this.order;
	},
	
	setOrder: function(value) {
		this.order = value;
	},
	
	getCompany: function() {
		return this.company;
	},
	
	preNs: function(cn) {
		return WT.preNs(this.ns, cn);
	},
	
	getInstance: function(create) {
		if (!Ext.isDefined(create)) create = true;
		var me = this;
		if (create && !me.instance) {
			me.createAndSetInstance();
		}
		return me.instance;
	},
	
	isServiceInited: function() {
		return this.initCalled;
	},
	
	initService: function() {
		var me = this, inst;
		if (!me.initCalled) {
			WTA.Log.debug('Initializing service [{0}]', me.getId());
			inst = me.getInstance();
			if (!inst) return false;
			
			try {
				me.doInstanceInit(inst);
				me.initCalled = true;
			} catch(e) {
				WTA.Log.error('Error while initializing instance');
				WTA.Log.exception(e);
				return false;
			}
		}
		return true;
	},
	
	createAndSetInstance: function() {
		Ext.raise('If you see this there is something wrong. This method must be overridden!');
	},
	
	doInstanceInit: function(inst) {
		inst.init.call(inst);
	}
});
