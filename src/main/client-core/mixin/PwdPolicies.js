/* 
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.mixin.PwdPolicies', {
	alternateClassName: 'WTA.mixin.PwdPolicies',
	extend: 'Ext.Mixin',
	uses: [
		'Ext.form.field.VTypes',
		'Sonicle.String'
	],
	
	mixinConfig: {
		id: 'wtpwdpolicies'
	},
	
	//https://techdocs.broadcom.com/us/en/symantec-security-software/identity-security/directory/14-0/administrating/manage-user-accounts-and-passwords/create-a-password-policy/how-to-configure-password-quality-rules.html#concept.dita_0a85cbdb992e79f526b9bbc690735f4e69805036_LimitRepetitionofSubstrings
	
	privates: {
		checkPolicies: function(pwd, pol, username, oldPwd) {
			var me = this, msgs = [];
			//if (levenThres === undefined) levenThres = 5;
			//if (tokenSize === undefined) tokenSize = 4;
			if (Ext.isObject(pol)) {
				var levenThres = parseInt(pol['levenThres']),
						tokenSize = parseInt(pol['tokenSize']);
				if (Ext.isNumber(pol['minLength']) && !me.checkMinLength(pwd, parseInt(pol['minLength']))) {
					msgs.push(WT.res(WT.ID, 'changePassword.error.minLength', parseInt(pol['minLength'])));
				}
				if (pol['complexity'] === true && !me.checkComplexity(pwd)) {
					msgs.push(WT.res('changePassword.error.complexity'));
				}
				if (pol['consecutiveDuplChars'] === true && !me.checkConsDuplChars(pwd)) {
					msgs.push(WT.res('changePassword.error.consecutiveDuplChars'));
				}
				if (pol['oldSimilarity'] === true && !me.checkSimilarity(pwd, oldPwd, levenThres, tokenSize)) {
					msgs.push(WT.res('changePassword.error.oldSimilarity'));
				}
				if (pol['usernameSimilarity'] === true && !me.checkSimilarity(pwd, username, levenThres, tokenSize)) {
					msgs.push(WT.res('changePassword.error.usernameSimilarity'));
				}
				if (msgs.length > 0) return msgs.join(' ');
			}
			return true;
		},
		
		checkMinLength: function(s, len) {
			return (Ext.isString(s) ? s.length : -1) >= len;
		},
		
		checkComplexity: function(s) {
			return Ext.form.field.VTypes.complexPassword(s);
		},
		
		checkConsDuplChars: function(s) {
			return !/^.*(.)\1.*$/.test(s);
		},
		
		checkSimilarity: function(a, b, levenThres, tokenSize) {
			console.log('levenThres: '+levenThres);
			console.log('tokenSize: '+tokenSize);
			var SoS = Sonicle.String,
					la = SoS.deflt(a, '').toLowerCase(),
					lb = SoS.deflt(b, '').toLowerCase();
			console.log('la: '+la);
			console.log('lb: '+lb);
			return SoS.levenshteinDistance(la, lb) >= levenThres && !SoS.containsSimilarTokens(la, lb, tokenSize);
		}
		
		/*
		similar_linux: function(a, b) {
			var i, j;
			for (i = j = 0; (i < a.length) && (i < b.length); i++) {
				if (b.indexOf(a[i]) > -1) j++;
			}
			return i >= j * 2 ? false : true;
		},
		
		similar_tokens: function(a, b, blockSize) {
			// matteo.albinola
			// matteo.
			
			if ((a.length < blockSize) || (b.length < blockSize)) return false;
			var i;
			for (i = 0; (i + blockSize) < a.length; i++) {
				if (b.indexOf(a.substr(i, blockSize)) !== -1) return true;
			}
			return false;
		}
		*/
	}
});
