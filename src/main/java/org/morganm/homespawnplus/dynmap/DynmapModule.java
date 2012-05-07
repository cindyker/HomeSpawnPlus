package org.morganm.homespawnplus.dynmap;
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
    	if( plugin.getConfig().getBoolean(ConfigOptions.DYNMAP_INTEGRATION_ENABLED, false) )
    		return;
    	
        info("initializing");
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
        /* Now, get markers API */
        markerapi = api.getMarkerAPI();
        if(markerapi == null) {
            severe("Error loading Dynmap marker API!");
            return;
        }
        
        /* Determine update period */
        double per = plugin.getConfig().getDouble(ConfigOptions.DYNMAP_INTEGRATION_UPDATE_PERIOD, 5.0);
        if(per < 2.0) per = 2.0;
        long updperiod = (long)(per*20.0);

        /* Check which is enabled */
        if(plugin.getConfig().getBoolean(ConfigOptions.DYNMAP_INTEGRATION_HOMES_ENABLED, true) == false) {
            HomeLocationManager mgr = new HomeLocationManager(plugin);
            ConfigurationSection cfg = plugin.getConfig().getConfigurationSection(ConfigOptions.DYNMAP_INTEGRATION_HOMES);
            /* Now, add marker set for homes */
            homelayer = new Layer(this, mgr, "homes", cfg, "Homes", "house", "%name%(home)", updperiod);
        }
        
        if(plugin.getConfig().getBoolean(ConfigOptions.DYNMAP_INTEGRATION_SPAWNS_ENABLED, true) == false) {
            SpawnLocationManager mgr = new SpawnLocationManager(plugin);
        	ConfigurationSection cfg = plugin.getConfig().getConfigurationSection(ConfigOptions.DYNMAP_INTEGRATION_SPAWNS);
	        /* Now, add marker set for spawns */
	        spawnLayer = new Layer(this, mgr, "spawns", cfg, "Spawns", "spawn", "[%name%]", updperiod);
        }
        
//        stop = false;
//        plugin.getServer().getScheduler().scheduleSyncDelayedTask(this, new MarkerUpdate(), 5*20);
        
        info("dynmap integration is activated");
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
//        stop = true;
    }

    private class OurServerListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler(priority=EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            Plugin p = event.getPlugin();
            String name = p.getDescription().getName();
            if(name.equals("dynmap") || name.equals("CommandBook")) {
                if(dynmap.isEnabled())
                    activate();
            }
        }
    }
    
    /** Supposed to update us on a regular interval, but is redundant with the smarter
     * updating built into layer. Disabling.
     * 
     */
    /*
    private class MarkerUpdate implements Runnable {
        public void run() {
            if(!stop)
                updateMarkers();
        }
    }
    */
    
    /** This method appears to be redundant with the one built into the Layer object
     * (which I broke out into it's own file). Except the Layer one is "smart" enough
     * to disable itself when no players are logged into the server.
     */
    /*
    private void updateMarkers() {
        if(homelayer != null) {
            homelayer.updateMarkerSet(homesmgr);
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new MarkerUpdate(), updperiod);
    }
    */

    public void info(String msg) {
        log.log(Level.INFO, logPrefix + msg);
    }
    public void severe(String msg) {
        log.log(Level.SEVERE, logPrefix + msg);
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
