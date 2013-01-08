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


import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.server.api.OfflinePlayer;
import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.server.api.Teleport;
import com.andune.minecraft.hsp.util.HomeUtil;

/**
 * @author andune
 *
 */
public class HomeOther extends BaseCommand {
    @Inject private Teleport teleport;
    @Inject private HomeUtil homeUtil;
    
//	private static final String OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.others";

	@Override
	public String[] getCommandAliases() { return new String[] {"homeo"}; }
	
	@Override
	public String getUsage() {
		return	server.getLocalizedMessage(HSPMessages.CMD_HOMEOTHER_USAGE);
	}

	@Override
	public boolean execute(Player p, String[] args) {
		if( args.length < 1 ) {
			return false;
		}
		
		String playerName = null;
		String worldName = null;
		String homeName = null;
		
		// try player name best match
		final OfflinePlayer otherPlayer = server.getBestMatchPlayer(args[0]);
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
					server.sendLocalizedMessage(p, HSPMessages.TOO_MANY_ARGUMENTS);
					return true;
				}
				homeName = args[i];
			}
		}
		
		if( worldName == null )
			worldName = p.getWorld().getName();
		
		com.andune.minecraft.hsp.entity.Home home;
		if( homeName != null ) {
			home = storage.getHomeDAO().findHomeByNameAndPlayer(homeName, playerName);
		}
		else {
			home = homeUtil.getDefaultHome(playerName, worldName);
		}
		
		// didn't find an exact match?  try a best guess match
		if( home == null )
			home = homeUtil.getBestMatchHome(playerName, worldName);
		
		if( home != null ) {
			server.sendLocalizedMessage(p, HSPMessages.CMD_HOMEOTHER_TELEPORTING,
					"home", home.getName(), "player", home.getPlayerName(), "world", home.getWorld());
			if( applyCost(p) )
	    		teleport.teleport(p, home.getLocation(), null);
		}
		else if( homeName != null )
			server.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_HOME_FOUND,
					"home", homeName, "player", playerName);
		else
			server.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_DEFAULT_HOME_FOUND,
					"player", playerName, "world", worldName);
		
		return true;
	}

}
