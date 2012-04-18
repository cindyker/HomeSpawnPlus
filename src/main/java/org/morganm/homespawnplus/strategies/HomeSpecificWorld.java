/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.HomeStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class HomeSpecificWorld extends HomeStrategy {
	private final String worldName;
	
	public HomeSpecificWorld(final String worldName) {
		this.worldName = worldName;
	}
	
	@Override
	public String getStrategyConfigName() {
		return "homeSpecificWorld";
	}

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return new StrategyResult(getModeHome(context, worldName));
	}

}
