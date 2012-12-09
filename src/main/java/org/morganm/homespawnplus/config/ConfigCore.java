/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.YamlFile;


/**
 * @author morganm
 *
 */
public class ConfigCore implements ConfigInterface {
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigCore(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("core.yml");
    }

    /**
     * Load (or reload) the configuration from the backing store.
     * 
     * @throws Exception
     */
    public void load() throws IOException, FileNotFoundException, ConfigException {
        yaml.load(file);
    }
    
    /**
     * Return the configured locale, such as "en", "de", "fr", etc.
     * 
     * @return
     */
    public String getLocale() {
        return yaml.getString("locale");
    }
    
    public boolean isDebug() {
        return yaml.getBoolean("debug");
    }
    
    /**
     * Is verbose logging enabled?
     * 
     * @return true if verbose logging is enabled
     */
    public boolean isVerboseLogging() {
        return yaml.getBoolean("verboseLogging");
    }
    
    /**
     *  Is verbose strategy logging enabled?
     *  
     * @return
     */
    public boolean isVerboseStrategyLogging() {
        return yaml.getBoolean("verboseStrategyLogging");
    }
    
    /**
     * Is safe teleport mode enabled?
     * 
     * @return true if safe teleport is enabled
     */
    public boolean isSafeTeleport() {
        return yaml.getBoolean("safeTeleport");
    }
    
    /**
     * Millisecond value for controlling performance-related warnings.
     * 
     * @return
     */
    public int getPerformanceWarnMillis() {
        return yaml.getInt("warnPerformanceMillis");
    }
    
    /**
     * Determine if the last home on a given world is always considered
     * the default.
     * 
     * @return true if the last home is the default
     */
    public boolean isLastHomeDefault() {
        return yaml.getBoolean("lastHomeIsDefault");
    }
    
    /**
     * Determine if teleport messages should be sent on home/spawn
     * commands to tell the player they have arrived.
     * 
     * @return
     */
    public boolean isTeleportMessages() {
        return yaml.getBoolean("teleportMessages");
    }
    
    /**
     * Determine if named permissions should be used for spawns, such that
     * if a player types "/spawn spawn1", a specific permission for spawn1
     * will be checked, such as "hsp.command.spawn.named.spawn1"
     * 
     * @return
     */
    public boolean isSpawnNamedPermissions() {
        return yaml.getBoolean("spawnNamedPermissions");
    }
    
    /**
     * Determine if setting default world spawn should also set the
     * spawn for that world on the backing server (ie. in the MC maps
     * itself).
     * 
     * @return
     */
    public boolean isOverrideWorld() {
        return yaml.getBoolean("override_world");
    }
    
    /**
     * Determine if sleeping in a bed should overwrite the defaultHome if a
     * bed home isn't found. In practice, when set, this means the bedHome
     * and the defaultHome will be the same.
     * 
     * @return
     */
    public boolean isBedHomeOverwriteDefault() {
        return yaml.getBoolean("bedHomeOverwritesDefault");
    }
}
