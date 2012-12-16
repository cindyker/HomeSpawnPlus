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
public class ConfigDynmap implements ConfigInterface {
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigDynmap(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("dynmap.yml");
    }

    @Override
    public void load() throws IOException, FileNotFoundException, ConfigException {
        yaml.load(file);
    }

    public boolean isEnabled() {
        return yaml.getBoolean("enabled");
    }
}
