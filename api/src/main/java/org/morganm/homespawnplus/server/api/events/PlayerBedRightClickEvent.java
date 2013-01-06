/**
 * 
 */
package org.morganm.homespawnplus.server.api.events;

import org.morganm.homespawnplus.server.api.Block;

/**
 * @author morganm
 *
 */
public interface PlayerBedRightClickEvent extends PlayerEvent {
    /**
     * Returns the clicked block
     *
     * @return Block returns the block clicked with this item.
     */
    public Block getClickedBlock();
    
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
