/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.server.api.YamlFile;

/**
 * @author morganm
 *
 */
public class BukkitYamlConfigFile implements YamlFile {
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
    private File configFile(File file) {
        File dataDir = plugin.getDataFolder();
        File configDir = new File(dataDir, "config");
        return new File(configDir, file.getName());
    }

    @Override
    public void save(File file) throws IOException {
        yaml.save(configFile(file));
    }

    @Override
    public void load(File file) throws FileNotFoundException, IOException, ConfigException {
        try {
            yaml.load(configFile(file));
        }
        catch(InvalidConfigurationException e) {
            throw new ConfigException(e);
        }
    }
    
    @Override
    public boolean contains(String path) {
        return yaml.contains(path);
    }

    @Override
    public Object get(String path) {
        return yaml.get(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return yaml.getBoolean(path);
    }

    @Override
    public int getInt(String path) {
        return yaml.getInt(path);
    }

    @Override
    public String getString(String path) {
        return yaml.getString(path);
    }

    private static final Set<String> emptySet = Collections.unmodifiableSet(new HashSet<String>());
    @Override
    public Set<String> getKeys(String path) {
        ConfigurationSection cs = yaml.getConfigurationSection(path);
        if( cs != null ) {
            return cs.getKeys(false);
        }
        else {
            return emptySet;
        }
    }
    
    private static final List<String> emptyList = Collections.unmodifiableList(new ArrayList<String>());
    @Override
    public List<String> getStringList(String path) {
        List<String> list = yaml.getStringList(path);
        if( list != null )
            return list;
        else
            return emptyList;
    }

    @Override
    public double getDouble(String path) {
        return yaml.getDouble(path);
    }
}
