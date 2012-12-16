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
public class PlayerBedEnterEvent extends PlayerEvent
implements org.morganm.homespawnplus.server.api.events.PlayerBedEnterEvent
{
    private org.bukkit.event.player.PlayerBedEnterEvent bukkitEvent;
    
    public PlayerBedEnterEvent(org.bukkit.event.player.PlayerBedEnterEvent bukkitEvent) {
        super(bukkitEvent);
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
