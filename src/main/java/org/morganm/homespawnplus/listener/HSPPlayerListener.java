/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
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
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
package org.morganm.homespawnplus.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
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
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.entity.UUIDHistory;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.Teleport;
import org.morganm.homespawnplus.util.UUIDNameUpdater;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


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
    
    /** Bukkit events are guaranteed to be single-threaded, so we take advantage
     * of this knowledge by recording the last known player/location for common
     * events and then checking at a MONITOR priority to see if it changed. This
     * allows us to warn the admin if another plugin changed the respawn/join
     * locations to something other than what they specified in HSP.
     */
    private Player lastRespawnPlayer;
    private Location lastRespawnLocation;
    
    // map sorted by PlayerName->Location->Time of event
    private final HashMap<String, ClickedEvent> bedClicks;
    private long lastCleanup;
    private final UUIDNameUpdater uuidNameUpdater;
    
    public HSPPlayerListener(HomeSpawnPlus instance) {
        logPrefix = HomeSpawnPlus.logPrefix;
        
        plugin = instance;
        util = plugin.getUtil();
        bedClicks = new HashMap<String, ClickedEvent>();
        debug = Debug.getInstance();
        uuidNameUpdater = new UUIDNameUpdater(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
        final String playerName = event.getName();
        final java.util.UUID uuid = event.getUniqueId();

        try {
            uuidNameUpdater.updateUUID(uuid, playerName);
        } catch(StorageException e) {
            log.log(Level.SEVERE, "Caught exception trying to update player UUID data for player "+playerName+", uuid "+uuid, e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
//    	debug.devDebug("onPlayerInteract: invoked");
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block b = event.getClickedBlock();
		// did they click on a bed? short-circuit this method if not (fail-fast)
        if( b.getType() != Material.BED_BLOCK )
        	return;

        // config option needs to be enabled in order to use this feature
        if( !plugin.getConfig().getBoolean(ConfigOptions.ENABLE_HOME_BEDS, false) )
        	return;

        // if BED_HOME_MUST_BE_NIGHT config is set, then we ignore this click and let
        // the PlayerBedEnterEvent handler handle it instead.
        if( plugin.getConfig().getBoolean(ConfigOptions.BED_HOME_MUST_BE_NIGHT, false) )
        	return;
        
        // canCancel is true if we can cancel the event to avoid spurious additional
        // "You can only sleep at night" messages. The original behavior did not do this,
        // so this is only false if they enable the "original" behavior
        boolean canCancel = !plugin.getConfig().getBoolean(ConfigOptions.BED_HOME_ORIGINAL_BEHAVIOR, false);

        // we do nothing further if the player is sneaking
        if( event.getPlayer().isSneaking() )
        	return;

		// if we get here, we know they clicked on a bed and configs are enabled for us
        // to something with that click
    	debug.debug("onPlayerInteract: calling doBedSet for player ",event.getPlayer());
    	
    	if( doBedSet(event.getPlayer(), b) )
    		event.setCancelled(canCancel);
    	// if we're never supposed to display "You can only sleep at night" message, then
    	// cancel the event to avoid the message
    	else if( plugin.getConfig().getBoolean(ConfigOptions.BED_HOME_NEVER_DISPLAY_NIGHT_MSG, false) )
    		event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onBedEvent(PlayerBedEnterEvent event) {
    	debug.devDebug("onBedEvent: invoked");
        // config option needs to be enabled in order to use this feature
        if( !plugin.getConfig().getBoolean(ConfigOptions.ENABLE_HOME_BEDS, false) )
        	return;
        
        // we do nothing further if the player is sneaking
        if( event.getPlayer().isSneaking() )
        	return;

        // we only handle events if BED_HOME_MUST_BE_NIGHT config is true, otherwise
        // the PlayerInteractEvent handler takes care of it.
        if( plugin.getConfig().getBoolean(ConfigOptions.BED_HOME_MUST_BE_NIGHT, false) ) {
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

    			// we update the Bukkit bed first as this avoids setHome() having to
    			// guess which bed we clicked on. However, it's possible setHome() will
    			// refuse to set the home for some reason, so we first record the
    			// old location so we can restore it if the setHome() call fails.
    			Location oldBedLoc = player.getBedSpawnLocation();
				player.setBedSpawnLocation(bedBlock.getLocation());	// update Bukkit bed
				
    			if( util.setHome(player.getName(), player.getLocation(), player.getName(), setDefaultHome, true) ) {
    				util.sendLocalizedMessage(player, HSPMessages.HOME_BED_SET);
    			}
    			else
    				player.setBedSpawnLocation(oldBedLoc);	// restore old bed if setHome() failed

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
    	
    	boolean isNewPlayer = util.isNewPlayer(p);
    	if( isNewPlayer ) {
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
    	
    	StrategyResult result = null;
    	// execute NEW_PLAYER strategy if player is new. If no results are returned, this
    	// will fall through to the ON_JOIN strategy instead.
    	if( isNewPlayer )
    		result = plugin.getStrategyEngine().getStrategyResult(EventType.NEW_PLAYER, p);
    	
    	// execute ON_JOIN strategy to find out where we should put the player, but only
    	// if there was no result from newPlayer checks
    	if( result == null || (result != null && result.getLocation() == null && !result.isExplicitDefault()) )
    		result = plugin.getStrategyEngine().getStrategyResult(EventType.ON_JOIN, p);
    	
    	Location joinLocation = null;
    	if( result != null )
    		joinLocation = result.getLocation();
    	
    	if( joinLocation != null ) {
    		util.delayedTeleport(p, joinLocation);
    		
    		// verify they ended up where we sent them by checking 5 tics later
    		final Location hspLocation = joinLocation;
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					Location currentLocation = p.getLocation();
					
					// do manual world/x/y/z check instead of .equals() so that we avoid comparing
					// pitch/yaw and also so we round to integer blocks instead of exact double loc
					if( currentLocation.getWorld() != hspLocation.getWorld()
							|| currentLocation.getBlockX() != hspLocation.getBlockX()
							|| currentLocation.getBlockY() != hspLocation.getBlockY()
							|| currentLocation.getBlockZ() != hspLocation.getBlockZ() ) {
						log.info(logPrefix + " onJoin: final player location is different than where HSP sent player, another plugin has changed the location."
								+" Player "+p.getName()+", HSP location "+util.shortLocationString(hspLocation)
								+", final player location "+util.shortLocationString(currentLocation));
					}
				}
			}, 5); 
    	}
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
    
    /** This method is bound manually instead of using @EventHandler annotation, so
     * that the priority can be dynamically assigned by the admin.
     * 
     * @param e
     */
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
    	long start = System.currentTimeMillis();
    	if( debug.isDevDebug() ) {
    		Location bedSpawn = e.getPlayer().getBedSpawnLocation();
    		debug.devDebug("onPlayerRespawn(): isBedSpawn=",e.isBedSpawn(),", bedSpawn=",bedSpawn);
    	}

    	if( util.isVerboseLogging() )
    		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to respawn player "+e.getPlayer().getName()+" (respawning).");

    	// execute ON_DEATH strategy to find out where we should spawn the player
    	Location l = plugin.getStrategyEngine().getStrategyLocation(EventType.ON_DEATH, e.getPlayer());
    	if( l != null ) {
    		e.setRespawnLocation(l);
    		lastRespawnLocation = l;
        	lastRespawnPlayer = e.getPlayer();
    	}

    	int warnMillis = plugin.getConfig().getInt(ConfigOptions.WARN_PERFORMANCE_MILLIS, 250); 
    	if( warnMillis > 0 ) {
            long totalTime = System.currentTimeMillis() - start;
            if( totalTime > warnMillis ) {
            	log.info("**LONG RESPAWN** Respawn for player "+e.getPlayer()+" took "+totalTime+" ms to run (> warning threshold of "+warnMillis+"ms)");
            }
    	}
    }

    /** Method to monitor successful cross-world teleports and
     * update PlayerLastLocation accordingly.
     * 
     * @param event
     */
    public void monitorPlayerTeleport(PlayerTeleportEvent event) {
        if( event.isCancelled() )
            return;
        
        // don't do anything if RECORD_LAST_LOCATION is disabled
        if( !plugin.getConfig().getBoolean(ConfigOptions.RECORD_LAST_LOCATION, true) )
            return;

    	// cross-world teleport event?
    	if( !event.getTo().getWorld().equals(event.getFrom().getWorld()) ) {
    		 PlayerLastLocationDAO dao = plugin.getStorage().getPlayerLastLocationDAO();
    		 PlayerLastLocation playerLastLocation = dao.findByWorldAndPlayerName(event.getPlayer().getWorld().getName(), event.getPlayer().getName());
    		 if( playerLastLocation == null ) {
    			 playerLastLocation = new PlayerLastLocation();
    			 playerLastLocation.setPlayerName(event.getPlayer().getName());
    		 }
			 playerLastLocation.setLocation(event.getFrom());

			 try {
				 dao.save(playerLastLocation);
			 }
			 catch(StorageException e) {
				 log.log(Level.WARNING, logPrefix+" Error writing to database: "+e.getMessage(), e);
			 }
			 debug.debug("Saved player ",event.getPlayer()," location as ",playerLastLocation);
    	}
    }

    @EventHandler(priority=EventPriority.LOW)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
    	if( event.isCancelled() )
    		return;

    	// implement "chunk refresh" if enabled
        // Code taken from codename_B's excellent BananaChunk plugin: this forces Bukkit
        // to refresh the chunk the player is teleporting into.
    	// commented out to prevent triggering Bukkit 1.3.1 refreshChunk() bug.
    	// https://bukkit.atlassian.net/browse/BUKKIT-2275
//    	if( plugin.getHSPConfig().getBoolean(ConfigOptions.RELOAD_CHUNK_ON_TELEPORT, false) ) {
//	    	Player player = event.getPlayer();
//	    	World world = player.getWorld();
//	    	Chunk chunk = world.getChunkAt(event.getTo());
//	    	int chunkx = chunk.getX();
//	    	int chunkz = chunk.getZ();
//	    	world.refreshChunk(chunkx, chunkz);
//    	}
    	
    	EventType type = null;
    	// cross-world teleport event?
		if( !event.getTo().getWorld().equals(event.getFrom().getWorld()) ) {
    		if( event.getPlayer().getName().equals(plugin.getMultiverseIntegration().getCurrentTeleporter()) ) {
    			type = EventType.MULTIVERSE_TELEPORT_CROSSWORLD;
            	debug.debug("multiverse crossworld teleport detected");
    		}
    		else {
    			type = EventType.CROSS_WORLD_TELEPORT;
            	debug.debug("crossworld teleport detected");
    		}
    		
    	}
		// same-world multiVerse teleport?
		else if( plugin.getMultiverseIntegration().getCurrentTeleporter() != null ) {
			type = EventType.MULTIVERSE_TELEPORT;
        	debug.debug("multiverse same world teleport detected");
		}
		
		if( type != null ) {
        	final StrategyContext context = new StrategyContext(plugin);
        	context.setEventType(type.toString());
        	context.setPlayer(event.getPlayer());
        	context.setLocation(event.getTo());	// location involved is the target location
        	context.setFromLocation(event.getFrom());
        	
    		// protect against a double-event for multiverse teleports
//    		if( plugin.getMultiverseIntegration().getCurrentTeleporter() != null ||
//    				!event.getPlayer().getName().equals(Teleport.getInstance().getCurrentTeleporter()) ) {
	        	StrategyResult result = plugin.getStrategyEngine().getStrategyResult(context);
	        	if( result != null && result.getLocation() != null )
	        		event.setTo(result.getLocation());
//    		}
		}
    	
    	// teleport is finished, clear current teleporter
    	plugin.getMultiverseIntegration().setCurrentTeleporter(null);
    	plugin.getMultiverseIntegration().setSourcePortalName(null);
    	plugin.getMultiverseIntegration().setDestinationPortalName(null);
		Teleport.getInstance().setCurrentTeleporter(null);
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void verifyRespawn(PlayerRespawnEvent e)
    {
		// don't proceed if admin has warnings turned off
		if( !plugin.getConfig().getBoolean(ConfigOptions.WARN_LOCATION_CHANGE, true) )
			return;

    	if( lastRespawnPlayer != null && lastRespawnLocation != null ) {
    		// shouldn't happen, but protect from silliness in case it does
    		if( !lastRespawnPlayer.equals(e.getPlayer()) ) {
    			lastRespawnPlayer = null;
    			lastRespawnLocation = null;
    			return;
    		}
    		
    		final Location respawnLoc = e.getRespawnLocation();

			// do manual world/x/y/z check instead of .equals() so that we avoid comparing
			// pitch/yaw and also so we round to integer blocks instead of exact double loc
			if( respawnLoc.getWorld() != lastRespawnLocation.getWorld()
					|| respawnLoc.getBlockX() != lastRespawnLocation.getBlockX()
					|| respawnLoc.getBlockY() != lastRespawnLocation.getBlockY()
					|| respawnLoc.getBlockZ() != lastRespawnLocation.getBlockZ() ) {
				log.info(logPrefix + " onDeath: final player location is different than where HSP sent player, another plugin has changed the location."
						+" Player "+lastRespawnPlayer.getName()+", HSP location "+util.shortLocationString(lastRespawnLocation)
						+", final player location "+util.shortLocationString(respawnLoc));
			}
    		
    	}
    	
    	lastRespawnPlayer = null;
    	lastRespawnLocation = null;
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
        if( plugin.getConfig().getBoolean(ConfigOptions.RECORD_LAST_LOCATION, true) ) {
            pm.registerEvent(PlayerTeleportEvent.class,
            		this,
            		EventPriority.MONITOR,
            		new EventExecutor() {
            			public void execute(Listener listener, Event event) throws EventException {
            				try {
            				    monitorPlayerTeleport((PlayerTeleportEvent) event);
            				} catch (Throwable t) {
            					throw new EventException(t);
            				}
            			}
    		        },
    		        plugin);
        }
   }
}
