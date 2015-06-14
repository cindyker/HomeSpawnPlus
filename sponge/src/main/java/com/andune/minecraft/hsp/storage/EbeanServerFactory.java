package com.andune.minecraft.hsp.storage;

import com.andune.minecraft.hsp.storage.ebean.SpongeEBeanUtils;
import com.andune.minecraft.hsp.storage.ebean.StorageEBeans;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
import com.google.inject.Inject;

/**
 * @author andune
 */
public class EbeanServerFactory {
    private final SpongeEBeanUtils eBeanUtils;

    @Inject
    public EbeanServerFactory(SpongeEBeanUtils eBeanUtils) {
        this.eBeanUtils = eBeanUtils;
    }

    public EbeanServer getEbeanServer(String name) {
        final ServerConfig db = new ServerConfig();

        db.setDefaultServer(false);
        db.setRegister(false);
        db.setClasses(StorageEBeans.getDatabaseClasses());
        db.setName(name);

        final DataSourceConfig ds = new DataSourceConfig();
        ds.setDriver(eBeanUtils.getDriver());
        ds.setUrl(eBeanUtils.getUrl());
        ds.setUsername(eBeanUtils.getUsername());
        ds.setPassword(eBeanUtils.getPassword());
        ds.setIsolationLevel(TransactionIsolation.getLevel(eBeanUtils.getIsolation()));
        db.setDatabasePlatform(new SQLitePlatform());
        db.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
        db.setDataSourceConfig(ds);

        return com.avaje.ebean.EbeanServerFactory.create(db);
    }
}
