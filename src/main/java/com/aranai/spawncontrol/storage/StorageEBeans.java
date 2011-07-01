/**
 * 
 */
package com.aranai.spawncontrol.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.entity.Home;
import com.aranai.spawncontrol.entity.Spawn;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

/** Implements storage using Bukkit Ebeans system.  This can be backed by either MySQL
 * or sqlLite depending on what the admin has configured in their bukkit.yml: it makes no
 * difference to us, the API is the same.
 * 
 * @author morganm
 *
 */
public class StorageEBeans implements Storage {
    private static final Logger log = SpawnControl.log;

	private final SpawnControl plugin;
	
	public StorageEBeans(SpawnControl plugin) {
		this.plugin = plugin;
		
		initializeStorage();
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#initializeStorage
	 */
	public void initializeStorage() {
		// Check that our tables exist - if they don't, then install the database. 
        try {
            EbeanServer db = plugin.getDatabase();
            if( db == null )
            	throw new NullPointerException("plugin.getDatabase() returned null EbeanServer!");
            
            db.find(Home.class).findRowCount();
            db.find(Spawn.class).findRowCount();
        } catch (PersistenceException ex) {
            log.info("Installing database for "
                    + plugin.getPluginName()
                    + " due to first time usage");
            plugin.initDB();
        }
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#getHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Home getHome(String world, String playerName) {
		EbeanServer db = plugin.getDatabase();
		String q = "find home where playerName = :playerName and world = :world";
		
		Query<Home> query = db.createQuery(Home.class, q);
		query.setParameter("playerName", playerName);
		query.setParameter("world", world);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#getSpawn(java.lang.String)
	 */
	@Override
	public Spawn getSpawn(String world) {
		EbeanServer db = plugin.getDatabase();
		String q = "find spawn where world = :world";
		
		Query<Spawn> query = db.createQuery(Spawn.class, q);
		query.setParameter("world", world);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#getSpawn(java.lang.String, java.lang.String)
	 */
	@Override
	public Spawn getSpawn(String world, String group) {
		EbeanServer db = plugin.getDatabase();
		String q = "find spawn where world = :world and group_name = :group";
		
		Query<Spawn> query = db.createQuery(Spawn.class, q);
		query.setParameter("world", world);
		query.setParameter("group", group);
		
		return query.findUnique();
	}

	/* We make the assumption that there are relatively few spawns and group combinations,
	 * thus the easiest algorithm is simply to grab all the spawns and iterate through
	 * them for the valid group list.
	 * 
	 * (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.Storage#getSpawnDefinedGroups()
	 */
	public Set<String> getSpawnDefinedGroups() {
		Set<String> groups = new HashSet<String>();
		Set<Spawn> spawns = getAllSpawns();
		
		for(Spawn spawn : spawns) {
			String group = spawn.getGroup();
			if( group != null )
				groups.add(group);
		}
		
		return groups;
	}

	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#getAllHomes
	 */
	public Set<Home> getAllHomes()	{
		return plugin.getDatabase().find(Home.class).findSet();
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#getAllSpawns
	 */
	public Set<Spawn> getAllSpawns() {
		return plugin.getDatabase().find(Spawn.class).findSet();
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#writeHome(com.aranai.spawncontrol.storage.Home)
	 */
	@Override
	public void writeHome(Home home) {
        plugin.getDatabase().save(home);
	}

	/* (non-Javadoc)
	 * @see com.aranai.spawncontrol.storage.IStorage#writeSpawn(com.aranai.spawncontrol.storage.Spawn)
	 */
	@Override
	public void writeSpawn(Spawn spawn) {
        plugin.getDatabase().save(spawn);
	}
}
