/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.SelectTagsBox', {
	alternateClassName: 'WTA.ux.SelectTagsBox',
	extend: 'WTA.ux.window.CustomPromptMsgBox',
	requires: [
		'Sonicle.webtop.core.model.Tag'
	],
	uses: [
		'Sonicle.Data',
		'Sonicle.String'
	],
	
	/**
	 * @cfg {String} [defaultColor]
	 * Color to use as default for new or if color is missing.
	 */
	defaultColor: '#FFFFFF',
	
	emptyText: '',
	
	/**
	 * @private
	 * @property initialSelection
	 */
	
	createCustomPrompt: function() {
		var me = this,
				customPromptId = me.id + '-grid';
		return {
			xtype: 'grid',
			id: customPromptId,
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.model.Tag',
				proxy: WTF.apiProxy(WT.ID, 'ManageTags', null, {
					writer: {
						allowSingle: false
					}
				}),
				sorters: [{property: 'builtIn', direction: 'DESC'}, 'name'],
				listeners: {
					load: function(s, recs) {
						if (s.loadCount === 1) {
							var sel = me.initialSelection, selRecs = [];
							if (sel && Ext.isArray(sel)) {
								Ext.iterate(recs, function(rec) {
									if (sel.indexOf(rec.getId()) !== -1) selRecs.push(rec);
								});
								me.getComponent(customPromptId).getSelectionModel().select(selRecs, false, false);
							}
						}
					}
				}
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: me.emptyText
			},
			selModel: {
				type: 'checkboxmodel'
			},
			columns: [
				{
					dataIndex: 'name',
					renderer: function(val, meta, rec) {
						var SoS = Sonicle.String;
						return '<i class="fas fa-tag" aria-hidden="true" style="font-size:1.2em;color:' + SoS.deflt(rec.get('color'), me.defaultColor) + '"></i>&nbsp;&nbsp;' + SoS.deflt(rec.get('name'), '');
					},
					flex: 1
				}
			],
			margin: '10 0 0 0',
			border: true,
			width: 250,
			height: 250
		};
	},
	
	setCustomPromptValue: function(value) {
		this.initialSelection = value;
	},
	
	getCustomPromptValue: function() {
		return Sonicle.Data.collectValues(this.customPrompt.getSelection());
	}
});
