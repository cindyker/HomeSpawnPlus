/**
 * 
 */
package org.morganm.homespawnplus.server.api.events;

import org.morganm.homespawnplus.server.api.Location;

/**
 * @author morganm
 *
 */
public interface PlayerMoveEvent extends PlayerEvent {
    /**
     * Gets the location this player moved from
     *
     * @return Location the player moved from
     */
    public Location getFrom();

    /**
     * Gets the location this player moved to
     *
     * @return Location the player moved to
     */
    public Location getTo();

    /**
     * Sets the location that this player will move to
     *
     * @param to New Location this player will move to
     */
    public void setTo(Location to);
}
