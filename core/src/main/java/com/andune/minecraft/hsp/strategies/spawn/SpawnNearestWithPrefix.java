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
package com.andune.minecraft.hsp.strategies.spawn;

import java.util.Set;

import javax.inject.Inject;

import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.andune.minecraft.hsp.strategy.BaseStrategy;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyMode;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.strategy.StrategyResultImpl;

/**
 * Spawn at the nearest spawn to the players current location, but
 * it must match the defined prefix.
 * 
 * This allows, for example, the implementation of a graveyard concept
 * without risk that the player might spawn at the newplayer or default
 * spawns. All graveyard spawns could be named with the prefix "graveyard"
 * and that prefix used to restrict this strategy.
 * 
 * @author andune
 *
 */
@OneArgStrategy
public class SpawnNearestWithPrefix extends BaseStrategy {
    @Inject private SpawnDAO spawnDAO;

    private final String prefix;
    
    public SpawnNearestWithPrefix(final String prefix) {
        this.prefix = prefix;
    }

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		// simple algorithm for now, it's not called that often and we assume the list
		// of spawns is relatively small (ie. not in the hundreds or thousands).
		final Set<? extends Spawn> allSpawns = spawnDAO.findAllSpawns();
		final Location playerLoc = context.getEventLocation();
		
		final boolean excludeNewPlayerSpawn = context.isModeEnabled(StrategyMode.MODE_EXCLUDE_NEW_PLAYER_SPAWN);
		
		final String playerWorld = playerLoc.getWorld().getName();
		double shortestDistance = -1;
		Spawn closestSpawn = null;
		for(Spawn theSpawn : allSpawns) {
			// this fixes a bug in R5+ where non-loaded worlds apparently won't even
			// return valid location or world objects anymore. So we check the String
			// world values before we do anything else and skip worlds that the
			// player is not on.
			if( !playerWorld.equals(theSpawn.getWorld()) )
				continue;
			
			// skip newPlayerSpawn if so directed
			if( excludeNewPlayerSpawn && theSpawn.isNewPlayerSpawn() ) {
                log.debug("Skipped spawn choice {} because mode {} is enabled", theSpawn, StrategyMode.MODE_EXCLUDE_NEW_PLAYER_SPAWN);
				continue;
			}
			
            if( !theSpawn.getName().startsWith(prefix) ) {
                log.debug("Skipped spawn choice {} because prefix doesn't match {}", theSpawn, prefix);
                continue;
            }
            
			final Location theLocation = theSpawn.getLocation();
			if( theLocation.getWorld().equals(playerLoc.getWorld()) ) {	// must be same world
				double distance = theLocation.distance(playerLoc);
				if( distance < shortestDistance || shortestDistance == -1 ) {
					shortestDistance = distance;
					closestSpawn = theSpawn;
				}
			}
		}
		
		return new StrategyResultImpl(closestSpawn);
	}
}
