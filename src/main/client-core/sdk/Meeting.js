/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.sdk.Meeting', {
	alternateClassName: 'WTA.sdk.Meeting',
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.form.Spacer',
		'Sonicle.form.Text',
		'WTA.ux.field.Meeting'
	],
	
	dockableConfig: {
		title: '{meeting.tit@com.sonicle.webtop.core}',
		iconCls: 'wt-icon-meeting',
		width: 450,
		height: 220
	},
	modeTitle: false,
	
	viewModel: {
		data: {
			data: {
				link: null,
				shareInfo: null,
				shareMailSubject: null,
				shareMailMessage: null,
				shareEventTitle: null,
				shareEventDescription: null
			}
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		me.setViewTitle(WT.res(WT.ID, 'meeting.tit', WT.getMeetingConfig().name));
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getVM();
		
		WTU.applyFormulas(vm, {
			foHasLink: WTF.foIsEmpty(null, 'data.link', true),
			foCopyEnabled: WTF.foIsEmpty(null, 'data.shareInfo', true),
			foEmailEnabled: WTF.foIsEmpty(null, 'data.shareMailMessage', true),
			foEventEnabled: WTF.foIsEmpty(null, 'data.shareEventDescription', true),
			foIsMeetingLocation: WTF.foGetFn(null, 'data.link', function(val) {
				return WT.isMeetingUrl(val);
			})
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtpanel',
			layout: 'center',
			items: [
				{
					xtype: 'wtpanel',
					items: [
						{
							xtype: 'sotext',
							text: me.mys.res('meeting.info.tit'),
							cls: 'wt-theme-text-tit',
							style: 'font-size:1.2em'
						}, {
							xtype: 'sotext',
							text: me.mys.res('meeting.info.txt'),
							cls: 'wt-theme-text-sub',
							style: 'font-size:0.9em'
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'wtmeetingfield',
							bind: '{data.link}',
							showClear: false,
							showJoin: false,
							listeners: {
								copy: function() {
									WT.toast(WT.res('meeting.toast.link.copied'));
								}
							},
							width: '100%'
						}, {
							xtype: 'sospacer'
						}
					],
					bbar: [
					    '->',
					    {
							xtype: 'button',
							text: me.mys.res('meeting.btn-share.lbl'),
							iconCls: 'fa fa-share-alt',
							menu: [
								{
									bind: {
										disabled: '{!foCopyEnabled}'
									},
									text: me.mys.res('meeting.btn-share.mni-copy.lbl'),
									iconCls: 'fa fa-bullseye',
									handler: function() {
										me.shareByCopy();
									}
								}, {
									bind: {
										disabled: '{!foEmailEnabled}'
									},
									text: me.mys.res('meeting.btn-share.mni-email.lbl'),
									iconCls: 'fa fa-envelope-o',
									handler: function() {
										me.shareByEmail();
										me.closeView(false);
									}
								}, {
									bind: {
										disabled: '{!foEventEnabled}'
									},
									text: me.mys.res('meeting.btn-share.mni-event.lbl'),
									iconCls: 'fa fa-calendar-check-o',
									handler: function() {
										me.shareByEvent();
										me.closeView(false);
									}
								}
							]
					    }, {
							bind: {
								disabled: '{!foHasLink}'
							},
							text: me.mys.res('meeting.btn-start.lbl'),
							iconCls: 'fa fa-video-camera',
							handler: function() {
								Sonicle.URLMgr.open(me.getVM().get('data.link'), true);
								me.closeView(false);
							}
					    },
					    '->'
					],
					maxWidth: 400
				}
			]
		});
		me.on('viewshow', me.onViewShow, me);
	},
	
	onViewShow: function() {
		var me = this;
		me.wait();
		me.getMeetingLink({
			callback: function(success, data) {
				me.unwait();
				if (success) {
					var vm = me.getVM();
					vm.set('data.link', data.link);
					vm.set('data.shareInfo', data.embedTexts.info);
					vm.set('data.shareMailSubject', data.embedTexts.emailSubject);
					vm.set('data.shareMailMessage', data.embedTexts.emailMessage);
					vm.set('data.shareEventTitle', data.embedTexts.eventTitle);
					vm.set('data.shareEventDescription', data.embedTexts.eventDescription);
				}
			}
		});
	},
	
	getMeetingLink: function(opts) {
		opts = opts || {};
		var me = this;	
		WT.ajaxReq(me.mys.ID, 'ManageMeeting', {
			params: {
				crud: 'create'
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	shareByCopy: function() {
		var me = this,
			vm = me.getVM();
		Sonicle.ClipboardMgr.copy(Ext.String.format(vm.get('data.shareInfo'), vm.get('data.link')));
		WT.toast(me.mys.res('meeting.toast.info.copied'));
	},
	
	shareByEmail: function() {
		var me = this,
			vm = me.getVM(),
			name = WT.getVar('userDisplayName'),
			mapi = WT.getServiceApi('com.sonicle.webtop.mail'),
			fmt = Ext.String.format;
		
		if (mapi) {
			var format = mapi.getComposeFormat(),
					message = fmt(vm.get('data.shareMailMessage'), name, vm.get('data.link')),
					content = (format === 'html') ? Sonicle.String.htmlEncodeLineBreaks(message.linkify()) : message;
			mapi.newMessage({
					format: format,
					subject: fmt(vm.get('data.shareMailSubject'), name),
					content: content
				}, {
					dirty: true,
					//contentReady: false,
					appendContent: false
			});
		}
	},
	
	shareByEvent: function() {
		var me = this,
			vm = me.getVM(),
			name = WT.getVar('userDisplayName'),
			capi = WT.getServiceApi('com.sonicle.webtop.calendar'),
			fmt = Ext.String.format;
		
		if (capi) {
			capi.addEvent({
				title: fmt(vm.get('data.shareEventTitle'), name),
				location: vm.get('data.link'),
				description: fmt(vm.get('data.shareEventDescription'), name, vm.get('data.link'))
			}, {
				dirty: true,
				callback: function(success) {
					if (success) capi.reloadEvents();
				}
			});
		}
	}
});
