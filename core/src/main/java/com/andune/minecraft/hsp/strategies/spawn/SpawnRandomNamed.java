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
package com.andune.minecraft.hsp.strategies.spawn;

import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.strategy.*;

import javax.inject.Inject;
import java.util.Random;

/**
 * Spawn strategy to spawn a random strategy out of a named list
 *
 * @author andune
 */
@OneArgStrategy
public class SpawnRandomNamed extends BaseStrategy {
    protected Storage storage;

    @Inject
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    private Random random = new Random(System.currentTimeMillis());
    private String[] names;

    public SpawnRandomNamed(final String arg) {
        if (arg != null)
            this.names = arg.split(",");
    }

    @Override
    public void validate() throws StrategyException {
        if (names == null)
            throw new StrategyException("no named spawns given");

        for (int i = 0; i < names.length; i++) {
            Spawn spawn = storage.getSpawnDAO().findSpawnByName(names[i]);
            if (spawn == null)
                log.warn("strategy {} references named spawn \"{}\", which doesn't exist", getStrategyConfigName(), names[i]);
        }
    }

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        int number = random.nextInt(names.length);
        String name = names[number];

        Spawn spawn = storage.getSpawnDAO().findSpawnByName(name);
        if (spawn == null)
            log.warn("No spawn found for name \"{}\" for \"{}\" strategy", name, getStrategyConfigName());

        return new StrategyResultImpl(spawn);
    }

    @Override
    public String getStrategyConfigName() {
        return "spawnRandomNamed";
    }

}
