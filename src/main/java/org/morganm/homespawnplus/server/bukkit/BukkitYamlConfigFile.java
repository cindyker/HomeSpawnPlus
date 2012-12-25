/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.server.api.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morganm
 *
 */
public class BukkitYamlConfigFile implements YamlFile {
    private static final Logger log = LoggerFactory.getLogger(BukkitYamlConfigFile.class);

    private final YamlConfiguration yaml;
    private final Plugin plugin;
    
    @Inject
    public BukkitYamlConfigFile(YamlConfiguration yaml, Plugin plugin) {
        this.yaml = yaml;
        this.plugin = plugin;
    }
    
    /**
     * Given a filename, return it's full config file path.
     * @param file
     * @return
     */
//    private File configFile(File file) {
//        File dataDir = plugin.getDataFolder();
//        File configDir = new File(dataDir, "config");
//        return new File(configDir, file.getName());
//    }

    @Override
    public void save(File file) throws IOException {
        yaml.save(file);
    }

    @Override
    public void load(File file) throws FileNotFoundException, IOException, ConfigException {
        log.debug("loading yaml file {}, name = {}", file, file.getName());
        try {
            yaml.load(file);
        }
        catch(InvalidConfigurationException e) {
            throw new ConfigException(e);
        }
        
        // load defaults if possible, if not, ignore any errors
        String fileName = "config/" + file.getName();

        // try file path with prefix, ie. "config/core.yml"
        log.debug("loading defaults for file {}", fileName);
        InputStream defConfigStream = plugin.getResource(fileName);
        if( defConfigStream != null ) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(defConfigStream);
            yaml.addDefaults(defaults);
            log.debug("defaults loaded for file {}", file);
        }
        // if that doesn't work, try file path alone, ie. "core.yml"
        else {
            fileName = file.getName();

            // try last two part of file path, ie. "core.yml"
            log.debug("loading defaults for file {}", fileName);
            defConfigStream = plugin.getResource(fileName);
            if( defConfigStream != null ) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(defConfigStream);
                yaml.addDefaults(defaults);
                log.debug("defaults loaded for file {}", file);
            }
        }
    }

    @Override
    public org.morganm.homespawnplus.server.api.ConfigurationSection getConfigurationSection(String path) {
        ConfigurationSection section = yaml.getConfigurationSection(path);
        log.debug("getConfigurationSection() path={}, section={}", path, section);
        if( section != null ) {
            return new BukkitConfigurationSection(section);
        }
        // try defaults
        else {
            ConfigurationSection rootSection = plugin.getConfig().getDefaults();
//            ConfigurationSection rootSection = yaml.getDefaultSection();
            if( rootSection != null )
                section = rootSection.getConfigurationSection(path);
            log.debug("getConfigurationSection() tried defaults, path={}, section={}", path, section);
            if( section != null )
                return new BukkitConfigurationSection(section);
            else
                return null;
        }
    }

    @Override
    public org.morganm.homespawnplus.server.api.ConfigurationSection getRootConfigurationSection() {
        ConfigurationSection section = yaml.getRoot();
        log.debug("getRootConfigurationSection() section={}", section);
        if( section != null ) {
            return new BukkitConfigurationSection(section);
        }
        // try defaults
        else {
            section = plugin.getConfig().getDefaults();
            log.debug("getConfigurationSection() tried defaults, section={}", section);
            if( section != null )
                return new BukkitConfigurationSection(section);
            else
                return null;
        }
    }
}
