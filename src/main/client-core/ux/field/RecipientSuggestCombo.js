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
	extend: 'Sonicle.form.field.ComboBox',
	alias: ['widget.wtrcptsuggestcombo', 'widget.wtrcptsuggestcombobox'],
	requires: [
		'Sonicle.String',
		'WTA.model.InternetRecipient'
	],
	
	/**
	 * @cfg {String[]} [sources]
	 * List of explicit sources in which search for recipients, leave empty for any source.
	 */
	sources: [],
	
	/**
	 * @cfg {Integer} [limit]
	 * Limit the results.
	 */
	limit: 100,
	
	/**
	 * @cfg {telephone|telephone2|fax|mobile|pager|email|list|im} [targetRecipientFieldType=email]
	 * The desired recipient's field to return during lookups.
	 */
	targetRecipientFieldType: 'email',
	
	/**
	 * @cfg {Boolean} [automaticRecipientAtEnd=false]
	 * Set to `true` to put automatic suggested contacts last in list.
	 */
	automaticRecipientAtEnd: false,
	
	config: {
		idValue: null
	},
	
	/**
     * @event enterkeypress
	 * Fires when the user presses the ENTER key.
	 * Return false to stop subsequent query operations.
	 * @param {Sonicle.webtop.core.ux.field.Recipients} this
	 * @param {Object} event The event object
     */
	
	publishes: ['idValue'],
	twoWayBindable: ['idValue'],
	
	typeAhead: false,
	minChars: 2,
	autoSelect: false,
	autoSelectLast: false,
	autoSelectMatches: false,
	queryMode: 'remote',
	triggerAction: 'all',
	valueField: 'description',
	displayField: 'description',
	createNewOnEnter: true,
	createNewOnBlur: false,
	forceSelection: false,
	filterPickList: true,
	triggerOnClick: false,
	collapseOnSelect: true,
	forceInputCleaning: true,
	
	escapeDisplayed: true,
	selectOnFocus: true,
	editable: true,
	hideTrigger: true,
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			store: {
				autoLoad: false,
				
				//trick to force isLoaded = true when doQuery is called
				//this is a bug of Ext solved on version 7.5.1
				//this will be removed when Ext us upgraded
				loadCount: 1,
				
				model: 'WTA.model.InternetRecipient',
				proxy: WTF.apiProxy(WT.ID, 'ManageInternetRecipients', 'recipients', {
					extraParams: {
						sources: me.sources,
						limit: me.limit,
						rftype: me.rftype || me.targetRecipientFieldType,
						autoLast: Ext.isBoolean(me.autoLast) ? me.autoLast : me.automaticRecipientAtEnd
					}
				})
			},
			labelTpl: new Ext.XTemplate([
				'{[this.computeValue(values)]}',
				{
					computeValue: function(values) {
						var SoS = Sonicle.String;
						return SoS.removeQuotes(SoS.coalesce(values['personal'], values['address'], values['description']));
					}
				}
			]),
			tipTpl: new Ext.XTemplate([
				'{[this.computeValue(values)]}',
				{
					computeValue: function(values) {
						var SoS = Sonicle.String;
						return SoS.htmlAttributeEncode(SoS.removeQuotes(values['description']));
					}
				}
			])
		});
		me.callParent(arguments);
	},
	
	initEvents: function() {
		var me = this;
		me.callParent();
		// Borrowed from Sonicle.form.field.search.Field, NOT useful here!
		/*
		me.altArrowKeyNav.map.addBinding({
			key: /^.$/,
			handler: me.onInputKey,
			scope: me
		});
		*/
		// ----------
		me.altArrowKeyNav.map.addBinding({
			key: Ext.event.Event.ENTER,
			handler: me.onInputKeyEnter,
			scope: me
		});
	},
	
	initListConfig: function() {
		var me = this;
		return Ext.apply(me.callParent() || {}, {
			sourceField: 'sourceLabel',
			disableFocusSaving: true,
			enableButton: true,
			escapeDisplay: true,
			getButtonTooltip: function() {
				return WT.res('wtrecipientsfield.entry.button.tip');
			},
			shouldShowButton: function(values) {
				return 'auto' === values.source;
			},
			buttonHandler: function(s, e, rec) {
				rec.drop();
				rec.store.sync();
			}
		});
	},
	
	doApplyConfig: function() {
		var me = this;
		Ext.apply(me, {
			store: {
				autoLoad: true,
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
	
	onChange: function(newVal, oldVal) {
		var me = this,
				sto = me.getStore();
		if (sto && !Ext.isEmpty(me.getIdValue()) && newVal !== oldVal && newVal !== me.initialValue2) {
			if (!me.forceSelection && me.queryMode !== 'local') {
				me.setIdValue(null);
			}
		}
		me.callParent(arguments);
	},
	
	updateValue: function() {
		var me = this, sel;
		me.callParent(arguments);
		// This usually occurs after the user picks element: we need to update the related ID.
		sel = me.getSelectedRecord();
		me.setIdValue(sel ? sel.get('recipientId') : null);
	},
	
	setValue: function(value) {
		var me = this;
		// Dump initial value
		if (me.initialValue2 === undefined) me.initialValue2 = value;
		me.callParent(arguments);
	},
	
	privates: {
		// Borrowed from Sonicle.form.field.search.Field, NOT useful here!
		/*
		onInputKey: function(keyCode, e) {
			// Disarm list-opening on any input!
			this.disarmListDelayedOpening();
			return true;
		}, 
		*/
		// ----------
		
		onInputKeyEnter: function(keyCode, e) {
			var me = this,
				picker = me.getPicker(),
				value = me.getValue();
			
			// Borrowed from Sonicle.form.field.search.Field, NOT useful here!
			//me.disarmListDelayedOpening();
			// ----------
			
			// Enter key should be handled here only when the combo's picker is 
			// not activated (expanded): there is a side-effect of the original 
			// impl. where after typing with no results, the picker is visually 
			// hidden but expanded flag is still set to true. We can track this 
			// situation checking picker's highlightedItem!
			if (!me.isExpanded || (picker && !picker.highlightedItem)) {
				e.stopEvent();
				me.fireEvent('enterkeypress', me, e, value);
				return false;
			} else {
				return true;
			}
		}
	}
});
