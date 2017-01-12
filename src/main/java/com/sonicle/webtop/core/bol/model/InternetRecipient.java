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
package com.sonicle.webtop.core.bol.model;

/**
 *
 * @author malbinola
 */
public class InternetRecipient {
	
	public static class RecipientType {
		String type;
		
		RecipientType(String t) {
			this.type=t;
		}
		
		public String getType() {
			return type;
		}
		
		public String toString() {
			return type;
		}
	};
	
	public static final RecipientType TO=new RecipientType("to");
	public static final RecipientType CC=new RecipientType("cc");
	public static final RecipientType BCC=new RecipientType("bcc");
	
	private String source;
	private String sourceName;
	private String type;
	private String personal;
	private String address;
	private RecipientType recipientType=TO;
	
	public InternetRecipient() {}
	
	public InternetRecipient(String source, String sourceName, String type, String personal, String address) {
		this.source = source;
		this.sourceName = sourceName;
		this.type = type;
		this.personal = personal;
		this.address = address;
	}

	public InternetRecipient(String source, String sourceName, String type, String personal, String address, RecipientType rt) {
		this.source = source;
		this.sourceName = sourceName;
		this.type = type;
		this.personal = personal;
		this.address = address;
		this.recipientType = rt;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public RecipientType getRecipientType() {
		return recipientType;
	}
	
	public void setRecipientType(RecipientType rt) {
		this.recipientType=rt;
	}

	public String getPersonal() {
		return personal;
	}

	public void setPersonal(String personal) {
		this.personal = personal;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
