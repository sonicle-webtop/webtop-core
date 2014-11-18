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
Ext.define('Sonicle.webtop.core.view.CoreOptionsC', {
	alternateClassName: 'WT.view.CoreOptionsC',
	extend: 'Ext.app.ViewController',
	
	onFormLoad: function() {
		this.getViewModel().set('values', this.getView().model.getData());
	},
	
	onFormSave: function() {
		this.getViewModel().set('values', this.getView().model.getData());
	},
	
	onLoadClick: function() {
		this.getView().loadForm('admin');
	},
	
	onSaveClick: function() {
		this.getView().saveForm();
	},
	
	onTFAEnableClick: function() {
		alert('TODO');
	},
	
	onTFADisableClick: function() {
		alert('TODO');
	},
	
	onUntrustThisClick: function() {
		var me = this;
		WT.confirm(WT.res('confirm.areyousure'), function(bid) {
			if(bid === 'yes') {
				WT.ajaxReq(WT.ID, 'TFAUntrustDevice', {
					params: {which: 'this'},
					callback: function(success) {
						if(success) me.getView().loadForm();
					}
				});
			}
		});
	},
	
	onUntrustOtherClick: function() {
		WT.confirm(WT.res('confirm.areyousure'), function(bid) {
			if(bid === 'yes') {
				WT.ajaxReq(WT.ID, 'TFAUntrustDevice', {
					params: {which: 'other'}
				});
			}
		});
	}
	
});
