/*
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.license4j.ValidationStatus;
import com.rits.cloning.Cloner;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.l4j.HardwareID;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.l4j.ProductLicense.LicenseInfo;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.bean.StringMap;
import com.sonicle.webtop.core.app.model.GenericSubject;
import com.sonicle.webtop.core.app.model.LicenseBase;
import com.sonicle.webtop.core.app.model.LicenseComputedStatus;
import com.sonicle.webtop.core.app.model.LicenseComputedStatus;
import com.sonicle.webtop.core.app.model.LicenseExInfo;
import com.sonicle.webtop.core.app.model.LicenseListOption;
import com.sonicle.webtop.core.app.sdk.WTLicenseActivationException;
import com.sonicle.webtop.core.app.sdk.WTLicenseMismatchException;
import com.sonicle.webtop.core.app.sdk.WTLicenseValidationException;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.bol.OLicense;
import com.sonicle.webtop.core.bol.OLicenseLease;
import com.sonicle.webtop.core.bol.VLicense;
import com.sonicle.webtop.core.app.events.LicenseUpdateEvent;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.LicenseDAO;
import com.sonicle.webtop.core.dal.LicenseLeaseDAO;
import com.sonicle.webtop.core.model.License;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.ServiceLicenseLease;
import com.sonicle.webtop.core.model.ServiceLicenseLease.LeaseOrigin;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.qualitycheck.Check;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class LicenseManager extends AbstractAppManager<LicenseManager> {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(LicenseManager.class);
	
	private Scheduler scheduler = null;
	private final JobKey dailyCleanupJobKey;
	private final JobKey dailyCheckJobKey;
	private final Map<String, ProductData> productLicenseCache = new ConcurrentHashMap<>();
	private final Map<String, Integer> licenseLeaseCache = new ConcurrentHashMap<>();
	private final Map<String, Integer> lastTrackedLeaseValue = new ConcurrentHashMap<>();
	
	LicenseManager(WebTopApp wta, Scheduler scheduler) {
		super(wta, true);
		this.scheduler = scheduler;
		this.dailyCleanupJobKey = JobKey.jobKey(CacheCleanupJob.class.getCanonicalName(), "webtop");
		this.dailyCheckJobKey = JobKey.jobKey(CheckJob.class.getCanonicalName(), "webtop");
		initialize();
	}
	
	@Override
	protected Logger doGetLogger() {
		return LOGGER;
	}
	
	@Override
	protected void doAppManagerInitialize() {
		
		try {
			LOGGER.debug("Scheduling daily cleanup job...");
			JobDetail jobDetail = JobBuilder.newJob(CacheCleanupJob.class)
				.withIdentity(dailyCleanupJobKey)
				.build();
			jobDetail.getJobDataMap().put("this", this);
			scheduler.scheduleJob(jobDetail, TriggerBuilder.newTrigger()
				//.withSchedule(org.quartz.SimpleScheduleBuilder.repeatMinutelyForever(5))
				.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 0))
				.build()
			);
			
		} catch (SchedulerException ex) {
			throw new WTRuntimeException(ex, "Unable to schedule CacheCleanupJob");
		}
		
		try {
			int hour = RandomUtils.nextInt(0, 24);
			int minute = RandomUtils.nextInt(0, 60);
			if (hour == 0 && minute == 0) minute++;
			LOGGER.debug("Scheduling daily check job... [{}:{}]", hour, minute);
			JobDetail jobDetail = JobBuilder.newJob(CheckJob.class)
				.withIdentity(dailyCheckJobKey)
				.build();
			jobDetail.getJobDataMap().put("this", this);
			scheduler.scheduleJob(jobDetail, TriggerBuilder.newTrigger()
				//.withSchedule(org.quartz.SimpleScheduleBuilder.repeatMinutelyForever(1))
				.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(hour, minute))
				.build()
			);
			
		} catch (SchedulerException ex) {
			throw new WTRuntimeException(ex, "Unable to schedule CheckJob");
		}
	}

	@Override
	protected void doAppManagerCleanup() {
		try {
			if (!scheduler.isShutdown()) {
				scheduler.deleteJobs(Arrays.asList(dailyCleanupJobKey, dailyCheckJobKey));
			}
		} catch (SchedulerException ex) {
			LOGGER.warn("Unable to delete jobs", ex);
		}
		scheduler = null;
		productLicenseCache.clear();
		licenseLeaseCache.clear();
		lastTrackedLeaseValue.clear();
	}
	
	public void cleanupLicenseCache() {
		productLicenseCache.clear();
		licenseLeaseCache.clear();
	}
	
	public String computeActivationHardwareID() throws WTException {
		final String hw1 = HardwareID.getHardwareIDFromHostName();
		if (LOGGER.isTraceEnabled()) LOGGER.trace("Computing HwID from hostname [{}]", hw1);
		String hw2 = HardwareID.getHardwareIDFromEthernetAddress(true);
		if (LOGGER.isTraceEnabled()) LOGGER.trace("Computing HwID from eth0 [{}]", hw2);
		if (hw2 == null) {
			hw2 = HardwareID.getHardwareIDFromUUIDString(WebTopProps.getUUID(getWebTopApp().getProperties()));
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Computing HwID from string [{}]", hw2);
		}
		if (StringUtils.isBlank(hw2)) throw new WTException("Computed HwID tokens do NOT meet requirement: unable to guarantee the uniqness!");
		return LangUtils.joinStrings("!", hw1, hw2);
	}
	
	public ProductLicense getProductLicenseOrThrow(final BaseServiceProduct product) throws WTNotFoundException {
		final ProductLicense license = getProductLicense(product);
		if (license == null) throw new WTNotFoundException("License not found [{}]", product.getProductCode());
		return license;
	}
	
	public ProductLicense getProductLicense(final BaseServiceProduct product) {
		// This will always return a value, see dummy object creation!
		return getProductLicenseData(product).license;
	}
	
	public ProductData getProductLicenseData(final BaseServiceProduct product) {
		Check.notNull(product, "product");
		
		String key = productLicenseCacheKey(product.getDomainId(), product.SERVICE_ID, product.getProductCode());
		ProductData data = productLicenseCache.computeIfAbsent(key, k -> {
			LicenseDAO licDao = LicenseDAO.getInstance();
			Connection con = null;
			
			try {
				if (WebTopManager.SYSADMIN_DOMAINID.equals(product.getDomainId())) return new ProductData(null, null, false);
				
				con = getConnection(true);
				OLicense olic = licDao.select(con, product.getDomainId(), product.SERVICE_ID, product.getProductCode());
				if (olic != null) {
					ProductLicense plicNew = new ProductLicense(product);
					plicNew.setLicenseString(olic.getString());
					plicNew.setActivationCustomHardwareId(computeActivationHardwareID());
					plicNew.setActivatedLicenseString(olic.getActivatedString());

					LicenseInfo li = plicNew.validate(true);
					/*
					LicenseInfo ali = plicNew.validate(false);
					//if (li.getLicenseID() != ali.getLicenseID()) throw new WTException("License ID mismatch [{} != {}]", li.getLicenseID(), ali.getLicenseID());
					boolean match = li.getLicenseID() == ali.getLicenseID();
					if (match && li.isValid() && ali.isValid() && ali.isActivationCompleted() && li.getQuantity() != null) {
						leasedUsers = lleaDao.selectUsersByDomainServiceProduct(con, domainId, product.getProductId().getServiceId(), product.getProductId().getProductCode(), li.getQuantity());
					}
					*/

					// Provided quantity can be a true value only if license validates correctly!
					Integer maxLease = li.isValid() ? li.getQuantity() : null;
					return new ProductData(plicNew, maxLease, olic.getAutoLease());

				} else {
					if (!StringUtils.isBlank(product.getBuiltInLicenseString())) {
						ProductLicense plicNew = new ProductLicense(product);
						plicNew.setLicenseString(product.getBuiltInLicenseString());
						plicNew.setCustomHardwareId(product.getBuiltInHardwareId());
						
						LicenseInfo li = plicNew.validate(true);
						return new ProductData(plicNew, null, false);
						
					} else {
						if (LOGGER.isTraceEnabled()) LOGGER.trace("License missing, returning dummy! [{}]", key);
						return new ProductData(null, null, false);
					}	
				}

			} catch(Throwable t) {
				LOGGER.error("Error retrieving registered license [{}]", key, t);
			} finally {
				DbUtils.closeQuietly(con);
			}
			return null;
		});
		return data;
	}
	
	public boolean checkLicense(final BaseServiceProduct product) {
		ProductLicense plic = getProductLicense(product);
		if (plic == null) return false;
		
		// Do license checks
		LicenseInfo li = plic.validate(true);
		if (!li.isValid()) return false; // Reject if license is not in status VALID
		if (li.isActivationRequired()) {
			LicenseInfo ali = plic.validate(false);
			if (!ali.isValid()) return false; // Reject if activated license is not in status VALID
			if (!ali.isActivationCompleted()) return false; // Reject if activated license is not in status ACTIVATED
			if (li.getLicenseID() != ali.getLicenseID()) return false; // Reject if license IDs mismatch
		}
		
		/*
		// Do license checks
		LicenseInfo li = plic.validate(true);
		LicenseInfo ali = plic.validate(false);
		if (!li.isValid()) return false; // Reject if license is not in status VALID
		if (!ali.isValid()) return false; // Reject if activated license is not in status VALID
		if (!ali.isActivationCompleted()) return false; // Reject if activated license is not in status ACTIVATED
		if (li.getLicenseID() != ali.getLicenseID()) return false; // Reject if license IDs mismatch
		*/
		
		return true;
	}
	
	public int checkLicenseLease(final BaseServiceProduct product, final String userId) {
		Check.notNull(product, "product");
		Check.notNull(product, "userId");
		
		String key = licenseLeaseCacheKey(product.getDomainId(), product.SERVICE_ID, product.getProductCode(), userId);
		Integer result = licenseLeaseCache.computeIfAbsent(key, k -> {
			ProductData pData = getProductLicenseData(product);
			// Do product checks (0)
			if (pData.license == null) return 0; // Failure: missing license
			
			// Do license checks (-1)
			LicenseInfo li = pData.license.validate(true);
			LicenseInfo ali = pData.license.validate(false);
			if (!li.isValid()) return -1; // Failure: license is not in status VALID
			if (!ali.isValid()) return -1; // Failure: activated license is not in status VALID
			if (!ali.isActivationCompleted()) return -1; // Failure: activated license is not in status ACTIVATED
			if (li.getLicenseID() != ali.getLicenseID()) return -1; // Failure: license IDs mismatch
			
			// Do lease checks (1, -2, -3)
			if (li.getQuantity() == null) return 1; // Success: quantity is unbounded
			try {
				return internalCheckAndAssignLicenseLease(product.getDomainId(), product.getProductId(), pData, userId, ServiceLicenseLease.LeaseOrigin.AUTO, li.getQuantity());
			
			} catch(Throwable t) {
				LOGGER.error("Error retrieving registered license [{}]", t, key);
				return null;
			}
		});
		if (LOGGER.isTraceEnabled()) LOGGER.trace("licenseLeaseCache : {} -> {}", key, result);
		return result == null ? 0 : result;
	}
	
	public void checkOnlineAvailability() {
		LicenseDAO licDao = LicenseDAO.getInstance();
		MultiValuedMap<String, ProductId> blacklisted = new ArrayListValuedHashMap<>();
		Connection con = null;
		
		try {
			if (!getWebTopApp().isLatest()) return;
			con = getConnection(true);
			Map<String, Integer> cfails = new HashMap<>();
			Map<String, List<String>> map = licDao.groupAllLicenses(con);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String domainId = entry.getKey();
				
				try {
					for (String productId : entry.getValue()) {
						ProductId prodId = new ProductId(productId);
						
						try {
							ProductData pdata = findProductLicenseData(domainId, prodId);
							if (pdata.license != null) {
								final String server = pdata.license.getLicenseServer();
								if (cfails.getOrDefault(server, 0) >= 2) continue;
								int ret = pdata.license.checkOnlineAvailability(5000);
								if (ret == -1) {
									int count = cfails.getOrDefault(server, 0);
									cfails.put(server, count+1);
								} else if (ret == 0) {
									blacklisted.put(domainId, prodId);
								} 
							}
						} catch(Throwable t) {
							// Do nothing...
						}
					}
				} catch(Throwable t) {
					// Do nothing...
				}
			}
			
			if (!blacklisted.isEmpty()) {
				boolean cleanup = false;
				for (Map.Entry<String, ProductId> entry : blacklisted.entries()) {
					try {
						int ret = licDao.updateActivation(con, entry.getKey(), entry.getValue().getServiceId(), entry.getValue().getProductCode(), null, null, null);
						if (ret > 0) cleanup = true;
					} catch(Throwable t) {
						// Do nothing...
						LOGGER.error("Error updating activation info", t);
					}
				}
				if (cleanup) cleanupLicenseCache();
			}
			
		} catch(Throwable t) {
			// Do nothing...
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<ServiceLicense> listLicenses(final String domainId, final BitFlags<LicenseListOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(options, "options");
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			con = getConnection(true);
			String ativationHwID = options.has(LicenseListOption.EXTENDED_INFO) ? computeActivationHardwareID() : null;
			ArrayList<ServiceLicense> items = new ArrayList<>();
			HashSet<String> realIds = new HashSet<>();
			for (VLicense vlic : licDao.viewByDomain(con, domainId)) {
				realIds.add(ProductId.build(vlic.getServiceId(), vlic.getProductCode()).toString());
				List<OLicenseLease> oleases = lleaDao.selectByDomainServiceProduct(con, domainId, vlic.getServiceId(), vlic.getProductCode());
				
				final ServiceLicense serviceLicense = AppManagerUtils.createServiceLicense(vlic, oleases);
				if (options.has(LicenseListOption.EXTENDED_INFO)) {
					try {
						BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(serviceLicense.getProductCode(), domainId);
						ProductLicense productLicense = getProductLicenseOrThrow(product);
						serviceLicense.setExtendedInfo(doLicenseGetExtendedInfo(serviceLicense, product, productLicense, ativationHwID));
					} catch (Exception ex2) {
						LOGGER.error("Unable to build extended info for '{}'", ex2, serviceLicense.getProductCode());
					}
				}
				items.add(serviceLicense);
			}
			
			if (options.has(LicenseListOption.INCLUDE_BUILTIN)) {
				for (String sid: getWebTopApp().getServiceManager().listRegisteredServices()) {
					ServiceManifest manifest = WT.getManifest(sid);
					for (ServiceManifest.Product smProduct : manifest.getProducts()) {
						ProductId productId = ProductId.build(sid, smProduct.code);
						if (realIds.contains(productId.toString())) continue;
						BaseServiceProduct product = ProductRegistry.getInstance().getServiceProduct(productId, domainId);
						if (product != null && StringUtils.isBlank(product.getBuiltInLicenseString())) continue;
						
						final ServiceLicense serviceLicense = AppManagerUtils.createBuiltInServiceLicense(product);
						if (options.has(LicenseListOption.EXTENDED_INFO)) {
							try {
								ProductLicense productLicense = getProductLicenseOrThrow(product);
								serviceLicense.setExtendedInfo(doLicenseGetExtendedInfo(serviceLicense, product, productLicense, ativationHwID));
							} catch (Exception ex2) {
								LOGGER.error("Unable to build extended info for '{}'", ex2, serviceLicense.getProductCode());
							}
						}
						items.add(serviceLicense);
					}
				}
			}
			
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ServiceLicense getLicense(final String domainId, final String productCode) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		Connection con = null;
		
		try {
			ProductRegistry.ProductEntry product = ProductRegistry.getInstance().getProductOrThrow(productCode);
			
			con = getConnection(true);
			return doServiceLicenseGet(con, domainId, product.getServiceId(), product.getProductCode(), true);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private LicenseExInfo doLicenseGetExtendedInfo(ServiceLicense serviceLicense, BaseServiceProduct serviceProduct, ProductLicense productLicense, final String hardwareID) {
		BitFlags<LicenseComputedStatus> status = BitFlags.noneOf(LicenseComputedStatus.class);
		
		if (!StringUtils.isBlank(serviceProduct.getBuiltInLicenseString())) productLicense.setCustomHardwareId(serviceProduct.getBuiltInHardwareId());
		productLicense.setLicenseString(serviceLicense.getLicenseString());
		productLicense.setActivationCustomHardwareId(hardwareID);
		productLicense.setActivatedLicenseString(serviceLicense.getActivatedLicenseString());
		
		LicenseInfo li = productLicense.validate(true);
		LicenseInfo ali = productLicense.validate(false);
		
		///////////////////////////
		//valid = (li.getLicenseID() == ali.getLicenseID()) && ali.isValid();
		//activated = ali.isActivationCompleted();
		//expired = li.isExpired();
		//expireSoon = li.isExpiringSoon();
		///////////////////////////
		
		if (li.isActivationRequired()) {
			if (ali.isActivationCompleted()) {
				status.set(LicenseComputedStatus.ACTIVATED);
			} else {
				status.set(LicenseComputedStatus.PENDING_ACTIVATION);
			}
			if (li.isValid() && ali.isValid() && ali.isActivationCompleted() && li.getLicenseID() == ali.getLicenseID()) {
				if (li.isValid()) status.set(LicenseComputedStatus.VALID);
			}
		} else {
			if (li.isValid()) status.set(LicenseComputedStatus.VALID);
		}
		if (li.isExpired()) {
			status.set(LicenseComputedStatus.EXPIRED);
		}
		if (li.isExpiringSoon()) {
			status.set(LicenseComputedStatus.EXPIRE_SOON);
		}
		
		return new LicenseExInfo(li.getHardwareID(), LicenseExInfo.buildRegisteredTo(li), status);
	}
	
	public final BitFlags<LicenseComputedStatus> getLicenseStatus(final String domainId, final String productCode, final String hardwareID) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		Check.notEmpty(hardwareID, "hardwareID");
		Connection con = null;
		
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
		ProductLicense productLicense = getProductLicenseOrThrow(product);
		
		ServiceLicense serviceLicense = null;
		try {
			con = getConnection(true);
			serviceLicense = doServiceLicenseGet(con, domainId, product.getProductId(), false);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		if (serviceLicense == null) throw new WTNotFoundException("Unknown product [{}]", productCode);
		
		// Analyze status...
		BitFlags<LicenseComputedStatus> status = BitFlags.noneOf(LicenseComputedStatus.class);
		
		if (!StringUtils.isBlank(product.getBuiltInLicenseString())) productLicense.setCustomHardwareId(product.getBuiltInHardwareId());
		productLicense.setLicenseString(serviceLicense.getLicenseString());
		productLicense.setActivationCustomHardwareId(hardwareID);
		productLicense.setActivatedLicenseString(serviceLicense.getActivatedLicenseString());
		
		LicenseInfo li = productLicense.validate(true);
		LicenseInfo ali = productLicense.validate(false);
		
		///////////////////////////
		//valid = (li.getLicenseID() == ali.getLicenseID()) && ali.isValid();
		//activated = ali.isActivationCompleted();
		//expired = li.isExpired();
		//expireSoon = li.isExpiringSoon();
		///////////////////////////
		
		if (li.isActivationRequired()) {
			if (ali.isActivationCompleted()) {
				status.set(LicenseComputedStatus.ACTIVATED);
			} else {
				status.set(LicenseComputedStatus.PENDING_ACTIVATION);
			}
			if (li.isValid() && ali.isValid() && ali.isActivationCompleted() && li.getLicenseID() == ali.getLicenseID()) {
				if (li.isValid()) status.set(LicenseComputedStatus.VALID);
			}
		} else {
			if (li.isValid()) status.set(LicenseComputedStatus.VALID);
		}
		if (li.isExpired()) {
			status.set(LicenseComputedStatus.EXPIRED);
		}
		if (li.isExpiringSoon()) {
			status.set(LicenseComputedStatus.EXPIRE_SOON);
		}
		
		return status;
	}
	
	public void addLicense(final String domainId, final String productCode, final LicenseBase license, final boolean autoActivate) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		Check.notNull(license, "license");
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
			
			ProductLicense productLicense = new ProductLicense(product);
			productLicense.setLicenseString(license.getLicenseString());
			LicenseInfo li = productLicense.validate(true);
			if (li.isInvalid()) throw new WTLicenseValidationException(li);
			
			//ProductLicense tplProductLicense = findProductLicense(license.getDomainId(), license.getProductId());
			//if (!tplProductLicense.getLicenseInfo().isValid()) throw new WTException("License provided for '{}' is not valid", license.getProductId().getProductCode());
			
			OLicense olic = AppManagerUtils.fillOLicense(new OLicense(), license);
			olic.setDomainId(domainId);
			olic.setServiceId(product.SERVICE_ID);
			olic.setProductCode(productCode);
			fillOLicenseWithDefaults(olic, li);
			
			con = getConnection(true);
			if (licDao.insert(con, olic) == 1) {
				// Cleanup cached dummy ProductLicense
				forgetProductLicense(domainId, product.SERVICE_ID, productCode);
			}
			if (autoActivate) activateLicense(product, null);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void changeLicense(final String domainId, final String productCode, final String newString, final String activatedString, final boolean force) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
		changeLicense(product, newString, activatedString, force);
	}
	
	public void changeLicense(final BaseServiceProduct product, final String newString, final String activatedString, final boolean force) throws WTException {
		Check.notNull(product, "product");
		Check.notEmpty(newString, "newString");
		
		ProductLicense productLicense = getProductLicenseOrThrow(product);
		boolean ret = internalChangeLicense(product.getDomainId(), product.SERVICE_ID, product.getProductCode(), productLicense, newString, activatedString, force);
		if (ret) forgetProductLicense(product.getDomainId(), product.SERVICE_ID, product.getProductCode());
	}
	
	public void modifyLicense(final String domainId, final String productCode, final String modificationKey, final String modifiedLicenseString) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
		modifyLicense(product, modificationKey, modifiedLicenseString);
	}
	
	public void modifyLicense(final BaseServiceProduct product, final String modificationKey, final String modifiedLicenseString) throws WTException {
		Check.notNull(product, "product");
		Check.notEmpty(modificationKey, "modificationKey");
		
		//FIXME: is modifiedLicenseString an activated string?
		//TODO: not supported yet! Verify implementation!
		ProductLicense productLicense = getProductLicenseOrThrow(product);
		boolean ret = internalModifyLicense(product.getDomainId(), product.SERVICE_ID, product.getProductCode(), productLicense, modificationKey, modifiedLicenseString);
		if (ret) {
			fireEvent(new LicenseUpdateEvent(product.getDomainId(), LicenseUpdateEvent.Type.MODIFY, product));
			forgetProductLicense(product.getDomainId(), product.SERVICE_ID, product.getProductCode());
		}
	}
	
	public void updateLicenseAutoLease(final String domainId, final String productCode, final boolean autoLease) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			ProductRegistry.ProductEntry product = ProductRegistry.getInstance().getProductOrThrow(productCode);
			
			con = getConnection(true);
			licDao.updateAutoLease(con, domainId, product.getServiceId(), product.getProductCode(), autoLease);
			forgetProductLicense(domainId, product.getServiceId(), product.getProductCode());
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteLicense(final String domainId, final String productCode, final boolean force) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
			ProductLicense productLicense = getProductLicenseOrThrow(product);
			
			LicenseInfo li = productLicense.validate(true);
			if (li.isInvalid()) throw new WTLicenseValidationException(li);
			if (!force) {
				li = productLicense.validate(false);
				if (li.isActivationCompleted()) throw new WTException("License is activated, deactivate it before proceed.");
			}
			
			con = getConnection(true);
			licDao.delete(con, domainId, product.SERVICE_ID, productCode);
			forgetProductLicense(domainId, product.SERVICE_ID, productCode);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void activateLicense(final String domainId, final String productCode, final String activatedString) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
		activateLicense(product, activatedString);
	}
	
	public void activateLicense(final BaseServiceProduct product, final String activatedString) throws WTException {
		Check.notNull(product, "product");
		
		ProductLicense productLicense = getProductLicenseOrThrow(product);
		boolean ret = internalActivateLicense(product.getDomainId(), product.getProductId(), productLicense, activatedString);
		if (ret) {
			fireEvent(new LicenseUpdateEvent(product.getDomainId(), LicenseUpdateEvent.Type.ACTIVATE, product));
			forgetProductLicense(product.getDomainId(), product.SERVICE_ID, product.getProductCode());
		}
	}
	
	public void deactivateLicense(final String domainId, final String productCode, final boolean offline) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
		deactivateLicense(product, offline);
	}
	
	public void deactivateLicense(final BaseServiceProduct product, final boolean offline) throws WTException {
		Check.notNull(product, "product");
		
		ProductLicense productLicense = getProductLicenseOrThrow(product);
		boolean ret = internalDeactivateLicense(product.getDomainId(), product.getProductId(), productLicense, offline);
		if (ret) {
			fireEvent(new LicenseUpdateEvent(product.getDomainId(), LicenseUpdateEvent.Type.DEACTIVATE, product));
			forgetProductLicense(product.getDomainId(), product.SERVICE_ID, product.getProductCode());
		}
	}
	
	public void assignLicenseLease(final String domainId, final String productCode, final Set<String> userIds) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		Check.notEmpty(userIds, "userIds");
		
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
		assignLicenseLease(product, userIds);
	}
	
	public void assignLicenseLease(final BaseServiceProduct product, final Set<String> userIds) throws WTException {
		Check.notNull(product, "product");
		Check.notEmpty(userIds, "userIds");
		
		ProductData productData = getProductLicenseData(product);
		if (productData == null) throw new WTNotFoundException("Unknown product [{}]", product.getProductCode());
		boolean ret = internalAssignLicenseLease(product.getDomainId(), product.getProductId(), productData, userIds, ServiceLicenseLease.LeaseOrigin.STATIC);
		if (ret) forgetLicenseLease(product.getDomainId(), product.SERVICE_ID, product.getProductCode(), userIds);
	}
	
	public void revokeLicenseLease(final BaseServiceProduct product, final Set<String> userIds) throws WTException {
		Check.notNull(product, "product");
		Check.notEmpty(userIds, "userIds");
		
		ProductData productData = getProductLicenseData(product);
		if (productData == null) throw new WTNotFoundException("Unknown product [{}]", product.getProductCode());
		internalRevokeLicenseLease(product.getDomainId(), product.getProductId(), productData, userIds);
		forgetLicenseLease(product.getDomainId(), product.SERVICE_ID, product.getProductCode(), userIds);
	}
	
	public void revokeLicenseLease(final String domainId, final String productCode, final Set<String> userIds) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(productCode, "productCode");
		Check.notEmpty(userIds, "userIds");
		
		//LOGGER.debug("Revoking license '{}' for user '{}'...", productId, new UserProfileId(domainId, userId));
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProductOrThrow(productCode, domainId);
		revokeLicenseLease(product, userIds);
	}
	
	public void revokeLicenseLease(final UserProfileId profileId) throws WTException {
		Check.notNull(profileId, "profileId");
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		LOGGER.debug("Revoking all licenses for profile '{}'...", profileId);
		List<OLicenseLease> leases = null;
		try {
			con = getConnection(true);
			leases = lleaDao.selectByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			for (OLicenseLease lease : leases) {
				//ProductId productId = ProductId.build(lease.getServiceId(), lease.getProductCode());
				//ProductLicense productLicense = findProductLicense(profileId.getDomainId(), productId);
				LOGGER.debug("[{}] Revoking license '{}'...", profileId, lease.getProductCode());
				//doRevokeLicenseLease(con, profileId.getDomainId(), productId, tplProductLicense, profileId.getUserId(), lease.getActivationString());
				forgetLicenseLease(profileId.getDomainId(), lease.getServiceId(), lease.getProductCode(), LangUtils.asSet(profileId.getUserId()));
			}
		
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	private ProductLicense findProductLicense(String domainId, ProductId productId) throws WTException {
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProduct(productId, domainId);
		if (product == null) throw new WTNotFoundException("Unknown product [{}]", productId);
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTNotFoundException("Unknown product [{}]", productId);
		return tplProductLicense;
	}
	*/
	
	private ProductLicense findProductLicense(String domainId, ProductId productId) throws WTException {
		return findProductLicense(domainId, productId.getProductCode());
	}
	
	private ProductLicense findProductLicense(String domainId, String productCode) throws WTException {
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProduct(productCode, domainId);
		if (product == null) throw new WTNotFoundException("Unknown product [{}]", productCode);
		ProductLicense productLicense = getProductLicense(product);
		if (productLicense == null) throw new WTNotFoundException("Unknown product [{}]", productCode);
		return productLicense;
	}
	
	private ProductData findProductLicenseData(String domainId, ProductId productId) throws WTException {
		BaseServiceProduct product = ProductRegistry.getInstance().getServiceProduct(productId, domainId);
		if (product == null) throw new WTNotFoundException("Unknown product [{}]", productId);
		ProductData plData = getProductLicenseData(product);
		if (plData == null) throw new WTNotFoundException("Unknown product [{}]", productId);
		return plData;
	}
	
	private LicenseInfo checkCompat(LicenseInfo base, LicenseInfo activated) throws WTLicenseMismatchException {
		if (base.getLicenseID() != activated.getLicenseID()) {
			throw new WTLicenseMismatchException(base.getLicenseID(), activated.getLicenseID());
		}
		return activated;
	}
	
	private boolean internalChangeLicense(final String domainId, final String serviceId, final String productCode, final ProductLicense plicOrig, final String newString, final String activatedString, final boolean force) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("[{}] Changing license...", productCode);
			LicenseInfo li = plicOrig.validate(true);
			LOGGER.debug("[{}] {} (old) -> Validate Be. [{}, {}]", productCode, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (li.isInvalid()) throw new WTLicenseValidationException(li);
			if (!force) {
				li = plicOrig.validate(false);
				LOGGER.debug("[{}] {} (old) -> Validate Af. [{}, {}]", productCode, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
				if (li.isActivationCompleted()) throw new WTException("License is activated, deactivate it before proceed.");
			}
			
			ProductLicense plic = Cloner.standard().deepClone(plicOrig);
			plic.setLicenseString(newString);
			li = plic.validate(true);
			LOGGER.debug("[{}] {} (new) -> Validate Be. [{}, {}]", productCode, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			
			LocalDate expDate = li.getExpirationDate();
			Integer quantity = li.getQuantity();
			
			if (StringUtils.isBlank(activatedString)) {
				LOGGER.debug("[{}] Activation string not provided. Performing automatic activation...", productCode);
				li = plic.autoActivate();
			} else {
				LOGGER.debug("[{}] Activated string provided. Performing manual activation...", productCode);
				li = checkCompat(li, plic.manualActivate(activatedString));
			}
			LOGGER.debug("[{}] {} (new) -> Validate Ac. [{}, {}]", productCode, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (!li.isActivationCompleted()) throw new WTLicenseActivationException(li);
			
			con = getConnection(true);
			boolean ret = licDao.replaceLicense(con, domainId, serviceId, productCode, plic.getLicenseString(), expDate, quantity, plic.getActivatedLicenseString(), DateTimeUtils.now(), li.getHardwareID()) == 1;
			if (quantity != null) {
				OLicense olic = licDao.lock(con, domainId, serviceId, productCode);
				if (olic == null) throw new WTException("Unable to lookup license '{}'", productCode);
				lleaDao.deleteExeedingByDomainServiceProduct(con, domainId, serviceId, productCode, quantity);
			}
			
			DbUtils.commitQuietly(con);
			return ret;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalModifyLicense(final String domainId, final String serviceId, final String productCode, final ProductLicense tplProductLicense, final String modificationKey, final String activatedString) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("[{}] Modifying license...", productCode);
			LicenseInfo li = tplProductLicense.validate(false);
			LOGGER.debug("[{}] {} (old) -> Validate Af. [{}, {}]", productCode, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			
			Integer quantity = li.getQuantity();
			
			ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
			if (StringUtils.isBlank(activatedString)) {
				LOGGER.debug("[{}] Activation string not provided. Performing automatic modification...", productCode);
				li = plic.modifyLicense(modificationKey);
			} else {
				LOGGER.debug("[{}] Activated string provided. Performing manual modification...", productCode);
				li = checkCompat(li, plic.manualModify(activatedString));
			}
			LOGGER.debug("[{}] {} (new) -> Validate Mo. [{}, {}]", productCode, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (!li.isActivationCompleted()) throw new WTLicenseActivationException(li);
			
			con = getConnection(true);
			boolean ret = licDao.updateActivation(con, domainId, serviceId, productCode, plic.getActivatedLicenseString(), DateTimeUtils.now(), li.getHardwareID(), li.getExpirationDate()) == 1;
			if (quantity != null) {
				OLicense olic = licDao.lock(con, domainId, serviceId, productCode);
				if (olic == null) throw new WTException("Unable to lookup license '{}'", productCode);
				lleaDao.deleteExeedingByDomainServiceProduct(con, domainId, serviceId, productCode, quantity);
			}
			
			DbUtils.commitQuietly(con);
			return ret;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalActivateLicense(final String domainId, final ProductId productId, final ProductLicense tplProductLicense, final String activatedString) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("[{}] Activating license...", productId);
			LicenseInfo li = tplProductLicense.validate(true);
			LOGGER.debug("[{}] {} -> Validate Be. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			
			Integer quantity = li.getQuantity();
			
			ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
			if (StringUtils.isBlank(activatedString)) {
				LOGGER.debug("[{}] Activation string not provided. Performing automatic activation...", productId);
				li = plic.autoActivate();
			} else {
				LOGGER.debug("[{}] Activated string provided. Performing manual activation...", productId);
				li = checkCompat(li, plic.manualActivate(activatedString));
			}
			LOGGER.debug("[{}] {} -> Validate Ac. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (!li.isActivationCompleted()) throw new WTLicenseActivationException(li);
			
			con = getConnection(true);
			boolean ret = licDao.updateActivation(con, domainId, productId.getServiceId(), productId.getProductCode(), plic.getActivatedLicenseString(), DateTimeUtils.now(), li.getHardwareID(), li.getExpirationDate()) == 1;
			if (quantity != null) {
				OLicense olic = licDao.lock(con, domainId, productId.getServiceId(), productId.getProductCode());
				if (olic == null) throw new WTException("Unable to lookup license '{}'", productId.getProductCode());
				lleaDao.deleteExeedingByDomainServiceProduct(con, domainId, productId.getServiceId(), productId.getProductCode(), quantity);
			}
			
			DbUtils.commitQuietly(con);
			return ret;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalDeactivateLicense(final String domainId, final ProductId productId, final ProductLicense tplProductLicense, final boolean offline) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("[{}] Deactivating license...", productId);
			LicenseInfo li = null;
			
			WTException afterUpdateThrow = null;
			ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
			if (!offline) {
				LOGGER.debug("[{}] Performing automatic deactivation...", productId);
				li = plic.autoDeactivate();
				LOGGER.debug("[{}] {} -> Validate De. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
				if (li.isActivationNotFound()) {
					// If activation is not found on server, simply erase activation
					// data for this license and only after throw the exception!
					afterUpdateThrow = new WTLicenseActivationException(li);
					
				} else if (ValidationStatus.MISMATCH_HARDWARE_ID.equals(li.getValidationStatus()) && li.isActivationCompleted()) {
					// If domainInternetName (for products that are bounded to 
					// it) is changed we can have this situation, we
					// can erase activation data locally for this license and 
					// only after throw the exception!
					afterUpdateThrow = new WTLicenseValidationException(li);
					
				} else if (!li.isDeactivationCompleted()) {
					throw new WTLicenseActivationException(li);
				}
				
			} else {
				LOGGER.debug("[{}] Performing manual deactivation...", productId);
				//li = plic.manualDeactivate();
			}
			
			con = getConnection(true);
			boolean ret = licDao.updateActivation(con, domainId, productId.getServiceId(), productId.getProductCode(), null, DateTimeUtils.now(), null) == 1;
			if (afterUpdateThrow != null) throw afterUpdateThrow;
			return ret;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private int internalCheckAndAssignLicenseLease(final String domainId, final ProductId productId, final ProductData pData, final String userId, final LeaseOrigin origin, final int maxQuantity) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			con = getConnection(false);
			OLicense olic = licDao.lock(con, domainId, productId.getServiceId(), productId.getProductCode());
			if (olic == null) throw new WTException("Unable to lookup license '{}'", productId.getProductCode());
			
			int ret = -2;
			int origCount = lleaDao.countByDomainServiceProduct(con, domainId, productId.getServiceId(), productId.getProductCode());
			if (lleaDao.existsByDomainServiceProductUser(con, domainId, productId.getServiceId(), productId.getProductCode(), userId)) {
				ret = origCount <= maxQuantity ? 1 : 2;
			} else if (!pData.autoLease) {
				ret = -3;
			} else {
				if (origCount +1 <= maxQuantity) {
					if (lleaDao.insert(con, domainId, productId.getServiceId(), productId.getProductCode(), userId, DateTimeUtils.now(), origin) == 1) {
						ret = 1;
					}
				}
			}
			
			DbUtils.commitQuietly(con);
			return ret;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalAssignLicenseLease(final String domainId, final ProductId productId, final ProductData pData, final Set<String> userIds, final LeaseOrigin origin) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			LicenseInfo li = pData.license.validate(true);
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (li.getQuantity() == null) throw new WTException("Lease attribution is not supported for '{}'", productId.getProductCode());
			
			con = getConnection(false);
			OLicense olic = licDao.lock(con, domainId, productId.getServiceId(), productId.getProductCode());
			if (olic == null) throw new WTException("Unable to lookup license '{}'", productId.getProductCode());
			
			boolean ret = false;
			Set<String> okUserIds = getWebTopApp().getWebTopManager().parseSubjectsAsStringLocals(userIds, true, domainId, GenericSubject.Type.USER);
			int origCount = lleaDao.countByDomainServiceProduct(con, domainId, productId.getServiceId(), productId.getProductCode());
			if ((origCount + userIds.size()) <= li.getQuantity()) {
				ret = lleaDao.batchInsert(con, domainId, productId.getServiceId(), productId.getProductCode(), okUserIds, DateTimeUtils.now(), origin).length == okUserIds.size();
			} else {
				throw new WTException("Unable to satisfy required quantity for '{}' [{}+{} > {}]", productId.getProductCode(), origCount, okUserIds.size(), li.getQuantity());
			}
			
			DbUtils.commitQuietly(con);
			return ret;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalRevokeLicenseLease(final String domainId, final ProductId productId, final ProductData pData, final Set<String> userIds) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			con = getConnection(false);
			OLicense olic = licDao.lock(con, domainId, productId.getServiceId(), productId.getProductCode());
			if (olic == null) throw new WTException("Unable to lookup license '{}'", productId.getProductCode());
			Set<String> okUserIds = getWebTopApp().getWebTopManager().parseSubjectsAsStringLocals(userIds, true, domainId, GenericSubject.Type.USER);
			boolean ret = lleaDao.delete(con, domainId, productId.getServiceId(), productId.getProductCode(), okUserIds) == okUserIds.size();
			
			DbUtils.commitQuietly(con);
			return ret;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void updateLeaseTrackingInfo() {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			if (!getWebTopApp().isLatest()) return;
			
			con = getConnection(true);
			Map<String, Integer> fails = new HashMap<>();
			Map<String, List<String>> map = licDao.groupAllLicenses(con);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String domainId = entry.getKey();
				
				try {
					for (String productId : entry.getValue()) {
						ProductId prodId = new ProductId(productId);

						try {
							ProductData pdata = findProductLicenseData(domainId, prodId);
							if (pdata.license != null && pdata.maxLease != null) {
								int currentLease = lleaDao.countByDomainServiceProduct(con, domainId, prodId.getServiceId(), prodId.getProductCode());
								
								final String leaseLTKey = domainId + "|" + prodId.toString();
								// Skip if value is not changed
								if (lastTrackedLeaseValue.getOrDefault(leaseLTKey, -1) == currentLease) continue;
								// Skip if failure counts have reached threshold
								final String server = pdata.license.getLicenseServer();
								if (fails.getOrDefault(server, 0) >= 2) continue;
								
								int ret = pdata.license.updateTrackingInfo(new StringMap().withValue("lease", String.valueOf(currentLease)), 5000);
								if (ret == -1) {
									int count = fails.getOrDefault(server, 0);
									fails.put(server, count+1);
								} else if (ret == 1) {
									lastTrackedLeaseValue.put(leaseLTKey, currentLease);
								}
							}
						} catch (Exception ex2) {
							// Do nothing...
						}
					}
				} catch (Exception ex1) {
					// Do nothing...
				}
			}
		} catch (Exception ex) {
			// Do nothing...
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void enforceConstraints() {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		int deleted = 0;
		try {
			if (!getWebTopApp().isLatest()) return;
			
			con = getConnection(true);
			Map<String, List<String>> map = licDao.groupAllLicenses(con);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String domainId = entry.getKey();
				
				try {
					for (String productId : entry.getValue()) {
						ProductId prodId = new ProductId(productId);

						try {
							ProductData pdata = findProductLicenseData(domainId, prodId);
							if (pdata.license != null && pdata.maxLease != null) {
								OLicense olic = licDao.lock(con, domainId, prodId.getServiceId(), prodId.getProductCode());
								if (olic == null) throw new WTException("Unable to lookup license '{}'", prodId.getProductCode());
								deleted += lleaDao.deleteExeedingByDomainServiceProduct(con, domainId, prodId.getServiceId(), prodId.getProductCode(), pdata.maxLease);
							
								DbUtils.commitQuietly(con);
							}
						} catch(Throwable t) {
							DbUtils.rollbackQuietly(con);
							// Do nothing...
						}
					}
				} catch(Throwable t) {
					// Do nothing...
				}
			}
			
		} catch(Throwable t) {
			// Do nothing...
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (deleted > 0) {
			cleanupLicenseCache();
		}
	}
	
	private ServiceLicense doServiceLicenseGet(Connection con, String domainId, ProductId product, boolean processLeases) throws DAOException, WTException {
		return doServiceLicenseGet(con, domainId, product.getServiceId(), product.getProductCode(), processLeases);
	}
	
	private ServiceLicense doServiceLicenseGet(Connection con, String domainId, String serviceId, String productCode, boolean processLeases) throws DAOException, WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		
		OLicense olic = licDao.select(con, domainId, serviceId, productCode);
		if (olic == null) return null;
		
		ServiceLicense lic = AppManagerUtils.createServiceLicense(olic);
		if (processLeases) {
			List<OLicenseLease> oleases = lleaDao.selectByDomainServiceProduct(con, domainId, serviceId, productCode);
			lic.setLeases(AppManagerUtils.createServiceLicenseLeaseMap(oleases));
		}
		return lic;
	}
	
	private <T extends OLicense> T fillOLicenseWithDefaults(T tgt, LicenseInfo li) {
		if ((tgt != null)) {
			tgt.setExpirationDate(li.getExpirationDate());
			tgt.setQuantity(li.getQuantity());
			if (tgt.getAutoLease() == null) tgt.setAutoLease(true);
		}
		return tgt;
	}
	
	private String productLicenseCacheKey(String domainId, String service, String productCode) {
		return domainId + "|" + service + "|" + productCode;
	}
	
	private String licenseLeaseCacheKey(String domainId, String service, String productCode, String user) {
		return domainId + "|" + service + "|" + productCode + "|" + user;
	}
	
	private void forgetProductLicense(String domainId, String serviceId, String productCode) {
		String key = productLicenseCacheKey(domainId, serviceId, productCode);
		productLicenseCache.remove(key);
		licenseLeaseCache.entrySet().removeIf(e -> StringUtils.startsWith(e.getKey(), key));
	}
	
	private void forgetLicenseLease(String domainId, String serviceId, String productCode, Set<String> users) {
		for (String user : users) {
			licenseLeaseCache.remove(licenseLeaseCacheKey(domainId, serviceId, productCode, user));
		}
	}
	
	public static class ProductData {
		public final ProductLicense license;
		public final Integer maxLease;
		public final boolean autoLease;
		
		public ProductData(ProductLicense license, Integer maxLease, boolean autoLease) {
			this.license = license;
			this.maxLease = maxLease;
			this.autoLease = autoLease;
		}
	}
	
	public static final class CacheCleanupJob implements Job {
		@Override
		public void execute(JobExecutionContext jec) throws JobExecutionException {
			LicenseManager o = (LicenseManager)jec.getMergedJobDataMap().get("this");
			o.cleanupLicenseCache();
		}
	}
	
	public static final class CheckJob implements Job {
		@Override
		public void execute(JobExecutionContext jec) throws JobExecutionException {
			LicenseManager o = (LicenseManager)jec.getMergedJobDataMap().get("this");
			o.updateLeaseTrackingInfo();
			o.checkOnlineAvailability();
			o.enforceConstraints();
		}
	}
}
