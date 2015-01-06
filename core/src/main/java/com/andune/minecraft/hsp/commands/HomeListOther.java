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

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.OfflinePlayer;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;

import javax.inject.Inject;

/**
 * @author andune
 */
@UberCommand(uberCommand = "home", subCommand = "listOther",
        aliases = {"lo"}, help = "List another player's homes")
public class HomeListOther extends BaseCommand {
    @Inject
    private HomeList homeListCommand;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"hlo"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_HOMELISTOTHER_USAGE);
    }

    @Override
    public boolean execute(CommandSender p, String cmd, String[] args) {
        if (!defaultCommandChecks(p))
            return true;

        String player = null;
        String world = "all";

        if (args.length < 1) {
            return false;
        }

        // try player name best match
        final OfflinePlayer otherPlayer = server.getBestMatchPlayer(args[0]);
        if (otherPlayer != null)
            player = otherPlayer.getName();
        else {
            server.sendLocalizedMessage(p, HSPMessages.GENERIC_PLAYER_NOT_FOUND, "player", args[0]);
            return true;
        }

        if (args.length > 1)
            world = args[1];

        return homeListCommand.executeCommand(p, player, world);
    }

}
