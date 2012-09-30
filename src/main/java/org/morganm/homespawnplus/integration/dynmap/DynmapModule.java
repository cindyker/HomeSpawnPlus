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
package org.morganm.homespawnplus.integration.dynmap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.util.Debug;

/** Dynmap module for HSP, code heavily borrowed from Mike Primm's
 * excellent DynmapCommandBookPlugin and simply adapted for HSP.
 * 
 * @author morganm
 *
 */
public class DynmapModule {
	
    private final Logger log;
    private final String logPrefix;

    private final HomeSpawnPlus plugin;
    private Plugin dynmap;
    private DynmapAPI api;
    private MarkerAPI markerapi;
    
    /* Homes layer settings */
    private Layer homelayer;
    
    /* Spawn layer settings */
    private Layer spawnLayer;
    
    private boolean activated = false;
    private boolean stop = false;
    private long updperiod;
    private MarkerUpdate markerUpdateObject = null;
    
    public DynmapModule(final HomeSpawnPlus plugin) {
    	this.plugin = plugin;
    	this.log = plugin.getLogger();
    	this.logPrefix = plugin.getLogPrefix();
    }

    /** Previously "onEnable" in CommandBook version. Since this is being coded as an integrated
     * piece of HSP, we change it to an init() method to be invoked by HSP directly.
     * 
     */
    public void init() {
    	// don't do anything if we're not supposed to
    	if( !plugin.getConfig().getBoolean(ConfigOptions.DYNMAP_INTEGRATION_ENABLED, false) )
    		return;
    	
        info("initializing HSP dynmap integration");
        PluginManager pm = plugin.getServer().getPluginManager();
        
        /* Get dynmap */
        dynmap = pm.getPlugin("dynmap");
        if(dynmap == null) {
            severe("Cannot find dynmap!");
            return;
        }
        api = (DynmapAPI)dynmap; /* Get API */

        pm.registerEvents(new OurServerListener(), plugin);        

        /* If dynmap is enabled, activate */
        if(dynmap.isEnabled())
            activate();
    }

    private void activate() {
    	// do nothing if we've already activated
    	if( activated )
    		return;
    	
        /* Now, get markers API */
        markerapi = api.getMarkerAPI();
        if(markerapi == null) {
            severe("Error loading Dynmap marker API!");
            return;
        }
        
        /* Determine update period */
        double per = plugin.getConfig().getDouble(ConfigOptions.DYNMAP_INTEGRATION_UPDATE_PERIOD, 5.0);
        if(per < 2.0) per = 2.0;
        updperiod = (long)(per*20.0);

        /* Check which is enabled */
        if(plugin.getConfig().getBoolean(ConfigOptions.DYNMAP_INTEGRATION_HOMES_ENABLED, true)) {
            HomeLocationManager mgr = new HomeLocationManager(plugin);
            ConfigurationSection cfg = plugin.getConfig().getConfigurationSection(ConfigOptions.DYNMAP_INTEGRATION_HOMES);
            /* Now, add marker set for homes */
            homelayer = new Layer(this, mgr, "homes", cfg, "Homes", "house", "%name%(home)", updperiod);
        }
        
        if(plugin.getConfig().getBoolean(ConfigOptions.DYNMAP_INTEGRATION_SPAWNS_ENABLED, true)) {
            SpawnLocationManager mgr = new SpawnLocationManager(plugin);
        	ConfigurationSection cfg = plugin.getConfig().getConfigurationSection(ConfigOptions.DYNMAP_INTEGRATION_SPAWNS);
        	Debug.getInstance().debug("spawn cfg=",cfg);
	        /* Now, add marker set for spawns */
	        spawnLayer = new Layer(this, mgr, "spawns", cfg, "Spawns", "spawn", "%name%", updperiod);
        }
        
        stop = false;
        markerUpdateObject = new MarkerUpdate();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, markerUpdateObject, 5*20);
        
        info("dynmap integration is activated");
        activated = true;
    }

    public void onDisable() {
        if(homelayer != null) {
            homelayer.stop();
            homelayer = null;
        }
        if(spawnLayer != null) {
            spawnLayer.stop();
            spawnLayer = null;
        }
        stop = true;
    }

    private class OurServerListener implements Listener {
        @EventHandler(priority=EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            Plugin p = event.getPlugin();
            String name = p.getDescription().getName();
            if(name.equals("dynmap") || name.equals("HomeSpawnPlus")) {
                if(dynmap.isEnabled())
                    activate();
            }
        }
    }
    
    private class MarkerUpdate implements Runnable {
        public void run() {
            if(!stop)
                updateMarkers();
        }
    }
    
    private void updateMarkers() {
    	long startTime = System.currentTimeMillis();
    	Debug.getInstance().debug("DynmapModule.updateMarkers() START");
    	
        if(homelayer != null)
            homelayer.updateMarkerSet();
        if(spawnLayer != null)
        	spawnLayer.updateMarkerSet();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, markerUpdateObject, updperiod);
        
    	Debug.getInstance().debug("DynmapModule.updateMarkers() END (",System.currentTimeMillis()-startTime," total ms)");
    }

    public void info(String msg) {
        log.log(Level.INFO, logPrefix + " " + msg);
    }
    public void severe(String msg) {
        log.log(Level.SEVERE, logPrefix + " " + msg);
    }
    
    public Server getServer() {
    	return plugin.getServer();
    }
    public JavaPlugin getPlugin() {
    	return plugin;
    }
    public MarkerAPI getMarkerAPI() {
    	return markerapi;
    }

}
