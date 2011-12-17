/**
 * 
 */
package org.morganm.homespawnplus.listener;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.WarmupManager;

/**
 * @author morganm
 *
 */
public class HSPEntityListener extends EntityListener {
	private WarmupManager warmupManager;
	
	public HSPEntityListener(HomeSpawnPlus plugin) {
		this.warmupManager = plugin.getWarmupmanager();
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		warmupManager.processEntityDamage(event);
	}
}
