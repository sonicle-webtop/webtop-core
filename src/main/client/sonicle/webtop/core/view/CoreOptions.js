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
		'Ext.ux.form.HSpacer',
		'Ext.ux.form.VSpacer',
		'WT.ux.panel.Separator'
	],
	
	defaults: {
		collapsible: true,
		margin: '5 5 0 5'
	},
	
	referenceHolder: true,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'panel',
			layout: 'form',
			title: WT.res('opts.account.tit'),
			items: [{
				xtype: 'textfield',
				fieldLabel: 'Campo 1'
			}, {
				xtype: 'textfield',
				fieldLabel: 'Campo 2'
			}, {
				xtype: 'combo',
				editable: false,
				store: {
					model: 'WT.model.Simple',
					proxy: WT.proxy('com.sonicle.webtop.core', 'GetLocales', 'locales')
				},
				valueField: 'id',
				displayField: 'description',
				fieldLabel: WT.res('opts.account.fld-locale.lbl')
			}]
		}, {
			xtype: 'panel',
			layout: 'form',
			title: WT.res('opts.appearance.tit'),
			items: [{
				xtype: 'combo',
				editable: false,
				store: {
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
						
						/*
						Ext.Ajax.request({
							url: 'service-request',
							params: {
								service: 'com.sonicle.webtop.core',
								action: 'SetTheme',
								theme: r[0].get('id')
							},
							success: function (r) {
								window.location.reload();
							}
						});
						*/
					},
					scope: this
				}
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
						editable: false,
						//store: 'WT.store.TFADelivery',
						valueField: 'id',
						displayField: 'description'
					}, {
						xtype: 'hspacer'
					}, {
						xtype: 'button',
						text: 'Attiva',
						handler: function() {
							me.lookupReference('delivery').getLayout().setActiveItem('email');
						}
					}, {
						xtype: 'button',
						text: 'Disattiva',
						handler: function() {
							me.lookupReference('delivery').getLayout().setActiveItem('googleauth');
						}
					}]
				}]
			}, {
				xtype: 'container',
				reference: 'delivery',
				layout: 'card',
				items: [{
					xtype: 'container',
					itemId: 'none'
				}, {
					xtype: 'container',
					itemId: 'googleauth',
					items: [{
						xtype: 'component',
						anchor: '100%',
						padding: '0 5 0 5',
						html: WT.res('opts.tfa.googleauth.html')
					}]
				}, {
					xtype: 'container',
					itemId: 'email',
					items: [{
						xtype: 'component',
						anchor: '100%',
						padding: '0 5 0 5',
						html: WT.res('opts.tfa.email.html')
					}, {
						xtype: 'container',
						layout: 'form',
						items: [{
							xtype: 'displayfield',
							value: 'ciaoooo@ciaoooo.it',
							fieldLabel: WT.res('tfa.setup.email.fld-emailaddress.lbl')
						}]
					}]
				}]
			}, {
				xtype: 'container',
				reference: 'thisdevice',
				layout: 'card',
				activeItem: 'trusted',
				items: [{
					xtype: 'container',
					itemId: 'trusted',
					layout: 'form',
					items: [{
						xtype: 'fieldset',
						layout: 'form',
						title: WT.res('opts.tfa.thisdevice.trusted.tit'),
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
	}
});
