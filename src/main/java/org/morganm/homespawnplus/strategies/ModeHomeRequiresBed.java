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
public class ModeHomeRequiresBed extends ModeStrategy {

	@Override
	public String getStrategyConfigName() {
		return "modeRequiresBed";
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_HOME_REQUIRES_BED;
	}

	/** This mode is additive with other modes.
	 * 
	 */
	@Override
	protected boolean isAdditive() {
		return true;
	}

}
