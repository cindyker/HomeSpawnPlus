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
public class ModeHomeNormal extends HomeModeStrategy {

	@Override
	protected HomeMode getHomeMode() {
		return HomeMode.MODE_HOME_NORMAL;
	}

	@Override
	public String getStrategyConfigName() {
		return "modeHomeNormal";
	}

}
