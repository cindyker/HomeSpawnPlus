/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author morganm
 *
 */
public interface ConfigInterface {
    /**
     * Load (or reload) the configuration from the backing store.
     * 
     * @throws Exception
     */
    public void load() throws IOException, FileNotFoundException, ConfigException;
    
    /**
     * Return the relative config filename of this config object. For example
     * "core.yml" or "events.yml". The implementation will determine the exact
     * location of these files based on the configuration.
     * 
     * @return
     */
    public String getConfigFileName();
    
    /**
     * Return the base path for the configuration. For example this could be
     * "core" or "events". If null, it means the config is expected at
     * the root node.
     * 
     * @return
     */
    public String getBasePath();
}
