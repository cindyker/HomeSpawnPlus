/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.config.ConfigCooldown.CooldownsPerPermission;
import org.morganm.homespawnplus.config.ConfigCooldown.CooldownsPerWorld;
import org.morganm.homespawnplus.server.api.YamlFile;

/**
 * @author morganm
 *
 */
@Singleton
public class ConfigCooldown extends ConfigPerXBase<CooldownsPerPermission, CooldownsPerWorld>
implements ConfigInterface, Initializable
{
    @Inject
    public ConfigCooldown(YamlFile yaml) {
        super("cooldown.yml", "cooldown", yaml);
    }

    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }
    
    @Override
    protected CooldownsPerPermission newPermissionEntry() {
        return new CooldownsPerPermission();
    }

    @Override
    protected CooldownsPerWorld newWorldEntry() {
        return new CooldownsPerWorld();
    }

    /**
     * Return a list of cooldowns that are on separate timers.
     * 
     * @return
     */
    public List<String> getSeparateCooldowns() {
        return super.getStringList("separation");
    }
    
    /**
     * Determine if global cooldowns should reset on player death.
     * 
     * @return
     */
    public boolean isGlobalResetOnDeath() {
        return super.getBoolean("resetOnDeath");
    }

    /**
     * For a given cooldown, return it's global cooldown time (exclusive of
     * per-permission and per-world cooldown values).
     * 
     * @param cooldown
     * @return
     */
    public int getGlobalCooldown(String cooldown) {
        final int cooldownValue = super.getInt(cooldown);
        return cooldownValue > 0 ? cooldownValue : 0;
    }

    /**
     * Determine if cooldown per-world is enabled for a given world.
     * @param world
     * @return
     */
    public boolean isCooldownPerWorld(String world) {
        CooldownsPerWorld entry = super.perWorldEntries.get(world);
        return entry != null ? entry.isCooldownPerWorld() : false;
    }

    /**
     * For a given world & cooldown combo, return the applicable timer.
     * 
     * @param cooldown
     * @param world
     * @return
     */
    public int getPerWorldCooldown(String cooldown, String world) {
        CooldownsPerWorld entry = super.perWorldEntries.get(world);
        return entry != null ? entry.getCooldowns().get(cooldown) : 0;
    }

    private static class Entry {
        boolean hasCooldownPerX = false;
        boolean isCooldownPerX = false;
        boolean hasResetOnDeath = false;
        boolean isResetOnDeath = false;
        Map<String, Integer> cooldowns = new HashMap<String, Integer>();
        
        public void setValue(String key, Object o) {
            if( key.startsWith("cooldownPer") ) {   // cooldownPerPermission and cooldownPerWorld
                hasCooldownPerX = true;
                isCooldownPerX = (Boolean) o;
            }
            else if( key.equals("resetOnDeath") ) {
                hasResetOnDeath = true;
                isResetOnDeath = (Boolean) o;
            }
            else {
                cooldowns.put(key, (Integer) o);
            }
        }
        
        public void finishedProcessing() {
            cooldowns = Collections.unmodifiableMap(cooldowns);
        }
    }

    public static class CooldownsPerPermission extends PerPermissionEntry {
        private Entry entry;
        
        public boolean hasCooldownPerPermission() { return entry.hasCooldownPerX; }
        public boolean isCooldownPerPermission() { return entry.isCooldownPerX; }
        public boolean hasResetOnDeath() { return entry.hasResetOnDeath; }
        public boolean isResetOnDeath() { return entry.isResetOnDeath; }
        public Map<String, Integer> getCooldowns() { return entry.cooldowns; }
        
        @Override
        public void setValue(String key, Object o) {
            entry.setValue(key,  o);
        }
        
        @Override
        public void finishedProcessing() {
            entry.finishedProcessing();
        }
    }

    public static class CooldownsPerWorld extends PerWorldEntry {
        private Entry entry;
        
        public boolean hasCooldownPerWorld() { return entry.hasCooldownPerX; }
        public boolean isCooldownPerWorld() { return entry.isCooldownPerX; }
        public boolean hasResetOnDeath() { return entry.hasResetOnDeath; }
        public boolean isResetOnDeath() { return entry.isResetOnDeath; }
        public Map<String, Integer> getCooldowns() { return entry.cooldowns; }
        
        @Override
        public void setValue(String key, Object o) {
            entry.setValue(key,  o);
        }
        
        @Override
        public void finishedProcessing() {
            entry.finishedProcessing();
        }
    }
}
