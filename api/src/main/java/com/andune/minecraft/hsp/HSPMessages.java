/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp;

/**
 * This is the only class in this package specific to HSP. Just provide your
 * own and use it in your own implementation.
 *
 * @author andune
 */
public enum HSPMessages {
    // v1.3.1
    HOME_BED_SET,
    HOME_BED_ONE_MORE_CLICK,

    CMD_HOME_NO_OTHERWORLD_PERMISSION,
    CMD_HOME_NO_HOME_ON_WORLD,
    CMD_HOME_NO_NAMED_HOME_PERMISSION,
    CMD_HOME_NO_NAMED_HOME_FOUND,
    NO_HOME_FOUND,

    CMD_SETHOME_NO_USE_RESERVED_NAME,
    CMD_SETHOME_NO_NAMED_HOME_PERMISSION,
    CMD_SETHOME_HOME_SET,
    CMD_SETHOME_DEFAULT_HOME_SET,

    CMD_SPAWN_NO_SPAWN_FOUND,

    CMD_GROUPSPAWN_NO_GROUPSPAWN_FOR_GROUP,

    CMD_HOMEDELETE_ERROR_DELETING_OTHER_HOME,
    CMD_HOMEDELETE_HOME_DELETED,
    CMD_HOMEDELETE_DEFAULT_HOME_DELETED,
    CMD_HOMEDELETE_NO_HOME_FOUND,
    CMD_HOMEDELETE_NO_DEFAULT_HOME_FOUND,

    CMD_HOMEDELETEOTHER_HOME_DELETED,
    CMD_HOMEDELETEOTHER_DEFAULT_HOME_DELETED,
    CMD_HOMEDELETEOTHER_NO_HOME_FOUND,
    CMD_HOMEDELETEOTHER_NO_DEFAULT_HOME_FOUND,

    CMD_HOMELIST_ALL_WORLDS,
    CMD_HOMELIST_FOR_WORLD,
    CMD_HOMELIST_NO_HOMES_FOUND,

    CMD_HOMEOTHER_TELEPORTING,

    CMD_SETDEFAULTHOME_SPECIFY_HOMENAME,
    CMD_SETDEFAULTHOME_HOME_CHANGED,

    CMD_SETDEFAULTSPAWN_SPECIFY_NAME,
    CMD_SETDEFAULTSPAWN_SPAWN_CHANGED,

    CMD_SETFIRSTSPAWN_SET,

    CMD_SETHOMEOTHER_HOME_SET,
    CMD_SETHOMEOTHER_DEFAULT_HOME_SET,

    CMD_SETSPAWN_SET_NAMED_SUCCESS,
    CMD_SETSPAWN_SET_SUCCESS,

    CMD_SPAWNDELETE_NO_SPAWN_FOUND,
    CMD_SPAWNDELETE_SPAWN_DELETED,

    CMD_SPAWNLIST_ALL_WORLDS,
    CMD_SPAWNLIST_FOR_WORLD,
    CMD_SPAWNLIST_NO_SPAWNS_FOUND,

    CMD_WARMUP_FINISHED,

    CMD_HSP_ERROR_RELOADING,
    CMD_HSP_CONFIG_RELOADED,
    CMD_HSP_DATA_RELOADED,
    CMD_HSP_DATA_BACKED_UP,
    CMD_HSP_DATA_BACKUP_ERROR,
    CMD_HSP_DATA_RESTORE_USAGE,
    CMD_HSP_DATA_RESTORE_SUCCESS,
    CMD_HSP_DATA_RESTORE_NO_FILE,

    WARMUP_STARTED,
    WARMUP_ALREADY_PENDING,
    WARMUP_CANCELLED_DAMAGE,
    WARMUP_CANCELLED_YOU_MOVED,

    COOLDOWN_IN_EFFECT,

    COST_CHARGED,
    COST_ERROR,
    COST_INSUFFICIENT_FUNDS,

    NO_PERMISSION,
    TOO_MANY_ARGUMENTS,

    GENERIC_NAME,
    GENERIC_GROUP,
    GENERIC_DEFAULT,
    GENERIC_WORLD_DEFAULT,

