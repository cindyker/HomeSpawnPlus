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
public class ModeNoIce extends ModeStrategy {

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_NO_ICE;
	}

	@Override
	protected boolean isAdditive() {
		return true;
	}
}
