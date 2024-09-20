/* 
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.app.util.FoldersTree', {
	singleton: true,
	alternateClassName: ['WTA.util.FoldersTree'],
	requires: [
		'Sonicle.tree.Utils'
	],
	
	coloredBoxTreeRenderer: function(opts) {
		opts = opts || {};
		return WTF.coloredBoxTreeRenderer({
			shouldCustomize: function(val, rec) {
				return rec.isFolder();
			},
			getColor: function(val, rec) {
				return '#'+rec.get('_color');
			},
			renderer: function(val, meta, rec) {
				var isPers = rec.isPersonalNode();
				meta.customElbowCls = 'wt-hidden';
				if (rec.isFolderRoot()) {
					meta.tdCls += ' wt-bold';
					return '<span>' + (isPers && opts.personalRootText ? opts.personalRootText : val) + '</span>';
				} else if (rec.isFolder()) {
					if (isPers && rec.get('_default')) {
						val += '<span class="wt-text-off wt-theme-text-color-off">&nbsp;(';
						val += (opts.defaultText || 'default');
						val += ')</span>';
					} else {
						var rr = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
						if (!rr.CREATE && !rr.UPDATE && !rr.DELETE) meta.tdCls += ' wt-theme-text-color-off';
					}
					return val;
				}
			}
		});
	},
	
	coloredCheckboxTreeRenderer: function(opts) {
		opts = opts || {};
		return WTF.coloredCheckboxTreeRenderer({
			shouldCustomize: function(val, rec) {
				return rec.isFolder();
			},
			getColor: function(val, rec) {
				return '#'+rec.get('_color');
			},
			renderer: function(val, meta, rec) {
				var isPers = rec.isPersonalNode(),
					countHtml = function(count) {
						if (Ext.isNumber(count) && count > 0) {
							return '<span class="wt-text-off wt-theme-text-color-off"'
								+ Sonicle.Utils.generateTooltipAttrs(opts.countTooltip ? Ext.String.format(opts.countTooltip, count) : null)
								+ '>&nbsp;+' + count + '</span>';
						} else {
							return '';
						}
					};
				meta.customElbowCls = 'wt-hidden';
				if (rec.isFolderRoot()) {
					meta.tdCls += ' wt-bold';
					meta.iconCls = 'wt-hidden';
					meta.customCheckboxCls = 'wt-tree-toggle ';
					meta.customCheckboxCls += rec.get('checked') ? 'wt-tree-toggle-on' : 'wt-tree-toggle-off';
					return '<span>' + (isPers && opts.personalRootText ? opts.personalRootText : val) + '</span>' + (opts.countField ? countHtml(rec.get(opts.countField)) : '');
					
				} else if (rec.isFolder()) {
					if (!isPers) {
						var rr = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
						if (!rr.CREATE && !rr.UPDATE && !rr.DELETE) meta.tdCls += ' wt-theme-text-color-off';
					}
					if (rec.get('_default') === true) {
						val += '<span class="wt-text-off wt-theme-text-color-off">&nbsp;(';
						val += (opts.defaultText || 'default');
						val += ')</span>';
					}
					return val;
				}
			}
		});
	},
	
	getMyFolderRoot: function(tree) {
		return this.getFolderRootByProfile(tree, WT.getVar('profileId'));
	},
	
	getFolderRootByProfile: function(tree, profileId) {
		return Sonicle.tree.Utils.findNodeBy(tree, function(node) {
			return node.isFolderRoot() && node.getProfileId() === profileId;
		});
	},
	
	getTargetFolder: function(tree) {
		var me = this,
				rootNode = me.getMyFolderRoot(tree),
				node = me.findDefaultFolder(rootNode);
		return (node) ? node : me.getBuiltInFolder(rootNode);
	},
	
	findDefaultFolder: function(rootNode) {
		return rootNode.findChildBy(function(n) {
			return (n.get('_default') === true);
		});
	},
	
	activateSingleFolder: function(foRootNode, targetFoNodeId) {
		var sto = foRootNode.getTreeStore();
		sto.suspendAutoSync();
		foRootNode.cascadeBy(function(n) {
			if (n !== foRootNode) {
				n.setActive(n.getId() === targetFoNodeId);
			}
		});
		sto.resumeAutoSync();
		sto.sync();
	},
	
	setActiveAllFolders: function(foRootNode, active) {
		var sto = foRootNode.getTreeStore();
		sto.suspendAutoSync();
		foRootNode.cascadeBy(function(n) {
			if (n !== foRootNode) {
				n.setActive(active);
			}
		});
		sto.resumeAutoSync();
		sto.sync();
	},
	
	/**
	 * Creates an object expliciting rights extracted from String.
	 * @param {String} rights The rights String.
	 * @returns {Object} Object with CREATE, READ, UPDATE, DELETE, MANAGE booleans.
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
	 * Set specified folder as the new default.
	 * NB: Node {@link Ext.data.NodeInterface model} must provide `isDefaultFolder()` and `setIsDefaultFolder` methods.
	 * @param {Ext.tree.Panel} tree Tree component on which operate.
	 * @param {String} targetFoNodeId The Node ID (look carefully, NOT the Folder ID).
	 */
	setFolderAsDefault: function(tree, targetFoNodeId) {
		var sto = tree.getStore(),
				newDeflt = sto.getNodeById(targetFoNodeId),
				oldDeflt = Sonicle.tree.Utils.findNodeBy(tree, function(node) {
					return node.isDefaultFolder() === true;
				}),
				newDeflt;
		
		if (newDeflt) {
			if (oldDeflt) oldDeflt.setIsDefaultFolder(false);
			newDeflt.setIsDefaultFolder(true);
		}
	},
	
	/**
	 * Returns the folder Node with specified folder ID.
	 * NB: Node {@link Ext.data.NodeInterface model} must provide `getFolderId()` method.
	 * @param {Ext.tree.Panel} tree Tree component on which operate.
	 * @param {String} folderId Desired folder ID.
	 * @returns {Ext.data.NodeInterface}
	 */
	getFolderById: function(tree, folderId) {
		return Sonicle.tree.Utils.findNodeBy(tree, function(node) {
			return node.getFolderId() === folderId;
		});
	},
	
	/**
	 * Returns the folder Node which is marked as default folder.
	 * NB: Node {@link Ext.data.NodeInterface model} must provide `isDefaultFolder()` method.
	 * @param {Ext.tree.Panel} tree Tree component on which operate.
	 * @returns {Ext.data.NodeInterface}
	 */
	getDefaultFolder: function(tree) {
		return Sonicle.tree.Utils.findNodeBy(tree, function(node) {
			return node.isDefaultFolder() === true;
		});
	},
	
	/**
	 * Returns the folder Node which is marked as built-in folder.
	 * NB: Node {@link Ext.data.NodeInterface model} must provide `isBuiltInFolder()` method.
	 * @param {Ext.tree.Panel} tree Tree component on which operate.
	 * @returns {Ext.data.NodeInterface}
	 */
	getBuiltInFolder: function(tree) {
		var rootNode = this.getMyFolderRoot(tree);
		return rootNode.findChildBy(function(n) {
			return (n.isBuiltInFolder() === true);
		});
	},
	
	/**
	 * Wrapper method that returns default folder Node or built-in one instead.
	 * NB: Node {@link Ext.data.NodeInterface model} must provide some methods.
	 * @param {Ext.tree.Panel} tree Tree component on which operate.
	 * @returns {Ext.data.NodeInterface}
	 */
	getDefaultOrBuiltInFolder: function(tree) {
		var node = this.getDefaultFolder(tree);
		return node ? node : this.getBuiltInFolder(tree);
	},
	
	/**
	 * Wrapper method that returns the folder underlined by the passed folderId 
	 * or the result of {@link #getDefaultOrBuiltInFolder} if not found. Null is
	 * returned if the candidate folder does NOT have elements creation rights.
	 * @param {Ext.tree.Panel} tree Tree component on which operate.
	 * @param {String} folderId 
	 * @returns {Ext.data.NodeInterface}
	 */
	getFolderForAdd: function(tree, folderId) {
		var me = this,
				node, er;
		if (!Ext.isEmpty(folderId)) {
			node = me.getFolderById(tree, folderId);
		} else {
			node = me.getDefaultOrBuiltInFolder(tree);
		}
		if (node) {
			er = me.toRightsObj(node.get('_erights'));
			if (er.CREATE) return node;
		}
		return null;
	}
});
