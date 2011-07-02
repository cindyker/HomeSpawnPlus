/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;

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
    private static final Logger log = HomeSpawnPlus.log;

	private final HomeSpawnPlus plugin;
	
	public StorageEBeans(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		
		initializeStorage();
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#initializeStorage
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
            plugin.installDatabaseDDL();
        }
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#getHome(java.lang.String, java.lang.String)
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
	 * @see org.morganm.homespawnplus.IStorage#getSpawn(java.lang.String)
	 */
	@Override
	public Spawn getSpawn(String world) {
		return getSpawn(world, Storage.HSP_WORLD_SPAWN_GROUP);
		
		/*
		EbeanServer db = plugin.getDatabase();
		String q = "find spawn where world = :world";
		
		Query<Spawn> query = db.createQuery(Spawn.class, q);
		query.setParameter("world", world);
		
		return query.findUnique();
		*/
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#getSpawn(java.lang.String, java.lang.String)
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
	 * @see org.morganm.homespawnplus.Storage#getSpawnDefinedGroups()
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
	 * @see org.morganm.homespawnplus.IStorage#getAllHomes
	 */
	public Set<Home> getAllHomes()	{
		return plugin.getDatabase().find(Home.class).findSet();
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#getAllSpawns
	 */
	public Set<Spawn> getAllSpawns() {
		return plugin.getDatabase().find(Spawn.class).findSet();
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#writeHome(org.morganm.homespawnplus.Home)
	 */
	@Override
	public void writeHome(Home home) {
        plugin.getDatabase().save(home);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#writeSpawn(org.morganm.homespawnplus.Spawn)
	 */
	@Override
	public void writeSpawn(Spawn spawn) {
        plugin.getDatabase().save(spawn);
	}

	@Override
	public void purgeCache() {
		// in theory we should pass this call through to the EBEAN server, but it doesn't
		// offer any support for this functionality.  So we do nothing.
	}
}
