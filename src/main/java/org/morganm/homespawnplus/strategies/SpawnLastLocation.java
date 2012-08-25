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
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		StrategyResult result = null;
		
		Player p = context.getPlayer();
		String worldName = context.getEventLocation().getWorld().getName();
		
		PlayerLastLocationDAO dao = plugin.getStorage().getPlayerLastLocationDAO();
		PlayerLastLocation	 pll = dao.findByWorldAndPlayerName(worldName, p.getName());
		
		if( pll != null )
			result = new StrategyResult(pll.getLocation());
		
		return result;
	}
}
