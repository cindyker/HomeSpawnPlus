package org.morganm.homespawnplus.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;


/**
 * Handle events for all World related events
 * @author morganm, Timberjaw
 */
public class HSPWorldListener implements Listener {
    private final HomeSpawnPlus plugin;
    private final HomeSpawnUtils util;
    
    public HSPWorldListener(HomeSpawnPlus instance) {
        plugin = instance;
        util = plugin.getUtil();
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event)
    {
    	World w = event.getWorld();
    	
    	//  Set spawn if one has not yet been set (new world)
    	if( util.getSpawn(w.getName()) == null ) {
    		if( util.isVerboseLogging() )
    			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " No global spawn found, setting global spawn to world spawn.");
			util.setSpawn(w.getSpawnLocation(), "onWorldLoad");
		}
    }
}