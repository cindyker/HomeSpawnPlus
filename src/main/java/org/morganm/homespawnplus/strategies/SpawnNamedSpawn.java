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
public class SpawnNamedSpawn extends BaseStrategy {
	private String namedSpawn;
	
	public SpawnNamedSpawn() {}
	public SpawnNamedSpawn(final String namedSpawn) {
		this.namedSpawn = namedSpawn;
	}

	@Override
	public StrategyResult evaluate(StrategyContext context) {
		// take the name from the argument, if given
		String name = context.getArg();
		// otherwise use the name given at instantiation
		if( name == null )
			name = namedSpawn;
		
		Spawn spawn = plugin.getUtil().getSpawnByName(name);
		
		// since namedSpawn is very specific, it's usually an error condition if we didn't
		// find a named spawn that the admin identified, so print a warning so they can
		// fix the issue.
		if( spawn == null )
			log.warning(logPrefix+" No spawn found for name \""+name+"\" for \""+getStrategyConfigName()+"\" strategy");
		
		return new StrategyResult(spawn);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnNamedSpawn";
	}

}
