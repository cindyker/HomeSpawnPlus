/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.morganm.homespawnplus.config.Config;
import org.morganm.homespawnplus.config.ConfigOptions;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author morganm
 *
 */
public class WarmupManager {
	private static final Logger log = HomeSpawnPlus.log;
	private final String logPrefix;
	
	private static int uniqueWarmupId = 0;
	
	private final HashMap<Integer, PendingWarmup> warmupsPending;
	private final HashMap<String, List<PendingWarmup>> warmupsPendingByPlayerName;
	
	private final HomeSpawnPlus plugin;
	private final Config config;
	
	public WarmupManager(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();
		this.logPrefix = HomeSpawnPlus.logPrefix;
		
		warmupsPending = new HashMap<Integer, PendingWarmup>();
		warmupsPendingByPlayerName = new HashMap<String, List<PendingWarmup>>();
	}
	
    private boolean isExemptFromWarmup(Player p, String warmup) {
    	if( plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".WarmupExempt."+warmup) )
    		return true;
    	else
    		return false;
    }
    
	/** Check to see if a given warmup should be enforced for a given player.
	 * 
	 * @param p
	 * @param warmupName
	 * @return true if warmup should be enforced, flase if not
	 */
	public boolean hasWarmup(Player p, String warmupName) {
		if( config.getBoolean(ConfigOptions.USE_WARMUPS, false) &&
			getWarmupTime(warmupName) > 0 &&
			!isExemptFromWarmup(p, warmupName) )
		{
			return true;
		}
		else {
			return false;
		}
	}
	
	/** Utility method for making sure a warmup is not currently pending already.
	 * This method does not start the warmup timer, in order to actually start
	 * the warmup you need to use startWarmup().
	 * 
	 * @param p
	 * @param warmupName
	 * @return true if warmup is already pending, false if not
	 */
	public boolean isWarmupPending(String playerName, String warmupName) {
		boolean warmupPending = false;
		
		List<PendingWarmup> pendingWarmups = warmupsPendingByPlayerName.get(playerName);
		if( pendingWarmups != null ) {
			for(PendingWarmup warmup : pendingWarmups) {
				if( warmup.warmupName.equals(warmupName) ) {
					warmupPending = true;
					break;
				}
			}
		}
			
		return warmupPending;
	}
	
	public int getWarmupTime(String warmupName) {
		return plugin.getConfig().getInt(ConfigOptions.WARMUP_BASE + warmupName, 0);
	}
	
	/** Start a given warmup.  Return true if the warmup was started successfully, false if not.
	 * 
	 * @param playerName
	 * @param warmupName
	 * @param warmupRunnable
	 * @return
	 */
	public boolean startWarmup(String playerName, String warmupName, WarmupRunner warmupRunnable) {
		if( isWarmupPending(playerName, warmupName) ) {		// don't let two of the same warmups start
			return false;
		}
		
		Player p = plugin.getServer().getPlayer(playerName);
		if( p == null ) {
			log.warning(logPrefix + " startWarmup() found null player object for name "+playerName);
			return false;
		}
		
		int warmupId = 0;
		synchronized(this) {
			warmupId = ++uniqueWarmupId;
		}
		
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		if( playerWarmups == null ) {
			playerWarmups = new ArrayList<PendingWarmup>();
			warmupsPendingByPlayerName.put(playerName, playerWarmups);
		}
		
		int warmupTime = getWarmupTime(warmupName);
		
		PendingWarmup warmup = new PendingWarmup();
		warmup.warmupId = warmupId;
		warmup.playerName = playerName;
		warmup.warmupName = warmupName;
		warmup.runner = warmupRunnable;
		warmup.startTime = System.currentTimeMillis();
		warmup.warmupTime = warmupTime * 1000;
		warmup.playerLocation = p.getLocation();
		
		warmupRunnable.setWarmupId(warmupId);
		warmupRunnable.setPlayerName(playerName);
		
		// keep track of the warmups we have pending
		warmupsPending.put(warmupId, warmup);
		playerWarmups.add(warmup);
		
		// kick off a Bukkit scheduler to the Runnable for the timer given.  We run
		// every 20 ticks (average 20 ticks = 1 second), then check real clock time.
		//
		// This allows us two things: A) we can manage to real clock time without a
		// separate scheduling mechanism, thus if the warmupTime is 5 seconds, we'll
		// be pretty close to that even on a server running at only 10 TPS.
		// and B) it allows us to cancelOnMove close to the player move event
		// without having to hook the expensive onPlayerMove() event.
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, warmup, 20);
		
		return true;
	}
	
	/*
	public boolean hasWarmups(String playerName) {
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		if( playerWarmups == null || playerWarmups.isEmpty() )
			return false;
		else
			return true;
	}
	*/
	
	public void cancelWarmup(int warmupId) {
		throw new NotImplementedException();
	}

	/**
	 * 
	 * @param warmupId
	 * @return true if the event is canceled, false if it is still active
	 */
	public boolean isCanceled(int warmupId) {
		PendingWarmup warmup = warmupsPending.get(warmupId);
		
		if( warmup != null && warmup.cancelled == false )
			return false;
		else
			return true;
	}
	
	public void processEntityDamage(EntityDamageEvent event) {
		// if we aren't supposed to cancel on damage, no further processing required
		if( !config.getBoolean(ConfigOptions.WARMUPS_ON_DAMAGE_CANCEL, false) )
			return;
		
		Entity e = event.getEntity();
		Player p = null;
		if( e instanceof Player ) {
			p = (Player) e;
		}
		
		if( p != null ) {
			String playerName = p.getName();
			List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);

			if( playerWarmups != null && !playerWarmups.isEmpty() ) {
				for(PendingWarmup warmup : playerWarmups) {
					warmup.cancel();	// possible ConcurrentModification exception?
					p.sendMessage("You took damage! Warmup "+warmup.warmupName+" cancelled.");
				}
			}
		}
	}
	
	public void processPlayerMove(PlayerMoveEvent event) {
		// if we aren't supposed to cancel on move, no further processing required
		if( !config.getBoolean(ConfigOptions.WARMUPS_ON_MOVE_CANCEL, false) )
			return;
		
		String playerName = event.getPlayer().getName();
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		
		if( playerWarmups != null && !playerWarmups.isEmpty() ) {
			for(PendingWarmup warmup : playerWarmups) {
				warmup.cancel();	// possible ConcurrentModification exception?
				event.getPlayer().sendMessage("You moved! Warmup "+warmup.warmupName+" cancelled.");
			}
		}
	}
	
	public void processPlayerTeleport(PlayerTeleportEvent event) {
		// if we aren't supposed to cancel on move, no further processing required
		if( !config.getBoolean(ConfigOptions.WARMUPS_ON_MOVE_CANCEL, false) )
			return;
			
		String playerName = event.getPlayer().getName();
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		
		if( playerWarmups != null && !playerWarmups.isEmpty() ) {
			for(PendingWarmup warmup : playerWarmups) {
				warmup.cancel();	// possible ConcurrentModification exception?
				event.getPlayer().sendMessage("You moved! Warmup "+warmup.warmupName+" cancelled.");
			}
		}
	}
	
	public void processPlayerPortal(PlayerPortalEvent event) {
		// if we aren't supposed to cancel on move, no further processing required
		if( !config.getBoolean(ConfigOptions.WARMUPS_ON_MOVE_CANCEL, false) )
			return;
		
		String playerName = event.getPlayer().getName();
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		
		if( playerWarmups != null && !playerWarmups.isEmpty() ) {
			for(PendingWarmup warmup : playerWarmups) {
				warmup.cancel();	// possible ConcurrentModification exception?
				event.getPlayer().sendMessage("You moved! Warmup "+warmup.warmupName+" cancelled");
//				if( !warmup.runner.onPlayerPortal(event) ) {
//				}
			}
		}
	}
	
	private class PendingWarmup implements Runnable {
		public int warmupId;
		public boolean cancelled = false;
		public String playerName;
		public String warmupName;
		public WarmupRunner runner;
		public int warmupTime = 0;
		public long startTime = 0;
		
		public Location playerLocation;
		
		private void cleanup() {
			warmupsPending.remove(warmupId);
			List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
			if( playerWarmups != null )
				playerWarmups.remove(this);
		}
		
		public void cancel() {
			cleanup();
			runner.cancel();
		}
		
		public void run() {
			Player p = plugin.getServer().getPlayer(playerName);
			// this can happen if the player logs out before the warmup fires.  So just cleanup and exit.
			if( p == null ) {
				cleanup();
				return;
			}
			
			// has the warmup fired?  If so, run it.
			if( System.currentTimeMillis() > (startTime + warmupTime) ) {
				cleanup();
				
				// now do whatever the warmup action is
				runner.run();
			}
			// otherwise do some checks and then schedule another run for another 20 ticks out
			else {
				boolean scheduleNext = true;
				
				if( config.getBoolean(ConfigOptions.WARMUPS_ON_MOVE_CANCEL, false) ) {
					Location currentLoc = p.getLocation();
					if( playerLocation.getX() != currentLoc.getX() ||
						playerLocation.getY() != currentLoc.getY() ||
						playerLocation.getZ() != currentLoc.getZ() ||
						!playerLocation.getWorld().getName().equals(currentLoc.getWorld().getName()) )
					{
						p.sendMessage("You moved! Warmup "+warmupName+" cancelled.");
						cleanup();
						scheduleNext = false;
					}
				}
				
				if( scheduleNext )
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 20);
			}
		}
	}
}
