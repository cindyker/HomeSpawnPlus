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
package org.morganm.homespawnplus.storage;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.OldHSP;
import org.morganm.homespawnplus.storage.ebean.StorageEBeans;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;

import com.google.inject.Injector;


/**
 * @author morganm
 *
 */
@Singleton
public class StorageFactory {
	public enum Type {
		EBEANS,
		CACHED_EBEANS,
		YAML,
		YAML_SINGLE_FILE,
		PERSISTANCE_REIMPLEMENTED_EBEANS
	}
	
	private final Injector injector;
	
	@Inject
	public StorageFactory(Injector injector) {
	    this.injector = injector;
	}
	
	/** Ordinarily this is BAD to expose enum ordinal values. Sadly, these
	 * values started life as static ints and were exposed in the config
	 * directly that way, so many existing configs have the int values in
	 * them and so backwards compatibility requires we allow the int values
	 * to still work.
	 */
	public Type getType(int intType) {
		Type[] types = Type.values();
		for(int i=0; i < types.length; i++) {
			if( types[i].ordinal() == intType )
				return types[i];
		}
		
		return Type.EBEANS;		// default to EBEANS
	}
	
	public Type getType(String stringType) {
		Type[] types = Type.values();
		for(int i=0; i < types.length; i++) {
			if( types[i].toString().equalsIgnoreCase(stringType) )
				return types[i];
		}
		
		return Type.EBEANS;		// default to EBEANS
	}
	
	public Storage getInstance(Type storageType, OldHSP plugin)
		throws StorageException, IOException
	{
		Storage storage = null;
		
		switch(storageType)
		{
		case CACHED_EBEANS:
			OldHSP.log.warning(OldHSP.logPrefix + " CACHED_EBEANS storage not currently supported, defaulting to regular EBEANS storage");
		case EBEANS:
		    storage = injector.getInstance(StorageEBeans.class);
			break;

		case YAML:
			storage = new StorageYaml(plugin, false, null);
			break;
			
		case YAML_SINGLE_FILE:
			storage = new StorageYaml(plugin, true, new File(plugin.getDataFolder(), "data.yml"));
			break;
			
		case PERSISTANCE_REIMPLEMENTED_EBEANS:
            StorageEBeans ebeans = injector.getInstance(StorageEBeans.class);
            ebeans.setUsePersistanceReimplemented(true);
            storage = ebeans;
			break;
			
		default:
			throw new StorageException("Unable to create Storage interface, invalid type given: "+storageType);
		}
		
		return storage;
	}

}
