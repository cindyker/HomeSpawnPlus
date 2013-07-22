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
package com.andune.minecraft.hsp.integration.dynmap;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.hsp.config.ConfigDynmap;
import com.andune.minecraft.hsp.storage.Storage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.MarkerAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Dynmap module for HSP, code heavily borrowed from Mike Primm's
 * excellent DynmapCommandBookPlugin and simply adapted for HSP.
 *
 * @author andune
 */
@Singleton
public class BukkitDynmapModule implements DynmapModule, Initializable {
    private final Logger log = LoggerFactory.getLogger(BukkitDynmapModule.class);

    private final ConfigDynmap config;
    private final Plugin plugin;
    private final Storage storage;
    private final Server server;
    private Plugin dynmap;
    private DynmapCommonAPI api;
    private MarkerAPI markerapi;

    /* Homes layer settings */
    private Layer homelayer;

    /* Spawn layer settings */
    private Layer spawnLayer;

    private boolean activated = false;
    private boolean stop = false;
    private long updperiod;
    private MarkerUpdate markerUpdateObject = null;

    @Inject
    public BukkitDynmapModule(Plugin bukkitPlugin, ConfigDynmap config, Storage storage, Server server) {
        this.plugin = bukkitPlugin;
        this.config = config;
        this.storage = storage;
        this.server = server;
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled() && dynmap != null;
    }

    @Override
    public String getVersion() {
        if (dynmap != null)
            return dynmap.getDescription().getVersion();
        else
            return null;
    }

    /**
     * Previously "onEnable" in CommandBook version. Since this is being coded as an integrated
     * piece of HSP, we change it to an init() method to be invoked by HSP directly.
     */
    @Override
    public void init() {
        // don't do anything if we're not supposed to
        if (!config.isEnabled())
            return;

        info("initializing HSP dynmap integration");
        PluginManager pm = plugin.getServer().getPluginManager();
        
        /* Get dynmap */
        dynmap = pm.getPlugin("dynmap");
        if (dynmap == null) {
            severe("Cannot find dynmap!");
            return;
        }
        api = (DynmapCommonAPI) dynmap; /* Get API */

        pm.registerEvents(new OurServerListener(), plugin);        

        /* If dynmap is enabled, activate */
        if (dynmap.isEnabled())
            activate();
    }

    @Override
    public void shutdown() throws Exception {
        if (homelayer != null) {
            homelayer.stop();
            homelayer = null;
        }
        if (spawnLayer != null) {
            spawnLayer.stop();
            spawnLayer = null;
        }
        stop = true;
    }

    @Override
    public int getInitPriority() {
        return 9;
    }

    private void activate() {
        // do nothing if we've already activated
        if (activated)
            return;

        /* Now, get markers API */
        markerapi = api.getMarkerAPI();
        if (markerapi == null) {
            severe("Error loading Dynmap marker API!");
            return;
        }
        
        /* Determine update period */
        double per = config.getUpdatePeriod();
        if (per < 2.0) per = 2.0;
        updperiod = (long) (per * 20.0);

        /* Check which is enabled */
        if (config.isHomesEnabled()) {
            HomeLocationManager mgr = new HomeLocationManager(storage);
            ConfigurationSection cfg = config.getHomesConfig();
            /* Now, add marker set for homes */
            homelayer = new Layer(markerapi, server, mgr, "homes", cfg,
                    "Homes", "house", "%name%(home)", updperiod);
        }

        if (config.isSpawnsEnabled()) {
            SpawnLocationManager mgr = new SpawnLocationManager(storage);
            ConfigurationSection cfg = config.getSpawnsConfig();
            log.debug("spawn cfg={}", cfg);
	        /* Now, add marker set for spawns */
            spawnLayer = new Layer(markerapi, server, mgr, "spawns", cfg,
                    "Spawns", "spawn", "%name%", updperiod);
        }

        stop = false;
        markerUpdateObject = new MarkerUpdate();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, markerUpdateObject, 5 * 20);

        info("dynmap integration is activated");
        activated = true;
    }

    private class OurServerListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            Plugin p = event.getPlugin();
            String name = p.getDescription().getName();
            if (name.equals("dynmap") || name.equals("HomeSpawnPlus")) {
                if (dynmap.isEnabled())
                    activate();
            }
        }
    }

    private class MarkerUpdate implements Runnable {
        public void run() {
            if (!stop)
                updateMarkers();
        }
    }

    private void updateMarkers() {
        long startTime = System.currentTimeMillis();
        log.debug("DynmapModule.updateMarkers() START");

        if (homelayer != null)
            homelayer.updateMarkerSet();
        if (spawnLayer != null)
            spawnLayer.updateMarkerSet();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, markerUpdateObject, updperiod);

        log.debug("DynmapModule.updateMarkers() END ({} total ms)", System.currentTimeMillis() - startTime);
    }

    public void info(String msg) {
        log.info(msg);
    }

    public void severe(String msg) {
        log.error(msg);
    }
}
