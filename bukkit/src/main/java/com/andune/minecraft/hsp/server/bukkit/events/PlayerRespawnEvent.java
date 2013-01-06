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
public class PlayerRespawnEvent extends PlayerEvent
implements com.andune.minecraft.hsp.server.api.events.PlayerRespawnEvent
{
    private org.bukkit.event.player.PlayerRespawnEvent event;

    public PlayerRespawnEvent(org.bukkit.event.player.PlayerRespawnEvent event, BukkitFactory bukkitFactory) {
        super(event, bukkitFactory);
        this.event = event;
    }

    @Override
    public void setRespawnLocation(Location respawnLocation) {
        event.setRespawnLocation(new BukkitLocation(respawnLocation).getBukkitLocation());
    }

    @Override
    public boolean isBedSpawn() {
        return event.isBedSpawn();
    }

    @Override
    public Location getRespawnLocation() {
        return new BukkitLocation(event.getRespawnLocation());
    }
}
