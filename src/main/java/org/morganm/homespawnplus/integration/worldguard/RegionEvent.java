/**
 * 
 */
package org.morganm.homespawnplus.integration.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author morganm
 *
 */
public abstract class RegionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private String regionName;
    private String regionWorldName;
    private Player player;
    private Location to;

    /**
     * 
     * @param regionName the name of the region
     * @param world the world the region belongs to
     * @param player the player involved in this event
     * @param to the location the player is moving to
     */
    public RegionEvent(final String regionName, final String regionWorldName, final Player player, final Location to) {
    	this.regionName = regionName;
    	this.regionWorldName = regionWorldName;
    	this.player = player;
    	this.to = to;
    }
    
    public Player getPlayer() { return player; }
    public String getRegionName() { return regionName; }
    public String getRegionWorldName() { return regionWorldName; }
    public Location getTo() { return to; }
    public void setTo(Location to) { this.to = to; }
    
	@Override
	public HandlerList getHandlers() {
        return handlers;
	}
}
