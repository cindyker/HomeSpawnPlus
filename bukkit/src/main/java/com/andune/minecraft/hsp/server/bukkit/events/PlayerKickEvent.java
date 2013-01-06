/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit.events;

import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;

/**
 * @author morganm
 *
 */
public class PlayerKickEvent extends PlayerEvent
implements com.andune.minecraft.hsp.server.api.events.PlayerKickEvent
{
    public PlayerKickEvent(org.bukkit.event.player.PlayerKickEvent bukkitEvent, BukkitFactory bukkitFactory) {
        super(bukkitEvent, bukkitFactory);
    }
}
