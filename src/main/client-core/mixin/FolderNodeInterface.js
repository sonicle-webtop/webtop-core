/* 
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.mixin.FolderNodeInterface', {
	alternateClassName: 'WTA.mixin.FolderNodeInterface',
	extend: 'Ext.Mixin',
	mixinConfig: {
		id: 'foldernodeinterface'
	},
	
	typeField: '',
	folderIdField: '',
	profileIdField: '',
	defaultField: '',
	builtInField: '',
	activeField: '',
	
	/*
	isFolderRoot -> isOrigin
	getProfileId -> getOwnerId
	*/
	
	isFolderRoot: function() {
		return this.get(this.typeField) === 'root';
	},
	
	isFolder: function() {
		return this.get(this.typeField) === 'folder';
	},
	
	getFolderId: function() {
		var me = this;
		return me.isFolder() ? me.get(me.folderIdField) : null;
	},
	
	getProfileId: function() {
		return this.get(this.profileIdField);
	},
	
	/**
	 * @deprecated
	 */
	hasProfile: function(profileId) {
		return this.getProfileId() === profileId;
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
		return this.isFolderRoot() ? this : this.parentNode;
	},
	
	isPersonalNode: function() {
		return this.isNodePersonal(this.getId());
	},
	
	privates: {
		isNodePersonal: function(nodeId) {
			return (nodeId === '0') || Ext.String.startsWith(nodeId, '0|');
		}
	}
});
