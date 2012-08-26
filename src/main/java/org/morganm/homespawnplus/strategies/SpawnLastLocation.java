/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnLastLocation extends BaseStrategy {
	private String world;
	
	public SpawnLastLocation() {}
	public SpawnLastLocation(final String world) {
		this.world = world;
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		StrategyResult result = null;
		final Player p = context.getPlayer();
		
		// take the world from the argument, if given
		String worldName = context.getArg();
		// otherwise use the name given at instantiation
		if( worldName == null )
			worldName = this.world;

		// if no arg was given at runtime or instantiation, use location
		// of the player to determine the world
		if( worldName == null )
			worldName = context.getEventLocation().getWorld().getName();

		PlayerLastLocationDAO dao = plugin.getStorage().getPlayerLastLocationDAO();
		PlayerLastLocation	 pll = dao.findByWorldAndPlayerName(worldName, p.getName());
		
		if( pll != null )
			result = new StrategyResult(pll.getLocation());
		
		return result;
	}
}
