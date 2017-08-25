/*
 * Override default Ext error handling in order to avoid application hang.
 * NB: This is only necessary when using ExtJs debug file!
 * Error are traced in console instead.
 */
Ext.Error.handle = function(err) {
	Ext.log({
		msg: err.msg,
		level: 'error',
		dump: err/*,
		stack: true*/
	});
	console.log(new Error().stack);
	//WT.error("<b>CRITICAL ERROR!</b><br><br><i>"+err.msg+"</i><br><br>See Javascript console log for details");
	return true;
};