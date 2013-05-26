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

import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;

/**
 * @author andune
 *
 */
@UberCommand(uberCommand="spawn", subCommand="rename", help="Rename a spawn")
public class SpawnRename extends BaseCommand {
    @Override
    public String[] getCommandAliases() { return new String[] {"spawnr", "renamespawn"}; }
    
    @Override
    public String getUsage() {
        return  server.getLocalizedMessage(HSPMessages.CMD_SPAWNRENAME_USAGE);
    }

    @Override
    public boolean execute(Player p, String[] args) {
        if( !defaultCommandChecks(p) )
            return true;
 
        com.andune.minecraft.hsp.entity.Spawn spawn = null;
        
        if( args.length < 2 ) {
            return false;
        }
        String newName = args[1];

        final SpawnDAO dao = storage.getSpawnDAO();
        
        // try search by ID number
        int id = -1;
        try {
            id = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e) {}
        if( id != -1 )
            spawn = dao.findSpawnById(id);
        
        // if argument was not a number or not found, then search by name
        if( spawn == null )
            spawn = dao.findSpawnByName(args[0]);
        
        if( spawn == null ) {
            server.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_NO_SPAWN_FOUND,
                    "name", args[0]);
            return true;
        }

        try {
            String oldName = spawn.getName();
            if( oldName == null ) {
                oldName = "id #"+spawn.getId();
            }
            
            spawn.setName(newName);
            dao.saveSpawn(spawn);
            
            server.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNRENAME_SPAWN_RENAMED,
                    "oldName", oldName, "newName", newName);
        }
        catch(StorageException e) {
            server.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
            log.warn("Error caught in /"+getCommandName()+": "+e.getMessage(), e);
        }
        return true;
    }
}
