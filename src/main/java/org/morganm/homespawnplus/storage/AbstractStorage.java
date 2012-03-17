/**
 * 
 */
package org.morganm.homespawnplus.storage;

import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.storage.dao.HomeInviteDAO;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;
import org.morganm.homespawnplus.storage.dao.VersionDAO;

/**
 * @author morganm
 *
 */
public abstract class AbstractStorage implements Storage {
	private HomeDAO homeDAO;
	private HomeInviteDAO homeInviteDAO;
	private SpawnDAO spawnDAO;
	private PlayerDAO playerDAO;
	private VersionDAO versionDAO;
	
	@Override
	public HomeDAO getHomeDAO() {
		return homeDAO;
	}

	@Override
	public HomeInviteDAO getHomeInviteDAO() {
		return homeInviteDAO;
	}

	@Override
	public SpawnDAO getSpawnDAO() {
		return spawnDAO;
	}

	@Override
	public PlayerDAO getPlayerDAO() {
		return playerDAO;
	}

	@Override
	public VersionDAO getVersionDAO() {
		return versionDAO;
	}

	protected void setHomeDAO(HomeDAO homeDAO) {
		this.homeDAO = homeDAO;
	}

	protected void setHomeInviteDAO(HomeInviteDAO homeInviteDAO) {
		this.homeInviteDAO = homeInviteDAO;
	}

	protected void setSpawnDAO(SpawnDAO spawnDAO) {
		this.spawnDAO = spawnDAO;
	}

	protected void setPlayerDAO(PlayerDAO playerDAO) {
		this.playerDAO = playerDAO;
	}

	protected void setVersionDAO(VersionDAO versionDAO) {
		this.versionDAO = versionDAO;
	}
}
