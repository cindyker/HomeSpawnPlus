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
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class SetDefaultSpawn extends BaseCommand {

	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_SETDEFAULTSPAWN_USAGE);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
//		boolean localOnlyFlag = false;
		
		if( args.length < 1 ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPECIFY_NAME);
//			util.sendMessage(p, "You must specify the spawnName to set as default.");
		}
		else {
			String spawnName = args[0];
			
			/*
			if( args[0].equals("-l") ) {
				if( args.length < 2 ) {
					util.sendMessage(p, "You must specify the spawnName to set as global default. Single world spawn is named "+Storage.HSP_WORLD_SPAWN_GROUP);
				}
				else {
					localOnlyFlag = true;
					spawnName = args[1];
				}
			}
			else {
				spawnName = args[0];
			}
			*/
			
			org.morganm.homespawnplus.entity.Spawn spawn = util.getSpawnByName(spawnName);
			if( spawn != null ) {
				Location l = spawn.getLocation();
				util.setSpawn(l, p.getName());
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPAWN_CHANGED,
						"name", spawn.getName(), "location", util.shortLocationString(l));
				
//				util.sendMessage(p, "Default spawn changed to "+spawn.getName()+" at location ["+util.shortLocationString(l)+"]");
				
				/*
				if( localOnlyFlag )
				else {
					// TODO: first blank out all globalSpawn flags.
					spawn.setGlobalDefaultSpawn(true);
				}
				
				plugin.getStorage().writeSpawn(spawn);
				*/
			}
		}
		
		return true;
	}

}
