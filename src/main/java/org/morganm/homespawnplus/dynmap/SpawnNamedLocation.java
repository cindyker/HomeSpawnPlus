/**
 * 
 */
package org.morganm.homespawnplus.dynmap;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.morganm.homespawnplus.entity.Spawn;

/**
 * @author morganm
 *
 */
public class SpawnNamedLocation implements NamedLocation {
	private final Spawn spawn;
	private String name = null;
	
	public SpawnNamedLocation(final Spawn spawn) {
		this.spawn = spawn;
	}

	@Override
	public Location getLocation() {
		return spawn.getLocation();
	}

	@Override
	public String getName() {
		if( name == null ) {
			name = spawn.getName();
			if( name == null ) {
				name = spawn.getLocation().getWorld().getName() + " spawn";
			}
		}

		return name;
	}

	@Override
	public String getPlayerName() {
		return null;
	}
	
	/** No flags exist (yet) to control visibility of spawns; if spawns
	 * are enabled, all of them will be shown.
	 */
	@Override
	public boolean isEnabled(ConfigurationSection section) {
		return true;
	}
}
