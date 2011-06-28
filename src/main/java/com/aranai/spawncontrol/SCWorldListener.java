package com.aranai.spawncontrol;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

import com.aranai.spawncontrol.config.ConfigOptions;

/**
 * Handle events for all World related events
 * @author Timberjaw
 */
public class SCWorldListener extends WorldListener {
    private final SpawnControl plugin;
    private final SpawnUtils util;
    
    public SCWorldListener(SpawnControl instance) {
        plugin = instance;
        util = plugin.getSpawnUtils();
    }
    
    public void onWorldLoad(WorldLoadEvent event)
    {
    	World w = event.getWorld();
    	String name = w.getName();
    	
    	//  Set spawn if one has not yet been set (new world)
    	if( util.getSpawn(w.getName()) == null ) {
			SpawnControl.log.info(SpawnControl.logPrefix + " No global spawn found, setting global spawn to world spawn.");
			util.setSpawn(w.getSpawnLocation(), "onWorldLoad");
		}
    	
    	/*
    	 * Override spawn if one is available and behavior_globalspawn is enabled
    	 */
    	
    	if( plugin.getConfig().getBoolean(ConfigOptions.SETTING_WORLD_OVERRIDE, false) )
    	{
	    	SpawnControl.log.info("[SpawnControl] Setting global spawn for '"+name+"'.");
	    	
	    	Location spawn = util.getSpawn(w.getName());
	    	
	    	if(spawn != null)
	    	{
	    		// Set spawn
	    		w.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
	    	}
	    	else
	    	{
	    		// No spawn available
	    		SpawnControl.log.info(SpawnControl.logPrefix + " No spawn available for '"+name+"'!");
	    	}
    	}
    }
}