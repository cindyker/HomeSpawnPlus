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
package com.andune.minecraft.hsp.strategies;

import javax.inject.Inject;


import com.andune.minecraft.hsp.strategy.ModeStrategyImpl;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyException;
import com.andune.minecraft.hsp.strategy.StrategyMode;

/**
 * @author morganm
 *
 */
@OneArgStrategy
public class ModeYBounds extends ModeStrategyImpl {
	private String arg;
	private int minY = 1;
	private int maxY = 255;

	@Inject
	public ModeYBounds(String arg) {
		this.arg = arg;
	}
	
	public int getMinY() { return minY; }
	public int getMaxY() { return maxY; }
	
	@Override
	public void validate() throws StrategyException {
		if( arg == null ) {
			logInfo(getStrategyConfigName()+" not given any bounds, using default bounds (minY="+minY+", maxY="+maxY+")");
			return;
		}
		
		String[] args = arg.split(";");
		if( args.length < 2 ) {
			try {
				minY = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing argument, not a number: "+args[0]);
			}
			
			logInfo(getStrategyConfigName()+" only given one bound, assuming bound is minY (minY="+minY+")");
		}
		else {
			try {
				minY = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing minY argument, not a number: "+args[0]);
			}
			
			try {
				maxY = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing maxY argument, not a number: "+args[1]);
			}
		}
	}
	
	@Override
	public String getStrategyConfigName() {
		return "modeYBounds";
	}

	@Override
	public StrategyMode getMode() {
		return StrategyMode.MODE_YBOUNDS;
	}

	@Override
	public boolean isAdditive() {
		return true;
	}
}
