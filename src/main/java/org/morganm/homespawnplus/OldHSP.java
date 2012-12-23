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
package org.morganm.homespawnplus;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.config.old.Config;
import org.morganm.homespawnplus.config.old.ConfigFactory;
import org.morganm.homespawnplus.config.old.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.i18n.Colors;
import org.morganm.homespawnplus.i18n.Locale;
import org.morganm.homespawnplus.i18n.LocaleConfig;
import org.morganm.homespawnplus.i18n.LocaleFactory;
import org.morganm.homespawnplus.integration.dynmap.DynmapModule;
import org.morganm.homespawnplus.integration.multiverse.MultiverseIntegration;
import org.morganm.homespawnplus.integration.worldguard.WorldGuardIntegration;
import org.morganm.homespawnplus.listener.HSPEntityListener;
import org.morganm.homespawnplus.listener.HSPPlayerListener;
import org.morganm.homespawnplus.listener.HSPWorldListener;
import org.morganm.homespawnplus.manager.CooldownManager;
import org.morganm.homespawnplus.manager.HomeInviteManager;
import org.morganm.homespawnplus.manager.WarmupManager;
import org.morganm.homespawnplus.server.api.command.CommandConfig;
import org.morganm.homespawnplus.server.api.command.CommandRegister;
import org.morganm.homespawnplus.server.bukkit.command.BukkitCommandConfig;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.StorageFactory;
import org.morganm.homespawnplus.storage.ebean.StorageEBeans;
import org.morganm.homespawnplus.strategy.StrategyEngine;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.General;
import org.morganm.homespawnplus.util.JarUtils;
import org.morganm.homespawnplus.util.PermissionSystem;

/**
 * HomeSpawnPlus plugin for Bukkit.
 *
 * @author morganm
 */
public class OldHSP extends JavaPlugin {
    
    /** Routine to detect other plugins that use the same commands as HSP and
     * often cause conflicts and create confusion.
     * 
     */
    private void detectAndWarn() {
    	// do nothing if warning is disabled
    	if( !getConfig().getBoolean(ConfigOptions.WARN_CONFLICTS, true) )
    		return;
    	
    	if( getServer().getPluginManager().getPlugin("Essentials") != null ) {
    		log.warning(logPrefix+" Essentials found. It is likely your HSP /home and /spawn commands will"
    				+ " end up going to Essentials instead.");
    		log.warning(logPrefix+" Also note that HSP can convert your homes from Essentials for you. Just"
    				+ " run the command \"/hspconvert essentials\" (must have hsp.command.admin permission)");
    		log.warning(logPrefix+" Set \"core.warnConflicts\" to false in your HSP config.yml to disable"
    				+ " this warning.");
    	}
    	
    	if( getServer().getPluginManager().getPlugin("CommandBook") != null ) {
    		log.warning(logPrefix+" CommandBook found. It is likely your HSP /home and /spawn commands will"
    				+ " end up going to CommandBook instead. Please add \"homes\" and"
    				+ " \"spawn-locations\" to your CommandBook config.yml \"components.disabled\" section.");
    		log.warning(logPrefix+" Set \"core.warnConflicts\" to false in your HSP config.yml to disable"
    				+ " this warning.");
    	}
    }

    
    
    /** Load our data from the backing data store.
     * 
     * @throws IOException
     * @throws StorageException
     */
    public void initializeDatabase() throws IOException, StorageException {
    	Debug.getInstance().devDebug("TRACE: BEGIN initializeDatabase");
    	
    	StorageFactory.Type type = null;
    	
    	String stringType = config.getString(ConfigOptions.STORAGE_TYPE, "EBEANS");
    	int intType = -1;
    	// backwards compatibility means it might be an integer,
    	// so look for that
    	try {
    		intType = Integer.valueOf(stringType);
    	}
    	catch(NumberFormatException e) {}	// ignore, we don't care
    	
    	if( intType != -1 )
    		type = StorageFactory.getType(intType);
    	else
    		type = StorageFactory.getType(stringType);
    		
    	Debug.getInstance().debug("using storage type ",type);
        storage = StorageFactory.getInstance(type, this);
        
        // Make sure storage system is initialized
        storage.initializeStorage();
        
        // TODO: possibly pre-cache the data here later
    	Debug.getInstance().devDebug("TRACE: END initializeDatabase");
    }
    
    private void updateConfigDefaultFile() {
		// make sure the config_defaults.yml file is always up-to-date. We do this by just 
		// deleting it and then letting the Config initialization code just copy it out
    	// of the JAR file on to disk.
		try {
			File configDefaultsFile = new File(YAML_CONFIG_ROOT_PATH+"config_defaults.yml");
			configDefaultsFile.delete();
			Debug.getInstance().devDebug("copying config_defaults.yml into place");
			ConfigFactory.getInstance(ConfigFactory.Type.YAML_EXTENDED_DEFAULT_FILE, this, YAML_CONFIG_ROOT_PATH+"config_defaults.yml").load();
		}
		catch(Exception e) {
			// we don't care if this fails, ignore any errors
		}
    }
    
    public void loadConfig(boolean processStrategies) throws ConfigException, IOException {
    	if( config == null )
    		config = ConfigFactory.getInstance(ConfigFactory.Type.YAML, this, YAML_CONFIG_ROOT_PATH+"config.yml");
		config.load();
		Debug.getInstance().setDebug(config.getBoolean(ConfigOptions.DEV_DEBUG, false), Level.FINEST);
		Debug.getInstance().setDebug(config.getBoolean(ConfigOptions.DEBUG, false));

		// also load/reload Locale
		LocaleConfig localeConfig = new LocaleConfig(
				config.getString("core.locale", "en"), this, "hsp", getFile(),
				log, logPrefix);
		locale = LocaleFactory.getLocale(localeConfig);
		Colors.setDefaultColor(config.getString("core.defaultMessageColor", "%yellow%"));
		
		if( processStrategies )
			processStrategyConfig();
    	
    	General.getInstance().setLocale(getLocale());
    }
    
    public void installDatabaseDDL() {
        installDDL();
    }
    
    public boolean hasPermission(String worldName, String playerName, String permissionNode) {
    	boolean result = perms.has(worldName, playerName, permissionNode);
    	
    	// if using OPS system, support legacy HSP "defaultPermission" setting
    	if( !result && perms.getSystemInUse() == PermissionSystem.Type.OPS ) {
    		List<String> defaultPerms = config.getStringList(ConfigOptions.DEFAULT_PERMISSIONS, null);
    		if( defaultPerms.contains(permissionNode) )
    			result = true;
    	}
    	return result;
    }
    
   /** Return true if the given player has access to the given permission node.
     * If we aren't using a Permission system, then defaults to op check - ops have
     * full access to all permissions.
     * 
     * @param p
     * @param permissionNode
     * @return
     */
    public boolean hasPermission(CommandSender sender, String permissionNode) {
    	boolean result = perms.has(sender, permissionNode);
    	
    	// if using OPS system, support legacy HSP "defaultPermission" setting
    	if( !result && perms.getSystemInUse() == PermissionSystem.Type.OPS ) {
    		List<String> defaultPerms = config.getStringList(ConfigOptions.DEFAULT_PERMISSIONS, null);
    		if( defaultPerms.contains(permissionNode) )
    			result = true;
    	}
    	return result;
    }
}
