/**
 * 
 */
package org.morganm.homespawnplus;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.integration.multiverse.MultiverseModule;
import org.morganm.homespawnplus.manager.WarmupManager;
import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.events.PlayerBedEnterEvent;
import org.morganm.homespawnplus.server.api.events.PlayerDamageEvent;
import org.morganm.homespawnplus.server.api.events.PlayerJoinEvent;
import org.morganm.homespawnplus.server.api.events.PlayerKickEvent;
import org.morganm.homespawnplus.server.api.events.PlayerMoveEvent;
import org.morganm.homespawnplus.server.api.events.PlayerQuitEvent;
import org.morganm.homespawnplus.server.api.events.PlayerRespawnEvent;
import org.morganm.homespawnplus.server.api.events.PlayerTeleportEvent;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyEngine;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.morganm.homespawnplus.util.BedUtils;
import org.morganm.homespawnplus.util.SpawnUtil;
j.LoggerFactory;

import com.andune.minecraft.commonlib.T
import com.andune.minecraft.commonlib.Teleport;
eleport;

/**
 * @author morganm
 *
 */
@Singleton
public class EventListener implements org.morganm.homespawnplus.server.api.event.EventListener {
    private final Logger log = LoggerFactory.getLogger(EventListener.class);
    private Storage storage;
    private StrategyEngine engine;
    private ConfigCore config;
    private Factory factory;
    private MultiverseModule multiVerse;
    private Teleport teleport;
    private SpawnUtil spawnUtil;
    private BedUtils bedUtil;
    private WarmupManager warmupManager;
    
    /** We record the last known player/location for common events so that we can
     * later check at a MONITOR priority to see if it changed.
     * 
     * This allows us to warn the admin if another plugin changed the respawn/join
     * locations to something other than what they specified in HSP.
     * 
     * Note that this behavior depends on the fact that event processing is single-
     * threaded. This is true for Bukkit; if HSP is ever implemented upon a server
     * framework where this is not true, this will have to be changed.
     */
    private Player lastRespawnPlayer;
    private Location lastRespawnLocation;
    
    @Inject
    public EventListener(ConfigCore config, Storage storage, StrategyEngine engine, Factory factory,
            MultiverseModule multiVerse, Teleport teleport, SpawnUtil spawnUtil,
            BedUtils bedUtil, WarmupManager warmupManager) {
        this.config = config;
        this.storage = storage;
        this.engine = engine;
        this.factory = factory;
        this.multiVerse = multiVerse;
        this.teleport = teleport;
        this.spawnUtil = spawnUtil;
        this.bedUtil = bedUtil;
        this.warmupManager = warmupManager;
    }
    
    @Override
    public void playerJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        final boolean isNewPlayer = p.isNewPlayer();
        
        if( isNewPlayer ) {
            if( config.isVerboseLogging() )
                log.info("New player {} detected.", p.getName());
        }
        
        // if they don't have a player record yet, create one.
        if( storage.getPlayerDAO().findPlayerByName(p.getName()) == null ) {
            org.morganm.homespawnplus.entity.Player storagePlayer = new org.morganm.homespawnplus.entity.Player(p);
            try {
                storage.getPlayerDAO().savePlayer(storagePlayer);
            }
            catch(StorageException e) {
                log.warn("Caught exception writing to storage ", e);
            }
        }
        
        if( config.isVerboseLogging() )
            log.info("Attempting to respawn player {} (joining).", p.getName());
        
        StrategyResult result = null;
        // execute NEW_PLAYER strategy if player is new. If no results are returned, this
        // will fall through to the ON_JOIN strategy instead.
        if( isNewPlayer )
            result = engine.getStrategyResult(EventType.NEW_PLAYER, p);
        
        // execute ON_JOIN strategy to find out where we should put the player, but only
        // if there was no result from newPlayer checks
        if( result == null || (result != null && !result.isExplicitDefault()) )
            result = engine.getStrategyResult(EventType.ON_JOIN, p);
        
        Location joinLocation = null;
        if( result != null )
            joinLocation = result.getLocation();
        
