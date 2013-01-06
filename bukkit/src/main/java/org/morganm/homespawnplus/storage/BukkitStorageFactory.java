/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.config.ConfigStorage;
import org.morganm.homespawnplus.config.ConfigStorage.Type;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;

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
