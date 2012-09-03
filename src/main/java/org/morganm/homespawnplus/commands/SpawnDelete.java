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

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;

/**
 * @author morganm
 *
 */
public class SpawnDelete extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"spawnd", "deletespawn", "rmspawn"}; }
	
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_SPAWNDELETE_USAGE);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		org.morganm.homespawnplus.entity.Spawn spawn = null;
		
		if( args.length < 1 ) {
			return false;
		}
		
		SpawnDAO dao = plugin.getStorage().getSpawnDAO();
		
		int id = -1;
		try {
			id = Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {}
		if( id != -1 )
			spawn = dao.findSpawnById(id);
		
		if( spawn == null )
			spawn = dao.findSpawnByName(args[0]);
		
		if( spawn == null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_NO_SPAWN_FOUND,
					"name", args[0]);
//			util.sendMessage(p, "No spawn found for name or id: "+args[0]);
			return true;
		}
		
		try {
			dao.deleteSpawn(spawn);
			util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_SPAWN_DELETED,
					"name", args[0]);
		}
		catch(StorageException e) {
			util.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
			log.log(Level.WARNING, "Error caught in /"+getCommandName()+": "+e.getMessage(), e);
		}
		return true;
	}

}
