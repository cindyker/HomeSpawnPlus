/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit.events;


import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;
import com.andune.minecraft.hsp.server.bukkit.BukkitLocation;

/**
 * @author morganm
 *
 */
public class PlayerTeleportEvent extends PlayerEvent implements com.andune.minecraft.hsp.server.api.events.PlayerTeleportEvent
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
