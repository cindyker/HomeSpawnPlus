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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class SetHomeOther extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"sethomeo", "sho"}; }

	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_SETHOMEOTHER_USAGE);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(final Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if(args.length < 1) {
			return false;
		}
		
		final String setter = p.getName();
		final Location l = p.getLocation();
		
		String homeowner = null;
		// try player name best match
		final OfflinePlayer otherPlayer = util.getBestMatchPlayer(args[0]);
		if( otherPlayer != null ) {
			homeowner = otherPlayer.getName();
		}
		// no match, no point in proceeding, no online or offline player by
		// that name exists
		else {
			util.sendLocalizedMessage(p, HSPMessages.PLAYER_NOT_FOUND,
					"player", args[0]);
			return true;
		}
		
		if( args.length > 1 ) {
			if( util.setNamedHome(homeowner, l, args[1], setter) )
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_HOME_SET,
						"name", args[1], "player", homeowner);
		}
		else {
			if( util.setHome(homeowner, l, setter, true, false) )
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_DEFAULT_HOME_SET,
						"player", homeowner);
		}
		
		return true;
	}

}
