/**
 * 
 */
package org.morganm.homespawnplus;

import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.config.ConfigOptions;

/**
 * @author morganm
 *
 */
public class SpawnStrategy {
	public enum Type {
		HOME_THIS_WORLD_ONLY,
		HOME_MULTI_WORLD,
		HOME_DEFAULT_WORLD,
		HOME_NEAREST_HOME,
		HOME_SPECIFIC_WORLD,
		HOME_ANY_WORLD,
		MODE_HOME_NORMAL,
		MODE_HOME_BED_ONLY,
		MODE_HOME_DEFAULT_ONLY,
		MODE_HOME_ANY,
		SPAWN_THIS_WORLD_ONLY,
		SPAWN_NEW_PLAYER,
		SPAWN_DEFAULT_WORLD,
		SPAWN_GROUP,
		SPAWN_GROUP_SPECIFIC_WORLD,
		SPAWN_NEAREST_SPAWN,
		SPAWN_SPECIFIC_WORLD,
		SPAWN_NAMED_SPAWN,
		SPAWN_WG_REGION,
		DEFAULT;		
	};
	
	private Type type;
	private String data;
	
	public SpawnStrategy() {}
	public SpawnStrategy(Type type) { this.type = type; }
	
	public Type getType() { return type; }
	public String getData() { return data; }
	
	public String toString() {
		return type.toString();
	}

	public static SpawnStrategy mapStringToStrategy(String s) throws ConfigException {
		SpawnStrategy strategy = new SpawnStrategy();
		
		if( ConfigOptions.STRATEGY_HOME_THIS_WORLD_ONLY.equals(s) ) {
			strategy.type = Type.HOME_THIS_WORLD_ONLY;
		}
		else if( ConfigOptions.STRATEGY_HOME_MULTI_WORLD.equals(s) ) {
			strategy.type = Type.HOME_MULTI_WORLD;
		}
		else if( ConfigOptions.STRATEGY_HOME_DEFAULT_WORLD.equals(s) ) {
			strategy.type = Type.HOME_DEFAULT_WORLD;
		}
		else if( ConfigOptions.STRATEGY_HOME_NEAREST_HOME.equals(s) ) {
			strategy.type = Type.HOME_NEAREST_HOME;
		}
		else if( ConfigOptions.STRATEGY_HOME_ANY_WORLD.equals(s) ) {
			strategy.type = Type.HOME_ANY_WORLD;
		}
		else if( s.startsWith(ConfigOptions.STRATEGY_HOME_SPECIFIC_WORLD) ) {
			String[] strings = s.split(":");
			if( strings.length < 2 )
				throw new ConfigException("Invalid strategy: "+s);
			
			strategy.type = Type.HOME_SPECIFIC_WORLD;
			strategy.data = strings[1];
		}
		else if( ConfigOptions.STRATEGY_MODE_HOME_NORMAL.equals(s) ) {
			strategy.type = Type.MODE_HOME_NORMAL;
		}
		else if( ConfigOptions.STRATEGY_MODE_HOME_BED_ONLY.equals(s) ) {
			strategy.type = Type.MODE_HOME_BED_ONLY;
		}
		else if( ConfigOptions.STRATEGY_MODE_HOME_DEFAULT_ONLY.equals(s) ) {
			strategy.type = Type.MODE_HOME_DEFAULT_ONLY;
		}
		else if( ConfigOptions.STRATEGY_MODE_HOME_ANY.equals(s) ) {
			strategy.type = Type.MODE_HOME_ANY;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_THIS_WORLD_ONLY.equals(s) ) {
			strategy.type = Type.SPAWN_THIS_WORLD_ONLY;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_NEW_PLAYER.equals(s) ) {
			strategy.type = Type.SPAWN_NEW_PLAYER;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_DEFAULT_WORLD.equals(s) ) {
			strategy.type = Type.SPAWN_DEFAULT_WORLD;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_GROUP.equals(s) ) {
			strategy.type = Type.SPAWN_GROUP;
		}
		else if( s.startsWith(ConfigOptions.STRATEGY_SPAWN_GROUP_SPECIFIC_WORLD) ) {
			String[] strings = s.split(":");
			if( strings.length < 2 )
				throw new ConfigException("Invalid strategy: "+s);
			
			strategy.type = Type.SPAWN_GROUP_SPECIFIC_WORLD;
			strategy.data = strings[1];
		}
		else if( s.startsWith(ConfigOptions.STRATEGY_SPAWN_SPECIFIC_WORLD) ) {
			String[] strings = s.split(":");
			if( strings.length < 2 )
				throw new ConfigException("Invalid strategy: "+s);
			
			strategy.type = Type.SPAWN_SPECIFIC_WORLD;
			strategy.data = strings[1];
		}
		else if( s.startsWith(ConfigOptions.STRATEGY_SPAWN_NAMED_SPAWN) ) {
			String[] strings = s.split(":");
			if( strings.length < 2 )
				throw new ConfigException("Invalid strategy: "+s);
			
			strategy.type = Type.SPAWN_NAMED_SPAWN;
			strategy.data = strings[1];
		}
		else if( ConfigOptions.STRATEGY_NEAREST_SPAWN.equals(s) ) {
			strategy.type = Type.SPAWN_NEAREST_SPAWN;
		}
		else if( ConfigOptions.STRATEGY_SPAWN_WG_REGION.equals(s) ) {
			strategy.type = Type.SPAWN_WG_REGION;
		}
		else if( ConfigOptions.STRATEGY_DEFAULT.equals(s) ) {
			strategy.type = Type.DEFAULT;
		}

		if( strategy.type == null )
			throw new IllegalArgumentException(s+" is not a valid SpawnStrategy");
		
		return strategy;
	}
	
}
