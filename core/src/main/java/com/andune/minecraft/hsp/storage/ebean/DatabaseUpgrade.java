package com.andune.minecraft.hsp.storage.ebean;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.entity.Version;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class responsible for checking to see if the database is updated to the
 * most current version and if not, upgrading the database from one version
 * to the next.
 *
 * @author andune
 */
public class DatabaseUpgrade {
    private static final int CURRENT_VERSION = 200;
    private final Logger log = LoggerFactory.getLogger(StorageEBeans.class);

    private final EbeanServer db;
    private final EBeanUtils ebeanUtils;
    private final StorageEBeans storage;

    /**
     * Intentional package-only visibility. Should not be instantiated from
     * outside of this package.
     */
    DatabaseUpgrade(EbeanServer db, EBeanUtils ebeanUtils, StorageEBeans storage) {
        this.db = db;
        this.ebeanUtils = ebeanUtils;
        this.storage = storage;
    }

    /**
     * Intentional package-only visibility. Should not be called from
     * outside of this package.
     */
    void upgradeDatabase() {
        // assume current version to start
        int knownVersion = CURRENT_VERSION;

        Version versionObject = null;
        try {
            versionObject = storage.getVersionDAO().getVersionObject();
        } catch (PersistenceException e) {
            // ignore exception
        }

        if (versionObject == null) {
            try {
                SqlUpdate update = db.createSqlUpdate("insert into hsp_version VALUES(1, " + CURRENT_VERSION + ")");
                update.execute();
            }
            // if the insert fails, then we know we are on version 63 (or earlier) of the db schema
            catch (PersistenceException e) {
                knownVersion = 63;
            }
        }
        else
            knownVersion = versionObject.getVersion();

        log.debug("knownVersion = {}", knownVersion);

        // determine if we are at version 62 of the database schema
        try {
            SqlQuery query = db.createSqlQuery("select world from hsp_player");
            query.setMaxRows(1);
            query.findList();
        } catch (PersistenceException e) {
            knownVersion = 62;
        }

        if (knownVersion < 63)
            updateToVersion63();

        if (knownVersion < 80)
            updateToVersion80();

        if (knownVersion < 91)
            updateToVersion91();

        if (knownVersion < 150)
            updateToVersion150();

        if (knownVersion < 170)
            updateToVersion170();

        if (knownVersion < 200)
            updateToVersion200();
    }

