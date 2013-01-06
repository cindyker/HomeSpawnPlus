/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import org.morganm.homespawnplus.server.bukkit.BukkitFactory;

/**
 * @author morganm
 *
 */
public class PlayerDamageEvent extends PlayerEvent 
implements org.morganm.homespawnplus.server.api.events.PlayerDamageEvent
{
    public PlayerDamageEvent(org.bukkit.entity.Player player, BukkitFactory bukkitFactory) {
        super(player, bukkitFactory);
    }
}
