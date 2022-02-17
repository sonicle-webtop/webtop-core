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
Ext.define('Sonicle.webtop.core.ux.IMMinChat', {
	alternateClassName: 'WTA.ux.IMMinChat',
	extend: 'Sonicle.webtop.core.ux.IMBaseChat',
	alias: 'widget.wtimminchat',
	uses: [
		'Sonicle.menu.Emoji'
	],
	
	header: false,
	
	initComponent: function() {
		var me = this;
		me.uploadTargetId = Ext.id(null, 'gridpanel');
		me.callParent(arguments);
		
		me.add([
			Ext.apply(me.createTodayCmp(me.uploadTargetId), {
				region: 'center'
			}),
			Ext.apply(me.createMessageFld(), {
				region: 'south',
				grow: true,
				growMin: 24
			})
		]);
	},
	
	createBBar: function() {
		var me = this, arr = [];
		
		arr.push(' ',
			{
				xtype: 'button',
				ui: 'default-toolbar',
				iconCls: 'wt-icon-emoji',
				tooltip: WT.res('wtimchat.btn-emoji.tip'),
				arrowVisible: false,
				menu: {
					xtype: 'soemojimenu',
					alignOffset: [0, -35],
					hideOnClick: false,
					pickerConfig: {
						header: false,
						tabPosition: 'bottom',
						recentsText: WT.res('soemojipicker.recents.tip'),
						peopleText: WT.res('soemojipicker.people.tip'),
						natureText: WT.res('soemojipicker.nature.tip'),
						foodsText: WT.res('soemojipicker.foods.tip'),
						activityText: WT.res('soemojipicker.activity.tip'),
						placesText: WT.res('soemojipicker.places.tip'),
						objectsText: WT.res('soemojipicker.objects.tip'),
						symbolsText: WT.res('soemojipicker.symbols.tip'),
						flagsText: WT.res('soemojipicker.flags.tip')
					},
					listeners: {
						select: function(s, emoji) {
							var fld = me.messageFld();
							fld.setValue(fld.getValue()+emoji);
						}
					}
				}
			},
			Ext.apply(me.createUploadBtn(me.uploadTargetId), {
				ui: 'default-toolbar'
			}),
			'->'
		);
		/*
		if (!me.isGroupChat) {
			arr.push(
				Ext.apply(me.createAudioCallButton(), {
					ui: 'default-toolbar'
				}),
				Ext.apply(me.createVideoCallButton(), {
					ui: 'default-toolbar'
				})
			);
		}
		*/
		arr.push(
			Ext.apply(me.createMeetingButton(), {
				ui: 'default-toolbar'
			})
		);
		
		arr.push(' ');
		return {
			xtype: 'toolbar',
			ui: 'footer',
			items: arr
		};
	}
});
