/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.StrategyMode;
import org.morganm.homespawnplus.strategy.ModeStrategy;

/**
 * @author morganm
 *
 */
public class ModeHomeBedOnly extends ModeStrategy {
	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_HOME_BED_ONLY;
	}
	
	@Override
	public String getStrategyConfigName() {
		return "modeHomeBedOnly";
	}

}
