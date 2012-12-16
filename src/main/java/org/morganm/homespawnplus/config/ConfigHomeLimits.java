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
public class ConfigHomeLimits implements ConfigInterface {
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigHomeLimits(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("homeLimits.yml");
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
     * Determine if single global home setting is enabled.
     * 
     * @return
     */
    public boolean isSingleGlobalHome() {
        return yaml.getBoolean("singleGlobalHome");
    }
}
