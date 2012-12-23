/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.morganm.homespawnplus.Permissions;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.World;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bukkit implementation of Player API.
 * 
 * @author morganm
 *
 */
public class BukkitPlayer implements Player {
    private static final Logger log = LoggerFactory.getLogger(BukkitPlayer.class);
    
    private ConfigCore configCore;
    private PlayerDAO playerDAO;
    private org.bukkit.entity.Player bukkitPlayer;
    private Permissions perm;
    
    /** Package private, should only be invoked from BukkitFactory.
     * 
     * @param configCore
     * @param playerDAO
     * @param bukkitPlayer
     */
    BukkitPlayer(ConfigCore configCore, PlayerDAO playerDAO, Permissions perm, org.bukkit.entity.Player bukkitPlayer) {
        this.configCore = configCore;
        this.playerDAO = playerDAO;
        this.perm = perm;
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
        boolean isNewPlayer = false;
        
        ConfigCore.NewPlayerStrategy newPlayerStrategy = configCore.getNewPlayerStrategy();
        switch(newPlayerStrategy) {
            case BUKKIT:
                isNewPlayer = !bukkitPlayer.hasPlayedBefore(); 
                break;
                
            case ORIGINAL:
                if( playerDAO.findPlayerByName(getName()) == null ) {
                    isNewPlayer = true;
                    break;
                }
                // ORIGINAL FALLS THORUGH TO PLAYER_DAT
                
            case PLAYER_DAT:
            default:
                File worldContainer = Bukkit.getWorldContainer();

                final List<org.bukkit.World> worlds = Bukkit.getWorlds();
                final String worldName = worlds.get(0).getName();
                final String playerDat = getName() + ".dat";

                File file = new File(worldContainer, worldName+"/players/"+playerDat);
                if( !file.exists() ) {
                    isNewPlayer = true;
                }
        }

        log.debug("isNewPlayer: using strategy {}, isNewPlayer={}", newPlayerStrategy, isNewPlayer);
        return isNewPlayer;
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    @Override
    public Location getLocation() {
        return new BukkitLocation(bukkitPlayer.getLocation());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasPermission(String permission) {
        return perm.hasPermission(this, permission);
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
