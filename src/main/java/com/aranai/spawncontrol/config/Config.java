/**
 * 
 */
package com.aranai.spawncontrol.config;

/** Our configuration which determines how a number of things about how the mod operates.
 * Intended to be used with ConfigOptions class for setting names, which keeps the interface
 * fairly generic and flexible and still allows for different implementations (ie. YAML, SQL,
 * properties, etc).
 * 
 * @author morganm
 *
 */
public interface Config {
	public void load() throws ConfigException;
	
    /**
     * Gets a boolean for a given node. This will either return an boolean
     * or the default value. If the object at the particular location is not
     * actually a boolean, the default value will be returned.
     *
     * @param path path to node (dot notation)
     * @param def  default value
     * @return boolean or default
     */
    public boolean getBoolean(String node, boolean def);
    
    /**
     * Gets a string at a location. This will either return an String
     * or null, with null meaning that no configuration value exists at
     * that location. If the object at the particular location is not actually
     * a string, it will be converted to its string representation.
     *
     * @param path path to node (dot notation)
     * @return string or null
     */
    public String getString(String path);

    /**
     * Gets a string at a location. This will either return an String
     * or the default value. If the object at the particular location is not
     * actually a string, it will be converted to its string representation.
     *
     * @param path path to node (dot notation)
     * @param def  default value
     * @return string or default
     */
    public String getString(String path, String def);
    
    /**
     * Gets an integer at a location. This will either return an integer
     * or the default value. If the object at the particular location is not
     * actually a integer, the default value will be returned. However, other
     * number types will be casted to an integer.
     *
     * @param path path to node (dot notation)
     * @param def  default value
     * @return int or default
     */
    public int getInt(String path, int def);
}
