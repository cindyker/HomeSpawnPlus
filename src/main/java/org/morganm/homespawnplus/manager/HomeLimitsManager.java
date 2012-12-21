/**
 * 
 */
package org.morganm.homespawnplus.manager;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigHomeLimits;
import org.morganm.homespawnplus.config.ConfigHomeLimits.LimitsPerPermission;
import org.morganm.homespawnplus.config.ConfigHomeLimits.LimitsPerWorld;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manager class for home limits.
 * 
 * @author morganm
 *
 */
public class HomeLimitsManager {
    private final Logger log = LoggerFactory.getLogger(HomeLimitsManager.class);
    
    @Inject private Storage storage;
    @Inject private ConfigHomeLimits config;
    
    private LimitReason limitCheck(Player p, String worldName) {
        int limit = getHomeLimit(p, worldName, true);
        int currentCount = getHomeCount(p.getName(), worldName);
        if( limit != -1 && currentCount + 1 > limit ) {
            return new LimitReason(LimitReason.Type.WORLD_LIMIT, limit);
        }
        
        // check global limit
        limit = getHomeLimit(p, null, false);
        currentCount = getHomeCount(p.getName(), null);
        if( limit != -1 && currentCount + 1 > limit ) {
            return new LimitReason(LimitReason.Type.GLOBAL_LIMIT, limit);
        }
        
        return LimitReason.NOT_AT_LIMIT;
    }
    
    /** Return true if the player has at least one free home slot (perWorld and global).
     * 
     * @param player
     * @param worldName
     * @param printMessage if true and the player is over the limit, a message will be pritned
     * to the player to tell them they are over the limit
     * @return
     */
    public boolean canPlayerAddHome(Player p, String worldName) {
        return limitCheck(p, worldName) == LimitReason.NOT_AT_LIMIT;
    }

    /**
     * If a player is at their home limit, return the message that should be
     * printed to the player to let them know why.
     * 
     * @param p
     * @param worldName
     * @return the reason or null if they are not at the limit
     */
    public String getLimitMessage(Player p, String worldName) {
        LimitReason reason = limitCheck(p, worldName);
        
        if( reason.type == LimitReason.Type.WORLD_LIMIT ) {
            return "You are at your limit of "+reason.limit+" homes for world \""+worldName+"\"";
        }
        else if( reason.type == LimitReason.Type.GLOBAL_LIMIT ) {
            return "You are at your global limit of "+reason.limit+" homes";
        }
        else {
            return null;
        }
    }

    /** Get the home count for a given player/world combo. Passing null for the worldName
     * will return the global count. 
     * 
     * @param playerName
     * @param worldName
     * @return
     */
    public int getHomeCount(String playerName, String worldName)
    {
        Set<Home> homes = storage.getHomeDAO().findHomesByWorldAndPlayer(worldName, playerName);
        
        if( homes != null )
            return homes.size();
        else
            return 0;
    }
    
    /**
     * 
     * @param playerName
     * @param world if world is null, the global value is used instead
     * @param perWorldLimit true if you want the perWorld limit, false if you want the global limit
     * 
     * @return --1 if limit is infinite or a number >= 0 that is the limit to be used
     */
    public int getHomeLimit(Player player, String worldName, boolean perWorldLimit) {
        Integer limit = null;
        
        log.debug("getHomeLimit() player = {}, worldName = {}, perWorldLimit = {}", player, worldName, perWorldLimit);
        
        // check per-permission entries
        MATCH_FOUND:
        for(Map.Entry<String, LimitsPerPermission> entry : config.getPerPermissionEntries().entrySet()) {
            Integer value = null;
            if( perWorldLimit )
                value = entry.getValue().getPerWorld();
            else
                value = entry.getValue().getGlobal();

            // only if there is a limit value for this entry do we do any extra processing
            if( value != null && value > 0 ) {
                // ok now check to see if player has a permisson in the list
                for(String perm : entry.getValue().getPermissions()) {
                    log.debug("processing per-permission permission {}", perm);
                    if( player.hasPermission(perm) ) {
                        limit = value;
                        break MATCH_FOUND;
                    }
                }
            }
        }
        log.debug("getHomeLimit() post-permission limit={}", limit);
        
        // we only check per-world limits if no per-permission limit was defined
        if( limit == null && worldName != null ) {
            LimitsPerWorld entry = config.getPerWorldEntry(worldName);
            if( perWorldLimit )
                limit = entry.getPerWorld();
            else
                limit = entry.getGlobal();

            log.debug("getHomeLimit() limit for world {} = {}", worldName, limit);
        }
        
        // use default setting if no limit defined yet
        if( limit == null ) {
            if( perWorldLimit )
                limit = config.getDefaultPerWorldLimit();
            else
                limit = config.getDefaultGlobalLimit();

            log.debug("getHomeLimit() default limit = {}", limit);
        }
        
        // if we get to here and still haven't found a value, we assume a sane default of 1
        if( limit == null || limit < 0 )
            limit = 1;
        
        log.debug("getHomeLimit() returning limit {} for player {}", limit, player);
        return limit;
    }

    /**
     * Method used to enforce single global home restriction. Given a player name, this
     * will always return the same (single) home object.
     * 
     * @param playerName
     * @return
     */
    public Home enforceSingleGlobalHome(String playerName) {
        Home home = null;
        
        log.debug("enforceSingleGlobalHome() ENTER");
        Set<Home> homes = storage.getHomeDAO().findHomesByPlayer(playerName);
        
        if( homes != null ) {
            // if we find a single home in the DB, move it to the new location
            if( homes.size() == 1 ) {
                log.debug("enforceSingleGlobalHome() found 1 home, updated it");
                home = homes.iterator().next();
            }
            // otherwise, delete all homes and a new one will be created below
            else {
                log.debug("enforceSingleGlobalHome() found multiple homes, removing them");
                for(Home h : homes) {
                    log.debug("enforceSingleGlobalHome() removing home {}",h);
                    try {
                        storage.getHomeDAO().deleteHome(h);
                    }
                    catch(StorageException e) {
                        log.warn("Error caught in enforceSingleGlobalHome", e);
                    }
                }
            }
        }
        
        log.debug("enforceSingleGlobalHome() EXIT, home={}",home);
        return home;
    }
    
    /** Check if we should enforce singleGlobalHome behavior for a given player.
     * 
     * @param world
     * @param playerName
     * @return
     */
    public boolean isSingleGlobalHomeEnabled(String world, String playerName) {
        return config.isSingleGlobalHome();
    }
    
    private static class LimitReason {
        private static LimitReason NOT_AT_LIMIT = new LimitReason(Type.NOT_AT_LIMIT, 0);
        
        private static enum Type {
            NOT_AT_LIMIT,
            WORLD_LIMIT,
            GLOBAL_LIMIT };
        
        public Type type;
        public int limit;
        public LimitReason(Type type, int limit) {
            this.type = type;
            this.limit = limit;
        }
    };
}
