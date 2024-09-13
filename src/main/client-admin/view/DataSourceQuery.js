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
Ext.define('Sonicle.webtop.core.admin.view.DataSourceQuery', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.form.Separator',
		'Sonicle.form.field.CodeEditor'
	],
	uses: [
		'WTA.sdk.OkView'
	],
	
	dockableConfig: {
		title: '{dataSourceQuery.tit}',
		iconCls: 'wtadm-icon-dataSourceQuery',
		width: 400,
		height: 480
	},
	
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.core.admin.model.DataSourceQuery',
	actionsResPrefix: 'dataSourceQuery',
	
	/**
	 * @cfg {String} domainId
	 * Target domain ID.
	 */
	domainId: null,
	
	/**
	 * cfg {String} dataSourceName
	 * The name of the target data-source referenced in model's data. Optional.
	 */
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			forcePagination: WTF.checkboxBind('record', 'forcePagination')
		});
	},
	
	returnModelExtraParams: function() {
		return {
			domainId: this.domainId
		};
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'wtfieldspanel',
					paddingTop: true,
					paddingSides: true,
					modelValidation: true,
					defaults: {
						labelWidth: 100
					},
					items: [
						{
							xtype: 'textfield',
							reference: 'fldname',
							bind: '{record.name}',
							fieldLabel: me.res('dataSourceQuery.fld-name.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textareafield',
							bind: '{record.description}',
							fieldLabel: me.res('dataSourceQuery.fld-description.lbl'),
							anchor: '100%'
						}, {
							xtype: 'checkbox',
							bind: '{forcePagination}',
							hideEmptyLabel: false,
							boxLabel: me.res('dataSourceQuery.fld-forcePagination.lbl')
						}
					]
				}, {
					xtype: 'wtfieldspanel',
					paddingSides: true,
					paddingBottom: true,
					modelValidation: true,
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					items: [
						{
							xtype: 'socodeditor',
							bind: '{record.rawSql}',
							fieldLabel: me.res('dataSourceQuery.fld-rawSql.lbl'),
							labelAlign: 'top',
							flex: 1
						}, {
							xtype: 'button',
							ui: 'default-toolbar',
							text: me.res('dataSourceQuery.act-openSqlQueryEditor.lbl'),
							handler: function() {
								me.openSqlEditorUI();
							}
						}
					],
					flex: 1
				}
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	editRawSql: function(dataSourceId, pagination, rawSql, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(WT.ID, 'sdk.SQLQueryEditor', {
				swapReturn: true,
				viewCfg: {
					dockableConfig: {modal: true},
					targetName: me.dataSourceName,
					runService: me.mys.ID,
					runAction: 'DataSourceQueryTester',
					runParams: {
						domainId: me.domainId
					},
					data: {
						dataSourceId: dataSourceId,
						pagination: pagination,
						rawSql: rawSql
					}
				}
			});
		vw.on('viewok', function(s, data) {
			Ext.callback(opts.callback, opts.scope || me, [true, data]);
		});
		vw.showView();
	},
	
	privates: {
		onViewLoad: function(s, success) {
			if (!success) return;
			var me = this;
			me.lref('fldname').focus(true);
		},
		
		openSqlEditorUI: function() {
			var me = this,
				mo = me.getModel();
			me.editRawSql(mo.get('dataSourceId'), mo.get('forcePagination'), mo.get('rawSql'), {
				callback: function(success, data) {
					if (success) {
						mo.set('rawSql', data.rawSql);
					}
				}
			});
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
