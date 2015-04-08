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
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.entity.*;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.HomeInvite;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.google.inject.Inject;

import java.util.Set;

/**
 * Command to show the personal spawn point of a given player.
 *
 * @author andune
 */
@UberCommand(uberCommand = "hsp", subCommand = "showPlayerInfo",
        aliases = {"spi"}, help = "Show info HSP has recorded about a player")
public class ShowPlayerInfo extends BaseCommand {
    private final String ALL = "all";

    @Inject
    private PlayerSpawnDAO playerSpawnDAO;
    @Inject
    private Factory factory;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"spi"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_SHOWPLAYERINFO_USAGE);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if (!defaultCommandChecks(sender))
            return true;

        if (args.length < 2) {
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

        boolean argMatched = false;
        final String arg = args[1];
        // show player name and UUID
        if (arg.equalsIgnoreCase("info") || ALL.equalsIgnoreCase(arg)) {
            argMatched = true;
            Player p = storage.getPlayerDAO().findPlayerByName(playerName);
            sender.sendMessage("Player name: " + p.getName());
            sender.sendMessage("Player UUID: "+p.getUUIDString());
            Location l = p.getLastLogoutLocation();
            sender.sendMessage("Player last logout location: "+(l != null ? l.shortLocationString() : null));
            sender.sendMessage("Player first seen: "+p.getDateCreated());

            com.andune.minecraft.commonlib.server.api.Player onlinePlayer = server.getPlayer(playerName);
            sender.sendMessage("Player current location: "+(onlinePlayer != null ?
                onlinePlayer.getLocation().shortLocationString() : "not online"));
        }

        // show player Homes
        if (arg.equalsIgnoreCase("homes") || ALL.equalsIgnoreCase(arg)) {
            argMatched = true;
            Set<? extends Home> homes = storage.getHomeDAO().findHomesByPlayer(playerName);
            if (homes != null && homes.size() > 0 ) {
                for(Home h : homes) {
                    sender.sendMessage("Player home name \""+h.getName()+"\" location = "+h.getLocation().shortLocationString());
                }
            }
            else
                sender.sendMessage("No homes found");
        }

        // show player PersonalSpawns
        if (arg.equalsIgnoreCase("spawns") || ALL.equalsIgnoreCase(arg)) {
            argMatched = true;
            Set<PlayerSpawn> spawns = storage.getPlayerSpawnDAO().findByPlayerName(playerName);
            if (spawns != null && spawns.size() > 0) {
                for(PlayerSpawn ps : spawns) {
                    if (ps.getSpawn() != null) {
                        sender.sendMessage("Player spawn for world "+ps.getWorld()+" is: "
                                +ps.getSpawn().getName()+" (spawn loc: "+ps.getSpawn().getLocation().shortLocationString()+")");
                    }
                    else {
                        sender.sendMessage("Player spawn location for world "+ps.getWorld()+" is: "+ps.getLocation().shortLocationString());
                    }
                }
            }
            else
                sender.sendMessage("No playerSpawns found");
        }

        // show player LastLocations
        if (arg.equalsIgnoreCase("ll") || ALL.equalsIgnoreCase(arg)) {
            argMatched = true;
            Set<PlayerLastLocation> lastLocations = storage.getPlayerLastLocationDAO().findByPlayerName(playerName);
            if (lastLocations != null && lastLocations.size() > 0) {
                for(PlayerLastLocation pll : lastLocations) {
                    sender.sendMessage("Player lastLocation for world "+pll.getWorld()+": "+pll.getLocation().shortLocationString());
                }
            }
            else
                sender.sendMessage("No lastLocations found");
        }

        // show player HomeInvites (owned)
        if (arg.equalsIgnoreCase("hio") || arg.equalsIgnoreCase("hi") || ALL.equalsIgnoreCase(arg)) {
            argMatched = true;
            Set<HomeInvite> his = storage.getHomeInviteDAO().findAllOpenInvites(playerName);
            if (his != null && his.size() > 0) {
                for(HomeInvite hi : his) {
                    String invitee=null;
                    if (hi.isPublic())
                        invitee="public invite";
                    else
                        invitee="to player \""+hi.getInvitedPlayer()+"\"";

                    sender.sendMessage("HomeInvite (owned) "+invitee
                            +" to home named \""+hi.getHome().getName()
                            +"\" loc="+hi.getHome().getLocation().shortLocationString()
                            +". Expires "+hi.getExpires());
                }
            }
            else
                sender.sendMessage("No homeInvites (owned) found");
        }

        // show player HomeInvites (received)
        if (arg.equalsIgnoreCase("hio") || arg.equalsIgnoreCase("hi") || ALL.equalsIgnoreCase(arg)) {
            argMatched = true;
            Set<HomeInvite> his = storage.getHomeInviteDAO().findAllAvailableInvites(playerName);
            if (his != null && his.size() > 0) {
                for(HomeInvite hi : his) {
                    String inviter=null;
                    if (hi.isPublic())
                        inviter="public invite";
                    else
                        inviter="from player "+hi.getHome().getPlayerName()+"\"";

                    sender.sendMessage("HomeInvite (invited) "+inviter
                            +" to home named \""+hi.getHome().getName()
                            +"\" loc="+hi.getHome().getLocation().shortLocationString()
                            +". Expires "+hi.getExpires());
                }
            }
            else
                sender.sendMessage("No homeInvites (invited) found");
        }

        if (!argMatched) {
            sender.sendMessage("arg "+arg+" not understood.");
            return false;
        }

        return true;
    }
}
