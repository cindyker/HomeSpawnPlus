/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnGroupSpecificWorld extends BaseStrategy {
	private final String worldName;
	
	public SpawnGroupSpecificWorld(final String worldName) {
		this.worldName = worldName;
	}

	public StrategyResult evaluate(final StrategyContext context, final String world) {
		Spawn spawn = null;
		
		String group = plugin.getPlayerGroup(world, context.getPlayer().getName());
		if( group != null )
			spawn = plugin.getUtil().getGroupSpawn(group, world);

		return new StrategyResult(spawn);		
	}
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		return evaluate(context, worldName);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnGroupSpecificWorld";
	}

}
