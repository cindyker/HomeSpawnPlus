/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag.RegionGroup;
import com.sk89q.worldguard.protection.managers.RegionManager;

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
	}
	
	/** This code adapted from WorldGuard class
	 *  com.sk89q.worldguard.bukkit.WorldGuardPlayerList, method
	 *  onPlayerRespawn().
	 *  
	 *  This is because there is no API provided by WorldGuard to determine this externally
	 *  nor is there a reliable way for me to use Bukkit to call WorldGuard's onPlayerRespawn()
	 *  directly since HSP's use might not be in a respawn event (for example, HSP might be
	 *  using this strategy in a /spawn command).
	 *  
	 *  So I've had to duplicate/adapt the WorldGuard method directly into HSP in order to
	 *  accurately check whether or not WorldGuard would respond to the current location with
	 *  a region spawn. Code is current as of WorldGuard build #309, built Oct 29, 2011.
	 * 
	 * @param player
	 * @return
	 */
	public Location getWorldGuardSpawnLocation(Player player) {
		Location loc = null;
		
		try {
			Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			if( p != null ) {
				WorldGuardPlugin worldGuard = (WorldGuardPlugin) p;
				Location location = player.getLocation();
	
				ConfigurationManager cfg = worldGuard.getGlobalStateManager();
				WorldConfiguration wcfg = cfg.get(player.getWorld());
	
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
		
		return loc;
	}
}
