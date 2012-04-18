/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.morganm.homespawnplus.util.WorldGuardInterface;

/**
 * @author morganm
 *
 */
public class SpawnWorldGuardRegion extends BaseStrategy {
    private WorldGuardInterface wgInterface;

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		if( wgInterface == null )
			wgInterface = new WorldGuardInterface(plugin);
		
		return new StrategyResult( wgInterface.getWorldGuardSpawnLocation(context.getPlayer()) );
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnWGregion";
	}

}
