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
/**
 *
 */
package com.andune.minecraft.hsp.commands;

import com.andune.minecraft.commonlib.i18n.ChatColor;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.server.api.ServerConfig;
import com.andune.minecraft.hsp.storage.Storage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author andune
 */
@UberCommand(uberCommand = "spawn", subCommand = "list", aliases = {"l"}, help = "List all spawns")
public class SpawnList extends BaseCommand {
    @Inject
    private ServerConfig serverConfig;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"spawnl", "listspawns", "spawns"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_SPAWNLIST_USAGE);
    }

    @Override
    public boolean execute(CommandSender p, String cmd, String[] originalArgs) {
        if (!defaultCommandChecks(p))
            return true;

        boolean showMapSpawn = false;
        List<String> args = new ArrayList<String>(originalArgs.length);

        String world = "all";
        if (originalArgs.length > 0) {
            for (int i = 0; i < originalArgs.length; i++) {
                if (originalArgs[i].equals("-m")) {
                    showMapSpawn = true;
                } else
                    args.add(originalArgs[i]);
            }

            if (args.size() > 0)
                world = args.get(0);
        }

        final Set<? extends com.andune.minecraft.hsp.entity.Spawn> spawns = storage.getSpawnDAO().findAllSpawns();

        boolean displayedSpawn = false;
        if (spawns != null && spawns.size() > 0) {
            if (world.equals("all") || world.equals("*")) {
                world = "all";
                server.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNLIST_ALL_WORLDS);
            } else
                server.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNLIST_FOR_WORLD,
                        "world", world);

            if (showMapSpawn) {
                List<World> worlds = null;
                if (world.equals("all")) {
                    worlds = server.getWorlds();
                } else {
                    worlds = new ArrayList<World>();
                    World w = server.getWorld(world);
                    if (w != null)
                        worlds.add(w);
                }

                for (World w : worlds) {
                    Location l = w.getSpawnLocation();
                    p.sendMessage(serverConfig.getDefaultColor() + "id: " + ChatColor.RED + "none " + serverConfig.getDefaultColor()
                            + l.shortLocationString()
                            + ChatColor.GREEN + " (map spawn)"
                    );
                }
            }

            for (com.andune.minecraft.hsp.entity.Spawn spawn : spawns) {
                if (!world.equals("all") && !world.equals(spawn.getWorld()))
                    continue;

                displayedSpawn = true;

                String group = spawn.getGroup();
                if (Storage.HSP_WORLD_SPAWN_GROUP.equals(group))
                    group = null;
                String name = spawn.getName();

                p.sendMessage(serverConfig.getDefaultColor() + "id: " + ChatColor.RED + spawn.getId() + " " + serverConfig.getDefaultColor()
                        + (name != null ? "[" + server.getLocalizedMessage(HSPMessages.GENERIC_NAME) + ": " + ChatColor.RED + name + serverConfig.getDefaultColor() + "] " : "")
                        + (group != null ? "[" + server.getLocalizedMessage(HSPMessages.GENERIC_GROUP) + ": " + ChatColor.RED + group + serverConfig.getDefaultColor() + "] " : "")
                        + spawn.getLocation().shortLocationString()
                        + (Storage.HSP_WORLD_SPAWN_GROUP.equals(spawn.getGroup())
                        ? ChatColor.GREEN + " (" + server.getLocalizedMessage(HSPMessages.GENERIC_WORLD_DEFAULT) + ")"
                        : ""));
            }
        }

        if (!displayedSpawn)
            server.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNLIST_NO_SPAWNS_FOUND,
                    "world", world);

        return true;
    }

}
