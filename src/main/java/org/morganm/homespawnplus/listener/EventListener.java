/**
 * 
 */
package org.morganm.homespawnplus.listener;

import javax.inject.Inject;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.events.PlayerJoinEvent;
import org.morganm.homespawnplus.server.api.events.PlayerRespawnEvent;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyEngine;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morganm
 *
 */
public class EventListener implements org.morganm.homespawnplus.server.api.events.EventListener {
    private final Logger log = LoggerFactory.getLogger(EventListener.class);
    private Storage storage;
    private StrategyEngine engine;
    private ConfigCore config;
    
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
    public EventListener(ConfigCore config, Storage storage, StrategyEngine engine) {
        this.config = config;
        this.storage = storage;
        this.engine = engine;
    }
    
    @Override
    public void playerJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        final boolean isNewPlayer = p.isNewPlayer();
        
        if( isNewPlayer ) {
            if( config.isVerboseLogging() )
                HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " New player "+p.getName()+" detected.");
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
}
