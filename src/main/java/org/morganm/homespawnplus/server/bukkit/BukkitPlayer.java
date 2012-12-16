/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.World;

/**
 * Bukkit implementation of Player API.
 * 
 * @author morganm
 *
 */
public class BukkitPlayer implements Player {
    private org.bukkit.entity.Player bukkitPlayer;
    
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

    @Override
    public void sendMessage(String message) {
        // TODO: add default color
        bukkitPlayer.sendMessage(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        // TODO: add default color
        bukkitPlayer.sendMessage(messages);
    }

    @Override
    public World getWorld() {
        return new BukkitWorld(bukkitPlayer.getWorld());
    }

    @Override
    public void setBedSpawnLocation(Location location) {
        // if BukkitPlayer is in use, it's because we're running on a Bukkit Server so
        // we can safely assume the incoming object is a BukkitLocation
        bukkitPlayer.setBedSpawnLocation( ((BukkitLocation) location).getBukkitLocation() );
    }

    @Override
    public void teleport(Location location) {
        // if BukkitPlayer is in use, it's because we're running on a Bukkit Server so
        // we can safely assume the incoming object is a BukkitLocation
        bukkitPlayer.teleport( ((BukkitLocation) location).getBukkitLocation() );
    }
    
    public boolean equals(Object o) {
        if( o == null )
            return false;
        if( !(o instanceof Player) )
            return false;
        String name = ((Player) o).getName();
        return getName().equals(name);
    }

    @Override
    public boolean isSneaking() {
        return bukkitPlayer.isSneaking();
    }
}
