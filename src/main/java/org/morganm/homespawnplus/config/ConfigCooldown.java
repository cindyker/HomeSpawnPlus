/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
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

import org.morganm.homespawnplus.server.api.YamlFile;

/**
 * @author morganm
 *
 */
@Singleton
public class ConfigCooldown implements ConfigInterface {
    private static final String RESET_ON_DEATH = "resetOnDeath";
    
    private final YamlFile yaml;
    private final File file;
    
    private Map<String, PerPermissionCooldownEntry> perPermissionEntries;
    
    @Inject
    public ConfigCooldown(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("cooldown.yml");
    }

    /**
     * Load (or reload) the configuration from the backing store.
     * 
     * @throws Exception
     */
    public void load() throws IOException, FileNotFoundException, ConfigException {
        yaml.load(file);
        populatePerPermissionEntries();
    }

    public boolean isEnabled() {
        return yaml.getBoolean("enabled");
    }
    
    /**
     * Return a list of cooldowns that are on separate timers.
     * 
     * @return
     */
    public List<String> getSeparateCooldowns() {
        return yaml.getStringList("separation");
    }
    
    /**
     * Determine if global cooldowns should reset on player death.
     * 
     * @return
     */
    public boolean isGlobalResetOnDeath() {
        return yaml.getBoolean(RESET_ON_DEATH);
    }
    
    /**
     * Determine if the admin has configured the resetOnDeath flag. For this
     * check it doesn't matter whether it's set to true or false, only that
     * it has been explicitly set.
     * 
     * @param world
     * @return
     */
    public boolean hasWorldResetOnDeathFlag(String world) {
        return yaml.contains("world."+world+"."+RESET_ON_DEATH);
    }

    /**
     * Determine whether a world's cooldowns should reset on death.
     * @param world
     * @return
     */
    public boolean isWorldResetOnDeath(String world) {
        return yaml.getBoolean("world."+world+"."+RESET_ON_DEATH);
    }

    /**
     * Return all per-permission permissions related to given cooldowns.
     * 
     * @param cooldowns the list of cooldowns to check
     * @return
     */
    /*
    public Set<String> getPerPermissionCooldownEntries(String[] cooldowns) {
        Set<String> returnPerms = new LinkedHashSet<String>();
        
        Set<String> entries = yaml.getKeys("permission");
        for(String entry : entries) {
            for(String cooldown : cooldowns) {
                // skip sections that don't apply to this cooldown
                final int cooldownValue = yaml.getInt("permission."+entry+"."+cooldown);
                if( cooldownValue <= 0 )
                    continue;
                
                List<String> perms = yaml.getStringList("permission."+entry+".permissions");
                if( perms == null ) {
                    perms = new ArrayList<String>(1);
                }
                perms.add("hsp.cooldown."+entry);     // add default entry permission
                
                returnPerms.addAll(perms);
            }
        }
        
        return returnPerms;
    }
    */

    /**
     * Given an entry key, return the permissions associated with that entry.
     * 
     * @param entry
     * @return
     */
    /*
    public Set<String> getPerPermmissionEntryPerms(String entry) {
        for(String cooldown : cooldowns) {
            // skip sections that don't apply to this cooldown
            final int cooldownValue = yaml.getInt("permission."+entry+"."+cooldown);
            if( cooldownValue <= 0 )
                continue;
            
            List<String> perms = yaml.getStringList("permission."+entry+".permissions");
            if( perms == null ) {
                perms = new ArrayList<String>(1);
            }
            perms.add("hsp.cooldown."+entry);     // add default entry permission
            
            returnPerms.addAll(perms);
        }
    }
    */
    
    /**
     * For a given permission & cooldowns combo, return the applicable timer.
     * 
     * @param cooldowns the list of cooldowns to check
     * @param permission
     * @return
     */
    /*
    public int getPerPermissionCooldown(String[] cooldowns, String permission) {
        Set<String> entries = yaml.getKeys("permission");
        for(String entry : entries) {
            for(String cooldown : cooldowns) {
                // skip sections that don't apply to this cooldown
                final int cooldownValue = yaml.getInt("permission."+entry+"."+cooldown);
                if( cooldownValue <= 0 )
                    continue;
    
                List<String> perms = yaml.getStringList("permission."+entry+".permissions");
                if( perms == null ) {
                    perms = new ArrayList<String>(1);
                }
                perms.add("hsp.cooldown."+entry);     // add default entry permission
                
                if( perms.contains(permission) ) {
                    return cooldownValue;
                }
            }
        }
        
        return 0;
    }
    */

