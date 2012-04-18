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
public class HomeMultiWorld extends BaseStrategy {

	private HomeLocalWorld homeLocalWorld;
	private HomeDefaultWorld homeDefaultWorld;
	
	public HomeMultiWorld() throws StrategyException {
		homeLocalWorld = (HomeLocalWorld) StrategyFactory.newStrategy(HomeLocalWorld.class);
		homeDefaultWorld = (HomeDefaultWorld) StrategyFactory.newStrategy(HomeDefaultWorld.class);
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		StrategyResult result = homeLocalWorld.evaluate(context);
		if( !result.isSuccess() )
			result = homeDefaultWorld.evaluate(context);
		
		return result;
	}

	@Override
	public String getStrategyConfigName() {
		return "homeMultiWorld";
	}

}
