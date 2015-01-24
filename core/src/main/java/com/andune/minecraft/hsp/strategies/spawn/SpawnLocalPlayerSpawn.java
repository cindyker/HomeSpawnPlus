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

import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.entity.PlayerSpawn;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.strategy.*;

import javax.inject.Inject;

/**
 * @author andune
 */
@NoArgStrategy
public class SpawnLocalPlayerSpawn extends BaseStrategy {
    @Inject
    private PlayerSpawnDAO playerSpawnDAO;

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        StrategyResult result = null;

        Player p = context.getPlayer();
        String worldName = context.getEventLocation().getWorld().getName();
        log.debug("SpawnLocalPlayerSpawn.evaluate() worldName={}", worldName);

        PlayerSpawn ps = playerSpawnDAO.findByWorldAndPlayerName(worldName, p.getName());

        log.debug("SpawnLocalPlayerSpawn.evaluate() ps={}, ps.id={}", ps, (ps != null ? ps.getId() : "null"));
        if (ps != null) {
            if (ps.getSpawn() != null) {
                log.debug("SpawnLocalPlayerSpawn.evaluate() ps.getSpawn()={}, id={}", ps.getSpawn(), ps.getSpawn().getId());
                result = new StrategyResultImpl(ps.getSpawn());
            }
            else
                result = new StrategyResultImpl(ps.getLocation());
        }

        return result;
    }
}
