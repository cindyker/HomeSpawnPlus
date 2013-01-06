/**
 * 
 */
package org.morganm.homespawnplus.server.api.impl;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigEconomy;
import org.morganm.homespawnplus.config.ConfigEconomy.PerPermissionEconomyEntry;
import org.morganm.homespawnplus.manager.HomeLimitsManager;
import org.morganm.homespawnplus.server.api.Economy;
import org.morganm.homespawnplus.server.api.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morganm
 *
 */
public abstract class EconomyAbstractImpl implements Economy {
    protected static final Logger log = LoggerFactory.getLogger(EconomyAbstractImpl.class);
    protected ConfigEconomy config;
    protected HomeLimitsManager homeLimitsManager;
    
    @Inject public void setConfigEconomy(ConfigEconomy config) { this.config = config; }
    @Inject public void setHomeLimitsManager(HomeLimitsManager hlm) { this.homeLimitsManager = hlm; }
    
    public int getCommandCost(Player player, String command) {
        Integer cost = null;
        
        Map<String, PerPermissionEconomyEntry> map = config.getPerPermissionEntries();

        PER_PERMISSION:
        for(Map.Entry<String, PerPermissionEconomyEntry> e : map.entrySet()) {
            PerPermissionEconomyEntry entry = e.getValue();
            
            Map<String, Integer> costs = entry.getCosts();
            Integer entryCost = costs.get(command);
            // if this command is not listed in this entry, skip it
            if( entryCost == null )
                continue;
            
            // command is listed, see if player has a matching permission
            List<String> permissions = entry.getPermissions();
            for(String perm : permissions) {
                if( player.hasPermission(perm) ) {
                    cost = entryCost;
                    break PER_PERMISSION;
                }
            }
        }
        
        // if we get here, there was no permission-specific cost, so look for
        // a world-specific cost instead
        cost = config.getWorldSpecificCost(player.getWorld().getName(), command);
        if( cost != null )
            return cost;
        
        // if we get here and no cost yet, then get the global cost (possibly 0)
        if( cost == null )
            cost = config.getGlobalCost(command);

        // apply sethome-multiplier, if any
        if( cost > 0 && command.equalsIgnoreCase("sethome") ) {
            double multiplier = config.getSethomeMultiplier();
            if( multiplier > 0 )
            {
                // by the time this method is called, the new home has already been created,
                // so it is already part of our globalHomeCount
                int globalHomeCount = homeLimitsManager.getHomeCount(player.getName(), null);
                if( globalHomeCount > 1 ) {
                    double totalCost = cost;
                    for(int i=1; i < globalHomeCount; i++)
                        totalCost *= multiplier; 
                    double additionalCost = totalCost - cost;
                    log.debug("applying sethome-multplier {} for player {}"
                            + ", total global home count={}, original cost={}, additionalCost={}",
                            multiplier, player, globalHomeCount, cost, additionalCost);
                    // should always be true, but check just in case
                    if( additionalCost > 0 )
                        cost += (int) additionalCost;
                }
            }
        }
        
        return cost;
    }
}
