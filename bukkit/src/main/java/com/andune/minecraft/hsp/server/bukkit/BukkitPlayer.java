/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.server.api.World;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;

/**
 * Bukkit implementation of Player API.
 * 
 * @author andune
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

    @Override
    public boolean isOnline() {
        return bukkitPlayer.isOnline();
    }
}
