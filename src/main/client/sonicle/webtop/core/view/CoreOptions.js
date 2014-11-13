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
Ext.define('Sonicle.webtop.core.view.CoreOptions', {
	alternateClassName: 'WT.view.CoreOptions',
	extend: 'WT.sdk.OptionPanel',
	requires: [
		'WT.store.TFADelivery',
		'WT.model.Simple',
		'WT.model.Options',
		'WT.store.TFADelivery',
		'Ext.ux.form.HSpacer',
		'Ext.ux.form.VSpacer'
	],
	controller: Ext.create('WT.view.CoreOptionsC'),
	model: 'WT.model.Options',
	idField: 'id',
	
	referenceHolder: true,
	defaults: {
		collapsible: true,
		margin: '5 5 0 5'
	},
	
	listeners: {
		load: 'onFormLoad',
		save: 'onFormSave'
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'panel',
			layout: 'form',
			title: WT.res('opts.account.tit'),
			items: [{
				xtype: 'hidden',
				name: 'id'
			}, {
				xtype: 'textfield',
				name: 'userId',
				fieldLabel: WT.res('opts.account.fld-userId.lbl')
			}, {
				xtype: 'textfield',
				name: 'displayName',
				fieldLabel: WT.res('opts.account.fld-displayName.lbl')
			}, {
				xtype: 'combo',
				name: 'locale',
				editable: false,
				store: {
					autoLoad: true,
					model: 'WT.model.Simple',
					proxy: WT.proxy('com.sonicle.webtop.core', 'GetLocales', 'locales')
				},
				valueField: 'id',
				displayField: 'description',
				fieldLabel: WT.res('opts.account.fld-locale.lbl')
			}]
		}, {
			xtype: 'panel',
			title: WT.res('opts.appearance.tit'),
			items: [{
				xtype: 'container',
				layout: 'form',
				items: [{
					xtype: 'combo',
					name: 'theme',
					editable: false,
					store: {
						autoLoad: true,
						model: 'WT.model.Simple',
						proxy: WT.proxy('com.sonicle.webtop.core', 'GetThemes', 'themes')
					},
					valueField: 'id',
					displayField: 'description',
					fieldLabel: WT.res('opts.appearance.fld-theme.lbl'),
					listeners: {
						select: function(c,r,o) {
							WT.ajaxReq('com.sonicle.webtop.core', 'SetTheme', {
								params: {
									theme: r[0].get('id')
								},
								callback: function(success, o) {
									if(success) window.location.reload();
								}
							});
						},
						scope: this
					}
				}, {
					xtype: 'combo',
					name: 'laf',
					editable: false,
					store: {
						autoLoad: true,
						model: 'WT.model.Simple',
						proxy: WT.proxy('com.sonicle.webtop.core', 'GetLooksAndFeels', 'lafs')
					},
					valueField: 'id',
					displayField: 'description',
					fieldLabel: WT.res('opts.appearance.fld-laf.lbl')
				}]
			}]
		}, {
			xtype: 'panel',
			title: WT.res('opts.tfa.tit'),
			items: [{
				xtype: 'container',
				layout: 'form',
				items: [{
					xtype : 'fieldcontainer',
					layout: 'hbox',
					fieldLabel: WT.res('opts.tfa.fld-delivery.lbl'),
					items: [{
						xtype: 'combo',
						reference: 'flddelivery',
						name: 'tfaDelivery',
						editable: false,
						store: Ext.create('WT.store.TFADelivery'),
						valueField: 'id',
						displayField: 'description'
					}, {
						xtype: 'hspacer'
					}, {
						xtype: 'button',
						text: 'Attiva',
						handler: function() {
							me.lookupReference('delivery').getLayout().setActiveItem('email');
						},
						bind: {
							hidden: '{isTFAEnabled}'
						}
					}, {
						xtype: 'button',
						text: 'Disattiva',
						handler: function() {
							me.lookupReference('delivery').getLayout().setActiveItem('googleauth');
						},
						bind: {
							hidden: '{!isTFAEnabled}'
						}
					}]
				}]
			}, {
				xtype: 'container',
				reference: 'delivery',
				layout: 'card',
				bind: {
					activeItem: '{activeDelivery}'
				},
				items: [{
					xtype: 'container',
					itemId: 'none'
				}, {
					xtype: 'container',
					itemId: 'googleauth',
					items: [{
						xtype: 'component',
						padding: '0 5 0 5',
						html: WT.res('opts.tfa.googleauth.html')
					}]
				}, {
					xtype: 'container',
					itemId: 'email',
					items: [{
						xtype: 'component',
						padding: '0 5 0 5',
						html: WT.res('opts.tfa.email.html')
					}, {
						xtype: 'container',
						layout: 'form',
						items: [{
							xtype: 'displayfield',
							name: 'tfaEmailAddress',
							fieldLabel: WT.res('tfa.setup.email.fld-emailaddress.lbl')
						}]
					}]
				}]
			}, {
				xtype: 'container',
				reference: 'thisdevice',
				layout: 'card',
				bind: {
					activeItem: '{activeThisDevice}'
				},
				items: [{
					xtype: 'container',
					itemId: 'trusted',
					layout: 'form',
					items: [{
						xtype: 'fieldset',
						layout: 'form',
						//title: WT.res('opts.tfa.thisdevice.trusted.tit'),
						bind: {
							title: '{thisTrustedOn}'
						},
						items: [{
							xtype: 'component',
							html: WT.res('opts.tfa.thisdevice.trusted.html')
						}, {
							xtype: 'vspacer'
						}, {
							xtype: 'button',
							itemId: 'untrustthis',
							text: WT.res('opts.tfa.btn-untrustthis.lbl'),
							handler: ''
						}]
					}]
				}, {
					xtype: 'container',
					itemId: 'nottrusted',
					layout: 'form',
					items: [{
						xtype: 'fieldset',
						title: WT.res('opts.tfa.thisdevice.nottrusted.tit'),
						items: [{
							xtype: 'component',
							html: WT.res('opts.tfa.thisdevice.nottrusted.html')
						}]
					}]
				}]
			}, {
				xtype: 'container',
				reference: 'otherdevices',
				layout: 'form',
				items: [{
					xtype: 'fieldset',
					title: WT.res('opts.tfa.otherdevices.tit'),
					items: [{
						xtype: 'component',
						html: WT.res('opts.tfa.otherdevices.html')
					}, {
						xtype: 'vspacer'
					}, {
						xtype: 'button',
						itemId: 'untrustother',
						text: WT.res('opts.tfa.btn-untrustother.lbl'),
						handler: ''
					}]
				}]
			}]
		});
		me.add({
			xtype: 'button',
			text: 'Load',
			handler: 'onLoadClick'
		});
		me.add({
			xtype: 'button',
			text: 'Sync',
			handler: 'onSyncClick'
		});
	},
	
	viewModel: {
		data: {
			values: null
		},
		
		formulas: {
			isTFAEnabled: function(get) {
				var values = get('values');
				if(!values) return false;
				return !Ext.isEmpty(values.tfaDelivery);
			},
			
			activeDelivery: function(get) {
				var values = get('values');
				if(!values) return 'none';
				return WT.returnIf(values.tfaDelivery, 'none');
			},
			
			activeThisDevice: function(get) {
				var values = get('values');
				if(!values) return 'nottrusted';
				return WT.returnIf(values.tfaIsTrusted, 'nottrusted');
			},
			
			thisTrustedOn: function(get) {
				var tit = WT.res('opts.tfa.thisdevice.trusted.tit');
				var values = get('values');
				if(!values) return tit;
				return Ext.String.format(tit, values.tfaTrustedOn);
			}
		}
	}
});
