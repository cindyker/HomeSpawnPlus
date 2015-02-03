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
/**
 *
 */
package com.andune.minecraft.hsp.integration.dynmap;

import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.hsp.entity.Spawn;

/**
 * @author andune
 */
public class SpawnNamedLocation implements NamedLocation {
    private final Spawn spawn;
    private String name = null;

    public SpawnNamedLocation(final Spawn spawn) {
        this.spawn = spawn;
    }

    @Override
    public Location getLocation() {
        return spawn.getLocation();
    }

    @Override
    public String getName() {
        if (name == null) {
            name = spawn.getName();
            if (name == null) {
                name = spawn.getLocation().getWorld().getName() + " spawn";
            }
        }

        return name;
    }

    @Override
    public String getPlayerName() {
        return null;
    }

    @Override
    public boolean isEnabled(ConfigurationSection section) {
        if (spawn.isDefaultSpawn())
            return true;

        // named spawns are only shown if asked to do so
        if (section.getBoolean("include-named-spawns"))
            return true;

        // if it hasn't been true yet, then we're not supposed to show it
        return false;
    }
}
