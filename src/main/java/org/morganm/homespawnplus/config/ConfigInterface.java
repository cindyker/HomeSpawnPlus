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
}
