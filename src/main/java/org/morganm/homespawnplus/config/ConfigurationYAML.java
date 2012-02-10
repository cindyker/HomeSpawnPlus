/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.SpawnStrategy;


/**
 * @author morganm
 *
 */
public class ConfigurationYAML extends YamlConfiguration implements Config {
	private static final Logger log = HomeSpawnPlus.log;
	private final String logPrefix; 
	
	private File file;
	private HomeSpawnPlus plugin;
	private boolean useExtendedFileAsDefault = false;
	
	public ConfigurationYAML(File file, HomeSpawnPlus plugin) {
		this.file = file;
		this.plugin = plugin;
		
		this.logPrefix = HomeSpawnPlus.logPrefix;
	}
	
	public void setExtendedFile(boolean state) { useExtendedFileAsDefault = state; }

	@Override
    public void load() throws ConfigException {
		// if no config exists, copy the default one out of the JAR file
		if( !file.exists() && !useExtendedFileAsDefault )
			copyConfigFromJar("config-basic.yml", file);
		if( !file.exists() )
			copyConfigFromJar("config.yml", file);
		
		try {
			super.load(file);
		}
		catch(Exception e) {
			throw new ConfigException(e);
		}
		
		if( getString("events.onGroupSpawnCommand") == null ) {
			log.info(logPrefix + " WARNING: old-style config found, you should look at config_defaults.yml and copy the latest config settings into your config.yml.");
		}

		loadDefaults();
			
		/*
		if( getString("core.eventPriority") == null ) {
			log.info(logPrefix + " old-style config found, moving and replacing with new default");
			file.renameTo(new File(file.toString() + ".old"));
			copyConfigFromJar("config.yml", file);
			
			try {
				super.load(file);
			}
			catch(Exception e) {
				throw new ConfigException(e);
			}
		}
		*/
		
//		loadConfiguration(file);
//		super.load();
    }

	private void loadDefaults() {
	    // Look for defaults in the jar
	    InputStream defConfigStream = plugin.getResource("config.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	 
	        setDefaults(defConfig);
	    }
	}
	
	/** Right now we don't allow updates in-game, so we don't do anything, because if we
	 * let it save, all the comments are lost.  In the future, I may allow in-game updates
	 * to the config file and this will just call super.save();
	 */
	@Override
	public void save() throws ConfigException {
		try {
			super.save(file);
		}
		catch(Exception e) {
			throw new ConfigException(e);
		}
	}
	
	/** Code adapted from Puckerpluck's MultiInv plugin.
	 * 
	 * @param string
	 * @return
	 */
    private void copyConfigFromJar(String fileName, File outfile) {
    	plugin.getJarUtils().copyConfigFromJar(fileName, outfile);
    }
    
    public List<String> getStringList(String path, List<String> def)
    {
    	List<String> list = null; 
    	if( isList(path) )
    	  list = super.getStringList(path);
    	
    	// no config value? set it to passed in default
    	if( list == null )
    		list = def;
    	
    	// still null? set it to an empty list
    	if( list == null )
    		list = new ArrayList<String>();
    	
    	return list;
    }
    
    /** Return the list of permissions that have strategies associated with them.
     * 
     * @return
     */
    @Override
    public Set<String> getPermStrategies() {
    	Set<String> permList;
    	
    	ConfigurationSection section = getConfigurationSection(ConfigOptions.SETTING_EVENTS_BASE
				+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE);
    	
    	if( section != null )
    		permList = section.getKeys(false);
    	else
    		permList = new HashSet<String>();
    	
    	return permList;
    }

    /** TODO: add caching so we aren't string->enum converting on every join/death
     * 
     */
	@Override
	public List<SpawnStrategy> getStrategies(String node) {
		List<SpawnStrategy> spawnStrategies = new ArrayList<SpawnStrategy>();
    	List<String> strategies = getStringList(node, null);

    	for(String s : strategies) {
    		try {
    			spawnStrategies.add(SpawnStrategy.mapStringToStrategy(s));
    		}
    		catch(ConfigException e) {
    			log.warning(e.getMessage());
    			e.printStackTrace();
    		}
    	}
    	
		return spawnStrategies;
	}
}
