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


    /*
    {
        if( l == null || p == null )
            return;
        if( cause == null )
            cause = TeleportCause.UNKNOWN;
        
        if( plugin.getConfig().getBoolean(ConfigOptions.SAFE_TELEPORT, true) ) {
            Location safeLocation = null;
            if( context != null )
                safeLocation = General.getInstance().getTeleport().safeLocation(l, context.getModeBounds(), context.getModeSafeTeleportFlags());
            else
                safeLocation = General.getInstance().getTeleport().safeLocation(l);
            
            if( safeLocation != null )
                l = safeLocation;
        }
        
        Teleport.getInstance().setCurrentTeleporter(p.getName());
        p.teleport(l, cause);
        Teleport.getInstance().setCurrentTeleporter(null);
    }*/
}
