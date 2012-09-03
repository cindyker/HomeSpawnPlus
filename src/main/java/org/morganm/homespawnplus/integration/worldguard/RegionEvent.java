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
package org.morganm.homespawnplus.integration.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * @author morganm
 *
 */
public abstract class RegionEvent extends Event {
    private String regionName;
    private String regionWorldName;
    private Player player;
    private Location to;

    /**
     * 
     * @param regionName the name of the region
     * @param world the world the region belongs to
     * @param player the player involved in this event
     * @param to the location the player is moving to
     */
    public RegionEvent(final String regionName, final String regionWorldName, final Player player, final Location to) {
    	this.regionName = regionName;
    	this.regionWorldName = regionWorldName;
    	this.player = player;
    	this.to = to;
    }
    
    public Player getPlayer() { return player; }
    public String getRegionName() { return regionName; }
    public String getRegionWorldName() { return regionWorldName; }
    public Location getTo() { return to; }
    public void setTo(Location to) { this.to = to; }
}
