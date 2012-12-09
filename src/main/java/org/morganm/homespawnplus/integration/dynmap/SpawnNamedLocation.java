/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.integration.dynmap;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.Storage;

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
	    // I dislike the reference to Storage, this should be changed to a member
	    // method for the 2.0 refactor
	    if( spawn.getGroup().equals(Storage.HSP_WORLD_SPAWN_GROUP) )
	        return true;

        if( section.getBoolean("include-named-spawns", true) )
            return true;

        // if it hasn't been true yet, then we're not supposed to show it
		return false;
	}
}
