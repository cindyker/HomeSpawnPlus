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
package com.andune.minecraft.hsp.storage.ebean;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;

import com.andune.minecraft.hsp.server.api.Plugin;

/** Class which acts as an interface to load the "bukkit.yml" EBean settings
 * for the purpose of allowing SQLite schema upgrades, which are impossible otherwise
 * with just the Ajave EBeanInterface provided by Bukkit via the JavaPlugin class. 
 * 
 * @author morganm
 *
 */
@Singleton
public class EBeanUtils {
	private final Plugin plugin;
	private final Properties connectionProperties;
	private final YamlConfiguration configuration;

	@Inject
	private EBeanUtils(Plugin plugin) {
		this.plugin = plugin;
		this.configuration = YamlConfiguration.loadConfiguration(new File("bukkit.yml"));
		connectionProperties = new Properties();
		connectionProperties.put("user", configuration.getString("database.username"));
		connectionProperties.put("password", configuration.getString("database.password"));
	}
	
	public String getDriver() {
		return configuration.getString("database.driver");
	}
	public String getUrl() {
		return configuration.getString("database.url");
	}
	public String getUsername() {
		return configuration.getString("database.username");
	}
	public String getPassword() {
		return configuration.getString("database.password");
	}
	public String getIsolation() {
		return configuration.getString("database.isolation");
	}
	public Boolean getLogging() {
		return configuration.getBoolean("database.logging", false);
	}
	public Boolean getRebuild() {
		return configuration.getBoolean("database.rebuild", false);
	}

	public boolean isSqlLite() {
		return configuration.getString("database.driver").contains("sqlite");
	}
	
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(replaceDatabaseString(
				configuration.getString("database.url")),
				connectionProperties);
	}

	private String replaceDatabaseString(String input) {
		input = input.replaceAll("\\{DIR\\}", plugin.getDataFolder().getPath().replaceAll("\\\\", "/") + "/");
		input = input.replaceAll("\\{NAME\\}", plugin.getName().replaceAll("[^\\w_-]", ""));
		return input;
	}}
