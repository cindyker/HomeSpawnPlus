/**
 * 
 */
package org.morganm.homespawnplus.strategy;

/** Different modes that strategies can run under, which changes
 * the behavior of other strategies based on the mode.
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
	MODE_NO_WATER,
	MODE_YBOUNDS,
	MODE_DEFAULT
}
