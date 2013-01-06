/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import org.morganm.homespawnplus.server.bukkit.BukkitFactory;

/**
 * @author morganm
 *
 */
public class PlayerKickEvent extends PlayerEvent
implements org.morganm.homespawnplus.server.api.events.PlayerKickEvent
{
    public PlayerKickEvent(org.bukkit.event.player.PlayerKickEvent bukkitEvent, BukkitFactory bukkitFactory) {
        super(bukkitEvent, bukkitFactory);
    }
}
