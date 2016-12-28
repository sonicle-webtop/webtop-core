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
import com.sonicle.webtop.core.app.DataSourcesConfig.HikariConfigMap;
import com.sonicle.webtop.core.sdk.interfaces.IConnectionProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.sql.DataSource;
import net.sf.qualitycheck.Check;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ConnectionManager implements IConnectionProvider {
	private static final Logger logger = WT.getLogger(ConnectionManager.class);
	public static final String DEFAULT_DATASOURCE = "default";
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized ConnectionManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		ConnectionManager conm = new ConnectionManager(wta);
		initialized = true;
		logger.info("ConnectionManager initialized");
		return conm;
	}
	
	private boolean shutdown = false;
	private WebTopApp wta = null;
	private DataSourcesConfig config = null;
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
	public void cleanup() {
		synchronized(pools) {
			shutdown = true;
			for(HikariDataSource pool: pools.values()) {
				pool.close();
			}
			pools.clear();
		}
		wta = null;
		logger.info("ConnectionManager destroyed");
	}
	
	private void init() {
		// Loads dataSources configuration
		String configResource = "/META-INF/data-sources.xml";
		config = new DataSourcesConfig();
		try {
			logger.debug("Loading dataSources configuration at [{}]", configResource);
			URL url = wta.getContextResource(configResource);
			config.parseConfiguration(url);
		} catch(Exception ex) {
			throw new RuntimeException("Unable to load dataSources configuration file", ex);
		}
		
		// Setup core sources
		logger.debug("Setting-up core dataSources...");
		HikariConfigMap coreSources = config.getSources(CoreManifest.ID);
		if(!coreSources.containsKey(DEFAULT_DATASOURCE)) {
			throw new RuntimeException("No core default dataSource defined");
		}
		for(Entry<String, HikariConfig> entry : coreSources.entrySet()) {
			registerDataSource(CoreManifest.ID, entry.getKey(), entry.getValue());
		}
	}
	
	public final void registerDataSource(String namespace, String dataSourceName, HikariConfig config) {
		logger.debug("Registering data source [{}] into namespace [{}]", dataSourceName, namespace);
		addPool(poolName(namespace, dataSourceName), config);
	}
	
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
	
	public DataSourcesConfig getConfiguration() {
		return config;
	}
	
	/**
	 * Builds a valid connection pool name.
	 * @param namespace The pool namespace (eg. the service ID).
	 * @param dataSourceName The data source name (eg. default)
	 * @return Concatenated name
	 */
	public String poolName(String namespace, String dataSourceName) {
		return namespace + "." + dataSourceName;
	}
	
	/**
	 * Returns the default Core DataSource.
	 * @return DataSource object.
	 * @throws SQLException 
	 */
	public DataSource getDataSource() throws SQLException {
		return getDataSource(CoreManifest.ID, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns the default DataSource from desired namespace.
	 * @param namespace The pool namespace.
	 * @return DataSource object.
	 * @throws SQLException 
	 */
	public DataSource getDataSource(String namespace) throws SQLException {
		return getDataSource(namespace, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns a DataSource from desired namespace.
	 * @param namespace The pool namespace.
	 * @param dataSourceName The dataSource name.
	 * @return DataSource object.
	 * @throws SQLException 
	 */
	public DataSource getDataSource(String namespace, String dataSourceName) throws SQLException {
		return getPool(poolName(namespace, dataSourceName));
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
	public Connection getConnection(boolean autoCommit) throws SQLException {
		return getConnection(CoreManifest.ID, autoCommit);
	}
	
	/**
	 * Returns the default connection from desired namespace.
	 * @param namespace The pool namespace.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	@Override
	public Connection getConnection(String namespace) throws SQLException {
		return getConnection(namespace, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns the default connection from desired namespace.
	 * @param namespace The pool namespace.
	 * @param autoCommit False to disable auto-commit mode; defaults to True.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getConnection(String namespace, boolean autoCommit) throws SQLException {
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
	public Connection getConnection(String namespace, String dataSourceName) throws SQLException {
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
	public Connection getConnection(String namespace, String dataSourceName, boolean autoCommit) throws SQLException {
		Check.notNull(namespace);
		Check.notNull(dataSourceName);
		String poolName = poolName(namespace, dataSourceName);
		Connection con = getPool(poolName).getConnection();
		con.setAutoCommit(autoCommit);
		return con;
	}
	
	/**
	 * Returns the default connection from desired namespace (with fallback).
	 * @param namespace The pool namespace.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getFallbackConnection(String namespace) throws SQLException {
		return getFallbackConnection(namespace, DEFAULT_DATASOURCE);
	}
	
	/**
	 * Returns the default connection from desired namespace (with fallback).
	 * @param namespace The pool namespace.
	 * @param autoCommit False to disable auto-commit mode; defaults to True.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getFallbackConnection(String namespace, boolean autoCommit) throws SQLException {
		return getFallbackConnection(namespace, DEFAULT_DATASOURCE, autoCommit);
	}
	
	/**
	 * Returns a connection from desired namespace (with fallback).
	 * @param namespace The pool namespace.
	 * @param dataSourceName The dataSource name.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getFallbackConnection(String namespace, String dataSourceName) throws SQLException {
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
	public Connection getFallbackConnection(String namespace, String dataSourceName, boolean autoCommit) throws SQLException {
		Check.notNull(namespace);
		Check.notNull(dataSourceName);
		String poolName = poolName(namespace, dataSourceName);
		if (isRegistered(namespace, dataSourceName)) {
			return getConnection(namespace, dataSourceName, autoCommit);
		} else {
			return getConnection(autoCommit);
		}
	}
	
	private void addPool(String poolName, HikariConfig config) {
		synchronized(pools) {
			if(shutdown) throw new RuntimeException("Manager is shutting down");
			if(pools.containsKey(poolName)) throw new RuntimeException(MessageFormat.format("Pool for [{0}] is already defined.", poolName));
			config.setPoolName(poolName);
			pools.put(poolName, new HikariDataSource(config));
		}
	}
	
	private HikariDataSource getPool(String name) {
		synchronized(pools) {
			if(shutdown) throw new RuntimeException("Manager is shutting down");
			if(!pools.containsKey(name)) throw new RuntimeException(MessageFormat.format("Pool [{0}] not found", name));
			return pools.get(name);
		}
	}
	
	public boolean isRegistered(String namespace, String dataSourceName) {
		return isRegistered(poolName(namespace, dataSourceName));
	}
	
	private boolean isRegistered(String poolName) {
		synchronized(pools) {
			if(shutdown) throw new RuntimeException("Manager is shutting down");
			return pools.containsKey(poolName);
		}
	}
	
	/*
	public boolean isRegistered(String poolName) {
		synchronized(pools) {
			if(shutdown) throw new RuntimeException("Manager is shutting down");
			return pools.containsKey(poolName);
		}
	}
	*/
	
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
}
