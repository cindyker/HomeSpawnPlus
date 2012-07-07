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
