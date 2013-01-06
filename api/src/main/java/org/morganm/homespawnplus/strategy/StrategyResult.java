package org.morganm.homespawnplus.strategy;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.server.api.Location;

public interface StrategyResult {

	public abstract void setLocation(Location l);

	public abstract boolean isSuccess();

	public abstract Location getLocation();

	public abstract Home getHome();

	public abstract Spawn getSpawn();

	public abstract boolean isExplicitDefault();

	public abstract void setContext(StrategyContext context);

	public abstract StrategyContext getContext();

}