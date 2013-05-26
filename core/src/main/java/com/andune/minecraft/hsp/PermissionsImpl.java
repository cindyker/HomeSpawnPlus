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
package com.andune.minecraft.hsp;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;


import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.PermissionSystem;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.Command;


/** All HSP Permissions are defined here.
 * 
 * @author andune
 *
 */
@Singleton
public class PermissionsImpl implements Permissions {
    private static final Logger log = LoggerFactory.getLogger(PermissionsImpl.class);
    /**
     * The base permission prefix - all HSP permissions start with this prefix. 
     */
    private static final String PERM_PREFIX = "hsp.";
    
    private final PermissionSystem permSystem;
    private final ConfigCore configCore;
    
    @Inject
    public PermissionsImpl(PermissionSystem permSystem, ConfigCore configCore) {
        this.permSystem = permSystem;
        this.configCore = configCore;
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasPermission(com.andune.minecraft.commonlib.server.api.CommandSender, java.lang.String)
     */
    @Override
    public boolean hasPermission(CommandSender sender, String perm) {
        boolean result = permSystem.has(sender, perm);
        log.debug("hasPermission: sender={}, perm={}, result={}", sender, perm, result);

        // support legacy HSP "defaultPermissions" setting
        if( !result ) {
            List<String> defaultPerms = configCore.getDefaultPermissions();
            if( defaultPerms != null && defaultPerms.contains(perm) )
                result = true;
        }

        return result;
    }

    /**
     * Prepend the PREFIX and check if a player has a given permission node. 
     * 
     * @param sender the player to check
     * @param perm the permission to check (PREFIX is automatically prepended)
     * 
     * @return true if the player has the permission
     */
    private boolean permCheck(CommandSender sender, String perm) {
        return hasPermission(sender, PERM_PREFIX+ perm);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasCommandPermission(com.andune.minecraft.commonlib.server.api.CommandSender, java.lang.String)
     */
    @Override
    public boolean hasCommandPermission(CommandSender sender, String command) {
        return permCheck(sender,  "command." + command);
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasCommandPermission(com.andune.minecraft.commonlib.server.api.CommandSender, com.andune.minecraft.hsp.command.Command)
     */
    @Override
    public boolean hasCommandPermission(CommandSender sender, Command command) {
        String customPerm = command.getCommandPermissionNode();
        if( customPerm != null )
            return permCheck(sender, customPerm);
        else 
            return hasCommandPermission(sender,  command.getCommandName());
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasSetHomeNamed(com.andune.minecraft.commonlib.server.api.Player)
     */
    @Override
    public boolean hasSetHomeNamed(Player player) {
        return permCheck(player, "command.sethome.named");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#isAdmin(com.andune.minecraft.commonlib.server.api.CommandSender)
     */
    @Override
    public boolean isAdmin(CommandSender sender) {
        return permCheck(sender, "admin");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#isWarmupExempt(com.andune.minecraft.commonlib.server.api.Player, java.lang.String)
     */
    @Override
    public boolean isWarmupExempt(Player player, String warmup) {
        return permCheck(player, "WarmupExempt."+warmup);
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#isCooldownExempt(com.andune.minecraft.commonlib.server.api.Player, java.lang.String)
     */
    @Override
    public boolean isCooldownExempt(Player player, String cooldown) {
        return permCheck(player, "CooldownExempt."+cooldown);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#isCostExempt(com.andune.minecraft.commonlib.server.api.Player, java.lang.String)
     */
    @Override
    public boolean isCostExempt(Player player, String cost) {
        return permCheck(player, "CostExempt."+cost);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasOtherGroupSpawnPermission(com.andune.minecraft.commonlib.server.api.Player)
     */
    @Override
    public boolean hasOtherGroupSpawnPermission(Player player) {
        return hasCommandPermission(player, "groupspawn.named");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasSpawnNamed(com.andune.minecraft.commonlib.server.api.Player, java.lang.String)
     */
    @Override
    public boolean hasSpawnNamed(Player player, String name) {
        if( name != null )
            return hasCommandPermission(player, "spawn.named."+name);
        else
            return hasCommandPermission(player, "spawn.named");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasPermanentHomeInvite(com.andune.minecraft.commonlib.server.api.Player)
     */
    @Override
    public boolean hasPermanentHomeInvite(Player player) {
        return hasCommandPermission(player, "homeinvite.permanent");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasHomeInviteOtherWorld(com.andune.minecraft.commonlib.server.api.Player)
     */
    @Override
    public boolean hasHomeInviteOtherWorld(Player player) {
        return hasCommandPermission(player, "homeinvitetp.otherworld");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasHomeOtherWorld(com.andune.minecraft.commonlib.server.api.Player)
     */
    @Override
    public boolean hasHomeOtherWorld(Player player) {
        return hasCommandPermission(player, "home.otherworld");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasHomeNamed(com.andune.minecraft.commonlib.server.api.Player)
     */
    @Override
    public boolean hasHomeNamed(Player player) {
        return hasCommandPermission(player, "home.named");
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.Permissions#hasBedSetHome(com.andune.minecraft.commonlib.server.api.Player)
     */
    @Override
    public boolean hasBedSetHome(Player player) {
        return permCheck(player, "home.bedsethome");
    }
}
