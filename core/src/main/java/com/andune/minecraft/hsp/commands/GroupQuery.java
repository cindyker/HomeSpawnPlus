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
package com.andune.minecraft.hsp.commands;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.OfflinePlayer;
import com.andune.minecraft.commonlib.server.api.PermissionSystem;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.util.SpawnUtil;

import javax.inject.Inject;

/**
 * Command to return the group HSP thinks a player is in, based on the underlying
 * Permission system in use.
 *
 * @author andune
 */
@UberCommand(uberCommand = "hsp", subCommand = "groupQuery",
        aliases = {"gq"}, help = "Query player primary group name")
public class GroupQuery extends BaseCommand {
    @Inject
    private PermissionSystem permSystem;
    @Inject
    private SpawnUtil util;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"gq"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_GROUPQUERY_USAGE);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if (!defaultCommandChecks(sender))
            return true;

        String playerName = null;
        String playerWorld = null;
        boolean playerOffline = false;

        if (args.length > 0) {
            OfflinePlayer offline = server.getBestMatchPlayer(args[0]);
            playerName = offline.getName();

            if (args.length > 1)
                playerWorld = args[1];
            else if (offline instanceof Player)
                playerWorld = ((Player) offline).getWorld().getName();
            else {
                // no way to get the world of an offline player, so we have
                // to just assume the default world
                playerWorld = util.getDefaultWorld();
            }

            // didn't find any player by that name, error out
            if (playerName == null) {
                sender.sendMessage("Player " + args[0] + " not found.");
                return true;
            }
        } else if (sender instanceof Player) {
            Player p = (Player) sender;
            playerName = p.getName();
            playerWorld = p.getWorld().getName();
        }

        if (playerName == null)
            return false;

        String group = permSystem.getPlayerGroup(playerWorld, playerName);
        sender.sendMessage("Player " + playerName + " is in group \"" + group + "\" on " + playerWorld
                + (playerOffline ? " [player offline]" : "")
                + " (using perms " + permSystem.getSystemInUse() + ")");
        return true;
    }
}
