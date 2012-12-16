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

import org.bukkit.event.Listener;
import org.morganm.homespawnplus.OldHSP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.destination.PortalDestination;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;

/** Class incomplete, may not be necessary.
 * 
 * @author morganm
 *
 */
public class MultiverseListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(MultiverseListener.class);
    
	final OldHSP plugin;
	final MultiverseSafeTeleporter teleporter;
	
	public MultiverseListener(final OldHSP plugin, final MultiverseSafeTeleporter teleporter) {
		this.plugin = plugin;
		this.teleporter = teleporter;
	}
	
	public void onMultiverseTeleport(MVTeleportEvent event) {
		if(event.isCancelled())
			return;
		
		log.debug("onMultiverseTeleport(): setting entity to {}",event.getTeleportee());
		plugin.getMultiverseIntegration().setCurrentTeleporter(event.getTeleportee().getName());
	}
	
	public void onMultiversePortalEvent(MVPortalEvent event) {
		if(event.isCancelled())
			return;
		
		log.debug("onMultiversePortalEvent(): setting entity to {}",event.getTeleportee());
		MVPortal portal = event.getSendingPortal();
		if( portal != null )
			plugin.getMultiverseIntegration().setSourcePortalName(portal.getName());
		
		MVDestination destination = event.getDestination();
		if( destination != null && destination instanceof PortalDestination ) {
			PortalDestination portalDestination = (PortalDestination) destination;
			plugin.getMultiverseIntegration().setDestinationPortalName(portalDestination.getName());
		}
	}
}
