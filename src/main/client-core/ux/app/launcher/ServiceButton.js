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
Ext.define('Sonicle.webtop.core.ux.app.launcher.ServiceButton', {
	alternateClassName: 'WTA.ux.app.launcher.ServiceButton',
	extend: 'Sonicle.button.Toggle',
	/*
	requires: [
		'Sonicle.plugin.BadgeText'
	],
	plugins: [{
		ptype: 'sobadgetext',
		align: 'bl'
	}],
	*/
	
	componentCls: 'wt-launcher-servicebutton',
	textAlign: 'left',
	
	config: {
		iconName: 'service'
	},
	
	/**
	 * @cfg {String} sid
	 * WebTop service ID.
	 */
	
	constructor: function(cfg) {
		var me = this,
			icfg = Sonicle.Utils.getConstructorConfigs(this, cfg, ['scale', 'iconName']),
			desc;
		if (Ext.isEmpty(cfg.sid)) Ext.raise('`sid` is mandatory');
		desc = WT.getApp().getDescriptor(cfg.sid);
		Ext.apply(cfg, me.buildButtonCfg(WT.getApp().getDescriptor(cfg.sid), icfg.scale, icfg.iconName));
		me.callParent([cfg]);
	},
	
	buildButtonCfg: function(desc, scale, iconName) {
		var me = this;
		return {
			itemId: desc.getId(),
			overflowText: desc.getName(),
			tooltip: me.buildTooltip(desc),
			onIconCls: WTF.cssIconCls(desc.getXid(), iconName, me.toIconSize(scale)),
			offIconCls: WTF.cssIconCls(desc.getXid(), iconName + '-off', me.toIconSize(scale))
		};
	},
	
	buildTooltip: function(desc) {
		var text, build;
		if (WT.isWTAdmin()) {
			build = desc.getBuild();
			text = Ext.String.format('v.{0}{1} - {2}', desc.getVersion(), Ext.isEmpty(build) ? '' : '('+build+')', desc.getCompany());
		} else {
			text = Ext.String.format('v.{0} - {1}', desc.getVersion(), desc.getCompany());
		}
		return {
			title: desc.getName(),
			text: text
		};
	},
	
	privates: {
		toIconSize: function(scale) {
			var s = 'sm';
			if (scale === 'medium') return 'md';
			if (scale === 'large') return 'lg';
			return s;
		}
	}
});
