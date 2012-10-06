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
package org.morganm.homespawnplus.strategy;

import java.util.ArrayList;

import org.morganm.homespawnplus.util.Teleport;

/** Different modes that strategies can run under, which can change
 * the behavior of other strategies.
 * 
 * @author morganm
 *
 */
public enum StrategyMode {
	MODE_HOME_NORMAL,
	MODE_HOME_BED_ONLY,
	MODE_HOME_NO_BED,
	MODE_HOME_DEFAULT_ONLY,
	MODE_HOME_ANY,
	MODE_HOME_REQUIRES_BED,
	MODE_NO_WATER (Teleport.FLAG_NO_WATER),
	MODE_NO_LILY_PAD (Teleport.FLAG_NO_LILY_PAD),
	MODE_NO_LEAVES (Teleport.FLAG_NO_LEAVES),
	MODE_NO_ICE (Teleport.FLAG_NO_ICE),
	MODE_YBOUNDS,
	MODE_DEFAULT,
	MODE_REMEMBER_SPAWN,
	MODE_REMEMBER_LOCATION,
	MODE_MULTIVERSE_SOURCE_PORTAL,
	MODE_MULTIVERSE_DESTINATION_PORTAL,
	MODE_IN_REGION,
	MODE_SOURCE_WORLD,
	MODE_EXCLUDE_NEW_PLAYER_SPAWN,
	MODE_DISTANCE_LIMITS;
	
	// associated Teleport safeMode flag, if any
	private int flagId = 0;
	
	private StrategyMode() {}
	private StrategyMode(int flagId) {
		this.flagId = flagId;
	}
	
	public int getFlagId() { return flagId; }
	
	private static StrategyMode[] safeModes;
	static {
		ArrayList<StrategyMode> list = new ArrayList<StrategyMode>(5);
		for(StrategyMode value : values()) {
			if( value.flagId > 0 )
				list.add(value);
		}
		
		safeModes = list.toArray(new StrategyMode[] {});
	}
	
	public static StrategyMode[] getSafeModes() {
		return safeModes;
	}
}
