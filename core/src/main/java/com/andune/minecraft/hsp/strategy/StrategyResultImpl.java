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
package com.andune.minecraft.hsp.strategy;

import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyResult;

/** Class to store result data from strategy evaluation.
 * 
 * @author andune
 *
 */
public class StrategyResultImpl implements StrategyResult {
	private Location location;
	private Home home;
	private Spawn spawn;
	private boolean explicitDefault = false;
	private StrategyContext context;
	
	/** This is true if the strategy is considered a successful match, such
	 * as having a location result or being a mode change, default value, etc 
	 * 
	 */
	private boolean isSuccess = false;
	
	public StrategyResultImpl(Home home) {
		this.home = home;
		if( home != null )
			this.location = home.getLocation();
		setSuccess();
	}
	public StrategyResultImpl(Spawn spawn) {
		this.spawn = spawn;
		if( spawn != null )
			this.location = spawn.getLocation();
		setSuccess();
	}
	public StrategyResultImpl(Location location) {
		this.location = location;
		setSuccess();
	}
	public StrategyResultImpl(boolean isSuccess, boolean explicitDefault) {
		this.isSuccess = isSuccess;
		this.explicitDefault = explicitDefault;
	}
	
	// determine success based on whether or not we have a location
	private void setSuccess() {
		if( this.location != null )
			this.isSuccess = true;
	}
	
	@Override
	public void setLocation(Location l) {
		location = l;
	}
	
	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.strategy.StrategyResult#isSuccess()
	 */
	@Override
	public boolean isSuccess() {
		return isSuccess;
	}
	@Override
	public Location getLocation() {
		return location;
	}
	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.strategy.StrategyResult#getHome()
	 */
	@Override
	public Home getHome() {
		return home;
	}
	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.strategy.StrategyResult#getSpawn()
	 */
	@Override
	public Spawn getSpawn() {
		return spawn;
	}
	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.strategy.StrategyResult#isExplicitDefault()
	 */
	@Override
	public boolean isExplicitDefault() {
		return explicitDefault;
	}
	@Override
	public void setContext(StrategyContext context) {
		this.context = context;
	}
	@Override
	public StrategyContext getContext() {
		return context;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[loc={");
		sb.append(getLocation() != null ? getLocation().shortLocationString() : "null");
		sb.append("}, home=");
		sb.append(getHome());
		sb.append(", spawn={");
		sb.append(getSpawn());
		sb.append("}]");
		return sb.toString();
	}
}
