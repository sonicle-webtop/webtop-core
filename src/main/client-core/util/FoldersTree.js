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
				meta.customElbowCls = 'wt-hidden';
				if (rec.isFolderRoot()) {
					meta.tdCls += ' wt-bold';
					return '<span style="opacity:0.7;">' + val + '</span>';
				} else if (rec.isFolder()) {
					if (rec.isPersonalNode() && rec.get('_default')) {
						val += '<span style="font-size:0.8em;opacity:0.4;">&nbsp;(';
						val += (opts.defaultText || 'default');
						val += ')</span>';
					} else {
						var rr = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
						if (!rr.CREATE && !rr.UPDATE && !rr.DELETE) meta.tdCls += ' wt-theme-text-greyed';
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
							return '<span style="font-size:0.8em;opacity:0.4;"'
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
					meta.customCheckboxCls = rec.get('checked') ? 'wt-tree-toggle-on' : 'wt-tree-toggle-off';
					return '<span style="opacity:0.7;">' + (isPers && opts.personalRootText ? opts.personalRootText : val) + '</span>' + (opts.countField ? countHtml(rec.get(opts.countField)) : '');
					
				} else if (rec.isFolder()) {
					if (isPers && rec.get('_default')) {
						val += '<span style="font-size:0.8em;opacity:0.4;">&nbsp;(';
						val += (opts.defaultText || 'default');
						val += ')</span>';
					} else {
						var rr = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
						if (!rr.CREATE && !rr.UPDATE && !rr.DELETE) meta.tdCls += ' wt-theme-text-greyed';
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
		return this.findNodeBy(tree, function(node) {
			return node.isFolderRoot() && node.hasProfile(profileId);
		});
	},
	
	getTargetFolder: function(tree) {
		var me = this,
				rootNode = me.getMyFolderRoot(tree),
				node = me.findDefaultFolder(rootNode);
		return (node) ? node : me.getBuiltInFolder(rootNode);
	},
	
	getBuiltInFolder: function(rootNode) {
		return rootNode.findChildBy(function(n) {
			return (n.get('_builtIn') === true);
		});
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
	 * Finds the first matching node in the tree by a function.
	 * If the function returns `true` it is considered a match.
	 * @param {type} tree
	 * @param {Function} fn The function to be called. It will be passed the following parameters:
	 *  @param {Ext.data.Model} fn.record The record to test for filtering. Access field values
	 *  @param {Object} fn.id The ID of the Record passed.
	 * @param {Object} [scope] The scope (this reference) in which the function is executed. Defaults to the Store.
	 * @return {Ext.data.NodeInterface} The matched node or null
	 */
	findNodeBy: function(tree, fn, scope) {
		var sto = tree.getStore(),
				result;
		Ext.Object.eachValue(sto.byIdMap, function(node) {
			if (fn.call(scope || sto, node, node.getId()) === true) {
				result = node;
				return false;
			}
		});
		return result;
	}
});
