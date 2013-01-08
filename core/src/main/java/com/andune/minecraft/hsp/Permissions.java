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


import com.andune.minecraft.hsp.command.Command;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.PermissionSystem;
import com.andune.minecraft.hsp.server.api.Player;


/** All HSP Permissions are defined here.
 * 
 * @author andune
 *
 */
@Singleton
public class Permissions {
    /**
     * The base permission prefix - all HSP permissions start with this prefix. 
     */
    private static final String PERM_PREFIX = "hsp.";
    
    private final PermissionSystem permSystem;
    private final ConfigCore configCore;
    
    @Inject
    public Permissions(PermissionSystem permSystem, ConfigCore configCore) {
        this.permSystem = permSystem;
        this.configCore = configCore;
    }
    
    /**
     * Determine if a sender has a given permission.
     * 
     * @deprecated you should use one of the specific permission methods
     * or add one if possible, to minimize the spread of permission concerns
     * throughout the plugin code. This method is only set public still
     * to serve the need of config-exposed permissions, where admins can
     * directly choose the permissions and therefore the plugin must test
     * those permissions as-is.
     * 
     * @param sender
     * @param perm
     * @return
     */
    public boolean hasPermission(CommandSender sender, String perm) {
        boolean result = permSystem.has(sender, perm);

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

    public boolean hasCommandPermission(CommandSender sender, String command) {
        return permCheck(sender,  "command." + command);
    }
    
    /**
     * Check for custom command permission first, if there is none, check the
     * default command permission.
     * 
     * @param sender
     * @param command
     * @return true if the sender has permission, false if not
     */
    public boolean hasCommandPermission(CommandSender sender, Command command) {
        String customPerm = command.getCommandPermissionNode();
        if( customPerm != null )
            return permCheck(sender, customPerm);
        else 
            return hasCommandPermission(sender,  command.getCommandName());
    }
    
    public boolean hasSetHomeNamed(Player player) {
        return permCheck(player, "command.sethome.named");
    }
    
    /**
     * Determine if the player should have HSP admin privileges.
     * 
     * @param player
     * @return
     */
    public boolean isAdmin(CommandSender sender) {
        return permCheck(sender, "admin");
    }
    
    /**
     * Determine if the player should be exempt from a given warmup.
     * 
     * @param player
     * @param warmup
     * @return
     */
    public boolean isWarmupExempt(Player player, String warmup) {
        return permCheck(player, "WarmupExempt."+warmup);
    }
    
    /**
     * Determine if the player should be exempt from a given cooldown.
     * 
     * @param player
     * @param cooldown
     * @return
     */
    public boolean isCooldownExempt(Player player, String cooldown) {
        return permCheck(player, "CooldownExempt."+cooldown);
    }

    /**
     * Determine if the player should be exempt from a given cost.
     * 
     * @param player
     * @param cooldown
     * @return
     */
    public boolean isCostExempt(Player player, String cost) {
        return permCheck(player, "CostExempt."+cost);
    }

    /**
     * Determine if a player has permission to specify an argument to 
     * groupspawn commands, such as "/groupspawn somegroup" 
     * 
     * @param player
     * @return
     */
    public boolean hasOtherGroupSpawnPermission(Player player) {
        return hasCommandPermission(player, "groupspawn.named");
    }
    
    /**
     * Determine if the player has permission to go to named spawns, such
     * as "/spawn spawn3".
     * 
     * @param player
     * @param name optional arg, if set, is appended to the permission check
     * @return
     */
    public boolean hasSpawnNamed(Player player, String name) {
        if( name != null )
            return hasCommandPermission(player, "spawn.named."+name);
        else
            return hasCommandPermission(player, "spawn.named");
    }
    
    /**
     * Determine if the player has permission to send out permament
     * home invites.
     * 
     * @param player
     * @return
     */
    public boolean hasPermanentHomeInvite(Player player) {
        return hasCommandPermission(player, "homeinvite.permanent");
    }
    
    public boolean hasHomeInviteOtherWorld(Player player) {
        return hasCommandPermission(player, "homeinvitetp.otherworld");
    }
    
    public boolean hasHomeOtherWorld(Player player) {
        return hasCommandPermission(player, "home.otherworld");
    }
    
    public boolean hasHomeNamed(Player player) {
        return hasCommandPermission(player, "home.named");
    }
    
    /**
     * Determine if player has permission to set bed homes.
     * 
     * @param player
     * @return
     */
    public boolean hasBedSetHome(Player player) {
        return permCheck(player, "home.bedsethome");
    }
}
