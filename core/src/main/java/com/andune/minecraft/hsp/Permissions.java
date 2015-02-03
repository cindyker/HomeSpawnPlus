/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.server.api.Command;

/**
 * This interface exists so that a proxy object can easily be created for
 * instances where we want to do something special with permission checks,
 * such as make them all true when a command block is executing something
 * on behalf of the player.
 *
 * @author andune
 */
public interface Permissions {

    /**
     * Determine if a sender has a given permission.
     *
     * @param sender
     * @param perm
     * @return
     * @deprecated you should use one of the specific permission methods
     *             or add one if possible, to minimize the spread of permission concerns
     *             throughout the plugin code. This method is only set still
     *             to serve the need of config-exposed permissions, where admins can
     *             directly choose the permissions and therefore the plugin must test
     *             those permissions as-is.
     */
    boolean hasPermission(CommandSender sender, String perm);

    boolean hasCommandPermission(CommandSender sender, String command);

    /**
     * Check for custom command permission first, if there is none, check the
     * default command permission.
     *
     * @param sender
     * @param command
     * @return true if the sender has permission, false if not
     */
    boolean hasCommandPermission(CommandSender sender, Command command);

    boolean hasSetHomeNamed(Player player);

    /**
     * Determine if the player should have HSP admin privileges.
     *
     * @param player
     * @return
     */
    boolean isAdmin(CommandSender sender);

    /**
     * Determine if the player should be exempt from a given warmup.
     *
     * @param player
     * @param warmup
     * @return
     */
    boolean isWarmupExempt(Player player, String warmup);

    /**
     * Determine if the player should be exempt from a given cooldown.
     *
     * @param player
     * @param cooldown
     * @return
     */
    boolean isCooldownExempt(Player player, String cooldown);

    /**
     * Determine if the player should be exempt from a given cost.
     *
     * @param player
     * @param cooldown
     * @return
     */
    boolean isCostExempt(Player player, String cost);

    /**
     * Determine if a player has permission to specify an argument to
     * groupspawn commands, such as "/groupspawn somegroup"
     *
     * @param player
     * @return
     */
    boolean hasOtherGroupSpawnPermission(Player player);

    /**
     * Determine if the player has permission to go to named spawns, such
     * as "/spawn spawn3".
     *
     * @param player
     * @param name   optional arg, if set, is appended to the permission check
     * @return
     */
    boolean hasSpawnNamed(Player player, String name);

    /**
     * Determine if the player has permission to send out permament
     * home invites.
     *
     * @param player
     * @return
     */
    boolean hasPermanentHomeInvite(Player player);

    boolean hasHomeInviteOtherWorld(Player player);

    boolean hasHomeOtherWorld(Player player);

    boolean hasHomeNamed(Player player);

    /**
     * Determine if player has permission to set bed homes.
     *
     * @param player
     * @return
     */
    boolean hasBedSetHome(Player player);
}