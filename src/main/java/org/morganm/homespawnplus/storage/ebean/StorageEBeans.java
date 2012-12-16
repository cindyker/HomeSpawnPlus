/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Singleton
public class StorageEBeans implements Storage {
    private static final Logger log = LoggerFactory.getLogger(StorageEBeans.class);
	private static final int CURRENT_VERSION = 170;

	private MyDatabase persistanceReimplementedDatabase;
	private boolean usePersistanceReimplemented = false;
	
	private final EbeanServer ebeanServer;
	private final EBeanUtils ebeanUtils;
	private final Plugin plugin;
	private HomeDAOEBean homeDAO;
	private HomeInviteDAOEBean homeInviteDAO;
	private SpawnDAOEBean spawnDAO;
	private PlayerDAOEBean playerDAO;
	private VersionDAOEBean versionDAO;
	private PlayerSpawnDAOEBean playerSpawnDAO;
	private PlayerLastLocationDAOEBean playerLastLocationDAO;

	@Inject
	public StorageEBeans(EbeanServer ebeanServer, EBeanUtils ebeanUtils, Plugin plugin) {
	    this.ebeanServer = ebeanServer;
	    this.ebeanUtils = ebeanUtils;
	    this.plugin = plugin;
		this.usePersistanceReimplemented = false;
	}
	
	public void setUsePersistanceReimplemented(boolean usePersistanceReimplemented) {
	    this.usePersistanceReimplemented = usePersistanceReimplemented;
	}
	
	@Override
	public String getImplName() {
		if( usePersistanceReimplemented )
			return "PersistenceReimplemented";
		else
			return "EBEANS";
	}
	
	public MyDatabase getPersistanceReimplementedDatabase() {
		return persistanceReimplementedDatabase;
	}
	
	public EbeanServer getDatabase() {
		if( usePersistanceReimplemented )
			return persistanceReimplementedDatabase.getDatabase();
		else
			return ebeanServer;
	}
	
	public boolean isUsePersistanceReimplemented() {
		return usePersistanceReimplemented;
	}

    public static List<Class<?>> getDatabaseClasses() {
        List<Class<?>> classList = new LinkedList<Class<?>>();
        classList.add(Home.class);
        classList.add(Spawn.class);
        classList.add(org.morganm.homespawnplus.entity.Player.class);
        classList.add(Version.class);
        classList.add(HomeInvite.class);
        classList.add(PlayerSpawn.class);
        classList.add(PlayerLastLocation.class);
        return classList;
    }

