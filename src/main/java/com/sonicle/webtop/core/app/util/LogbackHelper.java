/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import com.google.gson.annotations.SerializedName;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.PropUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.model.LoggerEntry;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.qualitycheck.Check;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.util.IOUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 *
 * @author malbinola
 */
public class LogbackHelper {
	public static final String LOGBACK_PROPERTIES_FILE = "logback.properties";
	public static final String PROP_APPENDER = "logback.webtop.log.appender";
	public static final String PROP_LOG_DIR = "logback.webtop.log.dir";
	public static final String PROP_LOG_FILE_BASENAME = "logback.webtop.log.file.basename";
	private static URL lastConfiguration = null;
	
	public static void printToSystemOut(String message, Object... arguments) {
		String date = DateTimeUtils.createYmdHmsFormatter(DateTimeZone.getDefault()).print(DateTimeUtils.now());
		System.out.println(date + " " + MessageFormatter.arrayFormat(message, arguments).getMessage());
	}
	
	public static String getLogFileBasename(Properties properties, String webappFullName) {
		String basename = PropUtils.isDefined(properties, WebTopProps.PROP_LOG_FILE_BASENAME) ? WebTopProps.getLogFileBasename(properties) : null;
		if (StringUtils.isBlank(basename)) basename = webappFullName;
		return basename;
	}
	
	public static void reloadConfiguration() throws JoranException {
		reloadConfiguration((LoggerContext)LoggerFactory.getILoggerFactory());
	}
	
	public static void reloadConfiguration(LoggerContext loggerContext) throws JoranException {
		if (lastConfiguration != null) {
			ContextInitializer ci = new ContextInitializer(loggerContext);
			loggerContext.reset();
			ci.configureByResource(lastConfiguration);
		}
	}
	
	public static void loadConfiguration(LoggerContext loggerContext, URL configurationUrl) throws JoranException {
		// https://stackoverflow.com/questions/24235296/how-to-define-logback-variables-properties-before-logback-auto-load-logback-xml
		// https://stackoverflow.com/questions/9320133/how-do-i-programmatically-tell-logback-to-reload-configuration
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(loggerContext);
		loggerContext.reset();
		jc.doConfigure(configurationUrl);
		lastConfiguration = configurationUrl;
	}
	
