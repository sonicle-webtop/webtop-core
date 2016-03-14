/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.selection.RowModel', {
	extend: 'Ext.selection.RowModel',
	alias: 'selection.sorowmodel',
	
	/**
	 * Remove passed selection or current selection from store,
	 * then automatically set the new selection near the first
	 * record of the old selection
	 * @param {Ext.data.Model[]} [selection] The selection to be removed. Defaults to current selection.
	 */
	removeSelection: function(selection) {
		var me=this,
			s=selection||me.getSelection(),
			st=me.store,
			ix=me.store.indexOf(s[0]);
		
		if (st.remove) {
			st.remove(s);
			me._reselect(ix);
		}
		else {
			st.reload({
				callback: function() {
					me._reselect(ix);
				}
			});
		}
		
	},
	
	/**
	 * Remove passed IDs from store,
	 * then automatically set the new selection near the first
	 * record of the first removed ID
	 * @param {String[]} [ids] The ids to be removed.
	 */
	removeIds: function(ids) {
		var me=this,
			st=me.store,
			ix=me.store.indexOfId(ids[0]);
		
		if (st.remove) {
			var recs=[];
			Ext.each(ids,function(id) {
				var r=st.getById(id);
				if (r) recs[recs.length]=r;
			});
			st.remove(recs);
		}
		else {
			st.reload({
				callback: function() {
					me._reselect(ix);
				}
			});
		}
	},
	
	_reselect: function(ix) {
		var me=this;
		if (ix>=me.store.getCount()) --ix;
		if (ix>=0) {
			me.view.bufferedRenderer.scrollTo(ix, true);
			me.select(ix);
		}
	}
	
	
});
