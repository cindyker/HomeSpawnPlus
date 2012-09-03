/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
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
