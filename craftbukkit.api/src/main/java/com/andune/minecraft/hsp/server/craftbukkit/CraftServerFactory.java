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
package com.andune.minecraft.hsp.server.craftbukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/** Class to determine which version of CraftServer we need at
 * runtime.
 * 
 * @author andune
 *
 */
public class CraftServerFactory {
    private final Plugin plugin;
    private CraftServer craftServer;

    public CraftServerFactory(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Code heavily borrowed from mbaxter's AbstractionExamplePlugin.
     * 
     * @return
     */
    public CraftServer getCraftServer() {
        if( craftServer != null )
            return craftServer;
        
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        
        // Get full package string of CraftServer.
        // org.bukkit.craftbukkit.versionstring (or for pre-refactor, just org.bukkit.craftbukkit
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        // If the last element of the package was "craftbukkit" we are pre-refactor
        if (version.equals("craftbukkit")) {
            version = "pre";
        }
        try {
            final Class<?> clazz = Class.forName("com.andune.minecraft.hsp.server.craftbukkit." + version + ".CraftServerImpl");
            // Check if we have an implementation class at that location.
            if (CraftServer.class.isAssignableFrom(clazz)) { // Make sure it actually implements our interface
                craftServer = (CraftServer) clazz.getConstructor().newInstance();
            }
        }
        catch (Exception e) {
        }
        
        if( craftServer == null )
            craftServer = new CraftServerNotAvailable(plugin);
        
        return craftServer;
    }
}