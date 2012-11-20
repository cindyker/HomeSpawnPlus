/**
 * 
 */
package org.morganm.homespawnplus.server.api.events;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;

/**
 * @author morganm
 *
 */
public interface PlayerJoinEvent {
    /**
     * Get the player that joined.
     * 
     * @return the player that joined
     */
    public Player getPlayer();
    
    /**
     * Set the location the player should spawn at.
     * 
     * @param l the location
     */
    public void setJoinLocation(Location l);
}
