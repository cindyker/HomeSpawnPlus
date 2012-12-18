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
public class ConfigHomeLimits extends AbstractConfigBase implements ConfigInterface, Initializable {
    @Inject
    public ConfigHomeLimits(YamlFile yaml) {
        super("homeLimits.yml", "homeLimits", yaml);
    }

    /**
     * Determine if single global home setting is enabled.
     * 
     * @return
     */
    public boolean isSingleGlobalHome() {
        return super.getBoolean("singleGlobalHome");
    }
}
