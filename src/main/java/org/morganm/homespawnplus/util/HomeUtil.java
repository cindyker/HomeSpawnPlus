/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.HSPMessages;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.manager.HomeLimitsManager;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.Server;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common routines related to management of Homes.
 * 
 * @author morganm
 *
 */
@Singleton
public class HomeUtil {
    private final Logger log = LoggerFactory.getLogger(HomeUtil.class);
    
    private final Storage storage;
    private final ConfigCore configCore;
    private final Server server;
    private final BedUtils bedUtil;
    private final HomeLimitsManager limits;
    
    @Inject
    public HomeUtil(Storage storage, ConfigCore configCore, Server server, BedUtils bedUtil,
            HomeLimitsManager limits)
    {
        this.storage = storage;
        this.configCore = configCore;
        this.server = server;
        this.bedUtil = bedUtil;
        this.limits = limits;
    }

    /** Return the home location of the given player and world.
     * 
     * @param playerName
     * @param worldName
     * @return the home location or null if no home is set
     */
    public Home getDefaultHome(String playerName, String worldName)
    {
        Home home = storage.getHomeDAO().findDefaultHome(worldName, playerName);
        
        // if there is no default home defined and the LAST_HOME_IS_DEFAULT flag is
        // set, check to see if there is a single home left on the world that we can
        // assume is the default.
        if( home == null && configCore.isLastHomeDefault() ) {
            Set<Home> homes = storage.getHomeDAO().findHomesByWorldAndPlayer(worldName, playerName);
            if( homes != null && homes.size() == 1 )
                home = homes.iterator().next();
        }
        
        return home;
    }
    
    /** Set a named home for a player.
     * 
     * @param playerName
     * @param l
     * @param homeName
     * @param updatedBy
     * 
     * @return null if success, a localized player-friendly error message if no success
     */
    public String setNamedHome(String playerName, Location l, String homeName, String updatedBy)
    {
        Home home = storage.getHomeDAO().findHomeByNameAndPlayer(homeName, playerName);
        
        // could be null if we are working with an offline player
        Player p = server.getPlayer(playerName);
        
        // if we get an object back, we already have a Home set for this player/homeName combo,
        // so we just update the x/y/z location of it.
        if( home != null ) {
            // if the world changed, then we need to check world limits on the new world
            if( p != null && !l.getWorld().getName().equals(home.getWorld()) ) {
                if( !limits.canPlayerAddHome(p, l.getWorld().getName()) )
                    return limits.getLimitMessage(p, l.getWorld().getName());
            }
            
            home.setLocation(l);
            home.setUpdatedBy(updatedBy);
        }
        // this is a new home for this player/world combo, create a new object
        else {
            // check if they are allowed to add another home
            if( p != null && !limits.canPlayerAddHome(p, l.getWorld().getName()) )
                return limits.getLimitMessage(p, l.getWorld().getName());
            
            home = new Home(playerName, l, updatedBy);
            home.setName(homeName);
        }
        
        try {
            storage.getHomeDAO().saveHome(home);
        }
        catch(StorageException e) {
            log.warn("Error saving home", e);
            return server.getLocalizedMessage(HSPMessages.GENERIC_ERROR);
        }
        return null;
    }

