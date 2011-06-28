/**
 * 
 */
package com.aranai.spawncontrol.config;

import java.io.File;
import java.io.IOException;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.storage.StorageException;

/**
 * @author morganm
 *
 */
public class ConfigFactory {
	public static enum Type
	{
		YAML
	}

	/** Get a Config instance of the specified type.
	 * 
	 * @param storageType type Type of config object to load
	 * @param plugin the calling plugin, passed to config objects, might be used to get database handle
	 * @param arg1 additional argument to be passed, perhaps filename or table name
	 * @return
	 * @throws StorageException
	 * @throws IOException
	 */
	public static Config getInstance(Type storageType, SpawnControl plugin, Object arg1)
	throws StorageException, IOException
{
	if ( storageType == Type.YAML ) {
		if( arg1 instanceof File )
			return new ConfigYAML((File) arg1);
		else
			throw new ConfigException("Unable to create Config interface: invalid YAML config file argument");
	}
	else {
		throw new ConfigException("Unable to create Config interface.");
	}
}
}
