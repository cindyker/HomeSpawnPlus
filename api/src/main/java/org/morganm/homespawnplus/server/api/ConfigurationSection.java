/**
 * 
 */
package org.morganm.homespawnplus.server.api;

import java.util.List;
import java.util.Set;

/** Modeled after the Bukkit object of the same name.
 * 
 * @author morganm
 *
 */
public interface ConfigurationSection {
    /**
     * Gets a set containing all keys in this section.
     * 
     * @return Set of keys contained within this ConfigurationSection.
     */
    public Set<String> getKeys();

    /**
     * Gets a set containing all keys in the specified path.
     * 
     * @return Set of keys contained within the specified path
     */
    public Set<String> getKeys(String path);

    /**
     * Gets the requested ConfigurationSection by path.
     *
     * @param path Path of the ConfigurationSection to get.
     * @return Requested ConfigurationSection.
     */
    public ConfigurationSection getConfigurationSection(String path);

    public boolean contains(String path);
    public Object get(String path);
    public boolean getBoolean(String path);
    public int getInt(String path);
    public Integer getInteger(String path);
    public double getDouble(String path);
    public String getString(String path);
    public List<String> getStringList(String path);
}
