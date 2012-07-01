/**
 * 
 */
package org.morganm.homespawnplus.dynmap;

import org.bukkit.Location;

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
}
