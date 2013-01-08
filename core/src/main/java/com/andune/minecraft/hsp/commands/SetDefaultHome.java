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


import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.storage.StorageException;

/**
 * @author andune
 *
 */
public class SetDefaultHome extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"sdh"}; }
	
	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_SETDEFAULTHOME_USAGE);
	}

	@Override
	public boolean execute(Player p, String[] args) {
		if( args.length < 1 ) {
			server.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTHOME_SPECIFY_HOMENAME);
		}
		else {
			String homeName = args[0];
			
			com.andune.minecraft.hsp.entity.Home home = storage.getHomeDAO().findHomeByNameAndPlayer(p.getName(), homeName);
			
			if( home != null ) {
				home.setDefaultHome(true);
				try {
					storage.getHomeDAO().saveHome(home);
					server.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTHOME_HOME_CHANGED,
							"world", home.getWorld(), "home", home.getName());
				}
				catch(StorageException e) {
				    server.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
					log.warn("Error caught in /"+getCommandName(), e);
				}
			}
		}
		
		return true;
	}

}
