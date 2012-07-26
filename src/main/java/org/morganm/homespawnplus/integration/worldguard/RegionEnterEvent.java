/**
 * 
 */
package org.morganm.homespawnplus.integration.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author morganm
 *
 */
public class RegionEnterEvent extends RegionEvent {
    /**
     * 
     * @param regionName the name of the region
     * @param world the world the region belongs to
     * @param player the player involved in this event
     * @param to the location the player is moving to
     */
    public RegionEnterEvent(final String regionName, final String regionWorldName, final Player player, final Location to) {
    	super(regionName, regionWorldName, player, to);
    }
}
