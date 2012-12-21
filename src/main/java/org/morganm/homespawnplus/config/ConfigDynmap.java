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
public class ConfigDynmap extends ConfigBase implements ConfigInterface, Initializable {
    @Inject
    public ConfigDynmap(YamlFile yaml) {
        super("dynmap.yml", "dynmap", yaml);
    }

    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }
}
