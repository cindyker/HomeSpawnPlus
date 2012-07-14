/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import java.util.Set;

import org.bukkit.Location;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.strategy.StrategyMode;
import org.morganm.homespawnplus.strategy.HomeStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class HomeNearestHome extends HomeStrategy {

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		// simple algorithm for now, it's not called that often and we assume the list
		// of homes is relatively small (ie. not in the hundreds or thousands).
		final Set<Home> allHomes = plugin.getStorage().getHomeDAO().findHomesByWorldAndPlayer(
				context.getEventLocation().getWorld().getName(), context.getPlayer().getName());
		final Location playerLoc = context.getEventLocation();
		
		double shortestDistance = -1;
		Home closestHome = null;
		for(Home theHome : allHomes) {
			if( context.isModeEnabled(StrategyMode.MODE_HOME_NO_BED) && theHome.isBedHome() )
				continue;
			
			if( context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(theHome) ) {
				logVerbose("Home ",theHome," skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
				continue;
			}
			
			final Location theLocation = theHome.getLocation();
			if( theLocation.getWorld().equals(playerLoc.getWorld()) ) {	// must be same world
				double distance = theLocation.distance(playerLoc);
				if( distance < shortestDistance || shortestDistance == -1 ) {
					shortestDistance = distance;
					closestHome = theHome;
				}
			}
		}
		
		return new StrategyResult(closestHome);
	}

	@Override
	public String getStrategyConfigName() {
		return "homeNearest";
	}

}
