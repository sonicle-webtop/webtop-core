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
Ext.define('Sonicle.webtop.core.ux.grid.column.ChatMessage', {
	alternateClassName: 'WTA.ux.grid.column.ChatMessage',
	extend: 'Ext.grid.column.Template',
	alias: 'widget.wtchatmessagecolumn',
	requires: [
		'Ext.XTemplate'
	],
	
	// https://codepen.io/zenorocha/pen/eZxYOK
	
	dateFormat: 'Y-m-d',
	timeFormat: 'H:i',
	senderIdField: 'fromId',
	senderNickField: 'fromNick',
	isSentField: 'isSent',
	timestampField: 'timestamp',
	actionField: 'action',
	dataField: 'data',
	textField: 'text',
	
	hiddenMetaCls: 'wt-'+'chatmsgcol-hidmeta',
	messageCls: 'wt-'+'chatmsgcol-msg',
	messageTextCls: 'wt-'+'chatmsgcol-msg-text',
	messageSentCls: 'wt-'+'chatmsgcol-msg-sent',
	messageReceivedCls: 'wt-'+'chatmsgcol-msg-received',
	messageSenderCls: 'wt-'+'chatmsgcol-sender',
	messageMetaCls: 'wt-'+'chatmsgcol-msgmeta',
	messageMetaTimeCls: 'wt-'+'chatmsgcol-msgmeta-time',
	fileWrapCls: 'wt-'+'chatmsgcol-filewrap',
	fileBodyCls: 'wt-'+'chatmsgcol-filebody',
	fileBodyIconCls: 'wt-'+'chatmsgcol-filebody-icon',
	fileBodyTextCls: 'wt-'+'chatmsgcol-filebody-text',
	fileMetaCls: 'wt-'+'chatmsgcol-filemeta',
	imagefileBodyCls: 'wt-'+'chatmsgcol-imgfilebody',
	sysMessageCls: 'wt-'+'chatmsgcol-sysmsg',
	infoSysMessageCls: 'wt-'+'chatmsgcol-sysmsg-info',
	warnSysMessageCls: 'wt-'+'chatmsgcol-sysmsg-warn',
	
	tpl: [
		'<tpl if="action == \'none\' || action == \'file\'">',
			'<tpl if="isSent">',
				'<div class="{messageCls} {messageSentCls}">',
				'<span class="{hiddenMetaCls}"><br></span>',
			'<tpl else>',
				'<div class="{messageCls} {messageReceivedCls}">',
					'<span class="{messageSenderCls}" data-content="{nick}"></span><br>',
			'</tpl>',
					'<span class="{hiddenMetaCls}">{msgClipText}</span>',
			'<tpl if="action == \'none\'">',
					'<span class="{messageTextCls}">{text}</span>',
			'<tpl else>',
					'<a class="{fileWrapCls}" href="{url}" target="_blank">',
						'<tpl if="isImage">',
							'<div class="{imagefileBodyCls}">',
								'<img src="{url}" alt="" />',
							'</div>',
							'<span class="{hiddenMetaCls}">{fileClipText}</span>',
						'<tpl else>',
							'<div class="{fileBodyCls}">',
								'<div class="{fileBodyIconCls} {fileIconCls}"></div>',
								'<span class="{fileBodyTextCls}">',
									'<span class="{messageTextCls}">{text}</span>',
									'<span class="{hiddenMetaCls}">{fileClipText}</span>',
								'</span>',
							'</div>',
						'</tpl>',
					'</a>',
					'<span class="{fileMetaCls}" data-content="{size}"></span>',
			'</tpl>',
					'<span class="{messageMetaCls}">',
						'<span class="{messageMetaTimeCls}" data-content="{time}"></span>',
					'</span>',
				'</div>',
		'<tpl else>',
				'<div style="width:{width}px;" class="{sysMessageCls} {sysMessageActionCls}">',
					'<span class="{messageTextCls} x-unselectable">{text}</span>',
				'</div>',
		'</tpl>',
		{
			compiled: true
		}
	],
	
	defaultRenderer: function(v, meta, rec) {
		var me = this,
			action = rec.get(me.actionField),
			ts = rec.get(me.timestampField),
			text = rec.get(me.textField),
			time = Ext.isEmpty(ts) ? '' : Ext.Date.format(ts, me.timeFormat),
			htmlText = text ? text : '';
		
		if (action === 'none' || action === 'file') {
			var isSent = rec.get(me.isSentField) === true,
					nick = rec.get(me.senderNickField),
					msgClipText = me.buildMsgClipText(ts, nick),
					obj;
			if (action === 'none') {
				obj = {
					text: htmlText.linkify()
				};
			} else if (action === 'file') {
				var data = Ext.JSON.decode(rec.get(me.dataField), true) || {};
				obj = {
					fileWrapCls: me.fileWrapCls,
					fileBodyCls: me.fileBodyCls,
					fileBodyIconCls: me.fileBodyIconCls,
					fileBodyTextCls: me.fileBodyTextCls,
					fileMetaCls: me.fileMetaCls,
					imagefileBodyCls: me.imagefileBodyCls,
					isImage: Ext.String.startsWith(data.mime, 'image'),
					text: htmlText,
					url: data.url ? data.url : '#',
					size: Sonicle.Bytes.format(data.size),
					fileIconCls: WTF.fileTypeCssIconCls(data ? data.ext : '', 'm'),
					fileClipText: me.buildFileClipText(data.url, data.mime, data.size)
				};
			}
			return me.tpl.apply(Ext.apply({
				action: action,
				messageCls: me.messageCls,
				messageTextCls: me.messageTextCls,
				messageSentCls: me.messageSentCls,
				messageReceivedCls: me.messageReceivedCls,
				messageSenderCls: me.messageSenderCls,
				messageMetaCls: me.messageMetaCls,
				messageMetaTimeCls: me.messageMetaTimeCls,
				hiddenMetaCls: me.hiddenMetaCls,
				isSent: isSent,
				nick: me.escapeAttr(nick),
				time: time,
				msgClipText: msgClipText
			}, obj || {}));
		} else {
			var actionCls = me.infoSysMessageCls;
			if (action === 'date') {
				text = Ext.Date.format(ts, me.dateFormat);
			} else if (action === 'close') {
				text = WT.res(WT.ID, 'wtchatmessagecolumn.close', rec.get(me.senderNickField));
			} else if (action === 'warn') {
				text = WT.res('wtchatmessagecolumn.warn.'+text);
				actionCls = me.warnSysMessageCls;
			}
			return me.tpl.apply({
				action: action,
				messageTextCls: me.messageTextCls,
				sysMessageCls: me.sysMessageCls,
				sysMessageActionCls: actionCls,
				width: Ext.util.TextMetrics.measure(this.el, text).width +10,
				time: time,
				text: text
			});	
		}
	},
	
	buildMsgClipText: function(ts, nick) {
		return '['
			+ Ext.Date.format(ts, this.dateFormat + ' ' + this.timeFormat)
			+ '][' + Ext.String.htmlEncode(nick) + ']&nbsp;';
	},
	
	buildFileClipText: function(url, mime, size) {
		return '&nbsp;[' + mime + ', ' + size + ', ' + Ext.String.htmlEncode(url) + ']';
	},
	
	escapeAttr: function(s) {
		return s.replace(/\"/g, '\\"');
	}
});
