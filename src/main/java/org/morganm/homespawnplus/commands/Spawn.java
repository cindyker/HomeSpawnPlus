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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.manager.WarmupRunner;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;


/**
 * @author morganm
 *
 */
public class Spawn extends BaseCommand
{
	private static final String OTHER_SPAWN_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.spawn.named";
	
	/*
	@Override
	public String[] getCommandAliases() { return new String[] {"globalspawn"}; }
	*/

	@Override
	public boolean execute(final Player p, final org.bukkit.command.Command command, String[] args) {
		if( !isEnabled() || !hasPermission(p) )
			return true;

		String cooldownName = "spawn";
		debug.devDebug("/spawn command run by player ",p);
		
		boolean isNamedSpawn=false;
		Location l = null;
		StrategyResult result = null;
		if( args.length > 0 ) {
			boolean hasPermission = false;
			if( plugin.hasPermission(p, OTHER_SPAWN_PERMISSION) ) {
				isNamedSpawn = true;
				org.morganm.homespawnplus.entity.Spawn spawn = null;
				result = plugin.getStrategyEngine().getStrategyResult(EventType.NAMED_SPAWN_COMMAND, p, args[0]);
				if( result != null ) {
					l = result.getLocation();
					spawn = result.getSpawn();
				}
				
				if( l == null ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWN_NO_SPAWN_FOUND, "name", args[0]);
					return true;
				}
				
				// if we check named permissions individually, then check now
				if( plugin.getConfig().getBoolean(ConfigOptions.SPAWN_NAMED_PERMISSIONS, false) ) {
					if( plugin.hasPermission(p, OTHER_SPAWN_PERMISSION + "." + spawn.getName().toLowerCase()) )
							hasPermission = true;
				}
				// otherwise they have permission since we already checked the base permission above
				else
					hasPermission = true;
			}
			
			if( !hasPermission ) {
				util.sendLocalizedMessage(p, HSPMessages.NO_PERMISSION);
				return true;
			}
		}
		else {
			result = util.getStrategyResult(EventType.SPAWN_COMMAND, p);
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
							util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", "spawn");
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
    		HomeSpawnPlus.log.warning(HomeSpawnPlus.logPrefix + " ERROR; not able to find a spawn location");
    	
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
    		if( plugin.getConfig().getBoolean(ConfigOptions.TELEPORT_MESSAGES, false) ) {
    			if( isNamedSpawn )
        			util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWN_NAMED_TELEPORTING,
        					"spawn", spawnName);
    			else
    				util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWN_TELEPORTING,
    						"spawn", spawnName);
    		}
    		
    		util.teleport(p, l, TeleportCause.COMMAND, context);
		}
	}
}
