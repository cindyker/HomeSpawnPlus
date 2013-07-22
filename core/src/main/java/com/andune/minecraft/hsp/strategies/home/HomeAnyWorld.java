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
package com.andune.minecraft.hsp.strategies.home;

import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.strategy.*;

import java.util.Set;

/**
 * @author andune
 */
@NoArgStrategy
public class HomeAnyWorld extends HomeStrategy {
    @Override
    public StrategyResult evaluate(StrategyContext context) {
        // get the Set of homes for this player for ALL worlds
        Set<? extends Home> homes = homeDAO.findHomesByPlayer(context.getPlayer().getName());
        log.debug("HomeAnyWorld: homes = {}", homes);

        Home home = null;

        if (homes != null && homes.size() > 0) {
            for (Home h : homes) {
                // skip this home if MODE_HOME_REQUIRES_BED is set and no bed is nearby
                if (context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(h)) {
                    logVerbose(" Home ", h, " skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
                    continue;
                }

                // in "normal" or "any" mode, we just grab the first home we find
                if (context.isDefaultModeEnabled() ||
                        context.isModeEnabled(StrategyMode.MODE_HOME_ANY)) {
                    home = h;
                    break;
                } else if (context.isModeEnabled(StrategyMode.MODE_HOME_BED_ONLY) && h.isBedHome()) {
                    home = h;
                    break;
                } else if (context.isModeEnabled(StrategyMode.MODE_HOME_DEFAULT_ONLY) && h.isDefaultHome()) {
                    home = h;
                    break;
                }
            }
        }

        return resultFactory.create(home);
    }

    @Override
    public String getStrategyConfigName() {
        return "homeAnyWorld";
    }

}
