package org.morganm.homespawnplus;


/**
 * @author morganm
 *
 */
public interface WarmupRunner extends Runnable {
	public void setPlayerName(String playerName);
	public void setWarmupId(int warmupId);
	public WarmupRunner setWarmupName(String warmupName);
	public String getWarmupName();
	
	/** Invoked if this warmup is canceled.
	 * 
	 */
	public void cancel();
	
}
