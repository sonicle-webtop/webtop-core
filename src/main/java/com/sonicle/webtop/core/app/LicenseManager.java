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

import com.license4j.ActivationStatus;
import com.license4j.ValidationStatus;
import com.rits.cloning.Cloner;
import com.sonicle.commons.Base58;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.l4j.ProductLicense.LicenseInfo;
import com.sonicle.commons.web.json.CId;
import com.sonicle.webtop.core.app.sdk.WTLicenseActivationException;
import com.sonicle.webtop.core.app.sdk.WTLicenseException;
import com.sonicle.webtop.core.app.sdk.WTLicenseValidationException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.app.util.ProductUtils;
import com.sonicle.webtop.core.bol.OLicense;
import com.sonicle.webtop.core.bol.OLicenseLease;
import com.sonicle.webtop.core.bol.VLicense;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.LicenseDAO;
import com.sonicle.webtop.core.dal.LicenseLeaseDAO;
import com.sonicle.webtop.core.model.License;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
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
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized LicenseManager initialize(WebTopApp wta, Scheduler scheduler) {
		if (initialized) throw new RuntimeException("Initialization already done");
		LicenseManager licm = new LicenseManager(wta, scheduler);
		initialized = true;
		LOGGER.info("Initialized");
		return licm;
	}
	
	private static final String TI_USERSNO = "usersNo";
	private WebTopApp wta = null;
	private Scheduler scheduler = null;
	private final JobKey dailyCleanupJobKey;
	private final Map<String, ProductLicenseData> productLicenseCache = new ConcurrentHashMap<>();
	private final Map<String, Integer> licenseLeaseCache = new ConcurrentHashMap<>();
	private final KeyedReentrantLocks assignLeaseLocks = new KeyedReentrantLocks();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private LicenseManager(WebTopApp wta, Scheduler scheduler) {
		this.wta = wta;
		this.scheduler = scheduler;
		dailyCleanupJobKey = JobKey.jobKey(CacheCleanupJob.class.getCanonicalName(), "webtop");
		try {
			JobDetail jobDetail = JobBuilder.newJob(CacheCleanupJob.class)
					.withIdentity(dailyCleanupJobKey)
					.build();
			jobDetail.getJobDataMap().put("this", this);
			Trigger trigger = TriggerBuilder.newTrigger()
					.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 0))
					.build();
			scheduler.scheduleJob(jobDetail, trigger);
			
		} catch (SchedulerException ex) {
			throw new WTRuntimeException(ex, "Unable to schedule CacheCleanupJob");
		}
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		try {
			scheduler.deleteJob(dailyCleanupJobKey);
		} catch (SchedulerException ex) {
			LOGGER.warn("Unable to delete CacheCleanupJob", ex);
		}
		productLicenseCache.clear();
		licenseLeaseCache.clear();
	}
	
	private void cleanupLicenseCache() {
		productLicenseCache.clear();
		licenseLeaseCache.clear();
	}
	
	public ProductLicense getProductLicense(final BaseServiceProduct product) {
		return getProductLicenseData(product).license;
	}
	
	public ProductLicenseData getProductLicenseData(final BaseServiceProduct product) {
		Check.notNull(product, "product");
		
		String key = productLicenseCacheKey(product.getInternetName(), product.SERVICE_ID, product.getProductCode());
		ProductLicenseData data = productLicenseCache.computeIfAbsent(key, k -> {
			Connection con = null;
			try {
				if (WebTopManager.INTERNETNAME_LOCAL.equals(product.getInternetName())) return new ProductLicenseData(null, false);
				String domainId = findDomainId(product.getInternetName());
				if (domainId == null) throw new WTException("Unable to lookup domainId for '{}'", product.getInternetName());

				con = wta.getConnectionManager().getConnection();
				ServiceLicense slic = doServiceLicenseGet(con, domainId, product.getProductId(), false);
				if (slic != null) {
					ProductLicense plicNew = new ProductLicense(product);
					plicNew.setLicenseString(slic.getString());
					plicNew.validate();
					return new ProductLicenseData(plicNew, slic.getAutoLease());

				} else {
					LOGGER.debug("License is missing, creating a dummy one! [{}]", key);
					return new ProductLicenseData(null, false);
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
	
	
	
	
	
	/*
	public ProductLicense getProductLicense(final BaseServiceProduct product) {
		Check.notNull(product, "product");
		
		String key = productLicenseCacheKey(product.getInternetName(), product.SERVICE_ID, product.getProductCode());
		ProductLicenseData data = productLicenseCache.computeIfAbsent(key, k -> computeProductLicenseData(product, k));
		return data.license;
	}
	
	public ProductLicense getProductLicense(final BaseServiceProduct product) {
		Check.notNull(product, "product");
		
		String key = productLicenseCacheKey(product.getInternetName(), product.SERVICE_ID, product.getProductCode());
		ProductLicense plic = productLicenseCache.computeIfAbsent(key, value -> {
			Connection con = null;
			try {
				String domainId = findDomainId(product.getInternetName());
				if (domainId == null) throw new WTException("Unable to lookup domainId for '{}'", product.getInternetName());
				
				con = wta.getConnectionManager().getConnection();
				ServiceLicense2 slic = doServiceLicenseGet(con, domainId, product.getProductId(), false);
				if (slic != null) {
					ProductLicense plicNew = new ProductLicense(product);
					plicNew.setLicenseString(slic.getString());
					plicNew.validate();
					return plicNew;
				} else {
					LOGGER.debug("License is missing, creating a dummy one! [{}]", key);
					return new DummyProductLicense();
				}
				
			} catch(Throwable t) {
				LOGGER.error("Error retrieving registered license [{}]", key, t);
			} finally {
				DbUtils.closeQuietly(con);
			}
			return null;
		});
		
		return (plic instanceof DummyProductLicense) ? null : plic;
	}
	*/
	
	public int checkLicenseLease(final BaseServiceProduct product, final String userId) {
		Check.notNull(product, "product");
		Check.notNull(product, "userId");
		
		String key = licenseLeaseCacheKey(product.getInternetName(), product.SERVICE_ID, product.getProductCode(), userId);
		Integer result = licenseLeaseCache.computeIfAbsent(key, k -> {
			ProductLicense tplProductLicense = getProductLicense(product);
			//if (tplProductLicense == null) return false;
			//if (!tplProductLicense.getLicenseInfo().isValid()) return false;
			//if (!tplProductLicense.getLicenseInfo().isActivationRequired()) return true;
			if (tplProductLicense == null) return 0;
			if (!tplProductLicense.getLicenseInfo().isValid()) return -1;
			if (!tplProductLicense.getLicenseInfo().isActivationRequired()) return 1;
			
			LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
			Connection con = null;
			
			try {
				String domainId = findDomainId(product.getInternetName());
				if (domainId == null) throw new WTException("Unable to lookup domainId for '{}'", product.getInternetName());
				
				ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
				
				con = wta.getConnectionManager().getConnection();
				String aString = lleaDao.selectActivationStringByDomainServiceProductUser(con, domainId, product.SERVICE_ID, product.getProductCode(), userId);
				if (StringUtils.isBlank(aString)) {
					String lockKey = assignLockKey(domainId, product.SERVICE_ID, product.getProductCode(), userId);
					try (KeyedReentrantLocks.KeyedLock lock = assignLeaseLocks.acquire(lockKey)) {
						internalAssignLicenseLease(domainId, product.getProductId(), tplProductLicense, userId, null);
					}
					
				} else {
					plic.setLicenseActivationString(aString);
					plic.validate();
					if (!plic.getLicenseInfo().isValid()) throw new WTLicenseValidationException(plic.getLicenseInfo(), ValidationStatus.LICENSE_VALID);
					if (plic.getLicenseInfo().isActivationRequired() && !plic.getLicenseInfo().isActivated()) throw new WTLicenseActivationException(plic.getLicenseInfo(), ActivationStatus.ACTIVATION_COMPLETED);
				}
				return 1;
				//return true;
			
			} catch(WTLicenseException t) {
				/* Do nothing... */
				LOGGER.trace("License not activated [{}]", key, t);
				return -2;
			} catch(Throwable t) {
				LOGGER.error("Error retrieving registered license [{}]", key, t);
				return null;
			}  finally {
				DbUtils.closeQuietly(con);
			}
			//return false;
		});
		//return result == null ? false : result;
		return result == null ? 0 : result;
	}
	
	private ProductLicense checkLicense(License license) throws WTException {
		return checkLicense(license.getDomainId(), license.getProductId(), license.getString());
	}
	
	private ProductLicense checkLicense(String domainId, ProductId productId, String licenseString) throws WTException {
		String internetName = findInternetName(domainId);
		ProductLicense prodLic = ProductUtils.getProductLicense(internetName, productId, licenseString);
		if (prodLic == null) throw new WTException("Unknown product [{}]", productId);
		if (!prodLic.getLicenseInfo().isValid()) throw new WTException("License provided for '{}' is not valid", productId.getProductCode());
		return prodLic;
	}
	
	public List<ServiceLicense> listLicenses(String domainId) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			ArrayList<ServiceLicense> items = new ArrayList<>();
			for (VLicense vlic : licDao.viewByDomain(con, domainId)) {
				Set<String> users = new LinkedHashSet(new CId(vlic.getUserIds()).getTokens());
				items.add(AppManagerUtils.createServiceLicense(vlic, users));
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
	
	public void addLicense(License license) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			String internetName = findInternetName(license.getDomainId());
			BaseServiceProduct product = ProductUtils.getProduct(internetName, license.getProductId());
			if (product == null) throw new WTException("Unknown product [{}]", license.getProductId());
			
			ProductLicense tplProductLicense = new ProductLicense(product);
			tplProductLicense.setLicenseString(license.getString());
			tplProductLicense.validate();
			if (!tplProductLicense.getLicenseInfo().isValid()) throw new WTLicenseValidationException(tplProductLicense.getLicenseInfo(), ValidationStatus.LICENSE_VALID);
			
			//ProductLicense tplProductLicense = findProductLicense(internetName, license.getProductId());
			//if (!tplProductLicense.getLicenseInfo().isValid()) throw new WTException("License provided for '{}' is not valid", license.getProductId().getProductCode());
			
			OLicense olic = AppManagerUtils.createOLicense(license);
			fillOLicenseWithDefaults(olic, tplProductLicense.getLicenseInfo());
			
			// Lookup online data taken from tracking-info
			HashMap<String, String> imap = retrieveUpdatedTrackingInfo(tplProductLicense);
			if (imap != null) {
				Integer usersNo = LangUtils.value(imap.get(TI_USERSNO), (Integer)null);
				if (usersNo != null) olic.setUsersNo(usersNo);
			}
			
			con = wta.getConnectionManager().getConnection();
			if (licDao.insert(con, olic) == 1) {
				// Cleanup cached dummy ProductLicense
				forgetProductLicense(internetName, license.getProductId());
			}
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateLicenseAutoLease(String domainId, ProductId productId, boolean autoLease) throws WTException {
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
	
	public void updateLicenseOnlineInfo(String domainId, ProductId productId) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			String internetName = findInternetName(domainId);
			ProductLicense tplProductLicense = findProductLicense(internetName, productId);
			
			Integer usersNo = tplProductLicense.getLicenseInfo().getUsersNo();
			
			// Lookup online data taken from tracking-info
			HashMap<String, String> imap = retrieveUpdatedTrackingInfo(tplProductLicense);
			if (imap != null) {
				usersNo = LangUtils.value(imap.get(TI_USERSNO), (Integer)null);
			}
			
			con = wta.getConnectionManager().getConnection();
			licDao.updateOnlineData(con, domainId, productId.getServiceId(), productId.getProductCode(), usersNo);
			forgetProductLicense(internetName, productId);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteLicense(String domainId, ProductId productId) throws WTException {
		deleteLicense(domainId, productId, false);
	}
	
	public void deleteLicense(String domainId, ProductId productId, boolean force) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			String internetName = findInternetName(domainId);
			ProductLicense tplProductLicense = findProductLicense(internetName, productId);
			
			con = wta.getConnectionManager().getConnection();
			Map<String, String> leasedUsers = lleaDao.selectByDomainServiceProduct(con, domainId, productId.getServiceId(), productId.getProductCode());
			for (Map.Entry<String, String> entry : leasedUsers.entrySet()) {
				try {
					doRevokeLicenseLease(con, domainId, productId, tplProductLicense, entry.getKey(), entry.getValue());
				} catch (WTLicenseException ex) {
					if (!force) throw ex;
				}
			}
			if (force) {
				// In force mode make sure to cleanup all!
				lleaDao.deleteByDomainServiceProduct(con, domainId, productId.getServiceId(), productId.getProductCode());
			}
			licDao.delete(con, domainId, productId.getServiceId(), productId.getProductCode());
			forgetProductLicense(internetName, productId);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private HashMap<String, String> retrieveUpdatedTrackingInfo(ProductLicense productLicense) {
		HashMap<String, String> map = new HashMap<>();
		map.put(TI_USERSNO, null);
		LOGGER.debug("Querying tracking-info...");
		int ret = productLicense.queryTrackingInfo(map, 5000);
		if (ret == 1) {
			LOGGER.debug("Tracking-info retrieved succesfully");
			return map;
		} else {
			LOGGER.debug("Unable to retrieve tracking-info: reason {}", ret);
			return null;
		}
	}
	
	private ProductLicense findProductLicense(String internetName, ProductId productId) throws WTException {
		BaseServiceProduct product = ProductUtils.getProduct(internetName, productId);
		if (product == null) throw new WTException("Unknown product [{}]", productId);
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", productId);
		return tplProductLicense;
	}
	
	
	public void assignLicenseLease(final BaseServiceProduct product, final String userId, final String activationString) throws WTException {
		String domainId = findDomainId(product.getInternetName());
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		boolean ret = internalAssignLicenseLease(domainId, product.getProductId(), tplProductLicense, userId, activationString);
		if (ret) forgetLicenseLease(product.getInternetName(), product.getProductId(), userId);
	}
	
	public void assignLicenseLease(final String domainId, final ProductId productId, final String userId, final String activationString) throws WTException {
		String internetName = findInternetName(domainId);
		ProductLicense tplProductLicense = findProductLicense(internetName, productId);
		
		boolean ret = internalAssignLicenseLease(domainId, productId, tplProductLicense, userId, activationString);
		if (ret) forgetLicenseLease(internetName, productId, userId);
	}
	
	public void revokeLicenseLease(final BaseServiceProduct product, final String userId) throws WTException {
		String domainId = findDomainId(product.getInternetName());
		ProductLicense tplProductLicense = getProductLicense(product);
		if (tplProductLicense == null) throw new WTException("Unknown product [{}]", product.getProductId());
		
		internalRevokeLicenseLease(domainId, product.getProductId(), tplProductLicense, userId);
		forgetLicenseLease(product.getInternetName(), product.getProductId(), userId);
	}
	
	public void revokeLicenseLease(final String domainId, final ProductId productId, final String userId) throws WTException {
		String internetName = findInternetName(domainId);
		ProductLicense tplProductLicense = findProductLicense(internetName, productId);
		
		LOGGER.debug("Revoking license '{}' for user '{}'...", productId, new UserProfileId(domainId, userId));
		internalRevokeLicenseLease(domainId, productId, tplProductLicense, userId);
		forgetLicenseLease(internetName, productId, userId);
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
				doRevokeLicenseLease(con, profileId.getDomainId(), productId, tplProductLicense, profileId.getUserId(), lease.getActivationString());
				forgetLicenseLease(internetName, productId, profileId.getUserId());
			}
		
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalAssignLicenseLease(final String domainId, final ProductId productId, final ProductLicense tplProductLicense, final String userId, final String activationString) throws WTException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		// If activationString = null, auto-activation will be performed!
		
		try {
			if (!tplProductLicense.getLicenseInfo().isValid()) throw new WTLicenseValidationException(tplProductLicense.getLicenseInfo(), ValidationStatus.LICENSE_VALID);
			ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
			plic.setActivationCustomHardwareId(activationHardwareId(userId));
			if (StringUtils.isBlank(activationString)) {
				LOGGER.debug("Activation string not provided. Trying automatic activation...");
				plic.autoActivate();
			} else {
				LOGGER.debug("Activation string provided. Performing manual activation...");
				plic.manualActivate(activationString);
			}
			if (!plic.getLicenseInfo().isActivated()) throw new WTLicenseActivationException(plic.getLicenseInfo(), ActivationStatus.ACTIVATION_COMPLETED);
			
			OLicenseLease ollea = new OLicenseLease();
			ollea.setDomainId(domainId);
			ollea.setServiceId(productId.getServiceId());
			ollea.setProductCode(productId.getProductCode());
			ollea.setUserId(userId);
			ollea.setActivationString(plic.getLicenseActivationString());
			
			con = wta.getConnectionManager().getConnection();
			boolean ret = lleaDao.insert(con, ollea) == 1;
			return ret;
		
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void internalRevokeLicenseLease(String domainId, ProductId productId, ProductLicense tplProductLicense, String userId) throws WTException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			if (!tplProductLicense.getLicenseInfo().isValid()) throw new WTLicenseValidationException(tplProductLicense.getLicenseInfo(), ValidationStatus.LICENSE_VALID);
			
			con = wta.getConnectionManager().getConnection();
			String aString = lleaDao.selectActivationStringByDomainServiceProductUser(con, domainId, productId.getServiceId(), productId.getProductCode(), userId);
			if (aString == null) throw new WTException("Activation string missing [{}, {}]", productId, userId);
			doRevokeLicenseLease(con, domainId, productId, tplProductLicense, userId, aString);
		
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void doRevokeLicenseLease(Connection con, String domainId, ProductId productId, ProductLicense tplProductLicense, String userId, String activationString) throws WTException, IOException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		
		ProductLicense plic = Cloner.standard().deepClone(tplProductLicense);
		plic.setLicenseActivationString(activationString);
		plic.setActivationCustomHardwareId(activationHardwareId(userId));
		plic.autoDeactivate();
		
		ActivationStatus status = plic.getLicenseInfo().getActivationStatus();
		if (ActivationStatus.DEACTIVATION_COMPLETED == status) {
			lleaDao.delete(con, domainId, productId.getServiceId(), productId.getProductCode(), userId);
		} else {
			throw new WTLicenseActivationException(plic.getLicenseInfo(), ActivationStatus.DEACTIVATION_COMPLETED);
		}
	}
	
	private ServiceLicense doServiceLicenseGet(Connection con, String domainId, ProductId product, boolean processLeases) throws DAOException, WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		
		OLicense olic = licDao.select(con, domainId, product.getServiceId(), product.getProductCode());
		if (olic == null) return null;
		
		ServiceLicense lic = AppManagerUtils.createServiceLicense(olic);
		if (processLeases) {
			Set<String> userIds = lleaDao.selectUsersByDomainServiceProduct(con, domainId, product.getServiceId(), product.getProductCode());
			lic.setLeasedUsers(userIds);
		}
		return lic;
	}
	
	private <T extends OLicense> T fillOLicenseWithDefaults(T tgt, LicenseInfo licInfo) {
		if ((tgt != null)) {
			tgt.setExpirationDate(licInfo.getExpirationDate());
			tgt.setUsersNo(licInfo.getUsersNo());
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
	
	private String assignLockKey(String domain, String service, String productCode, String user) {
		return domain + "|" + service + "|" + productCode + "|" + user;
	}
	
	private String activationHardwareId(final String userId) {
		return Base58.encode(userId.getBytes());
	}
	
	private void forgetProductLicense(String internetName, ProductId product) {
		String key = productLicenseCacheKey(internetName, product.getServiceId(), product.getProductCode());
		productLicenseCache.remove(key);
		licenseLeaseCache.entrySet().removeIf(e -> StringUtils.startsWith(e.getKey(), key));
	}
	
	private void forgetLicenseLease(String internetName, ProductId product, String user) {
		licenseLeaseCache.remove(licenseLeaseCacheKey(internetName, product.getServiceId(), product.getProductCode(), user));
	}
	
	public static class ProductLicenseData {
		public final ProductLicense license;
		public final boolean autoLease;
		
		public ProductLicenseData(ProductLicense license, boolean autoLease) {
			this.license = license;
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
}
