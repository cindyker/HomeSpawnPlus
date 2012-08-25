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
public class ModeMultiverseDestinationPortal extends ModeStrategy {
	private String portalName;

	public ModeMultiverseDestinationPortal(String portalName) {
		this.portalName = portalName;
	}
	
	public String getPortalName() {
		return portalName;
	}

	@Override
	public void validate() throws StrategyException {
		if( !plugin.getMultiverseIntegration().isMultiverseEnabled() )
			throw new StrategyException("Error validating strategy "+getStrategyConfigName()+": Multiverse-Core is not running");
		if( !plugin.getMultiverseIntegration().isMultiversePortalsEnabled() )
			throw new StrategyException("Error validating strategy "+getStrategyConfigName()+": Multiverse-Portals is not running");
		
		if( portalName == null )
			throw new StrategyException("Error validating strategy "+getStrategyConfigName()+": strategy argument is null");
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_MULTIVERSE_DESTINATION_PORTAL;
	}
	
	@Override
	protected boolean isAdditive() {
		return false;
	}
}
