/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import java.util.List;
import java.util.Set;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.strategy.HomeMode;
import org.morganm.homespawnplus.strategy.HomeStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class HomeAnyWorld extends HomeStrategy {

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		final List<HomeMode> currentModes = context.getCurrentModes();
		
		// get the Set of homes for this player for ALL worlds
		Set<Home> homes = plugin.getStorage().getHomeDAO().findHomesByPlayer(context.getPlayer().getName());
		debug.debug("HomeAnyWorld: homes = ", homes);
		
		Home home = null;
		
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				// skip this home if MODE_HOME_REQUIRES_BED is set and no bed is nearby
				if( isModeEnabled(currentModes, HomeMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(h) ) {
					logVerbose(" Home ",h," skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
					continue;
				}
				
				// in "normal" or "any" mode, we just grab the first home we find
				if( isModeEnabled(currentModes, HomeMode.MODE_HOME_NORMAL) ||
						isModeEnabled(currentModes, HomeMode.MODE_HOME_ANY) ) {
					home = h;
					break;
				}
				else if( isModeEnabled(currentModes, HomeMode.MODE_HOME_BED_ONLY) && h.isBedHome() ) {
					home = h;
					break;
				}
				else if( isModeEnabled(currentModes, HomeMode.MODE_HOME_DEFAULT_ONLY) && h.isDefaultHome() ) {
					home = h;
					break;
				}
			}
		}
		
		return new StrategyResult(home);
	}

	@Override
	public String getStrategyConfigName() {
		return "homeAnyWorld";
	}

}
