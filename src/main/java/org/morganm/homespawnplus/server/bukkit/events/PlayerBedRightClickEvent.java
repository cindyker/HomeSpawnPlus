/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import org.morganm.homespawnplus.server.api.Block;
import org.morganm.homespawnplus.server.bukkit.BukkitBlock;

/**
 * @author morganm
 *
 */
public class PlayerBedRightClickEvent extends PlayerEvent
implements org.morganm.homespawnplus.server.api.events.PlayerBedRightClickEvent
{
    private org.bukkit.event.player.PlayerInteractEvent bukkitEvent;
    
    public PlayerBedRightClickEvent(org.bukkit.event.player.PlayerInteractEvent bukkitEvent) {
        super(bukkitEvent);
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
