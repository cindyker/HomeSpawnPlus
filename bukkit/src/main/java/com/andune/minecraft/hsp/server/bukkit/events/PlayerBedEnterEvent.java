/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit.events;


import com.andune.minecraft.hsp.server.api.Block;
import com.andune.minecraft.hsp.server.bukkit.BukkitBlock;
import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;

/**
 * @author morganm
 *
 */
public class PlayerBedEnterEvent extends PlayerEvent
implements com.andune.minecraft.hsp.server.api.events.PlayerBedEnterEvent
{
    private org.bukkit.event.player.PlayerBedEnterEvent bukkitEvent;
    
    public PlayerBedEnterEvent(org.bukkit.event.player.PlayerBedEnterEvent bukkitEvent, BukkitFactory bukkitFactory) {
        super(bukkitEvent, bukkitFactory);
        this.bukkitEvent = bukkitEvent;
    }

    @Override
    public Block getBed() {
        return new BukkitBlock(bukkitEvent.getBed());
    }

    @Override
    public void setCancelled(boolean cancel) {
        bukkitEvent.setCancelled(cancel);
    }

    @Override
    public boolean isCanceled() {
        return bukkitEvent.isCancelled();
    }
}
