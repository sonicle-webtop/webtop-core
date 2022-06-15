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
package com.sonicle.webtop.core.app;
import com.sonicle.commons.ClassUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.webtop.core.app.DataSourcesConfig.HikariConfigMap;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.sdk.interfaces.IConnectionProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.StampedLock;
import javax.sql.DataSource;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ConnectionManager implements IConnectionProvider {
	private static final Logger logger = WT.getLogger(ConnectionManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized ConnectionManager initialize(WebTopApp wta) {
		if (initialized) throw new RuntimeException("Initialization already done");
		ConnectionManager conm = new ConnectionManager(wta);
		initialized = true;
		logger.info("Initialized");
		return conm;
	}
	
	public static final String CONFIG_NAME = "data-sources.xml";
	public static final String DEFAULT_CONFIG_RESOURCE_PATH = "/META-INF/" + CONFIG_NAME;
	public static final String DEFAULT_DATASOURCE = "default";
	private boolean shuttingDown = false;
	private WebTopApp wta = null;
	private DataSourcesConfig config = null;
	private final StampedLock lock = new StampedLock();
	private final HashMap<String, HikariDataSource> pools = new HashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private ConnectionManager(WebTopApp wta) {
		this.wta = wta;
		init();
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		long stamp = lock.writeLock();
		try {
			shuttingDown = true;
			for (Map.Entry<String, HikariDataSource> entry : pools.entrySet()) {
				try {
					entry.getValue().close();
				} catch(Throwable t) {
					logger.error("Unable to close pool [{}]", entry.getKey(), t);
				}
			}
			pools.clear();
		} finally {
			lock.unlockWrite(stamp);
		}
		wta = null;
		logger.info("Cleaned up");
	}
	
	private void init() {
		String path = null;
		File file = null;
		
		// Locate data sources configuration file:
		// 1 - look into custom webappsEtc directory
		//   1.1 look for '/path/to/webappsEtc/myWebappFullName/data-sources.xml'
		//   1.2 look for '/path/to/webappsEtc/data-sources.xml'
		// 2 - fallback on default configuration inside webapp
		//   2.1 look for '../META-INF/data-sources.xml'
		
		if (!StringUtils.isBlank(wta.getEtcPath())) {
			path = PathUtils.concatPathParts(wta.getEtcPath(), CONFIG_NAME);
			file = new File(path);
		}
		if ((file == null) || !file.exists()) {
			String etcDir = WebTopProps.getEtcDir(wta.getProperties());
			if (!StringUtils.isBlank(etcDir)) {
				path = PathUtils.concatPathParts(etcDir, CONFIG_NAME);
				file = new File(path);
			}
		}
		if ((file == null) || !file.exists()) {
			path = wta.getContextResourcePath(DEFAULT_CONFIG_RESOURCE_PATH);
			file = new File(path);
		}
		if (file == null) throw new WTRuntimeException("Configuration file not found [{}]", path);
		
		config = new DataSourcesConfig();
		try {
			logger.debug("Loading dataSources configuration at [{}]", path);
			config.parseConfiguration(file);
		} catch(Exception ex) {
			throw new RuntimeException("Unable to load dataSources configuration file", ex);
		}
		
		// Setup core sources
		logger.debug("Setting-up core dataSources...");
		HikariConfigMap coreSources = config.getSources(CoreManifest.ID);
		if (!coreSources.containsKey(DEFAULT_DATASOURCE)) {
			throw new RuntimeException("No core default dataSource defined");
		}
		for (Entry<String, HikariConfig> entry : coreSources.entrySet()) {
			registerDataSource(CoreManifest.ID, entry.getKey(), entry.getValue());
		}
	}
	
	public final void registerDataSource(final String namespace, final String dataSourceName, final HikariConfig config) {
		Check.notEmpty(namespace, "namespace");
		Check.notEmpty(dataSourceName, "dataSourceName");
		Check.notNull(config, "config");
		if (shuttingDown) throw new WTRuntimeException("Manager is coming down");
		if (!ClassUtils.hasStrictlyType(config, HikariConfig.class)) throw new IllegalArgumentException("You cannot use a subclass of HikariConfig here");
		String poolName = poolName(namespace, dataSourceName);
		
		long stamp = lock.writeLock();
		try {	
			logger.debug("Registering dataSource '{}' into namespace '{}' [{}]", dataSourceName, namespace);
			internalAddPool(poolName, config);
			logger.debug("DataSource '{}' successfully added", dataSourceName);
		} finally {
			lock.unlockWrite(stamp);
		}
	}
	
	public final void unregisterDataSource(final String namespace, final String dataSourceName) {
		Check.notEmpty(namespace, "namespace");
		Check.notEmpty(dataSourceName, "dataSourceName");
		if (shuttingDown) throw new WTRuntimeException("Manager is coming down");
		String poolName = poolName(namespace, dataSourceName);
		
		long stamp = lock.writeLock();
		try {	
			logger.debug("Unregistering dataSource '{}' into namespace '{}' [{}]", dataSourceName, namespace);
			internalRemovePool(poolName);
			logger.debug("DataSource '{}' successfully removed", dataSourceName);
		} finally {
			lock.unlockWrite(stamp);
		}
	}
	
	public DataSourcesConfig getConfiguration() {
		return config;
	}
	
	/**
	 * Builds a valid connection pool name.
	 * @param namespace The pool namespace (eg. the service ID).
	 * @param dataSourceName The data source name (eg. default)
	 * @return Concatenated name
	 */
	public String poolName(final String namespace, final String dataSourceName) {
		return Check.notNull(namespace, "namespace") + "." + Check.notNull(dataSourceName, "dataSourceName");
	}
	
	/**
	 * Returns the default Core DataSource.
	 * @return DataSource object.
	 */
	public DataSource getDataSource() {
		return getDataSource(CoreManifest.ID, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns the default DataSource from desired namespace.
	 * @param namespace The pool namespace.
	 * @return DataSource object.
	 */
	public DataSource getDataSource(final String namespace) {
		Check.notEmpty(namespace, "namespace");
		return getDataSource(namespace, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns a DataSource from desired namespace.
	 * @param namespace The pool namespace.
	 * @param dataSourceName The dataSource name.
	 * @return DataSource object.
	 */
	public DataSource getDataSource(final String namespace, final String dataSourceName) {
		Check.notEmpty(namespace, "namespace");
		Check.notEmpty(dataSourceName, "dataSourceName");
		if (shuttingDown) throw new WTRuntimeException("Manager is coming down");
		String poolName = poolName(namespace, dataSourceName);
		
		long stamp = lock.readLock();
		try {
			return internalGetPool(poolName);
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	/**
	 * Return the default Core connection.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(CoreManifest.ID);
	}
	
	/**
	 * Return the default Core connection.
	 * @param autoCommit False to disable auto-commit mode; defaults to True.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getConnection(final boolean autoCommit) throws SQLException {
		return getConnection(CoreManifest.ID, autoCommit);
	}
	
	/**
	 * Returns the default connection from desired namespace.
	 * @param namespace The pool namespace.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	@Override
	public Connection getConnection(final String namespace) throws SQLException {
		return getConnection(namespace, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns the default connection from desired namespace.
	 * @param namespace The pool namespace.
	 * @param autoCommit False to disable auto-commit mode; defaults to True.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getConnection(final String namespace, final boolean autoCommit) throws SQLException {
		return getConnection(namespace, DEFAULT_DATASOURCE, autoCommit);
	}
	
	/**
	 * Returns a connection from desired namespace.
	 * @param namespace The pool namespace.
	 * @param dataSourceName The dataSource name.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	@Override
	public Connection getConnection(final String namespace, final String dataSourceName) throws SQLException {
		return getConnection(namespace, dataSourceName, true);
	}
	
	/**
	 * Returns a connection from desired namespace.
	 * @param namespace The pool namespace.
	 * @param dataSourceName The dataSource name.
	 * @param autoCommit False to disable auto-commit mode; defaults to True.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getConnection(final String namespace, final String dataSourceName, final boolean autoCommit) throws SQLException {
		Check.notEmpty(namespace, "namespace");
		Check.notEmpty(dataSourceName, "dataSourceName");
		if (shuttingDown) throw new WTRuntimeException("Manager is coming down");
		String poolName = poolName(namespace, dataSourceName);
		
		long stamp = lock.readLock();
		try {
			return internalGetPoolConnection(poolName, autoCommit);
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	/**
	 * Returns the default connection from desired namespace (with fallback).
	 * @param namespace The pool namespace.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getFallbackConnection(final String namespace) throws SQLException {
		return getFallbackConnection(namespace, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns the default connection from desired namespace (with fallback).
	 * @param namespace The pool namespace.
	 * @param autoCommit False to disable auto-commit mode; defaults to True.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getFallbackConnection(final String namespace, final boolean autoCommit) throws SQLException {
		return getFallbackConnection(namespace, DEFAULT_DATASOURCE, autoCommit);
	}
	
	/**
	 * Returns a connection from desired namespace (with fallback).
	 * @param namespace The pool namespace.
	 * @param dataSourceName The dataSource name.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getFallbackConnection(final String namespace, final String dataSourceName) throws SQLException {
		return getFallbackConnection(namespace, dataSourceName, true);
	}
	
	/**
	 * Returns a connection from desired namespace (with fallback).
	 * @param namespace The pool namespace.
	 * @param dataSourceName The dataSource name.
	 * @param autoCommit False to disable auto-commit mode; defaults to True.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getFallbackConnection(final String namespace, final String dataSourceName, final boolean autoCommit) throws SQLException {
		Check.notEmpty(namespace, "namespace");
		Check.notEmpty(dataSourceName, "dataSourceName");
		if (shuttingDown) throw new WTRuntimeException("Manager is coming down");
		String poolName = poolName(namespace, dataSourceName);
		
		long stamp = lock.readLock();
		try {
			if (!internalIsRegistered(poolName)) poolName = poolName(CoreManifest.ID, DEFAULT_DATASOURCE);
			return internalGetPoolConnection(poolName, autoCommit);
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	public boolean isRegistered(final String namespace, final String dataSourceName) {
		Check.notEmpty(namespace, "namespace");
		Check.notEmpty(dataSourceName, "dataSourceName");
		if (shuttingDown) throw new WTRuntimeException("Manager is coming down");
		String poolName = poolName(namespace, dataSourceName);
		
		long stamp = lock.readLock();
		try {
			return internalIsRegistered(poolName);
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	private boolean internalIsRegistered(String poolName) {
		return pools.containsKey(poolName);
	}
	
	private void internalAddPool(String poolName, HikariConfig config) {
		if (pools.containsKey(poolName)) throw new WTRuntimeException("Pool for already defined. [{}]", poolName);
		config.setPoolName(poolName); // Make sure name is poolName!
		pools.put(poolName, new HikariDataSource(config));
	}
	
	private HikariDataSource internalGetPool(String poolName) {
		if (!pools.containsKey(poolName)) throw new WTRuntimeException("Pool not found. [{}]", poolName);
		return pools.get(poolName);
	}
	
	private Connection internalGetPoolConnection(String poolName, boolean autoCommit) throws SQLException {
		Connection con = internalGetPool(poolName).getConnection();
		con.setAutoCommit(autoCommit);
		return con;
	}
	
	private void internalRemovePool(String poolName) {
		HikariDataSource pool = pools.remove(poolName);
		if (pool == null) throw new WTRuntimeException("Pool not found. [{}]", poolName);
		pool.close();
	}
	
	/*
	private void addPool(String name, String driverClassName, String jdbcUrl, String username, String password) throws SQLException {
		synchronized(pools) {
			if(pools.containsKey(name)) throw new RuntimeException();
			HikariConfig config = new HikariConfig();
			config.setDriverClassName(driverClassName);
			config.setJdbcUrl(jdbcUrl);
			if(username != null) config.setUsername(username);
			if(password != null) config.setPassword(password);
			config.setMaximumPoolSize(5);
			config.setMaximumPoolSize(20);
			
			pools.put(name, new HikariDataSource(config));
		}
	}
	*/
	/*
	public void registerJdbc3DataSource(String name, String driverClassName, String jdbcUrl, String username, String password) throws SQLException {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driverClassName);
		config.setJdbcUrl(jdbcUrl);
		if(username != null) config.setUsername(username);
		if(password != null) config.setPassword(password);
		
		logger.debug("Registering jdbc3 data source [{}]", name);
		logger.trace("[{}, {}, {}]", driverClassName, jdbcUrl, username);
		addPool(name, config);
	}
	
	public void registerJdbc4DataSource(String poolName, String dataSourceClassName, String serverName, Integer serverPort, String databaseName, String user, String password) throws SQLException {
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName(dataSourceClassName);
		config.addDataSourceProperty("serverName", serverName);
		if(serverPort != null) config.addDataSourceProperty("port", serverPort);
		config.addDataSourceProperty("databaseName", databaseName);
		config.addDataSourceProperty("user", user);
		if(password != null) config.addDataSourceProperty("password", password);
		
		logger.debug("Registering jdbc4 data source [{}]", poolName);
		logger.trace("[{}, {}, {}, {}, {}]", dataSourceClassName, serverName, serverPort, databaseName, user);
		addPool(poolName, config);
	}
	*/
}
