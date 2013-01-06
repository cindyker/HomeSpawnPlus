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

import org.morganm.homespawnplus.strategy.ModeStrategyImpl;
import org.morganm.homespawnplus.strategy.OneArgStrategy;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyMode;

/** Mode to set distance bounds. This has the effect of modifying other
 * strategies to have to be within the distance bounce specified in order
 * to be used.
 * 
 * @author morganm
 *
 */
@OneArgStrategy
public class ModeDistanceLimits extends ModeStrategyImpl {
	private String arg;
	private int minDistance = 0;
	private int maxDistance = Integer.MAX_VALUE;

	public ModeDistanceLimits(String arg) {
		this.arg = arg;
	}
	
	public int getMinDistance() { return minDistance; }
	public int getMaxDistance() { return maxDistance; }
	
	@Override
	public void validate() throws StrategyException {
		if( arg == null ) {
            throw new StrategyException(getStrategyConfigName()+" not given any bounds; ignoring strategy");
		}
		
		String[] args = arg.split(";");
		if( args.length < 2 ) {
			try {
				maxDistance = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing argument, not a number: "+args[0]);
			}
			
			logInfo(getStrategyConfigName()+" only given one bound, assuming bound is maxDistance (maxDistance="+maxDistance+")");
		}
		else {
			try {
				minDistance = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing minY argument, not a number: "+args[0]);
			}
			
			try {
				maxDistance = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing maxY argument, not a number: "+args[1]);
			}
		}
	}
	
	@Override
	public String getStrategyConfigName() {
		return "modeDistanceLimits";
	}

	@Override
	public StrategyMode getMode() {
		return StrategyMode.MODE_DISTANCE_LIMITS;
	}

	@Override
	public boolean isAdditive() {
		return true;
	}
}
