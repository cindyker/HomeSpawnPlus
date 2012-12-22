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

    /**
     * Teleport a player to a location, with optional TeleportOptions. Uses
     * safeTeleport algorithm if safeTeleport is enabled.
     * 
     * @param p the player to teleport
     * @param l the location to teleport to
     * @param options optional TeleportOptions (can be null)
     */
    public void teleport(Player p, Location l, TeleportOptions options);


    /** Given a min and max (that define a square cube "region"), randomly pick
     * a location in between them, and then find a "safe spawn" point based on
     * that location (ie. that won't suffocate or be right above lava, etc).
     * 
     * @param min
     * @param max
     * @return the random safe Location, or null if one couldn't be located
     */
    public Location findRandomSafeLocation(Location min, Location max, TeleportOptions options);
}
