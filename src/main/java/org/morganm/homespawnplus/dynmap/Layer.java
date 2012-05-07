/**
 * 
 */
package org.morganm.homespawnplus.dynmap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

/**
 * @author morganm
 *
 */
public class Layer {
	private final DynmapModule module;
    private final MarkerAPI markerapi;
    private final LocationManager mgr;
    private final long updperiod;
    
	private MarkerSet set;
	private MarkerIcon deficon;
	private String labelfmt;
	private Set<String> visible;
	private Set<String> hidden;
	private Map<String, Marker> markers = new HashMap<String, Marker>();
	private boolean online_only;
    boolean stop = false;
	
	public Layer(final DynmapModule module, LocationManager mgr, String id, ConfigurationSection cfg,
			String deflabel, String deficon, String deflabelfmt, long updperiod) {
		this.module = module;
		this.mgr = mgr;
		this.markerapi = module.getMarkerAPI();
		this.updperiod = updperiod;

		init(id, cfg, deflabel, deficon, deflabelfmt);
	}
	
	private void init(String id, ConfigurationSection cfg, String deflabel, String deficon, String deflabelfmt)
	{
		set = markerapi.getMarkerSet("hsp." + id);
		if(set == null)
			set = markerapi.createMarkerSet("hsp."+id, cfg.getString("layer."+id+".name", deflabel), null, false);
		else
			set.setMarkerSetLabel(cfg.getString("layer."+id+".name", deflabel));
		if(set == null) {
			module.severe("Error creating " + deflabel + " marker set");
			return;
		}
		set.setLayerPriority(cfg.getInt("layer."+id+".layerprio", 10));
		set.setHideByDefault(cfg.getBoolean("layer."+id+".hidebydefault", false));
		int minzoom = cfg.getInt("layer."+id+".minzoom", 0);
		if(minzoom > 0) /* Don't call if non-default - lets us work with pre-0.28 dynmap */
			set.setMinZoom(minzoom);
		String icon = cfg.getString("layer."+id+".deficon", deficon);
		this.deficon = markerapi.getMarkerIcon(icon);
		if(this.deficon == null) {
			module.info("Unable to load default icon '" + icon + "' - using default '"+deficon+"'");
			this.deficon = markerapi.getMarkerIcon(deficon);
		}
		labelfmt = cfg.getString("layer."+id+".labelfmt", deflabelfmt);
		List<String> lst = cfg.getStringList("layer."+id+".visiblemarkers");
		if(lst != null)
			visible = new HashSet<String>(lst);
		lst = cfg.getStringList("layer."+id+".hiddenmarkers");
		if(lst != null)
			hidden = new HashSet<String>(lst);
		online_only = cfg.getBoolean("layer."+id+".online-only", false);
		if(online_only) {
			OurPlayerListener lsnr = new OurPlayerListener();

			module.getServer().getPluginManager().registerEvents(lsnr, module.getPlugin());
		}
	}
	
	public void stop() {
		stop = true;
		cleanup();
	}


	private void cleanup() {
		if(set != null) {
			set.deleteMarkerSet();
			set = null;
		}
		markers.clear();
	}

	private boolean isVisible(String id, String wname) {
		if((visible != null) && (visible.isEmpty() == false)) {
			if((visible.contains(id) == false) && (visible.contains("world:" + wname) == false))
				return false;
		}
		if((hidden != null) && (hidden.isEmpty() == false)) {
			if(hidden.contains(id) || hidden.contains("world:" + wname))
				return false;
		}
		return true;
	}

	private void updateMarkerSet() {
		Map<String, Marker> newmap = new HashMap<String, Marker>(); /* Build new map */
		/* For each world */
		for(World w : module.getServer().getWorlds()) {
			String wname = w.getName();
			List<NamedLocation> loclist = mgr.getLocations(w);  /* Get locations in this world */
			if(loclist == null) continue;

			for(NamedLocation nl : loclist) {
				/* Get location */
				Location loc = nl.getLocation();
				/* If not world specific list, we may get locations for other worlds - skip them */
				if(loc.getWorld() != w)
					continue;
				/* Get name */
				String name = nl.getName();
				/* Skip if not visible */
				if(isVisible(name, wname) == false)
					continue;
				/* If online only, check if player is online */
				if(online_only && (module.getServer().getPlayerExact(name) == null))
					continue;
				String id = wname + "/" + name;

				String label = labelfmt.replace("%name%", name);

				/* See if we already have marker */
				Marker m = markers.remove(id);
				if(m == null) { /* Not found?  Need new one */
					m = set.createMarker(id, label, wname, loc.getX(), loc.getY(), loc.getZ(), deficon, false);
				}
				else {  /* Else, update position if needed */
					m.setLocation(wname, loc.getX(), loc.getY(), loc.getZ());
					m.setLabel(label);
					m.setMarkerIcon(deficon);
				}
				newmap.put(id, m);    /* Add to new map */
			}
		}
		/* Now, review old map - anything left is gone */
		for(Marker oldm : markers.values()) {
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
     * @author morganm
     *
     */
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
            	updateMarkerSet();
            }
        }
    }
    
}
