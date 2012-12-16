/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.bukkit.BukkitPlayer;

/** Base class that represents an event about a player.
 * 
 * @author morganm
 *
 */
public abstract class PlayerEvent {
    protected org.bukkit.event.player.PlayerEvent bukkitPlayerEvent;
    protected Player player;
    
    public PlayerEvent(org.bukkit.event.player.PlayerEvent bukkitPlayerEvent) {
        this.bukkitPlayerEvent = bukkitPlayerEvent;
    }

    public Player getPlayer() {
        if( player == null )
            player = new BukkitPlayer(bukkitPlayerEvent.getPlayer());
        
        return player;
    }
}
