/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.bukkit.BukkitFactory;
import org.morganm.homespawnplus.server.bukkit.BukkitLocation;

/**
 * @author morganm
 *
 */
public class PlayerTeleportEvent extends PlayerEvent implements org.morganm.homespawnplus.server.api.events.PlayerTeleportEvent
{
    private final org.bukkit.event.player.PlayerTeleportEvent bukkitEvent;
    private Location from;
    private Location to;
    
    public PlayerTeleportEvent(org.bukkit.event.player.PlayerTeleportEvent bukkitEvent, BukkitFactory bukkitFactory) {
        super(bukkitEvent, bukkitFactory);
        this.bukkitEvent = bukkitEvent;
    }

    @Override
    public Location getFrom() {
        if( from == null )
            from = new BukkitLocation(bukkitEvent.getFrom());
        return from;
    }

    @Override
    public Location getTo() {
        if( to == null )
            to = new BukkitLocation(bukkitEvent.getTo());
        return to;
    }

    @Override
    public void setTo(Location to) {
        org.bukkit.Location bukkitLocation = ((BukkitLocation) to).getBukkitLocation();
        bukkitEvent.setTo(bukkitLocation);
    }
}
