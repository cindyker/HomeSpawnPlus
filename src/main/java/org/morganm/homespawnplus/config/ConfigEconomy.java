/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.server.api.YamlFile;

/** Configuration related to economy costs.
 * 
 * @author morganm
 *
 */
@Singleton
public class ConfigEconomy extends AbstractConfigBase implements ConfigInterface, Initializable {
    @Inject
    public ConfigEconomy(YamlFile yaml) {
        super("economy.yml", "cost", yaml);
    }

    /**
     * Determine whether or not economy has been enabled by admin.
     * 
     * @return true if economy is enabled
     */
    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }

    /**
     * Determine if we should print messages when charges happen.
     * 
     * @return true if verbose messages should be used
     */
    public boolean isVerboseOnCharge() {
        return super.getBoolean("verbose");
    }
    
    /**
     * Return a list of worlds that have world-specific costs.
     * 
     * @return
     */
    public List<String> getWorldsWithSpecificCosts() {
        // TODO: something
        return null;
    }
    
    /**
     * Return world-specific costs for a given command.
     * 
     * @return
     */
    public int getWorldSpecificCost(String world, String command) {
        // TODO: something
        return 0;
    }

    /**
     * Return a list of permissions that have permission-specific costs. The list
     * is returned in the order the permissions should be processed.
     * 
     * @return
     */
    public List<String> getPermissionsWithSpecificCosts() {
        // TODO: something
        return null;
    }
    
    /**
     * Return permission-specific costs for a given command. In the event that
     * multiple permissions are mapped to different costs, this call returns
     * the one with the highest priority (whichever one appears first in the
     * config).
     * 
     * @return
     */
    public int getPermissionSpecificCost(String permission, String command) {
        // TODO: something
        return 0;
    }
}
