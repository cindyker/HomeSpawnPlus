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

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class HomeList extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"homel", "listhomes", "hl", "homes"}; }
	
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_HOMELIST_USAGE);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		String world = "all";
		if( args.length > 0 )
			world = args[0];
		
		return executeCommand(p, p.getName(), world);
	}

	/** Package visibility, code is reused by HomeListOther.
	 * 
	 * @param p
	 * @param command
	 * @param args
	 * @return
	 */
	boolean executeCommand(CommandSender p, String player, String world) {
		Set<org.morganm.homespawnplus.entity.Home> homes;
		
		homes = plugin.getStorage().getHomeDAO().findHomesByWorldAndPlayer(world, player);
		
		if( homes != null && homes.size() > 0 ) {
			/*
			 *  MC uses variable-width font, so tabular sprintf-style formatted strings don't
			 *  line up properly.  Boo.
			util.sendMessage(p, String.format("%-16s %12s/%6s/%6s/%6s %-8s",
					"name",
					"world",
					"x","y","z",
					"default"));
					*/

			if( world.equals("all") || world.equals("*") )
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOMELIST_ALL_WORLDS,
						"player", player);
			else
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOMELIST_FOR_WORLD,
						"world", world, "player", player);
			
			for(org.morganm.homespawnplus.entity.Home home : homes) {
				String name = home.getName();
				if( name == null )
					name = "<noname>";
				util.sendMessage(p, name+" [id:"+home.getId()+"]: "+ util.shortLocationString(home)
						+ (home.isDefaultHome()
								? " ("+util.getLocalizedMessage(HSPMessages.GENERIC_DEFAULT)+")"
								: ""));
				/*
				util.sendMessage(p, String.format("%-16s %12s/%6d/%6d/%6d %-8s",
						home.getName(),
						home.getWorld().trim(),
						(int) home.getX(), (int) home.getY(), (int) home.getZ(),
						home.isDefaultHome() ? "yes" : "no"
					));
					*/
			}
		}
		else
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMELIST_NO_HOMES_FOUND,
					"world", world, "player", player);
//			util.sendMessage(p, "No homes found for world \""+world+"\"");

		return true;
	}

}
