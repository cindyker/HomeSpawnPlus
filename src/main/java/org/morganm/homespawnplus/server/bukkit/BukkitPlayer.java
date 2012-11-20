/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;

/**
 * Bukkit implementation of Player API.
 * 
 * @author morganm
 *
 */
public class BukkitPlayer implements Player {
    private org.bukkit.entity.Player bukkitPlayer;
    
    @Inject
    public BukkitPlayer(org.bukkit.entity.Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }
    
    /**
     *  Return the Bukkit Player object represented by this object.
     *  
     * @return
     */
    public org.bukkit.entity.Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    @Override
    public boolean isNewPlayer() {
        // TODO: upgrade to HSP algorithm
        return !bukkitPlayer.hasPlayedBefore();
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    @Override
    public Location getLocation() {
        return new BukkitLocation(bukkitPlayer.getLocation());
    }

    @Override
    public boolean hasPermission(String permission) {
        // TODO: upgrade to HSP permission system
        return bukkitPlayer.hasPermission(permission);
    }

    @Override
    public Location getBedSpawnLocation() {
        return new BukkitLocation(bukkitPlayer.getBedSpawnLocation());
    }
}
