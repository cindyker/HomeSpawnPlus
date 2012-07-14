/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyMode;

/**
 * @author morganm
 *
 */
public class ModeSourceWorld extends ModeStrategy {
	private String worldName;

	public ModeSourceWorld(String worldName) {
		this.worldName = worldName;
	}
	
	public String getWorldName() {
		return worldName;
	}

	@Override
	public void validate() throws StrategyException {
		if( worldName == null )
			throw new StrategyException("Error validating strategy "+getStrategyConfigName()+": strategy argument is null");
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_SOURCE_WORLD;
	}
	
	@Override
	protected boolean isAdditive() {
		return false;
	}
}
