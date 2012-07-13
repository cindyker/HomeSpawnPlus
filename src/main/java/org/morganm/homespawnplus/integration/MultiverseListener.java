/**
 * 
 */
package org.morganm.homespawnplus.integration;

import org.bukkit.event.Listener;
import org.morganm.homespawnplus.HomeSpawnPlus;

import com.onarandombox.MultiverseCore.event.MVTeleportEvent;

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
//		Location to = event.getDestination().getLocation(event.getTeleportee());
//		Location result = teleporter.hspEvent(event.getTeleportee(), event.getFrom(),to, false);
		
//		if( result != null && !result.equals(to) )
//			; // do nothing
	}
}