	public static void writeProperties(ClassLoader classLoader, Properties properties) throws IOException, URISyntaxException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(classLoader.getResource(LOGBACK_PROPERTIES_FILE).toURI()));
			properties.store(out, "");
		} finally {
			IOUtils.close(out);
		}
	}
	
	public static URL findURLOfCustomConfigurationFile(String webappsConfigDir, String webappFullName) {
		File file = null;
		if (!StringUtils.isBlank(webappsConfigDir)) {
			// Try to get file under: "/path/to/webappsConfig/myWebappFullName/logback.xml"
			file = new File(PathUtils.concatPathParts(webappsConfigDir, webappFullName, ContextInitializer.AUTOCONFIG_FILE));
			if (file.exists() && file.isFile()) {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e1) {}
			}
			
			// Try to get file under: "/path/to/webappsConfig/logback.xml"
			file = new File(PathUtils.concatPathParts(webappsConfigDir, ContextInitializer.AUTOCONFIG_FILE));
			if (file.exists() && file.isFile()) {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e1) {}
			}
		}
		return null;
	}
	
	public static URL findURLOfDefaultConfigurationFile(ClassLoader classLoader) {
		URL url = findConfigFileURLFromSystemProperties(classLoader);
		if (url != null) return url;
		return Loader.getResource(ContextInitializer.AUTOCONFIG_FILE, classLoader);
	}
	
	public static URL findConfigFileURLFromSystemProperties(ClassLoader classLoader) {
		String logbackConfigFile = OptionHelper.getSystemProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
		if (logbackConfigFile != null) {
			URL result = null;
			try {
				result = new URL(logbackConfigFile);
				return result;
			} catch (MalformedURLException e) {
				// so, resource is not a URL:
				// attempt to get the resource from the class path
				result = Loader.getResource(logbackConfigFile, classLoader);
				if (result != null) {
					return result;
				}
				File f = new File(logbackConfigFile);
				if (f.exists() && f.isFile()) {
					try {
						result = f.toURI().toURL();
					} catch (MalformedURLException e1) {}
				}
			}
		}
		return null;
	}
	
	public static LoggerEntry asLoggerEntry(ch.qos.logback.classic.Logger logger) {
		if (logger == null) return null;
		final LoggerEntry.Level effLevel = EnumUtils.forSerializedName(logger.getEffectiveLevel().levelStr, LoggerEntry.Level.class);
		return new LoggerEntry(logger.getName(), effLevel, null);
	}
	
	public static Map<String, Logger> getLoggers(boolean effective) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		
		if (effective) { // Only include loggers which explicit levels configured
			return loggerContext.getLoggerList().stream()
					.filter(item -> item.getLevel() != null)
					.collect(Collectors.toMap(item -> item.getName(), item -> item, (ov, nv) -> nv, LinkedHashMap::new));
		} else {
			return loggerContext.getLoggerList().stream()
					.collect(Collectors.toMap(item -> item.getName(), item -> item, (ov, nv) -> nv, LinkedHashMap::new));
		}
	}
	
	public static Map<String, Appender<ILoggingEvent>> getAppenders() {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		
		Map<String, Appender<ILoggingEvent>> appendersMap = new HashMap<>();
		for (Logger logger : loggerContext.getLoggerList()) {
			Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
			while (it.hasNext()) {
				Appender<ILoggingEvent> appender = it.next();
				if (!appendersMap.containsKey(appender.getName())) {
					appendersMap.put(appender.getName(), appender);
				}
			}
		}
		return appendersMap;
	}
	
	public static Set<File> getLogFiles() {
		HashSet<File> files = new HashSet<>();
		for (Appender<?> appender : getAppenders().values()) {
			if (appender instanceof FileAppender) {
				String path = ((FileAppender<?>) appender).getFile();
				 files.add(new File(path));
			}
		}
		return files;
	}
	
	public static File getLogFile(final String fileName) {
		Set<File> files = getLogFiles();
		for (File file : files) {
			if (file.getName().equals(fileName)) return file;
		}
		return null;
	}
	
	public static InputStream getLogFileStream(final String fileName, final long from, final long count) throws IOException {
		if (fileName.contains(File.pathSeparator) || fileName.contains("/")) {
			//log.warn("Cannot retrieve log files with path separators in their name");
			return null;
		}
		File file = getLogFile(fileName);
		if (file == null || !file.exists()) {
			//log.warn("Log file does not exist: {}", fileName);
			return null;
		}
		
		long fromByte = from;
		long bytesCount = count;
		if (count < 0) {
			bytesCount = Math.abs(count);
			fromByte = Math.max(0, file.length() - bytesCount);
		}
		
		InputStream input = new BufferedInputStream(new FileInputStream(file));
		if (fromByte == 0 && bytesCount >= file.length()) {
			return input;
		} else {
			input.skip(fromByte);
			return new BoundedInputStream(input, bytesCount);
		}
	}
	
	public static Map<String, LoggerNode> readIncludedLoggers(File includedFile) throws IOException {
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
			.configure(
				new Parameters()
					.xml()
					.setEncoding(StandardCharsets.UTF_8.name())
					.setFile(includedFile)
			);
		
		try {
			XMLConfiguration config = builder.getConfiguration();
			LinkedHashMap<String, LoggerNode> loggers = new LinkedHashMap<>();
			
			List<HierarchicalConfiguration<ImmutableNode>> loggerNodes = config.configurationsAt("logger");
			for (HierarchicalConfiguration<ImmutableNode> loggerNode : loggerNodes) {
				final String name = loggerNode.getString("[@name]");
				final LoggerEntry.Level level = EnumUtils.forSerializedName(loggerNode.getString("[@level]"), LoggerEntry.Level.class);
				loggers.put(name, new LoggerNode(name, level));
			}
			
			return loggers;
			
		} catch(ConfigurationException ex) {
			throw new IOException("Unable to read included file", ex);
		}
	}
	
	public static void writeIncludedLoggers(File includedFile, Collection<LoggerNode> loggers) throws IOException {
		boolean fileExist = includedFile.exists();
		
		XMLBuilderParameters params = new Parameters().xml().setEncoding(StandardCharsets.UTF_8.name());
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
			.configure(fileExist ? params.setFile(includedFile) : params);
		
		try {
			XMLConfiguration config = builder.getConfiguration();
			
			if (!StringUtils.equals(config.getRootElementName(), "included")) {
				config.setRootElementName("included");
			}
			
			if (fileExist) config.clearTree("logger");
			for (LoggerNode newLogger : loggers) {
				config.addProperty("logger(-1)[@name]", newLogger.name);
				config.addProperty("logger[@level]", EnumUtils.toSerializedName(newLogger.level));
			}
			
			if (fileExist) {
				builder.save();
			} else {
				new FileHandler(config).save(includedFile);
			}
			
		} catch(ConfigurationException ex) {
			throw new IOException("Unable to write included file", ex);
		}
	}
	
	public static void writeIncludedFile2(File file, Collection<LoggerNode> loggers, boolean preserveLoggers) throws IOException {
		boolean fileExist = file.exists();
		
		XMLBuilderParameters params = new Parameters().xml().setEncoding(StandardCharsets.UTF_8.name());
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
			.configure(fileExist ? params.setFile(file) : params);
		
		try {
			XMLConfiguration config = builder.getConfiguration();
			
			if (!StringUtils.equals(config.getRootElementName(), "included")) {
				config.setRootElementName("included");
			}
			
			// Process loggers already on file
			LinkedHashMap<String, LoggerNode> origLoggers = new LinkedHashMap<>();
			if (fileExist) {
				// Read them...
				List<HierarchicalConfiguration<ImmutableNode>> loggerNodes = config.configurationsAt("logger");
				for (HierarchicalConfiguration<ImmutableNode> loggerNode : loggerNodes) {
					final String name = loggerNode.getString("[@name]");
					final LoggerEntry.Level level = EnumUtils.forSerializedName(loggerNode.getString("[@level]"), LoggerEntry.Level.class);
					origLoggers.put(name, new LoggerNode(name, level));
				}
				
				// Clear them (if necessary)...
				if (!preserveLoggers) {
					config.clearTree("logger");
					for (LoggerNode origLogger : origLoggers.values()) {
						config.addProperty("logger(-1)[@name]", origLogger.name);
						config.addProperty("logger[@level]", EnumUtils.toSerializedName(origLogger.level));
					}
				}
			}
			
			// Process brand-new loggers
			for (LoggerNode newLogger : loggers) {
				if (origLoggers.containsKey(newLogger.name)) continue;
				config.addProperty("logger(-1)[@name]", newLogger.name);
				config.addProperty("logger[@level]", EnumUtils.toSerializedName(newLogger.level));
			}
			
			if (fileExist) {
				builder.save();
			} else {
				new FileHandler(config).save(file);
			}
			
		} catch(ConfigurationException ex) {
			throw new IOException("Unable to write included file", ex);
		}
	}
	
	public static class LoggerNode {
		public final String name;
		public final LoggerEntry.Level level;
		
		public LoggerNode(String name, String level) {
			this(name, EnumUtils.forSerializedName(level, LoggerEntry.Level.class));
		}
		
		public LoggerNode(String name, LoggerEntry.Level level) {
			this.name = Check.notNull(name, "name");
			this.level = level != null ? level : LoggerEntry.Level.OFF;
		}
	}
	
	public static enum Level {
		@SerializedName("TRACE") TRACE,
		@SerializedName("DEBUG") DEBUG,
		@SerializedName("INFO") INFO,
		@SerializedName("WARN") WARN,
		@SerializedName("ERROR") ERROR
	}
}
