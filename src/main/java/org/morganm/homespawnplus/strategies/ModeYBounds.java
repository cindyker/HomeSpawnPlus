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
public class ModeYBounds extends ModeStrategy {
	private String arg;
	private int minY = 1;
	private int maxY = 255;

	public ModeYBounds() {}
	public ModeYBounds(String arg) {
		this.arg = arg;
	}
	
	public int getMinY() { return minY; }
	public int getMaxY() { return maxY; }
	
	@Override
	public void validate() throws StrategyException {
		if( arg == null ) {
			logInfo(getStrategyConfigName()+" not given any bounds, using default bounds (minY="+minY+", maxY="+maxY+")");
			return;
		}
		
		String[] args = arg.split(";");
		if( args.length < 2 ) {
			try {
				minY = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing argument, not a number: "+args[0]);
			}
			
			logInfo(getStrategyConfigName()+" only given one bound, assuming bound is minY (minY="+minY+")");
		}
		else {
			try {
				minY = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing minY argument, not a number: "+args[0]);
			}
			
			try {
				maxY = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				throw new StrategyException(getStrategyConfigName()+" Error processing maxY argument, not a number: "+args[1]);
			}
		}
	}
	
	@Override
	public String getStrategyConfigName() {
		return "modeYBounds";
	}

	@Override
	protected StrategyMode getMode() {
		return StrategyMode.MODE_YBOUNDS;
	}

	@Override
	protected boolean isAdditive() {
		return true;
	}
}
