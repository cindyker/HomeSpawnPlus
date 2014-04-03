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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.convert.CommandBook;
import org.morganm.homespawnplus.convert.Essentials23;
import org.morganm.homespawnplus.convert.Essentials29;
import org.morganm.homespawnplus.convert.SpawnControl;


/**
 * @author morganm
 *
 */
public class HSPConvert extends BaseCommand {

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
	
	private boolean executePrivate(CommandSender sender, Command command, String[] args) {
		if( !isEnabled() || !plugin.hasPermission(sender, HomeSpawnPlus.BASE_PERMISSION_NODE+".admin") )
			return false;
		
		Runnable converter = null;
		
		if( args.length < 1 ) {
			sender.sendMessage("Usage: /"+getCommandName()+" [essentials|spawncontrol|commandbook]");
		}
		else if( args[0].equalsIgnoreCase("commandbook") ) {
			sender.sendMessage("Starting CommandBook conversion");
			converter = new CommandBook(plugin, sender);
		}
		else if( args[0].equalsIgnoreCase("essentials") ) {
			sender.sendMessage("Starting Essentials 2.9+ conversion");
			converter = new Essentials29(plugin, sender);
		}
		else if( args[0].equalsIgnoreCase("essentials23") ) {
			sender.sendMessage("Starting Essentials 2.3 conversion");
			converter = new Essentials23(plugin, sender);
		}
		else if( args[0].equalsIgnoreCase("spawncontrol") ) {
			sender.sendMessage("Starting SpawnControl conversion");
			converter = new SpawnControl(plugin, sender);
		}
		else {
			sender.sendMessage("Unknown conversion type: "+args[0]);
		}
		
		if( converter != null ) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, converter);
		}
		
		return true;
	}

}
