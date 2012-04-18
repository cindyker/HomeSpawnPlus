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
public class HomeDefaultWorld extends HomeStrategy {

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return new StrategyResult( super.getModeHome(context, plugin.getUtil().getDefaultWorld()) );
	}

	@Override
	public String getStrategyConfigName() {
		return "homeDefaultWorld";
	}

}
