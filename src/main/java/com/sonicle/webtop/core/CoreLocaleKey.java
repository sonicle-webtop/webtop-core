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
package com.sonicle.webtop.core;

/**
 *
 * @author malbinola
 */
public class CoreLocaleKey {
	public static final String SERVICE_NAME = "service.name";
	public static final String SERVICE_DESCRIPTION = "service.description";
	
	public static final String TPL_LOGIN_USERNAME_PLACEHOLDER = "tpl.login.username.placeholder";
	public static final String TPL_LOGIN_PASSWORD_PLACEHOLDER = "tpl.login.password.placeholder";
	public static final String TPL_LOGIN_DOMAIN_LABEL = "tpl.login.domain.label";
	public static final String TPL_LOGIN_DOMAIN_PROMPT = "tpl.login.domain.prompt";
	public static final String TPL_LOGIN_SUBMIT_LABEL = "tpl.login.submit.label";
	public static final String TPL_LOGIN_MAINTENANCE = "tpl.login.maintenance";
	public static final String TPL_LOGIN_ERROR_MAINTENANCE = "tpl.login.error.maintenance";
	public static final String TPL_LOGIN_ERROR_FAILURE = "tpl.login.error.failure";
	
	public static final String TPL_PASSWORD_MAIN_TITLE = "tpl.password.main.title";
	public static final String TPL_PASSWORD_MAIN_TEXT = "tpl.password.main.text";
	public static final String TPL_PASSWORD_PASSWORD_LABEL = "tpl.password.password.label";
	public static final String TPL_PASSWORD_PASSWORDCONFIRM_LABEL = "tpl.password.passwordConfirm.label";
	public static final String TPL_PASSWORD_SUBMIT_LABEL = "tpl.password.submit.label";
	public static final String TPL_PASSWORD_ERROR_EMPTYFIELD = "tpl.password.error.emptyfield";
	public static final String TPL_PASSWORD_ERROR_POLICY = "tpl.password.error.policy";
	public static final String TPL_PASSWORD_ERROR_CONFITMNOTMATCH = "tpl.password.error.confirmnotmatch";
	public static final String TPL_PASSWORD_ERROR_MUSTBEDIFFERENT = "tpl.password.error.mustbedifferent";
	public static final String TPL_PASSWORD_ERROR_UNEXPECTED = "tpl.password.error.unexpected";
	
	public static final String TPL_OTP_HELPTITLE = "tpl.otp.helptitle";
	public static final String TPL_OTP_DELIVERY_TITLE = "tpl.otp.delivery.title";
	public static final String TPL_OTP_DELIVERY_INFO = "tpl.otp.delivery.{0}.info";
	public static final String TPL_OTP_CODE_PLACEHOLDER = "tpl.otp.code.placeholder";
	public static final String TPL_OTP_SUBMIT_LABEL = "tpl.otp.submit.label";
	public static final String TPL_OTP_TRUST_LABEL = "tpl.otp.trust.label";
	public static final String TPL_OTP_ERROR_FAILURE = "tpl.otp.error.failure";
	
	public static final String TPL_NOTIFICATION_NOREPLY_FOOTER_HEADER = "tpl.notification.noreply.footer.header";
	public static final String TPL_NOTIFICATION_NOREPLY_FOOTER_MESSAGE = "tpl.notification.noreply.footer.message";
	public static final String TPL_NOTIFICATION_FOOTER_HEADER = "tpl.notification.footer.header";
	public static final String TPL_NOTIFICATION_FOOTER_MESSAGE = "tpl.notification.footer.message";
	public static final String TPL_EMAIL_OTPCODEVERIFICATION_BODY_HEADER = "tpl.email.otpCodeVerification.body.header";
	public static final String TPL_EMAIL_OTPCODEVERIFICATION_BODY_MESSAGE = "tpl.email.otpCodeVerification.body.message";
	public static final String TPL_EMAIL_DEVICESYNCCHECK_BODY_HEADER = "tpl.email.devicesSyncCheck.body.header";
	public static final String TPL_EMAIL_DEVICESYNCCHECK_BODY_MESSAGE = "tpl.email.devicesSyncCheck.body.message";
	
	public static final String SMS_ERROR_BAD_TEXT = "sms.error.bad.text";
	public static final String SMS_ERROR_BAD_FROM = "sms.error.bad.from";
	public static final String SMS_ERROR_BAD_CREDIT = "sms.error.bad.credit";
	public static final String SMS_ERROR_INVALID_RECIPIENT = "sms.error.invalid.recipient";
	
//public static final String OTP_SETUP_ERROR_CODE = "otp.setup.error.code";
	
	public static final String XMPP_ERROR_CONNECTION = "xmpp.error.connection";
	public static final String XMPP_ERROR_AUTHENTICATION = "xmpp.error.authentication";
	public static final String LOCALE_X = "locale.{0}";
	public static final String WORD_DATE_DAY = "word.date.day";
	public static final String WORD_DATE_MONTH = "word.date.month";
	public static final String WORD_DATE_YEAR = "word.date.year";
	public static final String WORD_DATE_TODAY = "word.date.today";
	public static final String WORD_DATE_YESTERDAY = "word.date.yesterday";
	public static final String WORD_DATE_TOMORROW = "word.date.tomorrow";
	public static final String WORD_TIME_SECOND = "word.time.second";
	public static final String WORD_TIME_SECONDS = "word.time.seconds";
	public static final String WORD_TIME_MINUTE = "word.time.minute";
	public static final String WORD_TIME_MINUTES = "word.time.minutes";
	public static final String WORD_TIME_HOUR = "word.time.hour";
	public static final String WORD_TIME_HOURS = "word.time.hours";
	public static final String WORD_ALL_MALE = "word.all.male";
	public static final String WORD_ALL_FEMALE = "word.all.female";
	public static final String WORD_NONE_MALE = "word.none.male";
	public static final String WORD_NONE_FEMALE = "word.none.female";
	public static final String WORD_YES = "word.yes";
	public static final String WORD_NO = "word.no";
	public static final String INTERNETRECIPIENT_AUTO = "internetRecipient.auto";
	public static final String INTERNETRECIPIENT_WEBTOP = "internetRecipient.webtop";
	
	public static final String DETECT_ATTACH_PATTERNS = "detect.attach.patterns";
	
	public static String TAGS_LABEL(String label) {
		return "tags."+label;
	} 
	
	
}
