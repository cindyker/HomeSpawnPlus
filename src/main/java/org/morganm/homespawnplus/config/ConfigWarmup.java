/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.YamlFile;

/**
 * @author morganm
 *
 */
public class ConfigWarmup implements ConfigInterface {
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigWarmup(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("warmup.yml");
    }

    @Override
    public void load() throws IOException, FileNotFoundException, ConfigException {
        yaml.load(file);
    }

    /**
     * Determine if warmups are enabled.
     * 
     * @return true if warmups are enabled.
     */
    public boolean isEnabled() {
        return yaml.getBoolean("enabled");
    }
    
    /**
     * Determine if warmups should be canceled when a player is damaged.
     * 
     * @return true if warmups should be canceled on damage
     */
    public boolean isCanceledOnDamage() {
        return yaml.getBoolean("onDamageCancel");
    }
    
    /**
     * Determine if warmups should be canceled when a player moves.
     * 
     * @return true if warmups should be canceled on movement
     */
    public boolean isCanceledOnMovement() {
        return yaml.getBoolean("onMoveCancel");
    }
    
    /**
     * Return all per-permission permissions related to a given warmup.
     * 
     * @param warmup
     * @return
     */
    public Set<String> getPerPermissionWarmups(String warmup) {
        Set<String> returnPerms = new LinkedHashSet<String>();
        
        Set<String> entries = yaml.getKeys("permission");
        for(String entry : entries) {
            // skip warmup entries that don't apply to this warmup
            final int warmupValue = yaml.getInt("permission."+entry+"."+warmup);
            if( warmupValue <= 0 )
                continue;
            
            List<String> perms = yaml.getStringList("permission."+entry+".permissions");
            if( perms == null ) {
                perms = new ArrayList<String>(1);
            }
            perms.add("hsp.warmup."+entry);     // add default entry permission
            
            returnPerms.addAll(perms);
        }
        
        return returnPerms;
    }
    
    /**
     * For a given permission & warmup combo, return the applicable warmup timer.
     * 
     * @param warmup
     * @param permission
     * @return
     */
    public int getPerPermissionWarmup(String warmup, String permission) {
        Set<String> entries = yaml.getKeys("permission");
        for(String entry : entries) {
            // skip warmup entries that don't apply to this warmup
            final int warmupValue = yaml.getInt("permission."+entry+"."+warmup);
            if( warmupValue <= 0 )
                continue;

            List<String> perms = yaml.getStringList("permission."+entry+".permissions");
            if( perms == null ) {
                perms = new ArrayList<String>(1);
            }
            perms.add("hsp.warmup."+entry);     // add default entry permission
            
            if( perms.contains(permission) ) {
                return warmupValue;
            }
        }
        
        return 0;
    }

    /**
     * For a given world & warmup combo, return the applicable warmup timer.
     * 
     * @param warmup
     * @param world
     * @return
     */
    public int getPerWorldWarmup(String warmup, String world) {
        final int warmupValue = yaml.getInt("world."+world+"."+warmup);
        if( warmupValue > 0 )
            return warmupValue;
        else
            return 0;
    }
    
    /**
     * For a given warmup, return it's global warmup time (exclusive of
     * per-permission and per-world warmup values).
     * 
     * @param warmup
     * @return
     */
    public int getGlobalWarmup(String warmup) {
        final int warmupValue = yaml.getInt(warmup);
        if( warmupValue > 0 )
            return warmupValue;
        else
            return 0;
    }
}