    private void updateToVersion63() {
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

    private void updateToVersion80() {
        log.info("Upgrading from version 0.6.3 database to version 0.8");
        SqlUpdate update = db.createSqlUpdate(
                "CREATE TABLE `hsp_version` (" +
                        "`id` int(11) NOT NULL," +
                        "`database_version` int(11) NOT NULL," +
                        "PRIMARY KEY (`id`)" +
                        ")"
        );
        update.execute();
        update = db.createSqlUpdate("insert into hsp_version VALUES(1,80)");
        update.execute();

        update = db.createSqlUpdate("ALTER TABLE hsp_spawn modify group_name varchar(32)");
        update.execute();
        log.info("Upgrade from version 0.6.3 database to version 0.8 complete");
    }

    private void updateToVersion91() {
        log.info("Upgrading from version 0.8 database to version 0.9.1");

        boolean success = false;
        // we must do some special work for SQLite since it doesn't respond to ALTER TABLE
        // statements from within the EBeanServer interface.  PITA!
        if (isSqlLite()) {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = ebeanUtils.getConnection();
                stmt = conn.createStatement();
                stmt.execute("BEGIN TRANSACTION;");
                stmt.execute("CREATE TEMPORARY TABLE hsphome_backup("
                        + "id integer primary key"
                        + ",player_name varchar(32)"
                        + ",updated_by varchar(32)"
                        + ",world varchar(32)"
                        + ",x double not null"
                        + ",y double not null"
                        + ",z double not null"
                        + ",pitch float not null"
                        + ",yaw float not null"
                        + ",last_modified timestamp not null"
                        + ",date_created timestamp not null);");
                stmt.execute("INSERT INTO hsphome_backup SELECT"
                        + " id, player_name,updated_by,world"
                        + ",x,y,z,pitch,yaw"
                        + ",last_modified,date_created"
                        + " FROM hsp_home;");
                stmt.execute("DROP TABLE hsp_home;");
                stmt.execute("CREATE TABLE hsp_home("
                        + "id integer primary key"
                        + ",player_name varchar(32)"
                        + ",name varchar(32)"
                        + ",updated_by varchar(32)"
                        + ",world varchar(32)"
                        + ",x double not null"
                        + ",y double not null"
                        + ",z double not null"
                        + ",pitch float not null"
                        + ",yaw float not null"
                        + ",default_home intger(1) not null DEFAULT 0"
                        + ",bed_home intger(1) not null DEFAULT 0"
                        + ",last_modified timestamp not null"
                        + ",date_created timestamp not null"
                        + ",constraint uq_hsp_home_1 unique (player_name,name));");
                stmt.execute("INSERT INTO hsp_home SELECT"
                        + " id, player_name,null,updated_by,world"
                        + ",x,y,z,pitch,yaw,1,0"
                        + ",last_modified,date_created"
                        + " FROM hsphome_backup;");
                stmt.execute("DROP TABLE hsphome_backup;");

                stmt.execute("COMMIT;");
                success = true;
            } catch (SQLException e) {
                log.error("error attempting to update SQLite database schema!", e);
            } finally {
                try {
                    if (stmt != null)
                        stmt.close();
                } catch (SQLException e) {
                    log.error("Caught exception closing SQL resource", e);
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                    log.error("Caught exception closing SQL resource", e);
                }
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

        if (success) {
            SqlUpdate update = db.createSqlUpdate("update hsp_home set default_home=1, bed_home=0");
            update.execute();

            update = db.createSqlUpdate("update hsp_version set database_version=91");
            update.execute();
            log.info("Upgrade from version 0.8 database to version 0.9.1 complete");
        }
    }

    private void updateToVersion150() {
        log.info("Upgrading from version 0.9.1 database to version 1.5.0");

        boolean success = false;
        if (isSqlLite()) {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = ebeanUtils.getConnection();
                stmt = conn.createStatement();
                stmt.execute("BEGIN TRANSACTION;");
                stmt.execute("CREATE TABLE hsp_homeinvite ("
                                + "id integer primary key,"
                                + "home_id integer not null,"
                                + "invited_player varchar(32) not null,"
                                + "expires timestamp,"
                                + "last_modified timestamp not null,"
                                + "date_created timestamp not null,"
                                + "constraint uq_hsp_homeinvite_1 unique (home_id,invited_player),"
                                + "constraint fk_hsp_homeinvite_home_1 foreign key (home_id) references hsp_home (id)"
                                + ");"
                );
                stmt.execute("CREATE INDEX ix_hsp_homeinvite_home_1 on hsp_homeinvite (home_id);");

                stmt.execute("COMMIT;");
                success = true;
            } catch (SQLException e) {
                log.error("error attempting to update SQLite database schema!", e);
            } finally {
                try {
                    if (stmt != null)
                        stmt.close();
                } catch (SQLException e) {
                    log.error("Caught exception closing SQL resource", e);
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                    log.error("Caught exception closing SQL resource", e);
                }
            }
        }
        // not SQLite
        else {
            SqlUpdate update = db.createSqlUpdate(
                    "CREATE TABLE `hsp_homeinvite` ("
                            + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                            + "`home_id` int(11) NOT NULL,"
                            + "`invited_player` varchar(32) NOT NULL,"
                            + "`expires` datetime DEFAULT NULL,"
                            + "`last_modified` datetime NOT NULL,"
                            + "`date_created` datetime NOT NULL,"
                            + "PRIMARY KEY (`id`),"
                            + "UNIQUE KEY `uq_hsp_homeinvite_1` (`home_id`,`invited_player`),"
                            + "KEY `ix_hsp_homeinvite_home_1` (`home_id`)"
//							+",CONSTRAINT `fk_hsp_homeinvite_home_1` FOREIGN KEY (`home_id`) REFERENCES `hsp_home` (`id`)"
                            + ")"
            );
            update.execute();
            success = true;
        }

        if (success) {
            SqlUpdate update = db.createSqlUpdate("update hsp_version set database_version=150");
            update.execute();
        }
        log.info("Upgrade from version 0.9.1 database to version 1.5.0 complete");
    }

    private void updateToVersion170() {
        log.info("Upgrading from version 1.5.0 database to version 1.7.0");

        boolean success = false;
        if (isSqlLite()) {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = ebeanUtils.getConnection();
                stmt = conn.createStatement();
                stmt.execute("BEGIN TRANSACTION;");
                stmt.execute("CREATE TABLE hsp_playerspawn (id integer primary key"
                                + ", player_name               varchar(32)"
                                + ", world                     varchar(32)"
                                + ", x                         double not null"
                                + ", y                         double not null"
                                + ", z                         double not null"
                                + ", pitch                     float"
                                + ", yaw                       float"
                                + ", spawn_id                  integer"
                                + ", last_modified             timestamp not null"
                                + ", date_created              timestamp not null"
                                + ", constraint uq_hsp_playerspawn_1 unique (world,player_name)"
                                + ", constraint fk_hsp_playerspawn_spawn_2 foreign key (spawn_id) references hsp_spawn (id)"
                                + ");"
                );
                stmt.execute("CREATE INDEX ix_hsp_playerspawn_spawn_2 on hsp_playerspawn (spawn_id);");

                stmt.execute("CREATE TABLE hsp_playerlastloc (id integer primary key"
                                + ", player_name               varchar(32)"
                                + ", world                     varchar(32)"
                                + ", x                         double not null"
                                + ", y                         double not null"
                                + ", z                         double not null"
                                + ", pitch                     float"
                                + ", yaw                       float"
                                + ", last_modified             timestamp not null"
                                + ", date_created              timestamp not null"
                                + ", constraint uq_hsp_playerlastloc_1 unique (world,player_name)"
                                + ");"
                );

                stmt.execute("COMMIT;");
                success = true;
            } catch (SQLException e) {
                log.error("error attempting to update SQLite database schema!", e);
            } finally {
                try {
                    if (stmt != null)
                        stmt.close();
                } catch (SQLException e) {
                    log.error("Caught exception closing SQL resource", e);
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                    log.error("Caught exception closing SQL resource", e);
                }
            }
        }
        else {
            SqlUpdate update = db.createSqlUpdate(
                    "CREATE TABLE `hsp_playerlastloc` ("
                            + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                            + "`player_name` varchar(32) DEFAULT NULL,"
                            + "`world` varchar(32) DEFAULT NULL,"
                            + "`x` double NOT NULL,"
                            + "`y` double NOT NULL,"
                            + "`z` double NOT NULL,"
                            + "`pitch` float DEFAULT NULL,"
                            + "`yaw` float DEFAULT NULL,"
                            + "`last_modified` datetime NOT NULL,"
                            + "`date_created` datetime NOT NULL,"
                            + "PRIMARY KEY (`id`),"
                            + "UNIQUE KEY `uq_hsp_playerlastloc_1` (`world`,`player_name`)"
                            + ")"
            );
            update.execute();

            update = db.createSqlUpdate(
                    "CREATE TABLE `hsp_playerspawn` ("
                            + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                            + "`player_name` varchar(32) DEFAULT NULL,"
                            + "`world` varchar(32) DEFAULT NULL,"
                            + "`x` double NOT NULL,"
                            + "`y` double NOT NULL,"
                            + "`z` double NOT NULL,"
                            + "`pitch` float DEFAULT NULL,"
                            + "`yaw` float DEFAULT NULL,"
                            + "`spawn_id` int(11) DEFAULT NULL,"
                            + "`last_modified` datetime NOT NULL,"
                            + "`date_created` datetime NOT NULL,"
                            + "PRIMARY KEY (`id`),"
                            + "UNIQUE KEY `uq_hsp_playerspawn_1` (`world`,`player_name`),"
                            + "KEY `ix_hsp_playerspawn_spawn_2` (`spawn_id`)"
                            + ")"
            );
            update.execute();

            success = true;
        }

        if (success) {
            SqlUpdate update = db.createSqlUpdate("update hsp_version set database_version=170");
            update.execute();
        }
        log.info("Upgrade from version 1.5.0 database to version 1.7.0 complete");
    }

    private void updateToVersion200() {
        log.info("Upgrading from version 1.7.0 database to version 2.0.0");

        // TODO: need to add UUID to player object
        boolean success = false;

        if (success) {
            SqlUpdate update = db.createSqlUpdate("update hsp_version set database_version=200");
            update.execute();
            log.info("Upgrade from version 1.7.0 database to version 2.0.0 complete");
        }
        else {
            log.error("Upgrade from version 1.7.0 database to version 2.0.0 ** NOT SUCCESSFUL **");
        }
    }

    /**
     * Return true if the backing Database is assumed to be SQLLite.
     *
     * @return
     */
    private boolean isSqlLite() {
        return ebeanUtils.isSqlLite();
    }
}
