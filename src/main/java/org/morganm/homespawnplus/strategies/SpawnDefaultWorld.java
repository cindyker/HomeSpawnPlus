/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnDefaultWorld extends BaseStrategy {

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return new StrategyResult(plugin.getUtil().getDefaultSpawn().getLocation());
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnDefaultWorld";
	}

}
