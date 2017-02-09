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
Ext.define('Sonicle.webtop.core.mixin.FoldersTree', {
	alternateClassName: 'WTA.mixin.FoldersTree',
	extend: 'Ext.Mixin',
	mixinConfig: {
		id: 'folderstree'
	},
	
	/**
	 * @private
	 */
	toF3RightsObj: function(rights) {
		var iof = function(s,v) { return s ? s.indexOf(v) !== -1 : false; },
				obj = {};
		obj['CREATE'] = iof(rights, 'c');
		obj['READ'] = iof(rights, 'r');
		obj['UPDATE'] = iof(rights, 'u');
		obj['DELETE'] = iof(rights, 'd');
		obj['MANAGE'] = iof(rights, 'm');
		return obj;
	},
	
	/**
	 * @private
	 * Returns folder that matches my profileId.
	 * @returns {Ext.data.NodeInterface}
	 */
	getF3MyRoot: function(tree) {
		return this.getF3RootByProfile(tree, WT.getVar('profileId'));
	},
	
	/**
	 * @private
	 * Returns folder that matches passed profileId.
	 * @returns {Ext.data.NodeInterface}
	 */
	getF3RootByProfile: function(tree, profileId) {
		return tree.getStore().findNode('_pid', profileId, false);
	},
	
	getF3FolderById: function(tree, foldId, idField) {
		return tree.getStore().findNode(idField, foldId, false);
	},
	
	/*
	 * @private
	 */
	getF3FolderByRoot: function(rootNode) {
		if (!rootNode) return null;
		var n = this.getF3DefaultFolderByRoot(rootNode);
		return n ? n : this.getF3BuiltInFolderByRoot(rootNode);
	},
	
	/*
	 * @private
	 */
	getF3DefaultFolderByRoot: function(rootNode) {
		if (!rootNode) return null;
		return rootNode.findChildBy(function(n) {
			return (n.get('_default') === true);
		});
	},
	
	/*
	 * @private
	 */
	getF3BuiltInFolderByRoot: function(rootNode) {
		if (!rootNode) return null;
		return rootNode.findChildBy(function(n) {
			return (n.get('_builtIn') === true);
		});
	},
	
	/**
	 * @private
	 * Returns the first tree node of the current selection.
	 */
	getF3SelectedNode: function(tree) {
		var sel = tree.getSelection();
		return (sel.length === 0) ? null : sel[0];
	},
	
	
	/*
	 * @private
	 */
	showHideF3Folder: function(node, show) {
		node.beginEdit();
		node.set('_visible', show);
		node.endEdit();
	},
	
	/*
	 * @private
	 */
	showHideAllF3Folders: function(parentNode, show) {
		var me = this,
				store = parentNode.getTreeStore();
		
		store.suspendAutoSync();
		parentNode.cascadeBy(function(n) {
			if(n !== parentNode) {
				n.set('checked', show);
				me.showHideFolder(n, show);
			}
		});
		store.resumeAutoSync();
		store.sync();
	},
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @private
	 * F3
	 */
	toRightsObj: function(rights) {
		var iof = function(s,v) { return s ? s.indexOf(v) !== -1 : false; },
				obj = {};
		obj['CREATE'] = iof(rights, 'c');
		obj['READ'] = iof(rights, 'r');
		obj['UPDATE'] = iof(rights, 'u');
		obj['DELETE'] = iof(rights, 'd');
		obj['MANAGE'] = iof(rights, 'm');
		return obj;
	},
	
	/**
	 * @private
	 * F3
	 * Returns folder that matches with my profileId.
	 * @returns {Ext.data.NodeInterface}
	 */
	getMyRoot: function(tree) {
		return tree.getStore().findNode('_pid', WT.getVar('profileId'), false);
	},
	
	/**
	 * @private
	 * F3
	 * Returns selected tree node.
	 */
	getSelectedNode: function(tree) {
		var sel = tree.getSelection();
		return (sel.length === 0) ? null : sel[0];
	},
	
	/**
	 * @private
	 * Returns selected root folder. If force param is 'true', this method 
	 * returns a default value if no selection is available.
	 * @param {Boolean} [force=false] 'true' to always return a value.
	 * @returns {Ext.data.NodeInterface}
	 */
	getSelectedRootFolder: function(tree, force) {
		var sel = tree.getSelection();
		
		if(sel.length === 0) {
			if(!force) return null;
			// As default returns myFolder, which have id equals to principal option
			return this.getMyRoot(tree);
		}
		return (sel[0].get('_type') === 'root') ? sel[0] : sel[0].parentNode;
	},
	
	/*
	 * @private
	 * Returns selected folder. If no selection is available, 
	 * this method tries to return the default folder and then the built-in one.
	 * @returns {Ext.data.NodeInterface}
	 */
	getSelectedFolder: function(tree) {
		var me = this,
				sel = tree.getSelection(),
				node;
		
		if(sel.length > 0) {
			if(sel[0].get('_type') === 'root') {
				node = me.getFolderByRoot(sel[0]);
				if(node) return node;
			} else {
				return sel[0];
			}
		}
		
		node = me.getMyRoot(tree);
		return me.getFolderByRoot(node);
	},
	
	/*
	 * @private
	 * F3
	 */
	getFolderByRoot: function(rootNode) {
		var node = this.getDefaultFolder(rootNode);
		return (node) ? node : this.getBuiltInFolder(rootNode);
	},
	
	/*
	 * @private
	 * F3
	 */
	getDefaultFolder: function(rootNode) {
		return rootNode.findChildBy(function(n) {
			return (n.get('_default') === true);
		});
	},
	
	/*
	 * @private
	 * F3
	 */
	getBuiltInFolder: function(rootNode) {
		return rootNode.findChildBy(function(n) {
			return (n.get('_builtIn') === true);
		});
	},
	
	/*
	 * @private
	 * F3
	 */
	showHideFolder: function(node, show) {
		node.beginEdit();
		node.set('_visible', show);
		node.endEdit();
	},
	
	/*
	 * @private
	 * F3
	 */
	showHideAllFolders: function(parentNode, show) {
		var me = this,
				store = parentNode.getTreeStore();
		
		store.suspendAutoSync();
		parentNode.cascadeBy(function(n) {
			if(n !== parentNode) {
				n.set('checked', show);
				me.showHideFolder(n, show);
			}
		});
		store.resumeAutoSync();
		store.sync();
	}
});
