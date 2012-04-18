/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import java.util.Set;

import org.bukkit.Location;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnNearestSpawn extends BaseStrategy {

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		// simple algorithm for now, it's not called that often and we assume the list
		// of spawns is relatively small (ie. not in the hundreds or thousands).
		final Set<Spawn> allSpawns = plugin.getStorage().getSpawnDAO().findAllSpawns();
		final Location playerLoc = context.getPlayer().getLocation();
		
		final String playerWorld = playerLoc.getWorld().getName();
		double shortestDistance = -1;
		Spawn closestSpawn = null;
		for(Spawn theSpawn : allSpawns) {
			// this fixes a bug in R5+ where non-loaded worlds apparently won't even
			// return valid location or world objects anymore. So we check the String
			// world values before we do anything else and skip worlds that the
			// player is not on.
			if( !playerWorld.equals(theSpawn.getWorld()) )
				continue;
			
			final Location theLocation = theSpawn.getLocation();
			if( theLocation.getWorld().equals(playerLoc.getWorld()) ) {	// must be same world
				double distance = theLocation.distance(playerLoc);
				if( distance < shortestDistance || shortestDistance == -1 ) {
					shortestDistance = distance;
					closestSpawn = theSpawn;
				}
			}
		}
		
		return new StrategyResult(closestSpawn);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnNearest";
	}

}
