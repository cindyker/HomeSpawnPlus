/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.integration.worldguard.WorldGuardInterface;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnWorldGuardRegion extends BaseStrategy {
    private WorldGuardInterface wgInterface;

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		if( wgInterface == null )
			wgInterface = plugin.getWorldGuardIntegration().getWorldGuardInterface();
		
		if( !plugin.getWorldGuardIntegration().isEnabled() ) {
			log.warning("Attempted to use "+getStrategyConfigName()+" without WorldGuard installed. Strategy ignored.");
			return null;
		}
		
		return new StrategyResult( wgInterface.getWorldGuardSpawnLocation(context.getPlayer()) );
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnWGregion";
	}

}
