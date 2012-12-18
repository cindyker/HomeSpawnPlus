/**
 * 
 */
package org.morganm.homespawnplus.server.api.impl;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigEconomy;
import org.morganm.homespawnplus.config.ConfigEconomy.PerPermissionEconomyEntry;
import org.morganm.homespawnplus.server.api.Economy;
import org.morganm.homespawnplus.server.api.Player;

/**
 * @author morganm
 *
 */
public abstract class EconomyAbstractImpl implements Economy {
    protected ConfigEconomy config;
    
    @Inject public void setConfigEconomy(ConfigEconomy config) { this.config = config; }
    
    public int getCommandCost(Player player, String command) {
        Map<String, PerPermissionEconomyEntry> map = config.getPerPermissionEntries();
        for(Map.Entry<String, PerPermissionEconomyEntry> e : map.entrySet()) {
            PerPermissionEconomyEntry entry = e.getValue();
            
            Map<String, Integer> costs = entry.getCosts();
            Integer cost = costs.get(command);
            // if this command is not listed in this entry, skip it
            if( cost == null )
                continue;
            
            // command is listed, see if player has a matching permission
            List<String> permissions = entry.getPermissions();
            for(String perm : permissions) {
                if( player.hasPermission(perm) ) {
                    return cost;
                }
            }
        }
        
        // if we get here, there was no permission-specific cost, so look for
        // a world-specific cost instead
        Integer cost = config.getWorldSpecificCost(player.getWorld().getName(), command);
        if( cost != null )
            return cost;
        
        // if we get here, just return the default cost (possibly 0)
        return config.getGlobalCooldown(command);
    }
}
