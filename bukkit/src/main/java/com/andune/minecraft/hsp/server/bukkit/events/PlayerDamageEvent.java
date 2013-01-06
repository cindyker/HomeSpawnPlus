/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit.events;

import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;

/**
 * @author morganm
 *
 */
public class PlayerDamageEvent extends PlayerEvent 
implements com.andune.minecraft.hsp.server.api.events.PlayerDamageEvent
{
    public PlayerDamageEvent(org.bukkit.entity.Player player, BukkitFactory bukkitFactory) {
        super(player, bukkitFactory);
    }
}
