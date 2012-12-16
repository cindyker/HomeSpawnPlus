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
package org.morganm.homespawnplus.integration.multiverse;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.homespawnplus.OldHSP;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.enums.TeleportResult;

/**
 * @author morganm
 *
 */
public class MultiverseSafeTeleporter implements SafeTTeleporter {
    private static final Logger log = LoggerFactory.getLogger(MultiverseSafeTeleporter.class);
    
	private final OldHSP hsp;
	private final MultiverseCore multiverse;
	private SafeTTeleporter original;
	
	public MultiverseSafeTeleporter(OldHSP hsp, MultiverseCore multiverse) {
		this.hsp = hsp;
		this.multiverse = multiverse;
	}
	
	public void install() {
		original = multiverse.getCore().getSafeTTeleporter();
		multiverse.getCore().setSafeTTeleporter(this);
	
		// turns out we have to hook TeleportCommand since it gets and keeps
		// a reference to safeTeleporter and therefore ignores ours.
//		TeleportCommand tpCmd = 
	}
	
	public void uninstall() {
		multiverse.getCore().setSafeTTeleporter(original);
	}
	
	@Override
	public Location getSafeLocation(Location l) {
		return original.getSafeLocation(l);
	}

	@Override
	public Location getSafeLocation(Location l, int tolerance, int radius) {
		return original.getSafeLocation(l, tolerance, radius);
	}

	/** Process HSP strategies related to a multiverse teleport event.
	 * 
	 * @param teleportee
	 * @param from
	 */
	public Location hspEvent(final Player teleportee, final Location from, boolean doTeleport) {
		if( teleportee == null )
			return null;
		
		log.debug("in hspEvent");
		
		Location finalLoc = null;
		
		// be safe to make sure we never interfere with Multiverse teleport
		try {
			final Location newLoc = teleportee.getLocation();
			finalLoc = newLoc;	// default finalLoc is what MV set, unless we change it below
			Location to = teleportee.getLocation();

			EventType eventType = null;
	    	if( from == null || !newLoc.getWorld().equals(from.getWorld()) )	// cross-world teleport event?
	    		eventType = EventType.MULTIVERSE_TELEPORT_CROSSWORLD;
    		else
	    		eventType = EventType.MULTIVERSE_TELEPORT;
	    	
			final StrategyContext context = new StrategyContext(hsp);
	    	context.setPlayer(teleportee);
	    	context.setEventType(eventType.toString());
	    	context.setLocation(to);
			StrategyResult result = hsp.getStrategyEngine().evaluateStrategies(context);
			
			if( result != null && result.getLocation() != null ) {
				finalLoc = result.getLocation();
				
				// if HSP strategies gave us a new location, teleport the player there now
				if( !finalLoc.equals(newLoc) ) {
					if( doTeleport ) {
						hsp.getMultiverseIntegration().setCurrentTeleporter(null);
						Teleport.getInstance().setCurrentTeleporter(teleportee.getName());
						hsp.getUtil().teleport(teleportee, result.getLocation(), TeleportCause.PLUGIN, context);
					}
				}
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		
		return finalLoc;
	}
	
	@Override
	public TeleportResult safelyTeleport(CommandSender teleporter,
			Entity teleportee, MVDestination d)
	{
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoked (#1)");
		
//		Location from = null;
//		if( teleportee != null )
//			from = teleportee.getLocation();
		
		Player p = null;
		if( teleportee instanceof Player )
			p = (Player) teleportee;
		
		if( p != null )
			hsp.getMultiverseIntegration().setCurrentTeleporter(p.getName());
		
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoking Multiverse. teleportee={}, p={}", teleportee, p);
		// let Multiverse do it's business
		TeleportResult result = original.safelyTeleport(teleporter, teleportee, d);
		if( teleportee != null ) {
			log.debug("MultiverseSafeTeleporter() safelyTelport() post-Multiverse location={}",teleportee.getLocation());
		}

		// no longer necessary, HSP will detect the teleport and do it's own processing based on
		// the setup information above
		// now give HSP a chance to do something else
//		if( p != null ) {
//			log.debug("MultiverseSafeTeleporter() safelyTelport() invoking HSP strategies");
//			hspEvent(p, from, true);
//			log.debug("MultiverseSafeTeleporter() safelyTelport() post-HSP location =",p.getLocation());
//		}
		
		return result;
	}

	@Override
	public TeleportResult safelyTeleport(CommandSender teleporter,
			Entity teleportee, Location location, boolean safely)
	{
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoked (#2)");
		
//		Location from = null;
//		if( teleportee != null )
//			from = teleportee.getLocation();
		
		Player p = null;
		if( teleportee instanceof Player )
			p = (Player) teleportee;
		
		if( p != null )
			hsp.getMultiverseIntegration().setCurrentTeleporter(p.getName());
		
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoking Multiverse. teleportee={}, p={}", teleportee, p);
		// let Multiverse do it's business
		TeleportResult result = original.safelyTeleport(teleporter, teleportee, location, safely);
		if( teleportee != null ) {
			log.debug("MultiverseSafeTeleporter() safelyTelport() post-Multiverse location={}",teleportee.getLocation());
		}

		// no longer necessary, HSP will detect the teleport and do it's own processing based on
		// the setup information above
		// now give HSP a chance to do something else
//		if( p != null ) {
//			log.debug("MultiverseSafeTeleporter() safelyTelport() invoking HSP strategies");
//			hspEvent(p, from, true);
//			log.debug("MultiverseSafeTeleporter() safelyTelport() post-HSP location =",p.getLocation());
//		}
		
		return result;
	}

	@Override
	public Location getSafeLocation(Entity e, MVDestination d) {
		return original.getSafeLocation(e, d);
	}

	@Override
	public Location findPortalBlockNextTo(Location l) {
		return original.findPortalBlockNextTo(l);
	}

}
