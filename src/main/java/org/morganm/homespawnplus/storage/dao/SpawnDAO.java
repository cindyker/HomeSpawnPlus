/**
 * 
 */
package org.morganm.homespawnplus.storage.dao;

import java.util.Set;

import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public interface SpawnDAO {
	public Spawn findSpawnByWorld(String world);
	public Spawn findSpawnByWorldAndGroup(String world, String group);
	public Spawn findSpawnByName(String name);
	public Spawn findSpawnById(int id);
	
	/** Return full set of defined spawn groups.
	 * 
	 * @return
	 */
	public Set<String> getSpawnDefinedGroups();

	public Set<Spawn> findAllSpawns();

	public void saveSpawn(Spawn spawn) throws StorageException;
	public void deleteSpawn(Spawn spawn) throws StorageException;
}
