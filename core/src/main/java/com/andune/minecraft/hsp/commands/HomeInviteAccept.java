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

import javax.inject.Inject;


import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.config.ConfigHomeInvites;
import com.andune.minecraft.hsp.manager.HomeInviteManager;
import com.andune.minecraft.hsp.manager.WarmupRunner;

/**
 * @author andune
 *
 */
@UberCommand(uberCommand="home", subCommand="inviteAccept",
    aliases={"ia"}, help="Accept a home invite")
public class HomeInviteAccept extends BaseCommand {
    @Inject private ConfigHomeInvites config;
    @Inject private HomeInviteManager homeInviteManager;
    @Inject private Teleport teleport;

	@Override
	public String[] getCommandAliases() { return new String[] {"hiaccept", "hia"}; }
	
	@Override
	public boolean execute(final Player p, final String[] args) {
		String warmupName = getCommandName();
		if( config.useHomeWarmup() )
			warmupName = "home";
		
		final com.andune.minecraft.hsp.entity.Home h = homeInviteManager.getInvitedHome(p);
		if( h != null ) {
			String cooldownName = getCooldownName("homeinviteaccept", Integer.toString(h.getId()));
			if( config.useHomeCooldown() )
				cooldownName = getCooldownName("home", Integer.toString(h.getId()));

			if( !cooldownCheck(p, cooldownName) )
				return true;

			if( hasWarmup(p, warmupName) ) {
	    		final Location finalL = h.getLocation();
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;
					private String cdName;
					private String wuName;
					
					public void run() {
						if( !canceled ) {
							server.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", h.getName());
							if( applyCost(p, true, cdName) )
								teleport.teleport(p, finalL, null);
							homeInviteManager.removeInvite(p);
						}
					}

					public void cancel() {
						canceled = true;
					}

					public void setPlayerName(String playerName) {}
					public void setWarmupId(int warmupId) {}
					public WarmupRunner setCooldownName(String cd) { cdName = cd; return this; }
					public WarmupRunner setWarmupName(String warmupName) { wuName = warmupName; return this; }
					public String getWarmupName() { return wuName; }
				}.setCooldownName(cooldownName).setWarmupName(warmupName));
			}
			else {
				if( applyCost(p, true, cooldownName) ) {
					teleport.teleport(p, h.getLocation(), null);
					server.sendLocalizedMessage(p, HSPMessages.CMD_HIACCEPT_TELEPORTED,
							"home", h.getName(), "player", h.getPlayerName());
					homeInviteManager.removeInvite(p);
				}
			}
		}
		else 
			server.sendLocalizedMessage(p, HSPMessages.CMD_HIACCEPT_NO_INVITE);
		
		return true;
	}

}
