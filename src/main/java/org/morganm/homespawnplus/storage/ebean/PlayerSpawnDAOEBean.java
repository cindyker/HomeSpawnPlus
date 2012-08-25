/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import java.util.Set;

import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

/**
 * @author morganm
 *
 */
public class PlayerSpawnDAOEBean implements PlayerSpawnDAO {
	private EbeanServer ebean;
	
	public PlayerSpawnDAOEBean(final EbeanServer ebean) {
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}

	@Override
	public PlayerSpawn findById(int id) {
		String q = "find spawn where id = :id";
		Query<PlayerSpawn> query = ebean.createQuery(PlayerSpawn.class, q);
		query.setParameter("id", id);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO#findByWorldAndPlayerName(java.lang.String, java.lang.String)
	 */
	@Override
	public PlayerSpawn findByWorldAndPlayerName(String world, String playerName) {
		String q = "find spawn where world = :world and player_name = :player_name";
		Query<PlayerSpawn> query = ebean.createQuery(PlayerSpawn.class, q);
		query.setParameter("world", world);
		query.setParameter("player_name", playerName);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO#findByPlayerName(java.lang.String)
	 */
	@Override
	public Set<PlayerSpawn> findByPlayerName(String playerName) {
		String q = "find spawn where player_name = :player_name";
		Query<PlayerSpawn> query = ebean.createQuery(PlayerSpawn.class, q);
		query.setParameter("player_name", playerName);
		
		return query.findSet();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO#findAll()
	 */
	@Override
	public Set<PlayerSpawn> findAll() {
		return ebean.find(PlayerSpawn.class).findSet();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO#save(org.morganm.homespawnplus.entity.PlayerSpawn)
	 */
	@Override
	public void save(PlayerSpawn playerSpawn) throws StorageException {
        ebean.save(playerSpawn);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO#delete(org.morganm.homespawnplus.entity.PlayerSpawn)
	 */
	@Override
	public void delete(PlayerSpawn playerSpawn) throws StorageException {
        ebean.delete(playerSpawn);
	}

}
