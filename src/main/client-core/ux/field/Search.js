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
		if (Ext.isEmpty(cfg.searchText)) {
			cfg.searchText = WT.res('wtsearchfield.search');
		}
		if (Ext.isEmpty(cfg.clearText)) {
			cfg.clearText = WT.res('wtsearchfield.clear');
		}
		me.callParent([cfg]);
	},
	
	mark: function(document, querySelector, value) {
		if (document) {
			var el = document.querySelector(querySelector),
					mark;
			if (el) {
				mark = new Mark(el);
				mark.mark(value);
				return mark;
			}
		}
		return null;
	},
	
	unMark: function(document, querySelector) {
		if (document) {
			var el = document.querySelector(querySelector),
					mark;
			if (el) {
				mark = new Mark(el);
				mark.unmark();
				return mark;
			}
		}
		return null;
	},
	
	markKeywords: function(el,querySelector) {		
		if (!el) return;
		
		var me=this,
			keywords = [],
			searchedValues = me.getValue().split(" ");

		searchedValues.forEach(function(element) {
			if(element.includes('from') || element.includes('to') 
					|| element.includes('subject') || element.includes('message')) {

				keywords.push(element.substr(element.indexOf(':') + 1));
			}
			if(!element.includes(':')) {
				keywords.push(element);
			}
		});

		me.unMark(el.dom, querySelector);
		me.mark(el.dom, querySelector, keywords);
		
	}
	
});
