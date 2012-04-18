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
public class SpawnSpecificWorld extends BaseStrategy {
	private final String worldName;
	
	public SpawnSpecificWorld(final String worldName) {
		this.worldName = worldName;
	}

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		Spawn spawn = plugin.getUtil().getSpawn(worldName);
		// since worldSpawn is very specific, it's usually an error condition if we didn't
		// find a spawn that the admin identified, so print a warning so they can fix the
		// issue.
		if( spawn == null )
			log.warning("No spawn found for name \""+worldName+"\" for \""+getStrategyConfigName()+"\" strategy");
		
		return new StrategyResult(spawn);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnSpecificWorld";
	}

}
