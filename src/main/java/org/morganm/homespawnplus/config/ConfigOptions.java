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
	public static final String WARN_CONFLICTS = "core.warnConflicts";
	public static final String WARN_NULL_STRATEGY = "core.warnNullStrategy";
	public static final String WARN_LOCATION_CHANGE = "core.warnLocationChange";
	public static final String WARN_PERFORMANCE_MILLIS = "core.warnPerformanceMillis";
	
	public static final String STORAGE_TYPE = "core.storage";
	public static final String DEFAULT_PERMISSIONS = "core.defaultPermissions";
	public static final String DEFAULT_WORLD = "core.defaultWorld";
	public static final String ENABLE_HOME_BEDS = "core.bedsethome";
	public static final String BED_HOME_2CLICKS = "core.bedhome2clicks";
	public static final String BED_HOME_MUST_BE_NIGHT = "core.bedHomeMustBeNight";
	public static final String BED_HOME_NEVER_DISPLAY_NIGHT_MSG = "core.bedHomeNeverDisplayNightMessage";
	public static final String BED_HOME_ORIGINAL_BEHAVIOR = "core.bedHomeOriginalBehavior";
	public static final String ENABLE_RECORD_LAST_LOGOUT = "core.recordLastLogout";
//	public static final String ENABLE_GROUP_SPAWN = "core.groupSpawnEnabled";
	public static final String SETTING_WORLD_OVERRIDE = "core.override_world";
	public static final String EVENT_PRIORITY = "core.eventPriority";
	public static final String LAST_HOME_IS_DEFAULT = "core.lastHomeIsDefault";
	public static final String RELOAD_CHUNK_ON_TELEPORT = "core.reloadChunkOnTeleport";
	public static final String NEW_PLAYER_STRATEGY = "core.newPlayerStrategy";
	public static final String SAFE_TELEPORT = "core.safeTeleport";
	public static final String SPAWN_NAMED_PERMISSIONS = "core.spawnNamedPermissions";
	public static final String BEDHOME_OVERWRITES_DEFAULT = "core.bedHomeOverwritesDefault";

	public static final String DYNMAP_INTEGRATION_ENABLED = "dynmap.enabled";
	public static final String DYNMAP_INTEGRATION_UPDATE_PERIOD = "dynmap.update.period";
	public static final String DYNMAP_INTEGRATION_HOMES = "dynmap.layer.homes";
	public static final String DYNMAP_INTEGRATION_HOMES_ENABLED = DYNMAP_INTEGRATION_HOMES + ".enable";
	public static final String DYNMAP_INTEGRATION_SPAWNS = "dynmap.layer.spawns";
	public static final String DYNMAP_INTEGRATION_SPAWNS_ENABLED = DYNMAP_INTEGRATION_SPAWNS + ".enable";

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
	
//	public static final String SETTING_JOIN_BEHAVIOR = "onJoin";
//	public static final String SETTING_DEATH_BEHAVIOR = "onDeath";
//	public static final String SETTING_SPAWN_CMD_BEHAVIOR = "onSpawnCommand";
//	public static final String SETTING_GROUPSPAWN_CMD_BEHAVIOR = "onGroupSpawnCommand";
//	public static final String SETTING_HOME_CMD_BEHAVIOR = "onHomeCommand";
//	public static final String SETTING_HOME_NAMED_CMD_BEHAVIOR = "onNamedHomeCommand";
	
	public static final String HOME_INVITE_TIMEOUT = "homeInvite.timeout";
	public static final String HOME_INVITE_ALLOW_BEDHOME = "homeInvite.allowBedHomeInvites";
	public static final String HOME_INVITE_USE_HOME_COOLDOWN = "homeInvite.useHomeCooldown";
	public static final String HOME_INVITE_USE_HOME_WARMUP = "homeInvite.useHomeWarmup";

	public static final String VALUE_DEFAULT = "default";
	public static final String VALUE_HOME = "home";
	public static final String VALUE_MULTIHOME = "multihome";
	public static final String VALUE_GROUP = "group";
	public static final String VALUE_WORLD = "world";
	public static final String VALUE_GLOBAL = "global";
	public static final String VALUE_NEW_PLAYER_SPAWN = "newPlayerSpawn";
	
	public static final String COOLDOWN_BASE = "cooldown.";
//	public static final String COOLDOWN_PER_HOME = COOLDOWN_BASE + "cooldownPerHome";
//	public static final String COOLDOWN_PER_HOME_OVERRIDE = COOLDOWN_BASE + "cooldownPerHomeOverride";
	public static final String COOLDOWN_SEPARATION = "cooldown.separation";
	public static final String COOLDOWN_RESET_ON_DEATH = "resetOnDeath";
	public static final String WARMUP_BASE = "warmup.";
	public static final String COST_BASE = "cost.";
	public static final String COST_VERBOSE = COST_BASE + "verbose";
	public static final String COST_SETHOME_MULTIPLIER = "cost.sethome-multiplier";
	public static final String HOME_LIMITS_BASE = "homeLimits.";
	public static final String HOME_LIMITS_DEFAULT = "default";
	public static final String HOME_LIMITS_PER_WORLD = "perWorld";
	public static final String HOME_LIMITS_GLOBAL = "global";
	public static final String SINGLE_GLOBAL_HOME = HOME_LIMITS_BASE + "singleGlobalHome";
	
	public static final String STRATEGY_DEFAULT = "default";
	
	public enum NewPlayerStrategy {
		ORIGINAL,
		BUKKIT,
		PLAYER_DAT
	}
}
