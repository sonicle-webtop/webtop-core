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
package com.sonicle.webtop.core.app.pbx;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sonicle.webtop.core.sdk.WTException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author gabriele.bulfon
 */
public class NethVoice extends PbxProvider {
	
	private static final String HDR_WWW_AUTHENTICATE="www-authenticate";
	private static final String HDR_WWW_AUTHENTICATE_PREFIX="Digest ";
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	private static final String WEBREST_AUTHENTICATE = "/authentication/login";
	private static final String WEBREST_CALL = "/astproxy/call";
	
	private String webrestURL;
	
	public NethVoice(String webrestURL) {
		this.webrestURL=webrestURL;
	}

	@Override
	public boolean call(String number, String username, char[] password) throws WTException {
		boolean success=false;
		try {
			String spass=new String(password);
			String nonce=checkAuthentication(username, spass);
			if (nonce!=null) {
				String tohash=username+":"+spass+":"+nonce;
				String token=calculateRFC2104HMAC(tohash,spass);
				HttpResponse<String> resp=Unirest.post(webrestURL+WEBREST_CALL)
					.header("Authorization", username+":"+token)
					.field("number", number)
					.asString();
				success=true;
			}
		} catch (Exception ex) {
			throw new WTException(ex.getMessage());
		}
		return success;
	}
	
	
	// private implementations
	
	private String checkAuthentication(String username, String password) throws UnirestException, WTException {
		HttpResponse<String> resp=Unirest.post(webrestURL+WEBREST_AUTHENTICATE)
			.field("username",username)
			.field("password",new String(password))
			.asString();
		
		String nonce=null;
		String hdr=resp.getHeaders().getFirst(HDR_WWW_AUTHENTICATE);
		if (hdr!=null) {
			if (hdr.startsWith(HDR_WWW_AUTHENTICATE_PREFIX))
				nonce=hdr.substring(HDR_WWW_AUTHENTICATE_PREFIX.length()).trim();
		} else {
			throw new WTException("Error during authentication");
		}
		
		return nonce;
	}
	
	private static String calculateRFC2104HMAC(String data, String key)
		throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		return toHexString(mac.doFinal(data.getBytes()));
	}
	
	private static String toHexString(byte[] bytes) {
		Formatter formatter = new Formatter();
		
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}

		return formatter.toString();
	}
	
}
