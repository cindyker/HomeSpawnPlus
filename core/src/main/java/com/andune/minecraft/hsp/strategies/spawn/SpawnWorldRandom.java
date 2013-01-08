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

import javax.inject.Inject;

import com.andune.minecraft.hsp.integration.worldborder.WorldBorder;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorder.BorderData;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.server.api.Teleport;
import com.andune.minecraft.hsp.server.api.TeleportOptions;
import com.andune.minecraft.hsp.server.api.World;
import com.andune.minecraft.hsp.strategy.BaseStrategy;
import com.andune.minecraft.hsp.strategy.NoArgStrategy;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyException;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.strategy.StrategyResultImpl;

/** Spawn at a completely random spot on the local world, obeying WorldBorder
 * limits, if any.
 * 
 * @author andune
 *
 */
@NoArgStrategy
@OneArgStrategy
public class SpawnWorldRandom extends BaseStrategy {
    @Inject private WorldBorder worldBorder;
    @Inject private Server server;
    @Inject private Factory factory;
    @Inject private Teleport teleport;
    
	// most commonly will find a safe location in the first few tries, but we
	// will try up to 20 times just in case we get a random run of bad
	// locations
	private static final int MAX_TRIES = 20;
	private String world;
	
	public SpawnWorldRandom() {}
	public SpawnWorldRandom(final String arg) {
		this.world = arg;
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		World w = null;
		if( world != null ) { 
            w = server.getWorld(world);
			if( w == null ) {
				log.warn("found null value when looking for world: {}", world);
				return null;
			}
		}
		else
			w = context.getEventLocation().getWorld();
		
		TeleportOptions teleportOptions = context.getTeleportOptions();
		
//		Teleport.Bounds yBounds = context.getTeleportOptions();
//		if( yBounds == null )
//			yBounds = Teleport.getInstance().getDefaultBounds();
//		log.debug("SpawnWorldRandom() minY=",yBounds.minY,", maxY=",yBounds.maxY);
		
		Location result = null;
        if( worldBorder.isEnabled() ) {
			BorderData border = worldBorder.getBorderData(w.getName());
			
			double x = border.getX();
			double z = border.getZ();
			int radius = border.getRadius();
			
			Location min = factory.newLocation(w.getName(), x-radius, teleportOptions.getMinY(), z-radius, 0, 0);
			Location max = factory.newLocation(w.getName(), x+radius, teleportOptions.getMaxY(), z+radius, 0, 0);
			
			// we loop and try multiple times, because it's possible we randomly select
			// a location outside of the border. If so, we just loop and guess again.
			// This is because our random routine finds a random location within a
			// square region, while WorldBorder might have defined a circular region.
			int tries = 0;
			while( result == null && tries < MAX_TRIES ) {
				tries++;
				log.debug("SpawnWorldRandom: try=",tries);
				result = teleport.findRandomSafeLocation(min, max, teleportOptions);
				log.debug("SpawnWorldRandom: try=",tries,", result=",result);
				if( result != null ) {
				    // if the random result isn't located inside the border, then we
				    // null it out and loop again
				    if( !border.insideBorder(result) )
				        result = null;
				}
			}
			
			if( tries == MAX_TRIES )
				log.warn(getStrategyConfigName()+" exceeded "+MAX_TRIES+" tries trying to find random location, likely indicates a problem with your configuration");
		}
		// no WorldBorder? just assume default min/max of +/- 1000
		else {
			Location min = factory.newLocation(w.getName(), -1000, teleportOptions.getMinY(), -1000, 0, 0);
			Location max = factory.newLocation(w.getName(), 1000, teleportOptions.getMaxY(), 1000, 0, 0);
			int tries = 0;
			while( result == null && tries < MAX_TRIES ) {
				tries++;
				log.debug("SpawnWorldRandom: try=",tries);
				result = teleport.findRandomSafeLocation(min, max, teleportOptions);
				log.debug("SpawnWorldRandom: try=",tries,", result=",result);
			}

			if( tries == MAX_TRIES )
				log.warn(getStrategyConfigName()+" exceeded "+MAX_TRIES+" tries trying to find random location, likely indicates a problem with your configuration");
		}
		
		if( result ==  null )
			return null;
		
		return new StrategyResultImpl(result);
	}
	
	@Override
	public void validate() throws StrategyException {
		if( world != null ) {
			World w = server.getWorld(world);
			if( w == null )
				throw new StrategyException(getStrategyConfigName()+" tried to reference world \""+world+"\", which doesn't exist");
		}
		
		if( !worldBorder.isEnabled() )
			log.info("Using "+getStrategyConfigName()+" strategy but WorldBorder is not installed; assuming maximum of 1000");
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnWorldRandom";
	}

}
