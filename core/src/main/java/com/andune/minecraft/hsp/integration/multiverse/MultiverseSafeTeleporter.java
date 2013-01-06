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
package com.andune.minecraft.hsp.integration.multiverse;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
    
	private final MultiverseCore multiverse;
	private final MultiverseModule multiverseModule;
	
    private SafeTTeleporter original;
	
	public MultiverseSafeTeleporter(MultiverseCore multiverse, MultiverseModule multiverseModule) {
		this.multiverse = multiverse;
		this.multiverseModule = multiverseModule;
	}
	
	public void install() {
		original = multiverse.getCore().getSafeTTeleporter();
		multiverse.getCore().setSafeTTeleporter(this);
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

	@Override
	public TeleportResult safelyTeleport(CommandSender teleporter,
			Entity teleportee, MVDestination d)
	{
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoked (#1)");
		
		Player p = null;
		if( teleportee instanceof Player )
			p = (Player) teleportee;
		
		if( p != null )
		    multiverseModule.setCurrentTeleporter(p.getName());
		
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoking Multiverse. teleportee={}, p={}", teleportee, p);
		// let Multiverse do it's business
		TeleportResult result = original.safelyTeleport(teleporter, teleportee, d);
		if( teleportee != null ) {
			log.debug("MultiverseSafeTeleporter() safelyTelport() post-Multiverse location={}",teleportee.getLocation());
		}

		return result;
	}

	@Override
	public TeleportResult safelyTeleport(CommandSender teleporter,
			Entity teleportee, Location location, boolean safely)
	{
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoked (#2)");
		
		Player p = null;
		if( teleportee instanceof Player )
			p = (Player) teleportee;
		
		if( p != null )
		    multiverseModule.setCurrentTeleporter(p.getName());
		
		log.debug("MultiverseSafeTeleporter() safelyTelport() invoking Multiverse. teleportee={}, p={}", teleportee, p);
		// let Multiverse do it's business
		TeleportResult result = original.safelyTeleport(teleporter, teleportee, location, safely);
		if( teleportee != null ) {
			log.debug("MultiverseSafeTeleporter() safelyTelport() post-Multiverse location={}",teleportee.getLocation());
		}

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
