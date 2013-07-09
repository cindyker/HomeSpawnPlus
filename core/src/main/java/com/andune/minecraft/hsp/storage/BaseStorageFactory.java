/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
 */
/**
 * 
 */
package com.andune.minecraft.hsp.storage;

import javax.inject.Inject;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.config.ConfigStorage.Type;
import com.andune.minecraft.hsp.storage.cache.StorageCache;
import com.andune.minecraft.hsp.storage.ebean.StorageEBeans;
import com.google.inject.Injector;


/**
 * @author andune
 *
 */
public abstract class BaseStorageFactory implements Initializable, StorageFactory {
    protected static final Logger log = LoggerFactory.getLogger(BaseStorageFactory.class);
    
    protected final ConfigStorage configStorage;
    protected final Injector injector;
    protected final Plugin plugin;
	
    protected Storage storageInstance;
	
	@Inject
	public BaseStorageFactory(ConfigStorage configStorage, Injector injector, Plugin plugin) {
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
		
		return Type.UNKNOWN;
	}
	
	static public Type getType(String stringType) {
		Type[] types = Type.values();
		for(int i=0; i < types.length; i++) {
			if( types[i].toString().equalsIgnoreCase(stringType) )
				return types[i];
		}
		
        return Type.UNKNOWN;
	}
	
	/* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.StorageFactory#getInstance()
     */
	@Override
    public Storage getInstance()
	{
	    if( storageInstance != null )
	        return storageInstance;

	    Type storageType = configStorage.getStorageType();
		log.debug("StorageFactory.getInstance(), type = {}", storageType);
		
		switch(storageType)
		{
		case PERSISTANCE_REIMPLEMENTED_EBEANS:
            StorageEBeans ebeans = injector.getInstance(StorageEBeans.class);
            ebeans.setUsePersistanceReimplemented(true);
            storageInstance = ebeans;
			break;

        // container-specific factories must provide YAML capabilities if they support them
        // and assign storageInstance, at which point any future calls to this method will
        // return the assigned StorageInstance and not hit this exception.
        case YAML:
        case YAML_SINGLE_FILE:
            log.warn(storageType+" not implemented on this server container, defaulting to EBEANS");
            break;
	            
        case CACHED_EBEANS:
            log.warn("CACHED_EBEANS storage is no longer supported, defaulting to regular EBEANS storage");
            break;

        case EBEANS:
            // if they explicitly chose ebeans, just exit since that is the default
            break;
            
        default:
            log.warn("Unknown storage type encountered, defaulting to EBEANS storage");
            break;
		}
		
        // default is just to use EBEANS
		if( storageInstance == null )
            storageInstance = injector.getInstance(StorageEBeans.class);
		
		// if using in-memory cache, the cache will just wrap the
		// backing store already chosen.
		if( configStorage.useInMemoryCache() ) {
			Storage backingStore = storageInstance;
			storageInstance = new StorageCache(backingStore);
			injector.injectMembers(storageInstance);
		}
		
		log.debug("BaseStorageFactory:getInstance() selected {} as storage", storageInstance.getImplName());
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
        getInstance().shutdownStorage();
    }
}
