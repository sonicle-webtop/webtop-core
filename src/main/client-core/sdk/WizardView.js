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
Ext.define('Sonicle.webtop.core.view.WizardView', {
	alternateClassName: 'WTA.sdk.WizardView',
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.webtop.core.sdk.WizardPage',
		'Sonicle.form.Spacer'
	],
	
	layout: 'card',
	bodyPadding: 5,
	confirm: 'yn',
	
	config: {		
		/**
		 * @cfg {Boolean} [useTrail=true]
		 * 'true' to enable display of step trails, 'false' otherwise. Default is true
		 */
		useTrail: true,
		
		/**
		 * @cfg {Boolean} [infiniteTrail=false]
		 * 'true' to hide the total number of pages in trail; a question mark is displayed instead.
		 */
		infiniteTrail: false,
		
		/**
		 * @cfg {Boolean} [disableNavAtEnd=true]
		 * 'true' to disable navigation buttons in the end page; 'false' otherwise.
		 */
		disableNavAtEnd: true,
		
		/**
		 * @cfg {Boolean} [disableNavOnSuccess=true]
		 * 'true' to disable navigation buttons if doAction call was completed successfully; 'false' otherwise.
		 */
		disableNavOnSuccess: true,
		
		/**
		 * @cfg {Boolean} [showDoButton=false]
		 */
		showDoButton: false,
		
		/**
		 * @cfg {Boolean} [lockDoButton=true]
		 * 'true' to automatically disable doButton after click; 'false' otherwise.
		 */
		lockDoButton: true,
		
		/**
		 * @cfg {String} doButtonText
		 * Text to display as doButton label. Default to resource string 'wizard.btn-do.lbl'.
		 */
		doButtonText: '{wizard.btn-do.lbl}',
		
		/**
		 * @cfg {String} endPageHeaderText
		 * Text to display as header in end page. Default to resource string 'wizard.end.tit'.
		 */
		endPageTitleText: '{wizard.end.tit}'
	},
	
	/**
	 * @method initPages
	 * Override this in order to return pages definition.
	 * With only a single path return a String array:
	 *		['step1','step2','step3']
	 *		
	 * Alternatively using multipath return an Object:
	 *		{
	 *			path1: ['path1step1','path1step2','path1step3'],
	 *			path2: ['path2step1','path2step2','path2step3']
	 *		}
	 *		
	 * @return {String[]/Object} Definition object.
	 */
	initPages: Ext.emptyFn,
	
	/**
	 * @method initAction
	 * Override this in order to return remote action definition.
	 * With only a single path return a String:
	 *		'actionABC'
	 *		
	 * Alternatively using multipath return an Object:
	 *		{
	 *			path1: 'actionABC',
	 *			path2: 'actionCDE'
	 *		}
	 *		
	 * @return {String/Object} Definition object.
	 */
	initAction: Ext.emptyFn,
	
	/** 
	 * @cfg {Function} doOperationParams
	 * A custom function to be called during doAction call to obtain any 
	 * additional extraParams to include into server request.
	 * @return {Object} extraParams definition object
	*/
	
	/**
	 * @property {Array/Object} pages
	 * Pages definition.
	 */
	pages: null,
	
	/**
	 * @property {Array/Object} action
	 * Ajax action definition.
	 */
	action: null,
	
	/**
	 * @private
	 */
	isMultiPath: false,
	
	/**
	 * @private
	 */
	doSuccess: false,
	
	/**
	 * @private
	 */
	doCount: 0,
	
	viewModel: {
		data: {
			tag: null,
			path: null,
			result: null //Save here resulting data after wizard completion
		}
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getVM(),
				ic = me.getInitialConfig(),
				data = ic['data'] || {};
		
		vm.set('tag', !Ext.isEmpty(data.tag) ? data.tag : me.getId());
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, {
			pathgroup: WTF.radioGroupBind(null, 'path', me.getId()+'-pathgroup')
		}));
		
		me.pages = me.initPages();
		me.action = me.initAction();
		
		Ext.apply(me, {
			tbar: [
				'->',
				{
					xtype: 'tbtext',
					reference: 'trail'
				}
			],
			bbar: [
				'->',
				{
					reference: 'btnback',
					xtype: 'button',
					text: WT.res('wizard.btn-back.lbl'),
					handler: function() {
						me.navigate(-1);
					},
					disabled: true
				}, {
					reference: 'btnforw',
					xtype: 'button',
					text: WT.res('wizard.btn-forw.lbl'),
					handler: function() {
						me.navigate(1);
					},
					disabled: true
				}, ' ', {
					reference: 'btndo',
					xtype: 'button',
					text: me.resDoButtonText(),
					handler: function(s) {
						s.setDisabled(true); // Avoids multi-runs!
						me.onDoClick();
					},
					hidden: !me.getShowDoButton(),
					disabled: true
				}, {
					reference: 'btncancel',
					xtype: 'button',
					text: WT.res('wizard.btn-cancel.lbl'),
					handler: function() {
						if(!me.isPathSelection() && !me.hasNext(1)) {
							me.fireEvent('wizardcompleted', me);
						}
						me.closeView();
					}
				}
			]
		});
		me.callParent(arguments);
		
		me.isMultiPath = false;
		if (!Ext.isArray(me.pages)) {
			Ext.iterate(me.pages, function(k,v) {
				if(!Ext.isArray(v)) Ext.Error.raise('Invalid pages definition object');
			});
			me.isMultiPath = true;
		}
		
		if (me.isMultiPath) {
			me.addPathPage();
		} else {
			me.addPages();
		}
	},
	
	getPages: function() {
		var me = this;
		if(me.isMultiPath) {
			return me.pages[me.getVM().get('path')];
		} else {
			return me.pages;
		}
	},
	
	getAct: function() {
		var me = this;
		if(me.isMultiPath) {
			return me.action[me.getVM().get('path')];
		} else {
			return me.action;
		}
	},
	
	addPathPage: function() {
		var me = this;
		me.add(me.createPathPage('', '', {}));
		me.onNavigate('path');
	},
	
	addPages: function() {
		var me = this,
				curpath = me.getVM().get('path');
		me.add(me.createPages(curpath));
		me.onNavigate(me.getPages()[0]);
	},
	
	createPages: function(path) {
		return [];
	},
	
	createPathPage: function(title, fieldLabel, fieldItems) {
		var me = this,
				items = [];
		Ext.iterate(fieldItems, function(obj,i) {
			items.push({
				name: me.getId()+'-pathgroup',
				inputValue: obj.value,
				boxLabel: obj.label
			});
		});
		
		return {
			itemId: 'path',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: Sonicle.String.htmlLineBreaks(title)
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				items: [{
					xtype: 'fieldset',
					title: fieldLabel,
					padding: '10px',
					items: [{
						xtype: 'radiogroup',
						bind: {
							value: '{pathgroup}'
						},
						columns: 1,
						items: items
					}]
				}]
			}]
		};
	},
	
	createEndPage: function() {
		var me = this;
		return {
			itemId: 'end',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: Sonicle.String.htmlLineBreaks(me.resEndPageTitleText())
			}, {
				xtype: 'sospacer'
			}, {
				reference: 'log',
				xtype: 'textarea',
				hidden: !me.getShowDoButton(),
				readOnly: true,
				fieldStyle: {
					fontSize: '12px'
				},
				anchor: '100% -50'
			}]
		};
	},
	
	isPathSelection: function(page) {
		page = page || this.getActivePage();
		return page === 'path';
	},
	
	getPagesCount: function() {
		var pages = this.getPages();
		return pages ? pages.length : 0;
	},
	
	getPageIndex: function(page) {
		var pages = this.getPages();
		return pages ? pages.indexOf(page) : -1;
	},
	
	getActivePage: function() {
		var page = this.getLayout().getActiveItem();
		return page.getItemId();
	},
	
	getPageCmp: function(itemId) {
		return this.getComponent(itemId);
	},
	
	hasNext: function(dir, page) {
		var me = this;
		page = page || me.getActivePage();
		return me.isPathSelection(page) ? false : !Ext.isEmpty(me.computeNext(dir, page));
	},
	
	/**
	 * @private
	 * @param {Integer} dir Navigation direction: 1 -> forward, -1 -> backward.
	 * @param {String} [page] 
	 * @return {String} The next page or null if there are no more pages.
	 */
	computeNext: function(dir, page) {
		var me = this, index;
		page = page || me.getActivePage();
		index = me.getPageIndex(page) + dir;
		return ((index >= 0) && (index < me.getPagesCount())) ? me.getPages()[index] : null;
	},
	
	/**
	 * @private
	 */
	navigate: function(dir) {
		var me = this,
				prev = me.getActivePage(),
				next = me.computeNext(dir);
		
		if (me.isPathSelection(prev)) {
			me.addPages();
		} else {
			if (me.fireEvent('beforenavigate', me, dir, next, prev) !== false) {
				me.onNavigate(next);
				me.fireEvent('navigate', me, dir, next, prev);
			}
		}
	},
	
	onNavigate: function(page) {
		var me = this;
		me.activatePage(page);
		me.updateButtons(page);
		if (!me.isPathSelection() && me.getUseTrail()) me.updateTrail();
	},
	
	updateButtons: function(page) {
		var me = this,
				btnCancel = me.lookupReference('btncancel'),
				btnBack = me.lookupReference('btnback'),
				btnForw = me.lookupReference('btnforw'),
				btnDo = me.lookupReference('btndo'),
				hasPrev = me.hasNext(-1, page),
				hasNext = me.hasNext(1, page);
		
		if (me.isPathSelection()) {
			btnBack.setDisabled(true);
			btnForw.setDisabled(false);
		} else if (me.getDisableNavAtEnd() && !hasNext) {
			btnBack.setDisabled(true);
			btnForw.setDisabled(true);
		} else if (me.getDisableNavOnSuccess() && me.doSuccess) {
			btnBack.setDisabled(true);
			btnForw.setDisabled(true);
		} else {
			btnBack.setDisabled(!hasPrev);
			btnForw.setDisabled(!hasNext);
		}
		if (me.getShowDoButton()) {
			if (me.isPathSelection(page)) {
				btnDo.setDisabled(true);
			} else if (me.getLockDoButton() && (me.doCount > 0)) {
				btnDo.setDisabled(true);
			} else {
				btnDo.setDisabled(hasNext);
			}
		}
		if (!hasNext) btnCancel.setText(WT.res('wizard.btn-close.lbl'));
	},
	
	updateTrail: function() {
		var me = this,
				trail = me.lookupReference('trail'),
				curr = me.getPageIndex(me.getActivePage()) +1,
				count = (me.getInfiniteTrail()) ? '?' : me.getPagesCount();
		trail.update(Ext.String.format(WT.res('wizard.trail.lbl'), curr, count));
	},
	
	activatePage: function(page) {
		var me = this,
				lay = me.getLayout();
		lay.setActiveItem(page);
		lay.getActiveItem().updateLayout();
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
		
		me.wait();
		WT.ajaxReq(me.mys.ID, me.getAct(), {
			timeout: 1000*60*5,
			params: params,
			callback: function(success, json) {
				me.unwait();
				me.doSuccess = success;
				me.updateButtons('end');
				if (json && json.data && !Ext.isEmpty(json.data.log)) {
					page.lookupReference('log').setValue(json.data.log);
				}
				if (success) {
					me.getVM().set('result', json.data.result);
					me.fireEvent('dosuccess', me, json.data.result);
				} else {
					me.fireEvent('dofailure', me, json ? json.data : null);
				}
			}
		});
	},
	
	canCloseView: function() {
		var me = this;
		// Returns false to stop view closing and to display a confirm message.
		if (me.isPathSelection()) return true;
		if (me.hasNext(1)) return false;
		return true;
	},
	
	privates: {
		opParams: function(params) {
			return Ext.apply(params || {}, {
				oid: this.getUId()
			});
		},
		
		resDoButtonText: function() {
			return WT.resTpl(WT.ID, this.getDoButtonText());
		},

		resEndPageTitleText: function() {
			return WT.resTpl(WT.ID, this.getEndPageTitleText());
		}
	}
});
