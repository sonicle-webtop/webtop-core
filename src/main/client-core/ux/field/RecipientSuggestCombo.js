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
Ext.define('Sonicle.webtop.core.ux.field.RecipientSuggestCombo', {
	alternateClassName: 'WTA.ux.field.RecipientSuggestCombo',
	extend: 'Sonicle.form.field.SourceComboBox',
	alias: ['widget.wtrcptsuggestcombo', 'widget.wtrcptsuggestcombobox'],
	
	requires: [
		'WTA.model.InternetRecipient'
	],
	plugins: [
		'soenterkeyplugin',
		'sofieldtooltip'
	],
	
	config: {
		/**
		 * @cfg {String[]} sources
		 * contacts sources, or null for anything.
		 */
		sources: [],

		/**
		 * @cfg {int} limit
		 * limit records number.
		 */
		limit: 100,
		
		/**
		 * @rftype {String} recipient field type for lookup
		 * email|fax|telephone...see RecipientFieldType.
		 * defaults to email
		 */
		rftype: "email",
		
		/**
		 * @autoLast {Boolean} automatic contacts suggested last in list
		 */
		autoLast: false
	},
	
	escapeDisplayed: true,
	typeAhead: false,
	minChars: 2,
	autoSelect: false,
	queryMode: 'remote',
	triggerAction: 'all',
	forceSelection: false,
	selectOnFocus: true,
	editable: true,
	hideTrigger: true,
	valueField: 'description',
	displayField: 'description',
	sourceField: 'sourceLabel',
	selectedId: null,
	selectedValue: null,
	
	initComponent: function() {
		var me = this;
		me.doApplyConfig();
		me.callParent(arguments);
		me.on('specialkey', me._onSpecialKey);
		me.on('select', me._onSelect);
		me.on('change', me._onChange);
	},
	
	doApplyConfig: function() {
		var me = this;
		Ext.apply(me, {
			store: {
				model: 'WTA.model.InternetRecipient',
				proxy: WTF.apiProxy(WT.ID, 'ManageInternetRecipients', 'recipients', {
					extraParams: {
						sources: me.getSources(),
						limit: me.getLimit(),
						rftype: me.getRftype(),
						autoLast: me.getAutoLast()
					}
				})
			}
		});
	},
	
	updateSuggestionContext: function(nv) {
		var me = this;
		if(me.store) {
			WTU.applyExtraParams(me.store, {
				context: nv
			});
		}
	},
	
	onBlur: function(e) {
		var me = this;
		me.callParent(arguments);
		// This avoids binding notify firing problems when typing into field
		// and bluring out rapidly; checkChange method forces internal updates!
		if(me.store && !me.store.isLoaded()) me.checkChange();
	},
	
	/*
    onDownArrow: function(e) {
		// Disable list opening on down arrow
		if (e.altKey) this.callParent(arguments);
    },
	*/
   
   getSelectedId: function() {
		return this.selectedId;
   },
   
   getSelectedValue: function() {
		return this.selectedValue;
   },
   
   clearSelectedData: function() {
	   this.selectedId=null;
	   this.selectedValue=null;
   },
	
   setSelectedData: function(id,value) {
	   this.selectedId=id;
	   this.selectedValue=value;
   },
	
	_onSpecialKey: function(s,e) {
		if(s.isExpanded) {
			if(e.shiftKey && (e.getKey() === e.DELETE)) {
				var pick = s.getPicker(),
						nav = pick.getNavigationModel(),
						rec = nav.getRecord();

				if(rec && rec.get("source")===this.self.SOURCE_AUTO) {
					rec.drop();
					s.getStore().sync();
				}
			}
		}
	},
	
	_onSelect: function(s,r,e) {
		var me=this;
		if (me.selectedId && r.get(me.valueField)===me.selectedValue) {
			//do not change anything if id is not empty and values are same
		} else {
			me.selectedId=r.get("recipientId");
			me.selectedValue=r.get(me.valueField);
		}
	},
	
	_onChange: function(s,nv,ov,e) {
		var me=this;
		if (me.selectedId && nv!=me.selectedValue && nv!==ov) {
			me.clearSelectedData();
		} 
	},
	
	statics: {
		SOURCE_AUTO: 'auto'
	}
});
