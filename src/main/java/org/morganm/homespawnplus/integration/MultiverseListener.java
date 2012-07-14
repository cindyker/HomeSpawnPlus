/**
 * 
 */
package org.morganm.homespawnplus.integration;

import org.bukkit.event.Listener;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.util.Debug;

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
		if(event.isCancelled())
			return;
		
		Debug.getInstance().debug("onMultiverseTeleport(): setting entity to ",event.getTeleportee());
		plugin.getMultiverseIntegration().setCurrentTeleporter(event.getTeleportee().getName());
	}
}
