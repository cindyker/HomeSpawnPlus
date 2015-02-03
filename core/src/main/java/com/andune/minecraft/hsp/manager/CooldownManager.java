/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.manager;

import com.andune.minecraft.commonlib.General;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.config.ConfigCooldown;
import com.andune.minecraft.hsp.config.ConfigCooldown.CooldownsPerWorld;
import com.andune.minecraft.hsp.server.api.Server;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class which manages player cooldowns.
 *
 * @author andune
 */
@Singleton
public class CooldownManager {
    // one full second in milliseconds
    private final static int ONE_SECOND = 1000;
    private final Logger log = LoggerFactory.getLogger(CooldownManager.class);

    private final Server server;
    private final ConfigCooldown config;
    private final Map<String, Long> cooldowns;
    private final General generalUtil;
    private final Permissions permissions;

    @Inject
    public CooldownManager(Server server, ConfigCooldown config, General generalUtil, Permissions permissions) {
        this.server = server;
        this.config = config;
        this.generalUtil = generalUtil;
        this.permissions = permissions;
        cooldowns = new Hashtable<String, Long>();
    }

    private boolean isExemptFromCooldown(Player p, String cooldown) {
        final CooldownNames cn = parseCooldownNames(cooldown);
        return permissions.isCooldownExempt(p, cn.baseName);
    }

    /**
     * Utility method for making sure a cooldown is available before we execute
     * a command.
     * <p/>
     * It also writes a message to the player letting them know they are still
     * in cooldown.
     *
     * @param p            the player
     * @param cooldownName the cooldown we are checking
     * @param sendMessage  if true, a message will be sent to the player explaining the
     *                     cooldown and time left.
     * @return true if cooldown is available, false if currently in cooldown
     *         period
     */
    public boolean cooldownCheck(Player p, String cooldownName, boolean sendMessage) {
        if (isExemptFromCooldown(p, cooldownName))
            return true;

        long cooldownTimeLeft = getCooldownRemaining(p, cooldownName);
        log.debug("cooldownCheck() p={}, cooldownName={}, cooldownTimeLeft={}", p, cooldownName, cooldownTimeLeft);
        if (cooldownTimeLeft > 0) {
            if (sendMessage) {
                p.sendMessage(server.getLocalizedMessage(HSPMessages.COOLDOWN_IN_EFFECT,
                        "name", cooldownName,
                        "time", generalUtil.displayTimeString(cooldownTimeLeft * ONE_SECOND,
                        false, null)));
            }
            log.debug("cooldownCheck() return false");
            return false;
        }

        log.debug("cooldownCheck() return true");
        return true;
    }

    public void setCooldown(final Player p, final String cooldown) {
        CooldownTime cdt = getCooldownTime(p, cooldown);

        if (cdt.cooldownTime > 0) {
            log.debug("saving cooldown {}, cooldownAmount = {}", cdt.cooldownName, cdt.cooldownTime);
            cooldowns.put(p.getName() + "." + cdt.cooldownName, System.currentTimeMillis());
        }
    }

    /**
     * Determine the number of remaining seconds for a given cooldown.
     *
     * @param p        the player whose cooldowns we are checking
     * @param cooldown the name of the cooldown being checked
     * @return the number of remaining seconds
     */
    public long getCooldownRemaining(final Player p, final String cooldown) {
        long cooldownRemaining = 0;
        log.debug("getCooldownRemaining(): p={} cooldown={}", p, cooldown);

        CooldownTime cdt = getCooldownTime(p, cooldown);
        int cooldownAmount = cdt.cooldownTime;
        if (cooldownAmount == 0)
            return 0;

        String key = p.getName() + "." + cdt.cooldownName;
        Long cooldownStartTime = cooldowns.get(key);
        log.debug("getCooldownRemaining(): key={}, cooldownStartTime={}", key, cooldownStartTime);
        if (cooldownStartTime != null) {
            long timeElapsed = (System.currentTimeMillis() - cooldownStartTime) / ONE_SECOND;
            log.debug("getCooldownRemaining(): key={}, timeElapsed={}", key, timeElapsed);

            if (timeElapsed > cooldownAmount) {
                log.debug("getCooldownRemaining(): expired cooldown removed, key={}", key);
                // cooldown expired, remove it
                cooldowns.remove(key);
            }
            else
                cooldownRemaining = cooldownAmount - timeElapsed;
        }

        log.debug("getCooldownRemaining(): cooldown remaining for key {} is {}", key, cooldownRemaining);
        return cooldownRemaining;
    }

