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
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.strategy.EventType;
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
    	debug.devDebug("onPlayerInteract: invoked");
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        // config option needs to be enabled in order to use this feature
        if( !plugin.getHSPConfig().getBoolean(ConfigOptions.ENABLE_HOME_BEDS, false) )
        	return;

        // if BED_HOME_MUST_BE_NIGHT config is set, then we ignore this click and let
        // the PlayerBedEnterEvent handler handle it instead.
        if( plugin.getHSPConfig().getBoolean(ConfigOptions.BED_HOME_MUST_BE_NIGHT, false) )
        	return;
        
        Block b = event.getClickedBlock();
		// did they click on a bed?
        if( b.getTypeId() == 26 ) {
        	debug.debug("onPlayerInteract: calling doBedSet for player ",event.getPlayer());
        	if( doBedSet(event.getPlayer(), b) )
        		event.setCancelled(true);
        	
        	// if they aren't sneaking, we cancel the event to avoid "You can only sleep
        	// at night" messages.
        	if( !event.getPlayer().isSneaking() )
        		event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onBedEvent(PlayerBedEnterEvent event) {
    	debug.devDebug("onBedEvent: invoked");
        // config option needs to be enabled in order to use this feature
        if( !plugin.getHSPConfig().getBoolean(ConfigOptions.ENABLE_HOME_BEDS, false) )
        	return;
        
        // we only handle events if BED_HOME_MUST_BE_NIGHT config is true, otherwise
        // the PlayerInteractEvent handler takes care of it.
        if( plugin.getHSPConfig().getBoolean(ConfigOptions.BED_HOME_MUST_BE_NIGHT, false) ) {
        	debug.debug("onBedEvent: calling doBedSet for player ",event.getPlayer());
        	if( doBedSet(event.getPlayer(), event.getBed()) )
        		event.setCancelled(true);
        }
    }
    
    /** Called when player right-clicks on a bed. Includes 2-click protection mechanism, if enabled.
     * 
     * @return true if the event should be canceled, false if not
     * @param p
     */
    private boolean doBedSet(final Player player, final Block bedBlock) {
    	// someone clicked on a bed, good time to keep the 2-click hash clean
    	cleanupBedClicks();

    	// make sure player has permission
    	if( !plugin.hasPermission(player, HomeSpawnPlus.BASE_PERMISSION_NODE+".home.bedsethome") ) {
    		debug.debug("onPlayerInteract(): player ",player," has no permission");
    		return false;
    	}

    	final boolean require2Clicks = plugin.getConfig().getBoolean(ConfigOptions.BED_HOME_2CLICKS, true);

    	ClickedEvent ce = bedClicks.get(player.getName());

    	// if there is an event in the cache, then this is their second click - save their home
    	if( ce != null || !require2Clicks ) {
    		if( ce == null || bedBlock.getLocation().equals(ce.location) ) {
    			boolean setDefaultHome = false;

    			// we set the bed to be the default home only if there isn't another non-bed
    			// default home that exists
    			Home existingDefaultHome = util.getDefaultHome(player.getName(), player.getWorld().getName());
    			if( existingDefaultHome == null || existingDefaultHome.isBedHome() )
    				setDefaultHome = true;

    			if( util.setHome(player.getName(), player.getLocation(), player.getName(), setDefaultHome, true) )
    				util.sendLocalizedMessage(player, HSPMessages.HOME_BED_SET);

    			bedClicks.remove(player.getName());
    		}
    	}
    	// otherwise this is first click, tell them to click again to save their home
    	else {
    		bedClicks.put(player.getName(), new ClickedEvent(bedBlock.getLocation(), System.currentTimeMillis()));
    		util.sendLocalizedMessage(player, HSPMessages.HOME_BED_ONE_MORE_CLICK);
			
			// cancel the first-click event if 2 clicks is required
			return require2Clicks;
    	}
    	
    	return false;
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
    	final Player p = event.getPlayer();
    	
		// Is this a new player?
    	if( util.isNewPlayer(p) ) {
    		if( util.isVerboseLogging() )
    			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " New player "+p.getName()+" detected.");
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
    	
    	// execute ON_JOIN strategy to find out where we should put the player
    	Location l = plugin.getStrategyEngine().getStrategyLocation(EventType.ON_JOIN, p);
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

    	// execute ON_DEATH strategy to find out where we should spawn the player
    	Location l = plugin.getStrategyEngine().getStrategyLocation(EventType.ON_DEATH, e.getPlayer());
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