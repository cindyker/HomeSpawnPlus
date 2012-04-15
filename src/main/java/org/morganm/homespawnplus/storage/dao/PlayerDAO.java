/**
 * 
 */
package org.morganm.homespawnplus.storage.dao;

import java.util.Set;

import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public interface PlayerDAO {
	public Player findPlayerByName(String name);
	public Set<Player> findAllPlayers();
	public void savePlayer(Player player) throws StorageException;
}
