package org.morganm.homespawnplus;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Spawn;


/**
 * Handle events for all World related events
 * @author morganm, Timberjaw
 */
public class HSPWorldListener extends WorldListener {
    private final HomeSpawnPlus plugin;
    private final HomeSpawnUtils util;
    
    public HSPWorldListener(HomeSpawnPlus instance) {
        plugin = instance;
        util = plugin.getUtil();
    }
    
    public void onWorldLoad(WorldLoadEvent event)
    {
    	World w = event.getWorld();
    	String name = w.getName();
    	
    	//  Set spawn if one has not yet been set (new world)
    	if( util.getSpawn(w.getName()) == null ) {
    		if( util.isVerboseLogging() )
    			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " No global spawn found, setting global spawn to world spawn.");
			util.setSpawn(w.getSpawnLocation(), "onWorldLoad");
		}
    	
    	/*
    	 * Override spawn if one is available and behavior_globalspawn is enabled
    	 */
    	
    	if( plugin.getHSPConfig().getBoolean(ConfigOptions.SETTING_WORLD_OVERRIDE, false) )
    	{
    		if( util.isVerboseLogging() )
    			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Setting global spawn for '"+name+"'.");
	    	
	    	Spawn spawn = util.getSpawn(w.getName());
	    	
	    	if(spawn != null) {
	    		Location l = spawn.getLocation();
	    		w.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
	    	}
	    	else {
	    		if( util.isVerboseLogging() )
	    			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " No spawn available for '"+name+"'!");
	    	}
    	}
    }
}