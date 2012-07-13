/**
 * 
 */
package org.morganm.homespawnplus.storage.dao;

import java.util.Set;

import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public interface PlayerLastLocationDAO {
	public PlayerLastLocation findById(int id);
	public PlayerLastLocation findByWorldAndPlayerName(String world, String playerName);
	public Set<PlayerLastLocation> findByPlayerName(String playerName);
	public Set<PlayerLastLocation> findAll();
	public void save(PlayerLastLocation playerLastLocation) throws StorageException;
}
