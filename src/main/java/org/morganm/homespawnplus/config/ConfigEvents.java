/**
 * 
 */
package org.morganm.homespawnplus.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.server.api.YamlFile;

/** Config file for event-related configuration.
 * 
 * @author morganm
 *
 */
@Singleton
public class ConfigEvents extends AbstractConfigBase implements ConfigInterface, Initializable {
    @Inject
    public ConfigEvents(YamlFile yaml) {
        super("events.yml", "events", yaml);
    }
}
