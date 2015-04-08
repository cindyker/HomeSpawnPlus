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
package com.andune.minecraft.hsp.config;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.hsp.config.ConfigCooldown.CooldownsPerPermission;
import com.andune.minecraft.hsp.config.ConfigCooldown.CooldownsPerWorld;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andune
 */
@Singleton
@ConfigOptions(fileName = "cooldown.yml", basePath = "cooldown")
public class ConfigCooldown extends ConfigPerXBase<CooldownsPerPermission, CooldownsPerWorld> implements Initializable {
    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }

    @Override
    protected CooldownsPerPermission newPermissionEntry() {
        return new CooldownsPerPermission();
    }

    @Override
    protected CooldownsPerWorld newWorldEntry() {
        return new CooldownsPerWorld();
    }

    /**
     * Return a list of cooldowns that are on separate timers.
     *
     * @return
     */
    public List<String> getSeparateCooldowns() {
        return super.getStringList("separation");
    }

    /**
     * Determine if global cooldowns should reset on player death.
     *
     * @return
     */
    public boolean isGlobalResetOnDeath() {
        return super.getBoolean("resetOnDeath");
    }

    /**
     * For a given cooldown, return it's global cooldown time (exclusive of
     * per-permission and per-world cooldown values).
     *
     * @param cooldown
     * @return
     */
    public int getGlobalCooldown(String cooldown) {
        final int cooldownValue = super.getInt(cooldown);
        return cooldownValue > 0 ? cooldownValue : 0;
    }

    /**
     * Determine if cooldown per-world is enabled for a given world.
     *
     * @param world
     * @return
     */
    public boolean isCooldownPerWorld(String world) {
        CooldownsPerWorld entry = super.perWorldEntries.get(world);
        return entry != null ? entry.isCooldownPerWorld() : false;
    }

    /**
     * For a given world & cooldown combo, return the applicable timer.
     *
     * @param cooldown
     * @param world
     * @return
     */
    public int getPerWorldCooldown(String cooldown, String world) {
        CooldownsPerWorld cooldowns = super.perWorldEntries.get(world);
        if(cooldowns != null)
        {
            Map<String, Integer> cooldownMap = cooldowns.getCooldowns();
            if(cooldownMap != null && cooldownMap.containsKey(cooldown))
               return cooldownMap.get(cooldown);
        }
        return 0;
    }

    private static class Entry {
        boolean hasCooldownPerX = false;
        boolean isCooldownPerX = false;
        boolean hasResetOnDeath = false;
        boolean isResetOnDeath = false;
        Map<String, Integer> cooldowns = new HashMap<String, Integer>();

        public void setValue(String key, Object o) {
            if (key.startsWith("cooldownPer")) {   // cooldownPerPermission and cooldownPerWorld
                hasCooldownPerX = true;
                isCooldownPerX = (Boolean) o;
            } else if (key.equalsIgnoreCase("resetOnDeath")) {
                hasResetOnDeath = true;
                isResetOnDeath = (Boolean) o;
            } else {
                cooldowns.put(key, (Integer) o);
            }
        }

        public void finishedProcessing() {
            cooldowns = Collections.unmodifiableMap(cooldowns);
        }
    }

    public static class CooldownsPerPermission extends PerPermissionEntry {
        private Entry entry = new Entry();

        public boolean hasCooldownPerPermission() {
            return entry.hasCooldownPerX;
        }

        public boolean isCooldownPerPermission() {
            return entry.isCooldownPerX;
        }

        public boolean hasResetOnDeath() {
            return entry.hasResetOnDeath;
        }

        public boolean isResetOnDeath() {
            return entry.isResetOnDeath;
        }

        public Map<String, Integer> getCooldowns() {
            return entry.cooldowns;
        }

        @Override
        public void setValue(String key, Object o) {
            entry.setValue(key, o);
        }

        @Override
        public void finishedProcessing() {
            entry.finishedProcessing();
        }
    }

    public static class CooldownsPerWorld extends PerWorldEntry {
        private Entry entry = new Entry();

        public boolean hasCooldownPerWorld() {
            return entry.hasCooldownPerX;
        }

        public boolean isCooldownPerWorld() {
            return entry.isCooldownPerX;
        }

        public boolean hasResetOnDeath() {
            return entry.hasResetOnDeath;
        }

        public boolean isResetOnDeath() {
            return entry.isResetOnDeath;
        }

        public Map<String, Integer> getCooldowns() {
            return entry.cooldowns;
        }

        @Override
        public void setValue(String key, Object o) {
            entry.setValue(key, o);
        }

        @Override
        public void finishedProcessing() {
            entry.finishedProcessing();
        }
    }
}
