/**
 * 
 */
package org.morganm.homespawnplus.config;

import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.server.api.ConfigurationSection;

/** Config file for event-related configuration.
 * 
 * The pattern used by this config object is different from the others because the
 * StrategyConfig object already has all the necessary logic to process the event
 * configs and it is sufficiently different from the other config patterns to
 * remain that way.
 * 
 * At some point it might make sense to merge the majority of code from StrategyConfig
 * class into this config object, but there wouldn't be a huge benefit except for
 * alignment with purpose in this package. For now it's better to leave the complex and
 * well-tested StrategyConfig class alone.
 * 
 * @author morganm
 *
 */
@Singleton
@ConfigOptions(fileName="events.yml", basePath="events")
public class ConfigEvents extends ConfigBase implements Initializable {
    public static final String SETTING_EVENTS_WORLDBASE = "world";
    public static final String SETTING_EVENTS_PERMBASE = "permission";

    /**
     * Return the base configuration section.
     * 
     * @return
     */
    public ConfigurationSection getBaseSection() {
        return configSection;
    }

    /**
     * Return a ConfigurationSection relative to the base section.
     * 
     * @param sectionName
     * @return
     */
    public ConfigurationSection getSection(String sectionName) {
        return configSection.getConfigurationSection(sectionName);
    }

    public ConfigurationSection getWorldSection() {
        return configSection.getConfigurationSection(SETTING_EVENTS_WORLDBASE);
    }

    public ConfigurationSection getPermissionSection() {
        return configSection.getConfigurationSection(SETTING_EVENTS_PERMBASE);
    }
}
