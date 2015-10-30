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
Ext.define("Ext.saki.form.field.Icon", {
	extend: "Ext.AbstractPlugin",
	alternateClassName: "Ext.ux.form.field.Icon",
	alias: ["plugin.saki-ficn", "plugin.ux-ficn"],
	iconBaseCls: "fa",
	iconCls: "fa-info-circle",
	iconMode: "font",
	iconColor: "#5278FF",
	iconWidth: 16,
	iconHeight: 16,
	cellWidthAdjust: 6,
	iconCursor: "pointer",
	iconMargin: "0 3px 0 3px",
	position: "afterInput",
	clickEvents: ["click", "contextmenu"],
	init: function (c) {
		var d = this;
		d.setCmp(c);
		c.on({afterrender: {scope: d, single: true, fn: d.afterCmpRender}});

		Ext.apply(c, {getIcon: function () {
				return d
			}, setIconCls: function (a) {
				this.iconEl.replaceCls(d.iconCls, a);
				d.iconCls = a
			}, setIconStyle: function (a) {
				this.iconEl.applyStyles(a)
			}, setIconColor: function (a) {
				this.setIconStyle({color: a})
			}, setIconTip: function (a) {
				this.iconEl.set({"data-qtip": a})
			}, setIconTipTitle: function (a) {
				this.iconEl.set({"data-qtitle": a})
			}})
	}, afterCmpRender: function () {
		var n = this, o = n.getCmp(), e = n.getIconConfig(), p = false, m = false, k, q = {tag: "div", style: {display: "table-cell", width: (n.iconWidth + n.cellWidthAdjust) + "px"}, cn: [e]};
		try {
			p = o instanceof Ext.form.field.Checkbox;
			m = o instanceof Ext.form.field.TextArea
		} catch (l) {
		}
		if (m) {
			Ext.apply(q.style, {"vertical-align": "top", "padding-top": "3px"})
		}
		switch (n.position) {
			case"afterInput":
				if (p) {
					k = o.inputEl.insertSibling(e, "after")
				} else {
					k = o.bodyEl.insertSibling(q, "after");
					k = k.down("i")
				}
				break;
			case"beforeInput":
				k = o.labelEl.next().insertSibling(q, "before");
				k = k.down("i");
				break;
			case"afterLabel":
				if (p && o.boxLabelEl) {
					e.style["vertical-align"] = "middle";
					k = o.boxLabelEl.insertSibling(e, "after")
				} else {
					k = o.labelEl.insertSibling(q, "after")
				}
				break;
			case"beforeLabel":
				if (p && o.boxLabelEl) {
					e.style["margin-left"] = (n.iconWidth + n.cellWidthAdjust / 2) + "px";
					o.boxLabelEl.setStyle({"padding-left": 0});
					k = o.boxLabelEl.insertSibling(e, "before")
				} else {
					k = o.labelEl.insertSibling(q, "before");
					k = k.down("i")
				}
				break
		}
		o.iconEl = k;
		if (n.tip) {
			if (!(n.tip instanceof Ext.Base)) {
				n.tip = Ext.widget("tooltip", n.tip)
			}
			n.tip.setTarget(k)
		}
		n.initEvents()
	}, initEvents: function () {
		var g = this, h = g.getCmp(), f = h.iconEl, e;
		Ext.Array.each(g.clickEvents, function (a) {
			f.on(a, function (b) {
				b.stopEvent();
				h.fireEvent("icon" + a, h, b);
				return false
			})
		})
	}, getIconConfig: function () {
		var f = this, d = f.getCmp(), e = {tag: "i", cls: Ext.String.format("{0} {1}", f.iconBaseCls, f.iconCls), style: {width: f.iconWidth + "px", height: f.iconHeight + "px", "font-size": f.iconHeight + "px", color: f.iconColor, cursor: f.iconCursor, margin: f.iconMargin}};
		if ("img" === f.iconMode && f.iconPath) {
			e.cls = f.iconBaseCls;
			e.cn = [{tag: "img", src: f.iconPath, style: {"vertical-align": "middle"}}]
		}
		if (!f.tip) {
			if (f.qtipTitle) {
				e["data-qtitle"] = f.qtipTitle
			}
			if (f.qtip) {
				e["data-qtip"] = f.qtip
			}
		}
		return e
	},
	destroy: function () {
		var b = this.getCmp().iconEl;
		b.removeAllListeners();
		Ext.destroy(b);
		b = null;
		console.log("destroyed")
	}});
