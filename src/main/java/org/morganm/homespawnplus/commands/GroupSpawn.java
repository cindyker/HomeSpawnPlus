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
import org.bukkit.command.Command;
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
public class GroupSpawn extends BaseCommand
{
	private static final String OTHER_GROUPSPAWN_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.groupspawn.named";
	
	@Override
	public String[] getCommandAliases() { return new String[] {"gs"}; }
	
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_GROUPSPAWN_USAGE);
	}

	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		if( !isEnabled() || !hasPermission(p) )
			return true;

		String cooldownName = "groupspawn";
		
		String groupName = plugin.getPlayerGroup(p.getWorld().getName(), p.getName());
		
		StrategyResult result = null;
		Location l = null;
		if( args.length > 0 ) {
			groupName = args[0];
			
			if( plugin.hasPermission(p, OTHER_GROUPSPAWN_PERMISSION) ) {
				org.morganm.homespawnplus.entity.Spawn spawn = util.getGroupSpawn(groupName, p.getWorld().getName());
				cooldownName = getCooldownName("groupspawn-named", groupName);
				if( spawn != null )
					l = spawn.getLocation();
				
				if( l == null ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_GROUPSPAWN_NO_GROUPSPAWN_FOR_GROUP,
							"group", args[0]);
					return true;
				}
			}
			else {
				util.sendLocalizedMessage(p, HSPMessages.NO_PERMISSION);
			}
		}
		else {
			result = plugin.getStrategyEngine().getStrategyResult(EventType.GROUPSPAWN_COMMAND, p);
			if( result != null )
				l = result.getLocation();
		}

		if( !cooldownCheck(p, cooldownName) )
			return true;
    	
		if( l == null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_GROUPSPAWN_NO_GROUPSPAWN_FOR_GROUP,
					"group", plugin.getPlayerGroup(p.getWorld().getName(), p.getName()));
			
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
						util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
								"name", getWarmupName(), "place", "group spawn");
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
    		if( plugin.getConfig().getBoolean(ConfigOptions.TELEPORT_MESSAGES, false) ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_GROUPSPAWN_TELEPORTING,
						"group", groupName);
    		}
    		
    		util.teleport(p, l, TeleportCause.COMMAND, context);
		}
	}
}
