/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit.config;

import org.bukkit.configuration.file.YamlConfiguration;

import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.storage.BaseStorageFactory;

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
        return BaseStorageFactory.getType(yaml.getString("storage.type"));
    }

}
