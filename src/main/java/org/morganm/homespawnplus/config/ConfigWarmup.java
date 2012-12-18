/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.server.api.YamlFile;

/**
 * @author morganm
 *
 */
@Singleton
public class ConfigWarmup extends AbstractConfigBase implements ConfigInterface, Initializable {
    @Inject
    public ConfigWarmup(YamlFile yaml) {
        super("warmup.yml", "warmup", yaml);
    }

    /**
     * Determine if warmups are enabled.
     * 
     * @return true if warmups are enabled.
     */
    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }
    
    /**
     * Determine if warmups should be canceled when a player is damaged.
     * 
     * @return true if warmups should be canceled on damage
     */
    public boolean isCanceledOnDamage() {
        return super.getBoolean("onDamageCancel");
    }
    
    /**
     * Determine if warmups should be canceled when a player moves.
     * 
     * @return true if warmups should be canceled on movement
     */
    public boolean isCanceledOnMovement() {
        return super.getBoolean("onMoveCancel");
    }
    
    /**
     * Return all per-permission permissions related to a given warmup.
     * 
     * @param warmup
     * @return
     */
    public Set<String> getPerPermissionWarmups(String warmup) {
        Set<String> returnPerms = new LinkedHashSet<String>();
        
        Set<String> entries = super.getKeys("permission");
        for(String entry : entries) {
            // skip warmup entries that don't apply to this warmup
            final int warmupValue = super.getInt("permission."+entry+"."+warmup);
            if( warmupValue <= 0 )
                continue;
            
            List<String> perms = super.getStringList("permission."+entry+".permissions");
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
        Set<String> entries = super.getKeys("permission");
        for(String entry : entries) {
            // skip warmup entries that don't apply to this warmup
            final int warmupValue = super.getInt("permission."+entry+"."+warmup);
            if( warmupValue <= 0 )
                continue;

            List<String> perms = super.getStringList("permission."+entry+".permissions");
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
        final int warmupValue = super.getInt("world."+world+"."+warmup);
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
        final int warmupValue = super.getInt(warmup);
        if( warmupValue > 0 )
            return warmupValue;
        else
            return 0;
    }
}
