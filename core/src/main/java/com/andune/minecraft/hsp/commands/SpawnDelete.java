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


import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;

/**
 * @author andune
 *
 */
public class SpawnDelete extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"spawnd", "deletespawn", "rmspawn"}; }
	
	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_SPAWNDELETE_USAGE);
	}

	@Override
	public boolean execute(CommandSender p, String cmd, String[] args) {
	    if( !defaultCommandChecks(p) )
	        return false;
	    
		Spawn spawn = null;
		
		if( args.length < 1 ) {
			return false;
		}
		
		SpawnDAO dao = storage.getSpawnDAO();
		
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
			server.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_NO_SPAWN_FOUND,
					"name", args[0]);
			return true;
		}
		
		try {
			dao.deleteSpawn(spawn);
			server.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_SPAWN_DELETED,
					"name", args[0]);
		}
		catch(StorageException e) {
			server.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
			log.warn("Error caught in /"+getCommandName(), e);
		}
		return true;
	}

}