    /** Set a players default or bed home.
     * 
     * @param playerName the player whose home is being set
     * @param l the location to set the home to
     * @param updatedBy the player the updated, if null it will be the player
     * @param defaultHome set to true if this home should have the defaultHome flag
     * @param bedHome set to true if this home should have the bedHome flag
     * 
     * @return null if success, a localized player-friendly error message if no success
     */
    public String setHome(String playerName, Location l, String updatedBy, boolean defaultHome, boolean bedHome)
    {
        final HomeDAO homeDAO = storage.getHomeDAO();
        Home home = homeDAO.findDefaultHome(l.getWorld().getName(), playerName);
        
        log.debug("setHome: (defaultHome) home={}",home);
        
        // if bedHome arg is set and the defaultHome is NOT the bedHome, then try to find an
        // existing bedHome that we can overwrite (should only be one bedHome per world)
        if( bedHome && (home == null || !home.isBedHome()) ) {
            home = homeDAO.findBedHome(l.getWorld().getName(), playerName);
            
            // if no bed home was found using existing bed name, check all other bed homes
            // for the bed flag
            if( home != null && !home.isBedHome() ) {
                Set<Home> homes = homeDAO.findHomesByWorldAndPlayer(l.getWorld().getName(), playerName);
                if( homes != null ) {
                    for(Home h : homes) {
                        if( h.isBedHome() ) {
                            home = h;
                            break;
                        }
                    }
                }
            }
            
            if( home == null && configCore.isBedHomeOverwriteDefault() ) {
                log.debug("setHome: bedHome flag enabled, but no bedHome found. Using default home");
                home = homeDAO.findDefaultHome(l.getWorld().getName(), playerName);
            }
        }
        
        // could be null if we are working with an offline player
        Player p = server.getPlayer(playerName);
        
        // if we get an object back, we already have a Home set for this player/world combo, so we
        // just update the x/y/z location of it.
        if( home != null ) {
            if( limits.isSingleGlobalHomeEnabled(l.getWorld().getName(), playerName) ) {
                home = limits.enforceSingleGlobalHome(playerName);
                
                // it's possible enforceSingleGlobalHome() just wiped all of our homes
                if( home == null ) {
                    home = new Home(playerName, l, updatedBy);
                }
            }
            // if the world changed, then we need to check world limits on the new world
            else if( p != null && !l.getWorld().getName().equals(home.getWorld()) ) {
                if( !limits.canPlayerAddHome(p, l.getWorld().getName()) )
                    return limits.getLimitMessage(p, l.getWorld().getName());
            }
            
            home.setLocation(l);
            home.setUpdatedBy(updatedBy);
        }
        // this is a new home for this player/world combo, create a new object
        else {
            if( limits.isSingleGlobalHomeEnabled(l.getWorld().getName(), playerName) ) {
                home = limits.enforceSingleGlobalHome(playerName);
                if( home != null ) {
                    home.setLocation(l);
                    home.setUpdatedBy(updatedBy);
                }
            }
            // check if they are allowed to add another home
            else if( p != null && !limits.canPlayerAddHome(p, l.getWorld().getName()) ) {
                return limits.getLimitMessage(p, l.getWorld().getName());
            }
            
            // it's possible singleGlobalHome code has found/created a home object for us now
            if( home == null )
                home = new Home(playerName, l, updatedBy);
        }
        
        // we don't set the value directly b/c the way to turn "off" an existing defaultHome is to
        // just set another one.
        if( defaultHome )
            home.setDefaultHome(true);
        
        if( bedHome ) {
            home.setName(l.getWorld().getName() + "_" + Storage.HSP_BED_RESERVED_NAME);
            bedUtil.setBukkitBedHome(p, l);
        }
        // we don't allow use of the reserved suffix "_bed" name unless the bed flag is true
        else if( home.getName() != null && home.getName().endsWith("_" + Storage.HSP_BED_RESERVED_NAME) ) {
            home.setName(null);
        }
        home.setBedHome(bedHome);
        
        log.debug("setHome() pre-commit, home={}",home);

        try {
            homeDAO.saveHome(home);
        }
        catch(StorageException e) {
            log.warn("Error saving home", e);
            return server.getLocalizedMessage(HSPMessages.GENERIC_ERROR);
        }
        
        return null;
    }

    /** Look for a partial name match for a home on a given world
     * 
     * @param playerName
     * @param worldName
     * @return the Home object or null if none found
     */
    public Home getBestMatchHome(String playerName, String worldName) {
        Set<Home> homes = storage.getHomeDAO().findAllHomes();
        
        // first find any possible homes based on the input
        ArrayList<Home> possibles = new ArrayList<Home>();
        for(Home home : homes) {
            String homeOwner = home.getPlayerName();
            if( worldName.equals(home.getWorld()) && homeOwner.contains(playerName) ) {
                possibles.add(home);
            }
        }
        
        if( possibles.size() == 0 )
            return null;
        else if( possibles.size() == 1 )
            return possibles.get(0);
        
        Home bestMatch = null;
        // now find the best match out of all the possibilities.  Could have fancier algorithm later,
        // but for now it just returns the first name it finds that startswith the player input.
        for(Home home : possibles) {
            String homeOwner = home.getPlayerName();
            if( homeOwner.startsWith(playerName) ) {
                bestMatch = home;
                break;
            }
        }
        // still no match out of the possibilities?  just take the first one on the list
        if( bestMatch == null )
            bestMatch = possibles.get(0);
        
        return bestMatch;
    }
}
