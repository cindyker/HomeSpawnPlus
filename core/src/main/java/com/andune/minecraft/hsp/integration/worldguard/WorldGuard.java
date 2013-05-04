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
package com.andune.minecraft.hsp.integration.worldguard;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.integration.PluginIntegration;

/**
 * @author andune
 *
 */
public interface WorldGuard extends PluginIntegration {
    /**
     * Return a ProtectedRegion for a given world & region.
     * 
     * @param worldName
     * @param regionName
     * @return
     */
    public ProtectedRegion getProtectedRegion(World world, String regionName);
    
    /**
     * Given a location, determine if it is within any WorldGuard regions
     * and if so, if said region has the spawn flag set. If it does, then
     * return the defined spawn point.
     * 
     * @param location
     * @return
     */
    public Location getWorldGuardSpawnLocation(Location location);

    /**
     * Register interest in a specific region. This makes sure any underlying
     * server hooks are in place to respond to events for the region.
     * 
     * @param world
     * @param regionName
     */
    public void registerRegion(World world, String regionName);
    
    /**
     * Determine if a given location is located within a given region by name.
     * 
     * @param l
     * @param regionName
     * @return
     */
    public boolean isLocationInRegion(Location l, String regionName);
}
