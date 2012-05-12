/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import java.util.List;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.strategy.HomeMode;
import org.morganm.homespawnplus.strategy.HomeStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class HomeNamedHome extends HomeStrategy {
	private String homeName;
	
	public HomeNamedHome() {}
	public HomeNamedHome(String homeName) {
		this.homeName = homeName;
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		Home home = null;
		
		// take the name from the argument, if given
		String name = context.getArg();
		// otherwise use the name given at instantiation
		if( name == null )
			name = homeName;
		
		debug.debug("HomeNamedHome: name=",name);

		if( name != null ) {
			final List<HomeMode> currentModes = context.getCurrentModes();

			home = plugin.getUtil().getHomeByName(context.getPlayer().getName(), name);
			
			debug.debug("HomeNamedHome: home pre-modes=",home,", current modes=",currentModes);
			
			if( isModeEnabled(currentModes, HomeMode.MODE_HOME_DEFAULT_ONLY) && !home.isDefaultHome() )
				home = null;
			if( isModeEnabled(currentModes, HomeMode.MODE_HOME_BED_ONLY) && !home.isBedHome() )
				home = null;
			if( isModeEnabled(currentModes, HomeMode.MODE_HOME_NO_BED) && home.isBedHome() )
				home = null;
			if( isModeEnabled(currentModes, HomeMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
				logVerbose("Home ",home," skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
				home = null;
			}
		}
		else
			log.warning(logPrefix+ " Strategy "+getStrategyConfigName()+" was not given a homeName argument, nothing to do");
		
		return new StrategyResult(home);
	}

	@Override
	public String getStrategyConfigName() {
		return "homeNamedHome";
	}

}
