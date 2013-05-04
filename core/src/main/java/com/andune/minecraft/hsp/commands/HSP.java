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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Scheduler;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Initializer;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.integration.dynmap.DynmapModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCore;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortals;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorder;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
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
    @Inject Scheduler scheduler;
    
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
        // admin command to clean up any playerName-case-caused dups
        else if( args[0].startsWith("dedup") || args[0].equals("dd") ) {
            sender.sendMessage("Starting async HSP database home playerName dup cleanup");
            scheduler.scheduleAsyncDelayedTask(new DeDupDatabaseRunner(sender), 0);
        }
        // admin command to switch all database player names to lowercase
        else if( args[0].startsWith("lowercase") || args[0].equals("lc") ) {
            sender.sendMessage("Starting async HSP database playerName-to-lowercase conversion");
            scheduler.scheduleAsyncDelayedTask(new LowerCaseDatabaseRunner(sender), 0);
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

    private class DeDupDatabaseRunner implements Runnable {
        private CommandSender sender;
        
        public DeDupDatabaseRunner(CommandSender sender) {
            this.sender = sender;
        }
        
        public void run() {
            log.debug("DeDupDatabaseRunner running");
            int dupsCleaned=0;
            try {
                storage.setDeferredWrites(true);
                final HomeDAO homeDAO = storage.getHomeDAO();

                // we fix dups by player, so we can keep the most recent home in the
                // event of duplicates. So we keep track of player homes we have fixed
                // so we can skip any others we come across as we're iterating the
                // allHomes hash.
                HashSet<String> playersFixed = new HashSet<String>(100);

                Set<? extends com.andune.minecraft.hsp.entity.Home> allHomes = homeDAO.findAllHomes();
                for(com.andune.minecraft.hsp.entity.Home home : allHomes) {
                    final String lcPlayerName = home.getPlayerName().toLowerCase();
                    if( playersFixed.contains(lcPlayerName) )
                        continue;
                    log.debug("home check for home name {}",lcPlayerName);

                    final HashMap<String, com.andune.minecraft.hsp.entity.Home> dupCheck = new HashMap<String, com.andune.minecraft.hsp.entity.Home>();
                    Set<? extends com.andune.minecraft.hsp.entity.Home> playerHomes = homeDAO.findHomesByPlayer(lcPlayerName);
                    // look for duplicates and delete all but the newest if we find any
                    for(com.andune.minecraft.hsp.entity.Home playerHome : playerHomes) {
                        final String homeName = playerHome.getName();
                        log.debug("dup check for home name \"{}\"", homeName);
                        // ignore no-name homes, they don't have to be unique
                        if( homeName == null )
                            continue;

                        // have we seen this home before?
                        com.andune.minecraft.hsp.entity.Home dup = dupCheck.get(homeName);
                        if( dup != null ) {
                            log.debug("found dup for home {}",homeName);
                            // determine which one is oldest and delete the oldest one
                            if( dup.getLastModified().getTime() < playerHome.getLastModified().getTime() ) {
                                // dup is oldest, delete it
                                log.info("Deleting oldest duplicate home (id "+dup.getId()+", name "+dup.getName()+") for player "+lcPlayerName);
                                homeDAO.deleteHome(dup);
                                dupCheck.put(homeName, playerHome); // record new record in our dup hash
                            }
                            else {
                                // playerHome is oldest, delete it
                                log.info("Deleting oldest duplicate home (id "+playerHome.getId()+", name "+playerHome.getName()+") for player "+lcPlayerName);
                                homeDAO.deleteHome(playerHome);
                            }
                            dupsCleaned++;
                        }
                        else {
                            log.debug("no dup found for home {}",homeName);
                            dupCheck.put(homeName, playerHome); // we have now, record it
                        }
                    }

                    playersFixed.add(lcPlayerName);
                }

                storage.flushAll();
            }
            catch(StorageException e) {
                log.error("Caught exception processing /hsp dedup", e);
            }
            finally {
                storage.setDeferredWrites(false);
            }

            sender.sendMessage("Database playerName dups have been cleaned up. "+dupsCleaned+" total dups found and cleaned");
        }
    }
    
    private class LowerCaseDatabaseRunner implements Runnable {
        private CommandSender sender;
        
        public LowerCaseDatabaseRunner(CommandSender sender) {
            this.sender = sender;
        }
        
        public void run() {
            log.debug("LowerCaseDatabaseRunner running");
            int conversions=0;
            try {
                storage.setDeferredWrites(true);
                final HomeDAO homeDAO = storage.getHomeDAO();

                // we fix names by player, so we can keep the most recent home in the
                // event of duplicates. So we keep track of player homes we have fixed
                // so we can skip any others we come across as we're iterating the
                // allHomes hash.
                HashSet<String> playersFixed = new HashSet<String>(100);

                Set<? extends com.andune.minecraft.hsp.entity.Home> allHomes = homeDAO.findAllHomes();
                for(com.andune.minecraft.hsp.entity.Home home : allHomes) {
                    final String lcPlayerName = home.getPlayerName().toLowerCase();
                    if( playersFixed.contains(lcPlayerName) )
                        continue;
                    log.debug("home check for home name {}",lcPlayerName);

                    final Set<? extends com.andune.minecraft.hsp.entity.Home> playerHomes = homeDAO.findHomesByPlayer(lcPlayerName);
                    for(com.andune.minecraft.hsp.entity.Home playerHome : playerHomes) {
                        // set home playerName to lower case if it's not already
                        if( !lcPlayerName.equals(playerHome.getPlayerName()) ) {
                            log.info("Fixing playerName to lowerCase for home id "+playerHome.getId()+", home name "+playerHome.getName()+" for player "+lcPlayerName);
                            playerHome.setPlayerName(lcPlayerName);
                            homeDAO.saveHome(playerHome);
                            conversions++;
                        }
                    }

                    playersFixed.add(lcPlayerName);
                }

                storage.flushAll();
            }
            catch(StorageException e) {
                log.error("Caught exception processing /hsp lc conversion", e);
            }
            finally {
                storage.setDeferredWrites(false);
            }

            sender.sendMessage("Database playerNames converted to lowerCase complete. Processed "+conversions+" conversions");
        }
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
