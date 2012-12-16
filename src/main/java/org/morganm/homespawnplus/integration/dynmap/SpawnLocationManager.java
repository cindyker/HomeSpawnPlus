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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.morganm.homespawnplus.OldHSP;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;

/**
 * @author morganm
 *
 */
public class SpawnLocationManager implements LocationManager {
	private final OldHSP plugin;

	public SpawnLocationManager(final OldHSP plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<NamedLocation> getLocations(final World w) {
		List<NamedLocation> locations = new ArrayList<NamedLocation>();
		
		String world = w.getName();
		
		SpawnDAO dao = plugin.getStorage().getSpawnDAO();
		Set<Spawn> allSpawns = dao.findAllSpawns();
		if( allSpawns != null && allSpawns.size() > 0 ) {
			for(Spawn spawn : allSpawns) {
				// skip any spawns that aren't in the given world
				if( !spawn.getWorld().equals(world) )
					continue;
				
				locations.add(new SpawnNamedLocation(spawn));
			}
		}
		
		return locations;
	}

}
