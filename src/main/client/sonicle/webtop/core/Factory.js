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
Ext.define('Sonicle.webtop.core.Factory', {
	singleton: true,
	alternateClassName: ['WT.Factory', 'WTF'],
	
	/*
	 * Builds the URL of a themed resource file (image, css, etc...) for a service.
	 * @param {String} sid The service ID.
	 * @param {String} relPath The relative resource path.
	 * @return {String} The URL
	 */
	resourceUrl: function(sid, relPath) {
		return Ext.String.format('resources/{0}/laf/{1}/{2}', sid, WT.getOption('laf'), relPath);
	},
	
	/*
	 * Builds the img HTML tag of a themed image for a service.
	 * @param {String} sid The service ID.
	 * @param {String} relPath The relative icon path.
	 * @param {int} width The icon width.
	 * @param {int} height The icon height.
	 * @param {String} [others] Other custom tag properties.
	 * @return {String} The complete image tag
	 */
	imageTag: function(sid, relPath, width, height, others) {
		var src = WTF.resourceUrl(sid,relPath);
		return Ext.String.format('<img src="{0}" width={1} height={2} {3} >', src, width, height, others||'');
	},
	
	/*
	 * Builds the URL of a global image.
	 * @param {String} relPath The relative resource path.
	 * @return {String} The URL
	 */
	globalImageUrl: function(relPath) {
		return Ext.String.format('resources/{0}/images/{1}', WT.ID, relPath);
	},
	
	/*
	 * Builds the img HTML tag of a themed core image.
	 * @param {String} relPath The relative icon path.
	 * @param {int} width The icon width.
	 * @param {int} height The icon height.
	 * @param {String} [others] Other custom tag properties.
	 * @return {String} The complete image tag
	 */
	globalImageTag: function(relPath,width,height,others) {
		var src = WTF.globalImageUrl(relPath);
		return Ext.String.format('<img src="{0}" width={1} height={2} {3} >', src, width, height, others||'');
	},
	
	/**
	 * Builds CSS class name namespacing it using service xid.
	 * @param {String} xid Service short ID.
	 * @param {String} name The CSS class name part.
	 * @return {String} The concatenated CSS class name.
	 */
	cssCls: function(xid, name) {
		return Ext.String.format('{0}-{1}', xid, name);
	},
	
	/**
	 * Builds CSS class name for icons namespacing it using service xid.
	 * For example, using 'service' as name, it will return '{xid}-icon-service'.
	 * Using 'service-l' as name it will return '{xid}-icon-service-l'.
	 * Likewise, using 'service' as name and 'l' as size it will return the
	 * same value: '{xid}-icon-service-l'.
	 * @param {String} xid Service short ID.
	 * @param {String} name The icon name part.
	 * @param {String} [size] Icon size (one of xs->16x16, s->24x24, m->32x32, l->48x48).
	 * @return {String} The concatenated CSS class name.
	 */
	cssIconCls: function(xid, name, size) {
		if(size === undefined) {
			return Ext.String.format('{0}-icon-{1}', xid, name);
		} else {
			return Ext.String.format('{0}-icon-{1}-{2}', xid, name, size);
		}
	},
	
	proxy: function(sid, act, rootp, opts) {
		opts = opts || {};
		return {
			type: 'ajax',
			url: 'service-request',
			extraParams: Ext.apply(opts.extraParams || {}, {
				service: sid,
				action: act
			}),
			reader: Ext.apply({
				type: 'json',
				rootProperty: rootp || 'data',
				messageProperty: 'message'
			}, opts.reader || {}),
			writer: Ext.apply({
				type: 'json'
			}, opts.writer || {}),
			listeners: {
				exception: function(proxy, request, operation, eOpts) {
					//TODO: localizzare il messaggio di errore
					WT.error('Error during action "'+act+'" on service "'+sid+'"',"Ajax Error");
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
				rootProperty: rootp || 'data',
				messageProperty: 'message'
			}, opts.reader || {}),
			writer: Ext.apply({
				type: 'json',
				writeAllFields: true
			}, opts.writer || {}),
			listeners: {
				exception: function(proxy, request, operation, eOpts) {
					//TODO: localizzare il messaggio di errore
					WT.error('Error during action "'+act+'" on service "'+svc+'"',"Ajax Error");
				}
			}
		};
	},
	
	/*
	 * Builds a service request URL, based on service ID, action and params.
	 * @param {String} sid The service ID
	 * @param {String} action The action to be called on service
	 * @param {Object} [params] Optional additional parameters that will be encoded into the URL
	 * @return {String} The encoded URL
	 */
	serviceRequestUrl: function(sid, action, params) {
		var url = "service-request?service="+sid+"&action="+action;
		if(params) url += "&"+Ext.Object.toQueryString(params);
		return url;
	},
	
	/*
	 * Build a service request URL, based on service ID, action and params, 
	 * adding the nowriter option into the params to allow for binary send of data.
	 * @param {String} sid The service ID
	 * @param {String} action The action to be called on service
	 * @param {Object} [params] Optional additional parameters that will be encoded into the URL
	 * @return {String} The encoded URL
	 */
	serviceRequestBinaryUrl: function(sid, action, params) {
		params = params || {};
		params.nowriter = true;
		return WTF.serviceRequestUrl(sid, action, params);
	},
	
	/**
	 * Helper method for building a {@link Ext.data.field.Field field} config.
	 * @param {String} name See {@link Ext.data.field.Field#name}
	 * @param {String} type See {@link Ext.data.field.Field#type}
	 * @param {Boolean} allowBlank If 'false' automatically adds the presence validator.
	 * @param {Object} [cfg] Custom config to apply.
	 * @returns {Object} The field config
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
	
	/**
	 * Helper method for building a calculated {@link Ext.data.field.Field field} config.
	 * @param {String} name See {@link Ext.data.field.Field#name}
	 * @param {String} type See {@link Ext.data.field.Field#type}
	 * @param {String/String[]} depends See {@link Ext.data.field.Field#depends}
	 * @param {Function} convert See {@link Ext.data.field.Field#convert}
	 * @param {Object} [cfg] Custom config to apply.
	 * @returns {Object} The field config
	 */
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
	
	/**
	 * Helper method for building a readOnly {@link Ext.data.field.Field field} config.
	 * @param {String} name See {@link Ext.data.field.Field#name}
	 * @param {String} type See {@link Ext.data.field.Field#type}
	 * @param {Object} [cfg] Custom config to apply.
	 * @returns {Object} The field config
	 */
	roField: function(name, type, cfg) {
		cfg = cfg || {};
		return Ext.apply({
			name: name,
			type: type,
			persist: false
		}, cfg);
	},
	
	localCombo: function(valueField, displayField, cfg) {
		cfg = cfg || {};
		return Ext.apply({
			xtype: 'combo',
			typeAhead: true,
			queryMode: 'local',
			forceSelection: true,
			selectOnFocus: true,
			triggerAction: 'all',
			valueField: valueField,
			displayField: displayField,
			submitEmptyText: false
		}, cfg);
	},
	
	remoteCombo: function(valueField, displayField, cfg) {
		cfg = cfg || {};
		return Ext.apply({
			xtype: 'combo',
			typeAhead: true,
			queryMode: 'remote',
			minChars: 2,
			forceSelection: true,
			selectOnFocus: true,
			triggerAction: 'all',
			valueField: valueField,
			displayField: displayField,
			submitEmptyText: false
		}, cfg);
	},
	
	clearTrigger: function(cfg) {
		cfg = cfg || {};
		return Ext.apply({
			type: 'clear',
			weight: -1,
			hideWhenEmpty: true,
			hideWhenMouseOut: true
		}, cfg);
	},
	
	/**
	 * Configures a renderer for looking-up columns value from a resource.
	 * Resource key will be the result of the following concatenation: '{key}.{value}'
	 * @param {Object} cfg Custom configuration object
	 * @param {String} cfg.id The service ID.
	 * @param {String} cfg.key The resource key.
	 * @returns {Function} The renderer function
	 */
	resColRenderer: function(cfg) {
		cfg = cfg || {};
		return function(value) {
			return WT.res(cfg.id, cfg.key + '.' + value);
		};
	},
	
	/**
	 * Configures a renderer for setting tdCls meta property.
	 * Class name will be the result of the following concatenation: '{clsPrefix}{value}'
	 * @param {Object} cfg Custom configuration object
	 * @param {String} [cfg.fieldName] Specifies the field from which getting value instead of current one.
	 * @param {String} [cfg.clsPrefix] Specifies the prefix to prepend to value. Defaults to ''.
	 * @param {String} [cfg.moreCls]
	 * @returns {Function} The renderer function
	 */
	clsColRenderer: function(cfg) {
		cfg = cfg || {};
		return function(value,meta,rec) {
			var val = (cfg.fieldName) ? rec.get(cfg.fieldName) : value,
					prefixed = (cfg.clsPrefix) ? cfg.clsPrefix + val : val,
					more = (cfg.moreCls) ? ' ' + cfg.moreCls : '';
			meta.tdCls = prefixed + more;
			return '';
		};
	},
	
	/**
	 *
	 * @param {Integer} size The icon size in pixels.
	 * @param {String} classPrefix A prefix to append before value returned by classField.
	 * @param {String} classField A field for getting css class value.
	 * @param {String} tooltipField A field for getting tooltip value.
	 * @return {Function} The icon redering function.
	 */
	/*iconRenderer: function(size, classPrefix, classField, tooltipField) {
		classPrefix = classPrefix || '';
		return function(v,md,rec) {
			var ttip = (tooltipField) ? rec.get(tooltipField) : '';
			var cls = (rec.get(classField)) ? classPrefix+rec.get(classField) : '';
			//if(cls && classPrefix) cls = classPrefix+cls;
			return '<div title="'+ttip+'" class="'+cls+'" style="width:'+size+'px;height:'+size+'px" />';
		};
	},
	*/
	
	/**
	 * Helper method for defining a {@link Ext.app.bind.Formula} that is able   
	 * to perform a two-way binding between checkbox and a boolean field.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * @param {String} fieldName Model's field name.
	 * @returns {Object} Formula configuration object
	 */
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
	
	/**
	 * Helper method for defining a {@link Ext.app.bind.Formula} that is able   
	 * to perform a two-way binding between checkboxgroup and a model's field.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * @param {String} fieldName Model's field name.
	 * @param {String} [objProp] Property name to set into value object. Defaults to fieldName.
	 * @returns {Object} Formula configuration object
	 */
	checkboxGroupBind: function(modelProp, fieldName, objProp) {
		return {
			bind: {bindTo: '{'+modelProp+'.'+fieldName+'}'},
			get: function(val) {
				var ret = {};
				ret[objProp || fieldName] = val;
				return ret;
			},
			set: function(val) {
				this.get(modelProp).set(fieldName, val[objProp || fieldName]);
			}
		};
	},
	
	/**
	 * Helper method for defining a {@link Ext.app.bind.Formula} that is able   
	 * to perform a two-way binding between radiogroup and a model's field.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * @param {String} fieldName Model's field name.
	 * @param {String} [objProp] Property name to set into value object. Defaults to fieldName.
	 * @returns {Object} Formula configuration object
	 */
	radioGroupBind: function(modelProp, fieldName, objProp) {
		return {
			bind: {bindTo: '{'+modelProp+'.'+fieldName+'}'},
			get: function(val) {
				var ret = {};
				ret[objProp || fieldName] = val;
				return ret;
			},
			set: function(val) {
				this.get(modelProp).set(fieldName, val[objProp || fieldName]);
			}
		};
	},
	
	/**
	 * Helper method for defining a {@link Ext.app.bind.Formula} that checks 
	 * equality between a model's field and passed value.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * @param {String} fieldName Model's field name.
	 * @param {Mixed} equalsTo Value to match.
	 * @returns {Object} Formula configuration object
	 */
	equalsFormula: function(modelProp, fieldName, equalsTo) {
		return {
			bind: {bindTo: '{'+modelProp+'.'+fieldName+'}'},
			get: function(val) {
				return (val === equalsTo);
			}
		};
	}
	
	
	/*
	wsMsg: function(service, action, config) {
		return Ext.JSON.encode(Ext.apply(config||{},{ service: service, action: action }));
	},
	*/
});
