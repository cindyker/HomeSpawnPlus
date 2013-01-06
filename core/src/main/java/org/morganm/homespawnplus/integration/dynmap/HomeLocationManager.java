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
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.dao.HomeDAO;

/**
 * @author morganm
 *
 */
public class HomeLocationManager implements LocationManager {
	private final Storage storage;

	public HomeLocationManager(final Storage storage) {
		this.storage = storage;
	}
	
	@Override
	public List<NamedLocation> getLocations(World w) {
		List<NamedLocation> locations = new ArrayList<NamedLocation>();
		
		String world = w.getName();
		
		HomeDAO dao = storage.getHomeDAO();
		Set<Home> allHomes = dao.findAllHomes();
		if( allHomes != null && allHomes.size() > 0 ) {
			for(Home home : allHomes) {
				// skip any homes that aren't in the given world
				if( !home.getWorld().equals(world) )
					continue;
				
				locations.add(new HomeNamedLocation(home));
			}
		}
		
		return locations;
	}
}
