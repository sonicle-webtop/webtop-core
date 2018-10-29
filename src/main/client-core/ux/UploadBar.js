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
Ext.define('Sonicle.webtop.core.ux.UploadBar', {
	alternateClassName: 'WTA.ux.UploadBar',
	extend: 'Ext.toolbar.Toolbar',
	alias: ['widget.wtuploadbar'],
	requires: [
		'Sonicle.Bytes',
		'Sonicle.upload.Button'
	],
	
	config: {
		/**
		 * @cfg {Number/String} maxFileSize
		 * Maximum file size in bytes or a bytes string. Defaults to 10MB.
		 */
		maxFileSize: 10485760
	},
	
	/**
	 * @cfg {String} sid
	 * WebTop service ID.
	 */
	sid: null,
	
	/**
	 * @cfg {String} uploadContext
	 * The upload context name.
	 */
	uploadContext: null,
	
	/**
	 * @cfg {String} uploadTag
	 * The upload tag to group files.
	 */
	uploadTag: null,
	
	/**
	 * @cfg {String} buttonIconCls
	 * Upload button icon class.
	 */
	buttonIconCls: 'wt-icon-file-upload-xs',
	
	/**
	 * @cfg {String} dropElement
	 * The ID of DOM element to be used as the dropzone for the files.
	 */
	dropElement: null,
	
	fileExtraParams: null,
	
	constructor: function(cfg) {
		if (Ext.isEmpty(cfg.sid)) Ext.raise('Config `sid` is mandatory.');
		if (Ext.isEmpty(cfg.uploadContext)) Ext.raise('Config `uploadContext` is mandatory.');
		this.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
				SoByt = Sonicle.Bytes,
				mfs = me.getMaxFileSize(),
				de = me.dropElement;
		
		if (Ext.isString(mfs)) mfs = SoByt.parse(mfs);
		me.callParent(arguments);
		me.add([{
			xtype: 'souploadbutton',
			itemId: 'btnupload',
			text: WT.res('wtuploadbar.btn-upload.lbl'),
			tooltip: WT.res('wtuploadbar.btn-upload.tip'),
			iconCls: me.buttonIconCls,
			uploaderConfig: WTF.uploader(me.sid, me.uploadContext, {
				extraParams: {
					tag: me.uploadTag
				},
				maxFileSize: mfs,
				dropElement: de ? de : undefined,
				fileExtraParams: function() {
					if (Ext.isFunction(me.fileExtraParams)) {
						return me.fileExtraParams.apply(me);
					} else {
						return null;
					}
				},
				listeners: {
					uploaderror: function(s, file, cause) {
						me.self.handleUploadError(s, file, cause);
					},
					fileuploaded: function(s, file, json, resp) {
						me.fireEvent('fileuploaded', me, file, json, resp);
					},
					overallprogress: function(s, percent, total, succeeded, failed, queued, speed) {
						var pro = me.getProgress(),
								drh = me.geDropHere();
						if (queued > 0) {
							pro.updateProgress(percent*0.01, WT.res('wtuploadbar.progress.lbl', percent, queued-1, SoByt.format(speed || 0)));
							pro.setHidden(false);
							drh.setHidden(true);
						} else {
							pro.reset();
							pro.setHidden(true);
							drh.setHidden(false);
						}
					}
				}
			})
		}, ' ', {
			xtype: 'progressbar',
			itemId: 'progress',
			hidden: true,
			flex: 1
		}, {
			xtype: 'toolbar',
			itemId: 'drophere',
			cls: 'wt-uploadbar-drophere',
			layout: {
				type: 'hbox',
				pack: 'center'
			},
			border: 1,
			style: {
				borderStyle: 'dashed',
				borderRadius: '4px'
			},
			items: [{
					xtype: 'tbtext',
					cls: 'x-unselectable wt-uploadbar-drophere-text',
					text: WT.res('wtuploadbar.drophere.lbl')
					
			}],
			flex: 1
		}, ' ']);
	},
	
	getUploadButton: function() {
		return this.getComponent('btnupload');
	},
	
	getProgress: function() {
		return this.getComponent('progress');
	},
	
	geDropHere: function() {
		return this.getComponent('drophere');
	},
	
	statics: {
		handleUploadError: function(uploader, file, cause) {
			if (cause === 'size') {
				WT.warn(WT.res(WT.ID, 'error.upload.sizeexceeded', Sonicle.Bytes.format(uploader.getConfig('maxFileSize'))));
			} else {
				WT.error(WT.res('error.upload'));
			}
		}
	}
});
