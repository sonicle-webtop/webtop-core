/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.rits.cloning.Cloner;
import com.sonicle.commons.ClassUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.beans.PageInfo;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.security.PasswordUtils;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreSettings.DataSourceTypeObj;
import com.sonicle.webtop.core.app.io.dbutils.FilterInfo;
import com.sonicle.webtop.core.app.io.dbutils.FilterableArrayListHandler;
import com.sonicle.webtop.core.app.io.dbutils.LongScalarHandler;
import com.sonicle.webtop.core.app.io.dbutils.RowsAndCols;
import com.sonicle.webtop.core.app.sdk.WTConnectionException;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.bol.ODataSource;
import com.sonicle.webtop.core.bol.ODataSourceQuery;
import com.sonicle.webtop.core.bol.VDataSourceQuery;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DataSourceDAO;
import com.sonicle.webtop.core.dal.DataSourceQueryDAO;
import com.sonicle.webtop.core.model.DataSourceQuery;
import com.sonicle.webtop.core.model.DataSourceQueryBase;
import com.sonicle.webtop.core.model.DataSource;
import com.sonicle.webtop.core.model.DataSourceBase;
import com.sonicle.webtop.core.model.DataSourcePooled;
import com.sonicle.webtop.core.model.DataSourceType;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.sf.qualitycheck.Check;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.StatementConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class DataSourcesManager extends AbstractAppManager<DataSourcesManager> {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(DataSourcesManager.class);	
	private static final Pattern PATTERN_QUERY_PLACEHOLDER = Pattern.compile("\\{\\{([A-Z][A-Z0-9_]*)\\}\\}");
	private static final HashMap<String, DataSourceType> WELLKNOWN_TYPES = new HashMap<>();
	private final LinkedHashSet<String> loadedDrivers = new LinkedHashSet<>();
	private final HashMap<String, HikariDataSource> pools = new HashMap<>();
	private final KeyedReentrantLocks<String> poolLocks = new KeyedReentrantLocks();
	private final ExecutorService dataSourceConnectionCheckers = Executors.newCachedThreadPool();
	
	private static final String DBPRODUCT_POSTGRES = "PostgreSQL";
	private static final String DBPRODUCT_MARIADB = "MariaDB";
	private static final String DBPRODUCT_MYSQL = "MySQL";
	
	static {
		WELLKNOWN_TYPES.put("postgresql", new DataSourceType("postgresql", "PostgreSQL", "text/x-pgsql"));
		WELLKNOWN_TYPES.put("mariadb", new DataSourceType("mariadb", "MariaDB", "text/x-mariadb"));
		WELLKNOWN_TYPES.put("mysql", new DataSourceType("mysql", "MySQL", "text/x-mysql"));
		// https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?redirectedfrom=MSDN&view=sql-server-ver15
		WELLKNOWN_TYPES.put("sqlserver", new DataSourceType("sqlserver", "MS SQLServer", "text/x-mssql", null, "databaseName"));
	}
	
	DataSourcesManager(WebTopApp wta) {
		super(wta, true);
		
		// Preload included drivers
		// Due to internal usage of "org.postgresql.Driver" we can safely skip it!
		loadDriverQuietly("org.mariadb.jdbc.Driver");
		loadDriverQuietly("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	}

	@Override
	protected void doAppManagerInitialize() {
		initPools();
	}
	
	@Override
	protected Logger doGetLogger() {
		return LOGGER;
	}
	
	@Override
	protected void doAppManagerCleanup() {
		dataSourceConnectionCheckers.shutdownNow();
		LOGGER.debug("Clearing pools...");
		clearPools();
		LOGGER.debug("Unloading drivers...");
		unloadLoadedDrivers();
	}
	
	private void initPools() {
		DataSourceDAO dsDao = DataSourceDAO.getInstance();
		Connection con = null;
		
		try {
			con = getConnection(CoreManifest.ID);
			for (ODataSource ods : dsDao.selectAll(con)) {
				final DataSource dataSource = fillDataSource(new DataSource(null), ods);
				decryptPassword(dataSource);
				
				try {
					final String poolName = toPoolName(dataSource.getDomainId(), dataSource.getDataSourceId());
					doAddPool(poolName, createHikariConfig(dataSource));
				} catch (Exception ex1) {
					LOGGER.error("Unable to initialize pool for data-source '{}:{}'", dataSource.getDataSourceId(), dataSource.getType(), ex1);
				}
			}

		} catch (Exception ex) {
			throw new WTRuntimeException(ex, "Unable to initialize DataSource pools");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void clearPools() {
		for (Map.Entry<String, HikariDataSource> entry : pools.entrySet()) {
			try {
				poolLocks.lockInterruptibly(entry.getKey());
				entry.getValue().close();
			} catch (InterruptedException ex) {
				LOGGER.error("Unable to clear pool '{}'", entry.getKey(), ex);
			} finally {
				poolLocks.unlock(entry.getKey());
			}
		}
		pools.clear();
	}
	
	public Map<String, DataSourceType> listDataSourceTypes(final String domainId) throws WTException {
		Check.notNull(domainId, "domainId");
		long stamp = readyLock();
		try {
			return doDataSourceTypesGet(domainId);
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public Map<String, DataSourcePooled> listDataSources(final String domainId) throws WTException {
		Check.notNull(domainId, "domainId");
		DataSourceDAO dsDao = DataSourceDAO.getInstance();
		DataSourceQueryDAO dsqDao = DataSourceQueryDAO.getInstance();
		Connection con = null;
		
		long stamp = readyLock();
		try {
			Map<String, DataSourceType> dsTypes = doDataSourceTypesGet(domainId);
			
			try {
				con = getConnection(CoreManifest.ID);
				Map<String, List<VDataSourceQuery>> queriesMap = dsqDao.mapDataSourceByDomain(con, domainId);
				LinkedHashMap<String, DataSourcePooled> items = new LinkedHashMap<>();
				for (ODataSource ods : dsDao.selectByDomain(con, domainId)) {
					final String poolName = toPoolName(domainId, ods.getDataSourceId());
					DataSourcePooled.PoolStatus poolStatus = null;
					try {
						poolLocks.lockInterruptibly(poolName);
						poolStatus = createPoolStatus(poolName);
					} finally {
						poolLocks.unlock(poolName);
					}
					
					DataSourceType dsType = dsTypes.get(ods.getType());
					final String dialectMime = dsType != null ? dsType.getDialectMime() : null;
					final DataSourcePooled dataSource = fillDataSource(new DataSourcePooled(dialectMime, poolStatus), ods);
					decryptPassword(dataSource);
					if (queriesMap.containsKey(ods.getDataSourceId())) {
						final Map<String, DataSourcePooled.Query> queries = queriesMap.get(ods.getDataSourceId()).stream()
							.collect(Collectors.toMap(item -> item.getQueryId(), item -> new DataSourcePooled.Query(item.getQueryId(), item.getName(), item.getDescription(), item.getUsageCount()), (ov, nv) -> nv, LinkedHashMap::new));
						dataSource.setQueries(queries);
					}
					items.put(ods.getDataSourceId(), dataSource);
				}
				return items;

			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public DataSource getDataSource(final String domainId, final String dataSourceId) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(dataSourceId, "dataSourceId");
		Connection con = null;
		
		long stamp = readyLock();
		try {
			try {
				con = getConnection(CoreManifest.ID);
				return doDataSourceGet(con, domainId, dataSourceId);
				
			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public DataSource addDataSource(final String domainId, final DataSourceBase dataSource) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(dataSource, "dataSource");
		DataSourceDAO dsDao = DataSourceDAO.getInstance();
		Connection con = null;
		
		long stamp = readyLock();
		try {
			DataSourceType dsType = doDataSourceTypeGet(domainId, dataSource.getType());
			if (dsType == null) throw new WTException("Unsupported dataSource type [{}]", dataSource.getType());
			
			try {
				encryptPassword(dataSource);
				String newDataSourceId = IdentifierUtils.getUUIDTimeBased(true);
				ODataSource ods = fillODataSource(new ODataSource(), dataSource);
				ods.setDataSourceId(newDataSourceId);
				ods.setDomainId(domainId);
				fillODataSourceDefaultsForInsert(ods, BaseDAO.createRevisionTimestamp());

				con = getConnection(CoreManifest.ID);
				boolean ret = dsDao.insert(con, ods) == 1;
				if (!ret) return null;
				DataSource created = fillDataSource(new DataSource(dsType.getDialectMime()), ods);
				decryptPassword(created);
				
				// Setup a pool for newly created dataSource
				lockedSetupPool(domainId, created, SetupPoolMode.ADD);
				
				return created;

			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public void updateDataSource(final String domainId, final String dataSourceId, final DataSourceBase dataSource, final boolean setPassword) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(dataSource, "dataSource");
		DataSourceDAO dsDao = DataSourceDAO.getInstance();
		Connection con = null;
		
		long stamp = readyLock();
		try {
			try {
				if (setPassword) encryptPassword(dataSource);
				ODataSource ods = fillODataSource(new ODataSource(), dataSource);
				ods.setDataSourceId(dataSourceId);
				ods.setDomainId(domainId);

				con = getConnection(CoreManifest.ID);
				boolean ret = dsDao.update(con, ods, setPassword, BaseDAO.createRevisionTimestamp()) == 1;
				if (ret == false) throw new WTNotFoundException("DataSource not found [{}, {}]", domainId, dataSourceId);
				
				DataSource updated = doDataSourceGet(con, domainId, dataSourceId);
				
				// Update the pool associated to the dataSource
				lockedSetupPool(domainId, updated, SetupPoolMode.UPDATE);

			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public void deleteDataSource(final String domainId, final String dataSourceId) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(dataSourceId, "dataSourceId");
		DataSourceDAO dsDao = DataSourceDAO.getInstance();
		Connection con = null;
		
		long stamp = readyLock();
		try {
			try {
				con = getConnection(CoreManifest.ID);
				int ret = dsDao.deleteByIdDomain(con, dataSourceId, domainId);
				if (ret == 0) throw new WTNotFoundException("DataSource not found [{}, {}]", domainId, dataSourceId);

			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
			
			// Remove the pool associated to the dataSource
			final String poolName = toPoolName(domainId, dataSourceId);
			try {
				poolLocks.lockInterruptibly(poolName);
				doRemovePool(poolName);
			} catch (InterruptedException ex) {
				LOGGER.error("Unable to remove pool '{}'", poolName, ex);
			} finally {
				poolLocks.unlock(poolName);
			}
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public void checkDataSourceConnection(final String domainId, final String dataSourceId) throws WTConnectionException, WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(dataSourceId, "dataSourceId");
		
		long stamp = readyLock();
		try {
			DataSource ds = null;
			Connection con = null;
			try {
				con = getConnection(CoreManifest.ID);
				ds = doDataSourceGet(con, domainId, dataSourceId);
				
			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
			
			if (ds == null) throw new WTNotFoundException("DataSource not found [{}, {}]", domainId, dataSourceId);
			DataSourceType dsType = doDataSourceTypeGet(domainId, ds.getType());
			if (dsType == null) throw new WTException("Unsupported dataSource type [{}]", ds.getType());
			doCheckDataSourceConnection(createJdbcConfig(dsType, ds.getServerName(), ds.getServerPort(), ds.getDatabaseName(), ds.getUsername(), ds.getPassword(), ds.getDriverProps()));
			
			// Add (if necessary) the pool associated to the dataSource
			lockedSetupPool(domainId, ds, SetupPoolMode.ADD_QUIETLY);
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public void checkDataSourceConnection(final String domainId, final String dataSourceType, final String serverName, final Integer serverPort, final String databaseName, final String username, final String password, final Map<String, String> props) throws WTConnectionException, WTException {
		long stamp = readyLock();
		try {
			DataSourceType dsType = doDataSourceTypeGet(domainId, dataSourceType);
			if (dsType == null) throw new WTException("Unsupported dataSource type [{}]", dataSourceType);
			doCheckDataSourceConnection(createJdbcConfig(dsType, serverName, serverPort, databaseName, username, password, props));
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public DataSourceQuery getDataSourceQuery(final String domainId, final String queryId) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(queryId, "queryId");
		Connection con = null;
		
		long stamp = readyLock();
		try {
			try {
				con = getConnection(CoreManifest.ID);
				return doDataSourceQueryGet(con, domainId, queryId);
				
			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public DataSourceQuery addDataSourceQuery(final String domainId, final String dataSourceId, final DataSourceQueryBase query) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(dataSourceId, "dataSourceId");
		Check.notNull(query, "query");
		DataSourceDAO dsDao = DataSourceDAO.getInstance();
		DataSourceQueryDAO dsqDao = DataSourceQueryDAO.getInstance();
		Connection con = null;
		
		long stamp = readyLock();
		try {
			try {
				String newQueryId = IdentifierUtils.getUUIDTimeBased(true);
				ODataSourceQuery odsq = fillODataSourceQuery(new ODataSourceQuery(), query);
				odsq.setQueryId(newQueryId);
				odsq.setDataSourceId(dataSourceId);
				fillODataSourceQueryDefaultsForInsert(odsq, BaseDAO.createRevisionTimestamp());

				con = getConnection(CoreManifest.ID);
				if (!dsDao.existByIdDomain(con, dataSourceId, domainId)) throw new WTNotFoundException("DataSource not found [{}, {}]", domainId, dataSourceId);
				boolean ret = dsqDao.insert(con, odsq) == 1;
				return !ret ? null : fillDataSourceQuery(new DataSourceQuery(), odsq);
				
			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public void updateDataSourceQuery(final String domainId, final String queryId, final DataSourceQueryBase query) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(queryId, "queryId");
		DataSourceQueryDAO dsqDao = DataSourceQueryDAO.getInstance();
		Connection con = null;
		
		long stamp = readyLock();
		try {
			try {
				ODataSourceQuery odsq = fillODataSourceQuery(new ODataSourceQuery(), query);
				odsq.setQueryId(queryId);
				
				con = getConnection(CoreManifest.ID);
				boolean ret = dsqDao.update(con, odsq, BaseDAO.createRevisionTimestamp(), domainId) == 1;
				if (ret == false) throw new WTNotFoundException("DataSource Query not found [{}, {}]", domainId, queryId);
				
			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public void deleteDataSourceQuery(final String domainId, final String queryId) throws WTException {
		Check.notNull(domainId, "domainId");
		Check.notNull(queryId, "queryId");
		DataSourceQueryDAO dsqDao = DataSourceQueryDAO.getInstance();
		Connection con = null;
		
		long stamp = readyLock();
		try {
			try {
				con = getConnection(CoreManifest.ID);
				int ret = dsqDao.deleteByIdDomain(con, queryId, domainId);
				if (ret == 0) throw new WTNotFoundException("DataSource Query not found [{}, {}]", domainId, queryId);
				
			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	private DataSourceQuery doDataSourceQueryLookup(final String domainId, final String dataSourceId) throws WTException {
		Connection con = null;
		
		try {
			con = getConnection(CoreManifest.ID);
			return doDataSourceQueryGet(con, domainId, dataSourceId);

		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, DataSourceQuery> listDataSourceQueries(final String domainId) throws WTException {
		Check.notNull(domainId, "domainId");
		
		long stamp = readyLock();
		try {
			DataSourceQueryDAO dsqDao = DataSourceQueryDAO.getInstance();
			Connection con = null;
			
			try {
				con = getConnection(CoreManifest.ID);
				LinkedHashMap<String, DataSourceQuery> items = new LinkedHashMap<>();
				for (ODataSourceQuery odsq : dsqDao.selectByDomain(con, domainId)) {
					items.put(odsq.getQueryId(), fillDataSourceQuery(new DataSourceQuery(), odsq));
				}
				return items;

			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public Set<String> guessQueryColumns(final String domainId, final String queryId, final QueryPlaceholders placeholders) throws WTException {
		long stamp = readyLock();
		try {
			DataSourceQuery dsQuery = doDataSourceQueryLookup(domainId, queryId);
			if (dsQuery == null) throw new WTNotFoundException("Query not found [{}, {}]", queryId, domainId);
			
			final String poolName = toPoolName(domainId, dsQuery.getDataSourceId());
			LOGGER.debug("Guessing '{}' query columns against '{}'", queryId, poolName);
			try {
				poolLocks.lockInterruptibly(poolName);
				Connection pcon = null;
				try {
					pcon = doGetPoolConnection(poolName);
					if (pcon == null) throw new DataSourceBase.WTPoolException("Pool not available [{}]", poolName);
					return doGuessQueryColumns(pcon, dsQuery.getRawSql(), placeholders);
					
				} catch (SQLException ex) {
					throw new WTException(ex, "Unable to get a connection from pool [{}]", poolName);
				} finally {
					DbUtils.closeQuietly(pcon);
				}
			} catch (InterruptedException ex) {
				throw new WTException(ex, "Unable to acquire pool '{}'", poolName);
			} finally {
				poolLocks.unlock(poolName);
			}
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public Set<String> extractQueryPlaceholders(final String domainId, final String queryId) throws WTException {
		long stamp = readyLock();
		try {
			DataSourceQuery dsQuery = doDataSourceQueryLookup(domainId, queryId);
			if (dsQuery == null) throw new WTNotFoundException("Query not found [{}, {}]", queryId, domainId);
			
			LOGGER.debug("Extracting '{}' query placeholders", queryId);
			return extractQueryPlaceholders(dsQuery.getRawSql());
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public static class FilterClause {
		public final Collection<String> fields;
		public final FilterInfo filterInfo;
		
		public FilterClause(Collection<String> fields, FilterInfo filterInfo) {
			this.fields = Check.notEmpty(fields, "fields");
			this.filterInfo = Check.notNull(filterInfo, "filterInfo");
		}
	}
	
	public <T> DataSourceBase.ExecuteQueryResult<T> executeQuery(final String domainId, final String queryId, final QueryPlaceholders placeholders, final PageInfo paginationInfo, final boolean debugReport, final ResultSetHandler<T> resultSetHandler, final FilterClause filterClause, final Integer maxRows) throws WTException {
		long stamp = readyLock();
		try {
			DataSourceQuery dsQuery = doDataSourceQueryLookup(domainId, queryId);
			if (dsQuery == null) throw new WTNotFoundException("Query not found [{}, {}]", queryId, domainId);
			if (dsQuery.getForcePagination() && paginationInfo == null) throw new WTException("Pagination is mandatory for this query [{}, {}]", queryId, domainId);
			
			final String poolName = toPoolName(domainId, dsQuery.getDataSourceId());
			LOGGER.debug("Executing query '{}' against '{}'", queryId, poolName);
			try {
				poolLocks.lockInterruptibly(poolName);
				Connection pcon = null;
				try {
					pcon = doGetPoolConnection(poolName);
					if (pcon == null) throw new DataSourceBase.WTPoolException("Pool not available [{}]", poolName);
					return doExecuteQuery(pcon, dsQuery.getRawSql(), placeholders, paginationInfo, debugReport, resultSetHandler, filterClause, maxRows);
					
				} catch (SQLException ex) {
					//TODO: evaluate whether to throw WTException in this case
					return new DataSourceBase.ExecuteQueryResult(false, ex.getMessage(), null, (Integer)null);
				} finally {
					DbUtils.closeQuietly(pcon);
				}
			} catch (InterruptedException ex) {
				throw new WTException(ex, "Unable to acquire pool '{}'", poolName);
			} finally {
				poolLocks.unlock(poolName);
			}
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	public <T> DataSourceBase.ExecuteQueryResult<T> executeRawQuery(final String domainId, final String dataSourceId, final String rawSql, final QueryPlaceholders placeholders, final PageInfo paginationInfo, final boolean debugReport, final ResultSetHandler<T> resultSetHandler, final Integer maxRows) throws WTException {
		long stamp = readyLock();
		try {
			final String poolName = toPoolName(domainId, dataSourceId);
			LOGGER.debug("Executing raw query against '{}'", poolName);
			try {
				poolLocks.lockInterruptibly(poolName);
				Connection pcon = null;
				try {
					pcon = doGetPoolConnection(poolName);
					if (pcon == null) throw new DataSourceBase.WTPoolException("Pool not available [{}]", poolName);
					return doExecuteQuery(pcon, rawSql, placeholders, paginationInfo, debugReport, resultSetHandler, null, maxRows);
					
				} catch (SQLException ex) {
					//TODO: evaluate whether to throw WTException in this case
					return new DataSourceBase.ExecuteQueryResult<>(false, ex.getMessage(), (T)null, (Integer)null);
				} finally {
					DbUtils.closeQuietly(pcon);
				}
			} catch (InterruptedException ex) {
				throw new WTException(ex, "Unable to acquire pool '{}'", poolName);
			} finally {
				poolLocks.unlock(poolName);
			}
			
		} finally {
			readyUnlock(stamp);
		}
	}
	
	private Set<String> doGuessQueryColumns(Connection con, String rawSql, QueryPlaceholders placeholders) throws WTException {
		if (StringUtils.isBlank(rawSql)) throw new WTException("Query is empty");
		
		try {
			String databaseProductName = con.getMetaData().getDatabaseProductName();
			String sanitizedQuery = replaceQueryPlaceholders(LangUtils.flattenLineBreaks(StringUtils.trim(rawSql)), placeholders);
			String sql = buildColumnGuessingQuery(databaseProductName, sanitizedQuery);
			
			QueryRunner qr = new QueryRunner(new StatementConfiguration.Builder().maxRows(1).build());
			RowsAndCols ret = qr.query(con, sql, new FilterableArrayListHandler());
			return ret.columns;
			
		} catch (SQLException ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		}
	}
	
	private <T> DataSourceBase.ExecuteQueryResult<T> doExecuteQuery(Connection con, String rawSql, QueryPlaceholders placeholders, PageInfo pagination, boolean debugReport, ResultSetHandler<T> resultSetHandler, FilterClause filterClause, Integer maxRows) throws WTException {
		
		if (StringUtils.isBlank(rawSql)) {
			if (debugReport) {
				return new DataSourceBase.ExecuteQueryResult(false, "> ERROR: query is empty", null, (Integer)null);
			} else {
				throw new WTException("Query is empty");
			}
		}
		
		boolean success = false;
		String message = null;
		Long totalCount = null;
		T resultSet = null;
		String sql = null;
		long start = 0, end;
		
		try {
			String databaseProductName = con.getMetaData().getDatabaseProductName();
			String sanitizedQuery = replaceQueryPlaceholders(LangUtils.flattenLineBreaks(StringUtils.trim(rawSql)), placeholders);
			QueryRunner qr = (maxRows != null) ? new QueryRunner(new StatementConfiguration.Builder().maxRows(maxRows).build()) : new QueryRunner();
			
			if (filterClause != null) {
				sql = buildFilterDataQuery(databaseProductName, sanitizedQuery, filterClause);
				start = System.nanoTime();
				if (LOGGER.isTraceEnabled()) LOGGER.trace("SQL Query (filter):\n{}", sql);
				resultSet = qr.query(con, sql, resultSetHandler);
				end = System.nanoTime();
				
			} else if (pagination != null) {
				sql = buildPaginationCountQuery(databaseProductName, sanitizedQuery);
				start = System.nanoTime();
				if (LOGGER.isTraceEnabled()) LOGGER.trace("SQL Query (total count):\n{}", sql);
				totalCount = qr.query(con, sql, new LongScalarHandler());
				sql = buildPaginationDataQuery(databaseProductName, sanitizedQuery, pagination);
				if (LOGGER.isTraceEnabled()) LOGGER.trace("SQL Query (data paginated):\n{}", sql);
				resultSet = qr.query(con, sql, resultSetHandler);
				end = System.nanoTime();
				
			} else {
				sql = sanitizedQuery;
				start = System.nanoTime();
				if (LOGGER.isTraceEnabled()) LOGGER.trace("SQL Query (data):\n{}", sql);
				resultSet = qr.query(con, sql, resultSetHandler);
				end = System.nanoTime();
			}
			success = true;
			if (debugReport) message = buildResultMessage(sql, start, end);
			
		} catch (SQLException ex) {
			if (debugReport) {
				end = System.nanoTime();
				message = buildResultMessage(sql, start, end, ex);
			} else {
				throw ExceptionUtils.wrapThrowable(ex);
			}
		}
		
		if (resultSet != null) {
			return new DataSourceBase.ExecuteQueryResult<>(success, message, resultSet, totalCount);
		} else {
			return new DataSourceBase.ExecuteQueryResult<>(success, message, (T)null, totalCount);
		}
	}
	
	private String buildResultMessage(String sql, long execStart, long execEnd) {
		return buildResultMessage(sql, execStart, execEnd, null);
	}
	
	private String buildResultMessage(String sql, long execStart, long execEnd, SQLException ex) {
		long millis = (execEnd > execStart) ? TimeUnit.MILLISECONDS.convert(execEnd-execStart, TimeUnit.NANOSECONDS) : 0;
		StringBuilder sb = new StringBuilder();
		sb.append(sql).append("\n");
		if (ex != null) {
			SQLException cause = ex.getNextException() != null ? ex.getNextException() : ex;
			String sqlState = cause.getSQLState();
			int errorCode = cause.getErrorCode();
			sb.append("> ERROR [SQLState: ").append(sqlState).append(", ErrorCode: ").append(errorCode).append("]:").append("\n");
			sb.append(cause.getMessage()).append("\n");
		} else {
			sb.append("> OK").append("\n");
		}
		
		sb.append("> Time: ").append((double)millis/1000).append("s\n");
		return sb.toString();
	}
	
	private Set<String> extractQueryPlaceholders(final String query) {
		LinkedHashSet<String> names = new LinkedHashSet<>();
		if (!StringUtils.isBlank(query)) {
			final Matcher matcher = PATTERN_QUERY_PLACEHOLDER.matcher(query);
			while (matcher.find()) {
				final String name = matcher.group(1);
				names.add(name);
			}
		}
		return names;
	}
	
	private String replaceQueryPlaceholders(final String query, final QueryPlaceholders placeholders) {
		if (placeholders != null) {
			String s = query;
			for (Map.Entry<String, String> entry : placeholders.values.entrySet()) {
				final String value = String.valueOf(entry.getValue());
				s = StringUtils.replace(s, "{{" + entry.getKey() + "}}", DbUtils.escapeSQL(StringUtils.defaultIfBlank(value, "")));
			}
			return s;
		} else {
			return query;
		}
	}
	
	private String buildColumnGuessingQuery(final String databaseProductName, final String query) {
		return query;
	}
	
	private String buildPaginationCountQuery(final String databaseProductName, final String query) {
		/*
		String from = query;
		//TODO: use regex here to match for eg. "ORDER    BY" or "ORDER \n\n BY", etc...
		int lastOrderBy = StringUtils.lastIndexOfIgnoreCase(query, "ORDER BY");
		if (lastOrderBy > 0) {
			from = StringUtils.trim(StringUtils.left(query, lastOrderBy));
		}
		*/
		String from = StringUtils.trim(stripQueryLastOrderBy(query));
		return "SELECT COUNT(1) FROM (" + from + ") AS __table__";
		
		//https://blog.jooq.org/calculating-pagination-metadata-without-extra-roundtrips-in-sql/
		//https://database.guide/pagination-in-sql-server-using-offset-fetch/
		//https://stackoverflow.com/questions/463859/there-are-a-method-to-paging-using-ansi-sql-only
		//https://social.msdn.microsoft.com/Forums/en-US/f24c0a3d-e8e9-4041-aca5-590a6e1a2e81/how-to-use-offset-fetch-next-in-sql-server-2008-r2?forum=databasedesign
		//https://www.postgresql.org/docs/current/sql-select.html#SQL-LIMIT
		//https://medium.com/swlh/sql-pagination-you-are-probably-doing-it-wrong-d0f2719cc166
		//https://use-the-index-luke.com/no-offset
	}
	
	private static final Pattern PATTERN_ORDERBY = Pattern.compile("(ORDER(?:\\s|\\R*\\s+)BY)", Pattern.CASE_INSENSITIVE);
	private String stripQueryLastOrderBy(final String query) {
		final Matcher matcher = PATTERN_ORDERBY.matcher(query);
		if (matcher.find()) {
			return query.substring(0, matcher.start(matcher.groupCount()));
		} else {
			return query;
		}
	}
	
	private String buildFilterDataQuery(final String databaseProductName, final String query, final FilterClause filterClause) {
		String where;
		if (filterClause.filterInfo.isQuery) {
			ArrayList<String> conditions = new ArrayList<>(filterClause.fields.size());
			for (String name : filterClause.fields) {
				conditions.add(
					"LOWER(\"" 
					+ DbUtils.escapeSQLIdentifier(name) 
					+ "\") LIKE '%" 
					+ StringUtils.lowerCase(DbUtils.escapeSQL(filterClause.filterInfo.value)) 
					+ "%'");
			}
			where = StringUtils.join(conditions, " OR ");
		} else {
			where = "\"" 
				+ DbUtils.escapeSQLIdentifier(filterClause.fields.iterator().next()) 
				+ "\" = '" + DbUtils.escapeSQL(filterClause.filterInfo.value) 
				+ "'";
		}
		String from = StringUtils.trim(stripQueryLastOrderBy(query));
		return "SELECT * FROM (" + from + ") AS __table__ WHERE " + where;
	}
	
	private String buildPaginationDataQuery(final String databaseProductName, final String query, final PageInfo pagination) {
		if (DBPRODUCT_MYSQL.equals(databaseProductName) || DBPRODUCT_MARIADB.equals(databaseProductName)) {
			return query
				+ " LIMIT " + String.valueOf(pagination.getPageSize())
				+ " OFFSET " + String.valueOf((pagination.getPageNumber()-1) * pagination.getPageSize());
		} else {
			return query
				+ " OFFSET " + String.valueOf((pagination.getPageNumber()-1) * pagination.getPageSize()) + " ROWS"
				+ " FETCH NEXT " + String.valueOf(pagination.getPageSize()) + " ROWS ONLY";
		}
	}
	
	private String toPoolName(final String domainId, final String dataSourceId) {
		return dataSourceId + "@" + domainId;
	}
	
	private DataSourcePooled.PoolStatus createPoolStatus(String poolName) {
		HikariDataSource pool = pools.get(poolName);
		if (pool == null) return null;
		//https://stackoverflow.com/questions/58489697/how-to-check-database-health-and-connection-pool-health
		HikariPool hp = extractHikariPool(pool);
		return new DataSourcePooled.PoolStatus(
			pool.getMaximumPoolSize(),
			hp != null ? hp.getActiveConnections() : null,
			hp != null ? (hp.getTotalConnections() - hp.getActiveConnections()) : null
		);
	}
	
	private HikariPool extractHikariPool(HikariDataSource hds) {
		try {
			java.lang.reflect.Field field = hds.getClass().getDeclaredField("pool");
			field.setAccessible(true);
			return (HikariPool)field.get(hds);
		} catch (Exception ex) {
			return null;
		}
	}
	
	private void doCheckDataSourceConnection(final JdbcConfig jdbcConfig) throws WTConnectionException, WTException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting connection for '{}'", jdbcConfig.url);
			LOGGER.debug("Properties:");
			dumpJdbcProps(jdbcConfig.info);
		}
		
		// Do connection test into a future due to lack of support for loginTimeout
		// in various JDBC drivers. Connection check will be manually 
		// cancelled after timeout expiration!
		Future<Boolean> future = dataSourceConnectionCheckers.submit(() -> {
			Connection con = null;
			try {
				con = DriverManager.getConnection(jdbcConfig.url, jdbcConfig.info);
				// --> Connection established successfully!
				return true;
			} catch (Exception ex) {
				// --> Connection failure!
				throw ex;
			} finally {
				DbUtils.closeQuietly(con);
			}
		});
		
		try {
			future.get(15, TimeUnit.SECONDS);
		} catch (ExecutionException ex) {
			throw ExceptionUtils.wrapThrowable(ex.getCause());
		} catch (TimeoutException ex) {
			throw new WTConnectionException("The connection to the host has failed. Error: \"Connection timeout\". Verify the connection properties. Make sure the DBMS servier is running on the host and accepting TCP/IP connections at the port. Make sure that TCP connections to the port are not blocked by a firewall.");
		} catch (CancellationException | InterruptedException ex) {
			throw new WTException("The connection to the host has failed. Error: \"Connection cancelled\"");
		}
	}
	
	private HikariConfig createHikariConfig(final DataSource dataSource) throws WTException {
		DataSourceType dsType = doDataSourceTypeGet(dataSource.getDomainId(), dataSource.getType());
		if (dsType == null) throw new WTException("Unsupported dataSource type [{}]", dataSource.getType());
		
		JdbcConfig jdbcConfig = createJdbcConfig(dsType, dataSource);
		Properties poolProps = new Properties();
		if (dataSource.getPoolProps() != null) poolProps.putAll(dataSource.getPoolProps());
		if (!poolProps.containsKey("maximumPoolSize")) poolProps.setProperty("maximumPoolSize", "3");
		
		HikariConfig config = new HikariConfig(poolProps);
		config.setConnectionTimeout(15000); // Lowers default connection timeout of 30s to not have problems with HTTP timeout
		config.setJdbcUrl(jdbcConfig.url);
		config.setDataSourceProperties(jdbcConfig.info);
		config.setReadOnly(true);
		return config;
	}
	
	private JdbcConfig createJdbcConfig(final DataSourceType dsType, final DataSource dataSource) {
		return createJdbcConfig(dsType, dataSource.getServerName(), dataSource.getServerPort(), dataSource.getDatabaseName(), dataSource.getUsername(), dataSource.getPassword(), dataSource.getDriverProps());
	}
	
	private JdbcConfig createJdbcConfig(final DataSourceType dsType, final String serverName, final Integer serverPort, final String databaseName, final String username, final String password, final Map<String, String> driverProps) {
		StringBuilder sb = new StringBuilder("jdbc:");
		sb.append(dsType.getProto());
		sb.append("://");
		sb.append(serverName);
		if (serverPort != null) {
			sb.append(":");
			sb.append(serverPort);
		}
		if (StringUtils.isBlank(dsType.getDatabasePropName())) {
			sb.append("/");
			sb.append(databaseName);
		}
		
		// Prepare driver properties
		Properties info = new Properties();
		if (!StringUtils.isBlank(dsType.getDatabasePropName()))info.put(dsType.getDatabasePropName(), databaseName);
		if (!StringUtils.isBlank(username)) info.put("user", username);
		if (!StringUtils.isBlank(password)) info.put("password", password);
		if (driverProps != null) info.putAll(driverProps);
		
		return new JdbcConfig(sb.toString(), info);
	}
	
	private LinkedHashMap<String, DataSourceType> doDataSourceTypesGet(final String domainId) {
		LinkedHashMap<String, DataSourceType> items = new LinkedHashMap<>();
		for (Map.Entry<String, DataSourceType> entry : WELLKNOWN_TYPES.entrySet()) {
			items.put(entry.getKey(), Cloner.standard().deepClone(entry.getValue()));
		}

		CoreServiceSettings css = new CoreServiceSettings(getWebTopApp().getSettingsManager(), CoreManifest.ID, domainId);
		for (DataSourceTypeObj dsto : css.getDataSourceAdditionalTypes()) {
			if (!StringUtils.isBlank(dsto.proto) && !StringUtils.isBlank(dsto.name)) {
				items.put(dsto.proto, new DataSourceType(dsto.proto, dsto.name, dsto.dialectMime, dsto.loadDriverClassName, dsto.databasePropName));
			}
		}
		return items;
	}
	
	private DataSourceType doDataSourceTypeGet(final String domainId, final String dataSourceType) {
		Map<String, DataSourceType> types = doDataSourceTypesGet(domainId);
		return types.get(dataSourceType);
	}
	
	private void encryptPassword(DataSourceBase dataSource) {
		String pass = dataSource.getPassword();
		if (!StringUtils.isBlank(pass)) {
			dataSource.setPassword(getWebTopApp().encryptData(pass));
		}
	}
	
	private void decryptPassword(DataSourceBase dataSource) {
		String pass = dataSource.getPassword();
		if (!StringUtils.isBlank(pass)) {
			dataSource.setPassword(getWebTopApp().decryptData(pass));
		}
	}
	
	private DataSource doDataSourceGet(final Connection con, final String domainId, final String dataSourceId) throws WTException {
		DataSourceDAO dsDao = DataSourceDAO.getInstance();
		
		ODataSource ods = dsDao.selectByIdDomain(con, dataSourceId, domainId);
		if (ods == null) return null;
		DataSourceType dsType = doDataSourceTypeGet(domainId, ods.getType());
		
		final String dialectMime = dsType != null ? dsType.getDialectMime() : null;
		final DataSource dataSource = fillDataSource(new DataSource(dialectMime), ods);
		decryptPassword(dataSource);
		
		return dataSource;
	}
	
	private DataSourceQuery doDataSourceQueryGet(final Connection con, final String domainId, final String queryId) throws WTException {
		DataSourceQueryDAO dsqDao = DataSourceQueryDAO.getInstance();
		
		ODataSourceQuery odsq = dsqDao.selectByIdDomain(con, queryId, domainId);
		if (odsq == null) return null;
		return fillDataSourceQuery(new DataSourceQuery(), odsq);
	}
	
	private void loadDriverQuietly(final String driverClassName) {
		if (!loadDriver(driverClassName)) {
			LOGGER.error("Unable to load driver class '{}'", driverClassName);
		}
	}
	
	private boolean loadDriver(final String driverClassName) {
		ClassLoader cl = LangUtils.findClassLoader(this.getClass());
		synchronized (loadedDrivers) {
			try {
				if (ClassUtils.isClassLoaded(cl, driverClassName)) {
					return true;
				} else {
					LOGGER.trace("Loading JDBC Driver '{}'...", driverClassName);
					Class<?> clazz = ClassUtils.classForNameQuietly(cl, driverClassName);
					if (clazz != null && ClassUtils.isClassInheritingFromParent(clazz, Driver.class)) {
						loadedDrivers.add(driverClassName);
						return true;
					} else {
						LOGGER.error("Unable to load driver class '{}'", driverClassName);
						return false;
					}
				}
			} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				return false;
			}
		}
	}
	
	private void unloadLoadedDrivers() {
		synchronized (loadedDrivers) {
			for (Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements(); ) {
				Driver driver = e.nextElement();
				if (loadedDrivers.contains(driver.getClass().getName())) {
					LOGGER.trace("Unloading JDBC Driver '{}'...", driver.getClass().getName());
					try {
						DriverManager.deregisterDriver(driver);
					} catch (SQLException ex) {
						LOGGER.error("Unable to deregister driver '{}'", driver.getClass().getName(), ex);
					}
				}
			}
		}
	}
	
	private static enum SetupPoolMode {
		ADD, UPDATE, ADD_QUIETLY; 
	}
	
	private void lockedSetupPool(final String domainId, final DataSource dataSource, final SetupPoolMode mode) throws DataSourceBase.WTPoolException, WTException {
		final String poolName = toPoolName(domainId, dataSource.getDataSourceId());
		try {
			poolLocks.lockInterruptibly(poolName);
			if (SetupPoolMode.UPDATE.equals(mode)) doRemovePool(poolName);
			try {
				if (!doAddPool(poolName, createHikariConfig(dataSource))) {
					if (!SetupPoolMode.ADD_QUIETLY.equals(mode)) {
						throw new WTException("Unable to create pool for data-source '{}'", dataSource.getDataSourceId());
					}
				}
			} catch (PoolInitializationException ex1) {
				throw new DataSourceBase.WTPoolException(ex1);
			}
		} catch (InterruptedException ex) {
			LOGGER.error("Unable to setup pool '{}'", poolName, ex);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			poolLocks.unlock(poolName);
		}
	}
	
	private boolean doAddPool(String poolName, HikariConfig config) throws PoolInitializationException {
		if (pools.containsKey(poolName)) {
			return false;
		} else {
			config.setPoolName(poolName); // Make sure name is poolName!
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Creating pool for '{}'", config.getJdbcUrl());
				LOGGER.debug("DataSource properties:");
				dumpJdbcProps(config.getDataSourceProperties());
			}
			pools.put(poolName, new HikariDataSource(config));
			return true;
		}
	}
	
	private boolean doRemovePool(String poolName) {
		HikariDataSource pool = pools.remove(poolName);
		if (pool == null) {
			return false;
		} else {
			pool.close();
			return true;
		}
	}
	
	private Connection doGetPoolConnection(String poolName) throws SQLException {
		HikariDataSource pool = pools.get(poolName);
		if (pool == null) {
			return null;
		} else {
			Connection con = pool.getConnection();
			con.setAutoCommit(true);
			return con;
		}
	}
	
	private static ODataSource fillODataSourceDefaultsForInsert(ODataSource tgt, DateTime defaultTimestamp) {
		if (tgt != null) {
			if (tgt.getRevisionTimestamp()== null) tgt.setRevisionTimestamp(defaultTimestamp);
		}
		return tgt;
	}
	
	private static ODataSource fillODataSource(ODataSource tgt, DataSourceBase src) {
		if ((tgt != null) && (src != null)) {
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setType(src.getType());
			tgt.setServerName(src.getServerName());
			tgt.setServerPort(src.getServerPort());
			tgt.setDatabaseName(src.getDatabaseName());
			tgt.setUsername(src.getUsername());
			tgt.setPassword(src.getPassword());
			tgt.setDriverRawProps(src.getDriverPropsAsString());
			tgt.setPoolRawProps(src.getPoolPropsAsString());
		}
		return tgt;
	}
	
	private static <T extends DataSource> T fillDataSource(T tgt, ODataSource src) {
		fillDataSource((DataSourceBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setDataSourceId(src.getDataSourceId());
			tgt.setDomainId(src.getDomainId());
		}
		return tgt;
	}
	
	private static <T extends DataSourceBase> T fillDataSource(T tgt, ODataSource src) {
		if ((tgt != null) && (src != null)) {
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setType(src.getType());
			tgt.setServerName(src.getServerName());
			tgt.setServerPort(src.getServerPort());
			tgt.setDatabaseName(src.getDatabaseName());
			tgt.setUsername(src.getUsername());
			tgt.setPassword(src.getPassword());
			tgt.setDriverProps(src.getDriverPropsMap());
			tgt.setPoolProps(src.getPoolPropsMap());
		}
		return tgt;
	}
	
	private static ODataSourceQuery fillODataSourceQueryDefaultsForInsert(ODataSourceQuery tgt, DateTime defaultTimestamp) {
		if (tgt != null) {
			if (tgt.getRevisionTimestamp()== null) tgt.setRevisionTimestamp(defaultTimestamp);
		}
		return tgt;
	}
	
	private static ODataSourceQuery fillODataSourceQuery(ODataSourceQuery tgt, DataSourceQueryBase src) {
		if ((tgt != null) && (src != null)) {
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setRawSql(src.getRawSql());
			tgt.setForcePagination(src.getForcePagination());
		}
		return tgt;
	}
	
	private static <T extends DataSourceQuery> T fillDataSourceQuery(T tgt, ODataSourceQuery src) {
		fillDataSourceQuery((DataSourceQueryBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setQueryId(src.getQueryId());
			tgt.setDataSourceId(src.getDataSourceId());
		}
		return tgt;
	}
	
	private static <T extends DataSourceQueryBase> T fillDataSourceQuery(T tgt, ODataSourceQuery src) {
		if ((tgt != null) && (src != null)) {
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setRawSql(src.getRawSql());
			tgt.setForcePagination(src.getForcePagination());
		}
		return tgt;
	}
	
	private void dumpJdbcProps(final Properties jdbcProps) {
		for (Enumeration<String> enums = (Enumeration<String>)jdbcProps.propertyNames(); enums.hasMoreElements();) {
			String key = enums.nextElement();
			if (!StringUtils.equalsIgnoreCase(key, "password")) {
				LOGGER.debug("  {}={}", key, jdbcProps.getProperty(key));
			} else {
				LOGGER.debug("  {}={}", key, PasswordUtils.printRedacted(jdbcProps.getProperty(key)));
			}
		}
	}
	
	private static class JdbcConfig {
		public final String url;
		public final Properties info;
		
		public JdbcConfig(String url, Properties info) {
			this.url = url;
			this.info = info;
		}
	}
	
	public static class QueryPlaceholders {
		public static final String CURRENT_DOMAIN_ID = "CURRENT_DOMAIN_ID";
		public static final String CURRENT_USER_ID = "CURRENT_USER_ID";
		public final Map<String, String> values;
		
		public QueryPlaceholders(String currentDomainId, String currentUserId) {
			this(currentDomainId, currentUserId, null);
		}
		
		public QueryPlaceholders(String currentDomainId, String currentUserId, Map<String, String> values) {
			Map<String, String> map = new LinkedHashMap<>();
			if (currentDomainId != null) map.put(CURRENT_DOMAIN_ID, currentDomainId);
			if (currentUserId != null) map.put(CURRENT_USER_ID, currentUserId);
			if (values != null) map.putAll(values);
			this.values = Collections.unmodifiableMap(map);
		}
	}
}
