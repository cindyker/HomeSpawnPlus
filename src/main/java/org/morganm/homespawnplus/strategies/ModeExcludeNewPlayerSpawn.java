/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyMode;

/** Mode to exclude newPlayer spawn from choice list when selecting
 * random or nearby spawns.
 * 
 * @author morganm
 *
 */
public class ModeExcludeNewPlayerSpawn extends ModeStrategy {
	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_EXCLUDE_NEW_PLAYER_SPAWN;
	}
	
	@Override
	protected boolean isAdditive() {
		return false;
	}
}
