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
import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.entity.Spawn;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;

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
	private final String logPrefix;
	
	public StorageEBeans(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.logPrefix = HomeSpawnPlus.logPrefix;
		
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
            db.find(Player.class).findRowCount();
        } catch (PersistenceException ex) {
            log.info("Installing database for "
                    + plugin.getPluginName()
                    + " due to first time usage");
            plugin.installDatabaseDDL();
        }
        
        try {
        	upgradeDatabase();
        } catch(Exception e) { e.printStackTrace(); }
	}
	
	private void upgradeDatabase() {
		int knownVersion = 063;		// start by assuming current version
		
		EbeanServer db = plugin.getDatabase();
		try {
			SqlQuery query = db.createSqlQuery("select world from hsp_player");
			query.findList();
		}
		catch(PersistenceException e) {
			knownVersion = 062;
		}
		
		if( knownVersion < 063 ) {
			log.info(logPrefix + " Upgrading from version 0.6.2 database to version 0.6.3");
			SqlUpdate update = db.createSqlUpdate("ALTER TABLE hsp_player "
					+ "ADD(`world` varchar(32) DEFAULT NULL"
					+ ",`x` double DEFAULT NULL"
					+ ",`y` double DEFAULT NULL"
					+ ",`z` double DEFAULT NULL"
					+ ",`pitch` float DEFAULT NULL"
					+ ",`yaw` float DEFAULT NULL);"
			);
			update.execute();
			log.info(logPrefix + " Upgrade from version 0.6.2 database to version 0.6.3 complete");
		}
		
		// TODO: upgrade for Spawn.group notNull
		// SQL: "alter table hsp_spawn modify group_name varchar(32);"
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

	@Override
	public Spawn getSpawnByName(String name) {
		EbeanServer db = plugin.getDatabase();
		String q = "find spawn where name = :name";
		
		Query<Spawn> query = db.createQuery(Spawn.class, q);
		query.setParameter("name", name);
		
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

	@Override
	public Player getPlayer(String name) {
		return plugin.getDatabase().find(Player.class).where().ieq("name", name).findUnique();
	}

	@Override
	public void writePlayer(Player player) {
        plugin.getDatabase().save(player);
	}

	@Override
	public void removeHome(Home home) {
		plugin.getDatabase().delete(home);
	}
}
