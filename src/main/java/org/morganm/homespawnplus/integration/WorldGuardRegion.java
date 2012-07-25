/**
 * 
 */
package org.morganm.homespawnplus.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jline.internal.Log;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.integration.worldguard.RegionEnterEvent;
import org.morganm.homespawnplus.integration.worldguard.RegionEvent;
import org.morganm.homespawnplus.integration.worldguard.RegionExitEvent;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

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
	/* Set to keep track of any "global" registers that weren't tied to a specific world, so
	 * we can lookup the original strategies that way.
	 */
	private final Set<String> globalRegisters = new HashSet<String>();
	
	public WorldGuardRegion(HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	public void registerRegion(World world, String regionName) {
		// if world argument is null, invoke no-argument version of this method,
		// which will register on all worlds
		if( world == null ) {
			registerRegion(regionName);
			return;
		}
		
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
	/** Given only a regionName, register that region for any world it is on.
	 * 
	 * @param regionName
	 */
	public void registerRegion(String regionName) {
		globalRegisters.add(regionName);
		List<World> worlds = Bukkit.getWorlds();
		for(World world : worlds) {
			registerRegion(world, regionName);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
        // If we didn't move a block, don't do anything
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
        	return;
        }
        
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
					RegionExitEvent regionEvent = new RegionExitEvent(region.getId(), fromWorld, event.getPlayer(), to);
					plugin.getServer().getPluginManager().callEvent(regionEvent);
					event.setTo(regionEvent.getTo());
					break;	// stop processing once we've found one
				}
			}
		}
		
		set = registered.get(toWorld);
		if( set != null ) {
			for(ProtectedRegion region : set) {
				// are we entering the region?
				if( region.contains(toVector) && !region.contains(fromVector) ) {
					RegionEnterEvent regionEvent = new RegionEnterEvent(region.getId(), toWorld, event.getPlayer(), to);
					plugin.getServer().getPluginManager().callEvent(regionEvent);
					event.setTo(regionEvent.getTo());
					break;	// stop processing once we've found one
				}
			}
		}
	}
	
	private Location processEvent(RegionEvent e, EventType baseEventType) {
		String regionName = e.getRegionName();
		String worldName = "," + e.getRegionWorldName();
		if( globalRegisters.contains(regionName) )
			worldName = "";
		
		String eventType = baseEventType.toString() + regionName + worldName;
		StrategyContext context = new StrategyContext(plugin);
		context.setEventType(eventType);
		context.setPlayer(e.getPlayer());
		StrategyResult result = plugin.getStrategyEngine().evaluateStrategies(context);
		if( result != null && result.getLocation() != null )
			return result.getLocation();
		else
			return null;
	}
	
	@EventHandler
	public void onRegionExit(RegionExitEvent e) {
		Location l = processEvent(e, EventType.EXIT_REGION);
		if( l != null ) {
			Log.debug("onRegionExit(): setting location to ",l);
			e.setTo(l);
		}
	}
	
	@EventHandler
	public void onRegionEnter(RegionEnterEvent e) {
		Location l = processEvent(e, EventType.ENTER_REGION);
		if( l != null ) {
			Log.debug("onRegionEnter(): setting location to ",l);
			e.setTo(l);
		}
	}
	
    public void registerEvents() {
    	PluginManager pm = plugin.getServer().getPluginManager();
    	pm.registerEvents(this, plugin);

    	/*
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
		        */
    }
	
}
