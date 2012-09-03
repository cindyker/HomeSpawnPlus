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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class HomeOther extends BaseCommand {
//	private static final String OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.others";

	@Override
	public String[] getCommandAliases() { return new String[] {"homeo"}; }
	
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_HOMEOTHER_USAGE);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
			return false;
		}
		
		String playerName = null;
		String worldName = null;
		String homeName = null;
		
		// try player name best match
		final OfflinePlayer otherPlayer = util.getBestMatchPlayer(args[0]);
		if( otherPlayer != null )
			playerName = otherPlayer.getName();
		else
			playerName = args[0];
		
		for(int i=1; i < args.length; i++) {
			if( args[i].startsWith("w:") ) {
				worldName = args[i].substring(2);
			}
			else {
				if( homeName != null ) {
					util.sendLocalizedMessage(p, HSPMessages.TOO_MANY_ARGUMENTS);
					return true;
				}
				homeName = args[i];
			}
		}
		
		if( worldName == null )
			worldName = p.getWorld().getName();
		
		org.morganm.homespawnplus.entity.Home home;
		if( homeName != null ) {
			home = plugin.getStorage().getHomeDAO().findHomeByNameAndPlayer(homeName, playerName);
		}
		else {
			home = util.getDefaultHome(playerName, worldName);
		}
		
		// didn't find an exact match?  try a best guess match
		if( home == null )
			home = util.getBestMatchHome(playerName, worldName);
		
		if( home != null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEOTHER_TELEPORTING,
					"home", home.getName(), "player", home.getPlayerName(), "world", home.getWorld());
			if( applyCost(p) )
	    		util.teleport(p, home.getLocation(), TeleportCause.COMMAND);
		}
		else if( homeName != null )
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_HOME_FOUND,
					"home", homeName, "player", playerName);
		else
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_DEFAULT_HOME_FOUND,
					"player", playerName, "world", worldName);
		
		return true;
	}

}
