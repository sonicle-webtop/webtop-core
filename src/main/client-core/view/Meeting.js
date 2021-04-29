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
Ext.define('Sonicle.webtop.core.view.Meeting', {
	//extend: 'Sonicle.webtop.core.sdk.Meeting'
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.form.Spacer',
		'Sonicle.form.Text',
		'WTA.ux.field.Meeting',
		'Sonicle.webtop.core.ux.MeetingBox'
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
				roomName: null,
				link: null,
				shareInfo: null,
				shareSubj: null,
				shareUnschedDesc: null,
				shareSchedDesc: null
			}
		}
	},
	
	/**
	 * @cfg {Object} [data]
	 * An object containing initial data values.
	 * 
	 * @cfg {String} [data.roomName] Value for `roomName` field.
	*/
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		me.setViewTitle(WT.res(WT.ID, 'meeting.tit', WT.getMeetingConfig().name));
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				vm = me.getVM();
		
		if (ic.data) vm.set('data', ic.data);
		WTU.applyFormulas(vm, {
			foHasLink: WTF.foIsEmpty(null, 'data.link', true),
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
									text: me.mys.res('meeting.btn-share.mni-copy.lbl'),
									iconCls: 'fa fa-bullseye',
									handler: function() {
										me.shareByCopy();
									}
								}, {
									text: me.mys.res('meeting.btn-share.mni-email.lbl'),
									iconCls: 'fa fa-envelope-o',
									disabled: !me.getMApi(),
									handler: function() {
										me.shareByEmail();
										me.closeView(false);
									}
								}, {
									text: me.mys.res('meeting.btn-share.mni-event.lbl'),
									iconCls: 'fa fa-calendar-check-o',
									disabled: !me.getCApi(),
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
		me.self.getMeetingLink(me.getVM().get('data.roomName'), {
			callback: function(success, data) {
				me.unwait();
				if (success) {
					var vm = me.getVM();
					vm.set('data.link', data.link);
					vm.set('data.shareInfo', data.embedTexts.info);
					vm.set('data.shareSubj', data.embedTexts.subject);
					vm.set('data.shareUnschedDesc', data.embedTexts.unschedDescription);
					vm.set('data.shareSchedDesc', data.embedTexts.schedDescription);
				}
			}
		});
	},
	
	shareByCopy() {
		var me = this,
			vm = me.getVM();
		Sonicle.ClipboardMgr.copy(Ext.String.format(vm.get('data.shareInfo'), vm.get('data.link')));
		WT.toast(me.mys.res('meeting.toast.info.copied'));
	},
	
	shareByEmail: function() {
		var me = this,
			vm = me.getVM(),
			meetingUrl = vm.get('data.link'),
			subject = vm.get('data.shareSubj'),
			unschedDesc = vm.get('data.shareUnschedDesc'),
			schedDesc = vm.get('data.shareSchedDesc'),
			name = WT.getVar('userDisplayName'),
			mapi = WT.getServiceApi('com.sonicle.webtop.mail'),
			fmt = Ext.String.format;
		
		if (mapi) {
			Sonicle.webtop.core.view.Meeting.promptForInfo({
				hideWhat: true,
				callback: function(ok, values) {
					if (ok) {
						var sdate = Ext.isDate(values[1]) ? Ext.Date.format(values[1], WT.getShortDateTimeFmt()) + ' ('+values[2]+')' : null,
								subj = fmt(subject, name),
								desc = sdate ? fmt(schedDesc, name, sdate, meetingUrl) : fmt(unschedDesc, name, meetingUrl),
								format = mapi.getComposeFormat();
						
						mapi.newMessage({
							format: format,
							subject: subj,
							content: (format === 'html') ? Sonicle.String.htmlEncodeLineBreaks(desc.linkify()) : desc,
							meetingUrl: meetingUrl,
							meetingSchedule: values[1],
							meetingScheduleTz: values[2]
						}, {dirty: true, appendContent: false});
					}
				}
			});
		}
	},
	
	shareByEvent() {
		var me = this,
			vm = me.getVM(),
			meetingUrl = vm.get('data.link'),
			unschedSubj = vm.get('data.shareSubj'),
			unschedDesc = vm.get('data.shareUnschedDesc'),
			name = WT.getVar('userDisplayName'),
			capi = WT.getServiceApi('com.sonicle.webtop.calendar'),
			fmt = Ext.String.format;
		
		if (capi) {
			Sonicle.webtop.core.view.Meeting.promptForInfo({
				hideWhat: true,
				callback: function(ok, values) {
					if (ok) {
						var SoD = Sonicle.Date,
								schedAt = SoD.idate(values[1], true);
						capi.addEvent(Sonicle.Utils.applyIfDefined({
								title: fmt(unschedSubj, name),
								location: meetingUrl,
								description: fmt(unschedDesc, name, meetingUrl)
							}, {
								startDate: schedAt,
								endDate: SoD.add(schedAt, {minutes: 30}),
								timezone: values[2]
						}), {dirty: true});
					}
				}
			});
		}
	},
	
	privates: {
		getMApi: function() {
			return WT.getServiceApi('com.sonicle.webtop.mail');
		},
		
		getCApi: function() {
			return WT.getServiceApi('com.sonicle.webtop.calendar');
		}
	},
	
	statics: {
		promptSubject: function(opts) {
			opts = opts || {};
			var me = this;
			WT.prompt(WT.res('meeting.prompt.subject.txt'), {
				title: WT.res('meeting.prompt.subject.tit'),
				fn: function(bid, subject, cfg) {
					Ext.callback(opts.callback, opts.scope || me, [bid === 'ok', subject]);
				}
			});
		},
		
		promptForInfo: function(opts) {
			opts = opts || {};
			var me = this,
				hideWhat = opts.hideWhat === true,
				hideDateTime = opts.hideDateTime === true,
				whatAsRoomName = opts.whatAsRoomName === true;
				
			WT.prompt(WT.res('meeting.promptForInfo.txt'), {
				title: WT.res('meeting.promptForInfo.tit'),
				instClass: 'Sonicle.webtop.core.ux.MeetingBox',
				instConfig: {
					hideWhat: hideWhat,
					hideDateTime: hideDateTime,
					startDay: WT.getStartDay(),
					dateFormat: WT.getShortDateFmt(),
					timeFormat: WT.getShortTimeFmt(),
					whatText: WT.res(whatAsRoomName ? 'meetingBox.whatText.roomName' : 'meetingBox.whatText'),
					whenText: WT.res('meetingBox.whenText'),
					nowTooltip: WT.res('meetingBox.nowTooltip')
				},
				config: {
					value: [whatAsRoomName ? WT.getVar('userDisplayName') : null, null, WT.getTimezone()]
				},
				fn: function(bid, value, cfg) {
					Ext.callback(opts.callback, opts.scope || me, [bid === 'ok', value]);
				}
			});
		},
		
		getMeetingLink: function(roomName, opts) {
			opts = opts || {};
			var me = this;	
			WT.ajaxReq(WT.ID, 'ManageMeeting', {
				params: {
					crud: 'create',
					room: roomName
				},
				callback: function(success, json) {
					Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
				}
			});
		}
	}
});
