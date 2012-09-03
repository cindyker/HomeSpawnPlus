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

import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

/** Our configuration which determines how a number of things about how this mod operates.
 * Intended to be used with ConfigOptions class for setting nodes as String keys.
 * 
 * @author morganm
 *
 */
public interface Config {
	/** Should be called after the config option is created to allow it to do any loading
	 * it needs to do.
	 * 
	 * @throws ConfigException
	 */
	public void load() throws ConfigException;
	
	/** Should be called whenever the config options change and you want them serialized
	 * out to the backing store.
	 * 
	 * @throws ConfigException
	 */
	public void save() throws ConfigException;
	
    /**
     * Gets a boolean for a given node. This will either return an boolean
     * or the default value. If the object at the particular location is not
     * actually a boolean, the default value will be returned.
     *
     * @param path path to node (dot notation)
     * @param def  default value
     * @return boolean or default
     */
    public boolean getBoolean(String node, boolean def);
    
    /**
     * Gets a string at a location. This will either return an String
     * or null, with null meaning that no configuration value exists at
     * that location. If the object at the particular location is not actually
     * a string, it will be converted to its string representation.
     *
     * @param path path to node (dot notation)
     * @return string or null
     */
    public String getString(String path);

    /**
     * Gets a string at a location. This will either return an String
     * or the default value. If the object at the particular location is not
     * actually a string, it will be converted to its string representation.
     *
     * @param path path to node (dot notation)
     * @param def  default value
     * @return string or default
     */
    public String getString(String path, String def);
    
    /**
     * Gets an integer at a location. This will either return an integer
     * or the default value. If the object at the particular location is not
     * actually a integer, the default value will be returned. However, other
     * number types will be casted to an integer.
     *
     * @param path path to node (dot notation)
     * @param def  default value
     * @return int or default
     */
    public int getInt(String path, int def);
    
    /**
     * Gets a list of strings. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. If an item in the list
     * is not a string, it will be converted to a string. The node must be
     * an actual list and not just a string.
     *
     * @param path path to node (dot notation)
     * @param def default value or null for an empty list as default
     * @return list of strings
     */
    public List<String> getStringList(String path, List<String> def);
    
    public ConfigurationSection getConfigurationSection(String node);
    
    /** Return the list of permissions that have strategies associated with them.
     * 
     * @return
     */
    public Set<String> getPermStrategies();
    
//    public List<OldSpawnStrategy> getStrategies(String node);
}
