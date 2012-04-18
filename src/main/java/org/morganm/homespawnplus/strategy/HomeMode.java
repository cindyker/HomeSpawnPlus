/**
 * 
 */
package org.morganm.homespawnplus.strategy;

/** Different modes that home strategies can run under, which changes
 * the behavior of the strategy based on the mode.
 * 
 * @author morganm
 *
 */
public enum HomeMode {
	MODE_HOME_NORMAL,
	MODE_HOME_BED_ONLY,
	MODE_HOME_NO_BED,
	MODE_HOME_DEFAULT_ONLY,
	MODE_HOME_ANY,
	MODE_HOME_REQUIRES_BED
}