    /**
     * cooldowns can can be as generic as just "home" or as specific as
     * "home-named.home1". This breaks down whatever input name is passed in and
     * breaks it down to it's component parts.
     */
    private CooldownNames parseCooldownNames(final String cooldown) {
        CooldownNames cn = new CooldownNames();

        // "home-named.home1"
        cn.fullName = cooldown;

        int index = cooldown.indexOf('.');

        if (index != -1) {
            // "home-named"
            cn.extendedName = cooldown.substring(0, index);
        }
        else
            cn.extendedName = cooldown;

        index = cn.extendedName.indexOf('-');
        if (index != -1) {
            // "home"
            cn.baseName = cn.extendedName.substring(0, index);
        }
        else
            cn.baseName = cn.extendedName;

        // up until this point, if just "home" was passed in, it will be in all
        // 3 variables. These checks will reduce it down to just the baseName.
        if (cn.fullName.equals(cn.baseName))
            cn.fullName = null;
        if (cn.fullName != null && cn.fullName.equals(cn.extendedName))
            cn.fullName = null;
        if (cn.extendedName.equals(cn.baseName))
            cn.extendedName = null;

        if (cn.fullName != null && cn.extendedName != null)
            cn.allNames = new String[]{cn.fullName, cn.extendedName, cn.baseName};
        else if (cn.extendedName != null)
            cn.allNames = new String[]{cn.extendedName, cn.baseName};
        else
            cn.allNames = new String[]{cn.baseName};

        return cn;
    }

    public boolean isCooldownSeparationEnabled(final String cooldown) {
        List<String> separateCooldowns = config.getSeparateCooldowns();
        return separateCooldowns.contains(cooldown);
    }

    /**
     * Return the time for the cooldown for the given player. This takes world and
     * permission-specific cooldowns into account. This also returns the cooldown
     * name, which can change if the admin wants the cooldown to be specific to
     * the world or permission.
     * <p/>
     * TODO: Cyclomatic Complexity of this method is high, should be refactored.
     *
     * @param player   the player
     * @param cooldown the cooldown name to check
     * @return the number of seconds the cooldown should have
     */
    private CooldownTime getCooldownTime(final Player player, final String cooldown) {
        final CooldownTime cdt = new CooldownTime();

        // default to existing cooldown name
        cdt.cooldownName = cooldown;

        final CooldownNames cn = parseCooldownNames(cooldown);
        log.debug("getCooldownTime(): cn.baseName={}, cn.extendedName={}, cn.fullName={}",
                cn.baseName, cn.extendedName, cn.fullName);

        if (cdt.cooldownTime <= 0) {
            Map<String, ConfigCooldown.CooldownsPerPermission> entries = config.getPerPermissionEntries();

            MATCH_FOUND:
            // iterate over each per-permission entry
            for (Map.Entry<String, ConfigCooldown.CooldownsPerPermission> entry : entries.entrySet()) {
                log.debug("processing per-permission entry {}", entry.getKey());

                // iterate over each possible cooldown name we are processing
                for (String name : cn.allNames) {
                    Integer value = entry.getValue().getCooldowns().get(name);

                    // only if there is a cooldown value for this name do we do any extra processing
                    if (value != null && value > 0) {
                        // ok now check to see if player has a permisson in the list
                        for (String perm : entry.getValue().getPermissions()) {
                            log.debug("processing per-permission permission {}", perm);
                            if (player.hasPermission(perm)) {
                                cdt.cooldownTime = value;

                                // change cooldown name if per-permissions flag is enabled to do so
                                if (entry.getValue().isCooldownPerPermission())
                                    cdt.cooldownName = cooldown + "." + perm;
                                else
                                    cdt.cooldownName = name;

                                log.debug("player {} has permission, match found", player);
                                break MATCH_FOUND;
                            }
                        }
                    }
                }
            }

            log.debug("getCooldownTime(): post-permission cooldown={}, name={}", cdt.cooldownTime, cdt.cooldownName);
        }

        // if cooldownTime is still 0, then check for world-specific cooldown
        if (cdt.cooldownTime <= 0) {
            final String worldName = player.getWorld().getName();

            for (String name : cn.allNames) {
                log.debug("getCooldownTime(): checking world cooldown config for world {}, cooldown {}", worldName, name);
                cdt.cooldownTime = config.getPerWorldCooldown(name, worldName);

                if (cdt.cooldownTime > 0) {
                    // change cooldown name if per-world flag is enabled
                    if (config.isCooldownPerWorld(worldName))
                        cdt.cooldownName = cooldown + "." + worldName;
                    else
                        cdt.cooldownName = name;

                    break;
                }
            }

            log.debug("getCooldownTime(): post-world world={}, cooldown={}, name={}",
                    worldName, cdt.cooldownTime, cdt.cooldownName);
        }

        // if cooldownTime is still 0, then check global cooldown setting
        if (cdt.cooldownTime <= 0) {
            for (String name : cn.allNames) {
                log.debug("getCooldownTime(): checking global cooldown config for cooldown {}", name);

                cdt.cooldownTime = config.getGlobalCooldown(name);
                if (cdt.cooldownTime > 0) {
                    cdt.cooldownName = name;
                    break;
                }
            }
            log.debug("getCooldownTime(): post-global cooldown={}, name={}", cdt.cooldownTime, cdt.cooldownName);
        }

        return cdt;
    }

