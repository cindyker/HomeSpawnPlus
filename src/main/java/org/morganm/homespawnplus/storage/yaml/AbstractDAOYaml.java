/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.BasicEntity;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableYamlObject;
import org.morganm.homespawnplus.util.Debug;

/** Utilities/routines common to YAML DAOs.
 * 
 * @author morganm
 *
 */
public abstract class AbstractDAOYaml<T extends BasicEntity, U extends SerializableYamlObject<T>>
implements YamlDAOInterface
{
	protected final Debug debug;
	protected final String configBase;
	protected YamlConfiguration yaml;
	protected File file;
	protected int nextId = -1;
	
	private boolean deferredWrite = false;
	
	protected AbstractDAOYaml(final String configBase) {
		this.configBase = configBase;
		debug = Debug.getInstance();
	}

	// while we don't do advanced caching here (that can be implemented elsewhere), we
	// do some basic caching and invalidation on update/delete. Since the most common
	// function is reads, this is an easy optimization for the most common use case.
	private boolean cacheInvalid = true;
	private Set<T> allObjects;
	
	public void load() throws IOException, InvalidConfigurationException {
		if( yaml == null )
			this.yaml = new YamlConfiguration();
		if( file.exists() )
			yaml.load(file);
	}
	public void save() throws IOException {
		if( yaml != null )
			yaml.save(file);
	}
	
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
		debug.devDebug("findAllObjects() invoked for object ", this);
		// if the cache is still valid, just return that
		if( !cacheInvalid ) {
			debug.devDebug("findAllObjects() cache is valid, returning cache");
			return allObjects;
		}
		
		if( allObjects == null ) {
			debug.devDebug("findAllObjects() allObjects is null, initializing variable");
			allObjects = new HashSet<T>(100);
		}
		if( cacheInvalid ) {
			debug.devDebug("findAllObjects() cache flagged as invalid, clearing cache");
			allObjects.clear();
		}
		
		ConfigurationSection section = yaml.getConfigurationSection(configBase);
		if( section != null ) {
			Set<String> keys = section.getKeys(false);
			debug.devDebug("findAllObjects() config section ",configBase," found, loading elements. keys=",keys);
			
			for(String key : keys) {
				debug.devDebug("findAllObjects() loading key ",key);
				@SuppressWarnings("unchecked")
				U object = (U) yaml.get(configBase+"."+key);
				allObjects.add(object.getObject());
			}
		}
		debug.devDebug("findAllObjects() finished loading ",allObjects.size()," elements");
			
		cacheInvalid = false;
		return allObjects;
	}
	
	protected void saveObject(T object) throws StorageException {
		if( object.getId() == 0 )
			object.setId(getNextId());
		object.setLastModified(new Timestamp(System.currentTimeMillis()));

		final String node = configBase+"."+object.getId();
		debug.devDebug("YAML: saveObject called on ",object,", writing to node ",node);
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
