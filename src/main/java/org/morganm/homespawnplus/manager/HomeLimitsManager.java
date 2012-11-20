/**
 * 
 */
package org.morganm.homespawnplus.manager;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.inject.Inject;

import org.bukkit.configuration.ConfigurationSection;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.old.Config;
import org.morganm.homespawnplus.config.old.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manager class for home limits.
 * 
 * TODO: eliminate Bukkit configuration dependency.
 * 
 * @author morganm
 *
 */
public class HomeLimitsManager {
    private final Logger log = LoggerFactory.getLogger(HomeLimitsManager.class);
    
    @Inject private Storage storage;
    
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
     */
    public int getHomeLimit(Player player, String worldName, boolean perWorldLimit) {
        int limit = -2;
        
        String limitKey = null;
        if( perWorldLimit )
            limitKey = ConfigOptions.HOME_LIMITS_PER_WORLD;
        else
            limitKey = ConfigOptions.HOME_LIMITS_GLOBAL;
        
        log.debug("getHomeLimit(), player = {}, worldName = {}, limitKey = {}", player, worldName, limitKey);
        
        Config config = plugin.getHSPConfig();
        
        // check permissions section; we iterate through the permissions of each section
        // and see if this player has that permission
        ConfigurationSection section = config.getConfigurationSection(
                ConfigOptions.HOME_LIMITS_BASE + "permission");
        if( section != null ) {
            Set<String> sections = section.getKeys(false);
            if( sections != null ) {
                for(String key : sections) {
                    log.debug("found limit section {}, checking permissions for section", key);
                    List<String> perms = config.getStringList(ConfigOptions.HOME_LIMITS_BASE
                            + "permission." + key + ".permissions", null);
                    if( perms != null ) {
                        for(String perm : perms) {
                            log.debug("checking permission {} for player {}", perm, player);
                            if( player.hasPermission(perm) ) {
                                limit = config.getInt(ConfigOptions.HOME_LIMITS_BASE
                                        + "permission." + key + "." + limitKey, -2);

                                log.debug("{} limit for permission {} = {}", limitKey, perm, limit);

                                if( limit != -2 )
                                    break;
                            }
                        }
                    }
                    
                    if( limit != -2 ) {
                        log.debug("Limit value of {} found as a result of section {}; stopping limit search",
                                limit, key);
                        break;
                    }
                }
            }
        }
        
        // try specific world setting if we haven't found a limit yet
        if( limit == -2 ) {
            limit = config.getInt(ConfigOptions.HOME_LIMITS_BASE + "world." + worldName + "." + limitKey, -2);
            log.debug("{} limit for world {} = {}", limitKey, worldName, limit);
        }
        
        if( limit == -2 ) {
            limit = config.getInt(ConfigOptions.HOME_LIMITS_BASE
                    + ConfigOptions.HOME_LIMITS_DEFAULT + "." + limitKey, -2);
            log.debug("{} default limit = {}", limitKey, limit);
        }
        
        // if we get to here and still haven't found a value, we assume a sane default of 1
        if( limit == -2 )
            limit = 1;
        
        log.debug("getHomeLimit() returning {} limit {} for player {}", limitKey, limit, player);
        
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
        if( plugin.getHSPConfig().getBoolean(ConfigOptions.SINGLE_GLOBAL_HOME, false) &&
                !plugin.hasPermission(world, playerName, HomeSpawnPlus.BASE_PERMISSION_NODE+".singleGlobalHomeExempt") ) {
            return true;
        }
        else
            return false;
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
