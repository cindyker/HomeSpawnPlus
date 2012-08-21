/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.strategies.ModeDefault;
import org.morganm.homespawnplus.strategies.ModeYBounds;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.General;
import org.morganm.homespawnplus.util.Teleport;

/** The context given to a strategy that is being evaluated.
 * 
 * @author morganm
 *
 */
public class StrategyContext {
	private EventType eventType;
	private Player player;
	private Location location;
	private String arg;
	
	/** As a strategy chain is being evaluated, the current mode might change. This
	 * is tracked here in the context object.
	 */
	private List<ModeStrategy> currentModes;

	public EventType getEventType() {
		return eventType;
	}
	public void setSpawnEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public Player getPlayer() {
		return player;
	}
	
	/** The location the event is happening, which may be different than
	 * the player location.
	 * 
	 * @return
	 */
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}

	public void setPlayer(Player player) {
		this.player = player;
		
		// if player isn't null and location is, then automatically update
		// location to the player's location (this can be overridden later)
		if( player != null && getLocation() == null )
			setLocation(player.getLocation());
	}
	
	public List<ModeStrategy> getCurrentModes() {
		return currentModes;
	}
	private final static ModeStrategy defaultMode = new ModeDefault();
	public void resetCurrentModes() {
		if( currentModes == null )
			currentModes = new ArrayList<ModeStrategy>(2);
		else
			currentModes.clear();

		currentModes.add(defaultMode);
	}
	
	private boolean isDefaultMode(final StrategyMode mode) {
		if( mode == StrategyMode.MODE_HOME_NORMAL || mode == StrategyMode.MODE_DEFAULT )
			return true;
		else
			return false;
	}
	
	public boolean isDefaultModeEnabled() {
		if( currentModes == null || currentModes.size() == 0 )
			return true;
		if( currentModes.size() >= 1 ) {
			StrategyMode mode = currentModes.get(0).getMode();
			return isDefaultMode(mode);
		}

		return false;
	}

	/** Loop through all existing modes that have been set to see if a given mode
	 * has been enabled.
	 * 
	 * @param mode
	 * @return
	 */
	public boolean isModeEnabled(final StrategyMode mode) {
		boolean ret = false;
		Debug.getInstance().devDebug("Mode check for mode ",mode);
		
		if( currentModes == null || currentModes.size() == 0 ) {
			if( isDefaultMode(mode) )
				ret = true;
			
			Debug.getInstance().devDebug("No modes defined, returning ",ret);
			return ret;
		}
		
		for(ModeStrategy currentMode : currentModes) {
			StrategyMode modeType = currentMode.getMode();
			if( modeType == mode ) {
				ret = true;
				break;
			}
		}
		
		Debug.getInstance().devDebug("mode check returning ",ret);
		return ret;
	}
	
	/** Using currently set modes, return any flags relevant to safeTeleport.
	 * 
	 * @return
	 */
	public int getModeSafeTeleportFlags() {
		int flags = 0;
		
		for(StrategyMode mode : StrategyMode.getSafeModes()) {
			if( isModeEnabled(mode) )
				flags |= mode.getFlagId();
		}
		
		Debug.getInstance().devDebug("getModeSafeTeleportFlags() flags=",flags);
		return flags;
	}
	
	
	/** Using currently set modes, return the current bounds (if any).
	 * 
	 * @return current bounds or null if no bounds are set
	 */
	public Teleport.Bounds getModeBounds() {
		List<ModeStrategy> modes = getCurrentModes();
		for(ModeStrategy mode : modes) {
			if( mode.getMode() == StrategyMode.MODE_YBOUNDS ) {
				ModeYBounds modeYBounds = (ModeYBounds) mode;
				Teleport.Bounds bounds = new Teleport.Bounds();
				bounds.minY = modeYBounds.getMinY();
				bounds.maxY = modeYBounds.getMaxY();
				return bounds;
			}
		}
		
		return null;
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
	
	public String toString() {
		return "{eventType="+eventType
			+",player="+player
			+",arg="+arg
			+",location="+(location != null ? General.getInstance().shortLocationString(location) : "null")
			+"}";
	}
}
