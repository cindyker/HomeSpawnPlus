/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/**
 * @author morganm
 *
 */
public interface PermissionSystem {
    /**
     * Return a string that represents the permission system
     * in use, this can be used for reporting metrics or printing
     * to logs.
     * 
     * @return
     */
    public String getSystemInUse();
    
    /**
     * Determine if a given sender has a specific permission.
     * 
     * @param worldName
     * @param playerName
     * @param permission
     * @return
     */
    public boolean has(String worldName, String playerName, String permission);
    
    /**
     * Determine if a given sender has a specific permission.
     * 
     * @param sender
     * @param permission
     * @return
     */
    public boolean has(CommandSender sender, String permission);

    /**
     * Given a world and a player, return that players "primary" group.
     * 
     * @param playerWorld
     * @param playerName
     * @return
     */
    public String getPlayerGroup(String playerWorld, String playerName);
}
