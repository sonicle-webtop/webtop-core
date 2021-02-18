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
Ext.define('Sonicle.webtop.core.ux.IMBigChat', {
	alternateClassName: 'WTA.ux.IMBigChat',
	extend: 'Sonicle.webtop.core.ux.IMBaseChat',
	alias: 'widget.wtimbigchat',
	requires: [
		'Sonicle.Date',
		'Sonicle.picker.RemoteDate',
		'Sonicle.webtop.core.model.IMMessageGrid',
		'Sonicle.webtop.core.model.IMChatSearchGrid'
	],
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getViewModel(), {
			foStatusIcon: {
				bind: {bindTo: '{friendStatus}'},
				get: function(val) {
					return WTF.cssIconCls(WT.XID, 'im-pstatus-'+val);
				}
			},
			foStatusText: {
				bind: {bindTo: '{friendStatus}'},
				get: function(val) {
					return WT.res('im.pstatus.'+val);
				}
			}
		});
	},
	
	initComponent: function() {
		var me = this;
		me.uploadTargetId = Ext.id(null, 'gridpanel');
		me.callParent(arguments);
		
		me.add([{
			region: 'center',
			xtype: 'container',
			layout: 'card',
			reference: 'card',
			activeItem: 0,
			items: [
				me.createTodayCmp(me.uploadTargetId),
				{
					xtype: 'grid',
					reference: 'gphistory',
					border: false,
					rowLines: false,
					viewConfig: {
						markDirty: false,
						stripeRows: false,
						enableTextSelection: true,
						deferEmptyText: false,
						emptyText: WT.res('wtimchat.gphistory.emp')
					},
					store: {
						model: 'Sonicle.webtop.core.model.IMMessageGrid',
						proxy: WTF.apiProxy(WT.ID, 'ManageGridIMChatMessages', 'data', {
							extraParams: {
								chatId: me.chatId
							}
						}),
						listeners: {
							beforeload: function(s) {
								WTU.applyExtraParams(s, {
									date: Ext.Date.format(me.getVM().get('chatDate'), 'Y-m-d')
								});
							}
						}
					},
					columns: [
						{
							xtype: 'wtchatmessagecolumn',
							dataIndex: 'id',
							dateFormat: WT.getShortDateFmt(),
							flex: 1
						}
					]
			}]
		}, {
			region: 'east',
			xtype: 'gridpanel',
			reference: 'gpchatsearch',
			title: WT.res('wtimchat.gpchatsearch.tit'),
			border: true,
			collapsed: true,
			collapsible: true,
			store: {
				autoLoad: false,
				model: 'Sonicle.webtop.core.model.IMChatSearchGrid',
				proxy: WTF.apiProxy(WT.ID, 'ManageGridIMChatSearch', 'data', {
					extraParams: {
						chatId: me.chatId
					}
				}),
				groupField: 'date',
				groupDir: 'DESC',
				//sorters: ['date', 'timestamp'],
				listeners: {
					beforeload: function(s) {
						s.getProxy().abort();
					}
				}
			},
			columns: [{
				dataIndex: 'id',
				renderer: function(val, meta, rec, rIdx, colIdx, sto) {
					var html = '';
					html += Ext.String.htmlEncode(Ext.Date.format(rec.get('timestamp'), me.dateFormat + ' ' + me.timeFormat));
					html += '<br>';
					html += Ext.String.htmlEncode(rec.get('fromNick'));
					html += '<br>';
					html += '<span style="font-size:0.9em;color:grey;">';
					html += Ext.String.htmlEncode(rec.get('text'));
					html += '</span>';
					return html; 
				},
				flex: 1
			}],
			features: [{
				ftype: 'grouping',
				groupHeaderTpl: [
					'{groupValue:this.formatValue} ({children.length})',
					{
						format: me.dateFormat,
						formatValue: function(v) {
							return Ext.Date.format(v, this.format);
						}
					}
				]
			}],
			dockedItems: [
				{
					xtype: 'textfield',
					dock: 'top',
					reference: 'fldchatsearch',
					hideFieldLabel: true,
					emptyText: WT.res('textfield.search.emp'),
					triggers: {
						clear: {
							type: 'soclear'
						}
					},
					listeners: {
						change: {
							fn: function(s) {
								this.searchChat(s.getValue());
							},
							scope: me,
							options: {buffer: 300}
						}
					}
				}
			],
			listeners: {
				beforeexpand: function() {
					me.lref('pnlemojis').collapse();
				},
				expand: function() {
					me.lref('fldchatsearch').focus(true, 100);
				},
				rowdblclick: function(s, rec) {
					me.showDate(rec.get('date'), rec.get('id'));
				}
			},
			width: '40%'
		}, {
			region: 'south',
			xtype: 'soemojipicker',
			reference: 'pnlemojis',
			header: false,
			collapsed: true,
			collapsible: true,
			collapseMode: 'placeholder',
			placeholder: { xtype: 'component', width: 0},
			recentsText: WT.res('soemojipicker.recents.tip'),
			peopleText: WT.res('soemojipicker.people.tip'),
			natureText: WT.res('soemojipicker.nature.tip'),
			foodsText: WT.res('soemojipicker.foods.tip'),
			activityText: WT.res('soemojipicker.activity.tip'),
			placesText: WT.res('soemojipicker.places.tip'),
			objectsText: WT.res('soemojipicker.objects.tip'),
			symbolsText: WT.res('soemojipicker.symbols.tip'),
			flagsText: WT.res('soemojipicker.flags.tip'),
			listeners: {
				select: function(s, emoji) {
					var fld = me.messageFld();
					fld.setValue(fld.getValue()+emoji);
				}
			}
		}]);
		
		me.on('afterrender', me.onAfterrender, me, {single: true});
		//me.on('activate', me.onActivate);
	},
	
	createTBar: function() {
		var me = this, arr = [];
		
		if (!me.isGroupChat) {
			arr.push(
				{
					xtype: 'tbtext',
					html: WT.res('wtimchat.tbi-status.lbl')
				}, {
					bind: {
						iconCls: '{foStatusIcon}',
						text: '{foStatusText}'
					},
					focusable: false,
					disabled: true
				},
				'-',
				//me.createAudioCallButton(),
				//me.createVideoCallButton(),
				me.createMeetingButton()
			);
		}
		
		arr.push(
			'->',
			me.createUploadBtn(me.uploadTargetId),
			'-',
			{
				xtype: 'button',
				tooltip: WT.res('wtimchat.gpchatsearch.tit'),
				iconCls: 'wt-icon-search',
				handler: function() {
					me.lref('gpchatsearch').toggleCollapse();
				}
			}, {
				xtype: 'splitbutton',
				tooltip: WT.res('wtimchat.btn-history.tip'),
				iconCls: 'wt-icon-history',
				menu: {
					xtype: 'datemenu',
					pickerCfg: {
						xtype: 'soremotedatepicker',
						ajaxUrl: WTF.requestBaseUrl(),
						ajaxExtraParams: {
							service: WT.ID,
							action: 'ManageIMChat',
							crud: 'dates',
							chatId: me.chatId
						}
					},
					listeners: {
						beforeshow: function(s) {
							var date = me.getVM().get('chatDate');
							if (date && s.picker) s.picker.setValue(date);
						},
						select: function(s, date) {
							me.showDate(date);
						}
					}
				},
				handler: function() {
					me.showDate(new Date());
				}
		});
		return arr;
	},
	
	createBBar: function() {
		var me = this;
		return {
			xtype: 'toolbar',
			ui: 'footer',
			items: [{
					xtype: 'button',
					ui: 'default-toolbar',
					glyph: 'xf118@FontAwesome',
					tooltip: WT.res('wtimchat.btn-emoji.tip'),
					enableToggle: true,
					toggleHandler: function(s, state) {
						var cmp = me.lref('pnlemojis');
						if (state) {
							s.setGlyph('xf078@FontAwesome');
							cmp.expand();
						} else {
							s.setGlyph('xf118@FontAwesome');
							cmp.collapse();
						}
					}
				}, ' ',
				Ext.apply(me.createMessageFld(), {
					grow: true,
					growMin: 40,
					flex: 1
				}), ' ',
				me.createSendBtn(), ' '
			]
		};
	},
	
	setHotMarker: function(hot) {
		var me = this;
		me.callParent(arguments);
		me.setIconCls(WTA.ux.IMBaseChat.buildIconCls(me.isGroupChat, hot));
	},
	
	sendMessage: function(text) {
		var me = this;
		me.lref('card').getLayout().setActiveItem(me.todayCmp());
		me.callParent(arguments);
	},
	
	addUploadMessage: function(data) {
		var me = this;
		me.lref('card').getLayout().setActiveItem(me.todayCmp());
		me.callParent(arguments);
	},
	
	showDate: function(date, messageId) {
		var me = this,
				lay = me.lref('card').getLayout(),
				focusMessage = function(grid, id) {
					var rec = grid.getStore().getById(id);
					if (rec) {
						grid.getView().focusRow(rec);
						grid.setSelection(rec);
					}
				},
				gp;
		
		me.getVM().set('chatDate', date);
		if (Sonicle.Date.isToday(date)) {
			gp = me.todayCmp();
			lay.setActiveItem(gp);
			if (messageId) focusMessage(gp, messageId);
		} else {
			gp = me.lref('gphistory');
			lay.setActiveItem(gp);
			gp.getStore().load({
				callback: function(recs, op, success) {
					if (success && messageId) focusMessage(gp, messageId);
				}
			});
		}
	},
	
	searchChat: function(query) {
		var me = this,
				gp = me.lref('gpchatsearch');
		WTU.loadWithExtraParams(gp.getStore(), {query: query});
	},
	
	privates: {
		
		onAfterrender: function(s) {
			// We need this otherwise in case of first chat field won't focus!
			s.messageFld().focus(true, 200);
		}
		/*
		onActivate: function(s) {
			if (s.scrollOnActivate) {
				s.scrollOnActivate = false;
				s.scrollToEnd();
			}
			s.setHotMarker(false);
			s.messageFld().focus(true, true);
		}
		*/
	}
});
