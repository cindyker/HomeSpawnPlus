package com.aranai.spawncontrol;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.aranai.spawncontrol.config.Config;
import com.aranai.spawncontrol.config.ConfigOptions;

/**
 * Handle events for all Player related events
 * @author Timberjaw
 */
public class SCPlayerListener extends PlayerListener {
    private final SpawnControl plugin;
    private final Config config;
    private final SpawnUtils util;

    public SCPlayerListener(SpawnControl instance) {
        plugin = instance;
        config = plugin.getConfig();
        util = plugin.getSpawnUtils();
    }

    /** Maybe should be moved to util?
     * 
     * @param preferredBehavior
     */
    private void doSpawn(Player p, String configBehaviorOption) {
    	String behavior = config.getString(configBehaviorOption, ConfigOptions.VALUE_DEFAULT);
    	
    	// default behavior is do nothing
    	if( behavior.equals(ConfigOptions.VALUE_DEFAULT) )
    		return;
    	
    	if( behavior.equals(ConfigOptions.VALUE_HOME) )
    		util.sendHome(p);
    	else if( behavior.equals(ConfigOptions.VALUE_MULTIHOME) )
	    	util.sendHome(p);			// TODO: need to implement multi home algorithm
    	else if( behavior.equals(ConfigOptions.VALUE_GROUP) )
	    	util.sendToGroupSpawn(p);
    	else if( behavior.equals(ConfigOptions.VALUE_WORLD) )
    		util.sendToSpawn(p);
    	else if( behavior.equals(ConfigOptions.VALUE_GLOBAL) )
    		util.sendToSpawn(p);		// TODO: need to implement global spawn
    }
    
    public void onPlayerJoin(PlayerJoinEvent e)
    {
    	Player p = e.getPlayer();
    	
		// Probably a new player
    	if(util.getHome(p.getName(), p.getWorld()) == null)
    	{
    		SpawnControl.log.info(SpawnControl.logPrefix + " Sending new player " + p.getName() + " to global spawn.");
    		
    		// Send player to global spawn
    		util.sendToSpawn(p);
    		
    		// Set home for player
    		util.setHome(p.getName(), util.getSpawn(p.getWorld().getName()), SpawnControl.logPrefix);
    	}
    	
    	SpawnControl.log.info(SpawnControl.logPrefix + " Attempting to respawn player "+p.getName()+" (joining).");
    	doSpawn(p, ConfigOptions.SETTING_JOIN_BEHAVIOR);
    }
    
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
    	SpawnControl.log.info(SpawnControl.logPrefix + " Attempting to respawn player "+e.getPlayer().getName()+" (respawning).");
    	doSpawn(e.getPlayer(), ConfigOptions.SETTING_JOIN_BEHAVIOR);
    }
    
}