    /**
     * For a given world & cooldown combo, return the applicable timer.
     * 
     * @param cooldown
     * @param world
     * @return
     */
    public int getPerWorldCooldown(String cooldown, String world) {
        final int cooldownValue = yaml.getInt("world."+world+"."+cooldown);
        if( cooldownValue > 0 )
            return cooldownValue;
        else
            return 0;
    }
    
    /**
     * For a given cooldown, return it's global cooldown time (exclusive of
     * per-permission and per-world cooldown values).
     * 
     * @param cooldown
     * @return
     */
    public int getGlobalCooldown(String cooldown) {
        final int cooldownValue = yaml.getInt(cooldown);
        if( cooldownValue > 0 )
            return cooldownValue;
        else
            return 0;
    }
    
    /**
     * Determine if cooldown per-world is enabled for a given world.
     * @param world
     * @return
     */
    public boolean isCooldownPerWorld(String world) {
        return yaml.getBoolean("world."+world+"."+"cooldownPerWorld");
    }
    
    /*
    public boolean isCooldownPerPermission(String[] cooldowns, String perm) {
        Set<String> entries = yaml.getKeys("permission");
        for(String entry : entries) {
            for(String cooldown : cooldowns) {
                // skip sections that don't apply to this cooldown
                final int cooldownValue = yaml.getInt("permission."+entry+"."+cooldown);
                if( cooldownValue <= 0 )
                    continue;
    
                List<String> perms = yaml.getStringList("permission."+entry+".permissions");
                if( perms == null ) {
                    perms = new ArrayList<String>(1);
                }
                perms.add("hsp.cooldown."+entry);     // add default entry permission
                
                if( perms.contains(permission) ) {
                    return cooldownValue;
                }
            }
        }
        
        return 0;
    }
    */
    
    public Map<String, PerPermissionCooldownEntry> getPerPermissionEntries() {
        return perPermissionEntries;
    }
    
    /**
     * Method to process the config and load into memory any per-permissions
     * config data for simple and efficient use later.
     */
    private void populatePerPermissionEntries() {
        final String base = "permission";

        perPermissionEntries = new LinkedHashMap<String, PerPermissionCooldownEntry>();
        
        Set<String> entries = yaml.getKeys(base);
        for(String entryName : entries) {
            final String baseEntry = base + "." + entryName;
            
            PerPermissionCooldownEntry entry = new PerPermissionCooldownEntry();
            perPermissionEntries.put(entryName, entry);
            
            // grab permissions related to this entry
            entry.permissions = yaml.getStringList(baseEntry+".permissions");
            if( entry.permissions == null ) {
                entry.permissions = new ArrayList<String>(1);
            }
            entry.permissions.add("hsp.cooldown."+entry);     // add default entry permission

            // make map immutable to protect against external changes
            entry.permissions = Collections.unmodifiableList(entry.permissions);

            // now look for individual cooldown settings
            for(String key : yaml.getKeys(baseEntry)) {
                if( key.equals("permissions") ) {   // skip, we already got these
                    continue;
                }
                if( key.equals("cooldownPerPermission") ) {
                    entry.cooldownPerPermission = yaml.getBoolean(baseEntry+".cooldownPerPermission");
                    continue;
                }
                if( key.equals(RESET_ON_DEATH) ) {
                    entry.resetOnDeath = yaml.getBoolean(baseEntry+"."+RESET_ON_DEATH);
                    continue;
                }
                
                // everything else is a cooldown entry
                entry.cooldowns.put(key, yaml.getInt(baseEntry+"."+key));
            }
            
            // make map immutable to protect against external changes
            entry.cooldowns = Collections.unmodifiableMap(entry.cooldowns);
        }
        
        // make map immutable to protect against external changes
        perPermissionEntries = Collections.unmodifiableMap(perPermissionEntries);
    }
        
    public class PerPermissionCooldownEntry {
        protected List<String> permissions;
        protected boolean cooldownPerPermission = false;
        protected boolean resetOnDeath = false;
        
        protected Map<String, Integer> cooldowns = new HashMap<String, Integer>();
        
        public List<String> getPermissions() { return permissions; }
        public boolean isCooldownPerPermission() { return cooldownPerPermission; }
        public boolean isResetOnDeath() { return resetOnDeath; }
        public Map<String, Integer> getCooldowns() { return cooldowns; }
    }
}
