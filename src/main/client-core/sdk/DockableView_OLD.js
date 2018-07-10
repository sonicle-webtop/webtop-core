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
Ext.define('Sonicle.webtop.core.sdk.DockableView', {
	alternateClassName: 'WTA.sdk.DockableView',
	extend: 'WTA.sdk.BaseView',
	
	config: {
		dockableConfig: {
			/**
			 * @cfg {String} iconCls
			 * The icon class to be used to apply as container's iconCls.
			 */
			
			/**
			 * @cfg {String} title
			 * The title text to be used to apply as container's title.
			 * Can be a value or a template string that points to a resource.
			 */
			
			width: 400,
			height: 200,
			
			/**
			 * @cfg {Boolean} constrainToService
			 */
			constrainToService: false,
			
			/**
			 * @cfg {Boolean} modal
			 */
			modal: false,
			
			minimizable: true,
			
			maximizable: true
		},
		
		/**
		 * @cfg {Boolean} promptConfirm
		 * This config controls the display of a confirm message on close.
		 * If True and if {@link #beforeviewclose event} returns that a close 
		 * operation would not be safe, a confirm message will be shown. 
		 */
		promptConfirm: true,
		
		/**
		 * @cfg {'ync'/'yn'} [confirm='ync']
		 * Controls confirm message buttons' appearance.
		 * Two values are allowed:
		 * - 'ync' - Yes+No+Cancel
		 * - 'yn' - Yes+No
		 */
		confirm: 'ync',
		
		/**
		 * @cfg {String} confirmMsg
		 * Custom confirm message to use.
		 */
		confirmMsg: null
	},
	
	/**
	 * @private
	 * @property {Boolean} ctInited
	 */
	ctInited: false,
	
	/**
	 * @event viewshow
	 * Fires after the view is shown.
	 * @param {WTA.sdk.DockableView} this
	 */
	
	/**
	 * @event viewdiscard
	 * Fires after the user when prompted, chooses to discard current view.
	 * This event is fired before {@link #viewclose} event.
	 * @param {WTA.sdk.DockableView} this
	 */
	
	/**
	 * @event viewclose
	 * Fires after the view is closed.
	 * @param {WTA.sdk.DockableView} this
	 */
	
	viewModel: {
		data: {
			_viewTitle: ''
		}
	},
	
	constructor: function(cfg) {
		var me = this, vm, dcfg;
		// Defines a basic viewModel (eg. useful for binding)
		//if(!me.viewModel) me.viewModel = Ext.create('Ext.app.ViewModel');
		if(Ext.isObject(cfg.dockableConfig)) {
			Ext.merge(me.config.dockableConfig, cfg.dockableConfig);
			delete cfg.dockableConfig;
		}
		me.callParent([cfg]);
		
		if(!cfg.title) {
			me.setBind({
				title: '{_viewTitle}'
			});
		}
		
		vm = me.getVM();
		dcfg = me.getDockableConfig();
		if(cfg.title) {
			vm.set('_viewTitle', '');
		} else {
			vm.set('_viewTitle', me.resTitle() || '');
		}
	},
	
	initComponent: function() {
		var me = this,
				cfg = me.getDockableConfig();
		
		if(cfg.iconCls) me.iconCls = cfg.iconCls;
		me.callParent(arguments);
		me.on('titlechange', me.onTitleChange);
		me.on('added', function(s,ct) {
			me.initCt(ct);
		}, me, {single: true});
		me.on('removed', function(s,ct) {
			me.cleanupCt(ct);
		}, me, {single: true});
	},
	
	/**
	 * @private
	 */
	initCt: function(ct) {
		var me = this;
		if(me.ctInited) return;
		
		if(ct.isXType('window')) {
			// Apply as config (window is not yet rendered)
			ct.title = me.title;
			if(me.dockableConfig.iconCls) ct.iconCls = me.dockableConfig.iconCls;
			
			// Le toolbar non vengono più applicate al container ma bensì alla vista
			// stessa, ora la view è un panel non più un component
			/*
			if(me.tbar || me.fbar || me.lbar || me.rbar || me.dockedItems || me.buttons) {
				Ext.apply(ct, {
					tbar: me.tbar,
					fbar: me.fbar,
					lbar: me.lbar,
					rbar: me.rbar,
					dockedItems: me.dockedItems,
					buttons: me.buttons,
					buttonAlign: me.buttonAlign,
					minButtonWidth: me.minButtonWidth
				});
				ct.bridgeToolbars(); // Force toolbar initialization on target component
				
				// Cleanup configured props
				var props = ['tbar','fbar','lbar','rbar','dockedItems','buttons'];
				for(var prop in props) delete me[prop];
			}
			*/
			
			ct.on('show', me.onCtWndShow, me);
			ct.on('beforeclose', me.onCtBeforeClose, me);
			ct.on('close', me.onCtWndClose, me);
			
			// TODO: gestire i window group
			/*
			if(me.useWG) ct.on('hide', me.onCtWndHide, me);
			*/
		}
		me.ctInited = true;
	},
	
	/**
	 * @private
	 */
	cleanupCt: function(ct) {
		var me = this;
		if(!me.ctInited) return;
		
		if(ct.isXType('window')) {
			ct.un('show', me.onCtWndShow, me);
			ct.un('beforeclose', me.onCtBeforeClose, me);
			ct.un('close', me.onCtWndClose, me);
			ct.un('hide', me.onCtWndHide, me);
		}
		me.ctInited = false;
	},
	
	/**
	 * @private
	 */
	onTitleChange: function(s,nv) {
		var me = this;
		if(me.ctInited) {
			if(me.ownerCt.isXType('window')) {
				me.ownerCt.setTitle(nv);
			}
		}
	},
	
	/**
	 * @private
	 */
	onCtWndShow: function() {
		var me = this;
		// TODO: gestire i window group
		/*
		if(me.useWG) {
			me.wg.each(function(wnd) {
				wnd.show();
			}, me);
		}
		*/
		me.fireEvent('viewshow', me);
	},
	
	/**
	 * @private
	 */
	onCtBeforeClose: function() {
		return this.onBeforeClose();
	},
	
	/*
	onCtBeforeClose__: function() {
		var me = this;
		// TODO: gestire i window group
		//if(me.useWG && me.hasWindows()) return false;
		if(me.promptConfirm && me.fireEvent('beforeviewclose', me) === false) {
			me.showConfirm();
			return false;
		}
		return true;
	},
	*/
	
	/**
	 * @private
	 */
	onCtWndClose: function() {
		var me = this;
		me.fireEvent('viewclose', me);
	},
	
	/**
	 * @private
	 */
	onCtWndHide: function() {
		// TODO: gestire i window group
		/*
		var me = this;
		if(me.useWG) {
			me.wg.hideAll();
		}
		*/
	},
	
	/**
	 * Method executed during container close. Returning false will prompt 
	 * (if enabled, see {@link #promptConfirm}) a confirm message.
	 * Child classes can override this method to implement their own custom logic.
	 * @return {Boolean}
	 */
	canCloseView: function() {
		return true;
	},
	
	/**
	 * Closes this view. If specified, the 'prompt' parameter overwrites
	 * current {@link #promptConfirm} definition.
	 * @param {Boolean} [prompt] Allow to override prompt confirm behaviour.
	 */
	closeView: function(prompt) {
		var me = this;
		if(prompt !== undefined) me.promptConfirm = prompt;
		if(me.ctInited) me.ownerCt.close();
	},
	
	/**
	 * Hides this view. no 'prompt' will be shown
	 */
	hideView: function() {
		var me = this;
		if(me.ctInited) me.ownerCt.hide();
	},
	
	getViewTitle: function() {
		return this.getVM().get('_viewTitle');
	},
	
	setViewTitle: function(title) {
		this.getVM().set('_viewTitle', title);
	},
	
	/**
	 * Shows the confirm message.
	 */
	showConfirm: function() {
		var me = this, msg;
		
		if(me.confirm === 'ync') {
			msg = me.confirmMsg || WT.res('confirm.save');
			WT.confirmYNC(msg, function(bid) {
				if(bid === 'yes') {
					me.onConfirmView();
				} else if(bid === 'no') {
					me.onDiscardView();
				}
			});
		} else {
			msg = me.confirmMsg || WT.res('confirm.areyousure');
			WT.confirm(msg, function(bid) {
				if(bid === 'yes') {
					me.onDiscardView();
				}
			});
		}
	},
	
	/**
	 * Handler method executed on confirm continue (answer: yes).
	 * Child classes can override this method to implement their own custom logic.
	 */
	onConfirmView: function() {
		this.closeView(false);
	},
	
	/**
	 * Handler method executed on confirm discard (answer: no).
	 * Child classes can override this method to implement their own custom logic.
	 */
	onDiscardView: function() {
		var me = this;
		me.fireEvent('viewdiscard', me);
		me.closeView(false);
	},
	
	/**
	 * Handler method executed on container before close.
	 * Child classes can override this method to implement their own custom logic.
	 */
	onBeforeClose: function() {
		var me = this;
		// TODO: gestire i window group
		/*
		if(me.useWG && me.hasWindows()) return false;
		*/
		if(me.getPromptConfirm() === false) return true;
		if(me.canCloseView() === false) {
			me.showConfirm();
			return false;
		}
		return true;
	},
	
	/**
	 * @private
	 */
	resTitle: function() {
		var me = this,
				sid = Ext.isString(me.mys) ? me.mys : me.mys.ID,
				cfg = me.getDockableConfig();
		return (cfg) ? WT.resTpl(sid, cfg.title) : null;
	}
});
