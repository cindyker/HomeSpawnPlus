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
package com.andune.minecraft.hsp.integration.dynmap;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.World;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.*;


/**
 * @author andune
 */
public class Layer {
    private final Logger log = LoggerFactory.getLogger(Layer.class);

    private final Server server;
    private final MarkerAPI markerapi;
    private final LocationManager mgr;
//    private final long updperiod;

    private MarkerSet set;
    private MarkerIcon deficon;
    private String labelfmt;
    private Set<String> visible;
    private Set<String> hidden;
    private Map<String, Marker> markers = new HashMap<String, Marker>();
    private boolean online_only;
    private ConfigurationSection cfg;
    boolean stop = false;

    public Layer(MarkerAPI markerapi, Server server, LocationManager mgr, String id,
                 ConfigurationSection cfg,
                 String deflabel, String deficon, String deflabelfmt, long updperiod) {
        this.server = server;
        this.mgr = mgr;
        this.markerapi = markerapi;
        this.cfg = cfg;
//		this.updperiod = updperiod;

        init(id);
    }

    private void init(String id) //, String deflabel, String deficon, String deflabelfmt)
    {
        final String label = cfg.getString("name");

        set = markerapi.getMarkerSet("hsp." + id);
        if (set == null)
            set = markerapi.createMarkerSet("hsp." + id, label, null, false);
        else
            set.setMarkerSetLabel(label);

        log.debug("Layer.init() created dynmap layer hsp.{}, set name=", "layer.{}.name", id, id);

        if (set == null) {
            log.error("Error creating " + label + " marker set");
            return;
        }
        set.setLayerPriority(cfg.getInt("layerprio"));
        set.setHideByDefault(cfg.getBoolean("hidebydefault"));
        log.debug("Layer.init() id={}, layerprio={}, hideByDefault={}",
                id, set.getLayerPriority(), set.getHideByDefault());

        int minzoom = cfg.getInt("minzoom");
        if (minzoom > 0) /* Don't call if non-default - lets us work with pre-0.28 dynmap */
            set.setMinZoom(minzoom);
        String icon = cfg.getString("deficon");
        this.deficon = markerapi.getMarkerIcon(icon);
//		if(this.deficon == null) {
//			module.info("Unable to load default icon '" + icon + "' - using default '"+deficon+"'");
//			this.deficon = markerapi.getMarkerIcon(deficon);
//		}
        labelfmt = cfg.getString("labelfmt");
        List<String> lst = cfg.getStringList("visiblemarkers");
        if (lst != null)
            visible = new HashSet<String>(lst);
        lst = cfg.getStringList("hiddenmarkers");
        if (lst != null)
            hidden = new HashSet<String>(lst);
        online_only = cfg.getBoolean("online-only");
        log.debug("Layer.init() id={}, online-only={}", id, online_only);

//		if(online_only) {
//			OurPlayerListener lsnr = new OurPlayerListener();
//
//			module.getServer().getPluginManager().registerEvents(lsnr, module.getPlugin());
//		}
    }

    public void stop() {
        stop = true;
        cleanup();
    }


    private void cleanup() {
        if (set != null) {
            set.deleteMarkerSet();
            set = null;
        }
        markers.clear();
    }

    private boolean isVisible(String id, String wname) {
        if ((visible != null) && (visible.isEmpty() == false)) {
            if ((visible.contains(id) == false) && (visible.contains("world:" + wname) == false))
                return false;
        }
        if ((hidden != null) && (hidden.isEmpty() == false)) {
            if (hidden.contains(id) || hidden.contains("world:" + wname))
                return false;
        }
        return true;
    }

    /**
     * Default package visibility. Called regularly to update the markers on the map.
     */
    void updateMarkerSet() {
        Map<String, Marker> newmap = new HashMap<String, Marker>(); /* Build new map */
        /* For each world */
        for (World w : server.getWorlds()) {
            String wname = w.getName();
            List<NamedLocation> loclist = mgr.getLocations(w);  /* Get locations in this world */
            if (loclist == null) continue;

            for (NamedLocation nl : loclist) {
				/* Get location */
                Location loc = nl.getLocation();
				/* Get name */
                String name = nl.getName();

                // if this location is not enabled, skip it
                if (!nl.isEnabled(cfg))
                    continue;

                log.debug("updateMarkerSet() name={}, loc={}", name, loc);
				/* If not world specific list, we may get locations for other worlds - skip them */
                if (loc.getWorld() != w)
                    continue;
				/* Skip if not visible */
                if (isVisible(name, wname) == false || name == null)
                    continue;
				/* If online only, check if player is online */
                if (online_only) {
                    String playerName = nl.getPlayerName();
                    log.debug("updateMarkerSet() name={}, playerName={}", name, playerName);

                    if (server.getPlayer(playerName) == null) {
                        log.debug("updateMarkerSet() name={}, player is offline, skipping", name);
                        continue;
                    }
                }
                String id = wname + "/" + name;

                log.debug("updateMarkerSet() name={} is visible, formatting label", name);
                String label = labelfmt.replace("%name%", name);
                log.debug("updateMarkerSet() name={}, label={}", name, label);

				/* See if we already have marker */
                Marker m = markers.remove(id);
                if (m == null) { /* Not found?  Need new one */
                    m = set.createMarker(id, label, wname, loc.getX(), loc.getY(), loc.getZ(), deficon, false);
                    log.debug("updateMarkerSet() name={} creating new marker", name);
                } else {  /* Else, update position if needed */
                    m.setLocation(wname, loc.getX(), loc.getY(), loc.getZ());
                    m.setLabel(label);
                    m.setMarkerIcon(deficon);
                    log.debug("updateMarkerSet() name={} updating existing marker", name);
                }

                log.debug("updateMarkerSet() name={} adding to map", name);
                newmap.put(id, m);    /* Add to new map */
            }
        }
		/* Now, review old map - anything left is gone */
        for (Marker oldm : markers.values()) {
            oldm.deleteMarker();
        }
		/* And replace with new map */
        markers.clear();
        markers = newmap;
    }

    /** Private listener that updates home markers on a regular interval. Could
     * be replaced by a real-time HSP API hook if I designed one, but this way
     * seems fine for now.
     *
     * Although this mechanism was inherited from the original
     * Dynmap-CommandBook, it appears completely redundant with the existing
     * update mechanism built into DynmapModule. Commenting out. 6/30/12 andune
     *
     * @author andune
     *
     */
	/*
    private class OurPlayerListener implements Listener, Runnable {
        @SuppressWarnings("unused")
        @EventHandler(priority=EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
        	module.getServer().getScheduler().scheduleSyncDelayedTask(module.getPlugin(), this, updperiod);
        }
        @SuppressWarnings("unused")
        @EventHandler(priority=EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
        	module.getServer().getScheduler().scheduleSyncDelayedTask(module.getPlugin(), this, updperiod);
        }
        public void run() {
            if((!stop) && (mgr != null)) {
            	log.debug("OurPlayerListener.run() updating marker set");
            	updateMarkerSet();
            }
        }
    }
    */

}
