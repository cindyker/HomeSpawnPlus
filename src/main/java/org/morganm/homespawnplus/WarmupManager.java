/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.morganm.homespawnplus.config.Config;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.util.Debug;

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
	private final Debug debug;
	private final Config config;
	
	public WarmupManager(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.config = plugin.getHSPConfig();
		this.logPrefix = HomeSpawnPlus.logPrefix;
    	this.debug = Debug.getInstance();
		
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
			getWarmupTime(p, warmupName).warmupTime > 0 &&
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
	
	public WarmupTime getWarmupTime(final Player player, final String warmup) {
    	final WarmupTime wut = new WarmupTime();
    	wut.warmupName = warmup;	// default to existing warmup name
    	
    	debug.debug("getWarmupTime(): warmup=",warmup);
    	
    	if( wut.warmupTime == 0 ) {
	    	ConfigurationSection cs = plugin.getHSPConfig().getConfigurationSection(ConfigOptions.WARMUP_BASE
	    			+ ConfigOptions.SETTING_EVENTS_PERMBASE);
	    	if( cs != null ) {
	    		Set<String> keys = cs.getKeys(false);
	    		if( keys != null ) 
	    			for(String entry : keys) {
						debug.debug("getWarmupTime(): checking entry ",entry);
	    				// stop looping once we find a non-zero warmupTime
	    				if( wut.warmupTime != 0 )
	    					break;
	    				
	    				int entryWarmup = plugin.getHSPConfig().getInt(ConfigOptions.WARMUP_BASE
	    						+ ConfigOptions.SETTING_EVENTS_PERMBASE + "." + entry + "." + warmup, 0);
	    				
	    				if( entryWarmup > 0 ) {
		    				List<String> perms = plugin.getHSPConfig().getStringList(ConfigOptions.WARMUP_BASE
		    						+ ConfigOptions.SETTING_EVENTS_PERMBASE + "."
		    						+ entry + ".permissions", null);
		
		    				for(String perm : perms) {
		    					debug.debug("getWarmupTime(): checking permission ",perm," for entry ",entry);
		
		    					if( plugin.hasPermission(player, perm) ) {
		    						wut.warmupTime = entryWarmup;
	    	    					wut.warmupName = warmup + "." + perm;
		    						break;
		    					}
		    				}
	    				}// end if( entryWarmup > 0 )
	    			}// end for(String entry : keys)
	    	}// end if( cs != null )
	    	
        	debug.debug("getWarmupTime(): post-permission warmup=",wut.warmupTime,", name=",wut.warmupName);
    	}
    	
    	// if warmupTime is still 0, then check for world-specific warmup
    	if( wut.warmupTime == 0 ) {
    		final String worldName = player.getWorld().getName();
    		wut.warmupTime = plugin.getHSPConfig().getInt(ConfigOptions.WARMUP_BASE
					+ ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
					+ worldName + "." + warmup, 0);
			wut.warmupName = warmup + "." + worldName;
			
	    	debug.debug("getWarmupTime(): post-world world=",worldName,", warmup=",wut.warmupTime,", name=",wut.warmupName);
    	}
    	
    	// if warmupTime is still 0, then check global warmup setting
    	if( wut.warmupTime == 0 ) {
    		wut.warmupTime = plugin.getHSPConfig().getInt(ConfigOptions.WARMUP_BASE + warmup, 0);
			wut.warmupName = warmup;
        	debug.debug("getWarmupTime(): post-global warmup=",wut.warmupTime,", name=",wut.warmupName);
    	}
    	
    	return wut;

//    	return plugin.getHSPConfig().getInt(ConfigOptions.WARMUP_BASE + warmupName, 0);
	}
	
	/** Start a given warmup.  Return true if the warmup was started successfully, false if not.
	 * 
	 * @param playerName
	 * @param warmupName
	 * @param warmupRunnable
	 * @return
	 */
	public boolean startWarmup(String playerName, WarmupRunner warmupRunnable) {
		Player p = plugin.getServer().getPlayer(playerName);
		if( p == null ) {
			log.warning(logPrefix + " startWarmup() found null player object for name "+playerName);
			return false;
		}
		
		WarmupTime wut = getWarmupTime(p, warmupRunnable.getWarmupName());
		final String warmupName = wut.warmupName;
		
		if( isWarmupPending(playerName, warmupName) ) {		// don't let two of the same warmups start
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
		
		PendingWarmup warmup = new PendingWarmup();
		warmup.warmupId = warmupId;
		warmup.playerName = playerName;
		warmup.warmupName = wut.warmupName;
		warmup.runner = warmupRunnable;
		warmup.startTime = System.currentTimeMillis();
		warmup.warmupTime = wut.warmupTime * 1000;
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
	
	/** To be called when an entity takes damage so that we can respond appropriately
	 * to any pending warmups.
	 * 
	 * @param event
	 */
	public void processEntityDamage(EntityDamageEvent event) {
		if( event.isCancelled() )
			return;
		
		// if we aren't supposed to cancel on damage, no further processing required
		if( !config.getBoolean(ConfigOptions.WARMUPS_ON_DAMAGE_CANCEL, false) )
			return;

		// don't do any extra processing if there are no pending warmups
		if( warmupsPending.isEmpty() )
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
				for(Iterator<PendingWarmup> i = playerWarmups.iterator(); i.hasNext();) {
					PendingWarmup warmup = i.next();
					
					// remove it directly to avoid ConcurrentModification exception.  Below
					// warmup.cancel() will also try to remove the object, but it will just
					// result in a NOOP since we already remove the element here.
					i.remove();
					
					warmup.cancel();
					
					plugin.getUtil().sendLocalizedMessage(p, HSPMessages.WARMUP_CANCELLED_DAMAGE, "name", warmup.warmupName);
//					p.sendMessage("You took damage! Warmup "+warmup.warmupName+" cancelled.");
				}
			}
		}
	}
	
	public class WarmupTime { 
		public int warmupTime = 0;
		public String warmupName;
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
				
				// do movement checks to see if player has moved since the warmup started
				if( config.getBoolean(ConfigOptions.WARMUPS_ON_MOVE_CANCEL, false) ) {
					Location currentLoc = p.getLocation();
					if( playerLocation.getBlockX() != currentLoc.getBlockX() ||
						playerLocation.getBlockY() != currentLoc.getBlockY() ||
						playerLocation.getBlockZ() != currentLoc.getBlockZ() ||
						!playerLocation.getWorld().getName().equals(currentLoc.getWorld().getName()) )
					{
						plugin.getUtil().sendLocalizedMessage(p, HSPMessages.WARMUP_CANCELLED_YOU_MOVED, "name", warmupName);
//						p.sendMessage("You moved! Warmup "+warmupName+" cancelled.");
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
