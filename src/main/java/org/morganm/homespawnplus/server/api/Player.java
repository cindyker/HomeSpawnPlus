/**
 * 
 */
package org.morganm.homespawnplus.server.api;

import org.bukkit.World;

/**
 * @author morganm
 *
 */
public interface Player extends CommandSender {
    /**
     * Determine if this player is a newly joined player (just logged in for the first time ever).
     * 
     * @return true if the player is new
     */
    public boolean isNewPlayer();
    
    /**
     *  Return the name of this player. Note this is the actual, proper-case name the the player
     *  logs into Minecraft with, not their display name which can be changed by plugins later.
     *  
     * @return the players name
     */
    public String getName();
    
    /**
     * Return the current Location of this player object
     * 
     * @return the current location
     */
    public Location getLocation();
    
    /**
     * Gets the current world this player resides in
     *
     * @return World
     */
    public World getWorld();
    
    /**
     * Determine if the player has the specified permission.
     * 
     * @param permission the permission to check
     * 
     * @return true if the player has the permission
     */
    public boolean hasPermission(String permission);
    
    /**
     * Gets the Location where the player will spawn at their bed, null if they have not slept
     * in one or their current bed spawn is invalid.
     *
     * @return Bed Spawn Location if bed exists, otherwise null.
     */
    public Location getBedSpawnLocation();
    
    /**
     * Sets the Location where the player will spawn at their bed.
     *
     * @param location where to set the respawn location
     */
    public void setBedSpawnLocation(Location location);
}
