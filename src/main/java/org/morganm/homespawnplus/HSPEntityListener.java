/**
 * 
 */
package org.morganm.homespawnplus;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

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
