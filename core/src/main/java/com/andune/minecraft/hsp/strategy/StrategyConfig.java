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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.server.api.Player;

/**
 * Interface class that exists so Guice can create a temporary proxy to avoid
 * circular dependencies. Only one implementation exists: StrategyConfigImpl
 * 
 * @author andune
 *
 */
public interface StrategyConfig extends Initializable {

    /** Given a specific event type, return the default strategy chain associated with
     * that event.
     * The returned set will use a Set implementation that guarantees ordering and whose
     * order is consistent with that of the underlying config.
     * 
     * @param event
     * @return
     */
    public Set<Strategy> getDefaultStrategies(String event);

    /** Given a specific event type and player, return all matching permission strategies.
     * The returned set will use a Set implementation that guarantees ordering and whose
     * order is consistent with that of the underlying config.
     * 
     * @param event
     * @param player
     * @return guaranteed to not be null
     */
    public List<Set<Strategy>> getPermissionStrategies(String event,
            Player player);

    /** Given a specific event type and world, return all matching world strategies.
     * The returned set will use a Set implementation that guarantees ordering and whose
     * order is consistent with that of the underlying config.
     * 
     * @param event
     * @param player
     * @return null if no world strategies exist for the world
     */
    public Set<Strategy> getWorldStrategies(String event, String world);

    /** Metrics: return total number of Permission-related strategies.
     * 
     * @return
     */
    public int getPermissionStrategyCount();

    /** Metrics: return total number of World-related strategies.
     * 
     * @return
     */
    public int getWorldStrategyCount();

    /** Metrics: return total number of default strategies.
     * 
     * @return
     */
    public int getDefaultStrategyCount();

    /** For metrics, return the count of each strategy that is in use.
     * 
     * @return
     */
    public Map<String, Integer> getStrategyCountMap();

}