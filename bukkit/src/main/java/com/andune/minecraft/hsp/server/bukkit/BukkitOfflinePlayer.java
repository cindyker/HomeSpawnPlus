/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import com.andune.minecraft.hsp.server.api.OfflinePlayer;

/**
 * @author morganm
 *
 */
public class BukkitOfflinePlayer implements OfflinePlayer {
    private final org.bukkit.OfflinePlayer bukkitOfflinePlayer;
    
    public BukkitOfflinePlayer(org.bukkit.OfflinePlayer bukkitOfflinePlayer) {
        this.bukkitOfflinePlayer = bukkitOfflinePlayer;
    }

    @Override
    public String getName() {
        return bukkitOfflinePlayer.getName();
    }
}
