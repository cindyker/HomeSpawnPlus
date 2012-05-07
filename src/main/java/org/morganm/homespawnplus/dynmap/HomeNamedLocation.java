/**
 * 
 */
package org.morganm.homespawnplus.dynmap;

import org.bukkit.Location;
import org.morganm.homespawnplus.entity.Home;

/**
 * @author morganm
 *
 */
public class HomeNamedLocation implements NamedLocation {
	private final Home home;
	
	public HomeNamedLocation(final Home home) {
		this.home = home;
	}

	@Override
	public Location getLocation() {
		return home.getLocation();
	}

	/** Home "names" on the map are just the player name.
	 * 
	 */
	@Override
	public String getName() {
		return home.getPlayerName();
	}
}
