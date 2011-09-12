/**
 * 
 */
package org.morganm.homespawnplus.config;

/**
 * @author morganm
 *
 */
public interface ConfigOptions {
	public static final String VERBOSE_LOGGING = "verboseLogging";
	
	public static final String STORAGE_TYPE = "core.storage";
	public static final String DEFAULT_PERMISSIONS = "core.defaultPermissions";
	
	public static final String USE_WARMUPS = "warmups.enabled";
	public static final String WARMUPS_ON_MOVE_CANCEL = "warmups.onMoveCancel";
	public static final String WARMUPS_ON_DAMAGE_CANCEL = "warmups.onDamageCancel";
	
	public static final String DEFAULT_WORLD = "spawn.defaultWorld";
	public static final String COMMAND_TOGGLE_BASE = "disabledCommands.";
	
	public static final String ENABLE_RECORD_LAST_LOGOUT = "spawn.recordLastLogout";
	public static final String ENABLE_HOME_BEDS = "home.bedsethome";
	public static final String ENABLE_GROUP_SPAWN = "spawn.groups";

	public static final String VALUE_JOIN_DEFAULT = "default";
	public static final String VALUE_JOIN_HOME = "home";
	public static final String VALUE_JOIN_GLOBAL = "global";
	public static final String VALUE_JOIN_GROUP = "group";
	public static final String VALUE_JOIN_WORLD = "world";
	
	public static final String SETTING_JOIN_BEHAVIOR = "spawn.onjoin";
	public static final String SETTING_DEATH_BEHAVIOR = "spawn.ondeath";
	public static final String SETTING_SPAWN_BEHAVIOR = "spawn.oncommand";

	public static final String VALUE_DEFAULT = "default";
	public static final String VALUE_HOME = "home";
	public static final String VALUE_MULTIHOME = "multihome";
	public static final String VALUE_GROUP = "group";
	public static final String VALUE_WORLD = "world";
	public static final String VALUE_GLOBAL = "global";
	
	public static final String SETTING_WORLD_OVERRIDE = "spawn.override_world";
	
	public static final String COOLDOWN_BASE = "cooldown.";
	public static final String WARMUP_BASE = "warmup.";
	
	public static final String STRATEGY_HOME_THIS_WORLD_ONLY = "homeLocalWorld";
	public static final String STRATEGY_HOME_DEFAULT_WORLD = "homeDefaultWorld";
	public static final String STRATEGY_HOME_MULTI_WORLD = "homeMultiWorld";
	public static final String STRATEGY_HOME_NEAREST_HOME = "homeNearestHome";
	public static final String STRATEGY_SPAWN_THIS_WORLD_ONLY = "spawnLocalWorld";
	public static final String STRATEGY_SPAWN_DEFAULT_WORLD = "spawnDefaultWorld";
	public static final String STRATEGY_SPAWN_GROUP = "spawnGroup";
	public static final String STRATEGY_NEAREST_SPAWN = "spawnNearestSpawn";
	public static final String STRATEGY_DEFAULT = "default";
}
