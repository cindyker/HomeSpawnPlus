package org.morganm.homespawnplus;


/**
 * @author morganm
 *
 */
public interface WarmupRunner extends Runnable {
	public void setPlayerName(String playerName);
	public void setWarmupId(int warmupId);
	
	/** Invoked if this warmup is canceled.
	 * 
	 */
	public void cancel();
	
	/** Process a player move.  Return true if the warmup should proceed, false if
	 * it should be canceled.
	 * 
	 * @param event
	 * @return
	 */
//	public boolean onPlayerMove(PlayerMoveEvent event);
	
	/** Process a player teleport.  Return true if the warmup should proceed, false if
	 * it should be canceled.
	 * 
	 * @param event
	 * @return
	 */
//	public boolean onPlayerTeleport(PlayerTeleportEvent event);
	
	/** Process a player portal event.  Return true if the warmup should proceed, false
	 * if it should be canceled.
	 * 
	 * @param event
	 * @return
	 */
//    public boolean onPlayerPortal(PlayerPortalEvent event);
}
