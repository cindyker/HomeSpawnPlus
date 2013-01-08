/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
 */
/**
 * 
 */
package com.andune.minecraft.hsp.strategies.spawn;

import javax.inject.Inject;


import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.andune.minecraft.hsp.strategy.BaseStrategy;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.strategy.StrategyResultImpl;

/**
 * @author andune
 *
 */
@OneArgStrategy
public class SpawnSpecificWorld extends BaseStrategy {
    @Inject private SpawnDAO spawnDAO;
    
	private final String worldName;
	
	public SpawnSpecificWorld(final String worldName) {
		this.worldName = worldName;
	}

	@Override
	public StrategyResult evaluate(StrategyContext context) {
	    Spawn spawn = spawnDAO.findSpawnByWorld(worldName);
		// since worldSpawn is very specific, it's usually an error condition if we didn't
		// find a spawn that the admin identified, so print a warning so they can fix the
		// issue.
		if( spawn == null )
			log.warn("No spawn found for name \""+worldName+"\" for \""+getStrategyConfigName()+"\" strategy");
		
		return new StrategyResultImpl(spawn);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnSpecificWorld";
	}
}
