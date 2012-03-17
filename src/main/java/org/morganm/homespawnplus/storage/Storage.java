/**
 * 
 */
package org.morganm.homespawnplus.storage;

import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.storage.dao.HomeInviteDAO;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;
import org.morganm.homespawnplus.storage.dao.VersionDAO;


/** Storage interface for stored objects this plugin uses.
 * 
 * @author morganm
 *
 */
public interface Storage {
	public static final String HSP_WORLD_SPAWN_GROUP = "HSP_GLOBAL";
	public static final String HSP_BED_RESERVED_NAME = "bed";
	
	/* This method is called to initialize the storage system.  If using a DB back end, this
	 * is the method that should create the tables if they don't exist.
	 * 
	 * It is possible that this method could be called multiple times, so it is this methods
	 * responsibility to keep track of whether it has already initialized and deal with that
	 * situation appropriately. 
	 */
	public void initializeStorage();
	
	public HomeDAO getHomeDAO();
	public HomeInviteDAO getHomeInviteDAO();
	public SpawnDAO getSpawnDAO();
	public PlayerDAO getPlayerDAO();
	public VersionDAO getVersionDAO();
	
	/** Notify the backing store that it should purge any in-memory cache it has.
	 */
	public void purgeCache();
	
//	public Player getPlayer(String name);
//	public void writePlayer(Player player);
	
//	public Home getDefaultHome(String world, String playerName);
//	public Home getBedHome(String world, String playerName);
//	public Home getNamedHome(String homeName, String playerName);
//	public Set<Home> getHomes(String world, String playerName);
	
//	public Spawn getSpawn(String world);
//	public Spawn getSpawn(String world, String group);
//	public Spawn getSpawnByName(String name);
//	public Spawn getSpawnById(int id);
	
//	public Set<String> getSpawnDefinedGroups();
	
//	public Set<Home> getAllHomes();
//	public Set<Spawn> getAllSpawns();
//	public Set<Player> getAllPlayers();
	
//	public void writeHome(Home home);
//	public void writeSpawn(Spawn spawn);
	
//	public void deleteHome(Home home);
//	public void deleteSpawn(Spawn spawn);
	
	public void deleteAllData();
}
