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
package org.morganm.homespawnplus.integration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;

/**
 * @author morganm
 *
 */
public class WorldBorder {
	@SuppressWarnings("unused")
	private final HomeSpawnPlus plugin;
	private com.wimbli.WorldBorder.WorldBorder worldBorder;
	
	public WorldBorder(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldBorder");
		if( p != null )
			worldBorder = (com.wimbli.WorldBorder.WorldBorder) p;
	}

	// TODO: finish me
	public BorderData getBorderData(String worldName) {
		if( worldBorder != null ) {
//			com.wimbli.WorldBorder.BorderData border = worldBorder.GetWorldBorder(worldName);
//			double x = border.getX();
//			double z = border.getZ();
//			int radius = border.getRadius();
			
//			Location min = new Location(w,x-radius, yBounds.minY, z-radius);
//			Location max = new Location(w,x+radius, yBounds.maxY, z+radius);
			
			return null;
		}
		else
			return defaultBorderData(worldName);
	}
	
	private BorderData defaultBorderData(String worldName) {
		World world = Bukkit.getWorld(worldName);
		return new BorderData(new Location(world,1000,0,1000), new Location(world,-1000,0,-1000));
	}

	static public class BorderData
	{
		private com.wimbli.WorldBorder.BorderData worldBorderData;
		private Location corner1;
		private Location corner2;
		
		public BorderData(com.wimbli.WorldBorder.BorderData worldBorderData) {
			this.worldBorderData = worldBorderData;
		}
		public BorderData(Location corner1, Location corner2) {
			this.corner1 = corner1;
			this.corner2 = corner2;
		}
		
		public boolean insideBorder(Location l) {
			if( worldBorderData != null )
				return worldBorderData.insideBorder(l);
			else
				return true;
		}
		
		public Location getCorner1() { return corner1; }
		public Location getCorner2() { return corner2; }
	}
}
