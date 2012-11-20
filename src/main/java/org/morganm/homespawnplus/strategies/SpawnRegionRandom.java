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
package org.morganm.homespawnplus.strategies;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.integration.worldguard.WorldGuardInterface;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.morganm.homespawnplus.util.Teleport;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/** Spawn at a random point inside of a named region.
 * 
 * @author morganm
 *
 */
public class SpawnRegionRandom extends BaseStrategy {
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
		if( wgInterface == null || !plugin.getWorldGuardIntegration().isEnabled() )
			return null;
		
		World theWorld = null;
		if( world != null ) {
			theWorld = Bukkit.getWorld(world);
			if( theWorld == null ) {
				log.warning(logPrefix+" found null value when looking for world: "+world);
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

		Teleport.Bounds yBounds = context.getTeleportOptions();
		if( yBounds == null )
			yBounds = Teleport.getInstance().getDefaultBounds();
		
		BlockVector bvMin = wgRegion.getMinimumPoint();
		// minimum Y never goes below yBounds
		int minY = bvMin.getBlockY();
		if( yBounds.minY > minY )
			minY = yBounds.minY;
		Location min = new Location(theWorld, bvMin.getBlockX(), minY, bvMin.getBlockZ());
		
		BlockVector bvMax = wgRegion.getMaximumPoint();
		// maximum Y never goes above yBounds
		int maxY = bvMax.getBlockY();
		if( yBounds.maxY > maxY )
			maxY = yBounds.maxY;
		Location max = new Location(theWorld, bvMax.getBlockX(), maxY, bvMax.getBlockZ());
		
		Location loc = plugin.getUtil().findRandomSafeLocation(min, max, yBounds, context.getModeSafeTeleportFlags());
		if( loc == null )
			return null;

		return new StrategyResult(loc);
	}
	
	@Override
	public void validate() throws StrategyException {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if( p == null )
			throw new StrategyException("Attempt to use "+getStrategyConfigName()+" strategy but WorldGuard is not installed");
		else {
			wgInterface = plugin.getWorldGuardIntegration().getWorldGuardInterface();
			
			// look for the region and print a warning if we didn't find one, so the admin
			// has a heads up they may have misconfigured the strategy
			boolean foundRegion = false;
			if( world == null ) {		// null world, then check all
				List<World> worlds = Bukkit.getWorlds();
				for(World world : worlds) {
					if( wgInterface.getWorldGuardRegion(world, region) != null )
						foundRegion = true;
				}
			}
			else {
				World bukkitWorld = Bukkit.getWorld(this.world);
				if( bukkitWorld == null )
					throw new StrategyException("Strategy "+getStrategyConfigName()+" references world \""+world+"\", which doesn't exist.");
				
				if( wgInterface.getWorldGuardRegion(bukkitWorld, region) != null )
					foundRegion = true;
			}

			if( !foundRegion ) {
				if( world != null )
					log.warning(logPrefix+" Strategy "+getStrategyConfigName()+" references region \""+region+"\" on world \""+world+"\", but no region by that name was found. Strategy will silently fail; this may be an error in your config");
				else
					log.warning(logPrefix+" Strategy "+getStrategyConfigName()+" references region \""+region+"\", but no region by that name was found in any world. Strategy will silently fail; this may be an error in your config");
			}
		}
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnRegionRandom";
	}

}
