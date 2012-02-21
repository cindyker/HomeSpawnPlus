/**
 * 
 */
package org.morganm.homespawnplus.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.WarmupManager;

/**
 * @author morganm
 *
 */
public class HSPEntityListener implements Listener {
	private WarmupManager warmupManager;
	
	public HSPEntityListener(HomeSpawnPlus plugin) {
		this.warmupManager = plugin.getWarmupmanager();
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		warmupManager.processEntityDamage(event);
	}
}
