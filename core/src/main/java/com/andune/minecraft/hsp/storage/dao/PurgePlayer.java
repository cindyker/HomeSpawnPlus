/**
 * 
 */
package com.andune.minecraft.hsp.storage.dao;

import java.util.Set;

/**
 * Interface for DAO storage objects that support the concept of purging a
 * player.
 * 
 * @author andune
 * 
 */
public interface PurgePlayer {
    /**
     * This method is called to purge a single player from the given backing
     * store for this entity type.
     * 
     * @param playerName the playername to purge
     * @return the number of rows purged
     */
    public int purgePlayer(String playerName);
    
    /**
     * Return a Set of all player names that this DAO knows about.
     * 
     * @return
     */
    public Set<String> getAllPlayerNames();

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.Storage#purgePlayerData(java.lang.Long)
     */
    public int purgePlayerData(long purgeTime);
    
}
