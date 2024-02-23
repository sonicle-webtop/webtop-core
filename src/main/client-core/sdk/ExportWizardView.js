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
Ext.define('Sonicle.webtop.core.sdk.ExportWizardView', {
	alternateClassName: 'WTA.sdk.ExportWizardView',
	extend: 'WTA.sdk.WizardView',
	requires: [
		'WTA.ux.data.ValueModel',
		'WTA.ux.panel.Form'
	],
	
	/**
	 * @property files
	 * Object containing allowed files per path
	 */
	files: null,
	
	disableNavAtEnd: false,
	showDoButton: true,
	endPageTitleText: '{exportwiz.end.tit}',
	useTrail: false,
	
	viewModel: {
		data: {
			file: null
		}
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getVM();
		
		me.files = me.initFiles();
		
		//me.on('beforenavigate', me.onBeforeNavigate);
		me.callParent(arguments);
		//me.on('viewclose', me.onViewClose);
	},
	
	/*onViewClose: function(s) {
	},*/
	
	initAction: function() {
		return {
			txt: 'ExportToText'
		};
	},
	
	initPages: function() {
		return {
			txt: ['end']
		};
	},
	
	initFiles: function() {
		return {
			txt: {label: WT.res('exportwiz.path.fld-path.txt'), extensions: 'csv,txt'}
		};
	},
	
	addPathPage: function() {
		var me = this, itms = [];
		Ext.iterate(me.files, function(k,v) {
			itms.push({value: k, label: v.label});
		});
		
		me.getVM().set('path', 'txt');
		me.add(me.createPathPage(
			WT.res('exportwiz.path.tit'), 
			WT.res('exportwiz.path.fld-path.tit'), 
			itms
		));
		me.onNavigate('path');
	},

	createPages: function(path) {
		var me = this;
		return [ me.createEndPage(path) ];
	},
	
	createEndPage: function() {
		var me = this;
		return {
			itemId: 'end',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: Sonicle.String.htmlLineBreaks(me.resEndPageTitleText())
			}]
		};
	},
	
	doOperationParams: function() {
		var me = this,
				path = me.getVM().get('path');
		return me.opParams(Ext.apply(me.buildDoParams(path), {
			path: path
		}));
	},
	
	onDoClick: function() {
		var me = this,
				page = me.getPageCmp('end'),
				params = me.opParams({op: 'do'}),
				obj;
		
		me.doCount++;
		if (Ext.isFunction(me.doOperationParams)) {
			obj = me.doOperationParams.call(me, []);
			if (Ext.isObject(obj)) params = Ext.apply(params, obj);
		}
		
		var href = WTF.processBinUrl(me.mys.ID,me.getAct(), params);
		window.open(href);
		me.doSuccess = true;
		me.updateButtons('end');
		me.getVM().set('result', true);
		me.fireEvent('dosuccess', me, true);
		me.closeView();
	}	
	
});
