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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.Storage;

/**
 * @author morganm
 *
 */
public class SpawnList extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"spawnl", "listspawns"}; }

	@Override
	public boolean execute(ConsoleCommandSender console, org.bukkit.command.Command command, String[] args) {
		return executePrivate(console, command, args);
	}

	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_SPAWNLIST_USAGE);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		return executePrivate(p, command, args);
	}
	
	private boolean executePrivate(CommandSender p, Command command, String[] originalArgs) {
		boolean showMapSpawn = false;
		List<String> args = new ArrayList<String>(originalArgs.length);
		
		String world = "all";
		if( originalArgs.length > 0 ) {
			for(int i=0; i < originalArgs.length; i++) {
				if( originalArgs[i].equals("-m") ) {
					showMapSpawn = true;
				}
				else
					args.add(originalArgs[i]);
			}
			
			if( args.size() > 0 )
				world = args.get(0);
		}
		
		final Set<org.morganm.homespawnplus.entity.Spawn> spawns = plugin.getStorage().getSpawnDAO().findAllSpawns();
		
		boolean displayedSpawn = false;
		if( spawns != null && spawns.size() > 0 ) {
			if( world.equals("all") || world.equals("*") ) {
				world = "all";
				util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNLIST_ALL_WORLDS);
//				util.sendMessage(p, "Spawn list for all worlds: ");
			}
			else
				util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNLIST_FOR_WORLD,
						"world", world);
//				util.sendMessage(p, "Spawn list on world \""+world+"\": ");
			
			if( showMapSpawn ) {
				List<World> worlds = null;
				if( world.equals("all") ) {
					worlds = plugin.getServer().getWorlds();
				}
				else {
					worlds = new ArrayList<World>();
					World w = plugin.getServer().getWorld(world);
					if( w != null )
						worlds.add(w);
				}
				
				for(World w : worlds) {
					Location l = w.getSpawnLocation();
					p.sendMessage(util.getDefaultColor() + "id: " + ChatColor.RED + "none " + util.getDefaultColor()
							+ util.shortLocationString(l)
							+ ChatColor.GREEN + " (map spawn)"
							);
				}
			}
			
			for(org.morganm.homespawnplus.entity.Spawn spawn : spawns) {
				if( !world.equals("all") && !world.equals(spawn.getWorld()) )
					continue;
					
				displayedSpawn = true;
				
				String group = spawn.getGroup();
				if( Storage.HSP_WORLD_SPAWN_GROUP.equals(group) )
					group = null;
				String name = spawn.getName();
				
				p.sendMessage(util.getDefaultColor() + "id: " + ChatColor.RED + spawn.getId() + " " + util.getDefaultColor()
						+ (name != null ? "["+util.getLocalizedMessage(HSPMessages.GENERIC_NAME)+": " + ChatColor.RED + name + util.getDefaultColor() + "] " : "")
						+ (group != null ? "["+util.getLocalizedMessage(HSPMessages.GENERIC_GROUP)+": " + ChatColor.RED + group + util.getDefaultColor() + "] " : "")
						+ util.shortLocationString(spawn)
						+ (Storage.HSP_WORLD_SPAWN_GROUP.equals(spawn.getGroup())
								? ChatColor.GREEN + " ("+util.getLocalizedMessage(HSPMessages.GENERIC_WORLD_DEFAULT)+")"
								: ""));
			}
		}
		
		if( !displayedSpawn )
			util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNLIST_NO_SPAWNS_FOUND,
					"world", world);
//			util.sendMessage(p, "No spawns found for world \""+world+"\"");

		return true;
	}

}
