/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.io.File;
import java.io.IOException;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.storage.ebean.StorageEBeans;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;


/**
 * @author morganm
 *
 */
public class StorageFactory {
	public static final int STORAGE_TYPE_EBEANS = 0;
	public static final int STORAGE_TYPE_CACHED_EBEANS = 1;
	public static final int STORAGE_TYPE_YAML_MULTI_FILE = 2;
	public static final int STORAGE_TYPE_YAML_SINGLE_FILE = 3;
	
	public static Storage getInstance(int storageType, HomeSpawnPlus plugin)
		throws StorageException, IOException
	{
		Storage storage = null;
		
		if ( storageType == STORAGE_TYPE_EBEANS ) {
			storage = new StorageEBeans(plugin);
		}
		else if( storageType == STORAGE_TYPE_CACHED_EBEANS ) {
			HomeSpawnPlus.log.warning(HomeSpawnPlus.logPrefix + " CACHED_EBEANS storage not currently supported, defaulting to regular EBEANS storage");
			storage = new StorageEBeans(plugin);
//			return new StorageCache(new StorageEBeans(plugin));
		}
		else if( storageType == STORAGE_TYPE_YAML_SINGLE_FILE ) {
			storage = new StorageYaml(plugin, true, new File(plugin.getDataFolder(), "data.yml"));
		}
		else if( storageType == STORAGE_TYPE_YAML_MULTI_FILE ) {
			storage = new StorageYaml(plugin, false, null);
		}
		else {
			throw new StorageException("Unable to create Storage interface, invalid type given: "+storageType);
		}
		
		return storage;
	}

}
