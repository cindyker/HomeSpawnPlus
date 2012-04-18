/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

/** The context given to a strategy that is being evaluated.
 * 
 * @author morganm
 *
 */
public class StrategyContext {
	private EventType eventType;
	private Player player;
	private String arg;
	
	/** As a strategy chain is being evaluated, the current mode might change. This
	 * is tracked here in the context object.
	 */
	private List<HomeMode> currentModes;

	public EventType getEventType() {
		return eventType;
	}
	public void setSpawnEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public List<HomeMode> getCurrentModes() {
		return currentModes;
	}
	private final static HomeMode defaultMode = HomeMode.MODE_HOME_NORMAL;
	public void resetCurrentModes() {
		if( currentModes == null )
			currentModes = new ArrayList<HomeMode>(2);
		else
			currentModes.clear();

		currentModes.add(defaultMode);
	}
	
	public String getArg() {
		return arg;
	}
	/** Optional argument that might be used by strategies to take input
	 * from user commands, for example. 
	 * 
	 * @param arg
	 */
	public void setArg(String arg) {
		this.arg = arg;
	}
}
