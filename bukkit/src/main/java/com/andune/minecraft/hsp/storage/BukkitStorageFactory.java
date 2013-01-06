/**
 * 
 */
package com.andune.minecraft.hsp.storage;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;


import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.config.ConfigStorage.Type;
import com.andune.minecraft.hsp.server.api.Plugin;
import com.andune.minecraft.hsp.storage.BaseStorageFactory;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.yaml.StorageYaml;
import com.google.inject.Injector;

/**
 * @author andune
 *
 */
@Singleton
public class BukkitStorageFactory extends BaseStorageFactory implements Initializable {
    @Inject
    public BukkitStorageFactory(ConfigStorage configStorage, Injector injector, Plugin plugin) {
        super(configStorage, injector, plugin);
    }

    @Override
    public Storage getInstance()
    {
        if( storageInstance != null )
            return storageInstance;

        Type storageType = configStorage.getStorageType();
        log.debug("BukkitStorageFactory.getInstance(), type = {}", storageType);
        
        switch(storageType)
        {
        case YAML:
            storageInstance = new StorageYaml(plugin, false, null);
            break;
            
        case YAML_SINGLE_FILE:
            storageInstance = new StorageYaml(plugin, true, new File(plugin.getDataFolder(), "data.yml"));
            break;
            
        default:
            // do nothing, super call below will do any additional storage resolution
        }
        
        return super.getInstance();
    }
}
