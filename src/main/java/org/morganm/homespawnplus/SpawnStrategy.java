/**
 * 
 */
package org.morganm.homespawnplus;

import org.morganm.homespawnplus.config.ConfigOptions;

/**
 * @author morganm
 *
 */
public enum SpawnStrategy {
	HOME_THIS_WORLD_ONLY,
	HOME_MULTI_WORLD,
	HOME_DEFAULT_WORLD,
	SPAWN_THIS_WORLD_ONLY,
	SPAWN_NEW_PLAYER,
	SPAWN_DEFAULT_WORLD,
	SPAWN_GROUP,
	SPAWN_NEAREST_SPAWN,
	HOME_NEAREST_HOME,
	DEFAULT;

	public static SpawnStrategy mapStringToStrategy(String s) {
		SpawnStrategy strategy = null;
		
		if( ConfigOptions.STRATEGY_HOME_THIS_WORLD_ONLY.equals(s) ) {
			strategy = HOME_THIS_WORLD_ONLY;
		}
		else if( ConfigOptions.STRATEGY_HOME_MULTI_WORLD.equals(s) ) {
			strategy = HOME_MULTI_WORLD;
		}
		else if( ConfigOptions.STRATEGY_HOME_DEFAULT_WORLD.equals(s) ) {
			strategy = HOME_DEFAULT_WORLD;
		}
		else if( ConfigOptions.STRATEGY_HOME_NEAREST_HOME.equals(s) ) {
			strategy = HOME_NEAREST_HOME;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_THIS_WORLD_ONLY.equals(s) ) {
			strategy = SPAWN_THIS_WORLD_ONLY;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_NEW_PLAYER.equals(s) ) {
			strategy = SPAWN_NEW_PLAYER;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_DEFAULT_WORLD.equals(s) ) {
			strategy = SPAWN_DEFAULT_WORLD;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_GROUP.equals(s) ) {
			strategy = SPAWN_GROUP;
		}
		else if( ConfigOptions.STRATEGY_NEAREST_SPAWN.equals(s) ) {
			strategy = SPAWN_NEAREST_SPAWN;
		}
		else if( ConfigOptions.STRATEGY_DEFAULT.equals(s) ) {
			strategy = DEFAULT;
		}

		if( strategy == null )
			throw new IllegalArgumentException(s+" is not a valid SpawnStrategy");
		
		return strategy;
	}
	
}
