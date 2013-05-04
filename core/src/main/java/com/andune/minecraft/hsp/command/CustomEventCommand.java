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
package com.andune.minecraft.hsp.command;

import javax.inject.Inject;


import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.manager.WarmupRunner;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyResult;

/**
 * @author andune
 *
 */
public class CustomEventCommand extends BaseCommand {
    @Inject private Teleport teleport;
	@Inject private StrategyEngine engine;
	
	public String getEvent() {
		return super.getStringParam("event");
	}
	
	public boolean getNoArg() {
		Object o = super.getParam("noArg");
		if( o instanceof Boolean )
			return (Boolean) o;
		else
			return false;		// default is false
	}

	@Override
	public boolean execute(final Player p, final String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		String event = getEvent();
		if( event == null ) {
			p.sendMessage("There was an error, please report it to your administrator");
			log.warn("CustomChainCommand event is null, cannot execute");
			return true;
		}
		
		// pass along an argument, if there was one (if allowed)
		String arg = null;
		if( args.length > 0 && !getNoArg() )
			arg = args[0];
		
		final StrategyResult result = engine.getStrategyResult(event, p, arg);
		final Location l = result.getLocation();
		if( l == null ) {
			server.sendLocalizedMessage(p, HSPMessages.NO_LOCATION_FOUND);
			return true;
		}
		
		final String cooldownName = getCooldownName(getCommandName(), null);
		
		if( hasWarmup(p, cooldownName) ) {	// warmup name is same as cooldown
			doWarmup(p, new WarmupRunner() {
				private boolean canceled = false;
				private String cdName;
				private String wuName;
				
				public void run() {
					if( !canceled ) {
					    server.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
								"name", getWarmupName(), "place", l.shortLocationString());
						if( applyCost(p, true, cdName) )
			                teleport.teleport(p, l, result.getContext().getTeleportOptions());
					}
				}

				public void cancel() {
					canceled = true;
				}

				public void setPlayerName(String playerName) {}
				public void setWarmupId(int warmupId) {}
				public WarmupRunner setCooldownName(String cd) { cdName = cd; return this; }
				public WarmupRunner setWarmupName(String warmupName) { wuName = warmupName; return this; }
				public String getWarmupName() { return wuName; }
			}.setCooldownName(cooldownName).setWarmupName(cooldownName));
		}
		else {
			if( applyCost(p, true, cooldownName) )
	            teleport.teleport(p, l, result.getContext().getTeleportOptions());
		}

		return true;
	}
}
