/**
 * 
 */
package org.morganm.homespawnplus.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.morganm.homespawnplus.Debug;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.entity.Version;

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
        EbeanServer db = plugin.getDatabase();
        if( db == null )
        	throw new NullPointerException("plugin.getDatabase() returned null EbeanServer!");
        
		// Check that our tables exist - if they don't, then install the database.
        try {
            db.find(Spawn.class).findRowCount();
        } catch (PersistenceException ex) {
            log.info("Installing database for "
                    + plugin.getPluginName()
                    + " due to first time usage");
            
            plugin.installDatabaseDDL();
			SqlUpdate update = db.createSqlUpdate("insert into hsp_version VALUES(1, 91)");
			update.execute();
        }
        
        try {
        	upgradeDatabase();
        } catch(Exception e) { e.printStackTrace(); }
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#getHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Set<Home> getHomes(String world, String playerName) {
		EbeanServer db = plugin.getDatabase();
		String q = "find home where playerName = :playerName and world like :world order by world";
		
		if( world == null || "all".equals(world) || "*".equals(world) )
			world = "%";
		
		Query<Home> query = db.createQuery(Home.class, q);
		query.setParameter("playerName", playerName);
		query.setParameter("world", world);
		
		return query.findSet();
	}

	@Override
	public Home getNamedHome(String homeName, String playerName) {
		EbeanServer db = plugin.getDatabase();
		String q = "find home where playerName = :playerName and name = :name";
		
		Query<Home> query = db.createQuery(Home.class, q);
		query.setParameter("playerName", playerName);
		query.setParameter("name", homeName);
		
		return query.findUnique();
	}
	
	@Override
	public Home getDefaultHome(String world, String playerName) {
		EbeanServer db = plugin.getDatabase();
		String q = "find home where playerName = :playerName and world = :world and defaultHome = 1";
		
		Query<Home> query = db.createQuery(Home.class, q);
		query.setParameter("playerName", playerName);
		query.setParameter("world", world);
		
		return query.findUnique();
		
	}
	
	@Override
	public Home getBedHome(String world, String playerName) {
		EbeanServer db = plugin.getDatabase();
		String q = "find home where playerName = :playerName and world = :world and bedHome = 1";
		
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
	 * @see org.morganm.homespawnplus.IStorage#getAllPlayers
	 */
	public Set<Player> getAllPlayers() {
		return plugin.getDatabase().find(Player.class).findSet();
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#writeHome(org.morganm.homespawnplus.Home)
	 */
	@Override
	public void writeHome(Home home) {
		plugin.getDatabase().beginTransaction();
		// We should only have one "BedHome" per player per world. So if this update is setting
		// BedHome to true, then we make sure to clear out all others for this player/world combo
		if( home.isBedHome() ) {
			SqlUpdate update = plugin.getDatabase().createSqlUpdate("update hsp_home set bed_home=0"
					+" where player_name = :playerName and world = :world and id != :id");
			update.setParameter("playerName", home.getPlayerName());
			update.setParameter("world", home.getWorld());
			update.setParameter("id", home.getId());
			update.execute();
		}
		
		// We should only have one defaultHome per player per world. So if this update is setting
		// defaultHome to true, then we make sure to clear out all others for this player/world combo
		if( home.isDefaultHome() ) {
			SqlUpdate update = plugin.getDatabase().createSqlUpdate("update hsp_home set default_home=0"
					+" where player_name = :playerName and world = :world and id != :id");
			update.setParameter("playerName", home.getPlayerName());
			update.setParameter("world", home.getWorld());
			update.setParameter("id", home.getId());
			update.execute();
		}
		plugin.getDatabase().commitTransaction();
		
        plugin.getDatabase().save(home);
	}
	
	@Override
	public void deleteHome(Home home) {
		plugin.getDatabase().delete(home);
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
	
	@Override
	public void deleteAllData() {
		EbeanServer db = plugin.getDatabase();
		db.beginTransaction();
		
		SqlUpdate update = db.createSqlUpdate("delete from hsp_spawn");
		update.execute();
		
		update = db.createSqlUpdate("delete from hsp_home");
		update.execute();
		
		update = db.createSqlUpdate("delete from hsp_player");
		update.execute();
		
		db.commitTransaction();
	}
	
	/** Return true if the backing Database is assumed to be SQLLite.
	 * 
	 * @return
	 */
	private boolean isSqlLite() {
		return EBeanUtils.getInstance().isSqlLite();
		
		/*
		EbeanServer db = plugin.getDatabase();
		
		// we start by assuming true and run a query to see if we can prove otherwise
		boolean isSqlLite = true;
		try {
			SqlQuery query = db.createSqlQuery("select VERSION();");
			query.findUnique();
			
			// if we get this far without blowing up it's definitely not SQLLite since SQLLite
			// doesn't support the VERSION() function.
			isSqlLite = false;
		}
		catch(Exception e) {}
		
		return isSqlLite;
		*/
	}
	
	private void upgradeDatabase() {
		int knownVersion = 80;		// assume current version to start
		
		EbeanServer db = plugin.getDatabase();

		Version versionObject = null;
		try {
			String q = "find version where id = 1";
			Query<Version> versionQuery = db.createQuery(Version.class, q);
			versionObject = versionQuery.findUnique();
		}
		// we ignore any exception here, we'll catch it below in the version checks
		catch(Exception e) {}
		
		if( versionObject == null ) {
			try {
				SqlUpdate update = db.createSqlUpdate("insert into hsp_version VALUES(1, 80)");
				update.execute();
			}
			// if the insert fails, then we know we are on version 63 (or earlier) of the db schema
			catch(PersistenceException e) {
				knownVersion = 63;
			}
		}
		else
			knownVersion = versionObject.getDatabaseVersion();
		
		Debug.getInstance().debug("knownVersion = ",knownVersion);

		/*
		try {
			SqlUpdate update = db.createSqlUpdate("insert into hsp_version VALUES(1,80)");
			update.execute();
			
			// use negative ID and random name so to be guaranteed to not conflict with any
			// legitmate 
			SqlUpdate update = db.createSqlUpdate("insert into hsp_spawn VALUES(-1,'world',null,'upgrade_check',null,0,0,0,0,0,SYSDATE(),SYSDATE());");
			update.execute();
			
			update = db.createSqlUpdate("delete from hsp_spawn where id = -1");
			update.execute();
		}
		catch(PersistenceException e) {
			knownVersion = 63;
		}
		*/
		
		// determine if we are at version 62 of the database schema
		try {
			SqlQuery query = db.createSqlQuery("select world from hsp_player");
			query.findList();
		}
		catch(PersistenceException e) {
			knownVersion = 62;
		}
		
		if( knownVersion < 63 ) {
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
		
		if( knownVersion < 80 ) {
			log.info(logPrefix + " Upgrading from version 0.6.3 database to version 0.8");
			SqlUpdate update = db.createSqlUpdate(
					"CREATE TABLE `hsp_version` ("+
					"`id` int(11) NOT NULL,"+
					"`database_version` int(11) NOT NULL,"+
					"PRIMARY KEY (`id`)"+
					")"
				);
			update.execute();
			update = db.createSqlUpdate("insert into hsp_version VALUES(1,80)");
			update.execute();

			update = db.createSqlUpdate("ALTER TABLE hsp_spawn modify group_name varchar(32)");
			update.execute();
			log.info(logPrefix + " Upgrade from version 0.6.3 database to version 0.8 complete");
		}
		
		if( knownVersion < 91 ) {
			log.info(logPrefix + " Upgrading from version 0.8 database to version 0.9.1");
			
			boolean success = false;
			// we must do some special work for SQLite since it doesn't respond to ALTER TABLE
			// statements from within the EBeanServer interface.  PITA!
			if( isSqlLite() ) {
				EBeanUtils ebu = EBeanUtils.getInstance();
				try {
					Connection conn = ebu.getConnection();
					Statement stmt = conn.createStatement();
					stmt.execute("BEGIN TRANSACTION;");
					stmt.execute("CREATE TEMPORARY TABLE hsphome_backup("
							+"id integer primary key"
							+",player_name varchar(32)"
							+",updated_by varchar(32)"
							+",world varchar(32)"
							+",x double not null"
							+",y double not null"
							+",z double not null"
							+",pitch float not null"
							+",yaw float not null"
							+",last_modified timestamp not null"
							+",date_created timestamp not null);");
					stmt.execute("INSERT INTO hsphome_backup SELECT"
							+" id, player_name,updated_by,world"
							+",x,y,z,pitch,yaw"
							+",last_modified,date_created"
							+" FROM hsp_home;");
					stmt.execute("DROP TABLE hsp_home;");
					stmt.execute("CREATE TABLE hsp_home("
							+"id integer primary key"
							+",player_name varchar(32)"
							+",name varchar(32)"
							+",updated_by varchar(32)"
							+",world varchar(32)"
							+",x double not null"
							+",y double not null"
							+",z double not null"
							+",pitch float not null"
							+",yaw float not null"
							+",default_home intger(1) not null DEFAULT 0"
							+",bed_home intger(1) not null DEFAULT 0"
							+",last_modified timestamp not null"
							+",date_created timestamp not null"
							+",constraint uq_hsp_home_1 unique (player_name,name));");
					stmt.execute("INSERT INTO hsp_home SELECT"
							+" id, player_name,null,updated_by,world"
							+",x,y,z,pitch,yaw,1,0"
							+",last_modified,date_created"
							+" FROM hsphome_backup;");
					stmt.execute("DROP TABLE hsphome_backup;");
					stmt.execute("COMMIT;");
//					stmt.execute("ALTER TABLE `hsp_home` ADD `name` varchar(32)");
//					stmt.execute("ALTER TABLE `hsp_home` ADD `bed_home` integer(1) not null");
//					stmt.execute("ALTER TABLE `hsp_home` ADD `default_home` integer(1) not null");
//					stmt.execute(sql);
					stmt.close();
					conn.close();
					
					success = true;
				}
				catch(SQLException e) {
					log.severe(logPrefix + " error attempting to update SQLite database schema!");
					e.printStackTrace();
				}
			}
			else {
				SqlUpdate update = db.createSqlUpdate("ALTER TABLE `hsp_home` ADD (`name` varchar(32)"
						+ ",`bed_home` tinyint(1) DEFAULT '0' NOT NULL"
						+ ",`default_home` tinyint(1) DEFAULT '0' NOT NULL"
						+ ");"
				  );
			    update.execute();
			    
			    update = db.createSqlUpdate("ALTER TABLE `hsp_home` DROP INDEX `uq_hsp_home_1`");
			    update.execute();
				
				update = db.createSqlUpdate("ALTER TABLE `hsp_home` ADD UNIQUE KEY `uq_hsp_home_1` (`player_name`,`name`)");
			    update.execute();
				success = true;
			}
			
			if( success ) {
				SqlUpdate update = db.createSqlUpdate("update hsp_home set default_home=1, bed_home=0");
				update.execute();
				
				update = db.createSqlUpdate("update hsp_version set database_version=91");
				update.execute();
				log.info(logPrefix + " Upgrade from version 0.8 database to version 0.9.1 complete");
			}
		}
	}
}
