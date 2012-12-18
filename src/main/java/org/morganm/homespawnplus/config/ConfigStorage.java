/**
 * 
 */
package org.morganm.homespawnplus.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.server.api.YamlFile;

/**
 * @author morganm
 *
 */
@Singleton
public class ConfigStorage extends AbstractConfigBase implements ConfigInterface, Initializable {
    public enum Type {
        EBEANS,
        NOTUSED,
        YAML,
        YAML_SINGLE_FILE,
        PERSISTANCE_REIMPLEMENTED_EBEANS;
    };
    
    @Inject
    public ConfigStorage(YamlFile yaml) {
        super("storage.yml", "storage", yaml);
    }
    
    public Type getStorageType() {
        // TODO: map String to Type
        return null; 
    }
}
