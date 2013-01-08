/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
 */
/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.server.api.command.CommandConfig;

/** This class is used to store state of commands configuration. This amounts to three
 * key pieces of data: any disabled commands, all custom defined commands and all
 * properties related to any custom defined commands.
 * 
 * @author andune
 *
 */
public class BukkitCommandConfig implements CommandConfig {
	private final Logger log = LoggerFactory.getLogger(BukkitCommandConfig.class);

	private Set<String> disabledCommands;
	private Map<String, Map<String, Object>> commandParams;
	private ConfigurationSection configSection;
	
	public BukkitCommandConfig() {
		emptyDefaults();
	}
	
	public void setConfigSection(ConfigurationSection configSection) {
	    this.configSection = configSection;
	}
	
	/** Setup empty default variables that do nothing (but avoid NPEs).
	 */
	private void emptyDefaults() {
		disabledCommands = new HashSet<String>();
		commandParams = new HashMap<String, Map<String, Object>>();
	}
	
	/* (non-Javadoc)
     * @see com.andune.minecraft.hsp.command.CommandConfigInterface#isDisabledCommand(java.lang.String)
     */
	@Override
    public boolean isDisabledCommand(String command) {
		if( disabledCommands.contains("*") )
			return true;
		else
			return disabledCommands.contains(command.toLowerCase());
	}
	
	/* (non-Javadoc)
     * @see com.andune.minecraft.hsp.command.CommandConfigInterface#getDefinedCommands()
     */
	@Override
    public Set<String> getDefinedCommands() {
		return commandParams.keySet();
	}
	
	/* (non-Javadoc)
     * @see com.andune.minecraft.hsp.command.CommandConfigInterface#getCommandParameters(java.lang.String)
     */
	@Override
    public Map<String, Object> getCommandParameters(String command) {
		Map<String, Object> ret = commandParams.get(command);
		
		// If null, create empty map and save it for future use 
		if( ret == null ) {
			ret = new HashMap<String, Object>();
			commandParams.put(command, ret);
		}
		
		return ret;
	}
	
	/* (non-Javadoc)
     * @see com.andune.minecraft.hsp.command.CommandConfigInterface#loadConfig(org.bukkit.configuration.ConfigurationSection)
     */
	@Override
    public void loadConfig() {
		if( configSection == null ) {
			emptyDefaults();
			return;
		}
		
		disabledCommands = new HashSet<String>();
		List<String> theList = configSection.getStringList("disabledCommands");
		if( theList != null ) {
			for(String s : theList) {
				disabledCommands.add(s.toLowerCase());
			}
		}
		else
		
		commandParams = new HashMap<String, Map<String, Object>>();
		Set<String> keys = configSection.getKeys(false);
		if( keys != null ) {
			for(String key : keys) {
				if( key.equals("disabledCommands") )
					continue;
				
				log.debug("loading config params for command {}",key);
				ConfigurationSection cmdSection = configSection.getConfigurationSection(key);
				if( cmdSection == null ) {
					log.warn("no parameters defined for command {}, skipping", key);
					continue;
				}
				
				Set<String> parameters = cmdSection.getKeys(false);
				if( parameters == null || parameters.size() == 0 ) {
					log.warn("no parameters defined for command {}, skipping", key);
					continue;
				}
				
				HashMap<String, Object> paramMap = new HashMap<String, Object>();
				commandParams.put(key,  paramMap);
				for(String param : parameters) {
					final Object val = cmdSection.get(param);
					log.debug("command {}; key={}, val={}", key, param, val);
					paramMap.put(param, val);
				}
			}
		}
	}

    /*
    public Set<String> getDisabledCommands() {
        return disabledCommands;
    }
    */
}
