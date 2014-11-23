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

import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.strategy.*;

import java.util.Collection;

/**
 * This strategy is conceptually similar to HomeLocalWorld except that it will
 * also consider associated worlds. For example, if this strategy were run with
 * a player on world "world" that had a home on "world_nether" but not one on
 * "world", this strategy would find the home on their associated home_nether
 * world and send them there.
 *
 * @author andune
 */
@NoArgStrategy
public class HomeAssociatedWorld extends HomeStrategy {
    @Override
    public StrategyResult evaluate(StrategyContext context) {
        Home result = null;

        World world = context.getEventLocation().getWorld();
        if (world != null) {
            // first we check the world the strategy is based on.
            result = super.getModeHome(context, world.getName());

            // failing that, parent world is next
            if (result == null) {
                World parent = world.getParentWorld();
                if (parent != null) {
                    result = super.getModeHome(context, parent.getName());
                }

                // failing that, children are next
                if (result == null) {
                    Collection<World> children = world.getChildWorlds();
                    for (World child : children) {
                        result = super.getModeHome(context, child.getName());
                        if (result != null)
                            break;
                    }
                }
            }
        }

        return new StrategyResultImpl(result);
    }
}
