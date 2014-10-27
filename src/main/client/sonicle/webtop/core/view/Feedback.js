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
Ext.define('Sonicle.webtop.core.view.Feedback', {
	extend: 'WT.sdk.FormView',
	requires: [
		'Sonicle.webtop.core.view.FeedbackC',
		'Ext.ux.form.Spacer',
		'Sonicle.webtop.core.model.AvailService'
	],
	controller: Ext.create('Sonicle.webtop.core.view.FeedbackC'),
	
	h2cCanvas: null,
	jpegQuality: 0.7, // 0.1 to 1 (1 = 100%)
	
	items: [{
		xtype: 'component',
		html: WT.res('feedback.text')
	}, 
	Ext.create('Ext.ux.form.Spacer', {height: 20}), 
	{
		xtype: 'hiddenfield',
		name: 'timestamp'
	}, {
		xtype: 'combo',
		editable: false,
		store: {
			model: 'Sonicle.webtop.core.model.AvailService',
			proxy: WT.ajaxProxy('com.sonicle.webtop.core', 'GetAvailableServices', 'services')
		},
		valueField: 'id',
		displayField: 'description',
		fieldLabel: WT.res('feedback.f-service.lbl')
	}, {
		xtype: 'textareafield',
		name: 'message',
		allowBlank: false,
		anchor: '100%',
		fieldLabel: WT.res('feedback.f-message.lbl')
	}, {
		xtype: 'checkbox',
		name: 'anonymous',
		hideLabel: true,
		boxLabel: WT.res('feedback.f-anonymous.lbl')
	}, {
		xtype: 'checkbox',
		name: 'screenshot',
		submitValue: false,
		hideLabel: true,
		boxLabel: WT.res('feedback.f-screenshot.lbl'),
		handler: 'onScreenshotChange',
		scope: this
	}],
	buttons: [{
		text: WT.res('send'),
		handler: 'onSendClick',
		scope: this
	}, {
		text: WT.res('cancel'),
		handler: 'onCancelClick',
		scope: this
	}]
});
