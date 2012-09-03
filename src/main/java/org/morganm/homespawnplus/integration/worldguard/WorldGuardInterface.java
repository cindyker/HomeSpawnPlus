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
package org.morganm.homespawnplus.integration.worldguard;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.util.Debug;

/** This class exists to wrap WorldGuard functionality so that our plugin can
 * load/function without WorldGuard, since WorldGuard is not referenced in any
 * class but this one, and we take care to make this class a soft dependency
 * in any class that it is referenced from.
 * 
 * @author morganm
 *
 */
public class WorldGuardInterface {
	private static final Logger log = HomeSpawnPlus.log;
	private static boolean worldGuardError = false;
	
	private final String logPrefix;
	private final HomeSpawnPlus plugin;
	
	public WorldGuardInterface(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.logPrefix = HomeSpawnPlus.logPrefix;
//		this.SPAWN_PERM = new RegionGroupFlag("spawn-group", RegionGroupFlag.RegionGroup.MEMBERS);
	}
	
	/** Return true if the given location is located in the region.
	 * 
	 * @param l
	 * @param regionName
	 * @return
	 */
	public boolean isLocationInRegion(org.bukkit.Location l, String regionName) {
		com.sk89q.worldguard.protection.regions.ProtectedRegion region = getWorldGuardRegion(l.getWorld(), regionName);
		if( region != null )
			return region.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		else
			return false;
	}
	
	/** This code adapted from WorldGuard class
	 *  com.sk89q.worldguard.bukkit.WorldGuardPlayerListener, method
	 *  onPlayerRespawn().
	 *  
	 *  This is because there is no API provided by WorldGuard to determine this externally
	 *  nor is there a reliable way for me to use Bukkit to call WorldGuard's onPlayerRespawn()
	 *  directly since HSP's use might not be in a respawn event (for example, HSP might be
	 *  using this strategy in a /spawn command).
	 *  
	 *  So I've had to duplicate/adapt the WorldGuard method directly into HSP in order to
	 *  accurately check whether or not WorldGuard would respond to the current location with
	 *  a region spawn.
	 *  
	 *  Code is current as of WorldGuard build #579 (WorldGuard 5.5.2), built Mar 12, 2012.
	 * 
	 * @param player
	 * @return
	 */
	public org.bukkit.Location getWorldGuardSpawnLocation(Player player) {
		Debug.getInstance().debug("getWorldGuardSpawnLocation(): player=",player);
		org.bukkit.Location loc = null;
		
		try {
			Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			if( p != null ) {
				com.sk89q.worldguard.bukkit.WorldGuardPlugin worldGuard = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) p;
				org.bukkit.Location location = player.getLocation();
	
				com.sk89q.worldguard.bukkit.ConfigurationManager cfg = worldGuard.getGlobalStateManager();
				com.sk89q.worldguard.bukkit.WorldConfiguration wcfg = cfg.get(player.getWorld());
	
				Debug.getInstance().debug("getWorldGuardSpawnLocation(): location=",location,", wcfg=",wcfg);
		        if (wcfg.useRegions) {
		        	com.sk89q.worldedit.Vector pt = com.sk89q.worldguard.bukkit.BukkitUtil.toVector(location);
		        	com.sk89q.worldguard.protection.managers.RegionManager mgr = worldGuard.getGlobalRegionManager().get(player.getWorld());
		            com.sk89q.worldguard.protection.ApplicableRegionSet set = mgr.getApplicableRegions(pt);
					Debug.getInstance().debug("getWorldGuardSpawnLocation(): wcfg.useRegion=true, set.size()=",set.size());

		            for(Iterator<com.sk89q.worldguard.protection.regions.ProtectedRegion> i = set.iterator(); i.hasNext();) {
		            	final com.sk89q.worldguard.protection.regions.ProtectedRegion region = i.next();
		            	final com.sk89q.worldedit.Location teleportLocation = region.getFlag(com.sk89q.worldguard.protection.flags.DefaultFlag.SPAWN_LOC);
		            	
		            	if( teleportLocation != null ) {
		            		org.bukkit.World world = Bukkit.getWorld(teleportLocation.getWorld().getName());
		            		com.sk89q.worldedit.Vector pos = teleportLocation.getPosition();
		            		loc = new org.bukkit.Location(world, pos.getX(), pos.getY(), pos.getZ(),
		            				teleportLocation.getYaw(), teleportLocation.getPitch());
							Debug.getInstance().debug("getWorldGuardSpawnLocation(): found loc=",loc);
		            		break;
		            	}
		            }
		            
//		            LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
//		            Vector spawn = set.getFlag(DefaultFlag.SPAWN_LOC, localPlayer);
//
//					Debug.getInstance().debug("getWorldGuardSpawnLocation(): wcfg.useRegion=true, spawn=",spawn);
//		            if (spawn != null) {
//		                loc = BukkitUtil.toLocation(player.getWorld(), spawn);
//		            }
		        }
		        
		        /* old code for pre-5.5 Worldguard
				if (wcfg.useRegions) {
					Vector pt = com.sk89q.worldguard.bukkit.BukkitUtil.toVector(location);
					RegionManager mgr = worldGuard.getGlobalRegionManager().get(player.getWorld());
					ApplicableRegionSet set = mgr.getApplicableRegions(pt);
	
					Vector spawn = set.getFlag(DefaultFlag.SPAWN_LOC);
	
					if (spawn != null) {
						RegionGroup group = set.getFlag(DefaultFlag.SPAWN_PERM);
						Location spawnLoc = BukkitUtil.toLocation(player.getWorld(), spawn);
	
						if (group != null) {
							LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
	
							if (RegionGroupFlag.isMember(set, group, localPlayer)) {
								loc = spawnLoc;
							}
						} else {
							loc = spawnLoc;
						}
					}
				}
				*/
			}
		}
		catch(Throwable e) {
			// we only print once to avoid spamming the log with errors, since this is possibly
			// a permanent condition (ie. admin chooses to run older version of WorldGuard that
			// this plugin is not compatible with)
			if( !worldGuardError ) {
				worldGuardError = true;
				log.warning(logPrefix + " Error trying to resolve WorldGuard spawn (this message will only print once): "+e.getMessage());
				e.printStackTrace();
			}
		}
		
		Debug.getInstance().debug("getWorldGuardSpawnLocation(): exit, loc=",loc);
		return loc;
	}
	
	public com.sk89q.worldguard.protection.regions.ProtectedRegion getWorldGuardRegion(World world, String regionName) {
		try {
			Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			if( p != null ) {
				com.sk89q.worldguard.bukkit.WorldGuardPlugin worldGuard = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) p;
				com.sk89q.worldguard.protection.managers.RegionManager mgr = worldGuard.getRegionManager(world);
				return mgr.getRegion(regionName);
			}
		}
		catch(Throwable e) {
			log.log(Level.WARNING, logPrefix + " Error trying find WorldGuard region \""+regionName+"\": "+e.getMessage(), e);
		}
		
		return null;
	}
}
