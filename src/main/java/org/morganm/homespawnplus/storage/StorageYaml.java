/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.io.File;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.entity.Spawn;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/** Not yet a complete implementation, this exists primarily for the purpose of
 * facilitating backup/restore to a YAML file, so it only implements the required
 * functions to enable that process.
 * 
 * @author morganm
 *
 */
public class StorageYaml implements Storage {
//	private HomeSpawnPlus plugin;
	private final File file;
	private Configuration storage;
	
	public StorageYaml(final File file) {
		this.file = file;
	}
	
	/** Not yet part of the interface, can only be called directly or internally.
	 */
	public void save() {
		if( storage != null )
			storage.save();
	}
	
	public void addHomes(Set<Home> homes) {
		final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		for(Home home : homes) {
			// for the purposes of addHomes, it's assumed lastModified and dateCreated already
			// exist in the Set, but just in case, we set them to current time if they are not.
			if( home.getLastModified() == null )
				home.setLastModified(timestamp);
			if( home.getDateCreated() == null )
				home.setDateCreated(timestamp);
			
			int id = home.getId();
			String baseNode = "homes."+id;
			storage.setProperty(baseNode+".player_name", home.getPlayerName());
			storage.setProperty(baseNode+".updatedBy", home.getUpdatedBy());
			storage.setProperty(baseNode+".world", home.getWorld());
			storage.setProperty(baseNode+".x", home.getX());
			storage.setProperty(baseNode+".y", home.getY());
			storage.setProperty(baseNode+".z", home.getZ());
			storage.setProperty(baseNode+".pitch", home.getPitch());
			storage.setProperty(baseNode+".yaw", home.getYaw());
			storage.setProperty(baseNode+".lastModified", new Long(home.getLastModified().getTime()).toString());
			storage.setProperty(baseNode+".dateCreated", new Long(home.getDateCreated().getTime()).toString());
		}
	}
	
	public void addSpawns(Set<Spawn> spawns) {
		final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		for(Spawn spawn : spawns) {
			// for the purposes of addSpawns, it's assumed lastModified and dateCreated already
			// exist in the Set, but just in case, we set them to current time if they are not.
			if( spawn.getLastModified() == null )
				spawn.setLastModified(timestamp);
			if( spawn.getDateCreated() == null )
				spawn.setDateCreated(timestamp);
			
			int id = spawn.getId();
			String baseNode = "spawns."+id;
			storage.setProperty(baseNode+".world", spawn.getWorld());
			storage.setProperty(baseNode+".name", spawn.getName());
			storage.setProperty(baseNode+".updatedBy", spawn.getUpdatedBy());
			storage.setProperty(baseNode+".group_name", spawn.getGroup());
			storage.setProperty(baseNode+".x", spawn.getX());
			storage.setProperty(baseNode+".y", spawn.getY());
			storage.setProperty(baseNode+".z", spawn.getZ());
			storage.setProperty(baseNode+".pitch", spawn.getPitch());
			storage.setProperty(baseNode+".yaw", spawn.getYaw());
			storage.setProperty(baseNode+".lastModified", new Long(spawn.getLastModified().getTime()).toString());
			storage.setProperty(baseNode+".dateCreated", new Long(spawn.getDateCreated().getTime()).toString());
		}
	}
	
