package org.morganm.homespawnplus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;


/**
 * Handle events for all Player related events
 * @author Timberjaw
 */
public class HSPPlayerListener extends PlayerListener {
	@SuppressWarnings("unused")
	private static final Logger log = HomeSpawnPlus.log;
	
    @SuppressWarnings("unused")
	private final String logPrefix; 
    private final HomeSpawnPlus plugin;
    private final HomeSpawnUtils util;
    // map sorted by PlayerName->Location->Time of event
    private final HashMap<String, ClickedEvent> bedClicks;
    private long lastCleanup;

    public HSPPlayerListener(HomeSpawnPlus instance) {
        logPrefix = HomeSpawnPlus.logPrefix;
        
        plugin = instance;
        util = plugin.getUtil();
        bedClicks = new HashMap<String, ClickedEvent>();
    }

    /** Maybe should be moved to util?  Return location player was sent to.
     * 
     * @param preferredBehavior
     */
    private Location doSpawn(Player p, String configBehaviorOption) {
    	String behavior = plugin.getConfig().getString(configBehaviorOption, ConfigOptions.VALUE_DEFAULT);
    	System.out.println("doSpawn, behavior = "+behavior);
    	
    	// default behavior is do nothing
    	if( behavior.equals(ConfigOptions.VALUE_DEFAULT) )
    		return null;
    	
    	if( behavior.equals(ConfigOptions.VALUE_HOME) )
    		return util.sendHome(p);
    	else if( behavior.equals(ConfigOptions.VALUE_MULTIHOME) ) {
    		Home home = util.getHome(p.getName(), p.getWorld());
    		
    		// no home set on current world, send them to default world
    		if( home == null ) {
        		home = util.getHome(p.getName(), util.getDefaultWorld());
        		if( home != null )
        			return home.getLocation();
        		// if home is null, this will fall-through to sendHome() below which will just
        		// send them to the spawn in the current world
    		}
    		
    		// they have a home set on this world, send them there
			return util.sendHome(p);
    	}
    	else if( behavior.equals(ConfigOptions.VALUE_GROUP) )
	    	return util.sendToGroupSpawn(p);
    	else if( behavior.equals(ConfigOptions.VALUE_WORLD) )
    		return util.sendToSpawn(p);
    	else if( behavior.equals(ConfigOptions.VALUE_GLOBAL) )
    		return util.sendToGlobalSpawn(p);
    	else
    		return null;
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.isCancelled())
            return;
        
        // config option needs to be enabled in order to use this feature
        if( !plugin.getConfig().getBoolean(ConfigOptions.ENABLE_HOME_BEDS, false) ) {
//        	log.info(logPrefix + " " + ConfigOptions.ENABLE_HOME_BEDS + " is disabled");
        	return;
        }
        
        // if permissions are enabled, they need to have permission too.
        if( plugin.isUsePermissions() && !plugin.hasPermission(event.getPlayer(), HomeSpawnPlus.BASE_PERMISSION_NODE+".home.bedsethome") ) {
//        	log.info(logPrefix + " onPlayerInteract(): no permission");
        	return;
        }
        
        Block b = event.getClickedBlock();
        if( b.getTypeId() == 26 ) {			// did they click on a bed?
//        	log.info(logPrefix + " clicked on bed");
        	// someone clicked on a bed, good time to keep the hash clean
        	cleanupBedClicks();

        	Player player = event.getPlayer();
        	ClickedEvent ce = bedClicks.get(player.getName());
        	
        	// if there is an event in the cache, then this is their second click - save their home
        	if( ce != null ) {
        		if( b.getLocation().equals(ce.location) ) {
        			util.setHome(player.getName(), player.getLocation(), player.getName());
        			
        			plugin.getUtil().sendMessage(player, "Your home has been set to this location.");
        			bedClicks.remove(player.getName());
            		event.setCancelled(true);
        		}
        	}
        	// otherwise this is first click, tell them to click again to save their home
        	else {
        		bedClicks.put(player.getName(), new ClickedEvent(b.getLocation(), System.currentTimeMillis()));
        		plugin.getUtil().sendMessage(player, "Click the bed one more time in the next 5 seconds to permanently change your home to this location.");
        		event.setCancelled(true);
        	}
        }
    }
    
    private void cleanupBedClicks() {
    	// skip cleanup if nothing to do
    	if( bedClicks.size() == 0 )
    		return;
    	
    	// don't run a cleanup if we just ran one in the last 5 seconds
    	if( System.currentTimeMillis() < lastCleanup+5000 )
    		return;
    	
    	lastCleanup = System.currentTimeMillis();
    	
    	long currentTime = System.currentTimeMillis();
    	
    	Set<Entry<String, ClickedEvent>> set = bedClicks.entrySet();
    	for(Iterator<Entry<String, ClickedEvent>> i = set.iterator(); i.hasNext();) {
    		Entry<String, ClickedEvent> e = i.next();
    		// if the click is older than 5 seconds, remove it
    		if( currentTime > e.getValue().timestamp+5000 ) {
    			i.remove();
    		}
    	}
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent e)
    {
    	Player p = e.getPlayer();
    	
		// Is this a new player?
    	if( plugin.getStorage().getPlayer(p.getName()) == null ) {
    		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Possible new player "+p.getName()+" detected, checking.");
    		org.morganm.homespawnplus.entity.Player storagePlayer = new org.morganm.homespawnplus.entity.Player(p);
    		plugin.getStorage().writePlayer(storagePlayer);

    		// also make sure they don't have a home set.  it's possible if they were running HomeSpawnPlus
    		// before the above Player check was created, that they already have a home set even though
    		// they weren't in the Player table yet.  In that case, we've already recorded them as a player
    		// now so just fall through to the default onjoin logic.
    		if( plugin.getStorage().getHome(p.getWorld().getName(), p.getName()) == null ) {
	    		// send the new player to the default world spawn
	    		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Sending new player " + p.getName() + " to global spawn.");
	    		util.sendToSpawn(p);
	    		return;
    		}
    	}
    	
    	HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to respawn player "+p.getName()+" (joining).");
    	doSpawn(p, ConfigOptions.SETTING_JOIN_BEHAVIOR);
    }
    
    private void updateQuitLocation(Player p)
    {
    	Location quitLocation = p.getLocation();
    	org.morganm.homespawnplus.entity.Player playerStorage = plugin.getStorage().getPlayer(p.getName());
    	playerStorage.updateLastLogoutLocation(quitLocation);
    	plugin.getStorage().writePlayer(playerStorage);
    }
    
    @Override
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event)
    {
    	updateQuitLocation(event.getPlayer());
    }
    
    @Override
    public void onPlayerKick(org.bukkit.event.player.PlayerKickEvent event)
    {
    	updateQuitLocation(event.getPlayer());
    }
    
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
    	HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to respawn player "+e.getPlayer().getName()+" (respawning).");
    	Location l = doSpawn(e.getPlayer(), ConfigOptions.SETTING_DEATH_BEHAVIOR);
    	if( l != null )
    		e.setRespawnLocation(l);
   }
 
    private class ClickedEvent {
    	public Location location;
    	public long timestamp;
    	
    	public ClickedEvent(Location location, long timestamp) {
    		this.location = location;
    		this.timestamp = timestamp;
    	}
    }
}