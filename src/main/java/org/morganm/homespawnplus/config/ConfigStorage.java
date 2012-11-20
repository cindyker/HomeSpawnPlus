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
public class ConfigStorage implements ConfigInterface {
    public enum Type {
        EBEANS,
        NOTUSED,
        YAML,
        YAML_SINGLE_FILE,
        PERSISTANCE_REIMPLEMENTED_EBEANS;
    };
    
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigStorage(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("storage.yml");
    }
    
    public Type getStorageType() {
        // TODO: map String to Type
        return null; 
    }

    @Override
    public void load() throws IOException, FileNotFoundException, ConfigException {
        yaml.load(file);
    }
}
