/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.bukkit.BukkitFactory;

import com.google.inject.Inject;

/** Base class that represents an event about a player.
 * 
 * @author morganm
 *
 */
public abstract class PlayerEvent {
    protected final org.bukkit.event.player.PlayerEvent bukkitPlayerEvent;
    protected final BukkitFactory bukkitFactory;
    protected Player player;
    
    @Inject
    public PlayerEvent(org.bukkit.event.player.PlayerEvent bukkitPlayerEvent, BukkitFactory bukkitFactory) {
        this.bukkitPlayerEvent = bukkitPlayerEvent;
        this.bukkitFactory = bukkitFactory;
    }

    @Inject
    public PlayerEvent(org.bukkit.entity.Player player, BukkitFactory bukkitFactory) {
        this.bukkitFactory = bukkitFactory;
        this.player = bukkitFactory.newBukkitPlayer(player);
        this.bukkitPlayerEvent = null;
    }

    public Player getPlayer() {
        if( player == null )
            player = bukkitFactory.newBukkitPlayer(bukkitPlayerEvent.getPlayer());
        
        return player;
    }
}