    /**
     * Should be called when a player dies. Will determine if the player's
     * cooldowns should be reset based on config options and location.
     * <p/>
     *
     * @param player the player
     */
    public void onDeath(Player player) {
        boolean resetOnDeath = false;
        // if we find a match in any config option, matchFound is set to true
        // to stop any further config processing.
        boolean matchFound = false;

        // check permission-specific settings
        Map<String, ConfigCooldown.CooldownsPerPermission> entries = config.getPerPermissionEntries();
        MATCH_FOUND:
        // iterate over each per-permission entry. We're looking for the first permission to
        // match the player, and that one (if found), will control the resetOnDeath flag
        for (Map.Entry<String, ConfigCooldown.CooldownsPerPermission> entry : entries.entrySet()) {
            // ok now check to see if player has a permisson in the list
            for (String perm : entry.getValue().getPermissions()) {
                if (player.hasPermission(perm)) {
                    matchFound = true;

                    if (entry.getValue().hasResetOnDeath())
                        resetOnDeath = entry.getValue().isResetOnDeath();
                    break MATCH_FOUND;
                }
            }
        }

        // check for world-specific setting
        if (!matchFound) {
            final String worldName = player.getLocation().getWorld().getName();
            CooldownsPerWorld cpw = config.getPerWorldEntry(worldName);
            if (cpw != null && cpw.hasResetOnDeath()) {
                resetOnDeath = cpw.isResetOnDeath();
                matchFound = true;
            }
        }

        // no permission or world-specific entry found, check default setting
        if (!matchFound)
            resetOnDeath = config.isGlobalResetOnDeath();

        // If resetonDeath flag is set, remove all cooldowns for this player
        if (resetOnDeath) {
            // cooldowns for this player will all start with this string
            final String playerBase = player.getName() + ".";

            for (Iterator<String> i = cooldowns.keySet().iterator(); i.hasNext(); ) {
                String key = i.next();
                if (key.startsWith(playerBase))
                    i.remove();
            }
        }
    }

    private static class CooldownTime {
        private int cooldownTime = 0;
        private String cooldownName;
    }

    /**
     * When a cooldown name is passed in, it can be of the full form "sethome-named.home1".
     * It will be broken down into it's component parts and put into the members of this
     * class for processing by the cooldown algorithms.
     *
     * @author andune
     */
    private static class CooldownNames {
        // "home"
        private String baseName = null;
        // "home-named"
        private String extendedName = null;
        // "home-named.home1" or just "home.home1"
        private String fullName = null;
        private String allNames[];
    }
}
