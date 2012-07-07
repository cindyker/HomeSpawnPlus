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
public class ModeHomeDefaultOnly extends ModeStrategy {

	@Override
	public String getStrategyConfigName() {
		return "modeHomeDefaultOnly";
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_HOME_DEFAULT_ONLY;
	}

	@Override
	protected boolean isAdditive() {
		return false;
	}
}
