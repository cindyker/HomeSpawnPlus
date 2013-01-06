/**
 * 
 */
package org.morganm.homespawnplus.server.api.events;

import org.morganm.homespawnplus.server.api.Block;

/**
 * @author morganm
 *
 */
public interface PlayerBedEnterEvent extends PlayerEvent {
    /**
     * Returns the bed block involved in this event.
     *
     * @return the bed block involved in this event
     */
    public Block getBed();

    /**
     * Sets the cancellation state of this event. A canceled event will not
     * be executed in the server.
     *
     * @param cancel true if you wish to cancel this event
     */
    public void setCancelled(boolean cancel);
    
    /**
     * Gets the cancellation state of this event.
     *
     * @return boolean cancellation state
     */
    public boolean isCanceled();
}
