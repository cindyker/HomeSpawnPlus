package org.morganm.homespawnplus.strategy;

import java.util.List;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.TeleportOptions;

public interface StrategyContext {

	public abstract String getEventType();

	public abstract void setEventType(String eventType);

	public abstract Player getPlayer();

	/** Return the "location" of the event, which might be a manually passed in location
	 * or the player location, depending on which data we have.
	 * 
	 * @return
	 */
	public abstract Location getEventLocation();

	/** The location the event is happening, which may be different than
	 * the player location.
	 * 
	 * @return
	 */
	public abstract Location getLocation();

	public abstract void setLocation(Location location);

	public abstract Location getFromLocation();

	public abstract void setFromLocation(Location fromLocation);

	public abstract void setPlayer(Player player);

	public abstract List<ModeStrategy> getCurrentModes();

	public abstract void addMode(ModeStrategy mode);

	public abstract void resetCurrentModes();

	/** Home default mode is true as long as no home exclusive modes are set
	 * that would cancel it out.
	 * 
	 * @return
	 */
	public abstract boolean isInHomeDefaultMode();

	public abstract boolean isDefaultModeEnabled();

	/** Loop through all existing modes that have been set to see if a given mode
	 * has been enabled.
	 * 
	 * @param mode
	 * @return
	 */
	public abstract boolean isModeEnabled(StrategyMode mode);

	/** If a mode is enabled, return the mode object.
	 * 
	 * @param mode
	 * @return
	 */
	public abstract ModeStrategy getMode(StrategyMode mode);

	/** Method for checking boolean states of any active modes to see if those
	 * modes allow strategy processing given the current context.
	 * 
	 * @return
	 */
	public abstract boolean isStrategyProcessingAllowed();

	/** Using currently set modes, return the current bounds
	 * 
	 * @return current bounds, guaranteed not to be null
	 */
	public abstract TeleportOptions getTeleportOptions();

	/** Validate the locations meet any distance limit criteria specified in the current
	 * context. The context "getEventLocation()" is the anchor location (usually the
	 * player location).
	 * 
	 * @param newLoation the location being compared
	 * @return true if the location is within the distance bounds, false if not
	 */
	public abstract boolean checkDistance(Location newLocation);

	public abstract String getArg();

	/** Optional argument that might be used by strategies to take input
	 * from user commands, for example. 
	 * 
	 * @param arg
	 */
	public abstract void setArg(String arg);

}