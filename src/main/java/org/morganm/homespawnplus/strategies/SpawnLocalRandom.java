/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
public class SpawnLocalRandom extends BaseStrategy {
	private Random random = new Random(System.currentTimeMillis());
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		Spawn spawn = null;
		
		String playerLocalWorld = context.getEventLocation().getWorld().getName();
		Set<Spawn> allSpawns = plugin.getStorage().getSpawnDAO().findAllSpawns();
		ArrayList<Spawn> spawnChoices = new ArrayList<Spawn>(5);
		for(Spawn theSpawn : allSpawns) {
			if( playerLocalWorld.equals(theSpawn.getWorld()) ) {
				spawnChoices.add(theSpawn);
			}
		}
		if( spawnChoices.size() > 0 ) {
			int randomChoice = random.nextInt(spawnChoices.size());
			spawn = spawnChoices.get(randomChoice);
		}
		
		return new StrategyResult(spawn);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnLocalRandom";
	}

}
