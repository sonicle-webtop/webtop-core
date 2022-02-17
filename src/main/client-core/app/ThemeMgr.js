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
Ext.define('Sonicle.webtop.core.app.ThemeMgr', {
	singleton: true,
	alternateClassName: ['WTA.ThemeMgr'],
	
	hierarchy: {
		'neptune': 'neptune',
		'neptune-touch': 'neptune',
		'crisp': 'neptune',
		'crisp-touch': 'crisp',
		'classic': 'classic',
		'gray': 'classic',
		'aria': 'aria',
		'graphite': 'graphite',
		'material': 'material',
		'triton': 'triton'
	},
	
	metrics: {
		'neptune': {
			'toolbar': {
				'marginTop': 6,
				'marginBottom': 6,
				'itemsSpacing': 6
			}
		},
		'classic': {
			'toolbar': {
				'marginTop': 2,
				'marginBottom': 2,
				'itemsSpacing': 2
			}
		}
	},
	
	/**
	 * Returns the base hierarchy for the specified theme.
	 * @param {String} theme The theme name
	 * @returns {String} The base name
	 */
	getBase: function(theme) {
		return this.hierarchy[theme];
	},
	
	/**
	 * Get desired component's metric for a theme.
	 * Note: this method is able to return metric following the theme hierarchy.
	 * @param {String} theme The theme name
	 * @param {String} xtype The component xtype
	 * @param {String} metric The metric name to get
	 * @returns {Mixed} Desired metric
	 */
	getMetric: function(theme, xtype, metric) {
		var o = this.getThemeMetrics(theme),
				xt = o ? o[xtype] : undefined;
		return xt ? xt[metric] : undefined;
	},
	
	/**
	 * @private
	 */
	getThemeMetrics: function(theme) {
		var me = this,
				deep = arguments[1] || 0, o;
		if(!theme) return undefined;
		o = me.metrics[theme];
		if(o) {
			return o;
		} else {
			return (deep === 3) ? undefined : me.getThemeMetrics(me.hierarchy[theme], deep+1);
		}
	}
});
