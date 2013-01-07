package com.andune.minecraft.hsp.storage.ebean;

import java.sql.Connection;
import java.sql.SQLException;

public interface EBeanUtils {
    public String getDriver();

    public String getUrl();

    public String getUsername();

    public String getPassword();

    public String getIsolation();

    public Boolean getLogging();

    public Boolean getRebuild();

    public boolean isSqlLite();

    public Connection getConnection() throws SQLException;
}