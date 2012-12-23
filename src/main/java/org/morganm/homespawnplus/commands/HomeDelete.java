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

import javax.inject.Inject;

import org.morganm.homespawnplus.HSPMessages;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.util.HomeUtil;

/**
 * @author morganm
 *
 */
public class HomeDelete extends BaseCommand {
    private HomeUtil homeUtil;
    
    @Inject
    public void setHomeUtil(HomeUtil homeUtil) {
        this.homeUtil = homeUtil;
    }

	@Override
	public String[] getCommandAliases() { return new String[] {"homed", "deletehome", "rmhome"}; }
	
	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETE_USAGE);
	}

	@Override
	public boolean execute(Player p, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		org.morganm.homespawnplus.entity.Home home = null;
		String homeName = null;
		
		if( args.length > 0 ) {
			homeName = args[0];
			
			int id = -1;
			try {
				id = Integer.parseInt(homeName);
			}
			catch(NumberFormatException e) {}
			
			if( id != -1 ) {
				home = storage.getHomeDAO().findHomeById(id);
				// make sure it belongs to this player
				if( home != null && !p.getName().equals(home.getPlayerName()) )
					home = null;
				
				// otherwise set the name according to the home that was selected
				if( home != null )
					homeName = home.getName() + " (id #"+id+")";
			}
			else if( args[0].startsWith("w:") ) {
				String worldName = homeName.substring(2);
				home = homeUtil.getDefaultHome(p.getName(), worldName);
				if( home == null ) {
				    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_NO_HOME_ON_WORLD, "world", worldName) );
					return true;
				}
			}
			else if( homeName.equals("<noname>") ) {
				Set<org.morganm.homespawnplus.entity.Home> homes = storage.getHomeDAO()
						.findHomesByWorldAndPlayer(p.getWorld().getName(), p.getName());
				if( homes != null ) {
					for(org.morganm.homespawnplus.entity.Home h : homes) {
						if( h.getName() == null ) {
							home = h;
							break;
						}
					}
				}
			}
			
			// if home is still null here, then just do a regular lookup
			if( home == null )
			    home = storage.getHomeDAO().findHomeByNameAndPlayer(homeName, p.getName());
		}
		else
			home = homeUtil.getDefaultHome(p.getName(), p.getWorld().getName());

		if( home != null ) {
			// safety check to be sure we aren't deleting someone else's home with this command
			// (this shouldn't be possible since all checks are keyed to this player's name, but
			// let's be paranoid anyway)
			if( !p.getName().equals(home.getPlayerName()) ) {
			    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETE_ERROR_DELETING_OTHER_HOME) );
				log.warn("ERROR: Shouldn't be possible! Player {} tried to delete home for player {}",
				        p.getName(), home.getPlayerName());
			}
			else {
				try {
					storage.getHomeDAO().deleteHome(home);
					if( homeName != null )
					    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETE_HOME_DELETED, "name", homeName) );
					else
					    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETE_DEFAULT_HOME_DELETED) );
				}
				catch(StorageException e) {
				    p.sendMessage( server.getLocalizedMessage(HSPMessages.GENERIC_ERROR) );
					log.warn("Error caught in /"+getCommandName(), e);
				}
			}
		}
		else if( homeName != null )
		    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETE_NO_HOME_FOUND, "name", homeName) );
		else
		    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETE_NO_DEFAULT_HOME_FOUND) );
		
		return true;
	}

}
