/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.morganm.homespawnplus.server.api.YamlFile;

/** This class generically implements the "config-per-world"
 * and "config-per-permission" patterns for use by other
 * config objects.
 * 
 * @author morganm
 *
 */
public abstract class ConfigPerXBase<P extends PerPermissionEntry, W extends PerWorldEntry> extends ConfigBase {
    protected Map<String, P> perPermissionEntries = new LinkedHashMap<String, P>();
    protected Map<String, W> perWorldEntries = new LinkedHashMap<String, W>();

    /**
     * 
     * @param fileName the default filename of the config file, such as "cooldown.yml"
     * @param basePath the root path of the YAML file, such as "cooldowns"
     * @param yaml the yaml file to use
     */
    protected ConfigPerXBase(String fileName, String basePath, YamlFile yaml) {
        super(fileName, basePath, yaml);
    }

    public void load() throws IOException, FileNotFoundException, ConfigException {
        super.load();
        populatePerWorldEntries();
        populatePerPermissionEntries();
    }

    /**
     * Should be overridden by the subclass to instantiate a new
     * PerPermissionEntry object that is specific to the subclass.
     * 
     * @return
     */
    protected abstract P newPermissionEntry();

    /**
     * Should be overridden by the subclass to instantiate a new
     * PerWorldEntry object that is specific to the subclass.
     * 
     * @return
     */
    protected abstract W newWorldEntry();
    
    /**
     * Return the per-permissions entries map.
     * 
     * @return guaranteed not to be null
     */
    public final Map<String, P> getPerPermissionEntries() {
        return perPermissionEntries;
    }

    /**
     * Return the per-world entries map.
     * 
     * @return guaranteed not to be null
     */
    public final W getPerWorldEntry(String worldName) {
        return perWorldEntries.get(worldName);
    }

    /**
     * Method to process the config and load into memory any per-permissions
     * config data for simple and efficient use later.
     */
    protected void populatePerPermissionEntries() {
        final String base = "permission";

        // linkedHashMap to maintain the ordering as defined in the config file
        perPermissionEntries = new LinkedHashMap<String, P>();
        
        Set<String> entries = super.getKeys(base);
        for(String entryName : entries) {
            final String baseEntry = base + "." + entryName;
            
            P entry = newPermissionEntry();
            perPermissionEntries.put(entryName, entry);
            
            // grab permissions related to this entry
            List<String> permissions = super.getStringList(baseEntry+".permissions");
            
            // if no explicit permissions were defined, then setup the default ones
            if( permissions == null || permissions.size() == 0 ) {
                permissions = new ArrayList<String>(3);
                
                permissions.add("hsp."+basePath+"."+entryName);         // add basePath permission
                permissions.add("hsp.entry."+entryName);                // add default entry permission
                permissions.add("group."+entryName);                    // add convenience group entry
            }

            // make map immutable to protect against external changes
            entry.setPermissions(Collections.unmodifiableList(permissions));

            // now look for individual settings
            for(String key : super.getKeys(baseEntry)) {
                if( key.equals("permissions") ) {   // skip, we already got these
                    continue;
                }
                
                // assign property
                entry.setValue(key, super.get(baseEntry+"."+key));
            }
            
            entry.finishedProcessing();
        }
        
        // make map immutable to protect against external changes
        perPermissionEntries = Collections.unmodifiableMap(perPermissionEntries);
    }

    /**
     * Method to process the config and load into memory any per-world
     * config data for simple and efficient use later.
     */
    protected void populatePerWorldEntries() {
        final String base = "world";

        // linkedHashMap to maintain the ordering as defined in the config file
        perWorldEntries = new LinkedHashMap<String, W>();
        
        Set<String> worlds = super.getKeys(base);
        for(String worldName : worlds) {
            final String baseEntry = base + "." + worldName;
            
            W entry = newWorldEntry();
            entry.setWorld(worldName);
            perWorldEntries.put(worldName, entry);

            // now look for individual settings
            for(String key : super.getKeys(baseEntry)) {
                // assign property
                entry.setValue(key, super.get(baseEntry+"."+key));
            }
            
            entry.finishedProcessing();
        }
        
        // make map immutable to protect against external changes
        perWorldEntries = Collections.unmodifiableMap(perWorldEntries);
    }
}
