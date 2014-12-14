/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.util;

import com.andune.minecraft.commonlib.server.api.Block;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.entity.Home;

/**
 * Utility methods related to manipulating beds in the environment or
 * player.
 * <p/>
 * This interface exists rather than a direct concrete class because there
 * is a circular dependency between BedUtils and HomeUtil. Guice can resolve
 * circular dependencies for us by using a temporary proxy, but only if
 * there is in interface involved to proxy.
 *
 * @author andune
 * @see com.andune.minecraft.hsp.util.BedUtilsImpl
 */
public interface BedUtils {

    /**
     * Find a bed starting at a given Block, up to maxDepth blocks away.
     *
     * @param l        the location to start the search
     * @param maxDepth maximum distance from original location to search
     * @return the location of the bed found or null if none found
     */
    public Location findBed(Block b, int maxDepth);

    /**
     * Although HSP records the exact location that a player is when clicking
     * on a bed (which allows the player to respawn exactly where they were),
     * the latest version of Bukkit actually check to see if the bed exists
     * at the players Bed location and print an annoying "Your home bed was
     * missing or obstructed" if there is not a block at the location.
     * <p/>
     * So this method takes a given location and finds the exact location of
     * the nearest bed and sets the Bukkit location there.
     *
     * @param player
     * @param l
     */
    public void setBukkitBedHome(Player player, Location l);

    /**
     * Look for a nearby bed to the given home.
     *
     * @param home
     * @return true if a bed is nearby, false if not
     */
    public boolean isBedNearby(Home home);

    /**
     * Called when player right-clicks on a bed. Includes 2-click protection mechanism, if enabled.
     *
     * @param p
     * @return true if the event should be canceled, false if not
     */
    public boolean doBedClick(Player player, Block bedBlock);

}