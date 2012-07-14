/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.ArrayList;

import org.morganm.homespawnplus.util.Teleport;

/** Different modes that strategies can run under, which can change
 * the behavior of other strategies.
 * 
 * @author morganm
 *
 */
public enum StrategyMode {
	MODE_HOME_NORMAL,
	MODE_HOME_BED_ONLY,
	MODE_HOME_NO_BED,
	MODE_HOME_DEFAULT_ONLY,
	MODE_HOME_ANY,
	MODE_HOME_REQUIRES_BED,
	MODE_NO_WATER (Teleport.FLAG_NO_WATER),
	MODE_NO_LILY_PAD (Teleport.FLAG_NO_LILY_PAD),
	MODE_NO_LEAVES (Teleport.FLAG_NO_LEAVES),
	MODE_NO_ICE (Teleport.FLAG_NO_ICE),
	MODE_YBOUNDS,
	MODE_DEFAULT,
	MODE_REMEMBER_SPAWN,
	MODE_REMEMBER_LOCATION;
	
	// associated Teleport safeMode flag, if any
	private int flagId = 0;
	
	private StrategyMode() {}
	private StrategyMode(int flagId) {
		this.flagId = flagId;
	}
	
	public int getFlagId() { return flagId; }
	
	private static StrategyMode[] safeModes;
	static {
		ArrayList<StrategyMode> list = new ArrayList<StrategyMode>(5);
		for(StrategyMode value : values()) {
			if( value.flagId > 0 )
				list.add(value);
		}
		
		safeModes = list.toArray(new StrategyMode[] {});
	}
	
	public static StrategyMode[] getSafeModes() {
		return safeModes;
	}
}
