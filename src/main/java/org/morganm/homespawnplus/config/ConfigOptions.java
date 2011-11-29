/**
 * 
 */
package org.morganm.homespawnplus.config;

/**
 * @author morganm
 *
 */
public interface ConfigOptions {
	public static final String VERBOSE_LOGGING = "core.verboseLogging";
	public static final String STRATEGY_VERBOSE_LOGGING = "core.verboseStrategyLogging";
	public static final String DEBUG = "core.debug";
	public static final String DEV_DEBUG = "core.devDebug";
	
	public static final String STORAGE_TYPE = "core.storage";
	public static final String DEFAULT_PERMISSIONS = "core.defaultPermissions";
	public static final String DEFAULT_WORLD = "core.defaultWorld";
	public static final String ENABLE_HOME_BEDS = "core.bedsethome";
	public static final String ENABLE_RECORD_LAST_LOGOUT = "core.recordLastLogout";
//	public static final String ENABLE_GROUP_SPAWN = "core.groupSpawnEnabled";
	public static final String SETTING_WORLD_OVERRIDE = "core.override_world";
	public static final String EVENT_PRIORITY = "core.eventPriority";
	public static final String LAST_HOME_IS_DEFAULT = "core.lastHomeIsDefault";
	
	public static final String USE_WARMUPS = "warmup.enabled";
	public static final String WARMUPS_ON_MOVE_CANCEL = "warmup.onMoveCancel";
	public static final String WARMUPS_ON_DAMAGE_CANCEL = "warmup.onDamageCancel";
	
	public static final String COMMAND_TOGGLE_BASE = "disabledCommands.";
	
	public static final String VALUE_JOIN_DEFAULT = "default";
	public static final String VALUE_JOIN_HOME = "home";
	public static final String VALUE_JOIN_GLOBAL = "global";
	public static final String VALUE_JOIN_GROUP = "group";
	public static final String VALUE_JOIN_WORLD = "world";
	
	public static final String SETTING_EVENTS_BASE = "events";
	public static final String SETTING_EVENTS_WORLDBASE = "world";
	public static final String SETTING_EVENTS_PERMBASE = "permission";
	public static final String SETTING_JOIN_BEHAVIOR = "onJoin";
	public static final String SETTING_DEATH_BEHAVIOR = "onDeath";
	public static final String SETTING_SPAWN_CMD_BEHAVIOR = "onSpawnCommand";
	public static final String SETTING_GROUPSPAWN_CMD_BEHAVIOR = "onGroupSpawnCommand";
	public static final String SETTING_HOME_CMD_BEHAVIOR = "onHomeCommand";

	public static final String VALUE_DEFAULT = "default";
	public static final String VALUE_HOME = "home";
	public static final String VALUE_MULTIHOME = "multihome";
	public static final String VALUE_GROUP = "group";
	public static final String VALUE_WORLD = "world";
	public static final String VALUE_GLOBAL = "global";
	public static final String VALUE_NEW_PLAYER_SPAWN = "newPlayerSpawn";
	
	public static final String COOLDOWN_BASE = "cooldown.";
	public static final String WARMUP_BASE = "warmup.";
	public static final String COST_BASE = "cost.";
	public static final String COST_VERBOSE = COST_BASE + "verbose";
	public static final String HOME_LIMITS_BASE = "homeLimits.";
	public static final String HOME_LIMITS_DEFAULT = "default";
	public static final String HOME_LIMITS_PER_WORLD = "perWorld";
	public static final String HOME_LIMITS_GLOBAL = "global";
	
	public static final String STRATEGY_HOME_THIS_WORLD_ONLY = "homeLocalWorld";
	public static final String STRATEGY_HOME_DEFAULT_WORLD = "homeDefaultWorld";
	public static final String STRATEGY_HOME_MULTI_WORLD = "homeMultiWorld";
	public static final String STRATEGY_HOME_NEAREST_HOME = "homeNearest";
	public static final String STRATEGY_HOME_SPECIFIC_WORLD = "homeSpecificWorld";
	public static final String STRATEGY_HOME_ANY_WORLD = "homeAnyWorld";
	
	public static final String STRATEGY_MODE_HOME_NORMAL = "modeHomeNormal";
	public static final String STRATEGY_MODE_HOME_BED_ONLY = "modeHomeBedOnly";
	public static final String STRATEGY_MODE_HOME_NO_BED = "modeHomeNoBed";
	public static final String STRATEGY_MODE_HOME_DEFAULT_ONLY = "modeHomeDefaultOnly";
	public static final String STRATEGY_MODE_HOME_ANY = "modeHomeAny";
	
	public static final String STRATEGY_SPAWN_WG_REGION = "spawnWGregion";
	public static final String STRATEGY_SPAWN_NEW_PLAYER = "spawnNewPlayer";
	public static final String STRATEGY_SPAWN_THIS_WORLD_ONLY = "spawnLocalWorld";
	public static final String STRATEGY_SPAWN_DEFAULT_WORLD = "spawnDefaultWorld";
	public static final String STRATEGY_SPAWN_SPECIFIC_WORLD = "spawnSpecificWorld";
	public static final String STRATEGY_SPAWN_NAMED_SPAWN = "spawnNamedSpawn";
	public static final String STRATEGY_SPAWN_GROUP = "spawnGroup";
	public static final String STRATEGY_SPAWN_GROUP_SPECIFIC_WORLD = "spawnGroupSpecificWorld";
	public static final String STRATEGY_NEAREST_SPAWN = "spawnNearest";
	
	public static final String STRATEGY_DEFAULT = "default";
}
