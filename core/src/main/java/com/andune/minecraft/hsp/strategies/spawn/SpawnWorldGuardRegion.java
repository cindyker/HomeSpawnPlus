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
package com.andune.minecraft.hsp.strategies.spawn;

import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;
import com.andune.minecraft.hsp.strategy.*;

import javax.inject.Inject;

/**
 * Spawn inside the WorldGuard region using the WorldGuard flag.
 * <p/>
 * This strategy requires WorldGuard and is specific to Bukkit.
 *
 * @author andune
 */
@NoArgStrategy
public class SpawnWorldGuardRegion extends BaseStrategy {
    @Inject
    private WorldGuard worldGuard;

    public SpawnWorldGuardRegion() {
        if( log.isDebugEnabled() ) {
            log.debug("SpawnWorldGuardRegion constructor() invoked. worldGuard={}", worldGuard);
            try {
                throw new Exception();
            } catch(Exception e) {
                log.debug("SpawnWorldGuardRegion constructor() stack trace: ", e);
            }
        }
    }

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        if (!worldGuard.isEnabled()) {
            log.warn("Attempted to use " + getStrategyConfigName() + " without WorldGuard installed. Strategy ignored.");
            return null;
        }

        return new StrategyResultImpl(worldGuard.getWorldGuardSpawnLocation(context.getEventLocation()));
    }

    @Override
    public void validate() throws StrategyException {
        log.debug("SpawnWorldGuardRegion.validate worldGuard={}", worldGuard);
        if (!worldGuard.isEnabled())
            throw new StrategyException("Attempted to use " + getStrategyConfigName() + " without WorldGuard installed. Strategy ignored.");
    }

    @Override
    public String getStrategyConfigName() {
        return "spawnWGregion";
    }

}
