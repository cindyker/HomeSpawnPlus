package org.morganm.homespawnplus.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;
import org.morganm.homespawnplus.SpawnInfo;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.util.Debug;


/**
 * Handle events for all Player related events
 * @author morganm, Timberjaw
 */
public class HSPPlayerListener implements Listener {
	private static final Logger log = HomeSpawnPlus.log;
	
	private final String logPrefix; 
    private final HomeSpawnPlus plugin;
    private final HomeSpawnUtils util;
    private final Debug debug;
    
    // map sorted by PlayerName->Location->Time of event
    private final HashMap<String, ClickedEvent> bedClicks;
    private long lastCleanup;
    
    public HSPPlayerListener(HomeSpawnPlus instance) {
        logPrefix = HomeSpawnPlus.logPrefix;
        
        plugin = instance;
        util = plugin.getUtil();
        bedClicks = new HashMap<String, ClickedEvent>();
        debug = Debug.getInstance();
    }

    /** Return location player should be sent to.
     * 
     * @param p
     * @param spawnInfo
     */
    private Location doSpawn(Player p, SpawnInfo spawnInfo) {
//    	spawnInfo.spawnStrategies = plugin.getHSPConfig().getStrategies(spawnInfo.spawnEventType);
    	Location l = util.getStrategyLocation(p, spawnInfo);
    	
    	// default behavior is do nothing
    	if( l == null ) {
    		// if we are spawning and the RECORD_LAST_LOGOUT config is set, then we lookup
    		// our last logout location and return that
    		if( ConfigOptions.SETTING_JOIN_BEHAVIOR.equals(spawnInfo.spawnEventType) &&
    				plugin.getHSPConfig().getBoolean(ConfigOptions.ENABLE_RECORD_LAST_LOGOUT, false) ) {
    			org.morganm.homespawnplus.entity.Player storagePlayer = plugin.getStorage().getPlayerDAO().findPlayerByName(p.getName());
    			if( storagePlayer != null )
    				l = storagePlayer.getLastLogoutLocation();
    		}
    	}
    	
    	return l;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.isCancelled())
            return;
        
        final Player p = event.getPlayer();
        
        // config option needs to be enabled in order to use this feature
        if( !plugin.getHSPConfig().getBoolean(ConfigOptions.ENABLE_HOME_BEDS, false) ) {
        	debug.debug("onPlayerInteract(): player ",p," bed right-click: config option ",ConfigOptions.ENABLE_HOME_BEDS," is disabled");
        	return;
        }
        
