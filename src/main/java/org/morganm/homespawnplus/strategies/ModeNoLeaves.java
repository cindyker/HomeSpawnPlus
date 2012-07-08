/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyMode;

/** Strategy to avoid spawning over leaves.
 * 
 * @author morganm
 *
 */
public class ModeNoLeaves extends ModeStrategy {

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_NO_LEAVES;
	}

	@Override
	protected boolean isAdditive() {
		return true;
	}
}
