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
public class SpawnLocalWorld extends BaseStrategy {

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return new StrategyResult( plugin.getUtil().getSpawn(context.getPlayer().getWorld().getName()) );
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnLocalWorld";
	}

}
