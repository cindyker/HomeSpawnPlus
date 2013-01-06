/**
 * 
 */
package org.morganm.homespawnplus.server.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.morganm.homespawnplus.config.ConfigException;

/** Interface to YAML configuration. HSP config files are written in
 * YAML, however through this interface they are not bound to any
 * specific implementation. So an implementation could be written to
 * bind to Bukkit's YAML, or SnakeYAML, or some other YAML API.
 * 
 * @author morganm
 *
 */
public interface YamlFile {
    public void save(File file) throws IOException;
    public void load(File file) throws FileNotFoundException, IOException, ConfigException;
    
    /**
     * Gets the requested ConfigurationSection by path.
     *
     * @param path Path of the ConfigurationSection to get.
     * @return Requested ConfigurationSection.
     */
    public ConfigurationSection getConfigurationSection(String path);
    
    /**
     * Returns the root configuration section for this file.
     * 
     * @return root ConfigurationSection.
     */
    public ConfigurationSection getRootConfigurationSection();
}
