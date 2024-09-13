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
		//return me.isFolder() ? WTA.sdk.mixin.FolderNodeInterface.tailwindColor(me.get(me.colorField)) : false;
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
	},
	
	statics: {
		tailwindColor: function(color) {
			//FIXME: remove me!
			var map = {
					'FFC6CC': 'FEE2E2', 'EF9A9A': 'FECACA', 'E57373': 'FCA5A5', 'EF5350': 'F87171', 'F44336': 'EF4444', 'E53935': 'DC2626', 'D32F2F': 'B91C1C', 'C62828': '991B1B', 'B71C1C': '7F1D1D', // -> red
					'F8BBD0': 'FCE7F3', 'F48FB1': 'FBCFE8', 'F06292': 'F9A8D4', 'EC407A': 'F472B6', 'E91E63': 'EC4899', 'D81860': 'DB2777', 'C2185B': 'BE185D', 'AD1457': '9D174D', '880E4F': '831843', // -> pink
					'E1BEE7': 'F3E8FF', 'CE93D8': 'E9D5FF', 'BA68C8': 'D8B4FE', 'AB47BC': 'C084FC', '9C27B0': 'A855F7', '8E24AA': '9333EA', '7B1FA2': '7E22CE', '6A1B9A': '6B21A8', '4A148C': '581C87', // -> purple
					'C5CAE9': 'DBEAFE', '9FA8DA': 'BFDBFE', '7986CB': '93C5FD', '5C6BC0': '60A5FA', '3F51B5': '3B82F6', '3949AB': '2563EB', '383F9F': '1D4ED8', '283593': '1E40AF', '1A237E': '1E3A8A', // -> blue
					'BBDEFB': 'CFFAFE', '90CAF9': 'A5F3FC', '64B5F6': '67E8F9', '42A5F5': '22D3EE', '2196F3': '06B6D4', '1E88E5': '0891B2', '1976D2': '0E7490', '1565C0': '155E75', '0D47A1': '164E63', // -> cyan
					'C8E6C9': 'D1FAE5', 'A5D6A7': 'A7F3D0', '81C784': '6EE7B7', '66BB6A': '34D399', '4CAF50': '10B981', '43A047': '059669', '388E3C': '047857', '2E7D32': '065F46', '1B5E20': '064E3B', // -> emerald
					'DCEDC8': 'ECFCCB', 'C5E1A5': 'D9F99D', 'AED581': 'BEF264', '9CCC65': 'A3E635', '8BC34A': '84CC16', '7CB342': '65A30D', '689F38': '4D7C0F', '558B2F': '3F6212', '33691E': '365314', // -> lime
					'FFF9C4': 'FEF3C7', 'FFF59D': 'FDE68A', 'FFF176': 'FCD34D', 'FFEE58': 'FBBF24', 'FFEB3B': 'F59E0B', 'FDD835': 'D97706', 'FBC02D': 'B45309', 'F9A825': '92400E', 'F57F17': '78350F', // -> yellow
					'FFE0B2': 'FFEDD5', 'FFCC80': 'FED7AA', 'FFB74D': 'FDBA74', 'FFA726': 'FB923C', 'FF9800': 'F97316', 'FB8C00': 'EA580C', 'F57C00': 'C2410C', 'EF6C00': '9A3412', 'E65100': '7C2D12', // -> orange
					'D7CCC8': 'F5F5F4', 'BCAAA4': 'E5E7EB', 'A1887F': 'D6D3D1', '8D6E63': 'A8A29E', '795548': '78716C', '6D4C41': '57534E', '5D4037': '44403C', '4E342E': '292524', '3E2723': '1C1917', // -> stone
					'FFFFFF': 'F3F4F6', 'EEEEEE': 'E5E7EB', 'E0E0E0': 'D1D5DB', 'BDBDBD': '9CA3AF', '9E9E9E': '6B7280', '757575': '4B5563', '616161': '374151', '424242': '1F2937', '212121': '111827', // -> gray
					
					// palette legacy
					'AC725E': '78716C', 'D06B64': 'F87171', 'F83A22': 'DC2626', 'FA573C': 'EF4444', 'FF7537': 'FB923C', 'FFAD46': 'FB923C', 'FAD165': 'FCD34D', 'FBE983': 'FDE68A', '4986E7': '3B82F6', '9FC6E7': '93C5FD', '9FE1E7': 'A5F3FC', '92E1C0': 'A7F3D0', '42D692': '34D399', '16A765': '059669', '7BD148': 'A3E635', 'B3DC6C': 'BEF264', '9A9CFF': '93C5FD', 'B99AFF': 'C084FC', 'A47AE2': 'C084FC', 'CD74E6': 'C084FC', 'F691B2': 'FCA5A5', 'CCA6AC': 'A8A29E', 'CABDBF': 'D6D3D1', 'C2C2C2': 'D6D3D1'
				},
				SoS = Sonicle.String,
				hasHash = SoS.startsWith(color, '#'),
				pcolor = SoS.removeStart(color, '#');
			return (hasHash ? '#' : '') + (map[pcolor] || pcolor);
		}
	}
});
