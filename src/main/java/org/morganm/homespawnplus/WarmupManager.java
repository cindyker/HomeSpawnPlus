/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.morganm.homespawnplus.config.ConfigOptions;

/**
 * @author morganm
 *
 */
public class WarmupManager {
	private static int uniqueWarmupId = 0;
	
	private final HashMap<Integer, PendingWarmup> warmupsPending;
	private final HashMap<String, List<PendingWarmup>> warmupsPendingByPlayerName;
	
	private final HomeSpawnPlus plugin;
	
	public WarmupManager(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		
		warmupsPending = new HashMap<Integer, PendingWarmup>();
		warmupsPendingByPlayerName = new HashMap<String, List<PendingWarmup>>();
	}
	
	/** Start a given warmup.  Return true if the warmup was started successfully, false if not.
	 * 
	 * @param playerName
	 * @param warmupName
	 * @param warmupRunnable
	 * @return
	 */
	public boolean startWarmup(String playerName, String warmupName, WarmupRunner warmupRunnable) {
		int warmupId = 0;
		synchronized(this) {
			warmupId = ++uniqueWarmupId;
		}
		
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		if( playerWarmups == null ) {
			playerWarmups = new ArrayList<PendingWarmup>();
			warmupsPendingByPlayerName.put(playerName, playerWarmups);
		}
		
		for(PendingWarmup pw : playerWarmups) {
			if( warmupName.equals(pw.warmupName) ) {
				// already a warmup by that name in progress, don't allow another one to start
				return false;
			}
		}
		
		int warmupTime = plugin.getConfig().getInt(ConfigOptions.WARMUP_BASE + warmupName, 0);
		
		PendingWarmup warmup = new PendingWarmup();
		warmup.warmupId = warmupId;
		warmup.playerName = playerName;
		warmup.warmupName = warmupName;
		warmup.runner = warmupRunnable;
		
		warmupRunnable.setWarmupId(warmupId);
		warmupRunnable.setPlayerName(playerName);
		
		// keep track of the warmups we have pending
		warmupsPending.put(warmupId, warmup);
		playerWarmups.add(warmup);
		
		// kick off a Bukkit scheduler to the Runnable for the timer given
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, warmup, warmupTime);
		
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
	
	public void processPlayerMove(PlayerMoveEvent event) {
		String playerName = event.getPlayer().getName();
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		
		if( playerWarmups != null && !playerWarmups.isEmpty() ) {
			for(PendingWarmup warmup : playerWarmups) {
				if( !warmup.runner.onPlayerMove(event) ) {
					warmup.cancel();	// possible ConcurrentModification exception?
				}
			}
		}
	}
	
	public void processPlayerTeleport(PlayerTeleportEvent event) {
		String playerName = event.getPlayer().getName();
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		
		if( playerWarmups != null && !playerWarmups.isEmpty() ) {
			for(PendingWarmup warmup : playerWarmups) {
				if( !warmup.runner.onPlayerTeleport(event) ) {
					warmup.cancel();	// possible ConcurrentModification exception?
				}
			}
		}
	}
	
	public void processPlayerPortal(PlayerPortalEvent event) {
		String playerName = event.getPlayer().getName();
		List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
		
		if( playerWarmups != null && !playerWarmups.isEmpty() ) {
			for(PendingWarmup warmup : playerWarmups) {
				if( !warmup.runner.onPlayerPortal(event) ) {
					warmup.cancel();	// possible ConcurrentModification exception?
				}
			}
		}
	}
	
	private class PendingWarmup implements Runnable {
		public int warmupId;
		public boolean cancelled = false;
		public String playerName;
		public String warmupName;
		public WarmupRunner runner;
		
		public void cancel() {
			warmupsPending.remove(warmupId);
			List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
			if( playerWarmups != null )
				playerWarmups.remove(this);
		}
		
		public void run() {
			// first cleanup the warmup from the list
			cancel();
			
			// now do whatever the warmup action is
			runner.run();
		}
	}
}
