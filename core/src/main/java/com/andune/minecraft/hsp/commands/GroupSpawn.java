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
import com.andune.minecraft.commonlib.server.api.PermissionSystem;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.manager.WarmupRunner;
import com.andune.minecraft.hsp.strategy.EventType;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.util.SpawnUtil;


/**
 * @author andune
 *
 */
@UberCommand(uberCommand="spawn", subCommand="group",
    aliases={"g"}, help="Go to your group spawn")
public class GroupSpawn extends BaseCommand
{
	@Inject private StrategyEngine engine;
	@Inject private Teleport teleport;
    @Inject private ConfigCore configCore;
    @Inject private PermissionSystem permSystem;
    @Inject private SpawnUtil util;
	
	@Override
	public String[] getCommandAliases() { return new String[] {"gs"}; }
	
	@Override
	public String getUsage() {
		return	server.getLocalizedMessage(HSPMessages.CMD_GROUPSPAWN_USAGE);
	}

	@Override
	public boolean execute(final Player p, final String[] args) {
		String cooldownName = "groupspawn";
		
		String groupName = permSystem.getPlayerGroup(p.getWorld().getName(), p.getName());
		
		StrategyResult result = null;
		Location l = null;
		if( args.length > 0 ) {
			groupName = args[0];

			if( permissions.hasOtherGroupSpawnPermission(p) ) {
				Spawn spawn = util.getGroupSpawn(groupName, p.getWorld().getName());
				cooldownName = getCooldownName("groupspawn-named", groupName);
				if( spawn != null )
					l = spawn.getLocation();
				
				if( l == null ) {
				    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_GROUPSPAWN_NO_GROUPSPAWN_FOR_GROUP,
							"group", args[0]) );
					return true;
				}
			}
			else {
				server.sendLocalizedMessage(p, HSPMessages.NO_PERMISSION);
			}
		}
		else {
			result = engine.getStrategyResult(EventType.GROUPSPAWN_COMMAND, p);
			if( result != null )
				l = result.getLocation();
		}

		if( !cooldownCheck(p, cooldownName) )
			return true;
    	
		if( l == null ) {
		    server.sendLocalizedMessage(p, HSPMessages.CMD_GROUPSPAWN_NO_GROUPSPAWN_FOR_GROUP,
					"group", groupName);
			
			return true;
		}
		
		final StrategyContext context;
		if( result != null )
			context = result.getContext();
		else
			context = null;

		if( hasWarmup(p) ) {
    		final Location finalL = l;
    		final String finalGroupName = groupName;
			doWarmup(p, new WarmupRunner() {
				private boolean canceled = false;
				private String wuName = getCommandName();

				public void run() {
					if( !canceled ) {
					    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_WARMUP_FINISHED,
								"name", getWarmupName(), "place", "group spawn") );
						doTeleport(p, finalL, context, finalGroupName);
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
			doTeleport(p, l, context, groupName);
		}
		
		return true;
	}

	/** Do the teleport including costs, cooldowns and displaying teleport
	 * messages. Is used from both warmups and sync call.
	 * 
	 * @param p
	 * @param l
	 */
	private void doTeleport(Player p, Location l, StrategyContext context,
			String groupName) {
		if( applyCost(p, true) ) {
		    if( configCore.isTeleportMessages() ) {
    		    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_GROUPSPAWN_TELEPORTING,
						"group", groupName) );
    		}
    		
    		teleport.teleport(p, l, context.getTeleportOptions());
		}
	}
}
