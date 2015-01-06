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
import com.andune.minecraft.hsp.command.CommandException;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.entity.*;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.google.inject.Inject;

/**
 * Command to set the personal spawn point of a player, which can
 * later be used by the "spawnLocalPlayerSpawn" strategy.
 *
 * @author andune
 */
@UberCommand(uberCommand = "spawn", subCommand = "setPlayerSpawn",
        aliases = {"sps"}, help = "Set personal spawn point for a player")
public class SetPlayerSpawn extends BaseCommand {
    @Inject
    private PlayerSpawnDAO playerSpawnDAO;
    @Inject
    private Factory factory;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"sps"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_SETPLAYERSPAWN_USAGE);
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

        // if only one argument, then we are setting to current player location
        if (args.length == 1) {
            if (sender instanceof Player) {
                location = ((Player) sender).getLocation();
            }
            else {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_CONSOLE_ERROR);
                return true;
            }
        }
        else {
            // look for "player spawn" syntax
            if (args.length == 2) {
                SpawnDAO dao = storage.getSpawnDAO();

                spawn = dao.findSpawnByName(args[1]);

                if (spawn==null) {
                    // parse out "id:" prefix if present
                    if (args[1].startsWith("id:")) {
                        args[1] = args[1].substring(3);
                    }
                    int id = -1;
                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                    }
                    if (id != -1)
                        spawn = dao.findSpawnById(id);
                }

                if (spawn == null) {
                    server.sendLocalizedMessage(sender, HSPMessages.CMD_SPAWN_NO_SPAWN_FOUND, "name", args[1]);
                    return true;
                }
            }
            // look for "player world x y z" and "player x y z" syntax
            else if (args.length >= 4) {
                String worldName = null;
                Integer x = null;
                Integer y = null;
                Integer z = null;

                int position = 3;
                // 4 args is "player x y z" syntax so world is assumed to be current world
                if (args.length == 4) {
                    if (sender instanceof Player) {
                        worldName = ((Player) sender).getWorld().getName();
                    }
                    else {
                        server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_CONSOLE_ERROR);
                        return true;
                    }
                }
                else {
                    position = 4;
                    World w = server.getWorld(args[1]);
                    if (w == null) {
                        server.sendLocalizedMessage(sender, HSPMessages.WORLD_NOT_FOUND, "world", args[1]);
                        return true;
                    }
                    worldName = w.getName();
                }

                try {
                    z = Integer.parseInt(args[position]);
                } catch (NumberFormatException e) {
                    server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_BADXYZ, "xyz", "Z", "value", args[position]);
                    return true;
                }

                try {
                    y = Integer.parseInt(args[--position]);
                } catch (NumberFormatException e) {
                    server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_BADXYZ, "xyz", "Y", "value", args[position]);
                    return true;
                }

                try {
                    x = Integer.parseInt(args[--position]);
                } catch (NumberFormatException e) {
                    server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_BADXYZ, "xyz", "X", "value", args[position]);
                    return true;
                }

                location = factory.newLocation(worldName, x, y, z, 0, 0);
            }
        }

        // if we didn't find anything valid, return usage error
        if (location == null && spawn == null) {
            server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_USAGE, "world", args[1]);
            return true;
        }

        // If we get here, we have a valid playerName and either a spawn or location to set
        PlayerSpawn playerSpawn = null;
        if (spawn != null) {
            playerSpawn = playerSpawnDAO.findByWorldAndPlayerName(spawn.getWorld(), playerName);
            if (playerSpawn == null) {
                playerSpawn = new PlayerSpawn();
                playerSpawn.setPlayerName(playerName);
            }
            playerSpawn.setSpawn(spawn);
        }
        // if no spawn is set then we use the location
        else {
            playerSpawn = playerSpawnDAO.findByWorldAndPlayerName(location.getWorld().getName(), playerName);
            if (playerSpawn == null) {
                playerSpawn = new PlayerSpawn();
                playerSpawn.setPlayerName(playerName);
            }

            // be sure to clear out spawn so the new location is used instead
            playerSpawn.setSpawn(null);
            playerSpawn.setLocation(location);
        }

        try {
            playerSpawnDAO.save(playerSpawn);
        } catch (StorageException e) {
            server.sendLocalizedMessage(sender, HSPMessages.GENERIC_ERROR);
            log.warn("Error caught in /" + getCommandName(), e);
        }

        if (spawn != null) {
            server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_SUCCESS_SPAWN,
                    "player", playerName, "spawn", spawn.getName());
        }
        else {
            server.sendLocalizedMessage(sender, HSPMessages.CMD_SETPLAYERSPAWN_SUCCESS_LOCATION,
                    "player", playerName, "location", location.shortLocationString());
        }
        return true;
    }

    private PlayerSpawn findOrCreatePlayerSpawn(String world, String player) {
        PlayerSpawn playerSpawn = playerSpawnDAO.findByWorldAndPlayerName(world, player);
        if (playerSpawn == null) {
            playerSpawn = new PlayerSpawn();
            playerSpawn.setPlayerName(player);
        }
        return playerSpawn;
    }
}
