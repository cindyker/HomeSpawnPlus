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
package org.morganm.homespawnplus.strategies;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnLocalPlayerSpawn extends BaseStrategy {
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		StrategyResult result = null;
		
		Player p = context.getPlayer();
		String worldName = context.getEventLocation().getWorld().getName();
		debug.debug("SpawnLocalPlayerSpawn.evaluate() worldName=",worldName);
		
		PlayerSpawnDAO dao = plugin.getStorage().getPlayerSpawnDAO();
		PlayerSpawn ps = dao.findByWorldAndPlayerName(worldName, p.getName());
		
		if( ps != null ) {
			if( ps.getSpawn() != null )
				result = new StrategyResult(ps.getSpawn());
			else
				result = new StrategyResult(ps.getLocation());
		}
		
		return result;
	}
}
