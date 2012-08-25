/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyMode;

/**
 * @author morganm
 *
 */
public class ModeInRegion extends ModeStrategy {
	private String regionName;

	public ModeInRegion(String regionName) {
		this.regionName = regionName;
	}
	
	public String getRegionName() {
		return regionName;
	}

	@Override
	public void validate() throws StrategyException {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if( p == null )
			throw new StrategyException("Attempt to use "+getStrategyConfigName()+" strategy but WorldGuard is not installed");
		
		if( regionName == null )
			throw new StrategyException("Error validating strategy "+getStrategyConfigName()+": strategy argument is null");
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_IN_REGION;
	}
	
	@Override
	protected boolean isAdditive() {
		return false;
	}
}
