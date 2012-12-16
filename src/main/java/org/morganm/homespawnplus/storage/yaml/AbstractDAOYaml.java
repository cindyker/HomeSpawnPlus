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
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.BasicEntity;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableYamlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities/routines common to YAML DAOs.
 * 
 * @author morganm
 *
 */
public abstract class AbstractDAOYaml<T extends BasicEntity, U extends SerializableYamlObject<T>>
implements YamlDAOInterface
{
    protected static final Logger log = LoggerFactory.getLogger(AbstractDAOYaml.class);

	protected final String configBase;
	protected YamlConfiguration yaml;
	protected File file;
	protected int nextId = -1;
	
	private boolean deferredWrite = false;
	
	// while we don't do advanced caching here (that can be implemented elsewhere), we
	// do some basic caching and invalidation on update/delete. Since the most common
	// function is reads, this is an easy optimization for the most common use case.
	private boolean cacheInvalid = true;
	private Set<T> allObjects;
	
	protected AbstractDAOYaml(final String configBase) {
		this.configBase = configBase;
	}

	public synchronized void load() throws IOException, InvalidConfigurationException {
		if( yaml == null ) {
			log.debug("AbstractDAOLYaml::load() instantiating new YamlConfiguration object");
			this.yaml = new YamlConfiguration();
		}
		if( file.exists() ) {
			log.debug("loading YAML file {}", file);
			yaml.load(file);
			log.debug("sections: {}",yaml.getKeys(false));
		}
	}
	public void save() throws IOException {
		if( yaml != null )
			yaml.save(file);
	}
//	public boolean isLoading() { return isLoading; }
	
	protected int getNextId() {
		// if we've already loaded the nextId, just increment it
		if( nextId != -1 )
			return ++nextId;
		
		// otherwise we need to figure it out
		int maxId=0;
		Set<? extends BasicEntity> objects = findAllObjects();
		if( objects != null && objects.size() > 0 ) {
			for(BasicEntity e: objects) {
				if( e.getId() > maxId )
					maxId = e.getId();
			}
		}
		if( maxId < 1 )
			maxId = 1;
		
		// now that we kow the max ID so far, increment it and return
		nextId = ++maxId;
		return nextId;
	}
	
	protected abstract U newSerializable(T object);

	protected Set<T> findAllObjects() {
		log.debug("findAllObjects() invoked for object {}",this);
		// if the cache is still valid, just return that
		if( !cacheInvalid ) {
			log.debug("findAllObjects() cache is valid, returning cache, size={}",allObjects.size());
			return allObjects;
		}
		
		if( allObjects == null ) {
			log.debug("findAllObjects() allObjects is null, initializing variable");
			allObjects = new LinkedHashSet<T>(100);
		}
		if( cacheInvalid ) {
			log.debug("findAllObjects() cache flagged as invalid, clearing cache");
			allObjects.clear();
		}
		
		log.debug("findAllObjects() loading section {}",configBase);
		ConfigurationSection section = yaml.getConfigurationSection(configBase);
		if( section != null ) {
			Set<String> keys = section.getKeys(false);
			log.debug("findAllObjects() config section {} found, loading elements. keys={}", configBase, keys);
			
			for(String key : keys) {
				log.debug("findAllObjects() loading key {}",key);
				@SuppressWarnings("unchecked")
				U object = (U) yaml.get(configBase+"."+key);
				allObjects.add(object.getObject());
			}
		}
		else
			log.debug("findAllObjects() section {} not found", configBase);
		
		log.debug("findAllObjects() finished loading {} elements", allObjects.size());
			
		cacheInvalid = false;
		return allObjects;
	}
	
	protected void saveObject(T object) throws StorageException {
		if( object.getId() == 0 )
			object.setId(getNextId());
		object.setLastModified(new Timestamp(System.currentTimeMillis()));

		final String node = configBase+"."+object.getId();
		log.debug("YAML: saveObject called on {}, writing to node {}", object, node);
		yaml.set(node, newSerializable(object));
		if( !deferredWrite ) {
			try {
				save();
			}
			catch(Exception e) {
				throw new StorageException(e);
			}
		}

		cacheInvalid = true;
	}

	public void deleteObject(T object) throws StorageException {
		if( object.getId() == 0 )
			return;
		
		yaml.set(configBase+"."+object.getId(), null);
		if( !deferredWrite ) {
			try {
				save();
			}
			catch(Exception e) {
				throw new StorageException(e);
			}
		}

		cacheInvalid = true;
	}
	
	public void invalidateCache() {
		cacheInvalid = true;
		if( allObjects != null )
			allObjects.clear();
	}
	
	public void setDeferredWrite(boolean deferred) {
		this.deferredWrite = deferred;
	}
	public void flush() throws StorageException {
		try {
			save();
		}
		catch(Exception e) {
			throw new StorageException(e);
		}
	}
	public void deleteAllData() throws StorageException {
		invalidateCache();
		yaml.set(configBase, null);
		if( file != null && file.exists() )
			file.delete();
	}
}