    // v1.5
    GENERIC_ERROR,
    NO_HOME_INVITE_FOUND,

    CMD_HOME_INVITE_NO_PLAYER_FOUND,
    CMD_HOME_INVITE_INVITE_SENT,
    CMD_HOME_INVITE_EXPIRE_TIME_SET,
    CMD_HOME_INVITE_BAD_TIME,
    CMD_HOME_INVITE_HOME_NOT_FOUND,
    CMD_HOME_INVITE_INVITE_RECEIVED,
    CMD_HOME_INVITE_NOT_ALLOWED,
    CMD_HOME_INVITE_NO_HOME_SPECIFIED,
    PLAYER_NOT_FOUND,

    CMD_SETMAPSPAWN_SET_SUCCESS,
    TEMP_HOMEINVITE_RECEIVED,
    CMD_HIACCEPT_NO_INVITE,
    CMD_HIACCEPT_TELEPORTED,

    // v1.5.6
    CMD_PERM_CHECK_TRUE,
    CMD_PERM_CHECK_FALSE,

    // v1.7
    NO_LOCATION_FOUND,

    // v1.7.3
    ERROR_ID_NUMBER_REQUIRED,
    HOMEINVITE_DELETED,
    HOMEINVITE_ID_NOT_FOUND,

    CMD_HOME_TELEPORTING,
    CMD_HOME_NAMED_TELEPORTING,
    CMD_HOME_BED_TELEPORTING,
    CMD_SPAWN_TELEPORTING,
    CMD_SPAWN_NAMED_TELEPORTING,
    CMD_HOME_INVITE_TELEPORTING,
    CMD_GROUPSPAWN_TELEPORTING,

    CMD_HOME_USAGE,
    CMD_HOMEINVITE_USAGE,
    CMD_GROUPSPAWN_USAGE,
    CMD_HOMEDELETE_USAGE,
    CMD_GROUPQUERY_USAGE,
    CMD_PERMCHECK_USAGE,
    CMD_HOMEDELETEOTHER_USAGE,
    CMD_HOMELIST_USAGE,
    CMD_HOMELISTOTHER_USAGE,
    CMD_HOME_INVITE_DELETE_USAGE,
    CMD_HOME_INVITE_TELEPORT_USAGE,
    CMD_HOMEOTHER_USAGE,
    CMD_HSP_USAGE,
    CMD_SETDEFAULTHOME_USAGE,
    CMD_SETDEFAULTSPAWN_USAGE,
    CMD_SETGROUPSPAWN_USAGE,
    CMD_SETHOME_USAGE,
    CMD_SETHOMEOTHER_USAGE,
    CMD_SETSPAWN_USAGE,
    CMD_SPAWNLIST_USAGE,
    CMD_SPAWNDELETE_USAGE,

    // v1.7.4
    CMD_SPAWNRENAME_USAGE,
    CMD_SPAWNRENAME_SPAWN_RENAMED,
    CMD_HOMERENAME_USAGE,
    CMD_HOMERENAME_HOME_RENAMED,
    CMD_HOMERENAME_ERROR_RENAMING_OTHER_HOME,
    CMD_HOMERENAME_NAMED_HOMES_ONLY,
    NO_NAMED_HOME_FOUND,

    // v1.7.5
    CMD_SETHOME_BAD_Y_LOCATION,

    // 2.0
    GENERIC_PLAYER_NOT_FOUND,
    CMD_HSP_PURGE_WRONG_ARGUMENTS,
    CMD_HSP_PURGE_REQUIRES_CONFIRM,
    CMD_HSP_PURGE_PLAYER_TIME,
    CMD_HSP_PURGE_RESULTS,
    CMD_HSP_PURGE_PLAYER_WORLD,
    CMD_HSP_PURGE_STARTING_ASYNC,
    FEATURE_NOT_IMPLEMENTED,
    CMD_HSP_UBER_USAGE,
    CMD_HSPCONVERT_USAGE,
    CMD_HSPDEBUG_USAGE,
}
