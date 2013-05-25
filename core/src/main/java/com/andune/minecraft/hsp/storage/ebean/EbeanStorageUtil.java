/**
 * 
 */
package com.andune.minecraft.hsp.storage.ebean;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.andune.minecraft.commonlib.server.api.OfflinePlayer;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.dao.PurgePlayer;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;

/**
 * Common routines utilized by storage subsystem objects.
 * 
 * @author andune
 *
 */
@Singleton
public class EbeanStorageUtil {
    private final EbeanServer ebean;
    private final Server server;
    
    @Inject
    public EbeanStorageUtil(Server server, EbeanServer ebean) {
        this.server = server;
        this.ebean = ebean;
    }
    
    public int purgePlayers(PurgePlayer purgeDAO, long purgeTime) {
        int rowsPurged = 0;
        
        Set<String> allPlayerNames = purgeDAO.getAllPlayerNames();
        for(String playerName : allPlayerNames) {
            OfflinePlayer player = server.getOfflinePlayer(playerName);
            
            if( player.getLastPlayed() < purgeTime ) {
                rowsPurged += purgeDAO.purgePlayer(playerName);
            }
        }
        
        return rowsPurged;
    }

    /**
     * Given a table, column and unique value, delete those rows from the
     * tables.
     * 
     * @param table
     * @param column
     * @param value
     * @return
     */
    public int deleteRows(String table, String column, String value) {
        int rowsPurged = 0;
        
        Transaction tx = ebean.beginTransaction();
        SqlUpdate update = ebean.createSqlUpdate("delete from "+table+" where "+column+" = :value");
        update.setParameter("value", value);
        rowsPurged += update.execute();
        tx.commit();
        
        return rowsPurged;
    }
}
