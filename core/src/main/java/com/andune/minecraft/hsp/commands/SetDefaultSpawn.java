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
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.util.SpawnUtil;

import javax.inject.Inject;

/**
 * @author andune
 */
@UberCommand(uberCommand = "spawn", subCommand = "setDefault",
        aliases = {"sd"}, help = "Change the default spawn for a world")
public class SetDefaultSpawn extends BaseCommand {
    @Inject
    private SpawnUtil util;

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_SETDEFAULTSPAWN_USAGE);
    }

    @Override
    public boolean execute(Player p, String[] args) throws CommandException {
        if (args.length < 1) {
            server.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPECIFY_NAME);
            return true;
        }

        try {
            Spawn spawn = storage.getSpawnDAO().findSpawnByName(args[0]);
            if (spawn != null) {
                Location l = spawn.getLocation();
                util.setDefaultWorldSpawn(l, p.getName());
                server.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPAWN_CHANGED,
                        "name", spawn.getName(), "location", l.shortLocationString());
            }
        } catch (StorageException e) {
            throw new CommandException(e);
        }

        return true;
    }
}
