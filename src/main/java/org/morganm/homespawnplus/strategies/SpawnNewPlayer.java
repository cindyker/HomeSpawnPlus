/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnNewPlayer extends BaseStrategy
{
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		Spawn spawn = null;
		
		if( plugin.getUtil().isNewPlayer(context.getPlayer()) ) {
			logVerbose("player is detemined to be a new player");
			spawn = plugin.getUtil().getSpawnByName(ConfigOptions.VALUE_NEW_PLAYER_SPAWN);
		}
		else 
			logVerbose("player is detemined to NOT be a new player");

		return new StrategyResult(spawn);
	}

	@Override
	public final String getStrategyConfigName() {
		return "spawnNewPlayer";
	}
}
