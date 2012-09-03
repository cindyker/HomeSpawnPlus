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
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.HomeSpawnPlus;


/**
 * @author morganm
 *
 */
public class ConfigurationYAML extends YamlConfiguration implements Config {
	private static final Logger log = HomeSpawnPlus.log;
	private final String logPrefix; 
	
	private File file;
	private HomeSpawnPlus plugin;
	private boolean useExtendedFileAsDefault = false;
	
	public ConfigurationYAML(File file, HomeSpawnPlus plugin) {
		this.file = file;
		this.plugin = plugin;
		
		this.logPrefix = HomeSpawnPlus.logPrefix;
	}
	
	public void setExtendedFile(boolean state) { useExtendedFileAsDefault = state; }

	@Override
    public void load() throws ConfigException {
		// if no config exists, copy the default one out of the JAR file
		if( !file.exists() && !useExtendedFileAsDefault )
			copyConfigFromJar("config-basic.yml", file);
		if( !file.exists() )
			copyConfigFromJar("config.yml", file);
		
		try {
			super.load(file);
		}
		catch(Exception e) {
			throw new ConfigException(e);
		}
		
		if( getString("events.onGroupSpawnCommand") == null ) {
			log.info(logPrefix + " WARNING: old-style config found, you should look at config_defaults.yml and copy the latest config settings into your config.yml.");
		}

		loadDefaults();
			
		/*
		if( getString("core.eventPriority") == null ) {
			log.info(logPrefix + " old-style config found, moving and replacing with new default");
			file.renameTo(new File(file.toString() + ".old"));
			copyConfigFromJar("config.yml", file);
			
			try {
				super.load(file);
			}
			catch(Exception e) {
				throw new ConfigException(e);
			}
		}
		*/
		
//		loadConfiguration(file);
//		super.load();
    }

	private void loadDefaults() {
	    // Look for defaults in the jar
	    InputStream defConfigStream = plugin.getResource("config.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	 
	        setDefaults(defConfig);
	        options().copyDefaults(true);
	    }
	}
	
	/** Right now we don't allow updates in-game, so we don't do anything, because if we
	 * let it save, all the comments are lost.  In the future, I may allow in-game updates
	 * to the config file and this will just call super.save();
	 */
	@Override
	public void save() throws ConfigException {
		try {
			super.save(file);
		}
		catch(Exception e) {
			throw new ConfigException(e);
		}
	}
	
	/** Code adapted from Puckerpluck's MultiInv plugin.
	 * 
	 * @param string
	 * @return
	 */
    private void copyConfigFromJar(String fileName, File outfile) {
    	plugin.getJarUtils().copyConfigFromJar(fileName, outfile);
    }
    
    public List<String> getStringList(String path, List<String> def)
    {
    	List<String> list = null; 
    	if( isList(path) )
    	  list = super.getStringList(path);
    	
    	// no config value? set it to passed in default
    	if( list == null )
    		list = def;
    	
    	// still null? set it to an empty list
    	if( list == null )
    		list = new ArrayList<String>();
    	
    	return list;
    }
    
    /** Return the list of permissions that have strategies associated with them.
     * 
     * @return
     */
    @Override
    public Set<String> getPermStrategies() {
    	Set<String> permList;
    	
    	ConfigurationSection section = getConfigurationSection(ConfigOptions.SETTING_EVENTS_BASE
				+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE);
    	
    	if( section != null )
    		permList = section.getKeys(false);
    	else
    		permList = new HashSet<String>();
    	
    	return permList;
    }

    /** TODO: add caching so we aren't string->enum converting on every join/death
     * 
     */
    /*
	@Override
	public List<OldSpawnStrategy> getStrategies(String node) {
		List<OldSpawnStrategy> spawnStrategies = new ArrayList<OldSpawnStrategy>();
    	List<String> strategies = getStringList(node, null);

    	for(String s : strategies) {
    		try {
    			spawnStrategies.add(OldSpawnStrategy.mapStringToStrategy(s));
    		}
    		catch(ConfigException e) {
    			log.warning(e.getMessage());
    			e.printStackTrace();
    		}
    	}
    	
		return spawnStrategies;
	}
	*/
}
