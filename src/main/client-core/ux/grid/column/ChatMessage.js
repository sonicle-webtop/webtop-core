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
	textField: 'text',
	
	messageCls: 'wt-'+'chatmsgcol-msg',
	messageSentCls: 'wt-'+'chatmsgcol-msg-sent',
	messageReceivedCls: 'wt-'+'chatmsgcol-msg-received',
	messageSenderCls: 'wt-'+'chatmsgcol-sender',
	messageMetaCls: 'wt-'+'chatmsgcol-meta',
	messageMetaTimeCls: 'wt-'+'chatmsgcol-meta-time',
	sysMessageCls: 'wt-'+'chatmsgcol-sysmsg',
	infoSysMessageCls: 'wt-'+'chatmsgcol-sysmsg-info',
	warnSysMessageCls: 'wt-'+'chatmsgcol-sysmsg-warn',
	
	tpl: [
		'<tpl switch="action">',
			'<tpl case="none">',
				'<tpl if="isSent">',
					'<div class="{messageCls} {messageSentCls}">',
				'<tpl else>',
					'<div class="{messageCls} {messageReceivedCls}">',
					'<span class="{messageSenderCls}">{nick}</span></br>',
				'</tpl>',
					'{text}',
					'<span class="{messageMetaCls}">',
						'<span class="{messageMetaTimeCls}">{time}</span>',
					'</span>',
					'</div>',
			'<tpl default>',
				'<div style="width:{width}px;" class="{sysMessageCls} {sysMessageActionCls}">{text}</div>',
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
			vts = Ext.isEmpty(ts) ? '' : Ext.Date.format(ts, me.timeFormat),
			htmlText = Ext.isEmpty(text) ? '&nbsp;' : Ext.String.htmlEncode(text);
		
		if (action === 'none') {
			var isSent = rec.get(me.isSentField) === true;
			return me.tpl.apply({
				action: action,
				messageCls: me.messageCls,
				messageSentCls: me.messageSentCls,
				messageReceivedCls: me.messageReceivedCls,
				messageSenderCls: me.messageSenderCls,
				messageMetaCls: me.messageMetaCls,
				messageMetaTimeCls: me.messageMetaTimeCls,
				isSent: isSent,
				nick: rec.get(me.senderNickField),
				time: vts,
				text: htmlText
			});
		} else {
			if (action === 'date') {
				text = Ext.Date.format(ts, me.dateFormat);
				htmlText = Ext.String.htmlEncode(text);
			}
			return me.tpl.apply({
				action: action,
				sysMessageCls: me.sysMessageCls,
				sysMessageActionCls: me.infoSysMessageCls,
				width: Ext.util.TextMetrics.measure(this.el, text).width +10,
				time: vts,
				text: htmlText
			});	
		}
	}
});
