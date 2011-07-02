/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.util.Set;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;


/** Storage interface for stored objects this plugin uses.
 * 
 * @author morganm
 *
 */
public interface Storage {
	/* This method is called to intialize the storage system.  If using a DB back end, this
	 * is the method that should create the tables if they don't exist.
	 * 
	 * It is possible that this method could be called multiple times, so it is this methods
	 * responsibility to keep track of whether it has already initialized and deal with that
	 * situation appropriately. 
	 */
	public void initializeStorage();
	
	/** Notify the backing store that it should purge any in-memory cache it has.
	 */
	public void purgeCache();
	
	public Home getHome(String world, String playerName);
	public Spawn getSpawn(String world);
	public Spawn getSpawn(String world, String group);
	
	public Set<String> getSpawnDefinedGroups();
	
	public Set<Home> getAllHomes();
	public Set<Spawn> getAllSpawns();
	
	public void writeHome(Home home);
	public void writeSpawn(Spawn spawn);
}
