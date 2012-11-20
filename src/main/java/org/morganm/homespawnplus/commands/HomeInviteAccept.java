/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
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
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.old.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.manager.WarmupRunner;

/**
 * @author morganm
 *
 */
public class HomeInviteAccept extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hiaccept", "hia"}; }
	
	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		if( !isEnabled() || !hasPermission(p) )
			return true;
		
		String warmupName = getCommandName();
		if( plugin.getConfig().getBoolean(ConfigOptions.HOME_INVITE_USE_HOME_WARMUP, true) )
			warmupName = "home";
		
		final org.morganm.homespawnplus.entity.Home h = plugin.getHomeInviteManager().getInvitedHome(p);
		if( h != null ) {
			String cooldownName = getCooldownName("homeinviteaccept", Integer.toString(h.getId()));
			if( plugin.getConfig().getBoolean(ConfigOptions.HOME_INVITE_USE_HOME_COOLDOWN, true) )
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
							util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", h.getName());
							if( applyCost(p, true, cdName) )
								util.teleport(p, finalL, TeleportCause.COMMAND);
							plugin.getHomeInviteManager().removeInvite(p);
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
					util.teleport(p, h.getLocation(), TeleportCause.COMMAND);
					util.sendLocalizedMessage(p, HSPMessages.CMD_HIACCEPT_TELEPORTED,
							"home", h.getName(), "player", h.getPlayerName());
					plugin.getHomeInviteManager().removeInvite(p);
				}
			}
		}
		else 
			util.sendLocalizedMessage(p, HSPMessages.CMD_HIACCEPT_NO_INVITE);
		
		return true;
	}

}
