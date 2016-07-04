/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 * http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
 */
Ext.define('Sonicle.Bytes', {
	singleton: true,
	
	unitSymbols: {
		iec: ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'],
		si: ['kB','MB','GB','TB','PB','EB','ZB','YB']
	},
	
	parserBases: [
		[['b', "bits"], {2: 1/8, 10: 1/8}],
		[['B', "Bytes"], {2: 1, 10: 1}],
		[["Kb"], {2: 128, 10: 125}],
		[["k", "K", "kb", "KB", "KiB", "Ki", "ki"], {2: 1024, 10: 1000}],
		[["Mb"], {2: 131072, 10: 125000}],
		[["m", "M", "mb", "MB", "MiB", "Mi", "mi"], {2: Math.pow(1024, 2), 10: 1.0e+6}],
		[["Gb"], {2: 1.342e+8, 10: 1.25e+8}],
		[["g", "G", "gb", "GB", "GiB", "Gi", "gi"], {2: Math.pow(1024, 3), 10: 1.0e+9}],
		[["Tb"], {2: 1.374e+11, 10: 1.25e+11}],
		[["t", "T", "tb", "TB", "TiB", "Ti", "ti"], {2: Math.pow(1024, 4), 10: 1.0e+12}],
		[["Pb"], {2: 1.407e+14, 10: 1.25e+14}],
		[["p", "P", "pb", "PB", "PiB", "Pi", "pi"], {2: Math.pow(1024, 5), 10: 1.0e+15}],
		[["Eb"], {2: 1.441e+17, 10: 1.25e+17}],
		[["e", "E", "eb", "EB", "EiB", "Ei", "ei"], {2: Math.pow(1024, 6), 10: 1.0e+18}]
	],
	
	/**
	 * Converts passed value in bytes in a human readable format.
	 * (eg. like '10 kB' or '100 MB')
	 * @param {int} value The value in bytes
	 * @param {Object} opts Computation options.
	 * @param {err|iec|si} [opts.units=err] Whether to use the erroneous (but common) 
	 *		representation (1024 magnitude + uppercase labels), 
	 *		the IEC units (1024 magnitude + IEC labels) 
	 *		or SI units (1000 magnitude)
	 * @param {String} [opts.separator] Separator to use between value and symbol.
	 * @param {int} [opts.decimals=2] Number of decimals to keep.
	 * @returns {String} The formatted string
	 */
	format: function(value, opts) {
		opts = opts || {};
		if(!value) return null;
		var uni = opts.units || 'err',
				mul = (uni === 'si') ? 1000: 1024,
				sym = this.unitSymbols[(uni === 'iec') ? 'iec' : 'si'],
				sep = opts.separator || ' ',
				dec = opts.decimals || 2,
				u = -1;
		
		if(uni === 'err') sym[0] = sym[0].toUpperCase();
		if(Math.abs(value) < mul) return value + sep + 'B';
		do {
			value /= mul;
			++u;
		} while(Math.abs(value) >= mul && u < sym.length - 1);
		return value.toFixed(dec) + sep + sym[u];
	},
	
	/**
	 * Parses a human readable file size string into a byte representation of it.
	 * @param {String} value The value to parse.
	 * @param {Object} opts Parsing options.
	 * @param {iec|si} [opts.base=iec] Whether to use the IEC (base 2) or the SI (base 10) multiplier. 
	 * @returns {Number} The parsed value or null
	 */
	parse: function(value, opts) {
		opts = opts || {};
		if(!value) return null;
		var pb = this.parserBases,
				base = (opts.base === 'si') ? 10: 2,
				parsed = value.match(/^([0-9\.,]*)(?:\s*)?(.*)$/),
				amount = parsed[1].replace(',','.'),
				unit = parsed[2],
				num, i;
		
		num = Ext.Number.from(amount, null);
		if((num == null) && !(unit.match(/\D*/).pop() === unit)) return null;
		if(Ext.isEmpty(unit)) return Math.round(num);
		
		for(i=0; i<pb.length; i++) {
			if(pb[i][0].indexOf(unit) != -1) {
				return Math.round(num * pb[i][1][base]);
			}
		}
		return null;
	},
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	function prettyNumber(pBytes, pUnits) {
    // Handle some special cases
    if(pBytes == 0) return '0 Bytes';
    if(pBytes == 1) return '1 Byte';
    if(pBytes == -1) return '-1 Byte';

    var bytes = Math.abs(pBytes)
    if(pUnits && pUnits.toLowerCase() && pUnits.toLowerCase() == 'si') {
        // SI units use the Metric representation based on 10^3 as a order of magnitude
        var orderOfMagnitude = Math.pow(10, 3);
        var abbreviations = ['Bytes', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    } else {
        // IEC units use 2^10 as an order of magnitude
        var orderOfMagnitude = Math.pow(2, 10);
        var abbreviations = ['Bytes', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
    }
    var i = Math.floor(Math.log(bytes) / Math.log(orderOfMagnitude));
    var result = (bytes / Math.pow(orderOfMagnitude, i));

    // This will get the sign right
    if(pBytes < 0) {
        result *= -1;
    }

    // This bit here is purely for show. it drops the percision on numbers greater than 100 before the units.
    // it also always shows the full number of bytes if bytes is the unit.
    if(result >= 99.995 || i==0) {
        return result.toFixed(0) + ' ' + abbreviations[i];
    } else {
        return result.toFixed(2) + ' ' + abbreviations[i];
    }
}
	*/
	
	/*
	 * Converts passed value in bytes in a human readable format.(eg. like '10 kB' or '100 MB')
	 * @param {int} bytes The value in bytes
	 * @param {iec|si} units Whether to use IEC units (2^10=1024) or SI units (10^3=1000)
	 * 
	 * 
	 * 
	 * @param {si|binary|uppercase} [opts.prefixes=si]
	 * @param {Boolean} [opts.si] Whether to use the SI multiple (1000) or binary one (1024)
	 * @param {Boolean} [opts.siUnits] Whether to use the SI units labels or binary ones
	 * @param {String} [opts.unitSeparator] Separator to use between value and unit
	 * @return {String} The formatted string
	 */
	humanReadableSize22: function(bytes, opts) {
		opts = opts || {};
		opts.unitSeparator = opts.unitSeparator || ' ';
		var mul = (opts.prefixes === 'binary') ? 1024 : 1000,
				units = (opts.prefixes === 'binary') ? ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'] : ['kB','MB','GB','TB','PB','EB','ZB','YB'],
				u;
		
		if(opts.prefixes === 'uppercase') units[0] = units[0].toUpperCase();
		if(Math.abs(bytes) < mul) return bytes + opts.unitSeparator + 'B';
		u = -1;
		do {
			bytes /= mul;
			++u;
		} while(Math.abs(bytes) >= mul && u < units.length - 1);
		return bytes.toFixed(1) + opts.unitSeparator + units[u];
		
		
		
		/*
		var thresh = (opts.si) ? 1000 : 1024,
				units = (opts.siUnits) ? ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'] : ['kB','MB','GB','TB','PB','EB','ZB','YB'],
				u;
		if(Math.abs(bytes) < thresh) return bytes + opts.unitSeparator + 'B';
		
		u = -1;
		do {
			bytes /= thresh;
			++u;
		} while(Math.abs(bytes) >= thresh && u < units.length - 1);
		return bytes.toFixed(1) + opts.unitSeparator + units[u];
		*/
		
		/*
		var s = bytes;
		bytes = parseInt(bytes/1024);
		if(bytes > 0) {
			if(bytes < 1024) {
				s = bytes + "KB";
			} else {
				s = parseInt(bytes/1024) + "MB";
			}
		}
		return s;
		*/
	}
});
