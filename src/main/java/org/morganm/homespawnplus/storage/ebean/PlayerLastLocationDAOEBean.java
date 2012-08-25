/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import java.util.Set;

import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

/**
 * @author morganm
 *
 */
public class PlayerLastLocationDAOEBean implements PlayerLastLocationDAO {
	private EbeanServer ebean;
	
	public PlayerLastLocationDAOEBean(final EbeanServer ebean) {
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO#findById(int)
	 */
	@Override
	public PlayerLastLocation findById(int id) {
		String q = "find spawn where id = :id";
		Query<PlayerLastLocation> query = ebean.createQuery(PlayerLastLocation.class, q);
		query.setParameter("id", id);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO#findByWorldAndPlayerName(java.lang.String, java.lang.String)
	 */
	@Override
	public PlayerLastLocation findByWorldAndPlayerName(String world,
			String playerName) {
		String q = "find spawn where world = :world and player_name = :player_name";
		Query<PlayerLastLocation> query = ebean.createQuery(PlayerLastLocation.class, q);
		query.setParameter("world", world);
		query.setParameter("player_name", playerName);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO#findByPlayerName(java.lang.String)
	 */
	@Override
	public Set<PlayerLastLocation> findByPlayerName(String playerName) {
		String q = "find spawn where player_name = :player_name";
		Query<PlayerLastLocation> query = ebean.createQuery(PlayerLastLocation.class, q);
		query.setParameter("player_name", playerName);
		
		return query.findSet();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO#findAll()
	 */
	@Override
	public Set<PlayerLastLocation> findAll() {
		return ebean.find(PlayerLastLocation.class).findSet();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO#save(org.morganm.homespawnplus.entity.PlayerLastLocation)
	 */
	@Override
	public void save(PlayerLastLocation playerLastLocation) throws StorageException {
        ebean.save(playerLastLocation);
	}

}
