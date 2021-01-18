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
Ext.define('Sonicle.webtop.core.app.Factory', {
	singleton: true,
	alternateClassName: ['WTA.Factory', 'WTF'],
	requires: [
		'Sonicle.String',
		'Sonicle.Data'
	],
	
	/**
	 * Builds base url for requests.
	 * @param {Object} [params] Optional additional parameters that will be encoded into the URL
	 * @returns {String} The encoded URL
	 */
	requestBaseUrl: function(params) {
		var ispub = WTS.appType === 'public',
				url = (ispub ? 'public' : 'service-request') + '?csrf=' + WTS.securityToken;
		//var url = Ext.String.format('service-request?csrf={0}', WTS.securityToken);
		return (params) ? Ext.String.urlAppend(url, Ext.Object.toQueryString(params)) : url;
	},
	
	/**
	 * Builds params object for a service request.
	 * @param {String} sid The service ID
	 * @param {String} action The action to be called on service
	 * @returns {Object} The params object
	 */
	processParams: function(sid, act) {
		if(WTS.appType === 'public') {
			return {
				action: act
			};
		} else {
			return {
				service: sid,
				action: act
			};
		}
	},
	
	/*
	 * Builds a service request URL, based on service ID, action and params.
	 * @param {String} sid The service ID
	 * @param {String} action The action to be called on service
	 * @param {Object} [params] Optional additional parameters that will be encoded into the URL
	 * @return {String} The encoded URL
	 */
	processUrl: function(sid, action, params) {
		/*
		var url = Ext.String.format('service-request?service={0}&action={1}', sid, action);
		if(params) url = Ext.String.urlAppend(url, Ext.Object.toQueryString(params));
		return url;
		*/
		
		var pars = Ext.apply({
			service: sid,
			action: action
		}, params || {});
		return WTF.requestBaseUrl(pars);
	},
	
	/*
	 * Build a service request URL, based on service ID, action and params, 
	 * adding the nowriter option into the params to allow for binary send of data.
	 * @param {String} sid The service ID
	 * @param {String} action The action to be called on service
	 * @param {Object} [params] Optional additional parameters that will be encoded into the URL
	 * @return {String} The encoded URL
	 */
	processBinUrl: function(sid, action, params) {
		//Do note change parameters order, needed by message editor cids management
		var pars = Ext.apply({
			service: sid,
			action: action,
			nowriter: true
		}, params || {});
		return WTF.requestBaseUrl(pars);
	},
	
	/*
	 * Builds the URL of a resource file for a service.
	 * @param {String} sid The service ID.
	 * @param {String} relPath The relative resource path.
	 * @return {String} The URL
	 */
	fileUrl: function(sid, relPath) {
		return Ext.String.format('resources/{0}/{1}', sid, relPath);
	},
	
	/*
	 * Builds the URL of a themed resource file (image, css, etc...) for a service.
	 * @param {String} sid The service ID.
	 * @param {String} relPath The relative resource path.
	 * @return {String} The URL
	 */
	resourceUrl: function(sid, relPath) {
		return 'resources/'+sid+'/'+WT.getApp().getServiceVersion(sid)+'/laf/'+WT.getVar('laf')+'/'+relPath;
		//return Ext.String.format('resources/{0}/laf/{1}/{2}', sid, WT.getVar('laf'), relPath);
	},
	
	/*
	 * Builds the URL of a global image.
	 * @param {String} relPath The relative resource path.
	 * @return {String} The URL
	 */
	globalImageUrl: function(relPath) {
		return 'resources/'+WT.ID+'/'+WT.getApp().getServiceVersion(WT.ID)+'/resources/images/'+relPath;
		//return Ext.String.format('resources/{0}/images/{1}', WT.ID, relPath);
	},
	
	/**
	 * Builds the URL of the fileType image.
	 * @param {String} sid The service ID.
	 * @param {String} [size] Icon size (one of xs->16x16, s->24x24, m->32x32, l->48x48).
	 * @param {String} ext The file extension.
	 * @returns {String} The fileType image URL
	 */
	fileTypeImageUrl: function(sid, ext, size) {
		var ftype = WTA.FileTypes.getFileType(ext),
				px = WTU.imgSizeToPx(size || 'xs');
		return this.resourceUrl(sid, 'filetypes/'+ftype+'_'+px+'.png');
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
		return xid+'-'+name;
		//return Ext.String.format('{0}-{1}', xid, name);
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
		var sz = '';
		if (Ext.isString(size)) {
			sz = '-' + size;
		}
		return xid + '-icon-' + name + sz;
	},
	
	fileTypeCssIconCls: function(ext, size) {
		var ftype = WTA.FileTypes.getFileType(ext),
				sz = '';
		if (Ext.isString(size)) {
			sz = '-' + size;
		}
		return WT.XID + '-ftype-' + ftype + sz;
	},
	
	headerWithGlyphIcon: function(iconCls) {
		return '<i class="'+iconCls+'" aria-hidden="true">\u00a0</i>';
	},
	
	/**
	 * Returns a gridColumn's header properly configured for displaying an icon.
	 * @param {type} iconCls
	 * @returns {String}
	 */
	headerWithIcon: function(iconCls) {
		return '<i class="wt-grid-header-icon '+iconCls+'">\u00a0\u00a0\u00a0\u00a0\u00a0</i>';
	},
		
	/**
	 * Helper method for building a config object for a generic {@link Ext.data.proxy.Ajax proxy}.
	 * @param {String} sid The service ID.
	 * @param {String} act The action name.
	 * @param {String} [rootp] The property that contains data items corresponding to the Model.
	 * @param {Object} [opts] Config options.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {Object} [opts.timeout] The number of milliseconds to wait for a response. Defaults to {@link Ext.data.proxy.Server#timeout}.
	 * @param {Boolean} [opts.autoAbort] Whether this request should abort any other previous pending requests. Defaults to `false`.
	 * @param {String} [opts.model] The name of the Model to tie to this Proxy.
	 * @param {Object} [opts.extraParams] Extra request params.
	 * @param {Object} [opts.reader]
	 * @param {Object} [opts.writer]
	 * @returns {Object} Proxy config object
	 */
	proxy: function(sid, act, rootp, opts) {
		opts = opts || {};
		var obj = {};
		if (opts.timeout) Ext.apply(obj, {timeout: opts.timeout});
		if (opts.autoAbort) Ext.apply(obj, {autoAbort: true});
		if (opts.model) Ext.apply(obj, {model: opts.model});
		return Ext.apply({
			type: 'soajax',
			url: WTF.requestBaseUrl(),
			extraParams: Ext.apply(opts.extraParams || {}, {
				service: sid,
				action: act
			}),
			reader: Ext.apply({
				type: 'json',
				keepRawData: true,
				rootProperty: rootp || 'data',
				messageProperty: 'message'
			}, opts.reader || {}),
			writer: Ext.apply({
				type: 'json'
			}, opts.writer || {}),
			listeners: Ext.merge({
				metachange: function(s, meta) {
					if (meta.error) WT.handleRequestMetaError(meta.error);
				},
				exception: function(s, req, op, eop) {
					WT.handleRequestError(sid, act, req, op);
				}
			}, opts.listeners || {})
		}, obj);
	},
	
	/**
	 * Helper method for building a config object for a {@link Ext.data.proxy.Ajax proxy} that reads Json data.
	 * @param {String} sid The service ID.
	 * @param {String} act The action name.
	 * @param {String} [rootp] The property that contains data items corresponding to the Model.
	 * @param {Object} [opts] Config options.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {Object} [opts.timeout] The number of milliseconds to wait for a response. Defaults to {@link Ext.data.proxy.Server#timeout}.
	 * @param {Boolean} [opts.autoAbort] Whether this request should abort any other previous pending requests. Defaults to `false`.
	 * @param {String} [opts.model] The name of the Model to tie to this Proxy.
	 * @param {Object} [opts.extraParams] Extra request params.
	 * @returns {Object} Proxy config object
	 */
	proxyReader: function(sid, act, rootp, opts) {
		opts = opts || {};
		var obj = {};
		if (opts.timeout) Ext.apply(obj, {timeout: opts.timeout});
		if (opts.autoAbort) Ext.apply(obj, {autoAbort: true});
		if (opts.model) Ext.apply(obj, {model: opts.model});
		return Ext.apply({
			type: 'soajax',
			url: WTF.requestBaseUrl(),
			extraParams: Ext.apply(opts.extraParams || {}, {
				service: sid,
				action: act
			}),
			reader: Ext.apply({
				type: 'json',
				keepRawData: true,
				rootProperty: rootp || 'data',
				messageProperty: 'message'
			}, opts.reader || {}),
			listeners: Ext.merge({
				metachange: function(s, meta) {
					if (meta.error) WT.handleRequestMetaError(meta.error);
				},
				exception: function(s, req, op, eop) {
					WT.handleRequestError(sid, act, req, op);
				}
			}, opts.listeners || {})
		}, obj);
	},
	
	/**
	 * Helper method for building a config object for a {@link Ext.data.proxy.Ajax proxy} using CRUD api.
	 * @param {String} sid The service ID
	 * @param {String} act The action name
	 * @param {String} [rootp] The property that contains data items corresponding to the Model.
	 * @param {Object} [opts] Config options.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {Object} [opts.timeout] The number of milliseconds to wait for a response. Defaults to {@link Ext.data.proxy.Server#timeout}.
	 * @param {Boolean} [opts.autoAbort] Whether this request should abort any other previous pending requests. Defaults to `false`.
	 * @param {String} [opts.model] The name of the Model to tie to this Proxy.
	 * @param {Object} [opts.extraParams] Extra request params.
	 * @returns {Object} Proxy config object
	 */
	apiProxy: function(sid, act, rootp, opts) {
		opts = opts || {};
		var obj = {};
		if (opts.timeout) Ext.apply(obj, {timeout: opts.timeout});
		if (opts.autoAbort) Ext.apply(obj, {autoAbort: true});
		if (opts.model) Ext.apply(obj, {model: opts.model});
		return Ext.apply({
			type: 'soajax',
			api: {
				create: WTF.requestBaseUrl({crud: 'create'}),
				read: WTF.requestBaseUrl({crud: 'read'}),
				update: WTF.requestBaseUrl({crud: 'update'}),
				destroy: WTF.requestBaseUrl({crud: 'delete'})
			},
			extraParams: Ext.apply(opts.extraParams || {}, {
				service: sid,
				action: act
			}),
			reader: Ext.apply({
				type: 'json',
				keepRawData: true,
				rootProperty: rootp || 'data',
				messageProperty: 'message'
			}, opts.reader || {}),
			writer: Ext.apply({
				type: 'json',
				writeAllFields: true
			}, opts.writer || {}),
			listeners: {
				metachange: function(s, meta) {
					if (meta.error) WT.handleRequestMetaError(meta.error);
				},
				exception: function(s, req, op, eop) {
					WT.handleRequestError(sid, act, req, op);
				}
			}
		}, obj);
	},
	
	/**
	 * Helper method for building a config object for a {@link Ext.data.proxy.Memory proxy}.
	 * @param {String} rootp The rootProperty
	 * @param {Object} [opts]
	 * @param {Object} [opts.reader]
	 * @returns {Object} Object config
	 */
	memoryProxy: function(rootp, opts) {
		opts = opts || {};
		var obj = {};
		return Ext.apply({
			type: 'memory',
			data: {success: true},
			reader: Ext.apply({
				type: 'json',
				rootProperty: rootp || 'data'
			}, opts.reader || {}),
			writer: Ext.apply({
				type: 'json',
				writeAllFields: true
			}, opts.writer || {})
		}, obj);
	},
	
	optionsProxy: function(svc, opts) {
		return WTF.apiProxy(svc, 'UserOptions', 'data', opts);
	},
	
	/**
	 * Helper method for building a config object for the uploader.
	 * @param {String} sid The service ID
	 * @param {String} context The upload context
	 * @param {Object} [opts]
	 * @param {Object} [opts.extraParams] Additional extra params to apply
	 * @returns {Object} Object config
	 */
	uploader: function(sid, context, opts) {
		opts = opts || {};
		return Ext.merge({
			url: WTF.requestBaseUrl(),
			extraParams: {
				service: sid,
				action: 'Upload',
				context: context
			},
			flashSwfUrl: 'resources/com.sonicle.webtop.core/0.0.0/resources/vendor/plupload/2.3.6/Moxie.swf',
			silverlightXapUrl: 'resources/com.sonicle.webtop.core/0.0.0/resources/vendor/plupload/2.3.6/Moxie.xap'
		}, opts);
	},
	
	/**
	 * Helper method for building a config object for {@link Ext.data.field.Field field}.
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
		cfg.validators = Ext.isArray(cfg.validators) ? Ext.Array.push(cfg.validators, validators) : validators;
		
		return Ext.apply({
			name: name,
			type: type,
			allowNull: true
		}, cfg, {
			defaultValue: null
		});
	},
	
	/**
	 * Helper method for building a config object for readOnly {@link Ext.data.field.Field field}.
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
			allowNull: true,
			persist: false
		}, cfg, {
			defaultValue: null
		});
	},
	
	/**
	 * Helper method for building a config object for a 
	 * foreign key {@link Ext.data.field.Field field}.
	 * Direct association needs to be defined using hasMany in parent model.
	 * @param {String} type See {@link Ext.data.field.Field#type}
	 * @returns {Object} The field config
	 */
	fkField: function(type) {
		return WTF.field('_fk', type, true);
	},
	
	/**
	 * Helper method for building a config object for an inverse 
	 * foreign key {@link Ext.data.field.Field field}.
	 * Inverse association is only defined using this method in child model.
	 * @param {String} type See {@link Ext.data.field.Field#type}
	 * @param {String} parent Parent model.
	 * @param {String} inverse Inverse name.
	 * @returns {Object} The field config
	 */
	fkInvField: function(type, parent, inverse) {
		return WTF.field('_fk', type, true, {
			reference: {
				parent: parent,
				inverse: inverse
			}
		});
	},
	
	/**
	 * Helper method for building a config object for a hasOne relation.
	 * @param {String} field The field name
	 * @param {String} model The linked model name
	 * @returns {Object} The hasOne config
	 */
	hasOne: function(field, model) {
		return {
			name: field,
			model: model
		};
	},
	
	/**
	 * Helper method for building a config object for a hasMany relation.
	 * @param {String} field The field name
	 * @param {String} model The linked model name
	 * @returns {Object} The hasMany config
	 */
	hasMany: function(field, model) {
		return {
			name: field,
			//associationKey: field,
			model: model,
			foreignKey: '_fk' // Deprecated from ExtJs 6.2.0
		};
	},
	
	/**
	 * Creates an object config that properly configure field's formats for
	 * reading and writing dates. Full date (Y-m-d H:i:s, like ISO) with no timezone.
	 * @param {Object} [cfg] Previous cfg to merge
	 * @returns {Object} An object config
	 */
	dateFieldYmdHisCfg: function(cfg) {
		return Ext.apply(cfg || {}, {
			dateFormat: 'Y-m-d H:i:s'
		});
	},
	
	/**
	 * Creates an object config that properly configure field's formats for
	 * reading and writing dates. ISO date pattern (Y-m-dTH:i:s.uO) will be used.
	 * @param {Object} [cfg] Previous cfg to merge
	 * @returns {Object} An object config
	 */
	dateFieldISOCfg: function(cfg) {
		return Ext.apply(cfg || {}, {
			dateReadFormat: 'Y-m-dTH:i:s.uO',
			dateWriteFormat: 'Y-m-d\\TH:i:s.uO'
		});
	},
	
	/**
	 * Helper method for building a config object for calculated model {@link Ext.data.field.Field field}.
	 * @param {String} name See {@link Ext.data.field.Field#name}
	 * @param {String} type See {@link Ext.data.field.Field#type}
	 * @param {String/String[]} depends See {@link Ext.data.field.Field#depends}
	 * @param {Function} convert See {@link Ext.data.field.Field#convert}
	 * @param {Object} [cfg] Custom config to apply.
	 * @returns {Object} The field config
	 */
	calcField: function(name, type, depends, convert, cfg) {
		cfg = cfg || {};
		var convFn = function(v, rec) {
			return convert.apply(this, Ext.Array.push([v, rec], Sonicle.Data.modelMultiGet(rec, depends)));
		};
		return Ext.apply({
			name: name,
			type: type,
			persist: false,
			depends: depends,
			convert: convFn
		}, cfg);
	},
	
	/**
	 * Builds a config object for a {@link Ext.form.field.ComboBox} form field.
	 * It renders a classic combobox with a unfilterable drop-down list.
	 * @param {String} valueField See {@link Ext.form.field.ComboBox#valueField}
	 * @param {String} displayField See {@link Ext.form.field.ComboBox#displayField}
	 * @param {type} [cfg] Custom config to apply.
	 * @returns {Object} The field config
	 */
	lookupCombo: function(valueField, displayField, cfg) {
		cfg = cfg || {};
		return Ext.apply({
			xtype: 'combo',
			editable: false,
			typeAhead: false,
			//queryMode: 'local',
			forceSelection: true,
			triggerAction: 'all',
			valueField: valueField,
			displayField: displayField,
			submitEmptyText: false
		}, cfg);
	},
	
	/**
	 * Builds a config object for a {@link Ext.form.field.ComboBox} form field.
	 * It renders a combobox with a local filtered drop-down list.
	 * @param {String} valueField See {@link Ext.form.field.ComboBox#valueField}
	 * @param {String} displayField See {@link Ext.form.field.ComboBox#displayField}
	 * @param {Object} [cfg] Custom config to apply.
	 * @returns {Object} The field config
	 */
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
	
	/**
	 * Builds a config object for a {@link Ext.form.field.ComboBox} form field.
	 * It renders a combobox with a remote filtered drop-down list.
	 * @param {String} valueField See {@link Ext.form.field.ComboBox#valueField}
	 * @param {String} displayField See {@link Ext.form.field.ComboBox#displayField}
	 * @param {type} [cfg] Custom config to apply.
	 * @returns {Object} The field config
	 */
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
			type: 'soclear',
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
	 * @param {Boolean} cfg.keepcase True to not apply lowercase transform to value
	 * @param {String} [cfg.emptyText] 
	 * @param {String} [cfg.emptyCls] The CSS class to apply when value is empty
	 * @returns {Function} The renderer function
	 */
	resColRenderer: function(cfg) {
		cfg = cfg || {};
		return function(value, meta) {
			if (value === null && Ext.isString(cfg.emptyText)) {
				if (Ext.isString(cfg.emptyCls)) meta.tdCls = cfg.emptyCls;
				return cfg.emptyText;
			} else {
				return WT.res(cfg.id, cfg.key + '.' + ((cfg.keepcase === true) ? String(value) : Ext.util.Format.lowercase(value)));
			}
		};
	},
	
	/**
	 * Configures a renderer for setting tdCls meta property.
	 * Class name will be the result of the following concatenation: '{clsPrefix}{value}'
	 * @param {Object} cfg Custom configuration object.
	 * @param {String} [cfg.valueField] Specifies the field from which getting value instead of current one.
	 * @param {String} [cfg.clsPrefix] Specifies the prefix to prepend to value. Defaults to ''.
	 * @param {String} [cfg.moreCls] Any other classes.
	 * @returns {Function} The renderer function
	 */
	clsColRenderer: function(cfg) {
		cfg = cfg || {};
		return function(value,meta,rec) {
			var val = (cfg.valueField) ? rec.get(cfg.valueField) : value,
					prefix = (cfg.clsPrefix) ? cfg.clsPrefix + val : val,
					more = (cfg.moreCls) ? ' ' + cfg.moreCls : '';
			meta.tdCls = prefix + more;
			return '';
		};
	},
	
	/**
	 * Configures a renderer for displaying label within related value
	 * using the following format: "{label} ({value})".
	 * @param {Object} cfg Custom configuration object.
	 * @param {String} [cfg.labelField] Specifies the field from which getting label value instead of current one.
	 * @param {String} cfg.valueField Specifies the field from which getting value.
	 * @returns {Function} The renderer function
	 */
	lvColRenderer: function(cfg) {
		cfg = cfg || {};
		return function(value,meta,rec) {
			var lbl = (cfg.labelField) ? rec.get(cfg.labelField) : value,
					v = (cfg.valueField) ? rec.get(cfg.valueField) : null;
			return (lbl && v) ? Ext.String.format('{0} ({1})', lbl, v) : lbl || v;
		};
	},
	
	/**
	 * Configures a renderer for adding an icon.
	 * Class name will be calculated using {@link WTF.cssIconCls}.
	 * @param {Object} cfg Custom configuration object.
	 * @param {String} [cfg.iconField] Specifies the field from which getting icon name instead of current field.
	 * @param {Function} [cfg.getIcon] A function which returns a calculated icon name.
	 * @param {String} [tipField] Specifies the field from which getting tooltip value.
	 * @param {Function} [cfg.getTip] A function which returns a calculated tooltip.
	 * 
	 * @param {String/Function} cfg.iconField Specifies the field from which getting name value instead of current one.
	 * @param {String} [cfg.tooltipField] Specifies the field from which getting tooltip value.
	 * 
	 * @param {String} cfg.xid Service short ID.
	 * @param {String} cfg.size Icon size (one of xs->16x16, s->24x24, m->32x32, l->48x48).
	 * @returns {Function} The renderer function
	 */
	iconColRenderer: function(cfg) {
		cfg = cfg || {};
		var evalValueFn = function(getFn, field, value, rec, fallback) {
			if(Ext.isFunction(getFn)) {
				return getFn(value, rec);
			} else if(rec && !Ext.isEmpty(field)) {
				return rec.get(field);
			} else {
				return (fallback === undefined) ? value : fallback;
			}
		};
		
		return function(value,meta,rec) {
			var ico = evalValueFn(cfg.getIcon, cfg.iconField, value, rec),
					ttip = evalValueFn(cfg.getTip, cfg.tipField, value, rec, null),
					cls = (ico) ? WTF.cssIconCls(cfg.xid, ico, cfg.size) : '',
					size = WTU.imgSizeToPx(cfg.size);
			if(ttip) {
				return '<div title="'+ttip+'" class="'+cls+'" style="width:'+size+'px;height:'+size+'px" />';
			} else {
				return '<div class="'+cls+'" style="width:'+size+'px;height:'+size+'px" />';
			}
		};
		
		/*
		var icoFn = Ext.isFunction(cfg.iconField);
		return function(value,meta,rec) {
			var data = (icoFn) ? cfg.iconField(rec) : ((cfg.iconField) ? rec.get(cfg.iconField) : value),
					ico = Ext.isArray(data) ? data[0] : data,
					ttip = Ext.isArray(data) ? data[1] : ((cfg.tooltipField) ? rec.get(cfg.tooltipField) : null),
					cls = (ico) ? WTF.cssIconCls(cfg.xid, ico, cfg.size) : '',
					size = WTU.imgSizeToPx(cfg.size);
			if(ttip) {
				return '<div title="'+ttip+'" class="'+cls+'" style="width:'+size+'px;height:'+size+'px" />';
			} else {
				return '<div class="'+cls+'" style="width:'+size+'px;height:'+size+'px" />';
			}
		};
		*/
	},
	
	/**
	 * Configures a tree renderer for displaying a colored box suitable for root > folder hierarchy.
	 * @param {Object} cfg Custom configuration object.
	 * @param {Function} [cfg.shouldCustomize] A function which returns `true` in correspondence of nodes that needs the colored box.
	 * @param {String} [cfg.colorField] Specifies the field from which getting color value.
	 * @param {Function} [cfg.getColor] A function which returns a calculated color.
	 * @param {Function} [cfg.renderer] Specifies a function in order to chain into the rendering process.
	 * @returns {Function} The renderer function
	 */
	coloredBoxTreeRenderer: function(cfg) {
		cfg = cfg || {};
		var evalValueFn = function(getFn, field, value, rec, fallback) {
			if (Ext.isFunction(getFn)) {
				return getFn(value, rec);
			} else if(rec && !Ext.isEmpty(field)) {
				return rec.get(field);
			} else {
				return (fallback === undefined) ? value : fallback;
			}
		};
		
		return function(val, meta, rec, ridx, cidx, sto, view) {
			var iconStyle;
			if (evalValueFn(cfg.shouldCustomize, null, val, rec, false) === true) {
				var co = evalValueFn(cfg.getColor, cfg.colorField, val, rec, null),
						obj = {};
				meta.iconCls = 'x-tree-checkbox';
				if (co === '#FFFFFF') {
					meta.iconCls += ' wt-tree-checkbox-white';
				} else {
					obj = {
						borderColor: co,
						backgroundColor: co
					};
				}
				iconStyle = Ext.DomHelper.generateStyles(obj);
				meta.tdCls = 'wt-grid-cell-treecolumn-colored';
			}
			meta.iconStyle = iconStyle;
			if (Ext.isFunction(cfg.renderer)) {
				return cfg.renderer(val, meta, rec, ridx, cidx, sto, view);
			} else {
				return val;
			}
		};
	},
	
	/**
	 * Configures a tree renderer for displaying a colored checkbox suitable for root > folder hierarchy.
	 * @param {Object} cfg Custom configuration object.
	 * @param {Function} [cfg.shouldCustomize] A function which returns `true` in correspondence of nodes that needs the colored box.
	 * @param {String} [cfg.colorField] Specifies the field from which getting color value.
	 * @param {Function} [cfg.getColor] A function which returns a calculated color.
	 * @param {Function} [cfg.renderer] Specifies a function in order to chain into the rendering process.
	 * @returns {Function} The renderer function
	 */
	coloredCheckboxTreeRenderer: function(cfg) {
		cfg = cfg || {};
		var evalValueFn = function(getFn, field, value, rec, fallback) {
			if (Ext.isFunction(getFn)) {
				return getFn(value, rec);
			} else if(rec && !Ext.isEmpty(field)) {
				return rec.get(field);
			} else {
				return (fallback === undefined) ? value : fallback;
			}
		};
		
		return function(val, meta, rec, ridx, cidx, sto, view) {
			var cbxCls = '', cbxStyle;
			if (evalValueFn(cfg.shouldCustomize, null, val, rec, false) === true) {
				var co = evalValueFn(cfg.getColor, cfg.colorField, val, rec, null),
						cofore = Sonicle.ColorUtils.bestForeColor(co, 0.64),
						obj = {};
				
				if (co === '#FFFFFF') {
					cbxCls = 'wt-tree-checkbox-white';
				} else {
					obj = {	borderColor: co	};
				}
				cbxCls += (cofore === '#000000' ? ' wt-tree-checkbox-darkmark' : ' wt-tree-checkbox-lightmark');
				if (rec.get('checked')) obj.backgroundColor = co;
				cbxStyle = Ext.DomHelper.generateStyles(obj);
				meta.iconCls = 'wt-hidden';
				meta.tdCls = 'wt-grid-cell-treecolumn-colored';
			}
			meta.customCheckboxCls = cbxCls;
			meta.checkboxStyle = cbxStyle;
			if (Ext.isFunction(cfg.renderer)) {
				return cfg.renderer(val, meta, rec, ridx, cidx, sto, view);
			} else {
				return val;
			}
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
	
	/*
	gridSelectionBind: function(modelProp, gripProp) {
		return {
			bind: {
				bindTo: '{'+gripProp+'.selection}',
				deep: true
			},
			get: function(model) {
				return model;
			},
			set: function(model) {
				if(!model.isModel) model = this.get(modelProp).getById(model);
				this.set()
				this.get(modelProp).set(fieldName, val);
			}
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
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
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
	 * Specify as empty string if you're working directly with viewModel.
	 * @param {String} fieldName Model's field name.
	 * @param {String} [objProp] Property name to set into value object. Defaults to fieldName.
	 * @returns {Object} Formula configuration object
	 */
	checkboxGroupBind: function(modelProp, fieldName, objProp) {
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				var v = {};
				v[objProp || fieldName] = val;
				return v;
			},
			set: function(val) {
				var o = modelProp ? this.get(modelProp) : this,
					v = val[objProp || fieldName];
				if(v !== undefined) o.set(fieldName, v);
			}
		};
	},
	
	/**
	 * Helper method for defining a {@link Ext.app.bind.Formula} that is able   
	 * to perform a two-way binding between radiogroup and a model's field.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * Specify as empty string if you're working directly with viewModel.
	 * @param {String} fieldName Model's field name.
	 * @param {String} [objProp] Property name to set into value object. Defaults to fieldName.
	 * @returns {Object} Formula configuration object
	 */
	radioGroupBind: function(modelProp, fieldName, objProp) {
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				var v = {};
				v[objProp || fieldName] = val;
				return v;
			},
			set: function(val) {
				var o = modelProp ? this.get(modelProp) : this,
						v = val[objProp || fieldName];
				if(v !== undefined) o.set(fieldName, v);
			}
		};
	},
	
	/**
	 * Helper method for defining a {@link Ext.app.bind.Formula} that is able   
	 * to perform a two-way binding between form-field and a model's field.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * Specify as empty string if you're working directly with viewModel.
	 * @param {String} fieldName Model's field name.
	 * @param {Function} getFn Function that calculate and return the value to get.
	 * @param {Function} setFn Function that calculate and return the value to set.
	 * @returns {Object} Formula configuration object
	 */
	foTwoWay: function(modelProp, fieldName, getFn, setFn) {
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				return Ext.callback(getFn, this, [val]);
			},
			set: function(val) {
				var o = modelProp ? this.get(modelProp) : this;
				if (val !== undefined) o.set(fieldName, Ext.callback(setFn, this, [val]));
			}
		};
	},
	
	foRecordTwoWay: function(modelProp, fieldName, valueField, targetRecId) {
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(sto) {
				if (!sto.isStore) Ext.raise('Store must be involved in binding');
				var rec = sto.getById(targetRecId);
				return rec ? rec.get(valueField) : undefined;
			},
			set: function(val) {
				var sto = this.get(Sonicle.String.join('.', modelProp, fieldName)), rec;
				if (!sto || !sto.isStore) Ext.raise('Store must be involved in binding');
				rec = sto.getById(targetRecId);
				if (rec) {
					rec.set(valueField, val);
				} else {
					var id = sto.getModel().idProperty,
							data = {};
					if (id) {
						data[id] = targetRecId;
						data[valueField] = val;
						sto.add(data);
					} else {
						Ext.raise('Unable to get model\'s idProperty');
					}
				}
			}
		};
	},
	
	/**
	 * Defines a{@link Ext.app.bind.Formula} that checks the equality between 
	 * a model field's value and passed value.
	 * @param {String} modelProp ViewModel's property in which the model is stored
	 * @param {String} fieldName Model's field name
	 * @param {Mixed} equalsTo Value to match
	 * @param {Boolean} [not=false] True to negate tests applying NOT operator
	 * @returns {Object} Formula configuration object
	 */
	foIsEqual: function(modelProp, fieldName, equalsTo, not) {
		if (arguments.length === 3) not = false;
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				return (not === true) ? (val !== equalsTo) : (val === equalsTo);
			}
		};
	},
	
	/**
	 * 
	 * @param {String} modelProp ViewModel's property in which the model is stored
	 * @param {String} fieldName Model's field name
	 * @param {Mixed[]|Mixed} values Allowed values list
	 * @param {Boolean} [not=false] True to negate tests applying NOT operator
	 * @returns {Object} Formula configuration object
	 */
	foIsIn: function(modelProp, fieldName, values, not) {
		if (arguments.length === 3) not = false;
		values = Ext.Array.from(values);
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				var iof = values.indexOf(val);
				return not === true ? iof === -1 : iof > -1;
			}
		};
	},
	
	/**
	 * Helper method for defining a {@link Ext.app.bind.Formula} that checks 
	 * equality between a model's field and passed value.
	 * @param {String} modelProp ViewModel's property in which the model is stored
	 * @param {String} fieldName Model's field name
	 * @param {Boolean} [not=false] True to apply NOT operator
	 * @returns {Object} Formula configuration object
	 */
	foIsEmpty: function(modelProp, fieldName, not) {
		if (arguments.length === 2) not = false;
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				return (not === true) ? !Ext.isEmpty(val) : Ext.isEmpty(val);
			}
		};
	},
	
	/**
	 * Defines a {@link Ext.app.bind.Formula} that returns the model field's 
	 * value if not empty, otherwise the specified default value.
	 * @param {String} modelProp ViewModel's property in which the model is stored
	 * @param {String} fieldName Model's field name
	 * @param {Mixed} defaultValue Value to apply if empty
	 * @returns {Object} Formula configuration object
	 */
	foDefaultIfEmpty: function(modelProp, fieldName, defaultValue) {
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				return Ext.isEmpty(val) ? defaultValue : val;
			}
		};
	},
	
	/**
	 * Defines a{@link Ext.app.bind.Formula} that returns a value computed by
	 * a customized function passed as parameter.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * @param {String} fieldName Model's field name.
	 * @param {Function} getFn A function to produce the desired value.
	 * @returns {Mixed} A value computed by the function.
	 */
	foGetFn: function(modelProp, fieldName, getFn) {
		if (!Ext.isFunction(getFn)) getFn = function(v) {return v;};
		return {
			bind: {bindTo: '{'+Sonicle.String.join('.', modelProp, fieldName)+'}'},
			get: function(val) {
				return getFn(val);
			}
		};
	},
	
	/**
	 * Defines a{@link Ext.app.bind.Formula} that returns a string by formatting
	 * the specified resource Key using the underlyning value.
	 * @param {String} modelProp ViewModel's property in which the model is stored.
	 * @param {String} fieldName Model's field name.
	 * @param {String} sid The service ID.
	 * @param {String} key The resource key.
	 * @returns {String} A localized and formatted resource string.
	 */
	foResFormat: function(modelProp, fieldName, sid, key) {
		return this.foGetFn(modelProp, fieldName, function(val) {
			return !Ext.isEmpty(val) ? WT.res(sid, key, val) : null;
		});
	},
	
	/**
	 * @deprecated Use {@link #foGetFn} instead.
	 */
	foCompare: function(modelProp, fieldName, compareFn) {
		Ext.log.warn("[WT.core] WTF.foCompare is deprecated, please use WTF.foGetFn instead.");
		return this.foGetFn(modelProp, fieldName, compareFn);
	}
	
	/*
	wsMsg: function(service, action, config) {
		return Ext.JSON.encode(Ext.apply(config||{},{ service: service, action: action }));
	},
	*/
});