        if( joinLocation != null )
            event.setJoinLocation(joinLocation);
    }

    @Override
    public void playerRespawn(PlayerRespawnEvent event) {
        long start = System.currentTimeMillis();
        if( log.isDebugEnabled() ) {
            Location bedSpawn = event.getPlayer().getBedSpawnLocation();
            log.debug("onPlayerRespawn(): isBedSpawn={}, bedSpawn={}", event.isBedSpawn(), bedSpawn);
        }

        if( config.isVerboseLogging() )
            log.info("Attempting to respawn player {} (respawning).", event.getPlayer().getName());

        // execute ON_DEATH strategy to find out where we should spawn the player
        Location l = engine.getStrategyLocation(EventType.ON_DEATH, event.getPlayer());
        if( l != null ) {
            event.setRespawnLocation(l);
            lastRespawnLocation = l;
            lastRespawnPlayer = event.getPlayer();
        }

        int warnMillis = config.getPerformanceWarnMillis();
        if( warnMillis > 0 ) {
            long totalTime = System.currentTimeMillis() - start;
            if( totalTime > warnMillis ) {
                log.info("**LONG RESPAWN** Respawn for player {} took {} ms to run (> warning threshold of {}ms)",
                        event.getPlayer(), totalTime, warnMillis);
            }
        }
    }

    @Override
    public void playerTeleport(PlayerTeleportEvent event) {
        EventType type = null;
        // cross-world teleport event?
        if( !event.getTo().getWorld().equals(event.getFrom().getWorld()) ) {
            if( event.getPlayer().getName().equals(multiVerse.getCurrentTeleporter()) ) {
                type = EventType.MULTIVERSE_TELEPORT_CROSSWORLD;
                log.debug("multiverse crossworld teleport detected");
            }
            else {
                type = EventType.CROSS_WORLD_TELEPORT;
                log.debug("crossworld teleport detected");
            }
            
        }
        // same-world multiVerse teleport?
        else if( multiVerse.getCurrentTeleporter() != null ) {
            type = EventType.MULTIVERSE_TELEPORT;
            log.debug("multiverse same world teleport detected");
        }
        
        // if this was some sort of teleport event that we should fire an HSP
        // event for, then set one up and invoke the strategy engine
        if( type != null ) {
            final StrategyContext context = factory.newStrategyContext();
            context.setEventType(type.toString());
            context.setPlayer(event.getPlayer());
            context.setLocation(event.getTo()); // location involved is the target location

            StrategyResult result = engine.getStrategyResult(context);
            if( result != null && result.getLocation() != null )
                event.setTo(result.getLocation());
        }
        
        // teleport is finished, clear current teleporter
        multiVerse.setCurrentTeleporter(null);
        multiVerse.setSourcePortalName(null);
        multiVerse.setDestinationPortalName(null);
        teleport.setCurrentTeleporter(null);
    }

    /** Method to monitor successful cross-world teleports and
     * update PlayerLastLocation accordingly.
     */
    @Override
    public void observePlayerTeleport(PlayerTeleportEvent event) {
        // cross-world teleport event?
        if( !event.getTo().getWorld().equals(event.getFrom().getWorld()) ) {
             PlayerLastLocationDAO dao = storage.getPlayerLastLocationDAO();
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
                 log.warn("Error writing to database", e);
             }
             log.debug("Saved player {} location as {}", event.getPlayer(), playerLastLocation);
        }
    }

    @Override
    public void observeRespawn(PlayerRespawnEvent event) {
        if( config.isWarnLocationChange() )
            return;

        if( lastRespawnPlayer != null && lastRespawnLocation != null ) {
            // shouldn't happen, but protect from silliness in case it does
            if( !lastRespawnPlayer.equals(event.getPlayer()) ) {
                lastRespawnPlayer = null;
                lastRespawnLocation = null;
                return;
            }
            
            final Location respawnLoc = event.getRespawnLocation();

            // do manual world/x/y/z check instead of .equals() so that we avoid comparing
            // pitch/yaw and also so we round to integer blocks instead of exact double loc
            if( respawnLoc.getWorld() != lastRespawnLocation.getWorld()
                    || respawnLoc.getBlockX() != lastRespawnLocation.getBlockX()
                    || respawnLoc.getBlockY() != lastRespawnLocation.getBlockY()
                    || respawnLoc.getBlockZ() != lastRespawnLocation.getBlockZ() ) {
                log.info("onDeath: final player location is different than where HSP sent player, another plugin has changed the location."
                        +" Player "+lastRespawnPlayer.getName()+", HSP location "+lastRespawnLocation.shortLocationString()
                        +", final player location "+respawnLoc.shortLocationString());
            }
            
        }
        
        lastRespawnPlayer = null;
        lastRespawnLocation = null;
    }
    
    @Override
    public void bedRightClick(org.morganm.homespawnplus.server.api.events.PlayerBedRightClickEvent event) {
        if( !config.isBedSetHome() )
            return;

        // if BED_HOME_MUST_BE_NIGHT config is set, then we ignore this click and let
        // the PlayerBedEnterEvent handler handle it instead.
        if( config.isBedHomeMustBeNight() )
            return;
        
        // we do nothing further if the player is sneaking; this allows sneaking to override
        // all HSP home behavior, to let players actually sleep in a bed without setting their
        // homes, for example.
        if( event.getPlayer().isSneaking() )
            return;

        // if we get here, we know they clicked on a bed and configs are enabled for us
        // to something with that click
        log.debug("bedRightClick: calling doBedSet for player {}",event.getPlayer());
        
        if( bedUtil.doBedClick(event.getPlayer(), event.getClickedBlock()) ) {
            // if original behavior is not enabled, then we cancel the event to avoid
            // a spurious "you can only sleep at night" message
            if( !config.isBedHomeOriginalBehavior() )
                event.setCancelled(true);
        }

        // if we're never supposed to display "You can only sleep at night" message, then
        // cancel the event to avoid the message
        if( config.isBedNeverDisplayNightMessage() )
            event.setCancelled(true);
    }
    
    @Override
    public void bedEvent(PlayerBedEnterEvent event) {
        log.debug("bedEvent: invoked");
        
        if( !config.isBedSetHome() )
            return;
        
        // we do nothing further if the player is sneaking
        if( event.getPlayer().isSneaking() )
            return;

        // we only handle events if BED_HOME_MUST_BE_NIGHT config is true, otherwise
        // the PlayerInteractEvent handler takes care of it.
        if( config.isBedHomeMustBeNight() ) {
            log.debug("bedEvent: calling doBedSet for player ",event.getPlayer());
            if( bedUtil.doBedClick(event.getPlayer(), event.getBed()) )
                event.setCancelled(true);
        }
        
    }

    @Override
    public void playerQuit(PlayerQuitEvent event) {
        spawnUtil.updateQuitLocation(event.getPlayer());
    }

    @Override
    public void playerKick(PlayerKickEvent event) {
        spawnUtil.updateQuitLocation(event.getPlayer());
    }
    
    @Override
    public void playerDamage(PlayerDamageEvent event) {
        warmupManager.processEntityDamage(event);
    }

    @Override
    public void playerMove(PlayerMoveEvent event) {
        // TODO Auto-generated method stub
        
    }
}
