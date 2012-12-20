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

import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.server.api.CommandSender;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.Storage;

/**
 * @author morganm
 *
 */
public class HomeList extends BaseCommand {
    @Inject private Storage storage;

	@Override
	public String[] getCommandAliases() { return new String[] {"homel", "listhomes", "hl", "homes"}; }
	
	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_HOMELIST_USAGE);
	}

    @Override
    public boolean execute(Player player, String[] args) {
        String world = "all";
        if( args.length > 0 )
            world = args[0];
        
        return executeCommand(player, player.getName(), world);
    }

	/** Package visibility, code is reused by HomeListOther.
	 * 
	 * @param sender
	 * @param command
	 * @param args
	 * @return
	 */
	boolean executeCommand(CommandSender sender, String player, String world) {
		Set<org.morganm.homespawnplus.entity.Home> homes;
		
		homes = storage.getHomeDAO().findHomesByWorldAndPlayer(world, player);
		
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
                sender.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMELIST_ALL_WORLDS,
						"player", player) );
			else
                sender.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMELIST_FOR_WORLD,
						"world", world, "player", player) );
			
			for(org.morganm.homespawnplus.entity.Home home : homes) {
				String name = home.getName();
				if( name == null )
					name = "<noname>";
				sender.sendMessage(name+" [id:"+home.getId()+"]: "+ home.getLocation().shortLocationString()
						+ (home.isDefaultHome()
								? " ("+server.getLocalizedMessage(HSPMessages.GENERIC_DEFAULT)+")"
								: ""));
			}
		}
		else
		    sender.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOMELIST_NO_HOMES_FOUND,
					"world", world, "player", player) );

		return true;
	}

}
