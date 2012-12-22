/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import org.morganm.homespawnplus.server.bukkit.BukkitFactory;

/**
 * @author morganm
 *
 */
public class PlayerQuitEvent extends PlayerEvent
implements org.morganm.homespawnplus.server.api.events.PlayerQuitEvent
{
    public PlayerQuitEvent(org.bukkit.event.player.PlayerQuitEvent bukkitEvent, BukkitFactory bukkitFactory) {
        super(bukkitEvent, bukkitFactory);
    }
}
