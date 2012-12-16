/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.YamlFile;

/**
 * @author morganm
 *
 */
@Singleton
public class ConfigHomeInvites implements ConfigInterface {
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigHomeInvites(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("homeInvites.yml");
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
     * Return the time (in seconds) for a sent invite to expire.
     * 
     * @return
     */
    public int getTimeout() {
        return yaml.getInt("timeout");
    }
    
    /**
     * Determine if invites are enabled for bedHomes.
     * 
     * @return
     */
    public boolean allowBedHomeInvites() {
        return yaml.getBoolean("allowBedHomeInvites");
    }
    
    /**
     * Determine if home invites should use shared home warmups instead of
     * their own warmups.
     * 
     * @return
     */
    public boolean useHomeWarmup() {
        return yaml.getBoolean("useHomeWarmup");
    }
    
    /**
     * Determine if home invites should use shared home cooldowns instead
     * of their own cooldowns.
     * 
     * @return
     */
    public boolean useHomeCooldown() {
        return yaml.getBoolean("useHomeCooldown");
    }
}
