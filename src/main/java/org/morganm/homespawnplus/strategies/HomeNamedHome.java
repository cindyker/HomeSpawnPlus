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

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.strategy.HomeStrategy;
import org.morganm.homespawnplus.strategy.NoArgStrategy;
import org.morganm.homespawnplus.strategy.OneArgStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyMode;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
@NoArgStrategy
@OneArgStrategy
public class HomeNamedHome extends HomeStrategy {
	private String homeName;
	
	public HomeNamedHome() {}
	public HomeNamedHome(String homeName) {
		this.homeName = homeName;
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		Home home = null;
		
		// take the name from the argument, if given
		String name = context.getArg();
		// otherwise use the name given at instantiation
		if( name == null )
			name = homeName;
		
		log.debug("HomeNamedHome: name={}",name);

		if( name != null ) {
		    home = homeDAO.findHomeByNameAndPlayer(name, context.getPlayer().getName());
			
			log.debug("HomeNamedHome: home pre-modes={}, current modes={}", home, context.getCurrentModes());
			
			if( context.isModeEnabled(StrategyMode.MODE_HOME_DEFAULT_ONLY) && !home.isDefaultHome() )
				home = null;
			if( context.isModeEnabled(StrategyMode.MODE_HOME_BED_ONLY) && !home.isBedHome() )
				home = null;
			if( context.isModeEnabled(StrategyMode.MODE_HOME_NO_BED) && home.isBedHome() )
				home = null;
			if( context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
				logVerbose("Home ",home," skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
				home = null;
			}
		}
		else
			log.warn("Strategy "+getStrategyConfigName()+" was not given a homeName argument, nothing to do");
		
		return new StrategyResult(home);
	}

	@Override
	public String getStrategyConfigName() {
		return "homeNamedHome";
	}

}
