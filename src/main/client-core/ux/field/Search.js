/* 
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.field.Search', {
	alternateClassName: 'WTA.ux.field.Search',
	extend: 'Sonicle.form.field.search.Field',
	alias: ['widget.wtsearchfield'],
	
	width: 400,
	
	/**
	 * @cfg {String[]} highlightKeywords
	 * The keywords whose values are taken into account when highlighting.
	 * All available keywords are used when set to undefined or null.
	 */
	highlightKeywords: null,
	
	/**
	 * @cfg {Boolean} highlightAnyText
	 * Set to `false` to not use query's any-text in highlighting.
	 */
	highlightAnyText: true,
	
	/**
     * @event query
	 * Fires when the user presses the ENTER key or clicks on the search icon.
	 * @param {Ext.form.field.Text} this
	 * @param {String} value Human-readable query text
	 * @param {Object} queryObject queryObject Query exploded into components
     */
	
	constructor: function(cfg) {
		var me = this;
		
		if (Ext.isEmpty(cfg.emptyText)) {
			cfg.emptyText = WT.res('textfield.search.emp');
		}
		if (Ext.isEmpty(cfg.trueText)) {
			cfg.trueText = WT.res('word.yes');
		}
		if (Ext.isEmpty(cfg.falseText)) {
			cfg.falseText = WT.res('word.no');
		}
		if (Ext.isEmpty(cfg.searchText)) {
			cfg.searchText = WT.res('wtsearchfield.search');
		}
		if (Ext.isEmpty(cfg.clearText)) {
			cfg.clearText = WT.res('wtsearchfield.clear');
		}
		if (Ext.isEmpty(cfg.usageText)) {
			cfg.usageText = WT.res('wtsearchfield.usage');
		}
		me.callParent([cfg]);
	},
	
	/**
	 * Highlight search keywords into target DOM elements.
	 * @param {Ext.dom.Element} el The parent DOM element in which apply marking.
	 * @param {String} querySelector DOMString indicating elements to be candidates for selection.
	 * @returns {Mark} Instance of Mark object
	 */
	highlight: function(el, querySelector) {
		var me = this,
				SoSS = Sonicle.SearchString,
				keywords = [], queryObject;
		
		if (!Ext.isEmpty(me.getValue())) {
			queryObject = SoSS.toQueryObject(SoSS.parseHumanQuery(me.getValue()));
			Ext.iterate(queryObject.conditionArray, function(item) {
				if (me.highlightKeywords && me.highlightKeywords.indexOf(item.keyword) === -1) return;
				if (!item.negated) keywords.push(item.value);
			});
			if (me.highlightAnyText && !Ext.isEmpty(queryObject.anyText)) keywords.push(queryObject.anyText);
		}
		
		me.clearHighlight(el, querySelector);
		if (keywords.length > 0) me.marked = me.mark(el.dom, querySelector, keywords);
		return me.marked;
	},
	
	/**
	 * Clear any previous highlight.
	 * @param {Ext.dom.Element} el The parent DOM element in which apply marking.
	 * @param {String} querySelector DOMString indicating elements to be candidates for selection.
	 */
	clearHighlight: function(el, querySelector) {
		var me = this;
		if (me.marked) {
			me.marked.unmark();
		} else if (el) {
			me.unMark(el.dom, querySelector);
		}
		delete me.marked;
	},
	
	/**
	 * Apply marking on a specified DOM element.
	 * @param {HTMLElement} dom The parent DOM element.
	 * @param {String} querySelector DOMString indicating elements to be processed.
	 * @param {String|String[]} keyword The keyword to be marked. Can also be an array with multiple keywords.
	 * @returns {Mark}
	 */
	mark: function(dom, querySelector, keyword) {
		if (dom) {
			var el = dom.querySelector(querySelector),
					mark;
			if (el) {
				mark = new Mark(el);
				mark.mark(keyword);
				return mark;
			}
		}
		return null;
	},
	
	/**
	 * Clear marking on a specified DOM element.
	 * @param {HTMLElement} dom The parent DOM element.
	 * @param {String} querySelector DOMString indicating elements to be processed.
	 */
	unMark: function(dom, querySelector) {
		if (dom) {
			var el = dom.querySelector(querySelector),
					mark;
			if (el) {
				mark = new Mark(el);
				mark.unmark();
				return mark;
			}
		}
		return null;
	},
	
	statics: {
		customFieldDefs2Fields: function(rawDefs) {
			var defObj = Ext.JSON.decode(rawDefs, true),
					fields = [], cfg;
			
			if (defObj) {
				Ext.iterate(defObj, function(field, indx) {
					cfg = WTA.ux.field.Search._customFieldCfg(field);
					if (cfg) fields.push(cfg);
				});
			}
			return fields;
		},
		
		_customFieldCfg: function(field) {
			var ftype = field.type,
					cfg = {
						name: 'cf_' + field.name,
						mapping: 'cfield|' + field.id,
						label: field.label
					};
			
			if ('text' === ftype || 'textarea' === ftype) {
				return Ext.apply(cfg, {type: 'string'});
				
			} else if ('number' === ftype) {
				return Ext.apply(cfg, {type: 'number'});
				
			} else if ('date' === ftype) {
				return Ext.apply(cfg, {
					type: 'date',
					//labelAlign: 'left',
					customConfig: {
						startDay: WT.getStartDay(),
						format: WT.getShortDateFmt()
					}
				});
				
			} else if ('time' === ftype) {
				return Ext.apply(cfg, {
					type: 'time',
					//labelAlign: 'left',
					customConfig: {
						format: WT.getShortTimeFmt()
					}
				});
				
			} else if ('datetime' === ftype) {
				//TODO: Add support to this when customFields will be extendes.
				return null;
				
			} else if ('combobox' === ftype) {
				return Ext.apply(cfg, {
					type: 'combo',
					customConfig: {
						store: field.values,
						typeAhead: true,
						queryMode: 'local',
						forceSelection: true,
						selectOnFocus: true,
						triggerAction: 'all',
						submitEmptyText: false
					}
				});
				
			} else if ('checkbox' === ftype) {
				return Ext.apply(cfg, {type: 'boolean'});
				
			} else {
				return null;
			}
		}
	}
});
