/**
 * 
 */
package org.morganm.homespawnplus.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.integration.worldguard.RegionExitEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/** Class to monitor for enter/exit to registered WorldGuard regions.
 * 
 * @author morganm
 *
 */
public class WorldGuardRegion implements Listener {
	private final HomeSpawnPlus plugin;
	private final Map<String, Set<ProtectedRegion>> registered = new HashMap<String, Set<ProtectedRegion>>();
	
	public WorldGuardRegion(HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	public void registerRegion(World world, String regionName) {
		final String worldName = world.getName();
		
		WorldGuardInterface wg = plugin.getWorldGuardIntegration().getWorldGuardInterface();
		ProtectedRegion region = wg.getWorldGuardRegion(world, regionName);
		if( region != null ) {
			Set<ProtectedRegion> set = registered.get(worldName);
			if( set == null ) {
				set = new HashSet<ProtectedRegion>();
				registered.put(worldName, set);
			}
			
			set.add(region);
		}
	}
	
	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		Vector toVector = new Vector(to.getX(), to.getY(), to.getZ());
		String toWorld = to.getWorld().getName();
		
		Location from = event.getFrom();
		Vector fromVector = new Vector(from.getX(), from.getY(), from.getZ());
		String fromWorld = from.getWorld().getName();
		
		Set<ProtectedRegion> set = registered.get(fromWorld);
		if( set != null ) {
			for(ProtectedRegion region : set) {
				// are we leaving the region?
				if( region.contains(fromVector) && !region.contains(toVector) ) {
					RegionExitEvent regionEvent = new RegionExitEvent(region.getId(), event.getPlayer());
					plugin.getServer().getPluginManager().callEvent(regionEvent);
				}
			}
		}
		
		registered.get(toWorld);
	}
	
    public void registerEvents() {
    	PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(PlayerRespawnEvent.class,
        		this,
        		EventPriority.NORMAL,
        		new EventExecutor() {
        			public void execute(Listener listener, Event event) throws EventException {
        				try {
        					onPlayerMove((PlayerMoveEvent) event);
        				} catch (Throwable t) {
        					throw new EventException(t);
        				}
        			}
		        },
		        plugin);
    }
	
}
