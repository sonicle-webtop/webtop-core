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
Ext.define('Sonicle.webtop.core.view.FeedbackC', {
	extend: 'Ext.app.ViewController',
	
	h2cCanvas: null,
	jpegQuality: 0.7, // 0.1 to 1 (1 = 100%)
	
	onSubmit: function(s,op,success) {
		if(success) {
			WT.info(WT.res('feedback.sent'));
			s.close();
		}
	},
	
	onCancelClick: function() {
		this.clearScreenshot();
		this.getView().close();
	},
	
	onSendClick: function() {
		var me = this;
		var w = this.getView();
		w.setFieldValue('timestamp', new Date().toString());
		var h2c = (me.h2cCanvas) ? me.h2cCanvas.toDataURL('image/jpeg', me.jpegQuality) : null;
		w.setFieldValue('image', h2c);
		w.submitForm();
	},
	
	onScreenshotChange: function(s, chk) {
		if(chk) {
			this.takeScreenshot();
		} else {
			this.clearScreenshot();
		}
	},
	
	takeScreenshot: function() {
		var me = this;
		var el = me.getView().getEl();
		el.mask(WT.res('feedback.capturing'), 'x-mask-loading');
		me.clearScreenshot();
		WT.loadScriptAsync('com.sonicle.webtop.core/lib/html2canvas.js', function(success) {
			if(success) {
				html2canvas([document.body], {
					onrendered: function(canvas) {
						var cel = Ext.get(canvas);
						cel.setStyle('position', 'absolute');
						cel.setStyle('left', 0);
						cel.setStyle('top', 0);
						cel.setStyle('z-index', 8900);
						Ext.get(document.body).insertSibling(cel);
						me.h2cCanvas = canvas;
						el.unmask();
					}
				});
			} else {
				el.unmask();
			}
		}, me);
	},
	
	clearScreenshot: function() {
		var me = this;
		if(me.h2cCanvas) {
			Ext.removeNode(Ext.get(me.h2cCanvas).dom);
			me.h2cCanvas = null;
			me.getView().setFieldValue('screenshot', false);
		}
	}
});
