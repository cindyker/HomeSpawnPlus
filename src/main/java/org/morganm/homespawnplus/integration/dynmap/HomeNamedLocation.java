/**
 * 
 */
package org.morganm.homespawnplus.integration.dynmap;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.morganm.homespawnplus.entity.Home;

/**
 * @author morganm
 *
 */
public class HomeNamedLocation implements NamedLocation {
	private final Home home;
	private String name;
	
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
		if( name == null ) {
			if( home.getName() != null )
				name = home.getPlayerName() + ":" + home.getName();
			else
				name = home.getPlayerName();
		}

		return name;
	}

	@Override
	public String getPlayerName() {
		return home.getPlayerName();
	}
	
	@Override
	public boolean isEnabled(ConfigurationSection section) {
		if( home.isDefaultHome() )
			return true;
		if( home.isBedHome() && section.getBoolean("include-bed-home", true) )
			return true;
		if( section.getBoolean("include-named-homes", true) )
			return true;

		// if it hasn't been true yet, then we're not supposed to show it
		return false;
	}
}
