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
	public enum Type {
		EBEANS,
		CACHED_EBEANS,
		YAML,
		YAML_SINGLE_FILE,
		PERSISTANCE_REIMPLEMENTED_EBEANS
	}
//	public static final int STORAGE_TYPE_EBEANS = 0;
//	public static final int STORAGE_TYPE_CACHED_EBEANS = 1;
//	public static final int STORAGE_TYPE_YAML_MULTI_FILE = 2;
//	public static final int STORAGE_TYPE_YAML_SINGLE_FILE = 3;
	
	/** Ordinarily this is BAD to expose enum ordinal values. Sadly, these
	 * values started life as static ints and were exposed in the config
	 * directly that way, so many existing configs have the int values in
	 * them and so backwards compatibility requires we allow the int values
	 * to still work.
	 */
	public static Type getType(int intType) {
		Type[] types = Type.values();
		for(int i=0; i < types.length; i++) {
			if( types[i].ordinal() == intType )
				return types[i];
		}
		
		return Type.EBEANS;		// default to EBEANS
	}
	
	public static Type getType(String stringType) {
		Type[] types = Type.values();
		for(int i=0; i < types.length; i++) {
			if( types[i].toString().equalsIgnoreCase(stringType) )
				return types[i];
		}
		
		return Type.EBEANS;		// default to EBEANS
	}
	
	public static Storage getInstance(Type storageType, HomeSpawnPlus plugin)
		throws StorageException, IOException
	{
		Storage storage = null;
		
		switch(storageType)
		{
		case CACHED_EBEANS:
			HomeSpawnPlus.log.warning(HomeSpawnPlus.logPrefix + " CACHED_EBEANS storage not currently supported, defaulting to regular EBEANS storage");
		case EBEANS:
			storage = new StorageEBeans(plugin);
			break;

		case YAML:
			storage = new StorageYaml(plugin, false, null);
			break;
			
		case YAML_SINGLE_FILE:
			storage = new StorageYaml(plugin, true, new File(plugin.getDataFolder(), "data.yml"));
			break;
			
		case PERSISTANCE_REIMPLEMENTED_EBEANS:
			storage = new StorageEBeans(plugin, true);
			break;
			
		default:
			throw new StorageException("Unable to create Storage interface, invalid type given: "+storageType);
		}
		
		return storage;
	}

}
