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

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.PermissionSystem;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;

/**
 * Bukkit implementation of Player API.
 * 
 * @author andune
 *
 */
public class BukkitPlayer extends com.andune.minecraft.commonlib.server.bukkit.BukkitPlayer {
    private static final Logger log = LoggerFactory.getLogger(BukkitPlayer.class);
    
    private ConfigCore configCore;
    private PlayerDAO playerDAO;
    private Server server;
    
    /** Protected constructor, should only be invoked from BukkitFactory.
     * 
     * @param configCore
     * @param playerDAO
     * @param bukkitPlayer
     */
    protected BukkitPlayer(ConfigCore configCore, PlayerDAO playerDAO,
            PermissionSystem perm, org.bukkit.entity.Player bukkitPlayer,
            Server server) {
        super(perm, bukkitPlayer);
        this.configCore = configCore;
        this.playerDAO = playerDAO;
        this.server = server;
    }
    
    @Override
    public void sendMessage(String message) {
        super.sendMessage(server.getDefaultColor() + message);
    }
    @Override
    public void sendMessage(String[] messages) {
        if( messages != null && messages.length > 0 )
            messages[0] = server.getDefaultColor() + messages[0];
        super.sendMessage(messages);
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
}
