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

import org.bukkit.Location;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;

/** Class to store result data from strategy evaluation.
 * 
 * @author morganm
 *
 */
public class StrategyResult {
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
	
	public StrategyResult(Home home) {
		this.home = home;
		if( home != null )
			this.location = home.getLocation();
		setSuccess();
	}
	public StrategyResult(Spawn spawn) {
		this.spawn = spawn;
		if( spawn != null )
			this.location = spawn.getLocation();
		setSuccess();
	}
	public StrategyResult(Location location) {
		this.location = location;
		setSuccess();
	}
	public StrategyResult(boolean isSuccess, boolean explicitDefault) {
		this.isSuccess = isSuccess;
		this.explicitDefault = explicitDefault;
	}
	
	// determine success based on whether or not we have a location
	private void setSuccess() {
		if( this.location != null )
			this.isSuccess = true;
	}
	
	public void setLocation(Location l) {
		location = l;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}
	public Location getLocation() {
		return location;
	}
	public Home getHome() {
		return home;
	}
	public Spawn getSpawn() {
		return spawn;
	}
	public boolean isExplicitDefault() {
		return explicitDefault;
	}
	public void setContext(StrategyContext context) {
		this.context = context;
	}
	public StrategyContext getContext() {
		return context;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[loc={");
		sb.append(HomeSpawnPlus.getInstance().getUtil().shortLocationString(getLocation()));
		sb.append("}, home=");
		sb.append(getHome());
		sb.append(", spawn={");
		sb.append(getSpawn());
		sb.append("}]");
		return sb.toString();
	}
}
