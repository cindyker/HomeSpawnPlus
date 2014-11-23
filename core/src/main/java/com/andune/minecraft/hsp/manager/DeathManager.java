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
package com.andune.minecraft.hsp.manager;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;

import java.util.HashMap;

/**
 * Class to keep track of player death locations.
 *
 * @author andune
 */
public class DeathManager {
    final HashMap<Player, Location> lastDeathLocation = new HashMap<Player, Location>();

    public DeathManager() {
    }

    /**
     * Return the last known location a player died at. This could be null
     * if the player hasn't died yet.
     *
     * @param p the player
     * @return the last location the player died at, or null
     */
    public Location getLastDeathLocation(Player p) {
        return lastDeathLocation.get(p);
    }

    /**
     * Should be called when a player dies so we can record their location.
     *
     * @param p the player
     */
    public void playerDied(final Player p) {
        lastDeathLocation.put(p, p.getLocation());
    }
}
