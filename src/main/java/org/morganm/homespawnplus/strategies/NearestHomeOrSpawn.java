/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.bukkit.Location;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyFactory;
import org.morganm.homespawnplus.strategy.StrategyResult;

/** Strategy to return the nearest home or spawn, whichever is closer.
 * 
 * @author morganm
 *
 */
public class NearestHomeOrSpawn extends BaseStrategy {
	private final HomeNearestHome nearestHome;
	private final SpawnNearestSpawn nearestSpawn;
	
	public NearestHomeOrSpawn() throws StrategyException {
		nearestHome = (HomeNearestHome) StrategyFactory.newStrategy(HomeNearestHome.class);
		nearestSpawn = (SpawnNearestSpawn) StrategyFactory.newStrategy(SpawnNearestSpawn.class);
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		StrategyResult homeResult = nearestHome.evaluate(context);
		StrategyResult spawnResult = nearestSpawn.evaluate(context);
		
		Location homeLocation;
		Location spawnLocation;
		
		// if either one is null, return the other
		if( homeResult == null )
			return spawnResult;
		else {
			homeLocation = homeResult.getLocation();
			if( homeLocation == null )
				return spawnResult;
		}
		if( spawnResult == null )
			return homeResult;
		else {
			spawnLocation = spawnResult.getLocation();
			if( spawnLocation == null )
				return homeResult;
		}

		double homeDistance = context.getPlayer().getLocation().distance(homeLocation);
		double spawnDistance = context.getPlayer().getLocation().distance(spawnLocation);
		
		// otherwise, compare the results and return the closer one
		if( homeDistance < spawnDistance )
			return homeResult;
		else
			return spawnResult;
	}

	@Override
	public String getStrategyConfigName() {
		return "nearestHomeOrSpawn";
	}

}
