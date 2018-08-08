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
package com.sonicle.webtop.core.util;

import com.fasterxml.uuid.Generators;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
@Deprecated
public class IdentifierUtils {
	private static final char[] VALID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456879".toCharArray();
	
	public static synchronized String getUUID() {
		return getUUID(false);
	}
	
	/**
	 * Generates a characters UUID.
	 * With noHiphens at False, resulting string will be 36 characters length; 32 otherwise.
	 * @param noDashes
	 * @return 
	 */
	@Deprecated
	public static synchronized String getUUID(boolean noDashes) {
		String uuid = UUID.randomUUID().toString();
		return (noDashes) ? StringUtils.replace(uuid, "-", "") : uuid;
	}
	
	@Deprecated
	public static synchronized String getUUIDTimeBased() {
		return getUUIDTimeBased(false);
	}
	
	@Deprecated
	public static synchronized String getUUIDTimeBased(boolean noDashes) {
		final String uuid = Generators.timeBasedGenerator().generate().toString();
		return (noDashes) ? StringUtils.replace(uuid, "-", "") : uuid;
	}
	
	@Deprecated
	public static synchronized String getUUIDRandom() {
		return getUUIDRandom(false);
	}
	
	@Deprecated
	public static synchronized String getUUIDRandom(boolean noDashes) {
		final String uuid = Generators.randomBasedGenerator().generate().toString();
		return (noDashes) ? StringUtils.replace(uuid, "-", "") : uuid;
	}
	
	@Deprecated
	public static synchronized String getCRSFToken() {
		try {
			byte[] buffer = new byte[80/8];
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.nextBytes(buffer);
			byte[] secretKey = Arrays.copyOf(buffer, 80/8);
			byte[] encodedKey = new Base32().encode(secretKey);
			return new String(encodedKey).toLowerCase();
		} catch(NoSuchAlgorithmException ex) {
			return null;
		}
	}
	
	@Deprecated
	public static synchronized String generateSecretKey() {
		try {
			byte[] buffer = new byte[80/8];
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.nextBytes(buffer);
			byte[] secretKey = Arrays.copyOf(buffer, 80/8);
			byte[] encodedKey = new Base32().encode(secretKey);
			return new String(encodedKey).toLowerCase();
		} catch(NoSuchAlgorithmException ex) {
			return null;
		}
	}
	
	@Deprecated
	public static synchronized String getRandomAlphaNumericString(int length) {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			Random rand = new Random();
			char buff[] = new char[length];
			for(int i = 0; i < length; ++i) {
				// reseed rand once you've used up all available entropy bits
				if ((i % 10) == 0) rand.setSeed(sr.nextLong()); // 64 bits of random!
				buff[i] = VALID_CHARACTERS[rand.nextInt(VALID_CHARACTERS.length)];
			}
			return new String(buff);
		} catch(NoSuchAlgorithmException ex) {
			return null;
		}
	}
}