	public void addPlayers(Set<Player> players) {
		final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		for(Player player : players) {
			// for the purposes of addPlayers, it's assumed lastModified and dateCreated already
			// exist in the Set, but just in case, we set them to current time if they are not.
			if( player.getLastModified() == null )
				player.setLastModified(timestamp);
			if( player.getDateCreated() == null )
				player.setDateCreated(timestamp);
			
			int id = player.getId();
			String baseNode = "players."+id;
			storage.setProperty(baseNode+".name", player.getName());
			storage.setProperty(baseNode+".world", player.getWorld());
			storage.setProperty(baseNode+".x", player.getX());
			storage.setProperty(baseNode+".y", player.getY());
			storage.setProperty(baseNode+".z", player.getZ());
			storage.setProperty(baseNode+".pitch", player.getPitch());
			storage.setProperty(baseNode+".yaw", player.getYaw());
			storage.setProperty(baseNode+".lastModified", new Long(player.getLastModified().getTime()).toString());
			storage.setProperty(baseNode+".dateCreated", new Long(player.getDateCreated().getTime()).toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#initializeStorage()
	 */
	@Override
	public void initializeStorage() {
		storage = new Configuration(file);
		storage.load();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#purgeCache()
	 */
	@Override
	public void purgeCache() {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getPlayer(java.lang.String)
	 */
	@Override
	public Player getPlayer(String name) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#writePlayer(org.morganm.homespawnplus.entity.Player)
	 */
	@Override
	public void writePlayer(Player player) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Home getDefaultHome(String world, String playerName) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getSpawn(java.lang.String)
	 */
	@Override
	public Spawn getSpawn(String world) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getSpawn(java.lang.String, java.lang.String)
	 */
	@Override
	public Spawn getSpawn(String world, String group) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getSpawnByName(java.lang.String)
	 */
	@Override
	public Spawn getSpawnByName(String name) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getSpawnDefinedGroups()
	 */
	@Override
	public Set<String> getSpawnDefinedGroups() {
		throw new NotImplementedException();
	}

	public Home getHome(int id) {
		final String baseNode = "homes."+id;
		final ConfigurationNode node = storage.getNode(baseNode);
		if( node == null )
			return null;
		
		final Home home = new Home();
		home.setId(id);
		home.setPlayerName(storage.getString(baseNode+".player_name"));
		home.setUpdatedBy(storage.getString(baseNode+".updatedBy"));
		home.setWorld(storage.getString(baseNode+".world"));
		home.setX(storage.getDouble(baseNode+".x", 0));
		home.setY(storage.getDouble(baseNode+".y", 0));
		home.setZ(storage.getDouble(baseNode+".z", 0));
		home.setPitch(new Double(storage.getDouble(baseNode+".pitch", 0)).floatValue());
		home.setYaw(new Double(storage.getDouble(baseNode+".yaw", 0)).floatValue());
		
		String sLastModified = storage.getString(baseNode+".lastModified");
		long lastModified = 0L;
		try {
			lastModified = Long.valueOf(sLastModified);
		} catch(NumberFormatException e) {}
		home.setLastModified(new Timestamp(lastModified));
		
		String sDateCreated = storage.getString(baseNode+".dateCreated");
		long dateCreated = 0L;
		try {
			dateCreated = Long.valueOf(sDateCreated);
		} catch(NumberFormatException e) {}
		home.setDateCreated(new Timestamp(dateCreated));
		
		return home;
	}
	
	public Spawn getSpawn(int id) {
		final String baseNode = "spawns."+id;
		final ConfigurationNode node = storage.getNode(baseNode);
		if( node == null )
			return null;
		
		final Spawn spawn = new Spawn();
		spawn.setId(id);
		spawn.setName(storage.getString(baseNode+".name"));
		spawn.setGroup(storage.getString(baseNode+".group_name"));
		spawn.setUpdatedBy(storage.getString(baseNode+".updatedBy"));
		spawn.setWorld(storage.getString(baseNode+".world"));
		spawn.setX(storage.getDouble(baseNode+".x", 0));
		spawn.setY(storage.getDouble(baseNode+".y", 0));
		spawn.setZ(storage.getDouble(baseNode+".z", 0));
		spawn.setPitch(new Double(storage.getDouble(baseNode+".pitch", 0)).floatValue());
		spawn.setYaw(new Double(storage.getDouble(baseNode+".yaw", 0)).floatValue());
		
		String sLastModified = storage.getString(baseNode+".lastModified");
		long lastModified = 0L;
		try {
			lastModified = Long.valueOf(sLastModified);
		} catch(NumberFormatException e) {}
		spawn.setLastModified(new Timestamp(lastModified));
		
		String sDateCreated = storage.getString(baseNode+".dateCreated");
		long dateCreated = 0L;
		try {
			dateCreated = Long.valueOf(sDateCreated);
		} catch(NumberFormatException e) {}
		spawn.setDateCreated(new Timestamp(dateCreated));
		
		return spawn;
	}
	
	public Player getPlayer(int id) {
		final String baseNode = "players."+id;
		final ConfigurationNode node = storage.getNode(baseNode);
		if( node == null )
			return null;
		
		final Player player = new Player();
		player.setId(id);
		player.setName(storage.getString(baseNode+".name"));
		player.setWorld(storage.getString(baseNode+".world"));
		player.setX(storage.getDouble(baseNode+".x", 0));
		player.setY(storage.getDouble(baseNode+".y", 0));
		player.setZ(storage.getDouble(baseNode+".z", 0));
		player.setPitch(new Double(storage.getDouble(baseNode+".pitch", 0)).floatValue());
		player.setYaw(new Double(storage.getDouble(baseNode+".yaw", 0)).floatValue());
		
		String sLastModified = storage.getString(baseNode+".lastModified");
		long lastModified = 0L;
		try {
			lastModified = Long.valueOf(sLastModified);
		} catch(NumberFormatException e) {}
		player.setLastModified(new Timestamp(lastModified));
		
		String sDateCreated = storage.getString(baseNode+".dateCreated");
		long dateCreated = 0L;
		try {
			dateCreated = Long.valueOf(sDateCreated);
		} catch(NumberFormatException e) {}
		player.setDateCreated(new Timestamp(dateCreated));
		
		return player;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getAllHomes()
	 */
	@Override
	public Set<Home> getAllHomes() {
		HashSet<Home> homes = new HashSet<Home>();
		
		List<String> keys = storage.getKeys("homes");
		if( keys != null ) {
			for(String key : keys) {
				int id = Integer.valueOf(key);
				homes.add(getHome(id));
			}
		}
		
		return homes;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getAllSpawns()
	 */
	@Override
	public Set<Spawn> getAllSpawns() {
		HashSet<Spawn> spawns = new HashSet<Spawn>();
		
		List<String> keys = storage.getKeys("spawns");
		if( keys != null ) {
			for(String key : keys) {
				int id = Integer.valueOf(key);
				spawns.add(getSpawn(id));
			}
		}
		
		return spawns;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#getAllPlayers()
	 */
	@Override
	public Set<Player> getAllPlayers() {
		HashSet<Player> players = new HashSet<Player>();
		
		List<String> keys = storage.getKeys("players");
		if( keys != null ) {
			for(String key : keys) {
				int id = Integer.valueOf(key);
				players.add(getPlayer(id));
			}
		}
		
		return players;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#writeHome(org.morganm.homespawnplus.entity.Home)
	 */
	@Override
	public void writeHome(Home home) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#writeSpawn(org.morganm.homespawnplus.entity.Spawn)
	 */
	@Override
	public void writeSpawn(Spawn spawn) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.Storage#removeHome(org.morganm.homespawnplus.entity.Home)
	 */
	@Override
	public void removeHome(Home home) {
		throw new NotImplementedException();
	}
	
	@Override
	public void deleteAllData() {
		storage = null;
		file.delete();
		initializeStorage();		
	}

	@Override
	public Home getNamedHome(String homeName, String playerName) {
		throw new NotImplementedException();
	}

	@Override
	public Set<Home> getHomes(String world, String playerName) {
		throw new NotImplementedException();
	}
}
