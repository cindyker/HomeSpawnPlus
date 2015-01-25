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
package com.andune.minecraft.hsp.commands;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.OfflinePlayer;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.entity.PlayerSpawn;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.google.inject.Inject;

import java.util.Set;

/**
 * Command to show the personal spawn point of a given player.
 *
 * @author andune
 */
@UberCommand(uberCommand = "spawn", subCommand = "showPlayerSpawn",
        aliases = {"shps"}, help = "Show personal spawn point for a player")
public class ShowPlayerSpawn extends BaseCommand {
    @Inject
    private PlayerSpawnDAO playerSpawnDAO;
    @Inject
    private Factory factory;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"shps"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_SHOWPLAYERSPAWN_USAGE);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if (!defaultCommandChecks(sender))
            return true;

        if (args.length < 1) {
            return false;
        }

        Location location = null;
        com.andune.minecraft.hsp.entity.Spawn spawn = null;

        OfflinePlayer op = server.getOfflinePlayer(args[0]);
        if (!op.isOnline() && !op.hasPlayedBefore()) {
            server.sendLocalizedMessage(sender, HSPMessages.PLAYER_NOT_FOUND, "player", args[0]);
            return true;
        }
        final String playerName = op.getName();

        String world = null;
        if (args.length > 1)
            world = args[1];
        // make sure it's a valid world so we can print a helpful error message if it's not
        World w = server.getWorld(world);
        if (world != null && w == null) {
            server.sendLocalizedMessage(sender, HSPMessages.WORLD_NOT_FOUND, "world", world);
            return true;
        }

        if (world == null) {
            // TODO: fix listing
            Set<PlayerSpawn> set = playerSpawnDAO.findByPlayerName(playerName);

            if (set != null && set.size() > 0 ) {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_SHOWPLAYERSPAWN_PLAYER_SPAWN_LIST,
                        "player", playerName);
                for(PlayerSpawn ps : set) {
                    showPlayerSpawn(sender, ps);
                }
            }
            else {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_SHOWPLAYERSPAWN_NO_SPAWN_FOUND,
                        "player", playerName, "world", world);
            }
        }
        else {
            PlayerSpawn playerSpawn = playerSpawnDAO.findByWorldAndPlayerName(world, playerName);

            if (playerSpawn != null) {
                showPlayerSpawn(sender, playerSpawn);
            }
            else {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_SHOWPLAYERSPAWN_NO_SPAWN_FOUND,
                        "player", playerName, "world", world);
            }
        }

        return true;
    }

    private void showPlayerSpawn(CommandSender sender, PlayerSpawn playerSpawn) {
        if (playerSpawn.getSpawn() != null) {
            server.sendLocalizedMessage(sender, HSPMessages.CMD_SHOWPLAYERSPAWN_SPAWN_FOUND,
                    "world", playerSpawn.getSpawn().getWorld(),
                    "spawn", playerSpawn.getSpawn().getName(),
                    "location", playerSpawn.getSpawn().getLocation().shortLocationString());
        }
        else {
            server.sendLocalizedMessage(sender, HSPMessages.CMD_SHOWPLAYERSPAWN_LOCATION_FOUND,
                    "world", playerSpawn.getLocation().getWorld().getName(),
                    "location", playerSpawn.getLocation().shortLocationString());
        }
    }
}
