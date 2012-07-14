/**
 * 
 */
package org.morganm.homespawnplus.integration;

import org.bukkit.event.Listener;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.util.Debug;

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
	final HomeSpawnPlus plugin;
	final MultiverseSafeTeleporter teleporter;
	
	public MultiverseListener(final HomeSpawnPlus plugin, final MultiverseSafeTeleporter teleporter) {
		this.plugin = plugin;
		this.teleporter = teleporter;
	}
	
	public void onMultiverseTeleport(MVTeleportEvent event) {
		if(event.isCancelled())
			return;
		
		Debug.getInstance().debug("onMultiverseTeleport(): setting entity to ",event.getTeleportee());
		plugin.getMultiverseIntegration().setCurrentTeleporter(event.getTeleportee().getName());
	}
	
	public void onMultiversePortalEvent(MVPortalEvent event) {
		if(event.isCancelled())
			return;
		
		Debug.getInstance().debug("onMultiversePortalEvent(): setting entity to ",event.getTeleportee());
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