	private void persistanceReimplementedInitialize() {
		persistanceReimplementedDatabase = new MyDatabase(plugin) {
        	protected java.util.List<Class<?>> getDatabaseClasses() {
        		return getDatabaseClasses();
            };        	
        };

        persistanceReimplementedDatabase.initializeDatabase(ebeanUtils.getDriver(), ebeanUtils.getUrl(),
                ebeanUtils.getUsername(), ebeanUtils.getPassword(), ebeanUtils.getIsolation(),
                ebeanUtils.getLogging(), ebeanUtils.getRebuild());
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.IStorage#initializeStorage
	 */
	public void initializeStorage() throws StorageException {
		if( usePersistanceReimplemented ) {
			persistanceReimplementedInitialize();
		}
		else {
	        if( ebeanServer == null )
	        	throw new NullPointerException("null EbeanServer!");
	        
			// Check that our tables exist - if they don't, then install the database.
	        try {
	            ebeanServer.find(Spawn.class).findRowCount();
	        } catch (PersistenceException ex) {
	            log.info("Installing database for "
	                    + plugin.getName()
	                    + " due to first time usage");
	            
	            // for some reason bukkit's EBEAN implementation blows up when trying
	            // to create the HomeInvite FK relationship. Pesistance reimplemented
	            // does not have this problem. So if we have to initialize the database,
	            // we always do it with Persistance Reimplemented, regardless of the
	            // EBEAN implementation we will use after this initialization.
	            persistanceReimplementedInitialize();
//	            plugin.installDatabaseDDL();
	        }
		}
        
        homeDAO = new HomeDAOEBean(getDatabase());
        homeInviteDAO = new HomeInviteDAOEBean(getDatabase(), this);
        spawnDAO = new SpawnDAOEBean(getDatabase());
        playerDAO = new PlayerDAOEBean(getDatabase());
        versionDAO = new VersionDAOEBean(getDatabase());
        playerSpawnDAO = new PlayerSpawnDAOEBean(getDatabase());
        playerLastLocationDAO = new PlayerLastLocationDAOEBean(getDatabase());
        
        try {
        	upgradeDatabase();
        } catch(Exception e) { e.printStackTrace(); }
	}
	
	@Override
	public org.morganm.homespawnplus.storage.dao.HomeDAO getHomeDAO() { return homeDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.HomeInviteDAO getHomeInviteDAO() { return homeInviteDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.PlayerDAO getPlayerDAO() { return playerDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.SpawnDAO getSpawnDAO() { return spawnDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.VersionDAO getVersionDAO() { return versionDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO getPlayerSpawnDAO() { return playerSpawnDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO getPlayerLastLocationDAO() { return playerLastLocationDAO; }
	
	@Override
	public void purgeCache() {
		// in theory we should pass this call through to the EBEAN server, but it doesn't
		// offer any support for this functionality.  So we do nothing.
	}

	@Override
	public void deleteAllData() {
		EbeanServer db = getDatabase();
		db.beginTransaction();
		
		SqlUpdate update = db.createSqlUpdate("delete from hsp_spawn");
		update.execute();
		
		update = db.createSqlUpdate("delete from hsp_home");
		update.execute();
		
		update = db.createSqlUpdate("delete from hsp_player");
		update.execute();
		
		update = db.createSqlUpdate("delete from hsp_homeinvite");
		update.execute();

		update = db.createSqlUpdate("delete from hsp_playerspawn");
		update.execute();
		
		update = db.createSqlUpdate("delete from hsp_playerlastloc");
		update.execute();
		
		db.commitTransaction();
	}
	
	/** Return true if the backing Database is assumed to be SQLLite.
	 * 
	 * @return
	 */
	private boolean isSqlLite() {
		return ebeanUtils.isSqlLite();
	}
	
	private void upgradeDatabase() {
		int knownVersion = CURRENT_VERSION;		// assume current version to start
		final EbeanServer db = getDatabase();
		Version versionObject = null;
		try {
			versionObject = getVersionDAO().getVersionObject();
		}
		catch(PersistenceException e) {
			// ignore exception
		}
		
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
			knownVersion = versionObject.getVersion();
		
		log.debug("knownVersion = {}",knownVersion);

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
		
		if( knownVersion < 150 )
			updateToVersion150(db);
		
		if( knownVersion < 170 )
			updateToVersion170(db);
	}
	
	private void updateToVersion63(final EbeanServer db) {
		log.info("Upgrading from version 0.6.2 database to version 0.6.3");
		SqlUpdate update = db.createSqlUpdate("ALTER TABLE hsp_player "
				+ "ADD(`world` varchar(32) DEFAULT NULL"
				+ ",`x` double DEFAULT NULL"
				+ ",`y` double DEFAULT NULL"
				+ ",`z` double DEFAULT NULL"
				+ ",`pitch` float DEFAULT NULL"
				+ ",`yaw` float DEFAULT NULL);"
		);
		update.execute();
		log.info("Upgrade from version 0.6.2 database to version 0.6.3 complete");
	}
	
	private void updateToVersion80(final EbeanServer db) {
		log.info("Upgrading from version 0.6.3 database to version 0.8");
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
		log.info("Upgrade from version 0.6.3 database to version 0.8 complete");
	}
	
	private void updateToVersion91(final EbeanServer db) {
		log.info("Upgrading from version 0.8 database to version 0.9.1");
		
		boolean success = false;
		// we must do some special work for SQLite since it doesn't respond to ALTER TABLE
		// statements from within the EBeanServer interface.  PITA!
		if( isSqlLite() ) {
			try {
				Connection conn = ebeanUtils.getConnection();
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
				log.error("error attempting to update SQLite database schema!", e);
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
			log.info("Upgrade from version 0.8 database to version 0.9.1 complete");
		}
	}
	
	private void updateToVersion150(final EbeanServer db) {
		log.info("Upgrading from version 0.9.1 database to version 1.5.0");
		
		boolean success = false;
		if( isSqlLite() ) {
			try {
				Connection conn = ebeanUtils.getConnection();
				Statement stmt = conn.createStatement();
				stmt.execute("BEGIN TRANSACTION;");
				stmt.execute("CREATE TABLE hsp_homeinvite ("
						+"id integer primary key,"
						+"home_id integer not null,"
						+"invited_player varchar(32) not null,"
						+"expires timestamp,"
						+"last_modified timestamp not null,"
						+"date_created timestamp not null,"
						+"constraint uq_hsp_homeinvite_1 unique (home_id,invited_player),"
						+"constraint fk_hsp_homeinvite_home_1 foreign key (home_id) references hsp_home (id)"
						+");"
						);
				stmt.execute("CREATE INDEX ix_hsp_homeinvite_home_1 on hsp_homeinvite (home_id);");

				stmt.execute("COMMIT;");
				stmt.close();
				conn.close();
				
				success = true;
			}
			catch(SQLException e) {
				log.error("error attempting to update SQLite database schema!", e);
			}
		}
		else {	// not SQLite
			SqlUpdate update = db.createSqlUpdate(
					"CREATE TABLE `hsp_homeinvite` ("
							+"`id` int(11) NOT NULL AUTO_INCREMENT,"
							+"`home_id` int(11) NOT NULL,"
							+"`invited_player` varchar(32) NOT NULL,"
							+"`expires` datetime DEFAULT NULL,"
							+"`last_modified` datetime NOT NULL,"
							+"`date_created` datetime NOT NULL,"
							+"PRIMARY KEY (`id`),"
							+"UNIQUE KEY `uq_hsp_homeinvite_1` (`home_id`,`invited_player`),"
							+"KEY `ix_hsp_homeinvite_home_1` (`home_id`)"
//							+",CONSTRAINT `fk_hsp_homeinvite_home_1` FOREIGN KEY (`home_id`) REFERENCES `hsp_home` (`id`)"
							+")"
					);
			update.execute();
			success = true;
		}
		
		if( success ) {
			SqlUpdate update = db.createSqlUpdate("update hsp_version set database_version=150");
			update.execute();
		}
		log.info("Upgrade from version 0.9.1 database to version 1.5.0 complete");
	}

	private void updateToVersion170(final EbeanServer db) {
		log.info("Upgrading from version 1.5.0 database to version 1.7.0");
		
		boolean success = false;
		if( isSqlLite() ) {
			try {
				Connection conn = ebeanUtils.getConnection();
				Statement stmt = conn.createStatement();
				stmt.execute("BEGIN TRANSACTION;");
				stmt.execute("CREATE TABLE hsp_playerspawn (id integer primary key"
						+", player_name               varchar(32)"
						+", world                     varchar(32)"
						+", x                         double not null"
						+", y                         double not null"
						+", z                         double not null"
						+", pitch                     float"
						+", yaw                       float"
						+", spawn_id                  integer"
						+", last_modified             timestamp not null"
						+", date_created              timestamp not null"
						+", constraint uq_hsp_playerspawn_1 unique (world,player_name)"
						+", constraint fk_hsp_playerspawn_spawn_2 foreign key (spawn_id) references hsp_spawn (id)"
						+");"
						);
				stmt.execute("CREATE INDEX ix_hsp_playerspawn_spawn_2 on hsp_playerspawn (spawn_id);");

				stmt.execute("CREATE TABLE hsp_playerlastloc (id integer primary key"
						+", player_name               varchar(32)"
						+", world                     varchar(32)"
						+", x                         double not null"
						+", y                         double not null"
						+", z                         double not null"
						+", pitch                     float"
						+", yaw                       float"
						+", last_modified             timestamp not null"
						+", date_created              timestamp not null"
						+", constraint uq_hsp_playerlastloc_1 unique (world,player_name)"
						+");"
						);
				
				stmt.execute("COMMIT;");
				stmt.close();
				conn.close();
				
				success = true;
			}
			catch(SQLException e) {
				log.error("error attempting to update SQLite database schema!", e);
			}
		}
		else {
			SqlUpdate update = db.createSqlUpdate(
					"CREATE TABLE `hsp_playerlastloc` ("
					+"`id` int(11) NOT NULL AUTO_INCREMENT,"
					+"`player_name` varchar(32) DEFAULT NULL,"
					+"`world` varchar(32) DEFAULT NULL,"
					+"`x` double NOT NULL,"
					+"`y` double NOT NULL,"
					+"`z` double NOT NULL,"
					+"`pitch` float DEFAULT NULL,"
					+"`yaw` float DEFAULT NULL,"
					+"`last_modified` datetime NOT NULL,"
					+"`date_created` datetime NOT NULL,"
					+"PRIMARY KEY (`id`),"
					+"UNIQUE KEY `uq_hsp_playerlastloc_1` (`world`,`player_name`)"
					+")"
				);
			update.execute();
			
			update = db.createSqlUpdate(
					"CREATE TABLE `hsp_playerspawn` ("
					+"`id` int(11) NOT NULL AUTO_INCREMENT,"
					+"`player_name` varchar(32) DEFAULT NULL,"
					+"`world` varchar(32) DEFAULT NULL,"
					+"`x` double NOT NULL,"
					+"`y` double NOT NULL,"
					+"`z` double NOT NULL,"
					+"`pitch` float DEFAULT NULL,"
					+"`yaw` float DEFAULT NULL,"
					+"`spawn_id` int(11) DEFAULT NULL,"
					+"`last_modified` datetime NOT NULL,"
					+"`date_created` datetime NOT NULL,"
					+"PRIMARY KEY (`id`),"
					+"UNIQUE KEY `uq_hsp_playerspawn_1` (`world`,`player_name`),"
					+"KEY `ix_hsp_playerspawn_spawn_2` (`spawn_id`)"
					+")"
				);
			update.execute();
			
			success = true;
		}

		if( success ) {
			SqlUpdate update = db.createSqlUpdate("update hsp_version set database_version=170");
			update.execute();
		}
		log.info("Upgrade from version 1.5.0 database to version 1.7.0 complete");
	}
	
	// Ebeans does nothing with these methods
	@Override
	public void setDeferredWrites(boolean deferred) {}
	@Override
	public void flushAll() throws StorageException {}
}
