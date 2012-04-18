/**
 * 
 */
package org.morganm.homespawnplus.strategy;


/** Base strategy for HomeModes. Implements common logic.
 * 
 * @author morganm
 *
 */
public abstract class HomeModeStrategy extends BaseStrategy {

	protected abstract HomeMode getHomeMode();
	protected boolean isAdditive() { return false; }
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		// if it's not an additive mode, then clear modes to "switch" to new mode
		if( !isAdditive() )
			context.getCurrentModes().clear();
		
		context.getCurrentModes().add(getHomeMode());
		logVerbose("Evaluated mode change strategy, new mode = "+context.getCurrentModes().toString());
		return null;
	}
}
