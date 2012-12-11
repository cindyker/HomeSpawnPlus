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

import javax.inject.Inject;

import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.server.api.CommandSender;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.mBukkitLib.PermissionSystem;

/**
 * @author morganm
 *
 */
public class PermCheck extends BaseCommand {
    @Inject private PermissionSystem permSystem;

	@Override
	public String[] getCommandAliases() { return new String[] {"pc"}; }

	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_PERMCHECK_USAGE);
	}

    public boolean execute(CommandSender sender, String[] args) {
        if( !defaultCommandChecks(sender) )
            return false;

		if( args.length < 1 ) 
			return false;
		
		String playerName = sender.getName();
		if( args.length > 1 )
			playerName = args[1];
		
		// let server do "fuzzy match" and then get the exact name
		Player p = server.getPlayer(playerName);
		if( p != null ) {
			playerName = p.getName();
		}
		
		String world = "world";
		// set world arg if passed an argument
		if( args.length > 2 ) {
			world = args[2];
		}
		// otherwise if the player is logged in, default to the world they are currently in
		else if( p != null ) {
			world = p.getWorld().getName();
		}
		
		String permission = args[0];
		
		boolean result = permSystem.has(world, playerName, permission);
		
		HSPMessages msg = null;
		if( result )
			msg = HSPMessages.CMD_PERM_CHECK_TRUE;
		else
			msg = HSPMessages.CMD_PERM_CHECK_FALSE;
		
		server.sendLocalizedMessage(sender, msg,
				"permission", permission,
				"player", playerName,
				"world", world,
				"system", permSystem.getSystemInUseString());
		
		return true;
	}

}
