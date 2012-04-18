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
public class Default extends BaseStrategy {

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return new StrategyResult(true, true);
	}

	@Override
	public String getStrategyConfigName() {
		return "default";
	}

}
