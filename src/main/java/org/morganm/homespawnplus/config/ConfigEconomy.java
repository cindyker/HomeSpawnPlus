/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<String, PerPermissionEconomyEntry> perPermissionEntries;
    
    @Inject
    public ConfigEconomy(YamlFile yaml) {
        super("economy.yml", "cost", yaml);
    }

    public void load() throws IOException, FileNotFoundException, ConfigException {
        super.load();
        populatePerPermissionEntries();
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
     * For a given command, return it's global cost (exclusive of
     * per-permission and per-world cost values).
     * 
     * @param cooldown
     * @return
     */
    public int getGlobalCooldown(String command) {
        final int cost = super.getInt(command);
        if( cost > 0 )
            return cost;
        else
            return 0;
    }

    /**
     * Return world-specific costs for a given command.
     * 
     * @return The cost for the command, or null if no cost was defined
     */
    public Integer getWorldSpecificCost(String world, String command) {
        final String key = "world."+world+"."+command;
        if( super.contains(key) ) {
            final int cost = super.getInt(key);
            if( cost > 0 )
                return cost;
            else
                return 0;
        }
        else
            return null;
    }

    /**
     * Return permission-specific costs.
     * 
     * @return
     */
    public Map<String, PerPermissionEconomyEntry> getPerPermissionEntries() {
        return perPermissionEntries;
    }
    
    /**
     * Method to process the config and load into memory any per-permissions
     * config data for simple and efficient use later.
     */
    private void populatePerPermissionEntries() {
        final String base = "permission";

        perPermissionEntries = new LinkedHashMap<String, PerPermissionEconomyEntry>();
        
        Set<String> entries = super.getKeys(base);
        for(String entryName : entries) {
            final String baseEntry = base + "." + entryName;
            
            PerPermissionEconomyEntry entry = new PerPermissionEconomyEntry();
            perPermissionEntries.put(entryName, entry);
            
            // grab permissions related to this entry
            entry.permissions = super.getStringList(baseEntry+".permissions");
            if( entry.permissions == null ) {
                entry.permissions = new ArrayList<String>(1);
            }
            entry.permissions.add("hsp.cost."+entry);     // add default entry permission

            // make map immutable to protect against external changes
            entry.permissions = Collections.unmodifiableList(entry.permissions);

            // now look for individual entry settings
            for(String key : super.getKeys(baseEntry)) {
                if( key.equals("permissions") ) {   // skip, we already got these
                    continue;
                }
                
                // everything else is a cost entry
                entry.costs.put(key, super.getInt(baseEntry+"."+key));
            }
            
            // make map immutable to protect against external changes
            entry.costs = Collections.unmodifiableMap(entry.costs);
        }
        
        // make map immutable to protect against external changes
        perPermissionEntries = Collections.unmodifiableMap(perPermissionEntries);
    }

    public class PerPermissionEconomyEntry {
        protected List<String> permissions;
        protected Map<String, Integer> costs = new HashMap<String, Integer>();
        
        public List<String> getPermissions() { return permissions; }
        public Map<String, Integer> getCosts() { return costs; }
    }
}
