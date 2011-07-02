/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.io.IOException;

import org.morganm.homespawnplus.HomeSpawnPlus;


/**
 * @author morganm
 *
 */
public class StorageFactory {
	public static enum Type
	{
		EBEANS,
		CACHED_EBEANS
	}
	
	public static Storage getInstance(Type storageType, HomeSpawnPlus plugin)
		throws StorageException, IOException
	{
		if ( storageType == Type.EBEANS ) {
			return new StorageEBeans(plugin);
		}
		else if( storageType == Type.CACHED_EBEANS ) {
			return new StorageCache(new StorageEBeans(plugin));
		}
		else {
			throw new StorageException("Unable to create Storage interface.");
		}
	}

}
