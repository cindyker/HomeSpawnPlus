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

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.command.CommandException;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.util.SpawnUtil;

import javax.inject.Inject;

/**
 * @author andune
 */
@UberCommand(uberCommand = "spawn", subCommand = "set", help = "Set the spawn for a world")
public class SetSpawn extends BaseCommand {
    @Inject
    private ConfigCore config;
    @Inject
    private SpawnUtil util;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"setglobalspawn"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_SETSPAWN_USAGE);
    }

    @Override
    public boolean execute(Player p, String[] args) throws CommandException {
        try {
            if (args.length > 0) {
                util.setNamedSpawn(args[0], p.getLocation(), p.getName());
                p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_SETSPAWN_SET_NAMED_SUCCESS, "name", args[0]));
            }
            else {
                util.setDefaultWorldSpawn(p.getLocation(), p.getName());
                p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_SETSPAWN_SET_SUCCESS));

                // also set map spawn if configured to do so
                if (config.isOverrideWorld()) {
                    final Location l = p.getLocation();
                    l.getWorld().setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_SETSPAWN_SET_SUCCESS));
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_SETMAPSPAWN_SET_SUCCESS,
                            "world", l.getWorld().getName(), "location", l.shortLocationString()));
                }
            }
        } catch (StorageException e) {
            throw new CommandException(e);
        }

        return true;
    }
}
