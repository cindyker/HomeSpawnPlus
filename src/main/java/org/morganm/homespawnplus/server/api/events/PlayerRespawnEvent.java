/**
 * 
 */
package org.morganm.homespawnplus.server.api.events;

import org.morganm.homespawnplus.server.api.Location;

/**
 * @author morganm
 *
 */
public interface PlayerRespawnEvent extends PlayerEvent {
    /**
     * Sets the new respawn location
     *
     * @param respawnLocation new location for the respawn
     */
    public void setRespawnLocation(Location respawnLocation);

    /**
     * Gets whether the respawn location is the player's bed.
     *
     * @return true if the respawn location is the player's bed.
     */
    public boolean isBedSpawn();
}
