/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.entity.Spawn;


/** Wraps another storage type and caches the entities in memory as they are read and written.
 * 
 * @author morganm
 *
 */
public class StorageCache implements Storage
{
	@SuppressWarnings("unused")
	private static final Logger log = HomeSpawnPlus.log;
	
	@SuppressWarnings("unused")
	private final String logPrefix;
	private Storage original;

	// only populated if getallHomes() is called
	private Set<Home> allHomes;
	// only populated if getallSpawns() is called
	private Set<Spawn> allSpawns;
	// only populated if getallPlayers() is called
	private Set<Player> allPlayers;
	
	// flat list of all groups that are used in spawngroups
	private Set<String> spawnDefinedGroups;
	
	// cache of players 
	private HashMap<String, Player> players;
	// all homes, organized by world then player
	private HashMap <String, HashMap<String, Home>> homes;
	// all spawns, organized by world
//	private HashMap <String, Spawn> spawns;
	// group spawns organized by world then group
	private HashMap <String, HashMap<String, Spawn>> groupSpawns;
	
	public StorageCache(Storage original) {
		this.original = original;
		homes = new HashMap <String, HashMap<String, Home>>();
//		spawns = new HashMap <String, Spawn>();
		groupSpawns = new HashMap <String, HashMap<String, Spawn>>();
		players = new HashMap<String, Player>();
		
		logPrefix = HomeSpawnPlus.logPrefix;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#initializeStorage()
	 */
	@Override
	public void initializeStorage() {
		original.initializeStorage();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#getHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Home getHome(String world, String playerName) {
		HashMap<String, Home> worldHomes = homes.get(world);
		if( worldHomes == null ) {
			worldHomes = new HashMap<String, Home>();
			homes.put(world, worldHomes);
		}
		
		Home home = worldHomes.get(playerName);
		// not cached, lets get it from backing store
		if( home == null ) {
			home = original.getHome(world, playerName);
			worldHomes.put(playerName, home);				// add it to cache
		}
		
		return home;
	}

	@Override
	public Home getNamedHome(String homeName, String playerName) {
		// TODO: lazy pass-through, since cache is currently broken. When I fix the
		// cache, this method should be updated to use it as well.
		return original.getNamedHome(homeName, playerName);
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#getSpawn(java.lang.String)
	 */
	@Override
	public Spawn getSpawn(String world) {
		return getSpawn(world, Storage.HSP_WORLD_SPAWN_GROUP);
		
		/*
		Spawn spawn = spawns.get(world);
		// not cached, lets get it from backing store
		if( spawn == null ) {
			spawn = original.getSpawn(world);
			spawns.put(world, spawn);						// add it to cache
		}
		
		return spawn;
		*/
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#getSpawn(java.lang.String, java.lang.String)
	 */
	@Override
	public Spawn getSpawn(String world, String group) {
		HashMap<String, Spawn> worldSpawns = groupSpawns.get(world);
		if( worldSpawns == null ) {
			worldSpawns = new HashMap<String, Spawn>();
			groupSpawns.put(world, worldSpawns);
		}
		
		Spawn spawn = worldSpawns.get(group);
		// not cached, lets get it from backing store
		if( spawn == null ) {
//			log.info(logPrefix + " no cached spawn found for "+world+","+group+", calling backing store");
			spawn = original.getSpawn(world, group);
			worldSpawns.put(group, spawn);					// add it to cache
		}
		
//		log.info(logPrefix + " getSpawn for "+world+","+group+" returning object "+spawn);
		return spawn;
	}
	
	@Override
	public Spawn getSpawnByName(String name) {
		// TODO: too lazy to cache this right now, just pass through
		return original.getSpawnByName(name);
	}
	
	// update a Home in the in-memory cache
	private void updateHome(Home home) {
		String world = home.getWorld();
		String playerName = home.getPlayerName();
		
		HashMap<String, Home> worldHomes = homes.get(world);
		if( worldHomes == null ) {
			worldHomes = new HashMap<String, Home>();
			homes.put(world, worldHomes);
		}
		worldHomes.put(playerName, home);
		
		if( allHomes != null )
			allHomes.add(home);
	}

	/** Called only when getAllHomes() hits the database to return all homes - since we have
	 * them all in memory already, we store them in the key-value hash.
	 * 
	 * @param allHomes
	 */
	private void updateHomes(Set<Home> allHomes) {
		for(Home home : allHomes) {
			updateHome(home);
		}
	}
	
	// update a Spawn in the in-memory cache
	private void updateSpawn(Spawn spawn) {
		String world = spawn.getWorld();
		String group = spawn.getGroup();
		
//		spawns.put(world, spawn);
		
		HashMap<String, Spawn> worldSpawns = groupSpawns.get(world);
		if( worldSpawns == null ) {
			worldSpawns = new HashMap<String, Spawn>();
			groupSpawns.put(world, worldSpawns);
		}
		worldSpawns.put(group, spawn);
		
//		log.info(logPrefix + " updated cached spawn for "+world+","+group+" to "+spawn);
		
		// if the spawnDefinedGroups cache is populated and group isn't null, make sure this group is in the set.
		if( spawnDefinedGroups != null && group != null )
			spawnDefinedGroups.add(group);
		
		if( allSpawns != null )
			allSpawns.add(spawn);
	}
	
	private void updatePlayer(Player player) {
		players.put(player.getName(), player);
	}
	
	/** Called only when getAllSpawns() hits the database to return all spawns - since we have
	 * them all in memory already, we store them in the key-value hashes.
	 * 
	 * @param allSpawns
	 */
	private void updateSpawns(Set<Spawn> allSpawns) {
		for(Spawn spawn : allSpawns) {
			updateSpawn(spawn);
		}
	}
	
	private void updatePlayers(Set<Player> allPlayers) {
		for(Player player : allPlayers) {
			updatePlayer(player);
		}
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#getSpawnDefinedGroups()
	 */
	public Set<String> getSpawnDefinedGroups() {
		if( spawnDefinedGroups == null )
			spawnDefinedGroups = original.getSpawnDefinedGroups();
		
		return spawnDefinedGroups;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#getAllHomes()
	 */
	@Override
	public Set<Home> getAllHomes() {
		if( allHomes == null ) {
			allHomes = original.getAllHomes();
			updateHomes(allHomes);
		}
		
		return allHomes;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#getAllSpawns()
	 */
	@Override
	public Set<Spawn> getAllSpawns() {
		if( allSpawns == null ) {
			allSpawns = original.getAllSpawns();
			updateSpawns(allSpawns);
		}
		
		return allSpawns;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#getAllPlayers
	 */
	public Set<Player> getAllPlayers() {
		if( allPlayers == null ) {
			allPlayers = original.getAllPlayers();
			updatePlayers(allPlayers);
		}
		
		return allPlayers;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#writeHome(org.morganm.homespawnplus.Home)
	 */
	@Override
	public void writeHome(Home home) {
		original.writeHome(home);
		
		updateHome(home);

		// update the allHomes cache too if it is populated
		if( allHomes != null )
			allHomes.add(home);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.Storage#writeSpawn(org.morganm.homespawnplus.Spawn)
	 */
	@Override
	public void writeSpawn(Spawn spawn) {
		original.writeSpawn(spawn);
		
		updateSpawn(spawn);

		// update the allSpawns cache too if it is populated
		if( allSpawns != null )
			allSpawns.add(spawn);
	}

	@Override
	public void purgeCache() {
		allHomes.clear();
		allSpawns.clear();
		spawnDefinedGroups.clear();
		homes.clear();
//		spawns.clear();
		groupSpawns.clear();
		players.clear();
		
		original.purgeCache();
	}

	@Override
	public Player getPlayer(String name) {
		Player p = original.getPlayer(name);
		updatePlayer(p);
		return p;
	}

	@Override
	public void writePlayer(Player player) {
		players.remove(player.getName());
		original.writePlayer(player);
	}

	@Override
	public void removeHome(Home home) {
		String world = home.getWorld();
		String playerName = home.getPlayerName();
		
		HashMap<String, Home> worldHomes = homes.get(world);
		if( worldHomes != null )
			worldHomes.remove(playerName);
		if( allHomes != null )
			allHomes.remove(home);
		
		original.removeHome(home);
	}

	@Override
	public void deleteAllData() {
		purgeCache();
		original.deleteAllData();
	}
}
