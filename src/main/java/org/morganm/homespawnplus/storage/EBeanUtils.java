/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.HomeSpawnPlus;

/** Class which acts as an interface to load the "bukkit.yml" EBean settings
 * for the purpose of allowing SQLite schema upgrades, which are impossible otherwise
 * with just the Ajave EBeanInterface provided by Bukkit via the JavaPlugin class. 
 * 
 * @author morganm
 *
 */
public class EBeanUtils {
	private static EBeanUtils instance;
	
	private final HomeSpawnPlus plugin;
	private final Properties connectionProperties;
	private final YamlConfiguration configuration;

	private EBeanUtils(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.configuration = YamlConfiguration.loadConfiguration(new File("bukkit.yml"));
		connectionProperties = new Properties();
		connectionProperties.put("user", configuration.getString("database.username"));
		connectionProperties.put("password", configuration.getString("database.password"));
	}
	
	public static EBeanUtils getInstance() {
		if( instance == null ) {
			instance = new EBeanUtils(HomeSpawnPlus.getInstance());
		}
		
		return instance;
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
		input = input.replaceAll("\\{NAME\\}", plugin.getDescription().getName().replaceAll("[^\\w_-]", ""));
		return input;
	}}
