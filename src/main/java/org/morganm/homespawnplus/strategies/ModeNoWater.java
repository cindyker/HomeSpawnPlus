/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyMode;

/** Mode that specifies teleports should not be done above water.
 * 
 * @author morganm
 *
 */
public class ModeNoWater extends ModeStrategy {

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_NO_WATER;
	}

	@Override
	public String getStrategyConfigName() {
		return "modeNoWater";
	}

	@Override
	protected boolean isAdditive() {
		return true;
	}
}
