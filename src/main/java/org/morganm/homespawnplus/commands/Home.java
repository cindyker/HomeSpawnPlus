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

import javax.inject.Inject;

import org.morganm.homespawnplus.OldHSP;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.manager.WarmupRunner;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.Teleport;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyEngine;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.morganm.homespawnplus.util.HomeUtil;


/**
 * @author morganm
 *
 */
public class Home extends BaseCommand
{
	private static final String OTHER_WORLD_PERMISSION = OldHSP.BASE_PERMISSION_NODE + ".command.home.otherworld";
	private static final String NAMED_HOME_PERMISSION = OldHSP.BASE_PERMISSION_NODE + ".command.home.named";
	
	private StrategyEngine engine;
	private ConfigCore configCore;
	@Inject private Teleport teleport;
	@Inject private HomeUtil homeUtil;
	
	@Inject
	public void setStrategyEngine(StrategyEngine engine) {
	    this.engine = engine;
	}
	
	@Inject
	public void setConfigCore(ConfigCore configCore) {
	    this.configCore = configCore;
	}

	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_HOME_USAGE);
 	}
	
	@Override
	public boolean execute(final Player p, String[] args)
	{
		log.debug("home command called player={}, args={}", p, args);

		// this flag is used to determine whether the player influenced the outcome of /home
		// with an arg or whether it was purely determined by the default home strategy, so
		// that we know whether the OTHER_WORLD_PERMISSION perm needs to be checked
		boolean playerDirectedArg = false;
		
		final String warmupName = getWarmupName(null);
		String cooldownName = null;
		org.morganm.homespawnplus.entity.Home theHome = null;
		
		StrategyResult result = null;
		Location l = null;
		if( args.length > 0 ) {
			playerDirectedArg = true;
			String homeName = null;
			
			if( args[0].startsWith("w:") ) {
				if( !p.hasPermission(OTHER_WORLD_PERMISSION) ) {
				    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION) );
	    			return true;
				}
				
				String worldName = args[0].substring(2);
				theHome = homeUtil.getDefaultHome(p.getName(), worldName);
				if( theHome == null ) {
				    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_NO_HOME_ON_WORLD, "world", worldName) );
					return true;
				}
			}
			else {
				if( !p.hasPermission(NAMED_HOME_PERMISSION) ) {
				    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_NO_NAMED_HOME_PERMISSION) );
					return true;
				}
				
				result = engine.getStrategyResult(EventType.NAMED_HOME_COMMAND, p, args[0]);
				theHome = result.getHome();
				l = result.getLocation();
			}

			// no location yet but we have a Home object? grab it from there
			if( l == null && theHome != null ) {
				l = theHome.getLocation();
				homeName = theHome.getName();
			}
			else
				homeName = args[0];
			
			cooldownName = getCooldownName("home-named", homeName);
			
			if( l == null ) {
			    server.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_NAMED_HOME_FOUND, "name", homeName);
				return true;
			}
		}
		else {
			result = engine.getStrategyResult(EventType.HOME_COMMAND, p);
			theHome = result.getHome();
			l = result.getLocation();
		}
		
		log.debug("home command running cooldown check, cooldownName={}",cooldownName);
		if( !cooldownCheck(p, cooldownName) )
			return true;
		
		final StrategyContext context;
		if( result != null )
			context = result.getContext();
		else
			context = null;
		
    	if( l != null ) {
    		// make sure it's on the same world, or if not, that we have
    		// cross-world home perms. We only evaluate this check if the
    		// player gave input for another world; admin-directed strategies
    		// always allow cross-world locations regardless of permissions.
    		if( playerDirectedArg && !p.getWorld().getName().equals(l.getWorld().getName()) &&
    				!p.hasPermission(OTHER_WORLD_PERMISSION) ) {
    		    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION) );
    			return true;
    		}
    		
			if( hasWarmup(p, warmupName) ) {
	    		final Location finalL = l;
	    		final org.morganm.homespawnplus.entity.Home finalHome = theHome;
	    		final boolean finalIsNamedHome = playerDirectedArg;
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;
					private String cdName;
					private String wuName;
					
					public void run() {
						if( !canceled ) {
						    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", "home") );
							doHomeTeleport(p, finalL, cdName, context,
									finalHome, finalIsNamedHome);
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
				}.setCooldownName(cooldownName).setWarmupName(warmupName));
			}
			else {
				doHomeTeleport(p, l, cooldownName, context, theHome, playerDirectedArg);
			}
    	}
    	else
    	    p.sendMessage( server.getLocalizedMessage(HSPMessages.NO_HOME_FOUND) );
    	
		return true;
	}
	
	/** Do a teleport to the home including costs, cooldowns and printing
	 * departure and arrival messages. Is used from both warmups and sync /home.
	 * 
	 * @param p
	 * @param l
	 */
	private void doHomeTeleport(Player p, Location l, String cooldownName,
			StrategyContext context, org.morganm.homespawnplus.entity.Home home,
			boolean isNamedHome)
	{
		String homeName = null;
		if( home != null )
			homeName = home.getName();
		
		if( applyCost(p, true, cooldownName) ) {
		    if( configCore.isTeleportMessages() ) {
    			if( home != null && home.isBedHome() )
    			    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_BED_TELEPORTING,
	    					"home", homeName) );
    			else if( isNamedHome )
    			    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_NAMED_TELEPORTING,
	    					"home", homeName) );
    			else
    			    p.sendMessage( server.getLocalizedMessage(HSPMessages.CMD_HOME_TELEPORTING,
	    					"home", homeName) );
    		}
    		
    		teleport.teleport(p, l, context.getTeleportOptions());
		}
	}
	
	private String getWarmupName(String homeName) {
		return getCommandName();

		/* warmup per home doesn't even make sense. Silly.
		 * 
		if( homeName != null && plugin.getHSPConfig().getBoolean(ConfigOptions.WARMUP_PER_HOME, false) )
			return getCommandName() + "." + homeName;
		else
			return getCommandName();
			*/
	}
}
