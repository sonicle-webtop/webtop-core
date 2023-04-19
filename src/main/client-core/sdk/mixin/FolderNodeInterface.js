/* 
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.sdk.mixin.FolderNodeInterface', {
	alternateClassName: 'WTA.sdk.mixin.FolderNodeInterface',
	extend: 'Ext.Mixin',
	requires: [
		'Sonicle.String',
		'WTA.util.FoldersTree2'
	],
	mixinConfig: {
		id: 'wtfoldernodeinterface2'
	},
	
	colorField: '', //'_color',
	defaultField: '', //'_default',
	builtInField: '', //'_builtIn',
	activeField: '', //'_active',
	originPermsField: '', //'_orPerms',
	folderPermsField: '', //'_foPerms',
	itemsPermsField: '', //'_itPerms',
	
	originRightsField: '',
	folderRightsField: '',
	itemsRightsField: '',
	
	isFolder: function() {
		return 'F' === this.parseId().type;
	},
	
	isOrigin: function() {
		return 'O' === this.parseId().type;
	},
	
	isGrouper: function() {
		return 'G' === this.parseId().type;
	},
	
	/**
	 * @deprecated use getOwnerPid instead
	 */
	getOwnerId: function() {
		return this.getOwnerPid();
	},
	
	getOwnerPid: function() {
		return (this.isOrigin() || this.isFolder()) ? this.parseId().origin : null;
	},
	
	getOwnerDomainId: function() {
		return Sonicle.String.substrAfterLast(this.getOwnerId(), '@');
	},
	
	getOwnerUserId: function() {
		return Sonicle.String.substrBeforeLast(this.getOwnerId(), '@');
	},
	
	getOriginPerms: function() {
		var me = this;
		return me.isOrigin() ? me.get(me.originPermsField) : null;
	},
	
	getFolderPerms: function() {
		var me = this;
		return (me.isOrigin() || me.isFolder()) ? me.get(me.folderPermsField) : null;
	},
	
	getItemsPerms: function() {
		var me = this;
		return (me.isOrigin() || me.isFolder()) ? me.get(me.itemsPermsField) : null;
	},
	
	getOriginRights: function() {
		return WTA.util.FoldersTree2.toRightsObj(this.getOriginPerms());
	},
	
	getFolderRights: function() {
		return WTA.util.FoldersTree2.toRightsObj(this.getFolderPerms());
	},
	
	getItemsRights: function() {
		return WTA.util.FoldersTree2.toRightsObj(this.getItemsPerms());
	},
	
	getFolderId: function() {
		return this.isFolder() ? this.parseId().folder : null;
	},
	
	getFolderColor: function() {
		var me = this;
		return me.isFolder() ? me.get(me.colorField) : false;
	},
	
	isBuiltInFolder: function() {
		var me = this;
		return me.isFolder() ? me.get(me.builtInField) : false;
	},
	
	isDefaultFolder: function() {
		var me = this;
		return me.isFolder() ? me.get(me.defaultField) : false;
	},
	
	setIsDefaultFolder: function(deflt) {
		var me = this;
		if (me.isFolder() && Ext.isBoolean(deflt)) me.set(me.defaultField, deflt);
	},
	
	isChecked: function() {
		return this.get('checked');
	},
	
	isActive: function() {
		return this.get(this.activeField) === true;
	},
	
	setActive: function(active) {
		var me = this;
		me.beginEdit();
		me.set('checked', active);
		me.set(me.activeField, active);
		me.endEdit();
	},
	
	refreshActive: function() {
		var me = this;
		me.set(me.activeField, me.get('checked') === true);
	},
	
	getFolderNode: function() {
		return this.isFolder() ? this : null;
	},
	
	getFolderRootNode: function() {
		var me = this;
		return (me.isOrigin() || me.isGrouper()) ? me : me.parentNode;
	},
	
	isPersonalNode: function() {
		return WT.getVar('profileId') === this.getOwnerId();
	},
	
	privates: {
		parseId: function() {
			var tokens = Sonicle.String.split(this.getId(), '|', 3);
			return {
				type: tokens[0],
				origin: tokens[1],
				folder: tokens[2]
			};
		}
	}
});
