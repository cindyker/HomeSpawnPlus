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
package com.andune.minecraft.hsp.integration.worldguard;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Factory;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.commonlib.server.bukkit.BukkitLocation;
import com.andune.minecraft.commonlib.server.bukkit.BukkitWorld;
import com.sk89q.worldedit.BlockVector;
import org.bukkit.Bukkit;

import java.util.Iterator;

/**
 * This class exists to wrap WorldGuard functionality so that our plugin can
 * load/function without WorldGuard, since WorldGuard is not referenced in any
 * class but this one, and we take care to make this class a soft dependency
 * in any class that it is referenced from.
 *
 * @author andune
 */
public class WorldGuardInterface {
    private static final Logger log = LoggerFactory.getLogger(WorldGuardInterface.class);
    private static boolean worldGuardError = false;

    private final org.bukkit.plugin.Plugin plugin;
    private final Factory factory;

    public WorldGuardInterface(org.bukkit.plugin.Plugin plugin, Factory factory) {
        this.plugin = plugin;
        this.factory = factory;
//		this.SPAWN_PERM = new RegionGroupFlag("spawn-group", RegionGroupFlag.RegionGroup.MEMBERS);
    }

    /**
     * Return true if the given location is located in the region.
     *
     * @param l
     * @param regionName
     * @return
     */
    public boolean isLocationInRegion(Location l, String regionName) {
        ProtectedRegion region = getProtectedRegion(l.getWorld(), regionName);
        if (region != null)
            return region.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        else
            return false;
    }

    /**
     * This code adapted from WorldGuard class
     * com.sk89q.worldguard.bukkit.WorldGuardPlayerListener, method
     * onPlayerRespawn().
     * <p/>
     * This is because there is no API provided by WorldGuard to determine this externally
     * nor is there a reliable way for me to use Bukkit to call WorldGuard's onPlayerRespawn()
     * directly since HSP's use might not be in a respawn event (for example, HSP might be
     * using this strategy in a /spawn command).
     * <p/>
     * So I've had to duplicate/adapt the WorldGuard method directly into HSP in order to
     * accurately check whether or not WorldGuard would respond to the current location with
     * a region spawn.
     * <p/>
     * Code is current as of WorldGuard build #579 (WorldGuard 5.5.2), built Mar 12, 2012.
     *
     * @param player
     * @return
     */
    public Location getWorldGuardSpawnLocation(Location location) {
        log.debug("getWorldGuardSpawnLocation(): location={}", location);
        Location returnLoc = null;

        try {
            org.bukkit.plugin.Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
            if (p != null) {
                // we know it's a BukkitLocation object since this class is Bukkit-specific
                org.bukkit.Location bukkitLocation = ((BukkitLocation) location).getBukkitLocation();
                org.bukkit.World bukkitWorld = ((BukkitWorld) location.getWorld()).getBukkitWorld();

                com.sk89q.worldguard.bukkit.WorldGuardPlugin worldGuard = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) p;
                com.sk89q.worldguard.bukkit.ConfigurationManager cfg = worldGuard.getGlobalStateManager();
                com.sk89q.worldguard.bukkit.WorldConfiguration wcfg = cfg.get(bukkitWorld);

                log.debug("getWorldGuardSpawnLocation(): location={}, wcfg={}", location, wcfg);
                if (wcfg.useRegions) {
                    com.sk89q.worldedit.Vector pt = com.sk89q.worldguard.bukkit.BukkitUtil.toVector(bukkitLocation);
                    com.sk89q.worldguard.protection.managers.RegionManager mgr = worldGuard.getGlobalRegionManager().get(bukkitWorld);
                    com.sk89q.worldguard.protection.ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                    log.debug("getWorldGuardSpawnLocation(): wcfg.useRegion=true, set.size()={}", set.size());

                    for (Iterator<com.sk89q.worldguard.protection.regions.ProtectedRegion> i = set.iterator(); i.hasNext(); ) {
                        final com.sk89q.worldguard.protection.regions.ProtectedRegion region = i.next();
                        final com.sk89q.worldedit.Location teleportLocation = region.getFlag(com.sk89q.worldguard.protection.flags.DefaultFlag.SPAWN_LOC);

                        if (teleportLocation != null) {
                            org.bukkit.World world = Bukkit.getWorld(teleportLocation.getWorld().getName());
                            com.sk89q.worldedit.Vector pos = teleportLocation.getPosition();
                            returnLoc = factory.newLocation(world.getName(), pos.getX(), pos.getY(), pos.getZ(),
                                    teleportLocation.getYaw(), teleportLocation.getPitch());
                            log.debug("getWorldGuardSpawnLocation(): found returnLoc={}", returnLoc);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // we only print once to avoid spamming the log with errors, since this is possibly
            // a permanent condition (ie. admin chooses to run older version of WorldGuard that
            // this plugin is not compatible with)
            if (!worldGuardError) {
                worldGuardError = true;
                log.warn("Error trying to resolve WorldGuard spawn (this message will only print once): " + e.getMessage(), e);
            }
        }

        log.debug("getWorldGuardSpawnLocation(): exit, returnLoc=", returnLoc);
        return returnLoc;
    }

    public com.sk89q.worldguard.protection.regions.ProtectedRegion getWorldGuardRegion(World world, String regionName) {
        org.bukkit.World bukkitWorld = ((BukkitWorld) world).getBukkitWorld();
        try {
            org.bukkit.plugin.Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
            if (p != null) {
                com.sk89q.worldguard.bukkit.WorldGuardPlugin worldGuard = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) p;
                com.sk89q.worldguard.protection.managers.RegionManager mgr = worldGuard.getRegionManager(bukkitWorld);
                return mgr.getRegion(regionName);
            }
        } catch (Exception e) {
            log.warn("Error trying find WorldGuard region \"" + regionName + "\": " + e.getMessage(), e);
        }

        return null;
    }

    public ProtectedRegion getProtectedRegion(World world, String regionName) {
        com.sk89q.worldguard.protection.regions.ProtectedRegion wgRegion = getWorldGuardRegion(world, regionName);
        if (wgRegion != null)
            return new HSPProtectedRegion(world.getName(), wgRegion);
        else
            return null;
    }

    /**
     * Implementation of ProtectedRegion interface that wraps the WorldGuard
     * region and delegates calls to it.
     *
     * @author andune
     */
    public class HSPProtectedRegion implements ProtectedRegion {
        private final String worldName;
        private final com.sk89q.worldguard.protection.regions.ProtectedRegion worldGuardRegion;

        public HSPProtectedRegion(String worldName,
                                  com.sk89q.worldguard.protection.regions.ProtectedRegion worldGuardRegion) {
            this.worldGuardRegion = worldGuardRegion;
            this.worldName = worldName;
        }

        @Override
        public String getName() {
            return worldGuardRegion.getId();
        }

        @Override
        public Location getMinimumPoint() {
            BlockVector bv = worldGuardRegion.getMinimumPoint();
            return factory.newLocation(worldName, bv.getX(), bv.getY(), bv.getZ(), 0, 0);
        }

        @Override
        public Location getMaximumPoint() {
            BlockVector bv = worldGuardRegion.getMaximumPoint();
            return factory.newLocation(worldName, bv.getX(), bv.getY(), bv.getZ(), 0, 0);
        }

        public boolean contains(int x, int y, int z) {
            return worldGuardRegion.contains(x, y, z);
        }
    }
}
