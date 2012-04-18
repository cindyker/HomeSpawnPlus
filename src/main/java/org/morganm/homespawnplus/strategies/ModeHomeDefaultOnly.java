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
public class ModeHomeDefaultOnly extends HomeModeStrategy {

	@Override
	public String getStrategyConfigName() {
		return "modeHomeDefaultOnly";
	}

	@Override
	protected HomeMode getHomeMode() {
		return HomeMode.MODE_HOME_DEFAULT_ONLY;
	}

}
