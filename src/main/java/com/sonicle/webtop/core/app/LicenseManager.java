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
import com.sonicle.commons.Base58;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.l4j.ProductLicense.LicenseInfo;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.bean.StringMap;
import com.sonicle.webtop.core.app.sdk.WTLicenseActivationException;
import com.sonicle.webtop.core.app.sdk.WTLicenseException;
import com.sonicle.webtop.core.app.sdk.WTLicenseMismatchException;
import com.sonicle.webtop.core.app.sdk.WTLicenseValidationException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.app.util.ProductUtils;
import com.sonicle.webtop.core.bol.OLicense;
import com.sonicle.webtop.core.bol.OLicenseLease;
import com.sonicle.webtop.core.bol.VLicense;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.dal.LicenseDAO;
import com.sonicle.webtop.core.dal.LicenseLeaseDAO;
import com.sonicle.webtop.core.model.License;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.ServiceLicenseLease;
import com.sonicle.webtop.core.model.ServiceLicenseLease.LeaseOrigin;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
public class LicenseManager {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(LicenseManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method.This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @param scheduler
	 * @return The instance.
	 */
	public static synchronized LicenseManager initialize(WebTopApp wta, Scheduler scheduler) {
		if (initialized) throw new RuntimeException("Initialization already done");
		LicenseManager licm = new LicenseManager(wta, scheduler);
		initialized = true;
		LOGGER.info("Initialized");
		return licm;
	}
	
	private WebTopApp wta = null;
	private Scheduler scheduler = null;
	private final JobKey dailyCleanupJobKey;
	private final JobKey dailyCheckJobKey;
	private final Map<String, ProductData> productLicenseCache = new ConcurrentHashMap<>();
	private final Map<String, Integer> licenseLeaseCache = new ConcurrentHashMap<>();
	private final Map<String, Integer> lastTrackedLeaseValue = new ConcurrentHashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private LicenseManager(WebTopApp wta, Scheduler scheduler) {
		this.wta = wta;
		this.scheduler = scheduler;
		
		this.dailyCleanupJobKey = JobKey.jobKey(CacheCleanupJob.class.getCanonicalName(), "webtop");
		try {
			LOGGER.debug("Scheduling daily cleanup job...");
			JobDetail jobDetail = JobBuilder.newJob(CacheCleanupJob.class)
					.withIdentity(dailyCleanupJobKey)
					.build();
			jobDetail.getJobDataMap().put("this", this);
			scheduler.scheduleJob(jobDetail, TriggerBuilder.newTrigger()
					.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 0))
					.build()
			);
			
		} catch (SchedulerException ex) {
			throw new WTRuntimeException(ex, "Unable to schedule CacheCleanupJob");
		}
		
