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
public class HomeLocalWorld extends HomeStrategy {
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return new StrategyResult( super.getModeHome(context, null) );
	}

	@Override
	public final String getStrategyConfigName() {
		return "homeLocalWorld";
	}
}
