/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyMode;

/**
 * @author morganm
 *
 */
public class ModeDefault extends ModeStrategy {

	@Override
	public String getStrategyConfigName() {
		return "modeDefault";
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_DEFAULT;
	}

	@Override
	protected boolean isAdditive() {
		return false;
	}
}
