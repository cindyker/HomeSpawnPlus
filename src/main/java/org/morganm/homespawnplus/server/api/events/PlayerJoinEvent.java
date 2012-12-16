/**
 * 
 */
package org.morganm.homespawnplus.server.api.events;

import org.morganm.homespawnplus.server.api.Location;

/**
 * @author morganm
 *
 */
public interface PlayerJoinEvent extends PlayerEvent {
    /**
     * Set the location the player should spawn at.
     * 
     * @param l the location
     */
    public void setJoinLocation(Location l);
}
