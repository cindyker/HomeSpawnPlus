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
package com.andune.minecraft.hsp.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigEconomy.PerPermissionEconomyEntry;
import com.andune.minecraft.hsp.config.ConfigEconomy.PerWorldEconomyEntry;

/** Configuration related to economy costs.
 * 
 * @author morganm
 *
 */
@Singleton
@ConfigOptions(fileName="economy.yml", basePath="cost")
public class ConfigEconomy extends ConfigPerXBase<PerPermissionEconomyEntry, PerWorldEconomyEntry> implements Initializable {
    /**
     * Determine whether or not economy has been enabled by admin.
     * 
     * @return true if economy is enabled
     */
    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }

    /**
     * Determine if we should print messages when charges happen.
     * 
     * @return true if verbose messages should be used
     */
    public boolean isVerboseOnCharge() {
        return super.getBoolean("verbose");
    }
    
    /**
     * If sethome-multiplier is non-zero, then each additional home past
     * the first (globally) will cost this much more. Example: sethome cost
     * is 500, sethome-multiplier is 1.5. First home will cost 500, second
     * will cost (500*1.5) = 750. 3rd home will cost (500*1.5*1.5) = 1125.
     * 4th home is (500*1.5*1.5*1.5) = 1687.5, and so on.
     * 
     * @return
     */
    public double getSethomeMultiplier() {
        return super.getDouble("sethome-multiplier");
    }

    /**
     * For a given command, return it's global cost (exclusive of
     * per-permission and per-world cost values).
     * 
     * @param cooldown
     * @return
     */
    public int getGlobalCost(String command) {
        final int cost = super.getInt(command);
        return cost > 0 ? cost : 0;
    }

    /**
     * Return world-specific costs for a given command.
     * 
     * @return The cost for the command, or null if no cost was defined
     */
    public Integer getWorldSpecificCost(String world, String command) {
        PerWorldEconomyEntry pwee = getPerWorldEntry(world);
        return pwee != null ? pwee.getCosts().get(command) : null;
    }

    public class PerPermissionEconomyEntry extends PerPermissionEntry {
        protected Map<String, Integer> costs = new HashMap<String, Integer>();
        public Map<String, Integer> getCosts() { return costs; }

        @Override
        void setValue(String key, Object o) {
            costs.put(key, (Integer) o);
        }

        public void finishedProcessing() {
            costs = Collections.unmodifiableMap(costs);
        }
    }

    public class PerWorldEconomyEntry extends PerWorldEntry {
        protected Map<String, Integer> costs = new HashMap<String, Integer>();
        public Map<String, Integer> getCosts() { return costs; }

        @Override
        void setValue(String key, Object o) {
            costs.put(key, (Integer) o);
        }

        public void finishedProcessing() {
            costs = Collections.unmodifiableMap(costs);
        }
    }

    @Override
    protected PerPermissionEconomyEntry newPermissionEntry() {
        return new PerPermissionEconomyEntry();
    }

    @Override
    protected PerWorldEconomyEntry newWorldEntry() {
        return new PerWorldEconomyEntry();
    }
}
