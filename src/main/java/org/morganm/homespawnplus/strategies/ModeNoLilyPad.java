/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyMode;

/** Mode that specifies teleports should not be done above lily pads.
 * 
 * @author morganm
 *
 */
public class ModeNoLilyPad extends ModeStrategy {
	@Override
	public String getStrategyConfigName() {
		return "modeNoLilyPad";
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_NO_LILY_PAD;
	}
	
	@Override
	protected boolean isAdditive() {
		return true;
	}
}
