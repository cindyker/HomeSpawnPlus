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

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.commonlib.server.bukkit.BukkitLocation;
import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;
import com.andune.minecraft.hsp.strategy.EventType;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.*;

/**
 * Class to monitor for enter/exit to registered WorldGuard regions.
 *
 * @author andune
 */
public class RegionListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(RegionListener.class);

    private final Plugin plugin;
    private final WorldGuardModule worldGuard;
    private final BukkitFactory factory;
    private final StrategyEngine strategyEngine;
    private final Server server;
    private final Map<String, Set<ProtectedRegion>> registered = new HashMap<String, Set<ProtectedRegion>>();
    /* Set to keep track of any "global" registers that weren't tied to a specific world, so
     * we can lookup the original strategies that way.
     */
    private final Set<String> globalRegisters = new HashSet<String>();
    private boolean eventsRegistered = false;

    public RegionListener(Plugin plugin, WorldGuardModule worldGuard, BukkitFactory factory,
                          StrategyEngine strategyEngine, Server server) {
        this.plugin = plugin;
        this.worldGuard = worldGuard;
        this.factory = factory;
        this.strategyEngine = strategyEngine;
        this.server = server;
    }

    public void registerRegion(World world, String regionName) {
        if (!eventsRegistered)
            registerEvents();

        log.debug("registerRegion(): world={}, region={}", world, regionName);
        // if world argument is null, invoke no-argument version of this method,
        // which will register on all worlds
        if (world == null) {
            registerRegion(regionName);
            return;
        }

        final String worldName = world.getName();

        ProtectedRegion region = worldGuard.getWorldGuardInterface().getWorldGuardRegion(world, regionName);
        log.debug("registerRegion(): region={}", region);
        if (region != null) {
            Set<ProtectedRegion> set = registered.get(worldName);
            if (set == null) {
                set = new HashSet<ProtectedRegion>();
                registered.put(worldName, set);
            }

            set.add(region);
        }
    }

    public void registerRegion(String regionName) {
        globalRegisters.add(regionName);
        List<World> worlds = server.getWorlds();
        for (World world : worlds) {
            registerRegion(world, regionName);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // If we didn't move a block, don't do anything
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Location to = event.getTo();
        Vector toVector = new Vector(to.getX(), to.getY(), to.getZ());
        String toWorld = to.getWorld().getName();

        Location from = event.getFrom();
        Vector fromVector = new Vector(from.getX(), from.getY(), from.getZ());
        String fromWorld = from.getWorld().getName();

        Set<ProtectedRegion> set = registered.get(fromWorld);
        if (set != null) {
            for (ProtectedRegion region : set) {
//				log.devDebug("checking region ",region);
                // are we leaving the region?
                if (region.contains(fromVector) && !region.contains(toVector)) {
                    RegionExitEvent regionEvent = new RegionExitEvent(region.getId(), fromWorld, event.getPlayer(), to);
                    plugin.getServer().getPluginManager().callEvent(regionEvent);
                    event.setTo(regionEvent.getTo());
                    break;    // stop processing once we've found one
                }
            }
        }

        set = registered.get(toWorld);
        if (set != null) {
            for (ProtectedRegion region : set) {
                // are we entering the region?
                if (region.contains(toVector) && !region.contains(fromVector)) {
                    RegionEnterEvent regionEvent = new RegionEnterEvent(region.getId(), toWorld, event.getPlayer(), to);
                    plugin.getServer().getPluginManager().callEvent(regionEvent);
                    event.setTo(regionEvent.getTo());
                    break;    // stop processing once we've found one
                }
            }
        }
    }

    private Location processEvent(RegionEvent e, EventType baseEventType) {
        String regionName = e.getRegionName();
        String worldName = "," + e.getRegionWorldName();
        if (globalRegisters.contains(regionName))
            worldName = "";

        String eventType = baseEventType.toString() + ";" + regionName + worldName;
        StrategyContext context = factory.newStrategyContext();
        context.setEventType(eventType);
        context.setPlayer(factory.newBukkitPlayer(e.getPlayer()));
        StrategyResult result = strategyEngine.evaluateStrategies(context);
        if (result != null && result.getLocation() != null) {
            BukkitLocation bukkitLocation = (BukkitLocation) result.getLocation();
            return bukkitLocation.getBukkitLocation();
        } else
            return null;
    }

    @EventHandler
    public void onRegionExit(RegionExitEvent e) {
        log.debug("onRegionExit() INVOKED, event={}", e);
        Location l = processEvent(e, EventType.EXIT_REGION);
        if (l != null) {
            log.debug("onRegionExit(): setting location to ", l);
            e.setTo(l);
        }
    }

    @EventHandler
    public void onRegionEnter(RegionEnterEvent e) {
        log.debug("onRegionEnter() INVOKED, event={}", e);
        Location l = processEvent(e, EventType.ENTER_REGION);
        if (l != null) {
            log.debug("onRegionEnter(): setting location to ", l);
            e.setTo(l);
        }
    }

    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);

        eventsRegistered = true;

    	/*
        pm.registerEvent(PlayerMoveEvent.class,
        		this,
        		EventPriority.NORMAL,
        		new EventExecutor() {
        			public void execute(Listener listener, Event event) throws EventException {
        				try {
        					onPlayerMove((PlayerMoveEvent) event);
        				} catch (Throwable t) {
        					throw new EventException(t);
        				}
        			}
		        },
		        plugin);
		        */
    }

}
