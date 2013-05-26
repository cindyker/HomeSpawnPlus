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


import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.manager.WarmupRunner;
import com.andune.minecraft.hsp.strategy.EventType;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyResult;

/**
 * @author andune
 *
 */
@UberCommand(uberCommand="spawn", subCommand="",
    aliases={"s"}, help="Go to spawn")
public class Spawn extends BaseCommand
{
    @Inject private StrategyEngine engine;
    @Inject private ConfigCore configCore;
    @Inject private Teleport teleport;
	
	@Override
	public boolean execute(final Player p, String[] args) {
		String cooldownName = "spawn";
		log.debug("/spawn command run by player {}", p);
		
		boolean isNamedSpawn=false;
		Location l = null;
		StrategyResult result = null;
		if( args.length > 0 ) {
			boolean hasPermission = false;
			if( permissions.hasSpawnNamed(p, null) ) {
				isNamedSpawn = true;
				com.andune.minecraft.hsp.entity.Spawn spawn = null;
				result = engine.getStrategyResult(EventType.NAMED_SPAWN_COMMAND, p, args[0]);
				if( result != null ) {
					l = result.getLocation();
					spawn = result.getSpawn();
				}
				
				if( l == null ) {
					p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_SPAWN_NO_SPAWN_FOUND, "name", args[0]) );
					return true;
				}
				
				// if we check named permissions individually, then check now
				if( configCore.isSpawnNamedPermissions() ) {
					if( permissions.hasSpawnNamed(p, spawn.getName().toLowerCase()) )
							hasPermission = true;
				}
				// otherwise they have permission since we already checked the base permission above
				else
					hasPermission = true;
			}
			
			if( !hasPermission ) {
				p.sendMessage( server.getLocalizedMessage(HSPMessages.NO_PERMISSION) );
				return true;
			}
		}
		else {
			result = engine.getStrategyResult(EventType.SPAWN_COMMAND, p);
			if( result != null ) {
				l = result.getLocation();
			}
		}
		
		final StrategyContext context;
		if( result != null )
			context = result.getContext();
		else
			context = null;
    	
		if( !cooldownCheck(p, cooldownName) )
			return true;
    	
    	if( l != null ) {
    		String spawnName = null;
    		if( result != null && result.getSpawn() != null )
    			spawnName = result.getSpawn().getName();
    		
			if( hasWarmup(p) ) {
	    		final Location finalL = l;
	    		final String finalSpawnName = spawnName;
	    		final boolean finalIsNamedSpawn = isNamedSpawn;
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;
					private String wuName = getCommandName();

					public void run() {
						if( !canceled ) {
							p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", "spawn") );
							doSpawnTeleport(p, finalL, context, finalSpawnName, finalIsNamedSpawn);
						}
					}

					public void cancel() {
						canceled = true;
					}

					public void setPlayerName(String playerName) {}
					public void setWarmupId(int warmupId) {}
					public WarmupRunner setWarmupName(String warmupName) { wuName = warmupName; return this; }
					public String getWarmupName() { return wuName; }
				});
			}
			else {
				doSpawnTeleport(p, l, context, spawnName, isNamedSpawn);
			}
    	}
    	else
    		log.warn("ERROR; not able to find a spawn location");
    	
		return true;
	}
	
	/** Do a teleport to the spawns including costs, cooldowns and printing
	 * departure and arrival messages. Is used from both warmups and sync /spawn.
	 * 
	 * @param p
	 * @param l
	 */
	private void doSpawnTeleport(Player p, Location l, StrategyContext context,
			String spawnName, boolean isNamedSpawn) {
		if( applyCost(p, true) ) {
		    if( configCore.isTeleportMessages() ) {
    			if( isNamedSpawn )
        			p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_SPAWN_NAMED_TELEPORTING,
        					"spawn", spawnName) );
    			else
    				p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_SPAWN_TELEPORTING,
    						"spawn", spawnName) );
    		}
    		
		    teleport.teleport(p, l, context.getTeleportOptions());
		}
	}
}
