/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.strategies.ModeDefault;
import org.morganm.homespawnplus.strategies.ModeInRegion;
import org.morganm.homespawnplus.strategies.ModeMultiverseDestinationPortal;
import org.morganm.homespawnplus.strategies.ModeMultiverseSourcePortal;
import org.morganm.homespawnplus.strategies.ModeSourceWorld;
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
	private final static ModeStrategy defaultMode = new ModeDefault();
	
	private final HomeSpawnPlus plugin;
	private String eventType;
	private Player player;
	private Location location;
	/* If there is a "fromLocation" for this action, it will be recorded here.
	 * 
	 */
	private Location fromLocation;
	private String arg;
	
	/** As a strategy chain is being evaluated, the current mode might change. This
	 * is tracked here in the context object.
	 */
	private List<ModeStrategy> currentModes;
	
	public StrategyContext(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}

	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Player getPlayer() {
		return player;
	}
	
	/** Return the "location" of the event, which might be a manually passed in location
	 * or the player location, depending on which data we have.
	 * 
	 * @return
	 */
	public Location getEventLocation() {
		if( location != null )
			return location;
		else
			return player.getLocation();
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
	
	public Location getFromLocation() {
		return fromLocation;
	}
	public void setFromLocation(Location fromLocation) {
		this.fromLocation = fromLocation;
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
	
	/** Home default mode is true as long as no home exclusive modes are set
	 * that would cancel it out.
	 * 
	 * @return
	 */
	public boolean isInHomeDefaultMode() {
		if( isDefaultModeEnabled() )
			return true;
		
		if( isModeEnabled(StrategyMode.MODE_HOME_ANY) )
			return false;
		if( isModeEnabled(StrategyMode.MODE_HOME_BED_ONLY) )
			return false;
		if( isModeEnabled(StrategyMode.MODE_HOME_NO_BED) )
			return false;
		if( isModeEnabled(StrategyMode.MODE_HOME_DEFAULT_ONLY) )
			return false;
		
		return true;
	}
	
	public boolean isDefaultModeEnabled() {
		if( currentModes == null || currentModes.size() == 0 )
			return true;
		if( currentModes.size() == 1 ) {
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
		boolean ret = getMode(mode) != null;
		Debug.getInstance().devDebug("isModeEnabled() mode=",mode,", ret=",ret);
		return ret;
	}
	
	/** If a mode is enabled, return the mode object.
	 * 
	 * @param mode
	 * @return
	 */
	public ModeStrategy getMode(final StrategyMode mode) {
		ModeStrategy ret = null;
		Debug.getInstance().devDebug("getMode() check for mode ",mode);
		
		if( currentModes == null || currentModes.size() == 0 ) {
			if( isDefaultMode(mode) )
				ret = defaultMode;
			
			Debug.getInstance().devDebug("getMode() No modes defined, returning ",ret);
			return ret;
		}
		
		for(ModeStrategy currentMode : currentModes) {
			StrategyMode modeType = currentMode.getMode();
			if( modeType == mode ) {
				ret = currentMode;
				break;
			}
		}
		
		Debug.getInstance().devDebug("getMode() returning ",ret);
		return ret;
	}
	
	/** Method for checking boolean states of any active modes to see if those
	 * modes allow strategy processing given the current context.
	 * 
	 * @return
	 */
	public boolean isStrategyProcessingAllowed() {
		if( plugin.getMultiverseIntegration().isEnabled() ) {
			ModeStrategy modeStrategy = getMode(StrategyMode.MODE_MULTIVERSE_SOURCE_PORTAL);
			if( modeStrategy != null && modeStrategy instanceof ModeMultiverseSourcePortal ) {
				ModeMultiverseSourcePortal mode = (ModeMultiverseSourcePortal) modeStrategy;
				String strategyPortalName = mode.getPortalName();
				String sourcePortalName = plugin.getMultiverseIntegration().getSourcePortalName();
				if( !strategyPortalName.equals(sourcePortalName) ) {
					Debug.getInstance().debug("isStrategyProcessingAllowed() returning false for source portal check. ",
							"strategyPortalName=",strategyPortalName,", sourcePortalName=",sourcePortalName);
					return false;
				}
			}
			
			modeStrategy = getMode(StrategyMode.MODE_MULTIVERSE_DESTINATION_PORTAL);
			if( modeStrategy != null && modeStrategy instanceof ModeMultiverseDestinationPortal ) {
				ModeMultiverseDestinationPortal mode = (ModeMultiverseDestinationPortal) modeStrategy;
				String strategyPortalName = mode.getPortalName();
				String destinationPortalName = plugin.getMultiverseIntegration().getDestinationPortalName();
				if( !strategyPortalName.equals(destinationPortalName) ) {
					Debug.getInstance().debug("isStrategyProcessingAllowed() returning false for destination portal check. ",
							"strategyPortalName=",strategyPortalName,", destinationPortalName=",destinationPortalName);
					return false;
				}
			}
		}
		
		if( plugin.getWorldGuardIntegration().isEnabled() ) {
			ModeStrategy modeStrategy = getMode(StrategyMode.MODE_IN_REGION);
			if( modeStrategy != null && modeStrategy instanceof ModeInRegion ) {
				ModeInRegion mode = (ModeInRegion) modeStrategy;
				String regionName = mode.getRegionName();
				
				if( !plugin.getWorldGuardIntegration().getWorldGuardInterface().isLocationInRegion(getEventLocation(), regionName) ) {
					Debug.getInstance().debug("isStrategyProcessingAllowed() returning false for worldguard region check. ",
							"region=",regionName);
					return false;
				}
			}
		}
		
		ModeStrategy modeStrategy = getMode(StrategyMode.MODE_SOURCE_WORLD);
		if( modeStrategy != null && modeStrategy instanceof ModeInRegion ) {
			ModeSourceWorld mode = (ModeSourceWorld) modeStrategy;
			String sourceWorld = mode.getWorldName();
			
			if( getFromLocation() == null || !getFromLocation().getWorld().getName().equals(sourceWorld) ) { 
				Debug.getInstance().debug("isStrategyProcessingAllowed() returning false for sourceWorld. ",
						"sourceWorld=",sourceWorld,", fromLocation=",fromLocation);
				return false;
			}
		}

		Debug.getInstance().debug("isStrategyProcessingAllowed() returning true");
		return true;
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
				+",player.location="+(player != null ? General.getInstance().shortLocationString(player.getLocation()) : null)
				+",location="+(location != null ? General.getInstance().shortLocationString(location) : null)
				+",arg="+arg
				+"}";
	}
}
