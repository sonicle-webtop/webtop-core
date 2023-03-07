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
Ext.define('Sonicle.webtop.core.app.Util', {
	singleton: true,
	alternateClassName: ['WTA.Util', 'WTU'],
	
	constructor: function(cfg) {
		var me = this;
		//me.resetHtmlCharEntities();
		me.callParent(cfg);
	},
	
	forItemId: function(s) {
		return s.replace(/\|/g, '-');
	},
	
	/**
	 * Convenience handler that completely disable the event, stopping its
	 * propagation and preventing browser default behaviour.
	 * @param {Ext.EventObject} event The event object
	 * @returns {Boolean}
	 */
	onDisabledEvent: function(event) {
		event.stopPropagation();
		event.preventDefault();
		return false;
	},
	
	/**
	 * Construct an alias using passed string.
	 * @param {String} prefix The prefix
	 * @param {String} s The source string
	 * @returns {String} Aliased string
	 */
	aliasize: function(prefix, s) {
		return prefix + '.' + s.toLowerCase().replace(/\./g, '');
	},
	
	/*
	 * Makes a string suitable for use as ID.
	 * Resulting string will be lowercase without any dots.
	 * @param {String} s The source string
	 * @returns {String} The resulting string
	 */
	idfy: function(s) {
		return s.toLowerCase().replace(/\./g, '');
	},
	
	/**
	 * @deprecated Use {@link Sonicle.String#deflt} instead
	 */
	deflt: function(value, ifEmpty) {
		Ext.log.warn("[WT.core] 'WTU.deflt' is deprecated, please use 'Sonicle.String.deflt' instead.");
		return (Ext.isEmpty(value)) ? ifEmpty : value;
	},
	
	/**
	 * Returns itself or the 1st element if the passed object is an Array.
	 * @param {Mixed/Mixed[]} obj The object to inspect
	 * @returns {Mixed} Object itself or the 1st element
	 */
	itselfOrFirst: function(obj) {
		return Ext.isArray(obj) ? obj[0] : obj;
	},
	
	/**
	 * @deprecated Use {@link Sonicle.Utils#toJSONArray} instead
	 */
	arrayAsParam: function(arr) {
		Ext.log.warn("[WT.core] 'WTU.arrayAsParam' is deprecated, please use 'Sonicle.Utils.toJSONArray' instead.");
		return Sonicle.Utils.toJSONArray(arr);
	},
	
	/**
	 * Null-safe method for checking xtype.
	 * @param {Mixed} obj An object instance.
	 * @param {String} xtype The xtype to check.
	 * @returns {Boolean}
	 */
	isXType: function(obj, xtype) {
		if (!Ext.isObject(obj)) return false;
		if (!Ext.isFunction(obj.isXType)) return false;
		return obj.isXType(xtype);
	},
	
	/**
	 * Checks if passed object instance is an {@link Ext.Action}.
	 * @param {Mixed} obj The object instance to check.
	 * @returns {Boolean} 'True' if passed object is an action.
	 */
	isAction: function(obj) {
		if (!Ext.isObject(obj)) return false;
		return (obj.isAction === true) && Ext.isFunction(obj.execute);
	},
	
	/**
	 * Returns corresponding pixels for passed image size.
	 * @param {String} size The image size (one of xs, s, m, l).
	 * @returns {Number} Pixels
	 */
	imgSizeToPx: function(size) {
		switch(size) {
			case 'xs':
				return 16;
			case 's':
				return 24;
			case 'm':
				return 32;
			case 'l':
				return 48;
			default:
				return 0;
		}
	},
	
	/*
	 * Check filename extension trying to guess a known file type name
	 * @param {String} filename The file name
	 * @return {String} The recognized file type name, as in WT.filtypes, "bin" if not
	 */
	getFileExtension: function(filename) {
		var ix=filename.lastIndexOf('.'),
			extension="bin";
	
		if (ix>=0) extension=filename.substring(ix+1).toLowerCase();
        return extension;
	},
    
	humanReadableDuration: function(seconds, opts) {
		opts = opts || {};
		opts.unitSeparator = opts.unitSeparator || ' ';
		var remaining = seconds,
				value, values, out, unitkey,
				units = [
					{name: 'years', value: 31536000},
					{name: 'months', value: 2628000},
					{name: 'weeks', value: 604800},
					{name: 'days', value: 86400},
					{name: 'hours', value: 3600},
					{name: 'minutes', value: 60},
					{name: 'seconds', value: 1}
				];
		
		values = [];
		Ext.iterate(units, function(unit, i) {
			if(Ext.isArray(opts.units)) {
				if(opts.units.indexOf(unit.name) < 0) return;
			}
			value = Math.floor(remaining / unit.value);
			if(value > 0) {
				values.push({
					unit: unit.name,
					value: value
				});
				remaining = remaining % unit.value;
			}
			if(remaining <= 0) return false;
		});
		
		out = [];
		Ext.iterate(values, function(value) {
			unitkey = (value.value > 1) ? value.unit : value.unit.substring(0, value.unit.length-1);
			out.push(value.value + opts.unitSeparator + WT.res('word.time.' + unitkey));
		});
		
		return out.join(' ');
	},
	
	/*
	 * @deprecated Use {@link Sonicle.String#humanReadableSize} instead
	 */
	humanReadableSize: function(bytes, opts) {
		Ext.log.warn("[WT.core] 'WTU.humanReadableSize' is deprecated, please use 'Sonicle.String.humanReadableSize' instead.");
		return Sonicle.String.humanReadableSize(bytes, opts);
	},
	
	/**
	 * @deprecated use Sonicle.Data.removeExtraParams() instead
	 */
	removeExtraParams: function(proxy, params) {
		Ext.log.warn('"WTU.removeExtraParams" is deprecated. Use "Sonicle.Data.removeExtraParams" instead.');
		Sonicle.Data.removeExtraParams.apply(this, arguments);
	},
	
	/**
	 * @deprecated use Sonicle.Data.applyExtraParams() instead
	 */
	applyExtraParams: function(proxy, params, clear) {
		Ext.log.warn('"WTU.applyExtraParams" is deprecated. Use "Sonicle.Data.applyExtraParams" instead.');
		Sonicle.Data.applyExtraParams.apply(this, arguments);
	},
	
	/**
	 * @deprecated use Sonicle.Data.storeNeedsSync() instead
	 */
	loadWithExtraParams: function(store, params, overwrite) {
		Ext.log.warn('"WTU.loadWithExtraParams" is deprecated. Use "Sonicle.Data.loadWithExtraParams" instead.');
		Sonicle.Data.loadWithExtraParams.apply(this, arguments);
	},
	
	/**
	 * @deprecated use Sonicle.Data.storeNeedsSync() instead
	 */
	needsSync: function(store) {
		Ext.log.warn('"WTU.needsSync" is deprecated. Use "Sonicle.Data.storeNeedsSync" instead.');
		return Sonicle.Data.storeNeedsSync(store);
	},
	
	/**
	 * @deprecated use Sonicle.Data.storeDeepClone() instead
	 */
	deepClone: function(store) {
		Ext.log.warn('"WTU.deepClone" is deprecated. Use "Sonicle.Data.storeDeepClone" instead.');
		return Sonicle.Data.storeDeepClone(store);
	},
	
	/**
	 * Applies provided formula definition to passed ViewModel.
	 * @param {Ext.app.ViewModel} vm ViewModel instance.
	 * @param {Object} formulas Formulas configuration
	 */
	applyFormulas: function(vm, formulas) {
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, formulas));
	},
	
	applyStores: function(vm, stores) {
		vm.setStores(Ext.apply(vm.getStores() || {}, stores));
	},
	
	removeHeader: function(cmp) {
		if(!cmp.isPanel) return;
		if(cmp.header && cmp.header.isHeader) {
			cmp.header.destroy();
		}
		cmp.header = false;
		cmp.updateHeader();
	},
	
	removeItems: function(cmp, startIndex) {
		var count = cmp.items.getCount(),
				itms = [];
		for(var i=startIndex; i<count; i++) {
			itms.push(cmp.getComponent(i));
		}
		Ext.iterate(itms, function(itm) {
			cmp.remove(itm);
		});
	},
	
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
	
	getCheckedRadioUsingDOM: function(keys) {
		var checked = null;
		Ext.iterate(keys, function(key) {
			if((checked === null) && (Ext.get(key).dom.checked === true)) checked = key;
		});
		return checked;
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
	
	/**
	 * Checks for each descendant field in passed container if there is a
	 * validation error that matches its {@link Ext.form.field.Field#getValidationField}.
	 * If found, field's invalid status will be set.
	 * @param {Ext.container.Container} container The container to look into.
	 * @param {Object} errors Validation error object
	 */
	updateFieldsErrors: function(container, errors) {
		if (container.isXType('container') && errors) {
			var flds = container.query('field');
			Ext.iterate(errors, function(err) {
				Ext.iterate(flds, function(fld) {
					var vf = fld.getValidationField();
					if (vf && (vf.getName() === err.id)) fld.markInvalid(err.msg);
				});
			});
		}
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
	
	_dockToProp: function(dock) {
		if(dock === 'top') return 'tbar';
		if(dock === 'bottom') return 'fbar';
		if(dock === 'left') return 'lbar';
		if(dock === 'right') return 'rbar';
		return null;
	},
	
	/**
	 * 
	 * @param {Ext.selection.Model} selModel The SelectionModel.
	 * @param {Boolean} requireSingle True to return null in case of multi-selection.
	 * @returns {undefined}
	 */
	getSelected: function(selModel, requireSingle) {
		if(requireSingle === undefined) requireSingle = true;
		var sel = selModel.getSelection();
		if(requireSingle && sel.length !== 1) return null;
		return (sel.length > 0) ? sel[0] : null;
	}
	
	/**
	 * Adds a set of character entity definitions to the set used by
	 * {@link WT#encodeHtmlEntities} and {@link WT#decodeHtmlEntities}.
	 * 
	 * This object should be keyed by the entity name sequence,
	 * with the value being the textual representation of the entity.
	 * 
	 * @param {Object} entObj The set of character entities to add to the current definitions.
	 *//*
	addHtmlCharEntities: function(entObj) {
		var me = this, charKeys = [], entityKeys = [], key, echar;
		for (key in entObj) {
			echar = entObj[key];
			me.entityToChar[key] = echar;
			me.charToEntity[echar] = key;
			charKeys.push(echar);
			entityKeys.push(key);
		}
		me.charToEntityRegex = new RegExp('(' + charKeys.join('|') + ')', 'g');
		//me.entityToCharRegex = new RegExp('(' + entityKeys.join('|') + '|&#[0-9]{1,5};' + ')', 'g');
		me.entityToCharRegex = new RegExp('(' + entityKeys.join('|') + ')', 'g');
	},*/
	
	/**
	 * Resets the set of character entity definitions used by 
	 * {@link WT#encodeHtmlEntities} and {@link WT#decodeHtmlEntities} 
	 * back to the default state.
	 *//*
	resetHtmlCharEntities: function() {
		var me = this;
		me.charToEntity = {};
		me.entityToChar = {};
		// add the default set
		me.addHtmlCharEntities({
			'&agrave;':'à',
			'&aacute;':'á',
			'&egrave;':'è',
			'&eacute;':'é',
			'&igrave;':'ì',
			'&iacute;':'í',
			'&ograve;':'ò',
			'&oacute;':'ó',
			'&ugrave;':'ù',
			'&uacute;':'ú'
		});
	},*/
	
	/**
	 * Convert certain special characters (à, è, etc..) to their HTML character equivalents for literal display in web pages.
	 * @param {String} value The string to encode.
	 * @returns {String} The encoded text.
	 *//*
	encodeHtmlEntities: function(value) {
		var me = this;
		var htmlEncodeReplaceFn = function(match, capture) {
			return me.charToEntity[capture];
		};
		return (!value) ? value : String(value).replace(me.charToEntityRegex, htmlEncodeReplaceFn);
	},*/
	
	/**
	 * Convert certain special characters (à, è, etc..) from their HTML character equivalents.
	 * @param {String} value The string to decode.
	 * @returns {String} The decoded text.
	 *//*
	decodeHtmlEntities: function(value) {
		var me = this;
		var htmlDecodeReplaceFn = function(match, capture) {
            return (capture in me.entityToChar) ? me.entityToChar[capture] : String.fromCharCode(parseInt(capture.substr(2), 10));
        };
		return (!value) ? value : String(value).replace(me.entityToCharRegex, htmlDecodeReplaceFn);
	},*/
});
