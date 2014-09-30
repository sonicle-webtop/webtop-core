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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import javax.sql.DataSource;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ConnectionManager {
	
	private final static Logger logger = WebTopApp.getLogger(ConnectionManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized ConnectionManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		ConnectionManager cm = new ConnectionManager(wta);
		initialized = true;
		logger.info("ConnectionManager initialized.");
		return cm;
	}
	
	private boolean shutdown = false;
	private WebTopApp wta = null;
	private final HashMap<String, HikariDataSource> pools = new HashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private ConnectionManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs shutdown process.
	 */
	public void shutdown() {
		synchronized(pools) {
			shutdown = true;
			for(HikariDataSource pool: pools.values()) {
				pool.shutdown();
			}
			pools.clear();
		}
	}
	
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
	
	/**
	 * Returns a DataSource from desired pool.
	 * @param poolName The name of the pool.
	 * @return DataSource object.
	 * @throws SQLException 
	 */
	public DataSource getDataSource(String poolName) throws SQLException {
		return getPool(poolName).getDataSource();
	}
	
	/**
	 * Returns a connection from desired pool.
	 * @param poolName The name of the pool.
	 * @return A ready Connection object.
	 * @throws SQLException 
	 */
	public Connection getConnection(String poolName) throws SQLException {
		return getPool(poolName).getConnection();
	}
	
	private void addPool(String poolName, HikariConfig config) {
		synchronized(pools) {
			if(shutdown) throw new RuntimeException("Manager is shutting down");
			if(pools.containsKey(poolName)) throw new RuntimeException(MessageFormat.format("Pool for [{0}] is already defined.", poolName));
			config.setPoolName(poolName);
			config.setMinimumIdle(5);
			config.setMaximumPoolSize(20);
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
