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

import java.util.Set;

import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.storage.StorageException;

/**
 * @author andune
 *
 */
public class HomeRename extends BaseCommand {
    @Override
    public String[] getCommandAliases() { return new String[] {"homer", "renamehome"}; }
    
    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_HOMERENAME_USAGE);
    }

    @Override
    public boolean execute(Player p, String[] args) {
        if( !defaultCommandChecks(p) )
            return true;
        
        com.andune.minecraft.hsp.entity.Home home = null;
        String homeName = null;
        
        if( args.length < 2 ) {
            return false;   // print usage
        }
        homeName = args[0];
        String newName = args[1];

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
        else if( homeName.equals("<noname>") ) {
            Set<? extends com.andune.minecraft.hsp.entity.Home> homes = storage.getHomeDAO().findHomesByWorldAndPlayer(p.getWorld().getName(), p.getName());
            if( homes != null ) {
                for(com.andune.minecraft.hsp.entity.Home h : homes) {
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
        
        if( home != null ) {
            // safety check to be sure we aren't renaming someone else's home with this command
            // (this shouldn't be possible since all checks are keyed to this player's name, but
            // let's be paranoid anyway)
            if( !p.getName().equals(home.getPlayerName()) ) {
                server.sendLocalizedMessage(p, HSPMessages.CMD_HOMERENAME_ERROR_RENAMING_OTHER_HOME);
                log.warn("ERROR: Shouldn't be possible! Player "+p.getName()+" tried to rename home for player "+home.getPlayerName());
            }
            else {
                if( home.isBedHome() || home.isDefaultHome() ) {
                    server.sendLocalizedMessage(p, HSPMessages.CMD_HOMERENAME_NAMED_HOMES_ONLY);
                    return true;
                }
                
                try {
                    home.setName(newName);
                    storage.getHomeDAO().saveHome(home);
                    server.sendLocalizedMessage(p, HSPMessages.CMD_HOMERENAME_HOME_RENAMED,
                            "oldName", homeName, "newName", newName);
                }
                catch(StorageException e) {
                    server.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
                    log.warn("Error caught in /"+getCommandName()+": "+e.getMessage(), e);
                }
            }
        }
        else {
            server.sendLocalizedMessage(p, HSPMessages.NO_NAMED_HOME_FOUND, "name", homeName);
        }
        
        return true;
    }
}
