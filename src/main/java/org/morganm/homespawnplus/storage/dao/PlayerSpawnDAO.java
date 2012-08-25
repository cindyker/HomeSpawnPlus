/**
 * 
 */
package org.morganm.homespawnplus.storage.dao;

import java.util.Set;

import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public interface PlayerSpawnDAO {
	public PlayerSpawn findById(int id);
	public PlayerSpawn findByWorldAndPlayerName(String world, String playerName);
	public Set<PlayerSpawn> findByPlayerName(String playerName);
	public Set<PlayerSpawn> findAll();
	public void save(PlayerSpawn playerSpawn) throws StorageException;
	public void delete(PlayerSpawn playerSpawn) throws StorageException;
}
