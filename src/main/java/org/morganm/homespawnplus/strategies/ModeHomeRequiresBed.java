/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.HomeMode;
import org.morganm.homespawnplus.strategy.HomeModeStrategy;

/**
 * @author morganm
 *
 */
public class ModeHomeRequiresBed extends HomeModeStrategy {

	@Override
	public String getStrategyConfigName() {
		return "modeRequiresBed";
	}

	@Override
	protected HomeMode getHomeMode() {
		return HomeMode.MODE_HOME_REQUIRES_BED;
	}

	/** This mode is additive with other modes.
	 * 
	 */
	@Override
	protected boolean isAdditive() {
		return true;
	}

}
