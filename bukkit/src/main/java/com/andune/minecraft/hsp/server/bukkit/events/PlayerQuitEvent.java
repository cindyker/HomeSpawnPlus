/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit.events;

import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;

/**
 * @author morganm
 *
 */
public class PlayerQuitEvent extends PlayerEvent
implements com.andune.minecraft.hsp.server.api.events.PlayerQuitEvent
{
    public PlayerQuitEvent(org.bukkit.event.player.PlayerQuitEvent bukkitEvent, BukkitFactory bukkitFactory) {
        super(bukkitEvent, bukkitFactory);
    }
}
