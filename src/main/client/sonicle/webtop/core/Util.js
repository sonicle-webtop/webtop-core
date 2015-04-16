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
Ext.define('Sonicle.webtop.core.Util', {
	singleton: true,
	alternateClassName: 'WT.Util',
	
	applyTbItems: function(obj, dock, items, append) {
		if(append === undefined) append = true;
		var me = this,
				prop = me._dockToProp(dock),
				bar = obj[prop];
		
		if(!prop) Ext.Error.raise('Please specify a valid docking property');
		
		if(Ext.isArray(bar)) {
			if(append) {
				obj[prop] = bar.concat(items);
			} else {
				obj[prop] = items.concat(bar);
			}
		} else if(Ext.isObject(bar)) {
			if(Ext.isArray(bar.items)) {
				if(append) {
					obj[prop].items = bar.items.concat(items);
				} else {
					obj[prop].items = items.concat(bar.items);
				}
			}
		} else if(bar && bar.isToolbar) {
			if(append) {
				obj[prop].items.addAll(items);
			} else {
				obj[prop].items.insert(0, items);
			}
		} else {
			obj[prop] = items;
		}
	},
	
	_dockToProp: function(dock) {
		if(dock === 'top') return 'tbar';
		if(dock === 'bottom') return 'fbar';
		if(dock === 'left') return 'lbar';
		if(dock === 'right') return 'rbar';
		return null;
	},
	
	getCheckedRadioUsingDOM: function(keys) {
		var checked = null;
		Ext.iterate(keys, function(key) {
			if((checked === null) && (Ext.get(key).dom.checked === true)) checked = key;
		});
		return checked;
	},
	
	/**
	 * Find a specific {@link Ext.form.field.Field field} in passed {@link Ext.form.Panel form}.
	 * @param {Ext.form.Panel} form The form panel.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @returns {Ext.form.field.Field} The first matching field, or `null` if none was found.
	 */
	getField: function(form, id) {
		return form.getForm().findField(id);
	},
	
	/**
	 * Gets value of a specific {@link Ext.form.field.Field field} in passed {@link Ext.form.Panel form}.
	 * @param {Ext.form.Panel} form The form panel.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @return {Mixed} Field value.
	 */
	getFieldValue: function(form, id) {
		var fld = this.getField(form, id);
		if(!fld) return undefined;
		if (fld.isXType('radiogroup')) {
			return fld.getValue().value;
		} else {
			return fld.getValue();
		}
	},
	
	/**
	 * Sets value for a specific {@link Ext.form.field.Field field} in passed {@link Ext.form.Panel form}.
	 * @param {Ext.form.Panel} form The form panel.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @param {Mixed} value The field value.
	 */
	setFieldValue: function(form, id, value, silent) {
		silent = silent || false;
		var fld = this.getField(form, id);
		if(silent && !fld) return;
		fld.setValue(value);
	},
	
	/**
	 * Checks if a specific {@link Ext.form.field.Field field} in passed {@link Ext.form.Panel form} is empty.
	 * @param {Ext.form.Panel} form The form panel.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @return {Bolean}
	 */
	isFieldEmpty: function(form, id) {
		return Ext.isEmpty(this.getFieldValue(form, id));
	},
	
	/**
	 * Focus a specific {@link Ext.form.field.Field field} in passed {@link Ext.form.Panel form}.
	 * @param {Ext.form.Panel} form The form panel.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 */
	focusField: function(form, id) {
		var fld = this.getField(form, id);
		if(fld) fld.focus();
	},
	
	proxy: function(svc, act, rootp, opts) {
		opts = opts || {};
		return {
			type: 'ajax',
			url: 'service-request',
			extraParams: Ext.apply(opts.extraParams || {}, {
				service: svc,
				action: act
			}),
			reader: Ext.apply(opts.reader || {}, {
				type: 'json',
				rootProperty: rootp || 'data',
				messageProperty: 'message'
			}),
			listeners: {
				exception: function(proxy, request, operation, eOpts) {
					//TODO: intl. user error message plus details
					WT.error('Error during action "'+act+'" on service "'+svc+'"',"Ajax Error");
				}
			}
		};
	},
	
	apiProxy: function(svc, act, rootp, opts) {
		rootp = rootp || 'data';
		opts = opts || {};
		return {
			type: 'ajax',
			api: {
				create: 'service-request?crud=create',
				read: 'service-request?crud=read',
				update: 'service-request?crud=update',
				destroy: 'service-request?crud=delete'
			},
			extraParams: Ext.apply(opts.extraParams || {}, {
				service: svc,
				action: act
			}),
			reader: Ext.apply({
				type: 'json',
				rootProperty: rootp,
				messageProperty: 'message'
			}, opts.reader || {}),
			writer: Ext.apply({
				type: 'json',
				writeAllFields: true
			}, opts.writer || {})
		};
	},
	
	/**
	 * Helper method for building a {@link Ext.data.field.Field field} config
	 * @param {String} name Field {@link Ext.data.field.Field#name}.
	 * @param {String} type Field {@link Ext.data.field.Field#type}.
	 * @param {type} allowBlank If 'false' automatically adds the presence validator.
	 * @param {Object} [cfg] Custom config to apply.
	 * @returns {Object}
	 */
	field: function(name, type, allowBlank, cfg) {
		cfg = cfg || {};
		var validators = [];
		if(!allowBlank) validators.push('presence');
		cfg.validators = (Ext.isArray(cfg.validators)) ? Ext.Array.push(cfg.validators, validators) : validators;
		
		return Ext.apply({
			name: name,
			type: type,
			allowNull: true
		}, cfg);
	},
	
	calcField: function(name, type, depends, convert, cfg) {
		cfg = cfg || {};
		return Ext.apply({
			name: name,
			type: type,
			persist: false,
			depends: depends,
			convert: convert
		}, cfg);
	},
	
	/*
	 * Builds the src url of a themed image for a service
	 * @param {String} sid The service id
	 * @param {String} relPath The relative icon path
	 * @return {String} the imageUrl
	 */
	imageUrl: function(sid, relPath) {
		return Ext.String.format('resources/{0}/laf/{1}/{2}', sid, WT.getOption('laf'), relPath);
	},
	
	/*
	 * Builds the img tag of a themed image for a service.
	 * @param {String} sid The service id
	 * @param {String} relPath The relative icon path
	 * @param {int} width The icon width
	 * @param {int} height The icon height
	 * @param {String} [others] other custom tag properties
	 * @return {String} the complete image tag
	 */
	imageTag: function(sid,relPath,width,height,others) {
		var src=this.imageUrl(sid,relPath);
		return Ext.String.format('<img src="{0}" width={1} height={2} {3} >',src,width,height,others||'');
	},
	
	/*
	 * Builds the src url of a global image
	 * @param {String} relPath The relative icon path
	 * @return {String} the imageUrl
	 */
	globalImageUrl: function(relPath) {
		return Ext.String.format('resources/{0}/images/{1}',WT.ID,relPath);
	},
	
	/*
	 * Builds the img tag of a core generic image
	 * @param {String} relPath The relative icon path
	 * @param {int} width The icon width
	 * @param {int} height The icon height
	 * @param {String} [others] other custom tag properties
	 * @return {String} the complete image tag
	 */
	globalImageTag: function(relPath,width,height,others) {
		var src=this.globalImageUrl(relPath);
		return Ext.String.format('<img src="{0}" width={1} height={2} {3} >',src,width,height,others||'');
	},
	
	/*
	 * Check filename extension trying to guess a known file type name
	 * @param {String} filename The file name
	 * @return {String} The recognized file type name, as in WT.filtypes, "bin" if not
	 */
	normalizeFileType: function(filename) {
		var ix=filename.lastIndexOf('.'),
			extension="bin";
	
		if (ix>=0) extension=filename.substring(ix+1).toLowerCase();
		var ft=WT.filetypes;
		var typename=ft[extension];
		if (!typename) typename=ft["bin"];
		return typename;
	},
	
	/*
	 * Format a human readble version of the passed size argument (xKB,xMB)
	 * @param {int} value The value of size to be formatted
	 * @return {String} The value if less than 1024, else xKB, xMB
	 */
	humanReadableSize: function(value) {
		var s = value;
		value = parseInt(value/1024);
		if (value>0) {
			if (value<1024) s = value+"KB";
			else s = parseInt(value/1024)+"MB";
		}
		return s;
	},
	
	/*
	 * Build a service request url, based on sid, action and params,
	 * usable in href or locations
	 * @param {String} sid The service id
	 * @param {String} action The action to be called on service
	 * @param {Object} [params] Optional additional parameters encoded into the url
	 * @return {String} The encoded url
	 */
	serviceRequestUrl: function(sid,action,params) {
		var url = "service-request?service="+sid+"&action="+action;
		if (params) url += "&"+Ext.Object.toQueryString(params);
		return url;
	},
	
	/*
	 * Build a service request url, based on sid, action and params, adding the
	 * nowriter option into the params to allow for binary send of data,
	 * usable in href or locations
	 * @param {String} sid The service id
	 * @param {String} action The action to be called on service
	 * @param {Object} [params] Optional additional parameters encoded into the url
	 * @return {String} The encoded url
	 */
	serviceRequestBinaryUrl: function(sid,action,params) {
		params = params || {};
		params.nowriter=true;
		return this.serviceRequestUrl(sid,action,params);
	},
	
	/*
	 * Build a data object with iframe data, using cross-browser code
	 * @param {String} iframename The name of the iframe
	 * @return {Object} A data object with doc and iframe properties
	 */
	getIFrameData: function(iframename) {
		var data={ doc: null, iframe: document.getElementById(iframename)};
		if (Ext.isIE) {
			if (window.frames[iframename]) data.doc=window.frames[iframename].document;
		} else {
			data.doc=data.iframe.contentDocument;
		}
		return data;
	},
	
	checkboxBind: function(modelProp, fieldName) {
		return {
			bind: {bindTo: '{'+modelProp+'.'+fieldName+'}'},
			get: function(val) {
				return val;
			},
			set: function(val) {
				this.get(modelProp).set(fieldName, val);
			}
		};
	},
	
	checkboxGroupBind: function(modelProp, fieldName) {
		return {
			bind: {bindTo: '{'+modelProp+'.'+fieldName+'}'},
			get: function(val) {
				var ret = {};
				ret[fieldName] = val;
				return ret;
			},
			set: function(val) {
				this.get(modelProp).set(fieldName, val[fieldName]);
			}
		};
	},
	
	equalsFormula: function(modelProp, fieldName, equalsTo) {
		return {
			bind: {bindTo: '{'+modelProp+'.'+fieldName+'}'},
			get: function(val) {
				return (val === equalsTo);
			}
		};
	}
	
	
});
