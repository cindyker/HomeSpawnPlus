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
public class ModeRememberLocation extends ModeStrategy {
	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_REMEMBER_LOCATION;
	}

	@Override
	protected boolean isAdditive() {
		return true;
	}
}
