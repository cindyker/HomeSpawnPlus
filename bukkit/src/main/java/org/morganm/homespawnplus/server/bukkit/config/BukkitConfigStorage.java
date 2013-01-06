/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.config.ConfigStorage;
import org.morganm.homespawnplus.storage.StorageFactory;

/**
 * @author morganm
 *
 */
public class BukkitConfigStorage implements ConfigStorage {
    private final YamlConfiguration yaml;

    public BukkitConfigStorage(YamlConfiguration yaml) {
        this.yaml = yaml;
    }
    
    @Override
    public Type getStorageType() {
        return StorageFactory.getType(yaml.getString("storage.type"));
    }

}
