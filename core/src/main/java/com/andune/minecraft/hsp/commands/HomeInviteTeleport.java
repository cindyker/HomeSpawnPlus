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

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.OfflinePlayer;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.config.ConfigHomeInvites;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.manager.WarmupRunner;
import com.andune.minecraft.hsp.storage.StorageException;

import javax.inject.Inject;
import java.util.Date;

/**
 * Cooldown, warmup and teleport logic structured similar to Home command.
 *
 * @author andune
 */
public class HomeInviteTeleport extends BaseCommand {
    @Inject
    private Teleport teleport;
    @Inject
    private ConfigHomeInvites homeInviteConfig;
    @Inject
    private ConfigCore coreConfig;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"hit", "hitp", "homeinvitetp"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_HOME_INVITE_TELEPORT_USAGE);
    }

    @Override
    public boolean execute(final Player p, String[] args) {
        if (args.length < 1) {
            return false;
        }

        Location l = null;
        com.andune.minecraft.hsp.entity.HomeInvite homeInvite = null;
        Home theHome = null;

        if (args.length == 1) {
            try {
                int id = Integer.parseInt(args[0]);
                homeInvite = storage.getHomeInviteDAO().findHomeInviteById(id);
            } catch (NumberFormatException e) {
                p.sendMessage("Error: Expected id number, got \"" + args[0] + "\"");
                return false;            // send command usage
            }
        } else if (args.length == 2) {
            // find the player
            OfflinePlayer foundPlayer = server.getBestMatchPlayer(args[0]);
            String targetPlayerName = null;
            if (foundPlayer != null)
                targetPlayerName = foundPlayer.getName();
            else
                p.sendMessage("Could not find player \"" + args[0] + "\"");

            if (targetPlayerName != null) {
                // now find the home with the name given for that player
                com.andune.minecraft.hsp.entity.Home home = storage.getHomeDAO().findHomeByNameAndPlayer(args[1], targetPlayerName);

                // now look for the HomeInvite for that home with this player as the invitee
                homeInvite = storage.getHomeInviteDAO().findInviteByHomeAndInvitee(home, p.getName());
            }
        } else {
            return false;        // send command usage
        }

        if (homeInvite != null) {
            // store home for future use in player messages
            theHome = homeInvite.getHome();

            if (theHome == null
                    || (theHome.getPlayerName() == null && theHome.getWorld() == null)) {
                server.sendLocalizedMessage(p, HSPMessages.NO_HOME_INVITE_FOUND);
                try {
                    storage.getHomeInviteDAO().deleteHomeInvite(homeInvite);
                } catch (Exception e) {
                    log.warn("Error deleting homeInvite", e);
                }
                return true;
            }

            // only allow public homes if admin has enabled them
            if (homeInvite.isPublic()) {
                if (!homeInviteConfig.allowPublicInvites())
                    homeInvite = null;
            }
            // if we're not the invited player, we can't use this invite id
            else if (!p.getName().equals(homeInvite.getInvitedPlayer()))
                homeInvite = null;

            // check for expiry of the invite
            Date expires = null;
            if (homeInvite != null)
                expires = homeInvite.getExpires();
            if (expires != null && expires.compareTo(new Date()) < 0) {
                deleteHomeInvite(homeInvite);
                homeInvite = null;
            }

            // check if it's a bedhome and we're not allowed to teleport to bedhomes
            if (!homeInviteConfig.allowBedHomeInvites() && homeInvite.getHome().isBedHome()) {
                deleteHomeInvite(homeInvite);
                homeInvite = null;
            }

            // if homeInvite is still non-null at this point, then we're allowed to use it
            if (homeInvite != null) {
                // BUG: EBEAN cascading is not working, the @OneToOne entity attached
                // to homeInvite has the id set, but not the attributes.
                log.debug("HomeInviteTeleport: home={}", homeInvite.getHome());
                l = homeInvite.getHome().getLocation();
            }
        }


        if (l != null) {
            // make sure it's on the same world, or if not, that we have cross-world home perms
            if (!p.getWorld().getName().equals(l.getWorld().getName()) && permissions.hasHomeInviteOtherWorld(p)) {
                server.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION);
                return true;
            }

            String cooldownName = getCooldownName(getCommandName(), Integer.toString(homeInvite.getId()));
            if (homeInviteConfig.useHomeCooldown())
                cooldownName = getCooldownName("home", Integer.toString(homeInvite.getId()));
            String warmupName = getCommandName();
            if (homeInviteConfig.useHomeWarmup())
                warmupName = "home";

            log.debug("homeInviteTeleport command running cooldown check, cooldownName={}", cooldownName);
            if (!cooldownCheck(p, cooldownName))
                return true;

            if (hasWarmup(p, warmupName)) {
                final Location finalL = l;
                final Home finalHome = theHome;
                final String placeString = "home of " + homeInvite.getHome().getPlayerName();
                doWarmup(p, new WarmupRunner() {
                    private boolean canceled = false;
                    private String cdName;
                    private String wuName;

                    public void run() {
                        if (!canceled) {
                            server.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
                                    "name", getWarmupName(), "place", placeString);
                            doHomeTeleport(p, finalL, cdName, finalHome);
                        }
                    }

                    public void cancel() {
                        canceled = true;
                    }

                    public void setPlayerName(String playerName) {
                    }

                    public void setWarmupId(int warmupId) {
                    }

                    public WarmupRunner setCooldownName(String cd) {
                        cdName = cd;
                        return this;
                    }

                    public WarmupRunner setWarmupName(String warmupName) {
                        wuName = warmupName;
                        return this;
                    }

                    public String getWarmupName() {
                        return wuName;
                    }
                }.setCooldownName(cooldownName).setWarmupName(warmupName));
            } else {
                doHomeTeleport(p, l, cooldownName, theHome);
            }
        } else
            server.sendLocalizedMessage(p, HSPMessages.NO_HOME_INVITE_FOUND);

        return true;
    }

    /**
     * Do a teleport to the home including costs, cooldowns and printing
     * departure and arrival messages. Is used from both warmups and sync /home.
     *
     * @param p
     * @param l
     */
    private void doHomeTeleport(Player p, Location l, String cooldownName,
                                Home home) {
        String homeName = "default";
        String playerName = "unknown";
        if (home != null) {
            if (home.getName() != null)
                homeName = home.getName();
            playerName = home.getPlayerName();
        }

        if (applyCost(p, true, cooldownName)) {
            if (coreConfig.isTeleportMessages())
                server.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_TELEPORTING,
                        "home", homeName,
                        "player", playerName);

            teleport.teleport(p, l, null);
        }
    }

    private void deleteHomeInvite(final com.andune.minecraft.hsp.entity.HomeInvite hi) {
        // it's expired, so delete it. we ignore any error here since it doesn't
        // affect the outcome of the rest of the command.
        try {
            storage.getHomeInviteDAO().deleteHomeInvite(hi);
        } catch (StorageException e) {
            log.warn("Caught exception: " + e.getMessage(), e);
        }
    }
}
