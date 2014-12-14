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
package com.andune.minecraft.hsp.integration.worldguard;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Abstraction layer for WorldGuardInterface. This allows this class (and
 * therefore HSP) to load even if WorldGuard isn't installed on the server.
 *
 * @author andune
 */
@Singleton
public class WorldGuardModule implements WorldGuard, Initializable {
    private final Plugin plugin;
    private final BukkitFactory factory;
    private final StrategyEngine strategyEngine;
    private WorldGuardInterface worldGuardInterface;
    private RegionListener worldGuardRegion;
    private Server server;

    @Inject
    public WorldGuardModule(Plugin plugin, BukkitFactory factory,
                            StrategyEngine strategyEngine, Server server) {
        this.plugin = plugin;
        this.factory = factory;
        this.strategyEngine = strategyEngine;
    }

    @Override
    public boolean isEnabled() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (p != null)
            return p.isEnabled();
        else
            return false;
    }

    @Override
    public String getVersion() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (p != null)
            return p.getDescription().getVersion();
        else
            return null;
    }

    @Override
    public void init() {
        if (!isEnabled())
            return;

        worldGuardInterface = new WorldGuardInterface(plugin, factory);
        worldGuardRegion = new RegionListener(plugin, this, factory, strategyEngine, server);
    }

    @Override
    public void shutdown() throws Exception {
        worldGuardInterface = null;
        worldGuardRegion = null;
    }

    @Override
    public int getInitPriority() {
        return 9;
    }

    @Override
    public ProtectedRegion getProtectedRegion(World world, String regionName) {
        return worldGuardInterface.getProtectedRegion(world, regionName);
    }

    @Override
    public Location getWorldGuardSpawnLocation(Location location) {
        return worldGuardInterface.getWorldGuardSpawnLocation(location);
    }

    @Override
    public void registerRegion(World world, String regionName) {
        worldGuardRegion.registerRegion(world, regionName);
    }

    @Override
    public boolean isLocationInRegion(Location l, String regionName) {
        return worldGuardInterface.isLocationInRegion(l, regionName);
    }

    /**
     * for internal package use
     */
    protected WorldGuardInterface getWorldGuardInterface() {
        return worldGuardInterface;
    }
}