        Block b = event.getClickedBlock();
        if( b.getTypeId() == 26 ) {			// did they click on a bed?
//        	log.info(logPrefix + " clicked on bed");
        	// someone clicked on a bed, good time to keep the hash clean
        	cleanupBedClicks();

            // if permissions are enabled, they need to have permission too.
            if( !plugin.hasPermission(event.getPlayer(), HomeSpawnPlus.BASE_PERMISSION_NODE+".home.bedsethome") ) {
            	debug.debug("onPlayerInteract(): player ",p," has no permission");
            	return;
            }
            
        	Player player = event.getPlayer();
        	ClickedEvent ce = bedClicks.get(player.getName());
        	
        	// if there is an event in the cache, then this is their second click - save their home
        	if( ce != null ) {
        		if( b.getLocation().equals(ce.location) ) {
        			boolean setDefaultHome = false;
        			
        			// we set the bed to be the default home only if there isn't another non-bed
        			// default home that exists
        			Home existingDefaultHome = util.getDefaultHome(player.getName(), player.getWorld().getName());
        			if( existingDefaultHome == null || existingDefaultHome.isBedHome() )
        				setDefaultHome = true;
        			
        			if( util.setHome(player.getName(), player.getLocation(), player.getName(), setDefaultHome, true) )
    					util.sendLocalizedMessage(p, HSPMessages.HOME_BED_SET);
//        				util.sendMessage(player, "Your home has been set to this location.");

        			bedClicks.remove(player.getName());
//            		event.setCancelled(true);
        		}
        	}
        	// otherwise this is first click, tell them to click again to save their home
        	else {
        		bedClicks.put(player.getName(), new ClickedEvent(b.getLocation(), System.currentTimeMillis()));
				util.sendLocalizedMessage(p, HSPMessages.HOME_BED_ONE_MORE_CLICK);
//        		util.sendMessage(player, "Click the bed one more time in the next 5 seconds to permanently change your home to this location.");
//        		event.setCancelled(true);
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
    
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	Player p = event.getPlayer();
    	
    	SpawnInfo spawnInfo = new SpawnInfo();
    	spawnInfo.spawnEventType = ConfigOptions.SETTING_JOIN_BEHAVIOR;
    	
		// Is this a new player?
    	if( util.isNewPlayer(p) ) {
    		if( util.isVerboseLogging() )
    			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " New player "+p.getName()+" detected.");
    		
    		spawnInfo.isFirstLogin = true;
    		
    		/*

    		// also make sure they don't have a home set.  it's possible if they were running HomeSpawnPlus
    		// before the above Player check was created, that they already have a home set even though
    		// they weren't in the Player table yet.  In that case, we've already recorded them as a player
    		// now so just fall through to the default onjoin logic.
    		if( plugin.getStorage().getHome(p.getWorld().getName(), p.getName()) == null ) {
	    		// send the new player to the default world spawn
    			if( isVerboseLogging() )
    				HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Sending new player " + p.getName() + " to global spawn.");
	    		
	    		Location l = util.getDefaultSpawn().getLocation();
	    		util.delayedTeleport(p, l);
	    		
	    		return;
    		}
    		*/
    	}
    	
		// if they don't have a player record yet, create one.
    	if( plugin.getStorage().getPlayerDAO().findPlayerByName(p.getName()) == null ) {
    		org.morganm.homespawnplus.entity.Player storagePlayer = new org.morganm.homespawnplus.entity.Player(p);
    		try {
    			plugin.getStorage().getPlayerDAO().savePlayer(storagePlayer);
    		}
    		catch(StorageException e) {
				log.log(Level.WARNING, "Caught exception "+e.getMessage(), e);
    		}
    	}
    	
    	if( util.isVerboseLogging() )
    		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to respawn player "+p.getName()+" (joining).");
    	
    	Location l = doSpawn(p, spawnInfo);
    	if( l != null )
    		util.delayedTeleport(p, l);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event)
    {
    	util.updateQuitLocation(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(org.bukkit.event.player.PlayerKickEvent event)
    {
    	util.updateQuitLocation(event.getPlayer());
    }
    
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
    	if( util.isVerboseLogging() )
    		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to respawn player "+e.getPlayer().getName()+" (respawning).");

    	SpawnInfo spawnInfo = new SpawnInfo();
    	spawnInfo.spawnEventType = ConfigOptions.SETTING_DEATH_BEHAVIOR;
    	Location l = doSpawn(e.getPlayer(), spawnInfo);

    	if( l != null )
    		e.setRespawnLocation(l);
    }

    /** Code taken from codename_B's excellent BananaChunk plugin: this forces Bukkit
     * to refresh the chunk the player is teleporting into.
     */
    public void onPlayerTeleport(PlayerTeleportEvent event) {
    	if( event.isCancelled() )
    		return;

    	if( plugin.getHSPConfig().getBoolean(ConfigOptions.RELOAD_CHUNK_ON_TELEPORT, true) ) {
	    	Player player = event.getPlayer();
	    	World world = player.getWorld();
	    	Chunk chunk = world.getChunkAt(event.getTo());
	    	int chunkx = chunk.getX();
	    	int chunkz = chunk.getZ();
	    	world.refreshChunk(chunkx, chunkz);
    	}
    }

    private class ClickedEvent {
    	public Location location;
    	public long timestamp;
    	
    	public ClickedEvent(Location location, long timestamp) {
    		this.location = location;
    		this.timestamp = timestamp;
    	}
    }
    
    /** New-style Bukkit events don't have any nice mechanism for allowing runtime
     * priority (it's all set design-time via annotations). Since HSP allows the admin
     * to change the event priority if they want, we need to setup event priorities
     * dynamically. This is *really* ugly with the new event system, so I moved it 
     * into it's own method to keep the main plugin onEnable() nice and clean.
     */
    public void registerEvents() {
    	PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(PlayerRespawnEvent.class,
        		this,
        		plugin.getEventPriority(),
        		new EventExecutor() {
        			public void execute(Listener listener, Event event) throws EventException {
        				try {
        					onPlayerRespawn((PlayerRespawnEvent) event);
        				} catch (Throwable t) {
        					throw new EventException(t);
        				}
        			}
		        },
		        plugin);
    	
        pm.registerEvent(PlayerJoinEvent.class,
        		this,
        		plugin.getEventPriority(),
        		new EventExecutor() {
        			public void execute(Listener listener, Event event) throws EventException {
        				try {
        					onPlayerJoin((PlayerJoinEvent) event);
        				} catch (Throwable t) {
        					throw new EventException(t);
        				}
        			}
		        },
		        plugin);

        // optional event registration
        if( pm.getPlugin("BananaChunk") == null ) {
            pm.registerEvent(PlayerTeleportEvent.class,
            		this,
            		EventPriority.LOW,
            		new EventExecutor() {
            			public void execute(Listener listener, Event event) throws EventException {
            				try {
            					onPlayerTeleport((PlayerTeleportEvent) event);
            				} catch (Throwable t) {
            					throw new EventException(t);
            				}
            			}
    		        },
    		        plugin);
        }
        else
        	log.info(logPrefix + " BananaChunk found, disabling internal teleport chunk refresh.");
   }
}