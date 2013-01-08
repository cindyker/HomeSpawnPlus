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

import javax.inject.Inject;

import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Initializer;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.integration.dynmap.DynmapModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCore;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortals;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorder;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;
import com.andune.minecraft.hsp.server.api.CommandSender;
import com.andune.minecraft.hsp.util.BackupUtil;

/**
 * @author andune
 *
 */
public class HSP extends BaseCommand {
    @Inject Initializer initializer;
    @Inject MultiverseCore multiverseCore;
    @Inject MultiversePortals multiversePortals;
    @Inject DynmapModule dynmap;
    @Inject WorldBorder worldBorder;
    @Inject WorldGuard worldGuard;
    @Inject BackupUtil backupUtil;
    
	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_HSP_USAGE);
	}
	
    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if( !permissions.isAdmin(sender) )
			return false;
		
		if( args.length < 1 ) {
			return false;
		}
		else if( args[0].startsWith("reloadc") || args[0].equals("rc") ) {
			boolean success = false;
			try {
			    initializer.initConfigs();
				
				// also call hookWarmups, in case admin changed the warmup settings
//				plugin.hookWarmups();
				
				success = true;
			}
			catch(Exception e) {
			    log.error("Caught exception reloading config", e);
				server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_ERROR_RELOADING);
			}
			
			if( success )
			    server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_CONFIG_RELOADED);
		}
        else if( args[0].startsWith("modules") ) {
            sender.sendMessage("Dynmap module "
                    + (dynmap.isEnabled() ? "enabled" : "disabled")
                    + ", detected version " + dynmap.getVersion());
            
            sender.sendMessage("Multiverse-Core module "
                    + (multiverseCore.isEnabled() ? "enabled" : "disabled")
                    + ", detected version " + multiverseCore.getVersion());
            sender.sendMessage("Multiverse-Portals module "
                    + (multiversePortals.isEnabled() ? "enabled" : "disabled")
                    + ", detected version " + multiversePortals.getVersion());
            
            sender.sendMessage("WorldBorder module "
                    + (worldBorder.isEnabled() ? "enabled" : "disabled")
                    + ", detected version " + worldBorder.getVersion());

            sender.sendMessage("WorldGuard module "
                    + (worldGuard.isEnabled() ? "enabled" : "disabled")
                    + ", detected version " + worldGuard.getVersion());
        }
		else if( args[0].startsWith("backup") ) {
		    String errorMessage = backupUtil.backup();
		    if( errorMessage == null ) {
	            server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_DATA_BACKED_UP,
	                    "file", backupUtil.getBackupFile());
		    }
		    else {
		        sender.sendMessage(errorMessage);
		    }
		}
		else if( args[0].startsWith("restore") ) {
			if( args.length < 2 || (!"OVERWRITE".equals(args[1])) ) {
				server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_DATA_RESTORE_USAGE,
				        "file", backupUtil.getBackupFile());
			}
			else {
	            String errorMessage = backupUtil.restore();
	            if( errorMessage == null ) {
	                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_DATA_RESTORE_SUCCESS,
	                        "file", backupUtil.getBackupFile());
	            }
	            else {
	                sender.sendMessage(errorMessage);
	            }
			}
		}
		else {
			return false;
		}

		return true;
	}
	
//	private void printUsage(CommandSender p, Command command) {
//		util.sendMessage(p, command.getUsage());
//		
////		util.sendMessage(p, "Usage:");
////		util.sendMessage(p, "/"+getCommandName()+" reloadconfig - reload config files");
////		util.sendMessage(p, "/"+getCommandName()+" reloaddata - force reloading of plugin data from database");
////		util.sendMessage(p, "/"+getCommandName()+" backup - backup database to a file");
////		util.sendMessage(p, "/"+getCommandName()+" restore - restore database from a file");
//	}

}
