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
public class ModeHomeAny extends HomeModeStrategy {

	@Override
	public String getStrategyConfigName() {
		return "modeHomeAny";
	}

	@Override
	protected HomeMode getHomeMode() {
		return HomeMode.MODE_HOME_ANY;
	}

}
