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
package com.andune.minecraft.hsp.strategy;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyResult;

/**
 * This interface was added primarily to prevent a circular reference in
 * dependency injection, which Guice can't work around unless there is an
 * interface to proxy temporarily. Although conceptually an interface would
 * also allow for different implementation options, I don't see a practical
 * use for that at this time given HSP's scope and the current Engine that
 * fulfills those needs. Thus Guice just binds this interface to the only
 * implementation that exists: StrategyEngineImpl
 * 
 * @author andune
 *
 */
public interface StrategyEngine {

    /** Convenience method for routines only interested in an output location (as
     * opposed to other result details).
     * 
     * @param event
     * @param player
     * @return
     */
    public StrategyResult getStrategyResult(StrategyContext context, String... args);

    public Location getStrategyLocation(EventType event, Player player, String... args);

    public StrategyResult getStrategyResult(String event, Player player, String... args);

    public StrategyResult getStrategyResult(EventType event, Player player, String... args);

    /** Given a StrategyContext, evaluate the strategies for that context.
     * 
     * @param context
     * 
     * @return API contract requires the result is never null
     */
    public StrategyResult evaluateStrategies(StrategyContext context);
}