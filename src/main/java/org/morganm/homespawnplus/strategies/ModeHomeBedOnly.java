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
public class ModeHomeBedOnly extends HomeModeStrategy {
	@Override
	protected HomeMode getHomeMode() {
		return HomeMode.MODE_HOME_BED_ONLY;
	}
	
	@Override
	public String getStrategyConfigName() {
		return "modeHomeBedOnly";
	}

}
