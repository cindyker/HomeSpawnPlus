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

import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.andune.minecraft.hsp.strategy.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

/**
 * Spawn at a random spawn point on the local world. For example, if
 * you have defined "spawn1", "spawn2" and "spawn3" on the local world,
 * this strategy will choose one of them at random.
 *
 * @author andune
 */
@NoArgStrategy
public class SpawnLocalRandom extends BaseStrategy {
    @Inject
    protected SpawnDAO spawnDAO;

    private Random random = new Random(hashCode());

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        Spawn spawn = null;

        final boolean excludeNewPlayerSpawn = context.isModeEnabled(StrategyMode.MODE_EXCLUDE_NEW_PLAYER_SPAWN);

        String playerLocalWorld = context.getEventLocation().getWorld().getName();
        Set<? extends Spawn> allSpawns = spawnDAO.findAllSpawns();
        ArrayList<Spawn> spawnChoices = new ArrayList<Spawn>(5);
        for (Spawn theSpawn : allSpawns) {
            // skip newPlayerSpawn if so directed
            if (excludeNewPlayerSpawn && theSpawn.isNewPlayerSpawn()) {
                log.debug("Skipped spawn choice {} because mode {} is enabled",
                        theSpawn, StrategyMode.MODE_EXCLUDE_NEW_PLAYER_SPAWN);
                continue;
            }

            if (playerLocalWorld.equals(theSpawn.getWorld())) {
                log.debug("SpawnLocalRandom: added spawn choice {}", theSpawn);
                spawnChoices.add(theSpawn);
            }
        }
        if (spawnChoices.size() > 0) {
            int randomChoice = random.nextInt(spawnChoices.size());
            spawn = spawnChoices.get(randomChoice);
            log.debug("SpawnLocalRandom: size = {}, randomChoice = {}, spawn = {}", spawnChoices.size(), randomChoice, spawn);
        }

        return new StrategyResultImpl(spawn);
    }

    @Override
    public String getStrategyConfigName() {
        return "spawnLocalRandom";
    }

}
