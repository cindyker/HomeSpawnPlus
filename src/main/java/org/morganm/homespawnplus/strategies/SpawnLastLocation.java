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

import javax.inject.Inject;

import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.NoArgStrategy;
import org.morganm.homespawnplus.strategy.OneArgStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
@NoArgStrategy
@OneArgStrategy
public class SpawnLastLocation extends BaseStrategy {
    protected Storage storage;
    @Inject public void setStorage(Storage storage) { this.storage = storage; }

	private String world;
	
	public SpawnLastLocation() {}
	public SpawnLastLocation(final String world) {
		this.world = world;
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		StrategyResult result = null;
		final Player p = context.getPlayer();
		
		// take the world from the argument, if given
		String worldName = context.getArg();
		// otherwise use the name given at instantiation
		if( worldName == null )
			worldName = this.world;

		// if no arg was given at runtime or instantiation, use location
		// of the player to determine the world
		if( worldName == null )
			worldName = context.getEventLocation().getWorld().getName();

		PlayerLastLocationDAO dao = storage.getPlayerLastLocationDAO();
		PlayerLastLocation	 pll = dao.findByWorldAndPlayerName(worldName, p.getName());
		
		if( pll != null )
			result = new StrategyResult(pll.getLocation());
		
		return result;
	}
}
