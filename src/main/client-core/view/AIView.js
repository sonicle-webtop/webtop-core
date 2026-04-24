/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
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
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */

Ext.define('Sonicle.webtop.core.view.AIView', {
	extend: 'WTA.sdk.DockableView',
	
	dockableConfig: {
		title: 'Chiedi alla A.I.',
		iconCls: 'wt-icon-ai',
		width: 800,
		height: 600
	},
	promptConfirm: false,
	full: true,
	answer: null,

	initComponent: function() {
		var me = this;
		
		me.callParent(arguments);

	},
	
	setBaloons: function() {
		var me = this, aiLoader = me.down('#aiLoader');
		if (aiLoader) me.remove(aiLoader);
		me.add({
			xtype: 'component',
			itemId: 'aiLoader',
			region: 'center',
			html: 
				'<div class="ai-thinking">'+
				'  <div class="ai-dot"></div>'+
				'  <div class="ai-dot"></div>'+
				'  <div class="ai-dot"></div>'+
				'</div>'
			,
			styleHtmlContent: true
		});
	},
	
	//override to render json
	setData: function(json) {
		
	},
	
	setAnswer: function(answer, format) {
		var me = this, aiLoader = me.down('#aiLoader');

		me.setTitle("A.I. answer");
		if (aiLoader) me.remove(aiLoader);
		me.add({
			xtype: 'uxiframe',
			itemId: 'aiLoader',
			region: 'center'
		});
		var f = (format || '').toLowerCase();
		var isHtml = (f === 'html' || f === 'minimal html' || f === 'minimal-html');
		me.typeText(answer, 2, isHtml);
		me.answer = answer;
	},

	setError: function(error) {
		this.setAnswer(error, 'text');
	},

	escapeHtml: function(s) {
		return String(s == null ? '' : s)
			.replace(/&/g, '&amp;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;')
			.replace(/'/g, '&#39;');
	},

	typeText: function(fullText, delay = 50, isHtml = false) {
		var me = this, aiLoader = me.down('#aiLoader');
		if (aiLoader) {
			// For non-HTML formats, escape HTML entities so model output cannot be
			// rendered as active content by the iframe. HTML formats have already
			// been sanitized server-side (AIOutputSanitizer) before arriving here.
			var text = isHtml ? (fullText == null ? '' : String(fullText)) : me.escapeHtml(fullText);

			var doc = aiLoader.getDoc();
			var pBody = window.getComputedStyle(document.body);
			var style = '<style>html,body{font-family:' + pBody.fontFamily +
				';color:' + pBody.color +
				';background:' + pBody.backgroundColor +
				';margin:0;padding:8px;font-size:13px;line-height:1.4}</style>';
			doc.open();
			doc.write(style);
			if (!isHtml) doc.write('<pre style="white-space:pre-wrap;font-family:inherit;margin:0">');
			var index = 0;
			var interval = setInterval(() => {
				var eIndex = index+10;
				if (eIndex > text.length) {
					clearInterval(interval);
					doc.write(text.substring(index,text.length));
					if (!isHtml) doc.write('</pre>');
					doc.close();
				}
				else {
					doc.write(text.substring(index,eIndex));
					index=eIndex;
				}
			}, delay);
		}

	},

	askQuestion: function(sid, action, params, func) {
		var me = this, format = (params && params.format) || 'text';
		me.setTitle("A.I. sta pensando...");
		me.setBaloons();
		WT.ajaxReq(sid, action, {
			timeout: WT.getVar("ajaxLongTimeout"),
			params: params,
			callback: function(success,json) {
				me.setTitle("A.I. ha risposto");
				if (func) func.apply(this, []);
				if (!success) return;

				if (json.success) {
					if (format == 'json') {
						var emails = JSON.parse(json.data).emails;
						me.setData(emails);
					} else {
						me.setAnswer(json.data, format);
					}
				} else {
					me.setError(json.message);
				}
			},
			failure: function(response) {
				me.setTitle("A.I. ha avuto un problema...");
				if (func) func.apply(this, []);
				if (response.status === 0) {
					// Could be a timeout or network error
					me.setError('Request failed: possible timeout or network issue');
				} else {
					me.setError('Failure with status: '+response.statusText);
				}
			}
		});
	},
	/*
	 * cfg: object with
	 *	question: string
	 *	instructions: string
	 *	format: string
	 *	when: string
	 *	//datestart: date
	 *	//dateend: date
	 */
	askRAG: function(sid, action, cfg, func) {
		var me = this, format = cfg.format || 'text';
		me.setTitle("A.I. sta pensando...");
		me.setBaloons();
		WT.ajaxReq(sid, action, {
			timeout: WT.getVar("ajaxLongTimeout"),
			params: {
				question: cfg.question,
				instructions: cfg.instructions||'',
				format: format,
				when: cfg.when||''
			},
			callback: function(success,json) {
				me.setTitle("A.I. ha risposto");
				if (func) func.apply(this, []);
				if (!success) return;

				if (json.success) {
					if (format == 'json') {
						var emails = JSON.parse(json.data).emails;
						me.setData(emails);
					} else {
						me.setAnswer(json.data, format);
					}
				} else {
					me.setError(json.message);
				}
			},
			failure: function(response) {
				me.setTitle("A.I. ha avuto un problema...");
				if (func) func.apply(this, []);
				if (response.status === 0) {
					// Could be a timeout or network error
					me.setError('Request failed: possible timeout or network issue');
				} else {
					me.setError('Failure with status: '+response.statusText);
				}
			}
		});
	}
	
});
