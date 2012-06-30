/**
 * 
 */
package org.morganm.homespawnplus.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.morganm.homespawnplus.HomeSpawnPlus;

/**
 * @author morganm
 *
 */
public class HSPEntityListener implements Listener {
	private final HomeSpawnPlus plugin;
	
	public HSPEntityListener(HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		plugin.getWarmupmanager().processEntityDamage(event);
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if( event.getEntity() instanceof Player ) {
			plugin.getCooldownManager().onDeath((Player)event.getEntity());
		}
	}
}
