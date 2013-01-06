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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.config.ConfigStorage;
import org.morganm.homespawnplus.config.ConfigStorage.Type;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.storage.ebean.StorageEBeans;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;


/**
 * @author morganm
 *
 */
@Singleton
public class StorageFactory implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(StorageFactory.class);
    
//	public enum Type {
//		EBEANS,
//		CACHED_EBEANS,
//		YAML,
//		YAML_SINGLE_FILE,
//		PERSISTANCE_REIMPLEMENTED_EBEANS
//	}
	
    private final ConfigStorage configStorage;
	private final Injector injector;
	private final Plugin plugin;
	
	private Storage storageInstance;
	
	@Inject
	public StorageFactory(ConfigStorage configStorage, Injector injector, Plugin plugin) {
	    this.configStorage = configStorage;
	    this.injector = injector;
	    this.plugin = plugin;
	}
	
	/** Ordinarily this is BAD to expose enum ordinal values. Sadly, these
	 * values started life as static ints and were exposed in the config
	 * directly that way, so many existing configs have the int values in
	 * them and so backwards compatibility requires we allow the int values
	 * to still work.
	 */
	static public Type getType(int intType) {
		Type[] types = Type.values();
		for(int i=0; i < types.length; i++) {
			if( types[i].ordinal() == intType )
				return types[i];
		}
		
		return Type.EBEANS;		// default to EBEANS
	}
	
	static public Type getType(String stringType) {
		Type[] types = Type.values();
		for(int i=0; i < types.length; i++) {
			if( types[i].toString().equalsIgnoreCase(stringType) )
				return types[i];
		}
		
		return Type.EBEANS;		// default to EBEANS
	}
	
	public Storage getInstance()
	{
	    if( storageInstance != null )
	        return storageInstance;

	    Type storageType = configStorage.getStorageType();
		log.debug("StorageFactory.getInstance(), type = {}", storageType);
		
		switch(storageType)
		{
		case YAML:
		    storageInstance = new StorageYaml(plugin, false, null);
			break;
			
		case YAML_SINGLE_FILE:
		    storageInstance = new StorageYaml(plugin, true, new File(plugin.getDataFolder(), "data.yml"));
			break;
			
		case PERSISTANCE_REIMPLEMENTED_EBEANS:
            StorageEBeans ebeans = injector.getInstance(StorageEBeans.class);
            ebeans.setUsePersistanceReimplemented(true);
            storageInstance = ebeans;
			break;
			
        case CACHED_EBEANS:
            log.warn("CACHED_EBEANS storage is no longer supported, defaulting to regular EBEANS storage");
        case EBEANS:
        default:                        // default is just to use EBEANS
            storageInstance = injector.getInstance(StorageEBeans.class);
            break;

//		default:
//			throw new StorageException("Unable to create Storage interface, invalid type given: "+storageType);
		}
		
		return storageInstance;
	}

    @Override
    public void init() throws Exception {
        getInstance().initializeStorage();
    }

    @Override
    public int getInitPriority() {
        return 8;
    }

    @Override
    public void shutdown() throws Exception {
        getInstance().flushAll();
    }
}
