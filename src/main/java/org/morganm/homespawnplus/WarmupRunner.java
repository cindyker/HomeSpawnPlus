package org.morganm.homespawnplus;

import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * @author morganm
 *
 */
public interface WarmupRunner extends Runnable {
	public void setPlayerName(String playerName);
	public void setWarmupId(int warmupId);
	
	/** Process a player move.  Return true if the warmup should proceed, false if
	 * it should be canceled.
	 * 
	 * @param event
	 * @return
	 */
	public boolean onPlayerMove(PlayerMoveEvent event);
	
	/** Process a player teleport.  Return true if the warmup should proceed, false if
	 * it should be canceled.
	 * 
	 * @param event
	 * @return
	 */
	public boolean onPlayerTeleport(PlayerTeleportEvent event);
	
	/** Process a player portal event.  Return true if the warmup should proceed, false
	 * if it should be canceled.
	 * 
	 * @param event
	 * @return
	 */
    public boolean onPlayerPortal(PlayerPortalEvent event);
}
