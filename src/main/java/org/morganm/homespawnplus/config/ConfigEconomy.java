/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.YamlFile;

/** Configuration related to economy costs.
 * 
 * @author morganm
 *
 */
public class ConfigEconomy implements ConfigInterface {
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigEconomy(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("economy.yml");
    }

    @Override
    public void load() throws IOException, FileNotFoundException, ConfigException {
        yaml.load(file);
    }

    /**
     * Determine whether or not economy has been enabled by admin.
     * 
     * @return true if economy is enabled
     */
    public boolean isEnabled() {
        return yaml.getBoolean("enabled");
    }

    /**
     * Determine if we should print messages when charges happen.
     * 
     * @return true if verbose messages should be used
     */
    public boolean isVerboseOnCharge();
    
    /**
     * Return a list of worlds that have world-specific costs.
     * 
     * @return
     */
    public List<String> getWorldsWithSpecificCosts();
    
    /**
     * Return world-specific costs for a given command.
     * 
     * @return
     */
    public int getWorldSpecificCost(String world, String command);

    /**
     * Return a list of permissions that have permission-specific costs. The list
     * is returned in the order the permissions should be processed.
     * 
     * @return
     */
    public List<String> getPermissionsWithSpecificCosts();
    
    /**
     * Return permission-specific costs for a given command. In the event that
     * multiple permissions are mapped to different costs, this call returns
     * the one with the highest priority (whichever one appears first in the
     * config).
     * 
     * @return
     */
    public int getPermissionSpecificCost(String permission, String command);
}
