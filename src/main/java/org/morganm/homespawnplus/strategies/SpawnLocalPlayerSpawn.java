/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnLocalPlayerSpawn extends BaseStrategy {
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		StrategyResult result = null;
		
		Player p = context.getPlayer();
		String worldName = context.getEventLocation().getWorld().getName();
		debug.debug("SpawnLocalPlayerSpawn.evaluate() worldName=",worldName);
		
		PlayerSpawnDAO dao = plugin.getStorage().getPlayerSpawnDAO();
		PlayerSpawn ps = dao.findByWorldAndPlayerName(worldName, p.getName());
		
		if( ps != null ) {
			if( ps.getSpawn() != null )
				result = new StrategyResult(ps.getSpawn());
			else
				result = new StrategyResult(ps.getLocation());
		}
		
		return result;
	}
}
