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

import com.andune.minecraft.commonlib.General;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.config.ConfigHomeInvites;
import com.andune.minecraft.hsp.entity.HomeInvite;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeInviteDAO;

import javax.inject.Inject;
import java.util.Date;
import java.util.Set;

/**
 * @author andune
 */
public class HomeInviteList extends BaseCommand {
    @Inject
    private ConfigHomeInvites config;
    @Inject
    private General general;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"hil"};
    }

    @Override
    public boolean execute(Player p, String[] args) {
        HomeInviteDAO dao = storage.getHomeInviteDAO();
        Set<HomeInvite> invites = null;

        // show public invites if requested
        if( config.allowPublicInvites() && args != null && args.length > 0 && args[0].equalsIgnoreCase("public") ) {
            invites = dao.findAllPublicInvites();
            if( invites != null && invites.size() > 0 ) {
                p.sendMessage("Public home invites:");
                for (HomeInvite invite : invites) {
                    if (isExpired(invite))
                        continue;
                    String homeName = invite.getHome().getName();
                    p.sendMessage("(id " + invite.getId() + ") -> Invite for home " + homeName
                                    + " from player " + invite.getHome().getPlayerName()
                                    + " [expires: " + (invite.getExpires() != null ?
                                    general.displayTimeString(
                                            invite.getExpires().getTime() - System.currentTimeMillis(), true, "m") :
                                    "never")
                                    + "]"
                    );
                }
            } else {
                p.sendMessage("There are no public home invites available");
            }

            return true;
        }

        invites = dao.findAllOpenInvites(p.getName());
        if (invites != null && invites.size() > 0) {
            p.sendMessage("Open invites for your homes:");
            for (HomeInvite invite : invites) {
                if (isExpired(invite))
                    continue;
                String homeName = invite.getHome().getName();
                p.sendMessage("(id " + invite.getId() + ") -> Invite for home " + homeName
                        + " to player " + invite.getInvitedPlayer()
                        + " [expires: " + (invite.getExpires() != null ?
                        general.displayTimeString(
                                invite.getExpires().getTime() - System.currentTimeMillis(), true, "m") :
                        "never")
                        + "]"
                );
            }
        } else
            p.sendMessage("You have no open invites to your homes");

        invites = dao.findAllAvailableInvites(p.getName());
        if (invites != null && invites.size() > 0) {
            p.sendMessage("Open invites extended to you:");
            for (HomeInvite invite : invites) {
                if (isExpired(invite))
                    continue;
                String homeName = invite.getHome().getName();
                String playerName = invite.getHome().getPlayerName();
                p.sendMessage("(id " + invite.getId() + ") -> Invite to home " + homeName
                        + " from player " + playerName
                        + " [expires: " + (invite.getExpires() != null ?
                        general.displayTimeString(
                                invite.getExpires().getTime() - System.currentTimeMillis(), true, "m") :
                        "never")
                        + "]"
                );
            }
        } else
            p.sendMessage("You have no invites to other players homes");

        if (config.allowPublicInvites()) {
            invites = dao.findAllPublicInvites();
            if( invites != null && invites.size() > 0 ) {
                server.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_LIST_PUBLIC_AVAILABLE);
                return true;
            }
        }
        return true;
    }

    /**
     * Check if a homeInvite is expired and if so, delete it. Any possible errors
     * related to deleting it are ignored.
     *
     * @param homeInvite
     * @return true if the invite is expired, false if not
     */
    private boolean isExpired(final HomeInvite homeInvite) {
        final com.andune.minecraft.hsp.entity.Home home = homeInvite.getHome();
        // if the home no longer exists, clean up the invite
        if (home == null
                || (home.getPlayerName() == null && home.getWorld() == null)) {
            deleteHomeInvite(homeInvite);
            return true;
        }

        Date expires = homeInvite.getExpires();
        if (expires != null && expires.compareTo(new Date()) < 0) {
            deleteHomeInvite(homeInvite);
            return true;    // expired
        }
        // also check if it's a bedhome and we're not allowed to teleport to bedhomes
        else if (!config.allowBedHomeInvites() && homeInvite.getHome().isBedHome()) {
            deleteHomeInvite(homeInvite);
            return true;
        } else
            return false;    // not expired
    }

    private void deleteHomeInvite(final com.andune.minecraft.hsp.entity.HomeInvite hi) {
        // it's expired, so delete it. we ignore any error here since it doesn't
        // affect the outcome of the rest of the command.
        try {
            storage.getHomeInviteDAO().deleteHomeInvite(hi);
        } catch (StorageException e) {
            log.warn("Caught exception", e);
        }
    }
}
