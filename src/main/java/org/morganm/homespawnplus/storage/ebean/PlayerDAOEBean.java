/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import java.util.Set;

import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;

import com.avaje.ebean.EbeanServer;

/**
 * @author morganm
 *
 */
public class PlayerDAOEBean implements PlayerDAO {
	private EbeanServer ebean;
	
	public PlayerDAOEBean(final EbeanServer ebean) {
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerDAO#findPlayerByName(java.lang.String)
	 */
	@Override
	public Player findPlayerByName(String name) {
		return ebean.find(Player.class).where().ieq("name", name).findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerDAO#findAllPlayers()
	 */
	@Override
	public Set<Player> findAllPlayers() {
		return ebean.find(Player.class).findSet();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.PlayerDAO#savePlayer(org.morganm.homespawnplus.entity.Player)
	 */
	@Override
	public void savePlayer(Player player) {
        ebean.save(player);
	}

}
