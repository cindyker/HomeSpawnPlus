/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.config.ConfigWarmup.WarmupsPerPermission;
import org.morganm.homespawnplus.config.ConfigWarmup.WarmupsPerWorld;

/**
 * @author morganm
 *
 */
@Singleton
@ConfigOptions(fileName="warmup.yml", basePath="warmup")
public class ConfigWarmup extends ConfigPerXBase<WarmupsPerPermission, WarmupsPerWorld> implements Initializable {
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
     * For a given world & warmup combo, return the applicable warmup timer.
     * 
     * @param warmup
     * @param world
     * @return
     */
    public int getPerWorldWarmup(String warmup, String world) {
        WarmupsPerWorld warmups = perWorldEntries.get(world);
        return warmups != null ? warmups.getWarmups().get(warmup) : 0;
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
        return warmupValue > 0 ? warmupValue : 0;
    }

    public class WarmupsPerPermission extends PerPermissionEntry {
        Map<String, Integer> warmups = new HashMap<String, Integer>();
        public Map<String, Integer> getWarmups() { return warmups; }
        
        public void setValue(String key, Object o) {
            warmups.put(key, (Integer) o);
        }
        
        public void finishedProcessing() {
            warmups = Collections.unmodifiableMap(warmups);
        }
    }

    public class WarmupsPerWorld extends PerWorldEntry {
        Map<String, Integer> warmups = new HashMap<String, Integer>();
        public Map<String, Integer> getWarmups() { return warmups; }
        
        public void setValue(String key, Object o) {
            warmups.put(key, (Integer) o);
        }
        
        public void finishedProcessing() {
            warmups = Collections.unmodifiableMap(warmups);
        }
    }

    @Override
    protected WarmupsPerPermission newPermissionEntry() { return new WarmupsPerPermission(); }

    @Override
    protected WarmupsPerWorld newWorldEntry() { return new WarmupsPerWorld(); }
}