		this.dailyCheckJobKey = JobKey.jobKey(CheckJob.class.getCanonicalName(), "webtop");
		try {
			int hour = RandomUtils.nextInt(0, 24);
			int minute = RandomUtils.nextInt(0, 60);
			if (hour == 0 && minute == 0) minute++;
			LOGGER.debug("Scheduling daily check job... [{}:{}]", hour, minute);
			JobDetail jobDetail = JobBuilder.newJob(CacheCleanupJob.class)
					.withIdentity(dailyCheckJobKey)
					.build();
			jobDetail.getJobDataMap().put("this", this);
			scheduler.scheduleJob(jobDetail, TriggerBuilder.newTrigger()
					.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(hour, minute))
					.build()
			);
			
		} catch (SchedulerException ex) {
			throw new WTRuntimeException(ex, "Unable to schedule CheckJob");
		}
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		try {
			scheduler.deleteJobs(Arrays.asList(dailyCleanupJobKey, dailyCheckJobKey));
		} catch (SchedulerException ex) {
			LOGGER.warn("Unable to delete jobs", ex);
		}
		productLicenseCache.clear();
		licenseLeaseCache.clear();
		lastTrackedLeaseValue.clear();
	}
	
	public void cleanupLicenseCache() {
		productLicenseCache.clear();
		licenseLeaseCache.clear();
	}
	
	public ProductLicense getProductLicense(final BaseServiceProduct product) {
		return getProductLicenseData(product).license;
	}
	
	public ProductData getProductLicenseData(final BaseServiceProduct product) {
		Check.notNull(product, "product");
		
		String key = productLicenseCacheKey(product.getInternetName(), product.SERVICE_ID, product.getProductCode());
		ProductData data = productLicenseCache.computeIfAbsent(key, k -> {
			LicenseDAO licDao = LicenseDAO.getInstance();
			LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
			Connection con = null;
			
			try {
				if (WebTopManager.INTERNETNAME_LOCAL.equals(product.getInternetName())) return new ProductData(null, false, null);
				String domainId = findDomainId(product.getInternetName());
				if (domainId == null) throw new WTException("Unable to lookup domainId for '{}'", product.getInternetName());
				
				con = wta.getConnectionManager().getConnection();
				OLicense olic = licDao.select(con, domainId, product.getProductId().getServiceId(), product.getProductId().getProductCode());
				if (olic != null) {
					ProductLicense plicNew = new ProductLicense(product);
					plicNew.setLicenseString(olic.getString());
					plicNew.setActivatedLicenseString(olic.getActivatedString());
					
					Set<String> leasedUsers = null;
					LicenseInfo li = plicNew.validate(true);
					LicenseInfo ali = plicNew.validate(false);
					//if (li.getLicenseID() != ali.getLicenseID()) throw new WTException("License ID mismatch [{} != {}]", li.getLicenseID(), ali.getLicenseID());
					boolean match = li.getLicenseID() == ali.getLicenseID();
					if (match && li.isValid() && ali.isValid() && ali.isActivationCompleted() && li.getQuantity() != null) {
						leasedUsers = lleaDao.selectUsersByDomainServiceProduct(con, domainId, product.getProductId().getServiceId(), product.getProductId().getProductCode(), li.getQuantity());
					}
					return new ProductData(plicNew, olic.getAutoLease(), leasedUsers);
					
				} else {
					if (LOGGER.isTraceEnabled()) LOGGER.trace("License missing, returning dummy! [{}]", key);
					return new ProductData(null, false, null);
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
		LicenseInfo ali = plic.validate(false);
		if (!li.isValid()) return false; // Reject if license is not in status VALID
		if (!ali.isValid()) return false; // Reject if activated license is not in status VALID
		if (!ali.isActivationCompleted()) return false; // Reject if activated license is not in status ACTIVATED
		if (li.getLicenseID() != ali.getLicenseID()) return false; // Reject if license IDs mismatch
		
		return true;
	}
	
	public int checkLicenseLease(final BaseServiceProduct product, final String userId) {
		Check.notNull(product, "product");
		Check.notNull(product, "userId");
		
		String key = licenseLeaseCacheKey(product.getInternetName(), product.SERVICE_ID, product.getProductCode(), userId);
		Integer result = licenseLeaseCache.computeIfAbsent(key, k -> {
			ProductData pData = getProductLicenseData(product);
			if (pData.license == null) return 0;
			
			// Do license checks
			LicenseInfo li = pData.license.validate(true);
			LicenseInfo ali = pData.license.validate(false);
			if (!li.isValid()) return -1; // Reject if license is not in status VALID
			if (!ali.isValid()) return -1; // Reject if activated license is not in status VALID
			if (!ali.isActivationCompleted()) return -1; // Reject if activated license is not in status ACTIVATED
			if (li.getLicenseID() != ali.getLicenseID()) return -1; // Reject if license IDs mismatch
			
			// Do lease checks
			if (li.getQuantity() == null) return 1; // Allow if quantity is unbounded
			if (!pData.leasedUsers.contains(userId) && !pData.autoLease) return 0; // Reject if auto-lease is off and user does not have a lease
			
			try {
				String domainId = findDomainId(product.getInternetName());
				if (domainId == null) throw new WTException("Unable to lookup domainId for '{}'", product.getInternetName());
				
				return internalAssignLicenseLease(domainId, product.getProductId(), pData, Arrays.asList(userId), ServiceLicenseLease.LeaseOrigin.AUTO) ? 1 : 0;
				
			} catch(WTLicenseException ex) {
				// Do nothing... 
				if (LOGGER.isTraceEnabled()) LOGGER.trace("License unassignable [{}]", ex, key);
				return -2;
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
			if (!wta.isLatest()) return;
			con = wta.getConnectionManager().getConnection();
			Map<String, Integer> cfails = new HashMap<>();
			Map<String, List<String>> map = licDao.groupAllLicenses(con);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String domainId = entry.getKey();
				
				try {
					String internetName = findInternetName(domainId);
					for (String productId : entry.getValue()) {
						ProductId prodId = new ProductId(productId);
						
						try {
							ProductData pdata = findProductLicenseData(internetName, prodId);
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
	
	public List<ServiceLicense> listLicenses(String domainId) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			ArrayList<ServiceLicense> items = new ArrayList<>();
			for (VLicense vlic : licDao.viewByDomain(con, domainId)) {
				List<OLicenseLease> oleases = lleaDao.selectByDomainServiceProduct(con, domainId, vlic.getServiceId(), vlic.getProductCode());
				items.add(AppManagerUtils.createServiceLicense(vlic, oleases));
			}
			return items;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ServiceLicense getLicense(String domainId, ProductId productId) throws WTException {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doServiceLicenseGet(con, domainId, productId, true);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addLicense(final License license, final boolean autoActivate) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			String internetName = findInternetName(license.getDomainId());
			BaseServiceProduct product = ProductUtils.getProduct(internetName, license.getProductId());
			if (product == null) throw new WTException("Unknown product [{}]", license.getProductId());
			
			ProductLicense tplProductLicense = new ProductLicense(product);
			tplProductLicense.setLicenseString(license.getLicenseString());
			LicenseInfo li = tplProductLicense.validate(true);
			if (li.isInvalid()) throw new WTLicenseValidationException(li);
			
			//ProductLicense tplProductLicense = findProductLicense(internetName, license.getProductId());
			//if (!tplProductLicense.getLicenseInfo().isValid()) throw new WTException("License provided for '{}' is not valid", license.getProductId().getProductCode());
			
			OLicense olic = AppManagerUtils.createOLicense(license);
			fillOLicenseWithDefaults(olic, li);
			
			con = wta.getConnectionManager().getConnection();
			if (licDao.insert(con, olic) == 1) {
				// Cleanup cached dummy ProductLicense
				forgetProductLicense(internetName, license.getProductId());
			}
			if (autoActivate) activateLicense(product, null);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void changeLicense(final String domainId, final ProductId productId, final String newString, final String activatedString, final boolean force) throws WTException {
		String internetName = findInternetName(domainId);
		ProductLicense tplProductLicense = findProductLicense(internetName, productId);
		
		boolean ret = internalChangeLicense(domainId, productId, tplProductLicense, newString, activatedString, force);
		if (ret) forgetProductLicense(internetName, productId);
	}
	
	public void changeLicense(final BaseServiceProduct product, final String newString, final String activatedString, final boolean force) throws WTException {
		String domainId = findDomainId(product.getInternetName());
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		boolean ret = internalChangeLicense(domainId, product.getProductId(), tplProductLicense, newString, activatedString, force);
		if (ret) forgetProductLicense(product.getInternetName(), product.getProductId());
	}
	
	public void modifyLicense(final BaseServiceProduct product, final String modificationKey, final String modifiedLicenseString) throws WTException {
		//TODO: not supported yet! Verify implementation!
		String domainId = findDomainId(product.getInternetName());
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		boolean ret = internalModifyLicense(domainId, product.getProductId(), tplProductLicense, modificationKey, modifiedLicenseString);
		if (ret) forgetProductLicense(product.getInternetName(), product.getProductId());
	}
	
	public void modifyLicense(final String domainId, final ProductId productId, final String modificationKey, final String modifiedLicenseString) throws WTException {
		//TODO: not supported yet! Verify implementation!
		String internetName = findInternetName(domainId);
		ProductLicense tplProductLicense = findProductLicense(internetName, productId);
		
		boolean ret = internalModifyLicense(domainId, productId, tplProductLicense, modificationKey, modifiedLicenseString);
		if (ret) forgetProductLicense(internetName, productId);
	}
	
	public void updateLicenseAutoLease(final String domainId, final ProductId productId, final boolean autoLease) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			String internetName = findInternetName(domainId);
			con = wta.getConnectionManager().getConnection();
			licDao.updateAutoLease(con, domainId, productId.getServiceId(), productId.getProductCode(), autoLease);
			forgetProductLicense(internetName, productId);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteLicense(final String domainId, final ProductId productId, final boolean force) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			String internetName = findInternetName(domainId);
			ProductLicense tplProductLicense = findProductLicense(internetName, productId);
			LicenseInfo li = tplProductLicense.validate(true);
			if (li.isInvalid()) throw new WTLicenseValidationException(li);
			if (!force) {
				li = tplProductLicense.validate(false);
				if (li.isActivationCompleted()) throw new WTException("License is activated, deactivate it before proceed.");
			}
			
			con = wta.getConnectionManager().getConnection();
			licDao.delete(con, domainId, productId.getServiceId(), productId.getProductCode());
			forgetProductLicense(internetName, productId);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void activateLicense(final BaseServiceProduct product, final String activatedString) throws WTException {
		String domainId = findDomainId(product.getInternetName());
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		boolean ret = internalActivateLicense(domainId, product.getProductId(), tplProductLicense, activatedString);
		if (ret) forgetProductLicense(product.getInternetName(), product.getProductId());
	}
	
	public void activateLicense(final String domainId, final ProductId productId, final String activatedString) throws WTException {
		String internetName = findInternetName(domainId);
		ProductLicense tplProductLicense = findProductLicense(internetName, productId);
		
		boolean ret = internalActivateLicense(domainId, productId, tplProductLicense, activatedString);
		if (ret) forgetProductLicense(internetName, productId);
	}
	
	public void deactivateLicense(final BaseServiceProduct product, final boolean offline) throws WTException {
		String domainId = findDomainId(product.getInternetName());
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		boolean ret = internalDeactivateLicense(domainId, product.getProductId(), tplProductLicense, offline);
		if (ret) forgetProductLicense(product.getInternetName(), product.getProductId());
	}
	
	public void deactivateLicense(final String domainId, final ProductId productId, final boolean offline) throws WTException {
		String internetName = findInternetName(domainId);
		ProductLicense tplProductLicense = findProductLicense(internetName, productId);
		
		boolean ret = internalDeactivateLicense(domainId, productId, tplProductLicense, offline);
		if (ret) forgetProductLicense(internetName, productId);
	}
	
	public void assignLicenseLease(final BaseServiceProduct product, final Collection<String> userIds) throws WTException {
		String domainId = findDomainId(product.getInternetName());
		ProductData plData = getProductLicenseData(product);
		if (plData == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		boolean ret = internalAssignLicenseLease(domainId, product.getProductId(), plData, userIds, ServiceLicenseLease.LeaseOrigin.STATIC);
		if (ret) forgetLicenseLease(product.getInternetName(), product.getProductId(), userIds);
	}
	
	public void assignLicenseLease(final String domainId, final ProductId productId, final Collection<String> userIds) throws WTException {
		String internetName = findInternetName(domainId);
		ProductData plData = findProductLicenseData(internetName, productId);
		
		boolean ret = internalAssignLicenseLease(domainId, productId, plData, userIds, ServiceLicenseLease.LeaseOrigin.STATIC);
		if (ret) forgetLicenseLease(internetName, productId, userIds);
	}
	
	public void revokeLicenseLease(final BaseServiceProduct product, final Collection<String> userIds) throws WTException {
		String domainId = findDomainId(product.getInternetName());
		ProductData plData = getProductLicenseData(product);
		if (plData == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		internalRevokeLicenseLease(domainId, product.getProductId(), plData, userIds);
		forgetLicenseLease(product.getInternetName(), product.getProductId(), userIds);
	}
	
	public void revokeLicenseLease(final String domainId, final ProductId productId, final Collection<String> userIds) throws WTException {
		String internetName = findInternetName(domainId);
		ProductData plData = findProductLicenseData(internetName, productId);
		
		//LOGGER.debug("Revoking license '{}' for user '{}'...", productId, new UserProfileId(domainId, userId));
		internalRevokeLicenseLease(domainId, productId, plData, userIds);
		forgetLicenseLease(internetName, productId, userIds);
	}
	
	public void revokeLicenseLease(final UserProfileId profileId) throws WTException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		String internetName = findInternetName(profileId.getDomainId());
		LOGGER.debug("Revoking all licenses for profile '{}'...", profileId);
		List<OLicenseLease> leases = null;
		try {
			con = wta.getConnectionManager().getConnection();
			leases = lleaDao.selectByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			for (OLicenseLease lease : leases) {
				ProductId productId = ProductId.build(lease.getServiceId(), lease.getProductCode());
				ProductLicense tplProductLicense = findProductLicense(internetName, productId);
				
				LOGGER.debug("[{}] Revoking license '{}'...", profileId, productId);
				//doRevokeLicenseLease(con, profileId.getDomainId(), productId, tplProductLicense, profileId.getUserId(), lease.getActivationString());
				forgetLicenseLease(internetName, productId, Arrays.asList(profileId.getUserId()));
			}
		
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ProductLicense findProductLicense(String internetName, ProductId productId) throws WTException {
		BaseServiceProduct product = ProductUtils.getProduct(internetName, productId);
		if (product == null) throw new WTException("Unknown product [{}]", productId);
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", productId);
		return tplProductLicense;
	}
	
	private ProductData findProductLicenseData(String internetName, ProductId productId) throws WTException {
		BaseServiceProduct product = ProductUtils.getProduct(internetName, productId);
		if (product == null) throw new WTException("Unknown product [{}]", productId);
		ProductData plData = getProductLicenseData(product);
		if (plData == null) throw new WTException("Unknown product [{}]", productId);
		return plData;
	}
	
	private LicenseInfo checkCompat(LicenseInfo base, LicenseInfo activated) throws WTLicenseMismatchException {
		if (base.getLicenseID() != activated.getLicenseID()) {
			throw new WTLicenseMismatchException(base.getLicenseID(), activated.getLicenseID());
		}
		return activated;
	}
	
	private boolean internalChangeLicense(final String domainId, final ProductId productId, final ProductLicense plicOrig, final String newString, final String activatedString, final boolean force) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("[{}] Changing license...", productId);
			LicenseInfo li = plicOrig.validate(true);
			LOGGER.debug("[{}] {} (old) -> Validate Be. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (li.isInvalid()) throw new WTLicenseValidationException(li);
			if (!force) {
				li = plicOrig.validate(false);
				LOGGER.debug("[{}] {} (old) -> Validate Af. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
				if (li.isActivationCompleted()) throw new WTException("License is activated, deactivate it before proceed.");
			}
			
			ProductLicense plic = Cloner.standard().deepClone(plicOrig);
			plic.setLicenseString(newString);
			li = plic.validate(true);
			LOGGER.debug("[{}] {} (new) -> Validate Be. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			
			LocalDate expDate = li.getExpirationDate();
			Integer quantity = li.getQuantity();
			
			if (StringUtils.isBlank(activatedString)) {
				LOGGER.debug("Activation string not provided. Performing automatic activation...");
				li = plic.autoActivate();
			} else {
				LOGGER.debug("Activated string provided. Performing manual activation...");
				li = checkCompat(li, plic.manualActivate(activatedString));
			}
			LOGGER.debug("[{}] {} (new) -> Validate Ac. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (!li.isActivationCompleted()) throw new WTLicenseActivationException(li);
			
			con = wta.getConnectionManager().getConnection();
			boolean ret = licDao.replaceLicense(con, domainId, productId.getServiceId(), productId.getProductCode(), plic.getLicenseString(), expDate, quantity, plic.getActivatedLicenseString(), DateTimeUtils.now(), li.getHardwareID()) == 1;
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalModifyLicense(final String domainId, final ProductId productId, final ProductLicense tplProductLicense, final String modificationKey, final String activatedString) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("[{}] Modifying license...", productId);
			LicenseInfo li = tplProductLicense.validate(false);
			LOGGER.debug("[{}] {} (old) -> Validate Af. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			
			ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
			if (StringUtils.isBlank(activatedString)) {
				LOGGER.debug("Activation string not provided. Performing automatic modification...");
				li = plic.modifyLicense(modificationKey);
			} else {
				LOGGER.debug("Activated string provided. Performing manual modification...");
				li = checkCompat(li, plic.manualModify(activatedString));
			}
			LOGGER.debug("[{}] {} (new) -> Validate Mo. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (!li.isActivationCompleted()) throw new WTLicenseActivationException(li);
			
			con = wta.getConnectionManager().getConnection();
			boolean ret = licDao.updateActivation(con, domainId, productId.getServiceId(), productId.getProductCode(), plic.getActivatedLicenseString(), DateTimeUtils.now(), li.getHardwareID()) == 1;
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalActivateLicense(final String domainId, final ProductId productId, final ProductLicense tplProductLicense, final String activatedString) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			LOGGER.debug("Activating license for '{}'", productId);
			LicenseInfo li = tplProductLicense.validate(true);
			LOGGER.debug("[{}] {} -> Validate Be. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			
			ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
			if (StringUtils.isBlank(activatedString)) {
				LOGGER.debug("Activation string not provided. Performing automatic activation...");
				li = plic.autoActivate();
			} else {
				LOGGER.debug("Activated string provided. Performing manual activation...");
				li = checkCompat(li, plic.manualActivate(activatedString));
			}
			LOGGER.debug("[{}] {} -> Validate Ac. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (!li.isActivationCompleted()) throw new WTLicenseActivationException(li);
			
			con = wta.getConnectionManager().getConnection();
			boolean ret = licDao.updateActivation(con, domainId, productId.getServiceId(), productId.getProductCode(), plic.getActivatedLicenseString(), DateTimeUtils.now(), li.getHardwareID()) == 1;
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
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
				LOGGER.debug("Performing automatic deactivation...");
				li = plic.autoDeactivate();
				LOGGER.debug("[{}] {} -> Validate De. [{}, {}]", productId, li.getLicenseID(), li.getValidationStatus(), li.getActivationStatus());
				if (li.isActivationNotFound()) {
					// If activation is not found on server, simply erase activation
					// data for this license and only after throw the exception!
					afterUpdateThrow = new WTLicenseActivationException(li);
					
				} else if (ValidationStatus.MISMATCH_HARDWARE_ID.equals(li.getValidationStatus()) && li.isActivationCompleted()) {
					// If internetName is changed we can have this situation, we
					// can erase activation data locally for this license and 
					// only after throw the exception!
					afterUpdateThrow = new WTLicenseValidationException(li);
					
				} else if (!li.isDeactivationCompleted()) {
					throw new WTLicenseActivationException(li);
				}
				
			} else {
				LOGGER.debug("Performing manual deactivation...");
				//li = plic.manualDeactivate();
			}
			
			con = wta.getConnectionManager().getConnection();
			boolean ret = licDao.updateActivation(con, domainId, productId.getServiceId(), productId.getProductCode(), null, DateTimeUtils.now(), null) == 1;
			if (afterUpdateThrow != null) throw afterUpdateThrow;
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalAssignLicenseLease(final String domainId, final ProductId productId, final ProductData pData, final Collection<String> userIds, final LeaseOrigin origin) throws WTException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			LicenseInfo li = pData.license.validate(true);
			if (!li.isValid()) throw new WTLicenseValidationException(li);
			if (li.getQuantity() == null) throw new WTException("Lease attribution is not supported for '{}'", productId.getProductCode());
			
			con = wta.getConnectionManager().getConnection();
			if (userIds.size() == 1) {
				return lleaDao.insert(con, domainId, productId.getServiceId(), productId.getProductCode(), userIds.iterator().next(), DateTimeUtils.now(), origin) == 1;
			} else {
				return lleaDao.batchInsert(con, domainId, productId.getServiceId(), productId.getProductCode(), userIds, DateTimeUtils.now(), origin).length == userIds.size();
			}
			
		} catch(DAOIntegrityViolationException ex) {
			// We rely on IntegrityException in order to determine if record is already present on db table.
			if (userIds.size() == 1) {
				return true;
			} else {
				throw ExceptionUtils.wrapThrowable(ex);
			}
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalRevokeLicenseLease(final String domainId, final ProductId productId, final ProductData pData, final Collection<String> userIds) throws WTException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return lleaDao.delete(con, domainId, productId.getServiceId(), productId.getProductCode(), userIds) == userIds.size();
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void updateLeaseTrackingInfo() {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			if (!wta.isLatest()) return;
			con = wta.getConnectionManager().getConnection();
			Map<String, Integer> fails = new HashMap<>();
			Map<String, List<String>> map = licDao.groupAllLicenses(con);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String domainId = entry.getKey();
				
				try {
					String internetName = findInternetName(domainId);
					for (String productId : entry.getValue()) {
						ProductId prodId = new ProductId(productId);

						try {
							ProductData pdata = findProductLicenseData(internetName, prodId);
							if (pdata.license != null && pdata.leasedUsers != null) {
								final String leaseLTKey = internetName + "|" + prodId.toString();
								// Skip if value is not changed
								if (lastTrackedLeaseValue.getOrDefault(leaseLTKey, -1) == pdata.leasedUsers.size()) continue;
								// Skip if failure counts have reached threshold
								final String server = pdata.license.getLicenseServer();
								if (fails.getOrDefault(server, 0) >= 2) continue;
								
								int lease = pdata.leasedUsers.size();
								int ret = pdata.license.updateTrackingInfo(new StringMap().withValue("lease", String.valueOf(lease)), 5000);
								if (ret == -1) {
									int count = fails.getOrDefault(server, 0);
									fails.put(server, count+1);
								} else if (ret == 1) {
									lastTrackedLeaseValue.put(leaseLTKey, lease);
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
		} catch(Throwable t) {
			// Do nothing...
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ServiceLicense doServiceLicenseGet(Connection con, String domainId, ProductId product, boolean processLeases) throws DAOException, WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		
		OLicense olic = licDao.select(con, domainId, product.getServiceId(), product.getProductCode());
		if (olic == null) return null;
		
		ServiceLicense lic = AppManagerUtils.createServiceLicense(olic);
		if (processLeases) {
			List<OLicenseLease> oleases = lleaDao.selectByDomainServiceProduct(con, domainId, product.getServiceId(), product.getProductCode());
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
	
	private String findDomainId(String internetName) throws WTException {
		try {
			return wta.getWebTopManager().internetNameToDomainId(internetName);
		} catch(Throwable t) {
			throw new WTException("Unable to lookup domainId for '{}'", internetName);
		}
	}
	
	private String findInternetName(String domainId) throws WTException {
		try {
			return wta.getWebTopManager().domainIdToInternetName(domainId);
		} catch(Throwable t) {
			throw new WTException("Unable to lookup internetName for '{}'", domainId);
		}
	}
	
	private String productLicenseCacheKey(String internetName, String service, String productCode) {
		return internetName + "|" + service + "|" + productCode;
	}
	
	private String licenseLeaseCacheKey(String internetName, String service, String productCode, String user) {
		return internetName + "|" + service + "|" + productCode + "|" + user;
	}
	
	private String activationHardwareId(final String userId) {
		return Base58.encode(userId.getBytes());
	}
	
	private void forgetProductLicense(String internetName, ProductId product) {
		String key = productLicenseCacheKey(internetName, product.getServiceId(), product.getProductCode());
		productLicenseCache.remove(key);
		licenseLeaseCache.entrySet().removeIf(e -> StringUtils.startsWith(e.getKey(), key));
	}
	
	private void forgetLicenseLease(String internetName, ProductId product, Collection<String> users) {
		for (String user : users) {
			licenseLeaseCache.remove(licenseLeaseCacheKey(internetName, product.getServiceId(), product.getProductCode(), user));
		}
	}
	
	public static class ProductData {
		public final ProductLicense license;
		public final boolean autoLease;
		public final Set<String> leasedUsers;
		
		public ProductData(ProductLicense license, boolean autoLease, Set<String> leasedUsers) {
			this.license = license;
			this.autoLease = autoLease;
			this.leasedUsers = leasedUsers != null ? Collections.unmodifiableSet(leasedUsers) : null;
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
		}
	}
}
