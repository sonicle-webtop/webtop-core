/* 
 * Copyright (C) 2024 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2024 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.mixin.HasUpload', {
	alternateClassName: 'WTA.mixin.HasUpload',
	extend: 'Ext.Mixin',
	mixinConfig: {
		id: 'hasupload'
	},
	requires: [
		'Sonicle.Bytes'
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
	 * @cfg {Boolean} autoStart
	 * `false` to control upload start manually on the uploader component.
	 */
	autoStart: true,
	
	/**
	 * @cfg {String} dropElement
	 * The ID of DOM element to be used as the dropzone for the files.
	 */
	dropElement: null,
	
	fileExtraParams: null,
	
	constructor: function(cfg) {
		var me = this,
			icfg = Sonicle.Utils.getConstructorConfigs(me, cfg, ['sid', 'uploadContext', 'uploaderConfig', 'uploadTag', 'autoStart', 'maxFileSize', 'fileExtraParams', 'dropElement']);
		
		if (Ext.isEmpty(icfg.sid)) Ext.raise('Config `sid` is mandatory.');
		if (Ext.isEmpty(icfg.uploadContext)) Ext.raise('Config `uploadContext` is mandatory.');
		
		cfg.uploaderConfig = Ext.merge(icfg.uploaderConfig || {}, WTF.uploader(icfg.sid, icfg.uploadContext, {
			autoStart: Ext.isBoolean(icfg.autoStart) ? icfg.autoStart : me.autoStart,
			extraParams: {
				tag: icfg.uploadTag
			},
			maxFileSize: Ext.isNumber(icfg.maxFileSize) ? icfg.maxFileSize : me.maxFileSize,
			dropElement: icfg.dropElement ? icfg.dropElement : undefined,
			fileExtraParams: function() {
				if (Ext.isFunction(icfg.fileExtraParams)) {
					return icfg.fileExtraParams.apply(me);
				} else {
					return null;
				}
			}
		}));
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