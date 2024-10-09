/* 
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.DataSource', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.VMUtils',
		'Sonicle.form.Separator',
		'Sonicle.webtop.core.admin.model.DataSourceTypeLkp'
	],
	
	dockableConfig: {
		title: '{dataSource.tit}',
		iconCls: 'wtadm-icon-dataSource',
		width: 450,
		height: 480
	},
	
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.core.admin.model.DataSource',
	returnModelExtraParams: function() {
		return {
			domainId: this.domainId
		};
	},
	focusField: {'new': 'fldname', 'edit': 'fldname'},
	actionsResPrefix: 'dataSource',
	
	/**
	 * @cfg {String} domainId
	 * Target domain ID.
	 */
	domainId: null,
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foTestEnabled: {
				bind: {bindTo: '{record}', deep: true},
				get: function(mo) {
					var iemp = function(rec, field) { return Ext.isEmpty(rec.get(field)); };
					return !mo ? false : (!iemp(mo, 'type') && !iemp(mo, 'serverName') && !iemp(mo, 'databaseName'));
				}
			}
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			scrollable: true,
			autoPadding: 'ts',
			modelValidation: true,
			defaults: {
				labelAlign: 'top',
				labelSeparator: ''
			},
			items: [
				{
					xtype: 'textfield',
					reference: 'fldname',
					bind: '{record.name}',
					fieldLabel: me.res('dataSource.fld-name.lbl'),
					anchor: '100%'
				}, {
					xtype: 'textfield',
					bind: '{record.friendlyId}',
					maskRe: Sonicle.data.validator.Username.maskRe,
					fieldLabel: me.res('dataSource.fld-friendlyId.lbl'),
					plugins: [
						{
							ptype: 'sofieldavailabilitycheck',
							baseIconCls: 'wt-opacity-50',
							availableTooltipText: WT.res('sofieldavailabilitycheck.availableTooltipText'),
							unavailableTooltipText: WT.res('sofieldavailabilitycheck.unavailableTooltipText'),
							checkAvailability: function(value, done) {
								if (Ext.isEmpty(value)) return false;
								if (me.getModel().getModified('friendlyId') === undefined) return false;
								WT.ajaxReq(me.mys.ID, 'ManageDomainDataSource', {
									params: {
										crud: 'check',
										domainId: me.domainId,
										friendlyId: value
									},
									callback: function(success, json) {
										done(success ? json.data : json.message);
									}
								});
							}
						}
					],
					anchor: '100%'
				}, {
					xtype: 'textareafield',
					bind: '{record.description}',
					fieldLabel: me.res('dataSource.fld-description.lbl'),
					anchor: '100%'
				}, {
					xtype: 'soformseparator',
					title: me.res('dataSource.dbserver.tit')
				},
				WTF.lookupCombo('id', 'label', {
					bind: '{record.type}',
					autoLoadOnValue: true,
					store: {
						autoLoad: true,
						model: me.mys.preNs('model.DataSourceTypeLkp'),
						proxy: WTF.proxy(me.mys.ID, 'LookupDataSourceTypes', null, {
							extraParams: {
								domainId: me.domainId
							}
						})
					},
					emptyText: me.res('dataSource.fld-type.emp'),
					fieldLabel: me.res('dataSource.fld-type.lbl'),
					anchor: '100%'
				}),
				{
					xtype: 'sofieldhgroup',
					items: [
						{
							xtype: 'textfield',
							bind: '{record.serverName}',
							inputType: 'url',
							emptyText: me.res('dataSource.fld-serverName.emp'),
							fieldLabel: me.res('dataSource.fld-server.lbl'),
							flex: 1
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'textfield',
							bind: '{record.serverPort}',
							emptyText: me.res('dataSource.fld-serverPort.emp'),
							width: 80
						}
					],
					anchor: '100%'
				}, {
					xtype: 'textfield',
					bind: '{record.databaseName}',
					fieldLabel: me.res('dataSource.fld-databaseName.lbl'),
					anchor: '100%'
				}, {
					xtype: 'sofakeinput' // Disable Chrome autofill
				}, {
					xtype: 'sofakeinput', // Disable Chrome autofill
					type: 'password'
				}, {
					xtype: 'sofieldhgroup',
					items: [
						{
							xtype: 'sofakeinput' // Disable Chrome autofill
						}, {
							xtype: 'textfield',
							bind: '{record.username}',
							plugins: 'sonoautocomplete',
							fieldLabel: me.res('dataSource.fld-username.lbl'),
							flex: 1
						}, {
							xtype: 'sohspacer',
							ui: 'small'
						}, {
							xtype: 'sofakeinput', // Disable Chrome autofill
							type: 'password'
						}, {
							xtype: 'textfield',
							bind: '{record.password}',
							inputType: 'password',
							plugins: 'sonoautocomplete',
							fieldLabel: me.res('dataSource.fld-password.lbl'),
							flex: 1
						}
					],
					anchor: '100%'
				}, {
					xtype: 'soformseparator',
					title: me.res('dataSource.rawProps.tit')
				}, {
					xtype: 'textfield',
					bind: '{record.driverProps}',
					fieldLabel: me.res('dataSource.fld-driverProps.lbl'),
					emptyText: me.res('dataSource.fld-driverProps.emp'),
					anchor: '100%'
				}, {
					xtype: 'textfield',
					bind: '{record.poolProps}',
					fieldLabel: me.res('dataSource.fld-poolProps.lbl'),
					emptyText: me.res('dataSource.fld-poolProps.emp'),
					anchor: '100%'
				}
			]
		});
	},
	
	initTBar: function() {
		var me = this;
		me.dockedItems = Sonicle.Utils.mergeDockedItems(me.dockedItems, 'top', [
			me.createTopToolbar1Cfg(me.prepareTopToolbarItems())
		]);
	},
	
	privates: {
		prepareTopToolbarItems: function() {
			var me = this;
			return [
				{
					xtype: 'button',
					bind: {
						disabled: '{!foTestEnabled}'
					},
					text: me.res('dataSource.act-testConnection.lbl'),
					handler: function() {
						me.testConnectionUI();
					}
				}
			];
		},
		
		testConnectionUI: function() {
			var me = this,
				mo = me.getModel();
			
			me.wait();
			WT.ajaxReq(me.mys.ID, 'ManageDomainDataSource', {
				params: {
					crud: 'testp',
					domainId: me.domainId,
					id: mo.getId(),
					type: mo.get('type'),
					serverName: mo.get('serverName'),
					serverPort: mo.get('serverPort'),
					databaseName: mo.get('databaseName'),
					username: mo.get('username'),
					password: mo.get('password'),
					rpassword: mo.get('rpassword'),
					driverProps: mo.get('driverProps')
				},
				callback: function(success, json) {
					me.unwait();
					if (success) {
						WT.info(me.res('dataSource.info.check'));
					} else {
						WT.error(me.res('dataSource.error.check', json.message));
					}
				}
			});
		}
	}
});
