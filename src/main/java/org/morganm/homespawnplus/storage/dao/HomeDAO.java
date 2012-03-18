/**
 * 
 */
package org.morganm.homespawnplus.storage.dao;

import java.util.Set;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public interface HomeDAO {
	public Home findHomeById(int id);
	public Home findDefaultHome(String world, String playerName);
	public Home findBedHome(String world, String playerName);
	public Home findHomeByNameAndPlayer(String homeName, String playerName);
	/** Given a world and player, find all homes on that world for that player.
	 * 
	 * @param world the world. A value of 'null', 'all' or '*' should be treated
	 * as a search for all worlds.
	 * @param playerName
	 * @return
	 */
	public Set<Home> findHomesByWorldAndPlayer(String world, String playerName);
	public Set<Home> findHomesByPlayer(String playerName);
	public Set<Home> findAllHomes();

	public void saveHome(Home home) throws StorageException;
	public void deleteHome(Home home) throws StorageException;
}
