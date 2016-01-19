/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.upload.Uploader', {
	requires: [
		'Sonicle.upload.Model'
	],
	mixins: [
		'Ext.mixin.Observable'
	],
	
	statics: {
		/**
		 * Adds passed mimeType and related extensions to mOxie internal 
		 * structures. (see mOxie.Mime.addMimeType() method)
		 * @param {String} mimeType The content mimeType.
		 * @param {String/String[]} extension File extensions to register within mimeType.
		 */
		registerMimeType: function(mimeType, extension) {
			if(!mOxie) return;
			if(!Ext.isArray(extension)) extension = [extension];
			var mm = mOxie.Mime,
					exts = mm.extensions[mimeType] || [];
			
			Ext.iterate(extension, function(ext) {
				if(exts.indexOf(ext) === -1) exts.push(ext);
			});
			mm.addMimeType(Ext.String.format('{0},{1}', mimeType, exts.join(' ')));
		}
	},
	
	config: {
		autoStart: true,
		autoRemoveUploaded: true,
		url: null,
		extraParams: null,
		mimeTypes: [],
		maxFileSize: '128mb',
		preventDuplicates: true,
		multiSelection: true,
		uniqueNames: true,
		runtimes: null,
		container: null,
		browseButton: null,
		dropElement: null,
		flashSwfUrl: null,
		silverlightXapUrl: null,
		pluploadConfig: null // Direct config to apply
	},
	
	pluOptions: null,
	
	constructor: function(owner, cfg) {
		var me = this;
		me.owner = owner;
		me.succeeded = [];
		me.failed = [];
		
		me.initConfig(cfg);
		me.mixins.observable.constructor.call(me, cfg);
		
		me.store = Ext.create('Ext.data.JsonStore', {
			model: 'Sonicle.upload.Model',
			listeners: {
				load: me.onStoreLoad,
				remove: me.onStoreRemove,
				update: me.onStoreUpdate,
				scope: me
			}
		});
		
		Ext.Function.interceptAfter(me, 'setExtraParams', function() {
			me.refreshPluOptions();
		});
	},
	
	mergeExtraParams: function(obj) {
		var me = this;
		me.setExtraParams(Ext.apply(me.getExtraParams() || {}, obj));
	},
	
	removeAll: function() {
		var me = this;
		me.store.each(function(rec) {
			if(rec) me.removeFile(rec.get('id'));
		});
	},
	
	removeUploaded: function() {
		var me = this;
		me.store.each(function(rec) {
			if(rec && (rec.get('status') === 5)) {
				me.removeFile(rec.get('id'));
			}
		});
	},
	
	removeFile: function(id) {
		var me = this,
				file = me.uploader.getFile(id);
		if(file) {
			me.uploader.removeFile(file);
		} else {
			me.store.remove(me.store.getById(id));
		}
	},
	
	cancel: function() {
		this.uploader.stop();
	},
	
	start: function() {
		var me = this;
		me.fireEvent('beforestart', me);
		me.uploader.start();
	},
	
	/**
	 * @private
	 */
	buildPluploadUrl: function(url, extraParams) {
		return Ext.String.urlAppend(url, Ext.Object.toQueryString(extraParams || {}));
	},
	
	/**
	 * @private
	 */
	buildPluOptions: function() {
		var me = this, pluopts;
		
		pluopts = Ext.apply({}, me.getPluploadConfig() || {}, {
			browse_button: me.getBrowseButton(),
			url: me.buildPluploadUrl(me.getUrl(), me.getExtraParams()),
			filters: {
				mime_types: me.getMimeTypes(),
				max_file_size: me.getMaxFileSize(),
				preventDuplicates: me.getPreventDuplicates()
			},
			multipart: true,
			multipart_params: {},
			chunk_size: 0, // Disabled
			//chunk_size: '1mb', // @see http://www.plupload.com/punbb/viewtopic.php?id=1259
			resize: null,
			multi_selection: me.getMultiSelection(),
			required_features: null,
			unique_names: me.getUniqueNames(),
			runtimes: me.getRuntimes(),
			container: me.getContainer(),
			drop_element: me.getDropElement(),
			flash_swf_url: me.getFlashSwfUrl(),
			silverlight_xap_url: me.getSilverlightXapUrl()
		});
		
		if(!pluopts.runtimes) {
			var runtimes = ['html5'];
			pluopts.flash_swf_url && runtimes.push('flash');
			pluopts.silverlight_xap_url && runtimes.push('silverlight');
			runtimes.push('html4');
			pluopts.runtimes = runtimes.join(',');
		}
		if(!pluopts.container) {
			pluopts.container = Ext.fly(pluopts.browse_button).parent().id;
		}
		
		return pluopts;
	},
	
	refreshPluOptions: function() {
		var me = this, opt;
		if(me.inited) {
			me.pluOptions = me.buildPluOptions();
			if(me.uploader) {
				me.uploader.setOption('url', me.pluOptions.url);
				/*
				for(opt in me.pluOptions) {
					me.uploader.setOption(opt, me.pluOptions[opt]);
				}
				*/
			}
		}
	},
	
	/**
	 * @private
	 */
	init: function() {
		var me = this;
		if(!me.inited) {
			me.inited = true;
			me.initUploader();
		}
	},
	
	/**
	 * @private
	 */
	updateProgress: function() {
		var me = this,
				progress = me.uploader.total,
				speed = Ext.util.Format.fileSize(progress.bytesPerSec),
				total = me.store.data.length,
				failed = me.failed.length,
				succeeded = me.succeeded.length,
				sent = failed + succeeded,
				queued = total - succeeded - failed,
				percent = progress.percent;
		
		me.fireEvent('updateprogress', me, total, percent, sent, succeeded, failed, queued, speed);		
	},
	
	/**
	 * @private
	 */
	updateStore: function(v) {
		var me = this,
				rec = me.store.getById(v.id),
				data;
		
		data = {
			id: v.id,
			name: v.name,
			size: v.size,
			percent: v.percent,
			status: v.status,
			loaded: v.loaded
		};
		
		if(rec) {
			rec.set(data);
			rec.commit();
		} else {
			me.store.loadData([data], true);
		}
	},
	
	onStoreLoad: function(sto, rec, op) {
		this.updateProgress();
	},
	
	onStoreRemove: function(sto, rec, op) {
		var me = this;
		
		if(sto.count() <= 0) {
			me.uploader.total.reset();
			me.fireEvent('storeempty', me);
		}
		
		var id = rec[0].get('id');
		Ext.each(me.succeeded, function(v) {
			if(v && v.id === id) Ext.Array.remove(me.succeeded, v);
		});
		Ext.each(me.failed, function(v) {
			if(v && v.id === id) Ext.Array.remove(me.failed, v);
		});
		
		me.updateProgress();
	},
	
	onStoreUpdate: function(sto, rec, op) {
		this.updateProgress();
	},
	
	enable: function() {
		console.log('TODO: Uploader.enable()');
	},
	
	disable: function() {
		console.log('TODO: Uploader.disable()');
	},
	
	/**
	 * @private
	 */
	initUploader: function() {
		var me = this;
		
		me.pluOptions = me.buildPluOptions();
		me.uploader = Ext.create('plupload.Uploader', me.pluOptions);
		
		Ext.each([
			'Init',
			'PostInit',
			'Refresh',
			'StateChanged',
			'QueueChanged',
			'BeforeUpload',
			'UploadFile',
			'UploadProgress',
			'FileUploaded',
			'ChunkUploaded',
			'FilesAdded',
			'FilesRemoved',
			'Error'
		], function (v) {
			me.uploader.bind(v, eval("me._" + v), me);
		}, me);

		me.uploader.init();
	},
	
	_Init: function(uploader, data) {
		this.runtime = data.runtime;
		this.fireEvent('uploadready', this);
	},
	
	_PostInit: function(uploader) {
		// Do nothing...
	},
	
	_Refresh: function(uploader) {
		Ext.each(uploader.files, function(v) {
			this.updateStore(v);
		}, this);
	},
	
	_StateChanged: function(uploader) {
		var me = this;
		if(uploader.state === 2) {
			me.fireEvent('uploadstarted', me);
		} else {
			me.fireEvent('uploadcomplete', me, me.succeeded, me.failed);
			if(me.getAutoRemoveUploaded()) me.removeUploaded();
		}
	},
	
	_QueueChanged: function(uploader) {
		// Do nothing...
	},
	
	_BeforeUpload: function(uploader, file) {
		this.fireEvent('beforeupload', this, uploader, file);
	},
	
	_UploadFile: function(uploader, file) {
		// Do nothing...
	},
	
	_UploadProgress: function(uploader, file) {
		var me = this,
				name = file.name,
				size = file.size,
				percent = file.percent;
		
		me.fireEvent('uploadprogress', me, file, name, size, percent);
		if(file.server_error) file.status = 4;
		me.updateStore(file);
	},
	
	_FileUploaded: function(uploader, file, status) {
		var me = this,
				response = Ext.JSON.decode(status.response);
		
		if(response.success === true) {
			if(response.data && (response.data.temp === true)) {
				file.uploadId = response.data.uploadId;
			}
			file.server_error = 0;
			me.updateStore(file);
			me.succeeded.push(file);
			me.fireEvent('fileuploaded', me, file, response);
		} else {
			//if(response.message) file.msg = '<span style="color: red">' + response.message + '</span>';
			file.server_error = 1;
			me.failed.push(file);
			me.fireEvent('uploaderror', me, Ext.apply(status, {
				file: file
			}));
		}
	},
	
	_ChunkUploaded: function() {
		// Do nothing...
	},
	
	_FilesAdded: function(uploader, files) {
		var me = this;
		if(me.pluOptions.multi_selection !== true) {
			if(me.store.data.length === 1) return false;
			files = [files[0]];
			uploader.files = [files[0]];
		}
		
		Ext.each(files, function(v) {
			me.updateStore(v);
		});
		
		if(me.fireEvent('filesadded', me, files) !== false) {
			if(me.getAutoStart() && uploader.state !== 2) {
				Ext.defer(function() {
					me.start();
				});
			}
		}
	},
	
	_FilesRemoved: function(uploader, files) {
		var me = this;
		Ext.each(files, function(file) {
			me.store.remove(me.store.getById(file.id));
		}, me);
	},
	
	_Error: function(uploader, data) {
		var me = this;
		
		if(data.file) {
			data.file.status = 4;
			me.failed.push(data.file);
			me.updateStore(data.file);
		}
		me.fireEvent('uploaderror', me, data);
	}
});
