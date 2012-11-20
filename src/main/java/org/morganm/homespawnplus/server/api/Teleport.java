/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/** Interface for handling safe teleports.
 * 
 * @author morganm
 *
 */
public interface Teleport {
    
    /**
     *  Given a location, return the nearest safe location that a player could teleport
     *  to.
     *  
     * @param location the starting location
     * 
     * @return the safe location
     */
    public Location safeLocation(Location location);
    
    /**
     *  Given a location, return the nearest safe location that a player could teleport
     *  to.
     *  
     * @param location the starting location
     * @param options options that modify the operation of the safe search algorithm
     * 
     * @return the safe location
     */
    public Location safeLocation(Location location, TeleportOptions options);
    
    /**
     *  Teleport a player, using the safe teleport algorithm to find a safe location
     *  based on the given location
     *  
     * @param player the player to teleport
     * @param location the starting location
     */
    public void safeTeleport(Player player, Location location);
}
