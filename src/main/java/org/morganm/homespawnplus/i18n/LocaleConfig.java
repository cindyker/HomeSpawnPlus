/**
 * 
 */
package org.morganm.homespawnplus.i18n;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author morganm
 *
 */
public final class LocaleConfig {
	private final String locale;
	private final JavaPlugin plugin;
	private final File jarFile;
	private final String pluginBaseName;
	private final Logger logger;
	private final String logPrefix;
	
	/**
	 * 
	 * @param locale the locale, such as "en" or "en_us"
	 * @param plugin the Bukkit JavaPlugin object
	 * @param pluginBaseName plugin base name, such as "hsp" or "lwc"
	 * @param jarFile the jar file for this plugin
	 * @param logger the plugin's java.util.logging.Logger log. can be null 
	 * @param logPrefix prefix to use for logging. can be null 
	 */
	public LocaleConfig(final String locale, final JavaPlugin plugin,
			final String pluginBaseName, final File jarFile,
			final Logger logger, final String logPrefix) {
		this.locale = locale;
		this.plugin = plugin;
		this.pluginBaseName = pluginBaseName;
		this.jarFile = jarFile;
		this.logger = logger;
		this.logPrefix = logPrefix;
	}

	public File getJarFile() {
		return jarFile;
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}

	public String getPluginBaseName() {
		return pluginBaseName;
	}

	public String getLocale() {
		return locale;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public String getLogPrefix() {
		return logPrefix;
	}
}
