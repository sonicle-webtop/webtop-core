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
Ext.define('Sonicle.webtop.core.ux.SuggestCombo', {
	alternateClassName: 'WT.ux.SuggestCombo',
	extend: 'Ext.form.field.ComboBox',
	alias: ['widget.wtsuggestcombo', 'widget.wtsuggestcombobox'],
	
	requires: [
		'WT.ux.data.ValueModel'
	],
	plugins: [
		'soenterkeyplugin',
		'sofieldtooltip'
	],
	
	/**
	 * @cfg {String} sid
	 * Webtop service ID.
	 */
	
	/**
	 * @cfg {String} suggestionContext
	 * Suggestion context.
	 */
	
	typeAhead: false,
	minChars: 2,
	autoSelect: false,
	queryMode: 'remote',
	triggerAction: 'all',
	forceSelection: false,
	selectOnFocus: true,
	editable: true,
	hideTrigger: true,
	valueField: 'id',
	displayField: 'id',
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			store: {
				model: 'WT.ux.data.ValueModel',
				proxy: WTF.apiProxy(me.sid, 'ManageSuggestions', 'data', {
					extraParams: {
						context: me.suggestionContext
					}
				})
			}
		});
		
		me.callParent(arguments);
		me.on('specialkey', me._onSpecialKey);
	},
	
	setSuggestionContext: function(value) {
		var me = this;
		me.suggestionContext = value;
		WTU.applyExtraParams(me.getStore(), {
			context: value
		});
	},
	
	/*
    onDownArrow: function(e) {
		// Disable list opening on down arrow
		if (e.altKey) this.callParent(arguments);
    },
	*/
	
	_onSpecialKey: function(s,e) {
		if(s.isExpanded) {
			if(e.shiftKey && (e.getKey() === e.DELETE)) {
				var pick = s.getPicker(),
						nav = pick.getNavigationModel(),
						rec = nav.getRecord();

				if(rec) {
					rec.drop();
					s.getStore().sync();
				}
			}
		}
	}
});
