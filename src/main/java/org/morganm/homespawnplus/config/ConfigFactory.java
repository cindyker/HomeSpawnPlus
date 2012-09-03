/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.IOException;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.storage.StorageException;


/**
 * @author morganm
 *
 */
public class ConfigFactory {
	public static enum Type
	{
		YAML,
		YAML_EXTENDED_DEFAULT_FILE
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
	public static Config getInstance(Type storageType, HomeSpawnPlus plugin, Object arg1)
	throws ConfigException, IOException
	{
		if ( storageType == Type.YAML || storageType == Type.YAML_EXTENDED_DEFAULT_FILE ) {
			File file = null; 
			if( arg1 instanceof File )
				file = (File) arg1;
			else if( arg1 instanceof String )
				file = new File((String) arg1);
			else
				throw new ConfigException("Unable to create Config interface: invalid YAML config file argument");

			ConfigurationYAML config = new ConfigurationYAML(file, plugin);
			if( storageType == Type.YAML_EXTENDED_DEFAULT_FILE )
				config.setExtendedFile(true);

			return config;
		}
		else {
			throw new ConfigException("Unable to create Config interface.");
		}
	}
}
