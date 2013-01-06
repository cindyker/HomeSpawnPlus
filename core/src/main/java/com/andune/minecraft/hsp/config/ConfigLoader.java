/**
 * 
 */
package com.andune.minecraft.hsp.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.server.api.ConfigurationSection;
import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.server.api.YamlFile;
Jimport com.andune.minecraft.commonlib.JarUtils;
arUtils;

/**
 * @author morganm
 *
 */
public class ConfigLoader  {
    private final Plugin plugin;
    private final Factory factory;
    private final JarUtils jarUtil;
    
    private YamlFile singleConfigFile;

    @Inject
    public ConfigLoader(Plugin plugin, Factory factory, JarUtils jarUtil) {
        this.plugin = plugin;
        this.factory = factory;
        this.jarUtil = jarUtil;
    }

    /**
     * Load the given configFile and return the configurationSection
     * that represents the config data. This will either load the config
     * from one large file ("config.yml") if it exists or from individual
     * config files.
     * 
     * @return
     * @throws ConfigException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public ConfigurationSection load(String fileName, String basePath) throws IOException, ConfigException {
        YamlFile yaml = getSingleConfigFile();

        // load individual config file if single "config.yml" is not in use
        if( yaml == null ) {
            File configFileName = new File(plugin.getDataFolder(), "config/"+fileName);
            if( !configFileName.exists() )
                installDefaultFile(fileName);
            yaml = factory.newYamlFile();
            yaml.load(configFileName);
        }

        return yaml.getConfigurationSection(basePath);
    }

    /**
     * If a single config file exists, this method will load it and
     * return it, caching it for future calls. If it doesn't exist,
     * this method returns null.
     * 
     * @return
     * @throws ConfigException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    private YamlFile getSingleConfigFile() throws FileNotFoundException, IOException, ConfigException {
        if( singleConfigFile == null ) {
            File configYml = new File(plugin.getDataFolder(), "config.yml");
            if( configYml.exists() ) {
                singleConfigFile = factory.newYamlFile();
                singleConfigFile.load(configYml);
            }
        }
        
        return singleConfigFile;
    }

    /**
     * Install the configuration default file if it doesn't exist.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void installDefaultFile(String fileName) throws FileNotFoundException, IOException {
        File pluginDir = plugin.getDataFolder();
        // create the config directory if it doesn't exist
        File configDir = new File(pluginDir, "config");
        if( !configDir.exists() )
            configDir.mkdirs();

        File configFile = new File(configDir, fileName);
        if( !configFile.exists() )
            jarUtil.copyConfigFromJar("config/"+fileName, configFile);
    }

}
