/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.List;

/** Container class: contains information related to a player spawning.
 * 
 * @author morganm
 *
 */
public class SpawnInfo {
	/* Spawn strategies we are supposed to use.
	 */
	public List<SpawnStrategy> spawnStrategies;
	
	/* The event type, should be one of:
	 * 
	 *   ConfigOptions.SETTING_JOIN_BEHAVIOR		// used when player is joining the server
	 *   ConfigOptions.SETTING_DEATH_BEHAVIOR		// used when player is respawning after death
	 *   ConfigOptions.SETTING_SPAWN_BEHAVIOR		// used when player is invoking spawn using a command
	 * 
	 */
	public String spawnEventType;
	
	/* True if the player spawning is their first login.
	 */
	public boolean isFirstLogin = false;
	
	public String argData;
}
