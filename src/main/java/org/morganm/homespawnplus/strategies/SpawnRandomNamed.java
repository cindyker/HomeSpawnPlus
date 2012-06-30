/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import java.util.Random;

import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyResult;

/** Spawn strategy to spawn a random strategy out of a named list
 * 
 * @author morganm
 *
 */
public class SpawnRandomNamed extends BaseStrategy {
	private Random random = new Random(System.currentTimeMillis());
	private String[] names;

	public SpawnRandomNamed(final String arg) {
		if( arg != null )
			this.names = arg.split(",");
	}
	
	@Override
	public void validate() throws StrategyException {
		if( names == null )
			throw new StrategyException("no named spawns given");
		
		for(int i=0; i < names.length; i++) {
			Spawn spawn = plugin.getUtil().getSpawnByName(names[i]);
			if( spawn == null )
				log.warning(logPrefix+" strategy "+getStrategyConfigName()+" references named spawn \""+names[i]+"\", which doesn't exist");
		}
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		int number = random.nextInt(names.length);
		String name = names[number];
		
		Spawn spawn = plugin.getUtil().getSpawnByName(name);
		if( spawn == null )
			log.warning(logPrefix+" No spawn found for name \""+name+"\" for \""+getStrategyConfigName()+"\" strategy");

		return new StrategyResult(spawn);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnRandomNamed";
	}

}
