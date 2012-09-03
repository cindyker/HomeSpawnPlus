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
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class HomeListOther extends BaseCommand {
	private HomeList homeListCommand;

	@Override
	public String[] getCommandAliases() { return new String[] {"hlo"}; }

	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_HOMELISTOTHER_USAGE);
	}

	@Override
	public org.morganm.homespawnplus.command.Command setPlugin(HomeSpawnPlus plugin) {
		homeListCommand = new HomeList();
		homeListCommand.setPlugin(plugin);
		
		return super.setPlugin(plugin);
	}

	@Override
	public boolean execute(ConsoleCommandSender console, org.bukkit.command.Command command, String[] args) {
		return executePrivate(console, command, args);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		return executePrivate(p, command, args);
	}
	
	private boolean executePrivate(CommandSender p, Command command, String[] args) {
		String player = null;
		String world = "all";
		
		if( args.length < 1 ) {
			return false;
		}
		
		// try player name best match
		final OfflinePlayer otherPlayer = util.getBestMatchPlayer(args[0]);
		if( otherPlayer != null )
			player = otherPlayer.getName();
		else
			player = args[0];

		if( args.length > 1 )
			world = args[1];
		
		return homeListCommand.executeCommand(p, player, world);
	}

}
