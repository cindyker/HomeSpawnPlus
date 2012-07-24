/**
 * 
 */
package org.morganm.homespawnplus.integration.worldguard;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author morganm
 *
 */
public class RegionExitEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private String regionName;
    private Player player;

    public RegionExitEvent(final String regionName, final Player player) {
    	this.regionName = regionName;
    	this.player = player;
    }
    
	@Override
	public HandlerList getHandlers() {
        return handlers;
	}
}
