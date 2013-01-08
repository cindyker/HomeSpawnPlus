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
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.OfflinePlayer;
import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.util.HomeUtil;

/**
 * @author andune
 *
 */
public class SetHomeOther extends BaseCommand {
    @Inject private HomeUtil util;

	@Override
	public String[] getCommandAliases() { return new String[] {"sethomeo", "sho"}; }

	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_SETHOMEOTHER_USAGE);
	}

	@Override
	public boolean execute(final Player p, String[] args) {
		if(args.length < 1) {
			return false;
		}
		
		final String setter = p.getName();
		final Location l = p.getLocation();
		
		String homeowner = null;
		// try player name best match
		final OfflinePlayer otherPlayer = server.getBestMatchPlayer(args[0]);
		if( otherPlayer != null ) {
			homeowner = otherPlayer.getName();
		}
		// no match, no point in proceeding, no online or offline player by
		// that name exists
		else {
			server.sendLocalizedMessage(p, HSPMessages.PLAYER_NOT_FOUND,
					"player", args[0]);
			return true;
		}
		
		if( args.length > 1 ) {
		    String errorMsg = util.setNamedHome(homeowner, l, args[1], setter);
			if( errorMsg != null)
			    p.sendMessage(errorMsg);
			else
			    server.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_HOME_SET,
						"name", args[1], "player", homeowner);
		}
		else {
		    String errorMsg = util.setHome(homeowner, l, setter, true, false);
            if( errorMsg != null)
                p.sendMessage(errorMsg);
            else
			    server.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_DEFAULT_HOME_SET,
						"player", homeowner);
		}
		
		return true;
	}

}
