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
public class PlayerBedRightClickEvent extends PlayerEvent
implements com.andune.minecraft.hsp.server.api.events.PlayerBedRightClickEvent
{
    private org.bukkit.event.player.PlayerInteractEvent bukkitEvent;
    
    public PlayerBedRightClickEvent(org.bukkit.event.player.PlayerInteractEvent bukkitEvent, BukkitFactory bukkitFactory) {
        super(bukkitEvent, bukkitFactory);
        this.bukkitEvent = bukkitEvent;
    }

    @Override
    public Block getClickedBlock() {
        return new BukkitBlock(bukkitEvent.getClickedBlock());
    }

    @Override
    public void setCancelled(boolean cancel) {
        bukkitEvent.setCancelled(true);
    }

    @Override
    public boolean isCanceled() {
        return bukkitEvent.isCancelled();
    }

}
