/**
 * 
 */
package com.aranai.spawncontrol.storage;

import java.io.IOException;

import com.aranai.spawncontrol.SpawnControl;

/**
 * @author morganm
 *
 */
public class StorageFactory {
	public static enum Type
	{
		EBEANS
	}
	
	public static Storage getInstance(Type storageType, SpawnControl plugin)
		throws StorageException, IOException
	{
		if ( storageType == Type.EBEANS ) {
			return new StorageEBeans(plugin);
		}
		else {
			throw new StorageException("Unable to create Storage interface.");
		}
	}

}
