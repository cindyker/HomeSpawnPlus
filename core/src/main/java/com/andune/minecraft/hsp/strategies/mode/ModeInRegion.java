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
package com.andune.minecraft.hsp.strategies.mode;

import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;
import com.andune.minecraft.hsp.strategy.ModeStrategyImpl;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyException;
import com.andune.minecraft.hsp.strategy.StrategyMode;

import javax.inject.Inject;

/**
 * @author andune
 */
@OneArgStrategy
public class ModeInRegion extends ModeStrategyImpl {
    @Inject
    WorldGuard worldGuard;
    private String regionName;

    public ModeInRegion(String regionName) {
        this.regionName = regionName;
    }

    public String getRegionName() {
        return regionName;
    }

    @Override
    public void validate() throws StrategyException {
        if (!worldGuard.isEnabled())
            throw new StrategyException("Attempt to use " + getStrategyConfigName() + " strategy but WorldGuard is not installed");

        if (regionName == null)
            throw new StrategyException("Error validating strategy " + getStrategyConfigName() + ": strategy argument is null");
    }

    @Override
    public StrategyMode getMode() {
        return StrategyMode.MODE_IN_REGION;
    }

    @Override
    public boolean isAdditive() {
        return false;
    }
}
