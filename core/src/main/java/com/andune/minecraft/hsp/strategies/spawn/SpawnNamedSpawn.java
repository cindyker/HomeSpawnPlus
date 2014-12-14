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
package com.andune.minecraft.hsp.strategies.spawn;

import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.strategy.*;

import javax.inject.Inject;
import java.util.List;

/**
 * @author andune
 */
@NoArgStrategy
@OneArgStrategy
public class SpawnNamedSpawn extends BaseStrategy {
    @Inject
    protected Server server;

    protected Storage storage;

    @Inject
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    private String namedSpawn;

    public SpawnNamedSpawn() {
    }

    public SpawnNamedSpawn(final String namedSpawn) {
        this.namedSpawn = namedSpawn;
    }

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        // take the name from the argument, if given
        String name = context.getArg();
        // otherwise use the name given at instantiation
        if (name == null)
            name = namedSpawn;

        Spawn spawn = storage.getSpawnDAO().findSpawnByName(name);

        // try by ID number if name didn't work
        if (spawn == null) {
            int id = -1;

            String idString = name;
            if (idString.startsWith("id:")) {
                idString = idString.substring(3);
            }

            try {
                id = Integer.valueOf(idString);
            } catch(NumberFormatException e) {} // ignore

            if (id > -1) {
                spawn = storage.getSpawnDAO().findSpawnById(id);
            }
        }

        // last, see if a world matches the arg and if so, use the default
        // spawn of that world
        if (spawn == null) {
            List<World> worlds = server.getWorlds();
            for(World w : worlds) {
                if (w.getName().equalsIgnoreCase(name)) {
                    spawn = storage.getSpawnDAO().findSpawnByWorld(w.getName());
                    break;
                }
            }
        }

        // since namedSpawn is very specific, it's usually an error condition if we didn't
        // find a named spawn that the admin identified, so print a warning so they can
        // fix the issue.
        if (spawn == null)
            log.warn("No spawn found for name \"{}\" for \"{}\" strategy", name, getStrategyConfigName());

        return new StrategyResultImpl(spawn);
    }

    @Override
    public String getStrategyConfigName() {
        return "spawnNamedSpawn";
    }

}
