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
public class ModeHomeNoBed extends HomeModeStrategy {

	@Override
	protected HomeMode getHomeMode() {
		return HomeMode.MODE_HOME_NO_BED;
	}

	@Override
	public String getStrategyConfigName() {
		return "modeHomeNoBed";
	}

}
