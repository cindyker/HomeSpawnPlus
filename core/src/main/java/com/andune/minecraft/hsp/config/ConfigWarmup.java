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
import com.andune.minecraft.hsp.config.ConfigWarmup.WarmupsPerPermission;
import com.andune.minecraft.hsp.config.ConfigWarmup.WarmupsPerWorld;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andune
 */
@Singleton
@ConfigOptions(fileName = "warmup.yml", basePath = "warmup")
public class ConfigWarmup extends ConfigPerXBase<WarmupsPerPermission, WarmupsPerWorld> implements Initializable {
    /**
     * Determine if warmups are enabled.
     *
     * @return true if warmups are enabled.
     */
    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }

    /**
     * Determine if warmups should be canceled when a player is damaged.
     *
     * @return true if warmups should be canceled on damage
     */
    public boolean isCanceledOnDamage() {
        return super.getBoolean("onDamageCancel");
    }

    /**
     * Determine if warmups should be canceled when a player moves.
     *
     * @return true if warmups should be canceled on movement
     */
    public boolean isCanceledOnMovement() {
        return super.getBoolean("onMoveCancel");
    }

    /**
     * For a given world & warmup combo, return the applicable warmup timer.
     *
     * @param warmup
     * @param world
     * @return
     */
    public int getPerWorldWarmup(String warmup, String world) {
        WarmupsPerWorld warmups = perWorldEntries.get(world);
        if(warmups != null)
        {
            Map<String, Integer> warmupMap = warmups.getWarmups();
            if(warmupMap != null && warmupMap.containsKey(warmup))
               return warmupMap.get(warmup);
        }
        return 0;
    }

    /**
     * For a given warmup, return it's global warmup time (exclusive of
     * per-permission and per-world warmup values).
     *
     * @param warmup
     * @return
     */
    public int getGlobalWarmup(String warmup) {
        final int warmupValue = super.getInt(warmup);
        return warmupValue > 0 ? warmupValue : 0;
    }

    public class WarmupsPerPermission extends PerPermissionEntry {
        Map<String, Integer> warmups = new HashMap<String, Integer>();

        public Map<String, Integer> getWarmups() {
            return warmups;
        }

        public void setValue(String key, Object o) {
            warmups.put(key, (Integer) o);
        }

        public void finishedProcessing() {
            warmups = Collections.unmodifiableMap(warmups);
        }
    }

    public class WarmupsPerWorld extends PerWorldEntry {
        Map<String, Integer> warmups = new HashMap<String, Integer>();

        public Map<String, Integer> getWarmups() {
            return warmups;
        }

        public void setValue(String key, Object o) {
            // warmupPerWorld option is deprecated, in fact it never actually
            // did anything at all, so we just silently ignore it
            if (key.equals("warmupPerWorld"))
                return;

            warmups.put(key, (Integer) o);
        }

        public void finishedProcessing() {
            warmups = Collections.unmodifiableMap(warmups);
        }
    }

    @Override
    protected WarmupsPerPermission newPermissionEntry() {
        return new WarmupsPerPermission();
    }

    @Override
    protected WarmupsPerWorld newWorldEntry() {
        return new WarmupsPerWorld();
    }
}
