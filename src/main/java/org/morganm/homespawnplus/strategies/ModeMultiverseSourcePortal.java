/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.morganm.homespawnplus.strategy.ModeStrategy;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyMode;

/** Mode used for detecting whether or not a Multiverse source portal
 * is involved in the current strategy chain.
 * 
 * @author morganm
 *
 */
public class ModeMultiverseSourcePortal extends ModeStrategy {
	private String portalName;

	public ModeMultiverseSourcePortal(String portalName) {
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
		return StrategyMode.MODE_MULTIVERSE_SOURCE_PORTAL;
	}
	
	@Override
	protected boolean isAdditive() {
		return false;
	}
}
