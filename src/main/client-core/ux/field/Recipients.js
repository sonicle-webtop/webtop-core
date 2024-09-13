/* 
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.field.Recipients', {
	alternateClassName: 'WTA.ux.field.Recipients',
	extend: 'Sonicle.form.field.TagView',
	alias: ['widget.wtrecipientsfield'],
	requires: [
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
	createNewOnBlur: true,
	forceSelection: false,
	filterPickList: true,
	triggerOnClick: false,
	collapseOnSelect: true,
	forceInputCleaning: true,
	clearOnBackspace: false, // Avoid deleting tags with backspace
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			cls: 'wt-recipients-field', //allows custom flex styles for inputEl
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
						rftype: me.targetRecipientFieldType,
						autoLast: me.automaticRecipientAtEnd
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
		
		if (me.delimiter && me.multiSelect) {
			me.origDelimiter = me.delimiter;
			me.origDelimiterRegexp = me.delimiterRegexp;
			me.delimiter = {};
			me.delimiter[Symbol.split] = function(s) {
				return Sonicle.String.parseEmailRecipients(s);
			};
			me.delimiterRegexp = {
				test: function(s) {
					return me.origDelimiterRegexp.test(s);
				}
			};
			me.delimiterRegexp[Symbol.split] = function(s) {
				return Sonicle.String.parseEmailRecipients(s);
			};
			/*
			me.delimiter = {
				[Symbol.split]: function(s) {
					return Sonicle.String.parseEmailRecipients(s);
				}
			};
			me.delimiterRegexp = {
				[Symbol.split]: function(s) {
					return Sonicle.String.parseEmailRecipients(s);
				},

				test: function(s) {
					return me.origDelimiterRegexp.test(s);
				}
			};
			*/
		}
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
	}
});

