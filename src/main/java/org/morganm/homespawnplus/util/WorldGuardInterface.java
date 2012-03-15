/**
 * 
 */
package org.morganm.homespawnplus.util;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/** This class exists to wrap WorldGuard functionality so that our plugin can
 * load/function without WorldGuard, since WorldGuard is not referenced in any
 * class but this one, and we take care to make this class a soft dependency
 * in any class that it is referenced from.
 * 
 * @author morganm
 *
 */
public class WorldGuardInterface {
	private static final Logger log = HomeSpawnPlus.log;
	private static boolean worldGuardError = false;
	
	private final String logPrefix;
	private final HomeSpawnPlus plugin;
	
	public WorldGuardInterface(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.logPrefix = HomeSpawnPlus.logPrefix;
//		this.SPAWN_PERM = new RegionGroupFlag("spawn-group", RegionGroupFlag.RegionGroup.MEMBERS);
	}
	
	/** This code adapted from WorldGuard class
	 *  com.sk89q.worldguard.bukkit.WorldGuardPlayerListener, method
	 *  onPlayerRespawn().
	 *  
	 *  This is because there is no API provided by WorldGuard to determine this externally
	 *  nor is there a reliable way for me to use Bukkit to call WorldGuard's onPlayerRespawn()
	 *  directly since HSP's use might not be in a respawn event (for example, HSP might be
	 *  using this strategy in a /spawn command).
	 *  
	 *  So I've had to duplicate/adapt the WorldGuard method directly into HSP in order to
	 *  accurately check whether or not WorldGuard would respond to the current location with
	 *  a region spawn.
	 *  
	 *  Code is current as of WorldGuard build #579 (WorldGuard 5.5.2), built Mar 12, 2012.
	 * 
	 * @param player
	 * @return
	 */
	public org.bukkit.Location getWorldGuardSpawnLocation(Player player) {
		Debug.getInstance().debug("getWorldGuardSpawnLocation(): player=",player);
		org.bukkit.Location loc = null;
		
		try {
			Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			if( p != null ) {
				WorldGuardPlugin worldGuard = (WorldGuardPlugin) p;
				org.bukkit.Location location = player.getLocation();
	
				ConfigurationManager cfg = worldGuard.getGlobalStateManager();
				WorldConfiguration wcfg = cfg.get(player.getWorld());
	
				Debug.getInstance().debug("getWorldGuardSpawnLocation(): location=",location,", wcfg=",wcfg);
		        if (wcfg.useRegions) {
		            Vector pt = toVector(location);
		            RegionManager mgr = worldGuard.getGlobalRegionManager().get(player.getWorld());
		            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
					Debug.getInstance().debug("getWorldGuardSpawnLocation(): wcfg.useRegion=true, set.size()=",set.size());

		            for(Iterator<ProtectedRegion> i = set.iterator(); i.hasNext();) {
		            	final ProtectedRegion region = i.next();
		            	final Location teleportLocation = region.getFlag(DefaultFlag.SPAWN_LOC);
		            	
		            	if( teleportLocation != null ) {
		            		org.bukkit.World world = Bukkit.getWorld(teleportLocation.getWorld().getName());
            				Vector pos = teleportLocation.getPosition();
		            		loc = new org.bukkit.Location(world, pos.getX(), pos.getY(), pos.getZ(),
		            				teleportLocation.getYaw(), teleportLocation.getPitch());
							Debug.getInstance().debug("getWorldGuardSpawnLocation(): found loc=",loc);
		            		break;
		            	}
		            }
		            
//		            LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
//		            Vector spawn = set.getFlag(DefaultFlag.SPAWN_LOC, localPlayer);
//
//					Debug.getInstance().debug("getWorldGuardSpawnLocation(): wcfg.useRegion=true, spawn=",spawn);
//		            if (spawn != null) {
//		                loc = BukkitUtil.toLocation(player.getWorld(), spawn);
//		            }
		        }
		        
		        /* old code for pre-5.5 Worldguard
				if (wcfg.useRegions) {
					Vector pt = com.sk89q.worldguard.bukkit.BukkitUtil.toVector(location);
					RegionManager mgr = worldGuard.getGlobalRegionManager().get(player.getWorld());
					ApplicableRegionSet set = mgr.getApplicableRegions(pt);
	
					Vector spawn = set.getFlag(DefaultFlag.SPAWN_LOC);
	
					if (spawn != null) {
						RegionGroup group = set.getFlag(DefaultFlag.SPAWN_PERM);
						Location spawnLoc = BukkitUtil.toLocation(player.getWorld(), spawn);
	
						if (group != null) {
							LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
	
							if (RegionGroupFlag.isMember(set, group, localPlayer)) {
								loc = spawnLoc;
							}
						} else {
							loc = spawnLoc;
						}
					}
				}
				*/
			}
		}
		catch(Exception e) {
			// we only print once to avoid spamming the log with errors, since this is possibly
			// a permanent condition (ie. admin chooses to run older version of WorldGuard that
			// this plugin is not compatible with)
			if( !worldGuardError ) {
				worldGuardError = true;
				log.warning(logPrefix + " Error trying to resolve WorldGuard spawn (this message will only print once): "+e.getMessage());
				e.printStackTrace();
			}
		}
		
		Debug.getInstance().debug("getWorldGuardSpawnLocation(): exit, loc=",loc);
		return loc;
	}
}
