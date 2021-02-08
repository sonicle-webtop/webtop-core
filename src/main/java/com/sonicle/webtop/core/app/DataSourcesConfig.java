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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.util.PropertyElf;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class DataSourcesConfig {
	private static final Logger logger = WT.getLogger(DataSourcesConfig.class);
	protected HashMap<String, HikariConfigMap> hm = new HashMap<>();
	
	public HikariConfigMap getSources(String serviceId) {
		return hm.get(serviceId);
	}
	
	public void parseConfiguration(File file) throws ConfigurationException {
		HikariConfigMap sources = null;
		String serviceId = null, sourceName = null;
		
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
			.configure(new Parameters()
				.xml()
				.setEncoding(StandardCharsets.UTF_8.name())
				.setFile(file)
			);
		XMLConfiguration config = builder.getConfiguration();
		
		List<HierarchicalConfiguration<ImmutableNode>> elServices = config.configurationsAt("service");
		for (HierarchicalConfiguration<ImmutableNode> elService : elServices) {
			serviceId = elService.getString("[@id]", null);
			if (serviceId == null) {
				logger.warn("Missing attribute [id] in [{}]", elService.toString());
				continue;
			}
			
			// Iterates over service' sources
			sources = new HikariConfigMap();
			List<HierarchicalConfiguration<ImmutableNode>> elSources = elService.configurationsAt("dataSource");
			for (HierarchicalConfiguration<ImmutableNode> elSource : elSources) {
				sourceName = elSource.getString("[@name]", ConnectionManager.DEFAULT_DATASOURCE);
				logger.trace("name: {}", sourceName);
				try {
					sources.put(sourceName, parseDataSource(elSource));
				} catch(ConfigurationException ex) {
					logger.warn("Error parsing dataSource definition [{}]", elSource.toString(), ex);
				}
			}
			
			hm.put(serviceId, sources);
		}
	}
	
	protected HikariConfig parseDataSource(HierarchicalConfiguration<ImmutableNode> dsEl) throws ConfigurationException {
		HikariConfig config = createHikariConfig();
		
		if (dsEl.containsKey("[@dataSourceClassName]")) { // Jdbc 4 configs
			config.setDataSourceClassName(dsEl.getString("[@dataSourceClassName]"));
			config.addDataSourceProperty("serverName", dsEl.getString("[@serverName]"));
			if (dsEl.containsKey("[@port]")) config.addDataSourceProperty("port", dsEl.getInt("[@port]"));
			config.addDataSourceProperty("databaseName", dsEl.getString("[@databaseName]"));
			
		} else if (dsEl.containsKey("[@driverClassName]")) { // Jdbc 3 configs
			config.setDriverClassName(dsEl.getString("[@driverClassName]"));
			config.setJdbcUrl(dsEl.getString("[@jdbcUrl]"));
		}
		
		if (dsEl.containsKey("[@username]")) config.setUsername(dsEl.getString("[@username]"));
		if (dsEl.containsKey("[@password]")) config.setPassword(dsEl.getString("[@password]"));
		
		if (!dsEl.isEmpty()) {
			List<HierarchicalConfiguration<ImmutableNode>> elProps = dsEl.configurationsAt("property");
			Properties props = new Properties();
			for (HierarchicalConfiguration<ImmutableNode> elProp : elProps) {
				if (elProp.containsKey("[@name]") && elProp.containsKey("[@value]")) {
					final String name = elProp.getString("[@name]");
					final String value = elProp.getString("[@value]");
					if (!StringUtils.isBlank(name)) {
						props.setProperty(name, value);
						logger.trace("property: {} -> {}", name, value);
					}
				}
			}
			PropertyElf.setTargetFromProperties(config, props);
		}
		
		
		// Common configs...
		/*
		if(dsEl.containsKey("[@autoCommit]")) config.setAutoCommit(dsEl.getBoolean("[@autoCommit]"));
		if(dsEl.containsKey("[@connectionTimeout]")) config.setConnectionTimeout(dsEl.getLong("[@connectionTimeout]"));
		if(dsEl.containsKey("[@idleTimeout]")) config.setIdleTimeout(dsEl.getLong("[@idleTimeout]"));
		if(dsEl.containsKey("[@maxLifetime]")) config.setMaxLifetime(dsEl.getLong("[@maxLifetime]"));
		if(dsEl.containsKey("[@minimumIdle]")) config.setMinimumIdle(dsEl.getInt("[@minimumIdle]"));
		if(dsEl.containsKey("[@maximumPoolSize]")) config.setMaximumPoolSize(dsEl.getInt("[@maximumPoolSize]"));
		*/
		
		return config;
	}
	
	protected HikariConfig createHikariConfig() {
		HikariConfig config = new HikariConfig();
		
		//TODO: applicare gli eventuali parametri di default e renderli dinamici
		// Our custom default configs
		//config.setMaximumPoolSize(10);
		//config.setMinimumIdle(config.getMaximumPoolSize());
		
		return config;
	}
	
	public class HikariConfigMap extends HashMap<String, HikariConfig> {
		public HikariConfigMap() {
			super();
		}
	}
}
