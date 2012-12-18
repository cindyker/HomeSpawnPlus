package org.morganm.homespawnplus.util;

import org.morganm.homespawnplus.server.api.Block;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;

/**
 * Utility methods related to manipulating beds in the environment or
 * player.
 * 
 * This interface exists rather than a direct concrete class because there
 * is a circular dependency between BedUtils and HomeUtil. Guice can resolve
 * circular dependencies for us by using a temporary proxy, but only if
 * there is in interface involved to proxy.
 * @see org.morganm.homespawnplus.util.BedUtilsImpl
 * 
 * @author morganm
 *
 */
public interface BedUtils {

    /** Find a bed starting at a given Block, up to maxDepth blocks away.
     * 
     * @param l the location to start the search
     * @param maxDepth maximum distance from original location to search
     * 
     * @return the location of the bed found or null if none found
     */
    public Location findBed(Block b, int maxDepth);

    /** Although HSP records the exact location that a player is when clicking
     * on a bed (which allows the player to respawn exactly where they were),
     * the latest version of Bukkit actually check to see if the bed exists
     * at the players Bed location and print an annoying "Your home bed was
     * missing or obstructed" if there is not a block at the location.
     * 
     * So this method takes a given location and finds the exact location of
     * the nearest bed and sets the Bukkit location there.
     * 
     * @param player
     * @param l
     */
    public void setBukkitBedHome(Player player, Location l);

    /** Called when player right-clicks on a bed. Includes 2-click protection mechanism, if enabled.
     * 
     * @return true if the event should be canceled, false if not
     * @param p
     */
    public boolean doBedClick(Player player, Block bedBlock);

}