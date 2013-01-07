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
package com.andune.minecraft.hsp.strategies;

import java.util.List;

import javax.inject.Inject;


import com.andune.minecraft.hsp.integration.worldguard.WorldGuardInterface;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuardModule;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.server.api.Teleport;
import com.andune.minecraft.hsp.server.api.TeleportOptions;
import com.andune.minecraft.hsp.server.api.World;
import com.andune.minecraft.hsp.strategy.BaseStrategy;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyException;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.strategy.StrategyResultImpl;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/** Spawn at a random point inside of a named region.
 * 
 * Bukkit/WorldGuard-specific strategy, currently will not run on any
 * server other than Bukkit.
 * 
 * @author morganm
 *
 */
@OneArgStrategy
public class SpawnRegionRandom extends BaseStrategy {
    @Inject private WorldGuardModule worldGuard;
    @Inject private Teleport teleport;
    @Inject private Server server;
    @Inject private Factory factory;
    
	private String world;
	private String region;
	private WorldGuardInterface wgInterface;

	public SpawnRegionRandom(final String arg) {
		// assume default, arg passed is full region
		this.region = arg;
		
		// look for world distinguisher and change region as appropriate
		if( arg != null ) {
			int i = arg.indexOf(";");
			if( i != -1 ) {
				world = arg.substring(0, i);
				region = arg.substring(i+1, arg.length());
			}
		}
	}

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		if( wgInterface == null || !worldGuard.isEnabled() )
			return null;
		
		World theWorld = null;
		if( world != null ) {
			theWorld = server.getWorld(world);
			if( theWorld == null ) {
				log.warn("found null value when looking for world: {}", world);
				return null;
			}
		}
		else
			theWorld = context.getEventLocation().getWorld();
		
		ProtectedRegion wgRegion = wgInterface.getWorldGuardRegion(theWorld, region);
		
		// region could legitimately be null, for example if they die on a world where
		// the region doesn't exist. We silently fail as this might be intended behavior
		// that the admin expects. If it was a mistake, hopefully checks elsewhere in
		// this class will have alerted the admin to the potential mistake.
		if( region == null )
			return null;

		TeleportOptions teleportOptions = context.getTeleportOptions();
//		if( yBounds == null )
//			yBounds = Teleport.getInstance().getDefaultBounds();
		
		BlockVector bvMin = wgRegion.getMinimumPoint();
		// minimum Y never goes below yBounds
		int minY = bvMin.getBlockY();
		if( teleportOptions.getMinY() > minY )
			minY = teleportOptions.getMinY();
		Location min = factory.newLocation(theWorld.getName(), bvMin.getBlockX(), minY, bvMin.getBlockZ(), 0, 0);
		
		BlockVector bvMax = wgRegion.getMaximumPoint();
		// maximum Y never goes above yBounds
		int maxY = bvMax.getBlockY();
		if( teleportOptions.getMaxY() > maxY )
			maxY = teleportOptions.getMaxY();
		Location max = factory.newLocation(theWorld.getName(), bvMax.getBlockX(), maxY, bvMax.getBlockZ(), 0, 0);
		
		Location loc = teleport.findRandomSafeLocation(min, max, teleportOptions);
		if( loc == null )
			return null;

		return new StrategyResultImpl(loc);
	}
	
	@Override
	public void validate() throws StrategyException {
        if( !worldGuard.isEnabled() )
			throw new StrategyException("Attempt to use "+getStrategyConfigName()+" strategy but WorldGuard is not installed");
		else {
			wgInterface = worldGuard.getWorldGuardInterface();
			
			// look for the region and print a warning if we didn't find one, so the admin
			// has a heads up they may have misconfigured the strategy
			boolean foundRegion = false;
			if( world == null ) {		// null world, then check all
				List<World> worlds = server.getWorlds();
				for(World world : worlds) {
					if( wgInterface.getWorldGuardRegion(world, region) != null )
						foundRegion = true;
				}
			}
			else {
				World theWorld = server.getWorld(this.world);
				if( theWorld == null )
					throw new StrategyException("Strategy "+getStrategyConfigName()+" references world \""+world+"\", which doesn't exist.");
				
				if( wgInterface.getWorldGuardRegion(theWorld, region) != null )
					foundRegion = true;
			}

			if( !foundRegion ) {
				if( world != null )
					log.warn("Strategy {} references region \"{}\" on world \"{}\", but no region by that name was found. Strategy will silently fail; this may be an error in your config",
					        getStrategyConfigName(), region, world);
				else
					log.warn("Strategy {} references region \"{}\", but no region by that name was found in any world. Strategy will silently fail; this may be an error in your config",
					        getStrategyConfigName(), region);
			}
		}
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnRegionRandom";
	}

}
