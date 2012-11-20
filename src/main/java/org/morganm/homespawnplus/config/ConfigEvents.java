/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.YamlFile;

/** Config file for event-related configuration.
 * 
 * @author morganm
 *
 */
public class ConfigEvents implements ConfigInterface {
    private final YamlFile yaml;
    private final File file;
    
    @Inject
    public ConfigEvents(YamlFile yaml) {
        this.yaml = yaml;
        this.file = new File("events.yml");
    }

    @Override
    public void load() throws IOException, FileNotFoundException, ConfigException {
        yaml.load(file);
    }
}
