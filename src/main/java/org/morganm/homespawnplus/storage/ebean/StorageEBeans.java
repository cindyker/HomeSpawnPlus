/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.storage.dao.AbstractStorage;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.MyDatabase;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;

/** Implements storage using Bukkit Ebeans system.  This can be backed by either MySQL
 * or sqlLite depending on what the admin has configured in their bukkit.yml: it makes no
 * difference to us, the API is the same.
 * 
 * @author morganm
 *
 */
public class StorageEBeans extends AbstractStorage {
	private static final int CURRENT_VERSION = 91;
    private static final Logger log = HomeSpawnPlus.log;

	private final HomeSpawnPlus plugin;
	private final String logPrefix;
	private MyDatabase database;
	
	public StorageEBeans(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.logPrefix = HomeSpawnPlus.logPrefix;
		
		initializeStorage();
	}
	
	public MyDatabase getDatabase() {
		return database;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#initializeStorage
	 */
	public void initializeStorage() {
        database = new MyDatabase(plugin) {
        	protected java.util.List<Class<?>> getDatabaseClasses() {
        		return plugin.getDatabaseClasses();
            };        	
        };
        
        EBeanUtils utils = EBeanUtils.getInstance();
        database.initializeDatabase(utils.getDriver(), utils.getUrl(), utils.getUsername(),
        		utils.getPassword(), utils.getIsolation(), utils.getLogging(), utils.getRebuild());

        setHomeDAO(new HomeDAOEBean(database.getDatabase()));
        setHomeInviteDAO(new HomeInviteDAOEBean(database.getDatabase(), plugin));
        setSpawnDAO(new SpawnDAOEBean(database.getDatabase()));
        setPlayerDAO(new PlayerDAOEBean(database.getDatabase()));
        setVersionDAO(new VersionDAOEBean(database.getDatabase()));
        
        try {
        	upgradeDatabase();
        } catch(Exception e) { e.printStackTrace(); }
	}
	
	@Override
	public void purgeCache() {
		// in theory we should pass this call through to the EBEAN server, but it doesn't
		// offer any support for this functionality.  So we do nothing.
	}

	@Override
	public void deleteAllData() {
		EbeanServer db = database.getDatabase();
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
	}
	
	private void upgradeDatabase() {
		int knownVersion = CURRENT_VERSION;		// assume current version to start
		final EbeanServer db = database.getDatabase();
		final Version versionObject = getVersionDAO().getVersionObject();
		
		if( versionObject == null ) {
			try {
				SqlUpdate update = db.createSqlUpdate("insert into hsp_version VALUES(1, "+CURRENT_VERSION+")");
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

		// determine if we are at version 62 of the database schema
		try {
			SqlQuery query = db.createSqlQuery("select world from hsp_player");
			query.findList();
		}
		catch(PersistenceException e) {
			knownVersion = 62;
		}
		
		if( knownVersion < 63 )
			updateToVersion63(db);
		
		if( knownVersion < 80 )
			updateToVersion80(db);
		
		if( knownVersion < 91 )
			updateToVersion91(db);
	}
	
	private void updateToVersion63(final EbeanServer db) {
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
	
	private void updateToVersion80(final EbeanServer db) {
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
	
	private void updateToVersion91(final EbeanServer db) {
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
//				stmt.execute("ALTER TABLE `hsp_home` ADD `name` varchar(32)");
//				stmt.execute("ALTER TABLE `hsp_home` ADD `bed_home` integer(1) not null");
//				stmt.execute("ALTER TABLE `hsp_home` ADD `default_home` integer(1) not null");
//				stmt.execute(sql);
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
