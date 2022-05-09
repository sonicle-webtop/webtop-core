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
package com.sonicle.webtop.core.bol.js;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Federico Ballarini
 */
public class JsAuditMessageInfo {
	public String from;
	public String fromDN;
	public ArrayList<String> tos;
	public ArrayList<String> tosDN;
	public ArrayList<String> ccs;
	public ArrayList<String> ccsDN;
	public ArrayList<String> bccs;
	public ArrayList<String> bccsDN;
	public String subject;
	public String folder;
	public String fwdFrom;
	public String rplTo;
	public Boolean sched;

	public JsAuditMessageInfo() {
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFromDN() {
		return fromDN;
	}

	public void setFromDN(String fromDN) {
		this.fromDN = fromDN;
	}
	
	public ArrayList<String> getTos() {
		return tos;
	}

	public void setTos(ArrayList<String> tos) {
		this.tos = tos;
	}

	public ArrayList<String> getTosDN() {
		return tosDN;
	}

	public void setTosDN(ArrayList<String> tosDN) {
		this.tosDN = tosDN;
	}

	public ArrayList<String> getCcs() {
		return ccs;
	}

	public void setCcs(ArrayList<String> ccs) {
		this.ccs = ccs;
	}

	public ArrayList<String> getCcsDN() {
		return ccsDN;
	}

	public void setCcsDN(ArrayList<String> ccsDN) {
		this.ccsDN = ccsDN;
	}

	public ArrayList<String> getBccs() {
		return bccs;
	}

	public void setBccs(ArrayList<String> bccs) {
		this.bccs = bccs;
	}

	public ArrayList<String> getBccsDN() {
		return bccsDN;
	}

	public void setBccsDN(ArrayList<String> bccsDN) {
		this.bccsDN = bccsDN;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getForwardedFrom() {
		return fwdFrom;
	}

	public void setForwardedFrom(String forwardedFrom) {
		this.fwdFrom = forwardedFrom;
	}

	public String getInReplyTo() {
		return rplTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.rplTo = inReplyTo;
	}

	public Boolean getScheduled() {
		return sched;
	}

	public void setScheduled(Boolean scheduled) {
		this.sched = scheduled;
	}
	
	
}
