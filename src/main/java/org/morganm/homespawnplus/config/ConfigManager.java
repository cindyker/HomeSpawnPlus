/**
 * 
 */
package org.morganm.homespawnplus.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morganm
 *
 */
@Singleton
public class ConfigManager {
    private final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    
    private final ConfigCore core;
    private final ConfigStorage storage;
    private final ConfigEconomy economy;
    
    @Inject
    public ConfigManager(ConfigCore core, ConfigStorage storage,
            ConfigEconomy economy)
    {
        this.core = core;
        this.storage = storage;
        this.economy = economy;
    }
    
    /** Can be used to load or reload all configs.
     * 
     */
    public void loadAll() {
        try {
            core.load();
            storage.load();
            economy.load();
        }
        catch(Exception e) {
            log.error("Error in config loadAll()", e);
        }
    }
}
