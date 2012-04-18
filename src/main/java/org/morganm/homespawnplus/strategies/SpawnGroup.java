/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyFactory;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnGroup extends BaseStrategy {
	private SpawnGroupSpecificWorld sgsw;

	public SpawnGroup() throws StrategyException {
		sgsw = (SpawnGroupSpecificWorld) StrategyFactory.newStrategy(SpawnGroupSpecificWorld.class, null);
		
	}
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return sgsw.evaluate(context, context.getPlayer().getWorld().getName());
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnGroup";
	}

}
