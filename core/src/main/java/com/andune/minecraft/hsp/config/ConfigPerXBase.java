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
package com.andune.minecraft.hsp.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This class generically implements the "config-per-world"
 * and "config-per-permission" patterns for use by other
 * config objects.
 * 
 * @author morganm
 *
 */
public abstract class ConfigPerXBase<P extends PerPermissionEntry, W extends PerWorldEntry> extends ConfigBase {
    protected Map<String, P> perPermissionEntries = new LinkedHashMap<String, P>();
    protected Map<String, W> perWorldEntries = new LinkedHashMap<String, W>();

    @Override
    public void init() throws Exception {
        super.init();
        populatePerWorldEntries();
        populatePerPermissionEntries();
    }

    /**
     * Should be overridden by the subclass to instantiate a new
     * PerPermissionEntry object that is specific to the subclass.
     * 
     * @return
     */
    protected abstract P newPermissionEntry();

    /**
     * Should be overridden by the subclass to instantiate a new
     * PerWorldEntry object that is specific to the subclass.
     * 
     * @return
     */
    protected abstract W newWorldEntry();
    
    /**
     * Return the per-permissions entries map.
     * 
     * @return guaranteed not to be null
     */
    public final Map<String, P> getPerPermissionEntries() {
        return perPermissionEntries;
    }

    /**
     * Return the per-world entries map.
     * 
     * @return guaranteed not to be null
     */
    public final W getPerWorldEntry(String worldName) {
        return perWorldEntries.get(worldName);
    }

    /**
     * Method to process the config and load into memory any per-permissions
     * config data for simple and efficient use later.
     */
    protected void populatePerPermissionEntries() {
        final String base = "permission";

        // linkedHashMap to maintain the ordering as defined in the config file
        perPermissionEntries = new LinkedHashMap<String, P>();
        
        Set<String> entries = super.getKeys(base);
        for(String entryName : entries) {
            final String baseEntry = base + "." + entryName;
            
            P entry = newPermissionEntry();
            perPermissionEntries.put(entryName, entry);
            
            // grab permissions related to this entry
            List<String> permissions = super.getStringList(baseEntry+".permissions");
            
            // if no explicit permissions were defined, then setup the default ones
            if( permissions == null || permissions.size() == 0 ) {
                permissions = new ArrayList<String>(3);
                
                permissions.add("hsp."+getBasePath()+"."+entryName);    // add basePath permission
                permissions.add("hsp.entry."+entryName);                // add default entry permission
                permissions.add("group."+entryName);                    // add convenience group entry
            }

            // make map immutable to protect against external changes
            entry.setPermissions(Collections.unmodifiableList(permissions));

            // now look for individual settings
            for(String key : super.getKeys(baseEntry)) {
                if( key.equals("permissions") ) {   // skip, we already got these
                    continue;
                }
                
                // assign property
                entry.setValue(key, super.get(baseEntry+"."+key));
            }
            
            entry.finishedProcessing();
        }
        
        // make map immutable to protect against external changes
        perPermissionEntries = Collections.unmodifiableMap(perPermissionEntries);
    }

    /**
     * Method to process the config and load into memory any per-world
     * config data for simple and efficient use later.
     */
    protected void populatePerWorldEntries() {
        final String base = "world";

        // linkedHashMap to maintain the ordering as defined in the config file
        perWorldEntries = new LinkedHashMap<String, W>();
        
        Set<String> worlds = super.getKeys(base);
        for(String worldName : worlds) {
            final String baseEntry = base + "." + worldName;
            
            W entry = newWorldEntry();
            entry.setWorld(worldName);
            perWorldEntries.put(worldName, entry);

            // now look for individual settings
            for(String key : super.getKeys(baseEntry)) {
                // assign property
                entry.setValue(key, super.get(baseEntry+"."+key));
            }
            
            entry.finishedProcessing();
        }
        
        // make map immutable to protect against external changes
        perWorldEntries = Collections.unmodifiableMap(perWorldEntries);
    }
}
