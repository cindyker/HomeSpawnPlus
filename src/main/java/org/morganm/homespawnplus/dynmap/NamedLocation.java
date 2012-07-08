/**
 * 
 */
package org.morganm.homespawnplus.dynmap;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/** Interface for HSP locations.
 * 
 * @author morganm
 *
 */
public interface NamedLocation {
	public Location getLocation();
	public String getName();

	/** If the object is owned by a player, this method should return
	 * the player name of the owner.
	 * 
	 * @return
	 */
	public String getPlayerName();
	
	/** Determine whether this NamedLocation is enabled and should be
	 * shown. The ConfigurationSection is expected to be relevant
	 * to the object type, so that it can look up any configuration
	 * options to make decisions about whether it is enabled or not.
	 * 
	 * @return
	 */
	public boolean isEnabled(ConfigurationSection section);
}
